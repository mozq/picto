/*!
 * Picto
 * Copyright 2016 Mozq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.mozq.picto.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.mozq.appsettings.AppSettings;
import net.mozq.appsettings.AppSettingsDirectory;
import net.mozq.picto.App;
import net.mozq.picto.LegacyPictoFileImport;
import net.mozq.picto.util.FileNameSupport;

/**
 * Manages presets (named, saved snapshots of {@link MainFrameSettings}) and the settings/presets/history
 * export-import archive: the Presets menu, saving/loading/renaming/deleting presets, resetting to defaults,
 * and importing or exporting a {@code .picto} data archive (including the legacy pre-archive format).
 */
final class PresetManager {
	private static final String PRESETS_DIR_NAME = "presets";
	static final String PRESET_FILE_NAME_EXT = "conf";
	static final int PRESET_SHORTCUT_COUNT = 9;
	private static final String PICTO_FILE_NAME_EXT = "picto";

	private final Component owner;
	private final JMenu mnPresets;
	private final MainFrameSettings settings;

	PresetManager(Component owner, JMenu mnPresets, MainFrameSettings settings) {
		this.owner = owner;
		this.mnPresets = mnPresets;
		this.settings = settings;
	}

	void rebuildPresetsMenu() {
		mnPresets.removeAll();

		List<PresetEntry> presets = listPresets();
		if (presets.isEmpty()) {
			JMenuItem emptyItem = new JMenuItem(Messages.getString("MainFrame.menu.presets.empty"));
			emptyItem.setEnabled(false);
			mnPresets.add(emptyItem);
		} else {
			int digit = 1;
			for (PresetEntry preset : presets) {
				JMenuItem presetItem = new JMenuItem(preset.name());
				if (digit <= PRESET_SHORTCUT_COUNT) {
					presetItem.setAccelerator(presetShortcutKeyStroke(digit));
				}
				presetItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						confirmAndLoadPreset(preset);
					}
				});
				mnPresets.add(presetItem);
				digit++;
			}
		}

		mnPresets.addSeparator();

		JMenuItem mntmDefaultSettings = new JMenuItem(Messages.getString("MainFrame.menu.presets.default"));
		mntmDefaultSettings.setMnemonic(KeyEvent.VK_D);
		mntmDefaultSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmAndResetToDefaultSettings();
			}
		});
		mnPresets.add(mntmDefaultSettings);

		mnPresets.addSeparator();

		JMenuItem mntmSavePreset = new JMenuItem(Messages.getString("MainFrame.menu.presets.save"));
		mntmSavePreset.setMnemonic(KeyEvent.VK_S);
		mntmSavePreset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				promptSaveCurrentAsPreset();
			}
		});
		mnPresets.add(mntmSavePreset);

		JMenuItem mntmManagePresets = new JMenuItem(Messages.getString("MainFrame.menu.presets.manage"));
		mntmManagePresets.setMnemonic(KeyEvent.VK_M);
		mntmManagePresets.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PresetsManageDialog manageDialog = new PresetsManageDialog(PresetManager.this);
				manageDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
				manageDialog.setLocationRelativeTo(owner);
				manageDialog.setVisible(true);
			}
		});
		mnPresets.add(mntmManagePresets);
	}

	private void promptSaveCurrentAsPreset() {
		JComboBox<String> nameComboBox = new JComboBox<>();
		nameComboBox.setEditable(true);
		for (PresetEntry preset : listPresets()) {
			nameComboBox.addItem(preset.name());
		}
		nameComboBox.setSelectedItem("");

		JPanel panel = new JPanel(new BorderLayout(0, MainFrame.INLINE_VGAP));
		panel.add(new JLabel(Messages.getString("message.prompt.preset.name")), BorderLayout.NORTH);
		panel.add(nameComboBox, BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(
				owner,
				panel,
				Messages.getString("MainFrame.menu.presets.save"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE
				);
		if (result != JOptionPane.OK_OPTION) {
			return;
		}

		Object editorItem = nameComboBox.getEditor().getItem();
		String name = editorItem == null ? "" : editorItem.toString().trim();
		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(owner, Messages.getString("message.warn.preset.name.empty"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		boolean alreadyExists = false;
		for (PresetEntry preset : listPresets()) {
			if (preset.name().equals(name)) {
				alreadyExists = true;
				break;
			}
		}
		if (alreadyExists && !DialogSupport.confirmYesNo(owner, "message.confirm.preset.overwrite", name)) {
			return;
		}

		try {
			saveCurrentSettingsAsPreset(name);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.error.preset.save", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);
			App.handleError(e1.getMessage(), e1);
		}
	}

	private void saveCurrentSettingsAsPreset(String name) throws IOException {
		AppSettingsDirectory dir = presetsDirectory();
		dir.ensureExists();

		String fileName = null;
		for (PresetEntry preset : listPresets()) {
			if (preset.name().equals(name)) {
				fileName = preset.fileName();
				break;
			}
		}
		if (fileName == null) {
			long timestamp = System.currentTimeMillis();
			fileName = dir.uniqueFileName(i -> "preset-" + timestamp + "-" + i + "." + PRESET_FILE_NAME_EXT);
		}

		AppSettings presetSettings = AppSettings.of(dir, fileName);
		settings.captureInto(presetSettings);
		presetSettings.addComment(name);
		presetSettings.store();
	}

	static KeyStroke presetShortcutKeyStroke(int digit) {
		int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		return KeyStroke.getKeyStroke(KeyEvent.VK_0 + digit, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK);
	}

	void loadPresetByIndex(int index) {
		List<PresetEntry> presets = listPresets();
		if (index >= presets.size()) {
			return;
		}
		confirmAndLoadPreset(presets.get(index));
	}

	private void confirmAndLoadPreset(PresetEntry preset) {
		if (!DialogSupport.confirmYesNo(owner, "message.confirm.preset.load", preset.name())) {
			return;
		}

		try {
			loadPreset(preset.fileName());
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.error.preset.load", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);
			App.handleError(e1.getMessage(), e1);
		}
	}

	private void loadPreset(String fileName) throws IOException {
		AppSettings presetSettings = AppSettings.of(presetsDirectory(), fileName);
		presetSettings.load();
		applySettingsSource(presetSettings);
	}

	private void confirmAndResetToDefaultSettings() {
		if (!DialogSupport.confirmYesNo(owner, "message.confirm.settings.default")) {
			return;
		}

		settings.applyDefault(App.settings());
		settings.load();
	}

	/** Copies every key from {@code source} into the live settings, then refreshes the UI from it. */
	private void applySettingsSource(AppSettings source) {
		AppSettings conf = App.settings();
		for (String key : source.keySet()) {
			conf.set(key, source.get(key));
		}

		settings.load();
	}

	/** Renames {@code fileName}'s preset to {@code newName}. Called by {@link PresetsManageDialog}. */
	void renamePreset(String fileName, String newName) throws IOException {
		AppSettings presetSettings = AppSettings.of(presetsDirectory(), fileName);
		presetSettings.load();
		presetSettings.clearComments();
		presetSettings.addComment(newName);
		presetSettings.store();
	}

	/** Deletes {@code fileName}'s preset. Called by {@link PresetsManageDialog}. */
	void deletePreset(String fileName) throws IOException {
		Files.delete(AppSettings.of(presetsDirectory(), fileName).path());
	}

	private AppSettingsDirectory presetsDirectory() {
		return AppSettings.directory(App.GROUP_NAME, App.APP_NAME, PRESETS_DIR_NAME);
	}

	/** Lists every saved preset, sorted by name. Called by {@link PresetsManageDialog}. */
	List<PresetEntry> listPresets() {
		AppSettingsDirectory dir = presetsDirectory();
		try {
			dir.ensureExists();
		} catch (IOException _) {
			return List.of();
		}

		List<PresetEntry> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir.path(), "*." + PRESET_FILE_NAME_EXT)) {
			for (Path file : stream) {
				String fileName = file.getFileName().toString();
				AppSettings presetSettings = AppSettings.of(dir, fileName);
				try {
					presetSettings.load();
				} catch (IOException _) {
					continue;
				}
				List<String> comments = presetSettings.comments();
				String name = comments.isEmpty() ? fileName : comments.get(0);
				result.add(new PresetEntry(name, fileName));
			}
		} catch (IOException _) {
			return List.of();
		}

		result.sort(Comparator.comparing(PresetEntry::name));
		return result;
	}

	record PresetEntry(String name, String fileName) {
		@Override
		public String toString() {
			return name;
		}
	}

	private enum PresetConflictResolution {
		RENAME, REPLACE, SKIP
	}

	void promptExportData() {
		boolean historyAvailable = Files.exists(App.history().path());
		boolean presetsAvailable = !listPresets().isEmpty();

		DataArchiveSupport.DataCategories selection = promptDataCategorySelection(
				Messages.getString("MainFrame.menu.settings.exportData"),
				true,
				presetsAvailable,
				historyAvailable);
		if (selection == null) {
			return;
		}
		if (!selection.any()) {
			JOptionPane.showMessageDialog(owner, Messages.getString("message.warn.dataSelection.empty"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("data.ext.description"), PICTO_FILE_NAME_EXT));

		int selected = filechooser.showSaveDialog(owner);
		if (selected != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = filechooser.getSelectedFile();
		if (!PICTO_FILE_NAME_EXT.equals(FileNameSupport.extension(file.getName()))) {
			file = new File(file.getParentFile(), file.getName() + "." + PICTO_FILE_NAME_EXT);
		}

		try {
			exportData(file.toPath(), selection);

			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.info.export.data"),
					null,
					JOptionPane.INFORMATION_MESSAGE
					);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.error.export.data", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);

			App.handleError(e1.getMessage(), e1);
		}
	}

	private void exportData(Path zipPath, DataArchiveSupport.DataCategories selection) throws IOException {
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
			if (selection.settings()) {
				DataArchiveSupport.writeSettingsEntry(zos, DataArchiveSupport.SETTINGS_ENTRY_NAME, App.settings());
			}
			if (selection.history()) {
				DataArchiveSupport.writeSettingsEntry(zos, DataArchiveSupport.HISTORY_ENTRY_NAME, App.history());
			}
			if (selection.presets()) {
				AppSettingsDirectory dir = presetsDirectory();
				int index = 1;
				for (PresetEntry preset : listPresets()) {
					DataArchiveSupport.writeFileEntry(zos, DataArchiveSupport.presetEntryName(index), dir.path().resolve(preset.fileName()));
					index++;
				}
			}
		}
	}

	void promptImportData() {
		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("data.ext.description"), PICTO_FILE_NAME_EXT));

		int selected = filechooser.showOpenDialog(owner);
		if (selected != JFileChooser.APPROVE_OPTION) {
			return;
		}
		Path zipPath = filechooser.getSelectedFile().toPath();

		// Backward compatibility: a *.picto file from before the ZIP-based archive format was a plain
		// key=value settings.properties dump, sharing the same extension as the current format. Remove
		// this branch and LegacyPictoFileImport once nobody plausibly still has one of those old exports.
		boolean isLegacyFile;
		try {
			isLegacyFile = LegacyPictoFileImport.isLegacyFile(zipPath);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.error.import.data", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);
			App.handleError(e1.getMessage(), e1);
			return;
		}
		if (isLegacyFile) {
			importLegacySettingsFile(zipPath);
			return;
		}

		DataArchiveSupport.DataCategories available;
		try {
			available = DataArchiveSupport.readAvailableCategories(zipPath);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.error.import.data", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);
			App.handleError(e1.getMessage(), e1);
			return;
		}
		if (!available.any()) {
			JOptionPane.showMessageDialog(owner, Messages.getString("message.warn.importData.none"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		DataArchiveSupport.DataCategories selection = promptDataCategorySelection(
				Messages.getString("MainFrame.menu.settings.importData"),
				available.settings(),
				available.presets(),
				available.history());
		if (selection == null) {
			return;
		}
		if (!selection.any()) {
			JOptionPane.showMessageDialog(owner, Messages.getString("message.warn.dataSelection.empty"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			if (!importData(zipPath, selection)) {
				return;
			}
			if (selection.settings()) {
				settings.load();
			}

			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.info.import.data"),
					null,
					JOptionPane.INFORMATION_MESSAGE
					);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.error.import.data", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);

			App.handleError(e1.getMessage(), e1);
		}
	}

	// Backward compatibility: see the comment at LegacyPictoFileImport's one call site above. Remove
	// this method alongside that branch and the LegacyPictoFileImport class.
	private void importLegacySettingsFile(Path legacyFile) {
		try {
			LegacyPictoFileImport.importInto(App.settings(), legacyFile);
			settings.load();

			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.info.import.data"),
					null,
					JOptionPane.INFORMATION_MESSAGE
					);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					owner,
					Messages.getString("message.error.import.data", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);

			App.handleError(e1.getMessage(), e1);
		}
	}

	/**
	 * Parses and validates every selected category before applying or persisting any of them, so corrupt or
	 * unreadable archive entries fail without changing the current settings. Once persistence starts, later
	 * write failures are reported to the user but are not fully rolled back. Returns {@code false} if the user
	 * cancelled the preset name-conflict prompt, in which case nothing was applied.
	 */
	private boolean importData(Path zipPath, DataArchiveSupport.DataCategories selection) throws IOException {
		Map<String, Object> settingsValues = null;
		Map<String, Object> historyValues = null;
		List<AppSettings> preparedPresets = null;

		try (ZipFile zip = new ZipFile(zipPath.toFile())) {
			if (selection.settings()) {
				settingsValues = DataArchiveSupport.readEntryValues(zip, DataArchiveSupport.SETTINGS_ENTRY_NAME);
			}
			if (selection.history()) {
				historyValues = DataArchiveSupport.readEntryValues(zip, DataArchiveSupport.HISTORY_ENTRY_NAME);
			}
			if (selection.presets()) {
				preparedPresets = preparePresetImports(zip);
				if (preparedPresets == null) {
					return false;
				}
			}
		}

		if (settingsValues != null) {
			for (Map.Entry<String, Object> entry : settingsValues.entrySet()) {
				App.settings().set(entry.getKey(), entry.getValue());
			}
			App.settings().store();
		}
		if (historyValues != null) {
			for (Map.Entry<String, Object> entry : historyValues.entrySet()) {
				App.history().set(entry.getKey(), entry.getValue());
			}
			App.history().store();
		}
		if (preparedPresets != null) {
			presetsDirectory().ensureExists();
			for (AppSettings preset : preparedPresets) {
				preset.store();
			}
		}
		return true;
	}

	/**
	 * Builds the {@link AppSettings} to store for each imported preset, without writing any of them yet.
	 * Returns {@code null} if the user cancelled the name-conflict prompt (shown at most once, covering
	 * every conflicting name in this batch at once).
	 */
	private List<AppSettings> preparePresetImports(ZipFile zip) throws IOException {
		List<DataArchiveSupport.ParsedPreset> parsed = DataArchiveSupport.readPresetEntries(zip);

		List<PresetEntry> existingPresets = listPresets();
		Map<String, PresetEntry> existingByName = new HashMap<>();
		for (PresetEntry preset : existingPresets) {
			existingByName.put(preset.name(), preset);
		}

		List<String> conflictingNames = new ArrayList<>();
		for (DataArchiveSupport.ParsedPreset p : parsed) {
			if (existingByName.containsKey(p.name())) {
				conflictingNames.add(p.name());
			}
		}

		PresetConflictResolution resolution = PresetConflictResolution.RENAME;
		if (!conflictingNames.isEmpty()) {
			PresetConflictResolution chosen = promptPresetConflictResolution(conflictingNames);
			if (chosen == null) {
				return null;
			}
			resolution = chosen;
		}

		AppSettingsDirectory dir = presetsDirectory();
		Set<String> namesInUse = new HashSet<>(existingByName.keySet());
		Set<String> fileNamesInUse = new HashSet<>();
		for (PresetEntry preset : existingPresets) {
			fileNamesInUse.add(preset.fileName());
		}

		List<AppSettings> result = new ArrayList<>();
		for (DataArchiveSupport.ParsedPreset p : parsed) {
			PresetEntry existing = existingByName.get(p.name());
			if (existing == null || resolution == PresetConflictResolution.RENAME) {
				String finalName = DataArchiveSupport.uniqueDisplayName(p.name(), namesInUse);
				String fileName = DataArchiveSupport.allocatePresetFileName(fileNamesInUse);
				result.add(buildPresetImport(dir, fileName, finalName, p.values()));
			} else if (resolution == PresetConflictResolution.REPLACE) {
				// Discards the existing preset's own content instead of merging onto it, so a replaced
				// preset ends up built exactly like a new one - same defaults, same imported keys - just
				// written under the existing name/file instead of a new one.
				result.add(buildPresetImport(dir, existing.fileName(), existing.name(), p.values()));
			}
			// SKIP: leave the existing preset untouched.
		}
		return result;
	}

	private AppSettings buildPresetImport(AppSettingsDirectory dir, String fileName, String name, Map<String, Object> values) {
		return DataArchiveSupport.buildPresetSettings(dir, fileName, settings::applyDefault, values, name);
	}

	private DataArchiveSupport.DataCategories promptDataCategorySelection(
			String title,
			boolean settingsAvailable,
			boolean presetsAvailable,
			boolean historyAvailable) {
		JCheckBox chkSettings = new JCheckBox(Messages.getString("MainFrame.dataCategory.settings"), settingsAvailable);
		chkSettings.setEnabled(settingsAvailable);
		JCheckBox chkPresets = new JCheckBox(Messages.getString("MainFrame.dataCategory.presets"), presetsAvailable);
		chkPresets.setEnabled(presetsAvailable);
		JCheckBox chkHistory = new JCheckBox(Messages.getString("MainFrame.dataCategory.history"), historyAvailable);
		chkHistory.setEnabled(historyAvailable);

		JPanel panel = new JPanel(new GridLayout(0, 1, 0, MainFrame.INLINE_VGAP));
		panel.add(chkSettings);
		panel.add(chkPresets);
		panel.add(chkHistory);

		int result = JOptionPane.showConfirmDialog(owner, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) {
			return null;
		}
		return new DataArchiveSupport.DataCategories(chkSettings.isSelected(), chkPresets.isSelected(), chkHistory.isSelected());
	}

	private PresetConflictResolution promptPresetConflictResolution(List<String> conflictingNames) {
		JRadioButton rdoRename = new JRadioButton(Messages.getString("MainFrame.presetConflict.rename"), true);
		JRadioButton rdoReplace = new JRadioButton(Messages.getString("MainFrame.presetConflict.replace"));
		JRadioButton rdoSkip = new JRadioButton(Messages.getString("MainFrame.presetConflict.skip"));
		ButtonGroup group = new ButtonGroup();
		group.add(rdoRename);
		group.add(rdoReplace);
		group.add(rdoSkip);

		StringBuilder names = new StringBuilder();
		for (String name : conflictingNames) {
			if (!names.isEmpty()) {
				names.append("<br>");
			}
			names.append(escapeHtml(name));
		}

		JPanel panel = new JPanel(new BorderLayout(0, MainFrame.INLINE_VGAP));
		panel.add(new JLabel(Messages.getString("MainFrame.presetConflict.message", names.toString())), BorderLayout.NORTH);
		JPanel radios = new JPanel(new GridLayout(0, 1));
		radios.add(rdoRename);
		radios.add(rdoReplace);
		radios.add(rdoSkip);
		panel.add(radios, BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(
				owner,
				panel,
				Messages.getString("MainFrame.presetConflict.title"),
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE
				);
		if (result != JOptionPane.OK_OPTION) {
			return null;
		}
		if (rdoReplace.isSelected()) {
			return PresetConflictResolution.REPLACE;
		}
		if (rdoSkip.isSelected()) {
			return PresetConflictResolution.SKIP;
		}
		return PresetConflictResolution.RENAME;
	}

	private static String escapeHtml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
