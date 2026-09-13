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

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import net.mozq.appsettings.AppSettings;
import net.mozq.picto.App;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.FileSizeUnit;
import net.mozq.picto.enums.OperationType;

/**
 * The mapping between persisted {@link AppSettings} keys and {@link MainFrame}'s Swing fields: which key
 * each field reads from and writes to, and its schema default. Kept as one table so loading, saving, and
 * resetting to defaults can't drift apart on a key, default, or field a future change touches only one
 * side of.
 */
final class MainFrameSettings {
	static final String DEFAULT_DEST_SUB_FILE_PATH_PATTERN = "${FileName}";

	private final JTextField txtSrcFolder;
	private final SourceOptionsPanel srcOpt;
	private final ButtonGroup btngrpOperationType;
	private final JTextField txtDestFolder;
	private final DestinationOptionsPanel destOpt;
	private final ChangesPanel changes;

	MainFrameSettings(
			JTextField txtSrcFolder,
			SourceOptionsPanel srcOpt,
			ButtonGroup btngrpOperationType,
			JTextField txtDestFolder,
			DestinationOptionsPanel destOpt,
			ChangesPanel changes) {
		this.txtSrcFolder = txtSrcFolder;
		this.srcOpt = srcOpt;
		this.btngrpOperationType = btngrpOperationType;
		this.txtDestFolder = txtDestFolder;
		this.destOpt = destOpt;
		this.changes = changes;
	}

	void load() {
		applyFrom(App.settings());
	}

	void store() throws IOException {
		AppSettings conf = App.settings();

		captureInto(conf);

		conf.store();
		App.deleteMigratedLegacySettingsIfNeeded();
	}

	void applyFrom(AppSettings conf) {
		for (SettingBinding binding : bindings()) {
			binding.applyFrom().accept(conf);
		}
	}

	void captureInto(AppSettings conf) {
		for (SettingBinding binding : bindings()) {
			binding.captureInto().accept(conf);
		}
	}

	/**
	 * Writes each bound setting's schema default into {@code conf}, without reading or touching any Swing
	 * component. Used as the base for a newly imported preset, so a key the import doesn't mention still
	 * ends up with the same value a never-customized preset would have, rather than being left undefined.
	 */
	void applyDefault(AppSettings conf) {
		for (SettingBinding binding : bindings()) {
			binding.applyDefault().accept(conf);
		}
	}

	private List<SettingBinding> bindings() {
		return List.of(
				SettingBinding.text(txtSrcFolder, "src.folder", ""),
				SettingBinding.text(srcOpt.txtFileNamePattern, "src.file.name.pattern", ""),
				SettingBinding.choice(srcOpt.cmbFileNamePatternSyntax, "src.file.name.pattern.syntax", FilePatternSyntax.class, FilePatternSyntax.Glob),
				SettingBinding.flag(srcOpt.chkIncludeSubfolders, "src.include.subfolders", true),
				SettingBinding.flag(srcOpt.chkIncludeHiddenFiles, "src.include.hidden.files", false),

				SettingBinding.text(srcOpt.txtFileSizeFrom, "src.file.size.from", ""),
				SettingBinding.text(srcOpt.txtFileSizeTo, "src.file.size.to", ""),
				SettingBinding.choice(srcOpt.cmbFileSizeUnit, "src.file.size.unit", FileSizeUnit.class, FileSizeUnit.MB),
				SettingBinding.text(srcOpt.txtCreatedFrom, "src.created.from", ""),
				SettingBinding.text(srcOpt.txtCreatedTo, "src.created.to", ""),
				SettingBinding.text(srcOpt.txtModifiedFrom, "src.modified.from", ""),
				SettingBinding.text(srcOpt.txtModifiedTo, "src.modified.to", ""),

				SettingBinding.radioChoice(btngrpOperationType, "operation.type", OperationType.class, OperationType.Copy),

				SettingBinding.text(txtDestFolder, "dest.folder", ""),
				SettingBinding.text(destOpt.txtSubFilePathPattern, "dest.sub.file.path.pattern", DEFAULT_DEST_SUB_FILE_PATH_PATTERN),
				SettingBinding.choice(destOpt.cmbExistingFileMethod, "dest.existing.file.method", ExistingFileMethod.class, ExistingFileMethod.Confirm),
				SettingBinding.flag(destOpt.chkCheckFileDigest, "dest.check.file.digest", false),

				SettingBinding.flag(changes.filedate.chkCreationDate, "changes.filedate.creation.date", false),
				SettingBinding.flag(changes.filedate.chkModifiedDate, "changes.filedate.modified.date", false),
				SettingBinding.flag(changes.filedate.chkAccessDate, "changes.filedate.access.date", false),
				SettingBinding.flag(changes.filedate.chkExifDate, "changes.filedate.exif.date", false),
				SettingBinding.choice(changes.filedate.cmbBaseDate, "changes.filedate.base.date.type", DateType.class, DateType.FileModifiedDate),
				SettingBinding.text(changes.filedate.txtCustomBaseDate, "changes.filedate.custom.base.date", ""),
				SettingBinding.choice(changes.filedate.cmbAdjustmentType, "changes.filedate.adjustment.type", DateModType.class, DateModType.None),
				SettingBinding.text(changes.filedate.txtAdjustmentYears, "changes.filedate.adjustment.years", ""),
				SettingBinding.text(changes.filedate.txtAdjustmentMonths, "changes.filedate.adjustment.months", ""),
				SettingBinding.text(changes.filedate.txtAdjustmentDays, "changes.filedate.adjustment.days", ""),
				SettingBinding.text(changes.filedate.txtAdjustmentHours, "changes.filedate.adjustment.hours", ""),
				SettingBinding.text(changes.filedate.txtAdjustmentMinutes, "changes.filedate.adjustment.minutes", ""),
				SettingBinding.text(changes.filedate.txtAdjustmentSeconds, "changes.filedate.adjustment.seconds", ""),
				SettingBinding.flag(changes.exif.chkRemoveGps, "changes.exif.remove.gps", false),
				SettingBinding.flag(changes.exif.chkRemoveAll, "changes.exif.remove.all", false));
	}

	/**
	 * One persisted setting's key paired with how to load it into its Swing field and how to read it back
	 * out, declared once so {@link #applyFrom} and {@link #captureInto} can't drift apart on the key,
	 * default, or field a future change touches only one side of.
	 */
	private record SettingBinding(
			Consumer<AppSettings> applyFrom,
			Consumer<AppSettings> captureInto,
			Consumer<AppSettings> applyDefault) {
		static SettingBinding text(JTextComponent field, String key, String defaultValue) {
			return new SettingBinding(
					conf -> field.setText(conf.getString(key, defaultValue)),
					conf -> conf.set(key, field.getText()),
					conf -> conf.set(key, defaultValue));
		}

		static SettingBinding flag(AbstractButton button, String key, boolean defaultValue) {
			return new SettingBinding(
					conf -> button.setSelected(conf.getBoolean(key, defaultValue)),
					conf -> conf.set(key, button.isSelected()),
					conf -> conf.set(key, defaultValue));
		}

		static <E extends Enum<E>> SettingBinding choice(JComboBox<E> combo, String key, Class<E> type, E defaultValue) {
			return new SettingBinding(
					conf -> combo.setSelectedItem(conf.getEnum(key, type, defaultValue)),
					conf -> conf.set(key, combo.getSelectedItem()),
					conf -> conf.set(key, defaultValue));
		}

		/**
		 * Like {@link #choice}, but for a mutually exclusive set of radio buttons instead of a combo box.
		 * Each button in {@code group} must have its action command set to the name of the enum constant it
		 * represents (as {@link MainFrame#buildOperationPanel} does for the operation-type radios), so this
		 * can read and set the selection generically instead of every caller writing its own button-to-enum
		 * mapping.
		 */
		static <E extends Enum<E>> SettingBinding radioChoice(ButtonGroup group, String key, Class<E> type, E defaultValue) {
			return new SettingBinding(
					conf -> selectButtonFor(group, conf.getEnum(key, type, defaultValue)),
					conf -> conf.set(key, selectedEnumValue(group, type, defaultValue)),
					conf -> conf.set(key, defaultValue));
		}
	}

	private static <E extends Enum<E>> void selectButtonFor(ButtonGroup group, E value) {
		for (java.util.Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements();) {
			AbstractButton button = e.nextElement();
			if (button.getActionCommand().equals(value.name())) {
				button.setSelected(true);
				return;
			}
		}
	}

	static <E extends Enum<E>> E selectedEnumValue(ButtonGroup group, Class<E> type, E defaultValue) {
		for (java.util.Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements();) {
			AbstractButton button = e.nextElement();
			if (button.isSelected()) {
				return Enum.valueOf(type, button.getActionCommand());
			}
		}
		return defaultValue;
	}
}
