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
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.appsettings.AppSettings;
import net.mozq.appsettings.AppSettingsDirectory;
import net.mozq.nanotemplate.NanoTemplate;
import net.mozq.picto.App;
import net.mozq.picto.AppMain;
import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.core.ProcessCondition;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FileSizeUnit;
import net.mozq.picto.enums.OperationType;
import net.mozq.picto.util.FileNameSupport;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_DEST_SUB_PATH_PATTERN = "${FileName}";
	private static final String EMPTY_DEST_SUB_PATH_PATTERN = "${SubFilePath}";
	private static final int WINDOW_PADDING = 10;
	private static final int MAIN_LABEL_WIDTH = 52;
	private static final int SECTION_PADDING = 8;
	private static final int SECTION_HEADER_GAP = 4;
	private static final int SECTION_GAP = 10;
	private static final int OPERATION_BOTTOM_GAP = 14;
	private static final int INLINE_HGAP = 6;
	private static final int INLINE_VGAP = 2;
	private static final int RUN_SPLIT_BUTTON_OVERLAP = 4;
	private static final int RUN_MENU_BUTTON_WIDTH = 28;
	private static final int RUN_STATUS_ICON_SIZE = 24;
	private static final int RUN_STATUS_ICON_PADDING = 8;
	private static final int OPTIONS_BODY_PADDING = 12;
	private static final int OPTIONS_BODY_TOP_PADDING_WITH_MATCH_COUNT = 4;
	private static final int OPTIONS_BODY_ARC = 12;
	private static final int OPTIONS_BODY_SHADE_LIGHT = -11;
	private static final int OPTIONS_BODY_SHADE_DARK = -8;
	private static final int MID_BRIGHTNESS = 128;
	private static final String CHECKED_ITEM_PREFIX = "✓ ";
	private static final String PRESETS_DIR_NAME = "presets";
	private static final String PRESET_FILE_NAME_EXT = "conf";
	private static final int PRESET_SHORTCUT_COUNT = 9;
	private static final int MATCH_COUNT_DEBOUNCE_MS = 400;

	private TimeZone timeZone = TimeZone.getDefault();

	private static final String SETTINGS_FILE_NAME_EXT = "conf";

	private final JFrame frame;
	private boolean windowLayoutReady;

	private JPanel contentPane;

	private JPanel pnlSrcConditions;
	private JPanel pnlSrcRootDirPath;
	private JTextField txtSrcRootDirPath;
	private JButton btnSrcRootDirSelect;
	private JLabel lblFilePattern;
	private JTextField txtFilePattern;
	private JComboBox<FilePatternSyntax> cmbFilePatternSyntax;
	private JCheckBox chkContainsSubs;
	private JCheckBox chkContainsHiddens;
	private JLabel lblFileSizeRange;
	private JTextField txtFileSizeRangeFrom;
	private JLabel lblFileSizeRangeTo;
	private JTextField txtFileSizeRangeTo;
	private JComboBox<FileSizeUnit> cmbFileSizeUnit;
	private JLabel lblCreationTimeRange;
	private JFormattedTextField txtCreationTimeRangeFrom;
	private JLabel lblCreationTimeRangeTo;
	private JFormattedTextField txtCreationTimeRangeTo;
	private JLabel lblModifiedTimeRange;
	private JFormattedTextField txtModifiedTimeRangeFrom;
	private JLabel lblModifiedTimeRangeTo;
	private JFormattedTextField txtModifiedTimeRangeTo;

	private JPanel pnlDestConditions;
	private JPanel pnlOperation;
	private JPanel pnlOpeType;
	private ButtonGroup btngrpOpeType = new ButtonGroup();
	private JRadioButton rdoOpeTypeCopy;
	private JRadioButton rdoOpeTypeMove;
	private JRadioButton rdoOpeTypeOverwrite;
	private JLabel lblExistingFileMethod;
	private JComboBox<ExistingFileMethod> cmbExistingFileMethod;
	private JPanel pnlControls;
	private JTextField txtDestRootDirPath;
	private JButton btnDestRootDirSelect;
	private JLabel lblDestSubPathPattern;
	private JTextField txtDestSubPathPattern;
	private JPanel pnlDestRootDirPath;
	private JLabel lblValidateFile;
	private JCheckBox chkCheckFileDigest;

	private JTabbedPane tabModConditions;
	private JLabel lblTargetDate;
	private JCheckBox chkChangeFileCreationDate;
	private JCheckBox chkChangeFileModifiedDate;
	private JCheckBox chkChangeFileAccessDate;
	private JCheckBox chkChangeExifDate;
	private JLabel lblBaseDateType;
	private JComboBox<DateType> cmbBaseDateType;
	private JFormattedTextField txtCustomBaseDate;
	private JLabel lblEditBaseDate;
	private JComboBox<DateModType> cmbDateModType;
	private JTextField txtDateModYears;
	private JLabel lblSepYM;
	private JTextField txtDateModMonths;
	private JLabel lblSepMD;
	private JTextField txtDateModDays;
	private JLabel lblSepDH;
	private JTextField txtDateModHours;
	private JLabel lblSepHM;
	private JTextField txtDateModMinutes;
	private JLabel lblSepMS;
	private JTextField txtDateModSeconds;

	private JCheckBox chkRemoveExifTagsGps;
	private JCheckBox chkRemoveExifTagsAll;

	private JButton btnStart;
	private JButton btnStartMenu;
	private JToggleButton btnSrcOptions;
	private JToggleButton btnDestOptions;
	private JToggleButton btnChanges;
	private JLabel lblDestConditionsTitle;
	private SourceOptionsPanel sourceOptionsPanel;
	private DestinationOptionsPanel destinationOptionsPanel;
	private JTextArea txtSrcOptionsSummary;
	private JTextArea txtDestOptionsSummary;
	private JTextArea txtChangesSummary;
	private JLabel lblRunSummary;
	private boolean showingRunSummary;
	private boolean matchCountEnabled;
	private SourceFileScanner sourceFileScanner;
	private SourceFileScanner.MatchCountStatus lastMatchCountStatus;
	private Timer matchCountTimer;
	private JMenuBar menuBar;
	private JMenu mnSettings;
	private JMenu mnLanguage;
	private JMenu mnAppearance;
	private JMenu mnPresets;
	private JMenu mnHelp;
	private JMenuItem mntmHelp;
	private JMenuItem mntmImportSettings;
	private JMenuItem mntmExportSettings;
	private boolean processing;
	private JLabel lblRunStatus;
	private ProcessDialog lastProcessDialog;

	private static final class MainFrameState {
		private final Rectangle bounds;
		private final int extendedState;
		private final boolean srcOptionsExpanded;
		private final boolean destOptionsExpanded;
		private final boolean changesExpanded;
		private final int changesTabIndex;

		private MainFrameState(
				Rectangle bounds,
				int extendedState,
				boolean srcOptionsExpanded,
				boolean destOptionsExpanded,
				boolean changesExpanded,
				int changesTabIndex) {
			this.bounds = bounds;
			this.extendedState = extendedState;
			this.srcOptionsExpanded = srcOptionsExpanded;
			this.destOptionsExpanded = destOptionsExpanded;
			this.changesExpanded = changesExpanded;
			this.changesTabIndex = changesTabIndex;
		}
	}


	/**
	 * Create the frame.
	 */
	public MainFrame() {
		this(null);
	}

	private MainFrame(MainFrameState state) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					storeSettings();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(
							null,
							Messages.getString("message.error.store.settings", e1.getLocalizedMessage()),
							null,
							JOptionPane.ERROR_MESSAGE
							);

					App.handleError(e1.getMessage(), e1);
				}
			}
		});

		setTitle(Messages.getString("MainFrame.title"));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 700);

		buildMenuBar();

		buildContentPane();

		buildSourcePanel();

		buildOperationPanel();

		buildDestinationPanel();

		buildChangesPanel();

		buildControlsPanel();

		installKeyboardShortcuts();

		InputSupport.installClickAwayFocusClear();

		installOperationListeners();

		matchCountTimer = new Timer(MATCH_COUNT_DEBOUNCE_MS, _ -> updateMatchCountTarget());
		matchCountTimer.setRepeats(false);

		installOptionsSummaryListeners();
		loadSettings();
		changeEnableDestConditions();
		changeEnableFileDateModConditions();
		restoreFrameState(state);
		updateOptionsSummaries();

		frame = this;
		windowLayoutReady = true;
		fitWindowToContent();
	}

	private void buildMenuBar() {
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnPresets = new JMenu(Messages.getString("MainFrame.menu.presets"));
		mnPresets.setMnemonic(KeyEvent.VK_P);
		mnPresets.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) {
				rebuildPresetsMenu();
			}

			public void menuDeselected(MenuEvent e) {
				// NOP
			}

			public void menuCanceled(MenuEvent e) {
				// NOP
			}
		});
		menuBar.add(mnPresets);

		mnSettings = new JMenu(Messages.getString("MainFrame.menu.settings"));
		mnSettings.setMnemonic(KeyEvent.VK_S);
		menuBar.add(mnSettings);

		mnLanguage = new JMenu(Messages.getString("MainFrame.menu.settings.language"));
		mnLanguage.setMnemonic(KeyEvent.VK_L);
		mnSettings.add(mnLanguage);
		ButtonGroup languageGroup = new ButtonGroup();
		addSettingsMenuItem(
				mnLanguage,
				languageGroup,
				Messages.getString("MainFrame.menu.settings.language.system"),
				AppMain.PREF_LOCALE_KEY,
				AppMain.PREF_SYSTEM,
				KeyEvent.VK_S);
		addSettingsMenuItem(
				mnLanguage,
				languageGroup,
				Messages.getString("MainFrame.menu.settings.language.en"),
				AppMain.PREF_LOCALE_KEY,
				AppMain.PREF_LOCALE_EN,
				0);
		addSettingsMenuItem(
				mnLanguage,
				languageGroup,
				Messages.getString("MainFrame.menu.settings.language.ja"),
				AppMain.PREF_LOCALE_KEY,
				AppMain.PREF_LOCALE_JA,
				0);

		mnAppearance = new JMenu(Messages.getString("MainFrame.menu.settings.appearance"));
		mnAppearance.setMnemonic(KeyEvent.VK_A);
		mnSettings.add(mnAppearance);
		ButtonGroup appearanceGroup = new ButtonGroup();
		addSettingsMenuItem(
				mnAppearance,
				appearanceGroup,
				Messages.getString("MainFrame.menu.settings.appearance.system"),
				AppMain.PREF_APPEARANCE_KEY,
				AppMain.PREF_SYSTEM,
				KeyEvent.VK_S);
		addSettingsMenuItem(
				mnAppearance,
				appearanceGroup,
				Messages.getString("MainFrame.menu.settings.appearance.light"),
				AppMain.PREF_APPEARANCE_KEY,
				AppMain.PREF_APPEARANCE_LIGHT,
				KeyEvent.VK_L);
		addSettingsMenuItem(
				mnAppearance,
				appearanceGroup,
				Messages.getString("MainFrame.menu.settings.appearance.dark"),
				AppMain.PREF_APPEARANCE_KEY,
				AppMain.PREF_APPEARANCE_DARK,
				KeyEvent.VK_D);

		mnSettings.addSeparator();

		int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

		mntmImportSettings = new JMenuItem(Messages.getString("MainFrame.menu.settings.importSettings"));
		mntmImportSettings.setMnemonic(KeyEvent.VK_I);
		mntmImportSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
		mntmImportSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("settings.ext.description"), SETTINGS_FILE_NAME_EXT));

				int selected = filechooser.showOpenDialog(frame);
				if (selected == JFileChooser.APPROVE_OPTION) {
					File file = filechooser.getSelectedFile();
					try {
						App.settings().loadFrom(file.toPath());
						loadSettings();

						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.info.import.settings"),
								null,
								JOptionPane.INFORMATION_MESSAGE
								);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.error.import.settings", e1.getLocalizedMessage()),
								null,
								JOptionPane.ERROR_MESSAGE
								);

						App.handleError(e1.getMessage(), e1);
					}
				}
			}
		});
		mnSettings.add(mntmImportSettings);

		mntmExportSettings = new JMenuItem(Messages.getString("MainFrame.menu.settings.exportSettings"));
		mntmExportSettings.setMnemonic(KeyEvent.VK_E);
		mntmExportSettings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
		mntmExportSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("settings.ext.description"), SETTINGS_FILE_NAME_EXT));

				int selected = filechooser.showSaveDialog(frame);
				if (selected == JFileChooser.APPROVE_OPTION) {
					File file = filechooser.getSelectedFile();

					if (!SETTINGS_FILE_NAME_EXT.equals(FileNameSupport.extension(file.getName()))) {
						file = new File(file.getParentFile(), file.getName() + "." + SETTINGS_FILE_NAME_EXT);
					}

					try {
						App.settings().storeTo(file.toPath());

						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.info.export.settings"),
								null,
								JOptionPane.INFORMATION_MESSAGE
								);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.error.export.settings", e1.getLocalizedMessage()),
								null,
								JOptionPane.ERROR_MESSAGE
								);

						App.handleError(e1.getMessage(), e1);
					}
				}
			}
		});
		mnSettings.add(mntmExportSettings);

		mnHelp = new JMenu(Messages.getString("MainFrame.menu.help"));
		mnHelp.setMnemonic(KeyEvent.VK_H);
		menuBar.add(mnHelp);

		mntmHelp = new JMenuItem(Messages.getString("MainFrame.menu.help.help"));
		mntmHelp.setMnemonic(KeyEvent.VK_H);
		mntmHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HelpDialog helpDialog = new HelpDialog();
				helpDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
				helpDialog.setLocationRelativeTo(frame);
				helpDialog.setVisible(true);
			}
		});
		mnHelp.add(mntmHelp);

	}

	private void buildContentPane() {
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{427, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

	}

	private void buildSourcePanel() {
		pnlSrcConditions = new JPanel();
		GridBagConstraints gbc_pnlSrcConditions = new GridBagConstraints();
		gbc_pnlSrcConditions.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlSrcConditions.anchor = GridBagConstraints.NORTH;
		gbc_pnlSrcConditions.insets = new Insets(0, 0, SECTION_GAP, 0);
		gbc_pnlSrcConditions.gridx = 0;
		gbc_pnlSrcConditions.gridy = 0;
		getContentPane().add(pnlSrcConditions, gbc_pnlSrcConditions);
		GridBagLayout gbl_pnlSrcConditions = new GridBagLayout();
		gbl_pnlSrcConditions.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSrcConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlSrcConditions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSrcConditions.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSrcConditions.setLayout(gbl_pnlSrcConditions);

		JLabel lblSrcConditionsTitle = newMainLabel(Messages.getString("MainFrame.srcConditionsTitle"));
		lblSrcConditionsTitle.setDisplayedMnemonic(KeyEvent.VK_F);
		GridBagConstraints gbc_lblSrcConditionsTitle = new GridBagConstraints();
		gbc_lblSrcConditionsTitle.anchor = GridBagConstraints.WEST;
		gbc_lblSrcConditionsTitle.insets = new Insets(0, 0, 5, 8);
		gbc_lblSrcConditionsTitle.gridx = 0;
		gbc_lblSrcConditionsTitle.gridy = 0;
		pnlSrcConditions.add(lblSrcConditionsTitle, gbc_lblSrcConditionsTitle);

		pnlSrcRootDirPath = new JPanel();
		pnlSrcRootDirPath.setBorder(null);
		GridBagConstraints gbc_pnlSrcRootDirPath = new GridBagConstraints();
		gbc_pnlSrcRootDirPath.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSrcRootDirPath.fill = GridBagConstraints.BOTH;
		gbc_pnlSrcRootDirPath.gridx = 1;
		gbc_pnlSrcRootDirPath.gridy = 0;
		pnlSrcConditions.add(pnlSrcRootDirPath, gbc_pnlSrcRootDirPath);
		pnlSrcRootDirPath.setLayout(new BorderLayout(INLINE_HGAP, 0));

		btnSrcRootDirSelect = new JButton(Messages.getString("MainFrame.srcRootDirSelect"));
		configureFolderSelectButton(btnSrcRootDirSelect);
		btnSrcOptions = newOptionsToggleButton(Messages.getString("MainFrame.srcOptionsTitle"));
		JPanel pnlSrcRootDirActions = new JPanel(new BorderLayout(0, 0));
		pnlSrcRootDirActions.add(btnSrcOptions, BorderLayout.EAST);
		pnlSrcRootDirPath.add(pnlSrcRootDirActions, BorderLayout.EAST);

		txtSrcRootDirPath = new JTextField();
		txtSrcRootDirPath.putClientProperty("JTextField.trailingComponent", btnSrcRootDirSelect);
		txtSrcRootDirPath.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, UIManager.getIcon("FileView.directoryIcon"));
		lblSrcConditionsTitle.setLabelFor(txtSrcRootDirPath);
		txtSrcRootDirPath.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.srcRootDirPath"));
		txtSrcRootDirPath.setToolTipText(Messages.getString("MainFrame.srcRootDirPath"));
		pnlSrcRootDirPath.add(txtSrcRootDirPath, BorderLayout.CENTER);
		txtSrcRootDirPath.setColumns(10);
		InputSupport.installLabelFocusAction(lblSrcConditionsTitle, txtSrcRootDirPath, LabelFocusBehavior.CARET_END);
		InputSupport.installFolderDropTarget(txtSrcRootDirPath);
		new FolderHistoryPopup(txtSrcRootDirPath, InputHistory.SRC_ROOT_DIR_KEY);
		installFolderChooserButton(btnSrcRootDirSelect, txtSrcRootDirPath);

		sourceOptionsPanel = new SourceOptionsPanel(INLINE_HGAP, INLINE_VGAP);
		stylizeOptionsBody(sourceOptionsPanel, OPTIONS_BODY_TOP_PADDING_WITH_MATCH_COUNT);
		sourceOptionsPanel.matchCountLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				matchCountLabelClicked();
			}
		});
		sourceOptionsPanel.matchCountStopButton.addActionListener(_ -> matchCountStopButtonClicked());

		GridBagConstraints gbc_sourceOptionsPanel = new GridBagConstraints();
		gbc_sourceOptionsPanel.fill = GridBagConstraints.BOTH;
		gbc_sourceOptionsPanel.gridwidth = 2;
		gbc_sourceOptionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_sourceOptionsPanel.gridx = 1;
		gbc_sourceOptionsPanel.gridy = 1;
		pnlSrcConditions.add(sourceOptionsPanel, gbc_sourceOptionsPanel);
		setOptionsExpanded(btnSrcOptions, sourceOptionsPanel, Messages.getString("MainFrame.srcOptionsTitle"), false);
		btnSrcOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnSrcOptions, sourceOptionsPanel, Messages.getString("MainFrame.srcOptionsTitle"), btnSrcOptions.isSelected());
			}
		});

		txtSrcOptionsSummary = newOptionsSummaryText(btnSrcOptions, sourceOptionsPanel, Messages.getString("MainFrame.srcOptionsTitle"));
		GridBagConstraints gbc_txtSrcOptionsSummary = new GridBagConstraints();
		gbc_txtSrcOptionsSummary.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSrcOptionsSummary.gridwidth = 2;
		gbc_txtSrcOptionsSummary.insets = new Insets(0, 0, 5, 0);
		gbc_txtSrcOptionsSummary.gridx = 1;
		gbc_txtSrcOptionsSummary.gridy = 1;
		pnlSrcConditions.add(txtSrcOptionsSummary, gbc_txtSrcOptionsSummary);

		lblFilePattern = sourceOptionsPanel.filePatternLabel;
		txtFilePattern = sourceOptionsPanel.filePatternTextField;
		cmbFilePatternSyntax = sourceOptionsPanel.filePatternSyntaxComboBox;
		chkContainsSubs = sourceOptionsPanel.containsSubsCheckBox;
		chkContainsHiddens = sourceOptionsPanel.containsHiddensCheckBox;
		lblFileSizeRange = sourceOptionsPanel.fileSizeRangeLabel;
		txtFileSizeRangeFrom = sourceOptionsPanel.fileSizeRangeFromTextField;
		lblFileSizeRangeTo = sourceOptionsPanel.fileSizeRangeToLabel;
		txtFileSizeRangeTo = sourceOptionsPanel.fileSizeRangeToTextField;
		cmbFileSizeUnit = sourceOptionsPanel.fileSizeUnitComboBox;
		lblCreationTimeRange = sourceOptionsPanel.creationTimeRangeLabel;
		txtCreationTimeRangeFrom = sourceOptionsPanel.creationTimeRangeFromTextField;
		lblCreationTimeRangeTo = sourceOptionsPanel.creationTimeRangeToLabel;
		txtCreationTimeRangeTo = sourceOptionsPanel.creationTimeRangeToTextField;
		lblModifiedTimeRange = sourceOptionsPanel.modifiedTimeRangeLabel;
		txtModifiedTimeRangeFrom = sourceOptionsPanel.modifiedTimeRangeFromTextField;
		lblModifiedTimeRangeTo = sourceOptionsPanel.modifiedTimeRangeToLabel;
		txtModifiedTimeRangeTo = sourceOptionsPanel.modifiedTimeRangeToTextField;
	}

	private void buildOperationPanel() {
		pnlOperation = new JPanel();
		GridBagConstraints gbc_pnlOperation = new GridBagConstraints();
		gbc_pnlOperation.insets = new Insets(0, MAIN_LABEL_WIDTH + 8, OPERATION_BOTTOM_GAP, 0);
		gbc_pnlOperation.anchor = GridBagConstraints.NORTH;
		gbc_pnlOperation.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlOperation.gridx = 0;
		gbc_pnlOperation.gridy = 1;
		getContentPane().add(pnlOperation, gbc_pnlOperation);
		GridBagLayout gbl_pnlOperation = new GridBagLayout();
		gbl_pnlOperation.columnWidths = new int[]{0, 0, 0};
		gbl_pnlOperation.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlOperation.rowHeights = new int[]{0, 0};
		gbl_pnlOperation.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlOperation.setLayout(gbl_pnlOperation);

		pnlOpeType = new JPanel();
		pnlOpeType.setBorder(null);
		GridBagConstraints gbc_pnlOpeType = new GridBagConstraints();
		gbc_pnlOpeType.anchor = GridBagConstraints.WEST;
		gbc_pnlOpeType.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlOpeType.gridx = 0;
		gbc_pnlOpeType.gridy = 0;
		pnlOperation.add(pnlOpeType, gbc_pnlOpeType);
		pnlOpeType.setLayout(new FlowLayout(FlowLayout.LEFT, INLINE_HGAP, 0));

		rdoOpeTypeCopy = new JRadioButton(Messages.getString("MainFrame.opeTypeCopy"));
		rdoOpeTypeCopy.setMnemonic(KeyEvent.VK_C);
		btngrpOpeType.add(rdoOpeTypeCopy);
		pnlOpeType.add(rdoOpeTypeCopy);

		rdoOpeTypeMove = new JRadioButton(Messages.getString("MainFrame.opeTypeMove"));
		rdoOpeTypeMove.setMnemonic(KeyEvent.VK_M);
		btngrpOpeType.add(rdoOpeTypeMove);
		pnlOpeType.add(rdoOpeTypeMove);

		rdoOpeTypeOverwrite = new JRadioButton(Messages.getString("MainFrame.opeTypeOverwrite"));
		rdoOpeTypeOverwrite.setMnemonic(KeyEvent.VK_O);
		btngrpOpeType.add(rdoOpeTypeOverwrite);
		pnlOpeType.add(rdoOpeTypeOverwrite);

	}

	private void buildDestinationPanel() {
		pnlDestConditions = new JPanel();
		GridBagConstraints gbc_pnlDestConditions = new GridBagConstraints();
		gbc_pnlDestConditions.insets = new Insets(0, 0, SECTION_GAP, 0);
		gbc_pnlDestConditions.anchor = GridBagConstraints.NORTH;
		gbc_pnlDestConditions.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlDestConditions.gridx = 0;
		gbc_pnlDestConditions.gridy = 2;
		getContentPane().add(pnlDestConditions, gbc_pnlDestConditions);
		GridBagLayout gbl_pnlDestConditions = new GridBagLayout();
		gbl_pnlDestConditions.columnWidths = new int[]{0, 0, 0};
		gbl_pnlDestConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlDestConditions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlDestConditions.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlDestConditions.setLayout(gbl_pnlDestConditions);

		lblDestConditionsTitle = newMainLabel(Messages.getString("MainFrame.destConditionsTitle"));
		lblDestConditionsTitle.setDisplayedMnemonic(KeyEvent.VK_T);
		GridBagConstraints gbc_lblDestConditionsTitle = new GridBagConstraints();
		gbc_lblDestConditionsTitle.anchor = GridBagConstraints.WEST;
		gbc_lblDestConditionsTitle.insets = new Insets(0, 0, 5, 8);
		gbc_lblDestConditionsTitle.gridx = 0;
		gbc_lblDestConditionsTitle.gridy = 0;
		pnlDestConditions.add(lblDestConditionsTitle, gbc_lblDestConditionsTitle);

		pnlDestRootDirPath = new JPanel();
		pnlDestRootDirPath.setBorder(null);
		GridBagConstraints gbc_pnlDestRootDirPath = new GridBagConstraints();
		gbc_pnlDestRootDirPath.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDestRootDirPath.fill = GridBagConstraints.BOTH;
		gbc_pnlDestRootDirPath.gridx = 1;
		gbc_pnlDestRootDirPath.gridy = 0;
		pnlDestConditions.add(pnlDestRootDirPath, gbc_pnlDestRootDirPath);
		pnlDestRootDirPath.setLayout(new BorderLayout(INLINE_HGAP, 0));

		btnDestRootDirSelect = new JButton(Messages.getString("MainFrame.destRootDirSelect"));
		configureFolderSelectButton(btnDestRootDirSelect);
		btnDestOptions = newOptionsToggleButton(Messages.getString("MainFrame.destOptionsTitle"));
		JPanel pnlDestRootDirActions = new JPanel(new BorderLayout(0, 0));
		pnlDestRootDirActions.add(btnDestOptions, BorderLayout.EAST);
		pnlDestRootDirPath.add(pnlDestRootDirActions, BorderLayout.EAST);

		txtDestRootDirPath = new JTextField();
		txtDestRootDirPath.putClientProperty("JTextField.trailingComponent", btnDestRootDirSelect);
		txtDestRootDirPath.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, UIManager.getIcon("FileView.directoryIcon"));
		lblDestConditionsTitle.setLabelFor(txtDestRootDirPath);
		txtDestRootDirPath.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.destRootDirPath"));
		txtDestRootDirPath.setToolTipText(Messages.getString("MainFrame.destRootDirPath"));
		pnlDestRootDirPath.add(txtDestRootDirPath, BorderLayout.CENTER);
		txtDestRootDirPath.setColumns(10);
		InputSupport.installLabelFocusAction(lblDestConditionsTitle, txtDestRootDirPath, LabelFocusBehavior.CARET_END);
		InputSupport.installFolderDropTarget(txtDestRootDirPath);
		new FolderHistoryPopup(txtDestRootDirPath, InputHistory.DEST_ROOT_DIR_KEY);
		installFolderChooserButton(btnDestRootDirSelect, txtDestRootDirPath);

		destinationOptionsPanel = new DestinationOptionsPanel();
		stylizeOptionsBody(destinationOptionsPanel);
		GridBagConstraints gbc_destinationOptionsPanel = new GridBagConstraints();
		gbc_destinationOptionsPanel.fill = GridBagConstraints.BOTH;
		gbc_destinationOptionsPanel.gridwidth = 2;
		gbc_destinationOptionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_destinationOptionsPanel.gridx = 1;
		gbc_destinationOptionsPanel.gridy = 1;
		pnlDestConditions.add(destinationOptionsPanel, gbc_destinationOptionsPanel);
		setOptionsExpanded(btnDestOptions, destinationOptionsPanel, Messages.getString("MainFrame.destOptionsTitle"), false);
		btnDestOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnDestOptions, destinationOptionsPanel, Messages.getString("MainFrame.destOptionsTitle"), btnDestOptions.isSelected());
			}
		});

		txtDestOptionsSummary = newOptionsSummaryText(btnDestOptions, destinationOptionsPanel, Messages.getString("MainFrame.destOptionsTitle"));
		GridBagConstraints gbc_txtDestOptionsSummary = new GridBagConstraints();
		gbc_txtDestOptionsSummary.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDestOptionsSummary.gridwidth = 2;
		gbc_txtDestOptionsSummary.insets = new Insets(0, 0, 5, 0);
		gbc_txtDestOptionsSummary.gridx = 1;
		gbc_txtDestOptionsSummary.gridy = 1;
		pnlDestConditions.add(txtDestOptionsSummary, gbc_txtDestOptionsSummary);

		lblDestSubPathPattern = destinationOptionsPanel.destSubPathPatternLabel;
		txtDestSubPathPattern = destinationOptionsPanel.destSubPathPatternTextField;
		lblExistingFileMethod = destinationOptionsPanel.existingFileMethodLabel;
		cmbExistingFileMethod = destinationOptionsPanel.existingFileMethodComboBox;
		lblValidateFile = destinationOptionsPanel.validateFileLabel;
		chkCheckFileDigest = destinationOptionsPanel.checkFileDigestCheckBox;

	}

	private void buildChangesPanel() {
		btnChanges = newOptionsToggleButton(Messages.getString("MainFrame.changesTitle"));
		btnChanges.setMnemonic(KeyEvent.VK_G);
		btnChanges.setFont(btnChanges.getFont().deriveFont(Font.BOLD, btnChanges.getFont().getSize2D() + 1.0f));
		GridBagConstraints gbc_btnChanges = new GridBagConstraints();
		gbc_btnChanges.anchor = GridBagConstraints.WEST;
		gbc_btnChanges.insets = new Insets(0, 0, SECTION_HEADER_GAP, 0);
		gbc_btnChanges.gridx = 0;
		gbc_btnChanges.gridy = 3;
		contentPane.add(btnChanges, gbc_btnChanges);

		ChangesPanel changesPanel = new ChangesPanel(
				SECTION_PADDING,
				INLINE_HGAP,
				INLINE_VGAP,
				this::changeEnableFileDateModConditions,
				this::fitWindowToContent);
		tabModConditions = changesPanel;
		GridBagConstraints gbc_tabModConditions = new GridBagConstraints();
		gbc_tabModConditions.insets = new Insets(0, 0, SECTION_GAP, 0);
		gbc_tabModConditions.fill = GridBagConstraints.BOTH;
		gbc_tabModConditions.gridx = 0;
		gbc_tabModConditions.gridy = 5;
		contentPane.add(tabModConditions, gbc_tabModConditions);

		txtChangesSummary = newOptionsSummaryText(btnChanges, tabModConditions, Messages.getString("MainFrame.changesTitle"));
		GridBagConstraints gbc_txtChangesSummary = new GridBagConstraints();
		gbc_txtChangesSummary.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtChangesSummary.insets = new Insets(0, MAIN_LABEL_WIDTH + 8, SECTION_GAP, 0);
		gbc_txtChangesSummary.gridx = 0;
		gbc_txtChangesSummary.gridy = 4;
		contentPane.add(txtChangesSummary, gbc_txtChangesSummary);

		lblTargetDate = changesPanel.targetDateLabel;
		chkChangeFileCreationDate = changesPanel.changeFileCreationDateCheckBox;
		chkChangeFileModifiedDate = changesPanel.changeFileModifiedDateCheckBox;
		chkChangeFileAccessDate = changesPanel.changeFileAccessDateCheckBox;
		chkChangeExifDate = changesPanel.changeExifDateCheckBox;
		lblBaseDateType = changesPanel.baseDateTypeLabel;
		cmbBaseDateType = changesPanel.baseDateTypeComboBox;
		txtCustomBaseDate = changesPanel.customBaseDateTextField;
		lblEditBaseDate = changesPanel.editBaseDateLabel;
		cmbDateModType = changesPanel.dateModTypeComboBox;
		txtDateModYears = changesPanel.dateModYearsTextField;
		lblSepYM = changesPanel.yearMonthSeparatorLabel;
		txtDateModMonths = changesPanel.dateModMonthsTextField;
		lblSepMD = changesPanel.monthDaySeparatorLabel;
		txtDateModDays = changesPanel.dateModDaysTextField;
		lblSepDH = changesPanel.dayHourSeparatorLabel;
		txtDateModHours = changesPanel.dateModHoursTextField;
		lblSepHM = changesPanel.hourMinuteSeparatorLabel;
		txtDateModMinutes = changesPanel.dateModMinutesTextField;
		lblSepMS = changesPanel.minuteSecondSeparatorLabel;
		txtDateModSeconds = changesPanel.dateModSecondsTextField;
		chkRemoveExifTagsGps = changesPanel.removeExifTagsGpsCheckBox;
		chkRemoveExifTagsAll = changesPanel.removeExifTagsAllCheckBox;

		setOptionsExpanded(btnChanges, tabModConditions, Messages.getString("MainFrame.changesTitle"), false);
		btnChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnChanges, tabModConditions, Messages.getString("MainFrame.changesTitle"), btnChanges.isSelected());
			}
		});

	}

	private void buildControlsPanel() {
		pnlControls = new JPanel();
		GridBagConstraints gbc_pnlControls = new GridBagConstraints();
		gbc_pnlControls.anchor = GridBagConstraints.SOUTH;
		gbc_pnlControls.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlControls.gridx = 0;
		gbc_pnlControls.gridy = 6;
		getContentPane().add(pnlControls, gbc_pnlControls);
		pnlControls.setLayout(new BorderLayout(0, INLINE_VGAP));

		btnStart = new JButton(Messages.getString("MainFrame.start"));
		btnStart.setMnemonic(KeyEvent.VK_R);
		btnStart.putClientProperty("JButton.buttonType", "default");
		btnStart.setMargin(new Insets(7, 28, 7, 28));
		btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runProcess(false);
			}
		});

		btnStartMenu = new JButton("\u25be");
		btnStartMenu.putClientProperty(FlatClientProperties.BUTTON_TYPE, "default");
		btnStartMenu.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 0);
		btnStartMenu.setMargin(new Insets(7, 5, 7, 5));
		btnStartMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		Dimension btnStartMenuSize = btnStartMenu.getPreferredSize();
		btnStartMenuSize.width = RUN_MENU_BUTTON_WIDTH;
		btnStartMenu.setPreferredSize(btnStartMenuSize);
		JPopupMenu runMenu = new JPopupMenu();
		JMenuItem mntmDryRun = new JMenuItem(Messages.getString("MainFrame.dryRun"));
		mntmDryRun.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		mntmDryRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runProcess(true);
			}
		});
		runMenu.add(mntmDryRun);
		btnStartMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runMenu.show(btnStart, 0, btnStart.getHeight());
			}
		});

		JPanel pnlRunButton = new JPanel(new FlowLayout(FlowLayout.CENTER, INLINE_HGAP, 0));
		pnlRunButton.setBorder(null);
		pnlRunButton.add(newRunSplitButtonPanel(btnStart, btnStartMenu));

		// A fixed-size square, regardless of whether an icon is currently set, so toggling it on/off (or
		// between the processing/results icons) never shifts btnStart's own centered position. The icon is
		// centered inside it with room to spare on every side, so the circular hover chip (a square with an
		// arc equal to its own size rounds into a true circle) reads as clickable padding around the icon
		// rather than a shape hugging it tightly.
		int runStatusDiameter = RUN_STATUS_ICON_SIZE + RUN_STATUS_ICON_PADDING * 2;
		lblRunStatus = new JLabel();
		lblRunStatus.setPreferredSize(new Dimension(runStatusDiameter, runStatusDiameter));
		lblRunStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblRunStatus.setOpaque(true);
		lblRunStatus.putClientProperty(FlatClientProperties.STYLE, "arc: " + runStatusDiameter);
		Color runStatusBackground = lblRunStatus.getBackground();
		Color runStatusHoverBackground = shade(color("Panel.background", new Color(0xf2f2f2)));
		lblRunStatus.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showLastProcessDialog();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// Nothing to click through to yet (no run has happened since the last one was superseded) -
				// the hover highlight would otherwise falsely suggest this spot does something right now.
				if (lblRunStatus.getIcon() != null) {
					lblRunStatus.setBackground(runStatusHoverBackground);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				lblRunStatus.setBackground(runStatusBackground);
			}
		});
		pnlRunButton.add(lblRunStatus);

		pnlControls.add(pnlRunButton, BorderLayout.CENTER);

		lblRunSummary = newRunSummaryLabel();
		installRunSummaryHover(btnStart);
		installRunSummaryHover(btnStartMenu);
		pnlControls.add(lblRunSummary, BorderLayout.SOUTH);

	}

	private void installKeyboardShortcuts() {
		int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		JRootPane rootPane = getRootPane();

		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcutKeyMask), "picto.run");
		rootPane.getActionMap().put("picto.run", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				runProcess(false);
			}
		});

		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK), "picto.dryRun");
		rootPane.getActionMap().put("picto.dryRun", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				runProcess(true);
			}
		});

		for (int digit = 1; digit <= PRESET_SHORTCUT_COUNT; digit++) {
			int index = digit - 1;
			String actionKey = "picto.loadPreset" + digit;
			rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(presetShortcutKeyStroke(digit), actionKey);
			rootPane.getActionMap().put(actionKey, new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					loadPresetByIndex(index);
				}
			});
		}
	}

	private void installOperationListeners() {
		rdoOpeTypeCopy.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableDestConditions();
			}
		});
		rdoOpeTypeMove.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableDestConditions();
			}
		});
		rdoOpeTypeOverwrite.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableDestConditions();
			}
		});

	}

	protected void loadSettings() {
		applySettingsFrom(App.settings());
	}

	/**
	 * One persisted setting's key paired with how to load it into its Swing field and how to read it back
	 * out, declared once so {@link #applySettingsFrom} and {@link #captureSettingsInto} can't drift apart on
	 * the key, default, or field a future change touches only one side of.
	 */
	private record SettingBinding(Consumer<AppSettings> applyFrom, Consumer<AppSettings> captureInto) {
		static SettingBinding text(JTextComponent field, String key, String defaultValue) {
			return new SettingBinding(
					conf -> field.setText(conf.getString(key, defaultValue)),
					conf -> conf.set(key, field.getText()));
		}

		static SettingBinding flag(AbstractButton button, String key, boolean defaultValue) {
			return new SettingBinding(
					conf -> button.setSelected(conf.getBoolean(key, defaultValue)),
					conf -> conf.set(key, button.isSelected()));
		}

		static <E extends Enum<E>> SettingBinding choice(JComboBox<E> combo, String key, Class<E> type, E defaultValue) {
			return new SettingBinding(
					conf -> combo.setSelectedItem(conf.getEnum(key, type, defaultValue)),
					conf -> conf.set(key, combo.getSelectedItem()));
		}
	}

	private List<SettingBinding> settingBindings() {
		return List.of(
				SettingBinding.text(txtSrcRootDirPath, "src.root.dir", ""),
				SettingBinding.text(txtFilePattern, "file.pattern", ""),
				SettingBinding.choice(cmbFilePatternSyntax, "file.pattern.syntax", FilePatternSyntax.class, FilePatternSyntax.GLOB),
				SettingBinding.flag(chkContainsSubs, "contains.subs", true),
				SettingBinding.flag(chkContainsHiddens, "contains.hiddens", false),

				SettingBinding.text(txtFileSizeRangeFrom, "file.size.range.from", ""),
				SettingBinding.text(txtFileSizeRangeTo, "file.size.range.to", ""),
				SettingBinding.choice(cmbFileSizeUnit, "file.size.unit", FileSizeUnit.class, FileSizeUnit.MB),
				SettingBinding.text(txtCreationTimeRangeFrom, "creation.time.range.from", ""),
				SettingBinding.text(txtCreationTimeRangeTo, "creation.time.range.to", ""),
				SettingBinding.text(txtModifiedTimeRangeFrom, "modified.time.range.from", ""),
				SettingBinding.text(txtModifiedTimeRangeTo, "modified.time.range.to", ""),

				SettingBinding.flag(rdoOpeTypeCopy, "ope.type.copy", true),
				SettingBinding.flag(rdoOpeTypeMove, "ope.type.move", false),
				SettingBinding.flag(rdoOpeTypeOverwrite, "ope.type.overwrite", false),

				SettingBinding.text(txtDestRootDirPath, "dest.root.dir", ""),
				SettingBinding.text(txtDestSubPathPattern, "dest.sub.path.pattern", DEFAULT_DEST_SUB_PATH_PATTERN),
				SettingBinding.choice(cmbExistingFileMethod, "existing.file.method", ExistingFileMethod.class, ExistingFileMethod.Confirm),
				SettingBinding.flag(chkCheckFileDigest, "check.file.digest", false),

				SettingBinding.flag(chkChangeFileCreationDate, "change.file.creation.date", false),
				SettingBinding.flag(chkChangeFileModifiedDate, "change.file.modified.date", false),
				SettingBinding.flag(chkChangeFileAccessDate, "change.file.access.date", false),
				SettingBinding.flag(chkChangeExifDate, "change.file.exif.date", false),
				SettingBinding.choice(cmbBaseDateType, "base.date.type", DateType.class, DateType.FileModifiedDate),
				SettingBinding.text(txtCustomBaseDate, "custom.base.date", ""),
				SettingBinding.choice(cmbDateModType, "date.mod.type", DateModType.class, DateModType.None),
				SettingBinding.text(txtDateModYears, "date.mod.year", ""),
				SettingBinding.text(txtDateModMonths, "date.mod.month", ""),
				SettingBinding.text(txtDateModDays, "date.mod.day", ""),
				SettingBinding.text(txtDateModHours, "date.mod.hour", ""),
				SettingBinding.text(txtDateModMinutes, "date.mod.minute", ""),
				SettingBinding.text(txtDateModSeconds, "date.mod.second", ""),
				SettingBinding.flag(chkRemoveExifTagsGps, "remove.exif.tags.gps", false),
				SettingBinding.flag(chkRemoveExifTagsAll, "remove.exif.tags.all", false));
	}

	private void applySettingsFrom(AppSettings conf) {
		for (SettingBinding binding : settingBindings()) {
			binding.applyFrom().accept(conf);
		}
	}

	protected void storeSettings() throws IOException {
		AppSettings conf = App.settings();

		conf.set(AppMain.PREF_LOCALE_KEY, conf.getString(AppMain.PREF_LOCALE_KEY, AppMain.PREF_SYSTEM));
		conf.set(AppMain.PREF_APPEARANCE_KEY, conf.getString(AppMain.PREF_APPEARANCE_KEY, AppMain.PREF_SYSTEM));

		captureSettingsInto(conf);

		conf.store();
		App.deleteMigratedLegacySettingsIfNeeded();
	}

	private void captureSettingsInto(AppSettings conf) {
		for (SettingBinding binding : settingBindings()) {
			binding.captureInto().accept(conf);
		}
	}

	private void rebuildPresetsMenu() {
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
				PresetsManageDialog manageDialog = new PresetsManageDialog(MainFrame.this);
				manageDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
				manageDialog.setLocationRelativeTo(frame);
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

		JPanel panel = new JPanel(new BorderLayout(0, INLINE_VGAP));
		panel.add(new JLabel(Messages.getString("message.prompt.preset.name")), BorderLayout.NORTH);
		panel.add(nameComboBox, BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(
				frame,
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
			JOptionPane.showMessageDialog(frame, Messages.getString("message.warn.preset.name.empty"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		boolean alreadyExists = false;
		for (PresetEntry preset : listPresets()) {
			if (preset.name().equals(name)) {
				alreadyExists = true;
				break;
			}
		}
		if (alreadyExists) {
			int overwrite = JOptionPane.showConfirmDialog(
					frame,
					Messages.getString("message.confirm.preset.overwrite", name),
					null,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
					);
			if (overwrite != JOptionPane.YES_OPTION) {
				return;
			}
		}

		try {
			saveCurrentSettingsAsPreset(name);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(
					frame,
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
		captureSettingsInto(presetSettings);
		presetSettings.addComment(name);
		presetSettings.store();
	}

	private static KeyStroke presetShortcutKeyStroke(int digit) {
		int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		return KeyStroke.getKeyStroke(KeyEvent.VK_0 + digit, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK);
	}

	private void loadPresetByIndex(int index) {
		List<PresetEntry> presets = listPresets();
		if (index >= presets.size()) {
			return;
		}
		confirmAndLoadPreset(presets.get(index));
	}

	private void confirmAndLoadPreset(PresetEntry preset) {
		int result = JOptionPane.showConfirmDialog(
				frame,
				Messages.getString("message.confirm.preset.load", preset.name()),
				null,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
				);
		if (result != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			loadPreset(preset.fileName());
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(
					frame,
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

		AppSettings conf = App.settings();
		for (String key : presetSettings.keySet()) {
			conf.set(key, presetSettings.get(key));
		}

		loadSettings();
	}

	void renamePreset(String fileName, String newName) throws IOException {
		AppSettings presetSettings = AppSettings.of(presetsDirectory(), fileName);
		presetSettings.load();
		presetSettings.clearComments();
		presetSettings.addComment(newName);
		presetSettings.store();
	}

	void deletePreset(String fileName) throws IOException {
		Files.delete(AppSettings.of(presetsDirectory(), fileName).path());
	}

	private AppSettingsDirectory presetsDirectory() {
		return AppSettings.directory(App.GROUP_NAME, App.APP_NAME, PRESETS_DIR_NAME);
	}

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

	private void addSettingsMenuItem(JMenu menu, ButtonGroup group, String label, String key, String value, int mnemonic) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
		if (mnemonic != 0) {
			item.setMnemonic(mnemonic);
		}
		item.setActionCommand(value);
		item.setSelected(value.equals(App.settings().getString(key, AppMain.PREF_SYSTEM)));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (processing) {
					selectCurrentSettingsMenuItem(group, key);
					showSettingsProcessingMessage();
					return;
				}
				if (!value.equals(App.settings().getString(key, AppMain.PREF_SYSTEM))) {
					App.settings().set(key, value);
					try {
						storeSettings();
						applySettingsImmediately();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(
								frame,
								Messages.getString("message.error.store.settings", e1.getLocalizedMessage()),
								null,
								JOptionPane.ERROR_MESSAGE
								);
						App.handleError(e1.getMessage(), e1);
					}
				}
			}
		});
		group.add(item);
		menu.add(item);
	}

	private static void selectCurrentSettingsMenuItem(ButtonGroup group, String key) {
		String currentValue = App.settings().getString(key, AppMain.PREF_SYSTEM);
		for (java.util.Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements();) {
			AbstractButton button = e.nextElement();
			if (currentValue.equals(button.getActionCommand())) {
				button.setSelected(true);
				break;
			}
		}
	}

	private void showSettingsProcessingMessage() {
		JOptionPane.showMessageDialog(
				frame,
				Messages.getString("message.warn.settings.processing"),
				null,
				JOptionPane.WARNING_MESSAGE
				);
	}

	private void applySettingsImmediately() {
		MainFrameState state = captureFrameState();
		if (sourceFileScanner != null) {
			// This frame is about to be discarded for a replacement with the new language/theme; without
			// this, the scan's background thread would keep walking the folder tree indefinitely (nothing
			// else ever stops it) purely to update a disposed, invisible frame's label.
			sourceFileScanner.cancel();
		}
		AppMain.applyConfiguredUiSettings();
		MainFrame nextFrame = new MainFrame(state);
		nextFrame.setVisible(true);
		dispose();
	}

	private MainFrameState captureFrameState() {
		return new MainFrameState(
				getBounds(),
				getExtendedState(),
				btnSrcOptions.isSelected(),
				btnDestOptions.isSelected(),
				btnChanges.isSelected(),
				tabModConditions.getSelectedIndex());
	}

	private void restoreFrameState(MainFrameState state) {
		if (state == null) {
			return;
		}
		if (state.bounds != null) {
			setBounds(state.bounds);
		}
		setOptionsExpanded(btnSrcOptions, sourceOptionsPanel, Messages.getString("MainFrame.srcOptionsTitle"), state.srcOptionsExpanded);
		setOptionsExpanded(btnDestOptions, destinationOptionsPanel, Messages.getString("MainFrame.destOptionsTitle"), state.destOptionsExpanded);
		setOptionsExpanded(btnChanges, tabModConditions, Messages.getString("MainFrame.changesTitle"), state.changesExpanded);
		if (state.changesTabIndex >= 0 && state.changesTabIndex < tabModConditions.getTabCount()) {
			tabModConditions.setSelectedIndex(state.changesTabIndex);
		}
		if (state.extendedState != Frame.NORMAL) {
			setExtendedState(state.extendedState);
		}
	}

	private JLabel newMainLabel(String title) {
		JLabel label = new JLabel(title);
		label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D() + 1.0f));
		Dimension size = new Dimension(MAIN_LABEL_WIDTH, label.getPreferredSize().height);
		label.setMinimumSize(size);
		label.setPreferredSize(size);
		return label;
	}

	private static JPanel newRunSplitButtonPanel(JButton mainButton, JButton menuButton) {
		JPanel panel = new JPanel(null) {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension mainSize = mainButton.getPreferredSize();
				Dimension menuSize = menuButton.getPreferredSize();
				return new Dimension(
						mainSize.width + menuSize.width - RUN_SPLIT_BUTTON_OVERLAP,
						Math.max(mainSize.height, menuSize.height));
			}

			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}

			@Override
			public void doLayout() {
				Dimension mainSize = mainButton.getPreferredSize();
				Dimension menuSize = menuButton.getPreferredSize();
				int height = Math.max(mainSize.height, menuSize.height);
				mainButton.setBounds(0, 0, mainSize.width, height);
				menuButton.setBounds(mainSize.width - RUN_SPLIT_BUTTON_OVERLAP, 0, menuSize.width, height);
			}
		};
		panel.setOpaque(false);
		panel.add(mainButton);
		panel.add(menuButton);
		return panel;
	}

	private JToggleButton newOptionsToggleButton(String title) {
		JToggleButton button = new JToggleButton();
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setMargin(new Insets(2, 4, 2, 4));
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setOptionsToggleButtonText(button, title, false);
		return button;
	}

	private void installFolderChooserButton(JButton button, JTextField targetField) {
		button.addActionListener(_ -> {
			JFileChooser filechooser = new JFileChooser();
			filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (!targetField.getText().isEmpty()) {
				filechooser.setCurrentDirectory(new File(targetField.getText()));
			}

			int selected = filechooser.showOpenDialog(frame);
			if (selected == JFileChooser.APPROVE_OPTION) {
				File file = filechooser.getSelectedFile();
				targetField.setText(file.getAbsolutePath());
			}
		});
	}

	private static void configureFolderSelectButton(JButton button) {
		Color textFieldBackground = color("TextField.background", new Color(0xffffff));
		Color buttonBackground = color("Button.background", new Color(0xf3f3f3));
		Color buttonHoverBackground = color("Button.hoverBackground", buttonBackground);
		Color normalBackground = blend(textFieldBackground, buttonBackground, 0.45f);
		Color hoverBackground = blend(normalBackground, buttonHoverBackground, 0.45f);

		button.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 0);
		button.setMargin(new Insets(2, 8, 2, 8));
		button.setBackground(normalBackground);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				if (button.isEnabled()) {
					button.setBackground(hoverBackground);
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				button.setBackground(normalBackground);
			}
		});
	}

	private static void stylizeOptionsBody(JPanel optionsBody) {
		stylizeOptionsBody(optionsBody, OPTIONS_BODY_PADDING);
	}

	private static void stylizeOptionsBody(JPanel optionsBody, int topPadding) {
		Color panelBackground = color("Panel.background", new Color(0xf2f2f2));
		optionsBody.setOpaque(true);
		optionsBody.setBackground(shade(panelBackground));
		optionsBody.setBorder(BorderFactory.createEmptyBorder(
				topPadding, OPTIONS_BODY_PADDING, OPTIONS_BODY_PADDING, OPTIONS_BODY_PADDING));
		optionsBody.putClientProperty(FlatClientProperties.STYLE, "arc: " + OPTIONS_BODY_ARC);
	}

	private static Color shade(Color base) {
		boolean isLight = (base.getRed() + base.getGreen() + base.getBlue()) / 3 >= MID_BRIGHTNESS;
		int delta = isLight ? OPTIONS_BODY_SHADE_LIGHT : OPTIONS_BODY_SHADE_DARK;
		return new Color(
				clamp(base.getRed() + delta),
				clamp(base.getGreen() + delta),
				clamp(base.getBlue() + delta));
	}

	private static int clamp(int value) {
		return Math.max(0, Math.min(255, value));
	}

	private static Color color(String key, Color fallback) {
		Color color = UIManager.getColor(key);
		return color != null ? color : fallback;
	}

	private static Color blend(Color base, Color overlay, float overlayRatio) {
		float baseRatio = 1.0f - overlayRatio;
		return new Color(
				Math.round(base.getRed() * baseRatio + overlay.getRed() * overlayRatio),
				Math.round(base.getGreen() * baseRatio + overlay.getGreen() * overlayRatio),
				Math.round(base.getBlue() * baseRatio + overlay.getBlue() * overlayRatio));
	}

	private JTextArea newOptionsSummaryText(JToggleButton button, JComponent optionsBody, String title) {
		JTextArea summary = newSummaryText();
		summary.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		summary.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (button.isEnabled()) {
					setOptionsExpanded(button, optionsBody, title, true);
				}
			}
		});
		return summary;
	}

	private JTextArea newSummaryText() {
		JTextArea summary = new JTextArea();
		summary.setEditable(false);
		summary.setFocusable(false);
		summary.setLineWrap(true);
		summary.setWrapStyleWord(true);
		summary.setOpaque(false);
		summary.setBorder(new EmptyBorder(1, 4, 2, 4));
		summary.setFont(summary.getFont().deriveFont(summary.getFont().getSize2D() - 1.0f));
		Color foreground = UIManager.getColor("Label.disabledForeground");
		if (foreground == null) {
			foreground = UIManager.getColor("Label.foreground");
		}
		summary.setForeground(foreground);
		return summary;
	}

	private JLabel newRunSummaryLabel() {
		JLabel label = new JLabel(" ");
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(label.getFont().getSize2D() - 1.0f));
		Color foreground = UIManager.getColor("Label.disabledForeground");
		if (foreground == null) {
			foreground = UIManager.getColor("Label.foreground");
		}
		label.setForeground(foreground);
		return label;
	}

	private void installRunSummaryHover(AbstractButton button) {
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				showingRunSummary = true;
				updateRunSummary();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				showingRunSummary = false;
				updateRunSummary();
			}
		});
	}

	private void setOptionsExpanded(JToggleButton button, JComponent optionsBody, String title, boolean expanded) {
		button.setSelected(expanded);
		setOptionsToggleButtonText(button, title, expanded);
		optionsBody.setVisible(expanded);
		updateOptionsSummaries();
		fitWindowToContent();
	}

	private void installOptionsSummaryListeners() {
		DocumentListener documentListener = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				optionsSummaryChanged();
			}

			public void removeUpdate(DocumentEvent e) {
				optionsSummaryChanged();
			}

			public void changedUpdate(DocumentEvent e) {
				optionsSummaryChanged();
			}
		};
		ChangeListener changeListener = _ -> optionsSummaryChanged();
		ItemListener itemListener = e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				optionsSummaryChanged();
			}
		};

		addDocumentListener(documentListener,
				txtFilePattern,
				txtFileSizeRangeFrom,
				txtFileSizeRangeTo,
				txtCreationTimeRangeFrom,
				txtCreationTimeRangeTo,
				txtModifiedTimeRangeFrom,
				txtModifiedTimeRangeTo,
				txtSrcRootDirPath,
				txtDestRootDirPath,
				txtDestSubPathPattern,
				txtCustomBaseDate,
				txtDateModYears,
				txtDateModMonths,
				txtDateModDays,
				txtDateModHours,
				txtDateModMinutes,
				txtDateModSeconds);
		addChangeListener(changeListener,
				chkContainsSubs,
				chkContainsHiddens,
				chkCheckFileDigest,
				chkChangeFileCreationDate,
				chkChangeFileModifiedDate,
				chkChangeFileAccessDate,
				chkChangeExifDate,
				chkRemoveExifTagsGps,
				chkRemoveExifTagsAll,
				rdoOpeTypeCopy,
				rdoOpeTypeMove,
				rdoOpeTypeOverwrite);
		cmbFilePatternSyntax.addItemListener(itemListener);
		cmbFileSizeUnit.addItemListener(itemListener);
		cmbExistingFileMethod.addItemListener(itemListener);
		cmbBaseDateType.addItemListener(itemListener);
		cmbDateModType.addItemListener(itemListener);
	}

	private static void addDocumentListener(DocumentListener listener, JTextField... fields) {
		for (JTextField field : fields) {
			field.getDocument().addDocumentListener(listener);
		}
	}

	private static void addChangeListener(ChangeListener listener, AbstractButton... buttons) {
		for (AbstractButton button : buttons) {
			button.addChangeListener(listener);
		}
	}

	private void optionsSummaryChanged() {
		updateOptionsSummaries();
		fitWindowToContent();
		if (matchCountEnabled) {
			matchCountTimer.restart();
		}
	}

	private void updateOptionsSummaries() {
		if (txtSrcOptionsSummary == null || txtDestOptionsSummary == null || txtChangesSummary == null || btnStart == null || btnStartMenu == null || lblRunSummary == null) {
			return;
		}
		updateOptionsSummary(txtSrcOptionsSummary, sourceOptionsPanel, sourceOptionsSummary());
		updateOptionsSummary(txtDestOptionsSummary, destinationOptionsPanel, destinationOptionsSummary());
		updateOptionsSummary(txtChangesSummary, tabModConditions, changesSummary());
		updateRunSummary();
	}

	private void updateRunSummary() {
		btnStart.setToolTipText(null);
		btnStartMenu.setToolTipText(null);
		if (showingRunSummary) {
			showRunSummary();
		} else {
			clearRunSummary();
		}
	}

	private void showRunSummary() {
		lblRunSummary.setText(PathTextSupport.abbreviateMiddle(runSummary(), lblRunSummary.getWidth() - 8, lblRunSummary));
	}

	private void clearRunSummary() {
		lblRunSummary.setText(" ");
	}

	private String runSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		String summary = values.operationType + ": "
				+ Messages.getString("MainFrame.srcConditionsTitle") + " "
				+ PathTextSupport.shortPathText(fieldText(txtSrcRootDirPath), Messages.getString("MainFrame.srcRootDirPath"));
		if (values.operationType == OperationType.Overwrite) {
			return summary;
		}
		return summary + " -> "
				+ Messages.getString("MainFrame.destConditionsTitle") + " "
				+ PathTextSupport.shortPathText(fieldText(txtDestRootDirPath), Messages.getString("MainFrame.destRootDirPath"));
	}

	private static void updateOptionsSummary(JTextArea summary, JComponent optionsBody, String text) {
		summary.setText(text);
		summary.setVisible(!optionsBody.isVisible() && !text.isEmpty());
	}

	private String sourceOptionsSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		List<String> items = new ArrayList<>();
		if (!values.filePattern.isEmpty()) {
			String pattern = values.filePattern;
			if (values.filePatternRegex) {
				pattern += " (" + FilePatternSyntax.REGEX + ")";
			}
			items.add(summaryItem(Messages.getString("MainFrame.filePattern"), pattern));
		}
		if (values.depth != 1) {
			items.add(checkedItem(Messages.getString("MainFrame.containsSubs")));
		}
		if (values.containsHiddens) {
			items.add(checkedItem(Messages.getString("MainFrame.containsHiddens")));
		}
		if (values.sizeRangeFrom != null || values.sizeRangeTo != null) {
			String range = rangeText(fieldText(txtFileSizeRangeFrom), fieldText(txtFileSizeRangeTo));
			items.add(summaryItem(Messages.getString("MainFrame.fileSizeRange"), range + " " + cmbFileSizeUnit.getSelectedItem()));
		}
		if (values.creationTimeRangeFrom != null || values.creationTimeRangeTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.creationTimeRange"),
					rangeText(dateFieldText(txtCreationTimeRangeFrom), dateFieldText(txtCreationTimeRangeTo))));
		}
		if (values.modifiedTimeRangeFrom != null || values.modifiedTimeRangeTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.modifiedTimeRange"),
					rangeText(dateFieldText(txtModifiedTimeRangeFrom), dateFieldText(txtModifiedTimeRangeTo))));
		}
		return joinOptionsSummary(items);
	}

	private String destinationOptionsSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		List<String> items = new ArrayList<>();
		if (!values.destSubPathPattern.isBlank() && !DEFAULT_DEST_SUB_PATH_PATTERN.equals(values.destSubPathPattern)) {
			items.add(summaryItem(Messages.getString("MainFrame.destSubPathPattern"), values.destSubPathPattern));
		}
		if (values.existingFileMethod != ExistingFileMethod.Confirm) {
			items.add(summaryItem(Messages.getString("MainFrame.existingFileMethod"), String.valueOf(values.existingFileMethod)));
		}
		if (values.checkDigest) {
			items.add(checkedItem(Messages.getString("MainFrame.validateFile")));
		}
		return joinOptionsSummary(items);
	}

	private String changesSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		List<String> items = new ArrayList<>();
		boolean changesFileDate = false;
		if (values.changeFileCreationDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changeFileCreationDate")));
			changesFileDate = true;
		}
		if (values.changeFileModifiedDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changeFileModifiedDate")));
			changesFileDate = true;
		}
		if (values.changeFileAccessDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changeFileAccessDate")));
			changesFileDate = true;
		}
		if (values.changeExifDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changeFileExifDate")));
			changesFileDate = true;
		}
		if (changesFileDate) {
			items.add(summaryItem(Messages.getString("MainFrame.changeFileBaseDateType"), baseDateTypeSummary(values)));
		}
		if (changesFileDate && values.baseDateModType != DateModType.None) {
			items.add(summaryItem(Messages.getString("MainFrame.changeFileEditBaseDate"), adjustmentSummary(values)));
		}
		if (values.removeExifTagsGps) {
			items.add(checkedItem(Messages.getString("MainFrame.removeExifTagsGps")));
		}
		if (values.removeExifTagsAll) {
			items.add(checkedItem(Messages.getString("MainFrame.removeExifTagsAll")));
		}
		return joinOptionsSummary(items);
	}

	private static String fieldText(JTextField field) {
		String text = field.getText();
		return text == null ? "" : text.trim();
	}

	private static String dateFieldText(JTextField field) {
		return DateTimeText.compact(field.getText());
	}

	private String baseDateTypeSummary(ProcessConditionValues values) {
		String value = String.valueOf(values.baseDateType);
		String customDate = dateFieldText(txtCustomBaseDate);
		if (values.baseDateType == DateType.CustomDate && values.customBaseDate != null && !customDate.isEmpty()) {
			value += " " + customDate;
		}
		return value;
	}

	private String adjustmentSummary(ProcessConditionValues values) {
		String value = String.valueOf(values.baseDateModType);
		List<String> amounts = new ArrayList<>();
		addAdjustmentAmount(amounts, values.baseDateModYears, "Y");
		addAdjustmentAmount(amounts, values.baseDateModMonths, "M");
		addAdjustmentAmount(amounts, values.baseDateModDays, "D");
		addAdjustmentAmount(amounts, values.baseDateModHours, "h");
		addAdjustmentAmount(amounts, values.baseDateModMinutes, "m");
		addAdjustmentAmount(amounts, values.baseDateModSeconds, "s");
		if (!amounts.isEmpty()) {
			value += " " + String.join(" ", amounts);
		}
		return value;
	}

	private static void addAdjustmentAmount(List<String> amounts, Integer value, String suffix) {
		if (value != null) {
			amounts.add(value + suffix);
		}
	}

	private static String rangeText(String from, String to) {
		if (from.isEmpty()) {
			return to;
		}
		if (to.isEmpty()) {
			return from;
		}
		return from + " - " + to;
	}

	private static String summaryItem(String label, String value) {
		return label + ": " + value;
	}

	private static String checkedItem(String label) {
		return CHECKED_ITEM_PREFIX + label;
	}

	private static String joinOptionsSummary(List<String> items) {
		return String.join(" / ", items);
	}

	private void fitWindowToContent() {
		if (!windowLayoutReady) {
			return;
		}
		invalidate();
		if (!isShowing()) {
			int currentWidth = getWidth();
			pack();
			setSize(currentWidth, getHeight());
			return;
		}
		Dimension preferredSize = getPreferredSize();
		setSize(getWidth(), preferredSize.height);
		validate();
	}

	private void setOptionsToggleButtonText(JToggleButton button, String title, boolean expanded) {
		button.setText((expanded ? "\u25be " : "\u25b8 ") + title);
	}

	private void changeEnableDestConditions() {
		setEnableDestConditions(!rdoOpeTypeOverwrite.isSelected());
	}

	private void setEnableDestConditions(boolean enabled) {
		lblDestConditionsTitle.setEnabled(enabled);
		txtDestRootDirPath.setEnabled(enabled);
		btnDestRootDirSelect.setEnabled(enabled);
		btnDestOptions.setEnabled(enabled);
		if (txtDestOptionsSummary != null) {
			txtDestOptionsSummary.setEnabled(enabled);
		}
		lblDestSubPathPattern.setEnabled(enabled);
		txtDestSubPathPattern.setEnabled(enabled);
		lblExistingFileMethod.setEnabled(enabled);
		cmbExistingFileMethod.setEnabled(enabled);
		lblValidateFile.setEnabled(enabled);
		chkCheckFileDigest.setEnabled(enabled);
	}

	private void changeEnableFileDateModConditions() {
		if (chkChangeFileCreationDate.isSelected()
				|| chkChangeFileModifiedDate.isSelected()
				|| chkChangeFileAccessDate.isSelected()
				|| chkChangeExifDate.isSelected()) {
			setEnableFileDateModConditions(true);
		} else {
			setEnableFileDateModConditions(false);
		}
	}

	private void setEnableFileDateModConditions(boolean enabled) {
		lblBaseDateType.setEnabled(enabled);
		cmbBaseDateType.setEnabled(enabled);
		InputSupport.setTextFieldEnabled(txtCustomBaseDate, enabled);
		lblEditBaseDate.setEnabled(enabled);
		cmbDateModType.setEnabled(enabled);

		boolean adjustmentEnabled = enabled && cmbDateModType.getSelectedItem() != DateModType.None;
		InputSupport.setTextFieldEnabled(txtDateModYears, adjustmentEnabled);
		lblSepYM.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtDateModMonths, adjustmentEnabled);
		lblSepMD.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtDateModDays, adjustmentEnabled);
		lblSepDH.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtDateModHours, adjustmentEnabled);
		lblSepHM.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtDateModMinutes, adjustmentEnabled);
		lblSepMS.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtDateModSeconds, adjustmentEnabled);
	}

	private void runProcess(boolean dryRun) {
		if (lastProcessDialog != null && lastProcessDialog.isProcessing()) {
			// A run is still going in the background (the dialog may have just been closed rather than
			// stopped) - surface it instead of starting a second one concurrently over the same files.
			showLastProcessDialog();
			return;
		}

		ProcessCondition processCondition = createProcessCondition(dryRun);

		if (processCondition == null) {
			// Validation failed
			return;
		}

		if (!dryRun) {
			InputHistory.record(InputHistory.SRC_ROOT_DIR_KEY, processCondition.getSrcRootPath());
			if (processCondition.getOperationType() != OperationType.Overwrite) {
				InputHistory.record(InputHistory.DEST_ROOT_DIR_KEY, processCondition.getDestRootPath());
			}
			InputHistory.record(InputHistory.FILE_PATTERN_KEY, fieldText(txtFilePattern));
			InputHistory.record(InputHistory.DEST_SUB_PATH_PATTERN_KEY, fieldText(txtDestSubPathPattern));
		}

		if (lastProcessDialog != null) {
			// The previous run already finished (isProcessing() is false above) and the user has moved on
			// to a new one; its results are no longer reachable via the status icon once replaced, so it's
			// safe to release it now.
			lastProcessDialog.dispose();
		}

		ProcessDialog processDialog = new ProcessDialog(frame);
		processDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		processDialog.setLocationRelativeTo(frame);
		processDialog.setOnStateChanged(this::updateRunStatusIndicator);
		lastProcessDialog = processDialog;
		processing = true;
		mnSettings.setEnabled(false);
		processDialog.doProcess(processCondition);
		// setVisible(true) on a modal dialog blocks until it's hidden, which - now that closing only hides
		// it (see ProcessDialog's HIDE_ON_CLOSE) - can happen well before the background run actually
		// completes. Clearing `processing`/re-enabling the settings menu is handled by
		// updateRunStatusIndicator() reacting to the dialog's real completion instead of happening here.
		processDialog.setVisible(true);
	}

	private void showLastProcessDialog() {
		if (lastProcessDialog == null) {
			return;
		}
		lastProcessDialog.setVisible(true);
		lastProcessDialog.toFront();
	}

	private void updateRunStatusIndicator() {
		if (lastProcessDialog == null) {
			lblRunStatus.setIcon(null);
			lblRunStatus.setToolTipText(null);
			lblRunStatus.setCursor(Cursor.getDefaultCursor());
			return;
		}
		lblRunStatus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		Color runStatusIconColor = color("Label.foreground", Color.BLACK);
		if (lastProcessDialog.isProcessing()) {
			lblRunStatus.setIcon(IconSupport.loadIcon("net/mozq/picto/resources/icons/icon-run-processing.png", RUN_STATUS_ICON_SIZE, runStatusIconColor));
			lblRunStatus.setToolTipText(Messages.getString("MainFrame.runStatus.processing"));
		} else {
			lblRunStatus.setIcon(IconSupport.loadIcon("net/mozq/picto/resources/icons/icon-run-results.png", RUN_STATUS_ICON_SIZE, runStatusIconColor));
			lblRunStatus.setToolTipText(Messages.getString("MainFrame.runStatus.results"));
			processing = false;
			mnSettings.setEnabled(true);
		}
	}

	private ProcessConditionValues collectProcessConditionValues() {
		ProcessConditionValues values = new ProcessConditionValues();

		values.srcRootDirPath = Paths.get(txtSrcRootDirPath.getText()).normalize();
		values.filePattern = fieldText(txtFilePattern);
		values.filePatternRegex = getSelectedFilePatternSyntax().isRegex();
		values.containsHiddens = chkContainsHiddens.isEnabled() && chkContainsHiddens.isSelected();
		values.followLinks = false;
		values.depth = (chkContainsSubs.isEnabled() && chkContainsSubs.isSelected()) ? Integer.MAX_VALUE : 1;

		if (rdoOpeTypeMove.isSelected()) {
			values.operationType = OperationType.Move;
		} else if (rdoOpeTypeOverwrite.isSelected()) {
			values.operationType = OperationType.Overwrite;
		} else {
			values.operationType = OperationType.Copy;
		}

		values.destRootDirPath = Paths.get(txtDestRootDirPath.getText()).normalize();
		values.destSubPathPattern = fieldText(txtDestSubPathPattern);
		values.existingFileMethod = (ExistingFileMethod)cmbExistingFileMethod.getSelectedItem();
		values.checkDigest = chkCheckFileDigest.isEnabled() && chkCheckFileDigest.isSelected();

		values.changeFileCreationDate = chkChangeFileCreationDate.isEnabled() && chkChangeFileCreationDate.isSelected();
		values.changeFileModifiedDate = chkChangeFileModifiedDate.isEnabled() && chkChangeFileModifiedDate.isSelected();
		values.changeFileAccessDate = chkChangeFileAccessDate.isEnabled() && chkChangeFileAccessDate.isSelected();
		values.changeExifDate = chkChangeExifDate.isEnabled() && chkChangeExifDate.isSelected();
		values.baseDateType = (DateType)cmbBaseDateType.getSelectedItem();
		values.customBaseDate = DateTimeText.parseDate(txtCustomBaseDate.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.baseDateModType = (DateModType)cmbDateModType.getSelectedItem();
		values.baseDateModYears = parseInteger(txtDateModYears.getText());
		values.baseDateModMonths = parseInteger(txtDateModMonths.getText());
		values.baseDateModDays = parseInteger(txtDateModDays.getText());
		values.baseDateModHours = parseInteger(txtDateModHours.getText());
		values.baseDateModMinutes = parseInteger(txtDateModMinutes.getText());
		values.baseDateModSeconds = parseInteger(txtDateModSeconds.getText());

		values.removeExifTagsGps = chkRemoveExifTagsGps.isEnabled() && chkRemoveExifTagsGps.isSelected();
		values.removeExifTagsAll = chkRemoveExifTagsAll.isEnabled() && chkRemoveExifTagsAll.isSelected();

		FileSizeUnit fileSizeUnit = (FileSizeUnit)cmbFileSizeUnit.getSelectedItem();
		values.sizeRangeFrom = convertSize(parseLong(txtFileSizeRangeFrom.getText()), fileSizeUnit);
		values.sizeRangeTo = convertSize(parseLong(txtFileSizeRangeTo.getText()), fileSizeUnit);

		values.creationTimeRangeFrom = DateTimeText.parseDate(txtCreationTimeRangeFrom.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.creationTimeRangeTo = DateTimeText.parseDate(txtCreationTimeRangeTo.getText(), timeZone, Year.now().getValue(), 12, 31, 23, 59, 59, 999);
		values.modifiedTimeRangeFrom = DateTimeText.parseDate(txtModifiedTimeRangeFrom.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.modifiedTimeRangeTo = DateTimeText.parseDate(txtModifiedTimeRangeTo.getText(), timeZone, Year.now().getValue(), 12, 31, 23, 59, 59, 999);

		return values;
	}

	private ProcessCondition createProcessCondition(boolean dryRun) {

		if (!showValidationResult(ProcessConditionValidator.validateSourceFolder(
				fieldText(txtSrcRootDirPath),
				Messages.getString("MainFrame.srcRootDirPath")))) {
			return null;
		}
		if (!rdoOpeTypeOverwrite.isSelected()
				&& !showValidationResult(ProcessConditionValidator.validateDestinationFolder(
						fieldText(txtDestRootDirPath),
						Messages.getString("MainFrame.destRootDirPath")))) {
			return null;
		}

		ProcessConditionValues values = collectProcessConditionValues();

		if (!showValidationResult(ProcessConditionValidator.validate(values))) {
			return null;
		}

		if (!confirmDestructiveOperation(values, dryRun)) {
			return null;
		}

		if (values.checkDigest && (values.changeExifDate || values.removeExifTagsGps || values.removeExifTagsAll)) {
			int ret = JOptionPane.showConfirmDialog(
					frame,
					Messages.getString("message.confirm.change.file.with.checkFileDigest"),
					null,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE
					);
			if (ret == JOptionPane.NO_OPTION) {
				return null;
			}

			values.checkDigest = false;
		}

		PictoPathFilter pathFilter = buildPathFilter(values);


		String destSubPathPattern = values.destSubPathPattern.isBlank()
				? EMPTY_DEST_SUB_PATH_PATTERN
				: values.destSubPathPattern;
		NanoTemplate destSubPathTemplate = new NanoTemplate(destSubPathPattern).timeZone(timeZone);

		ProcessCondition processCondition = new ProcessCondition();
		processCondition.setTimeZone(timeZone);
		processCondition.setSrcRootPath(values.srcRootDirPath);
		processCondition.setDestRootPath(values.destRootDirPath);
		processCondition.setDepth(values.depth);
		processCondition.setPathFilter(pathFilter);
		processCondition.setFollowLinks(values.followLinks);
		processCondition.setDestSubPathTemplate(destSubPathTemplate);
		processCondition.setOperationType(values.operationType);
		processCondition.setExistingFileMethod(values.existingFileMethod);
		processCondition.setCheckDigest(values.checkDigest);
		processCondition.setChangeFileCreationDate(values.changeFileCreationDate);
		processCondition.setChangeFileModifiedDate(values.changeFileModifiedDate);
		processCondition.setChangeFileAccessDate(values.changeFileAccessDate);
		processCondition.setChangeExifDate(values.changeExifDate);
		processCondition.setBaseDateType(values.baseDateType);
		processCondition.setCustomBaseDate(values.customBaseDate);
		processCondition.setBaseDateModType(values.baseDateModType);
		processCondition.setBaseDateModYears(values.baseDateModYears);
		processCondition.setBaseDateModMonths(values.baseDateModMonths);
		processCondition.setBaseDateModDays(values.baseDateModDays);
		processCondition.setBaseDateModHours(values.baseDateModHours);
		processCondition.setBaseDateModMinutes(values.baseDateModMinutes);
		processCondition.setBaseDateModSeconds(values.baseDateModSeconds);
		processCondition.setRemoveExifTagsGps(values.removeExifTagsGps);
		processCondition.setRemoveExifTagsAll(values.removeExifTagsAll);
		processCondition.setDryRun(dryRun);

		return processCondition;
	}

	private static PictoPathFilter buildPathFilter(ProcessConditionValues values) {
		PictoPathFilter pathFilter = new PictoPathFilter();
		pathFilter.setPathPattern(values.filePattern, values.srcRootDirPath, values.filePatternRegex);
		pathFilter.setContainsHiddens(values.containsHiddens);
		pathFilter.setSizeRange(values.sizeRangeFrom, values.sizeRangeTo);
		pathFilter.setCreationTimeRange(values.creationTimeRangeFrom, values.creationTimeRangeTo);
		pathFilter.setModifiedTimeRange(values.modifiedTimeRangeFrom, values.modifiedTimeRangeTo);
//		pathFilter.setAccessTimeRange(from, to);
		return pathFilter;
	}

	private void matchCountLabelClicked() {
		if (sourceFileScanner == null) {
			matchCountEnabled = true;
			updateMatchCountTarget();
			return;
		}

		SourceFileScanner.MatchCountStatus status = lastMatchCountStatus;
		if (status == null) {
			return;
		}
		switch (status.state()) {
		case SCANNING -> sourceFileScanner.stop();
		case PAUSED -> sourceFileScanner.resume();
		case EXACT -> { }
		}
	}

	private void matchCountStopButtonClicked() {
		if (sourceFileScanner != null) {
			sourceFileScanner.stop();
		}
	}

	private void updateMatchCountTarget() {
		if (!matchCountEnabled) {
			return;
		}
		ProcessConditionValues values = collectProcessConditionValues();

		if (!Files.isDirectory(values.srcRootDirPath)) {
			if (sourceFileScanner != null) {
				sourceFileScanner.cancel();
				sourceFileScanner = null;
			}
			matchCountEnabled = false;
			lastMatchCountStatus = null;
			sourceOptionsPanel.matchCountLabel.setText(Messages.getString("MainFrame.matchCount.prompt"));
			sourceOptionsPanel.setMatchCountScanning(false);
			return;
		}

		PictoPathFilter pathFilter;
		boolean includeSubfolders = values.depth != 1;
		try {
			pathFilter = buildPathFilter(values);
		} catch (Exception e) {
			sourceOptionsPanel.matchCountLabel.setText("");
			sourceOptionsPanel.setMatchCountScanning(false);
			return;
		}

		if (sourceFileScanner != null && !sourceFileScanner.srcRootPath().equals(values.srcRootDirPath)) {
			// The source folder changed while a count was enabled for the previous one; that opt-in doesn't
			// carry over to a different folder (it could be much larger), so require an explicit re-click.
			sourceFileScanner.cancel();
			sourceFileScanner = null;
			matchCountEnabled = false;
			lastMatchCountStatus = null;
			sourceOptionsPanel.matchCountLabel.setText(Messages.getString("MainFrame.matchCount.prompt"));
			sourceOptionsPanel.setMatchCountScanning(false);
			return;
		}

		if (sourceFileScanner == null) {
			lastMatchCountStatus = null;
			// A status callback already in flight when this scanner is later cancelled and replaced
			// (e.g. the source folder changes again) would otherwise still reach renderMatchCount() and
			// overwrite the reset UI with stale SCANNING/PAUSED/EXACT text. The holder lets the callback
			// check, at delivery time, whether it's still the current scanner before touching the UI.
			SourceFileScanner[] holder = new SourceFileScanner[1];
			try {
				sourceFileScanner = new SourceFileScanner(
						values.srcRootDirPath, pathFilter, includeSubfolders,
						status -> SwingUtilities.invokeLater(() -> {
							if (sourceFileScanner == holder[0]) {
								renderMatchCount(status);
							}
						}));
			} catch (IOException e) {
				sourceFileScanner = null;
				sourceOptionsPanel.matchCountLabel.setText("");
				sourceOptionsPanel.setMatchCountScanning(false);
				return;
			}
			holder[0] = sourceFileScanner;
			sourceFileScanner.start();
		} else {
			sourceFileScanner.updateTarget(pathFilter, includeSubfolders);
		}
	}

	private void renderMatchCount(SourceFileScanner.MatchCountStatus status) {
		lastMatchCountStatus = status;
		String text = switch (status.state()) {
		case SCANNING -> Messages.getString("MainFrame.matchCount.scanning", status.count());
		case PAUSED -> Messages.getString("MainFrame.matchCount.paused", status.count());
		case EXACT -> Messages.getString("MainFrame.matchCount", status.count());
		};
		sourceOptionsPanel.matchCountLabel.setText(text);
		sourceOptionsPanel.setMatchCountScanning(status.state() == SourceFileScanner.MatchCountStatus.State.SCANNING);
		if (status.state() == SourceFileScanner.MatchCountStatus.State.PAUSED) {
			sourceOptionsPanel.matchCountLabel.setToolTipText(Messages.getString("MainFrame.matchCount.resume"));
		}
	}

	private boolean confirmDestructiveOperation(ProcessConditionValues values, boolean dryRun) {
		if (dryRun || values.operationType == OperationType.Copy) {
			return true;
		}
		String messageKey = switch (values.operationType) {
		case Move -> "message.confirm.destructive.move";
		case Overwrite -> "message.confirm.destructive.overwrite";
		case Copy -> throw new IllegalStateException(values.operationType.toString());
		};
		int ret = JOptionPane.showConfirmDialog(
				frame,
				Messages.getString(messageKey),
				null,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
				);
		return ret == JOptionPane.YES_OPTION;
	}

	private boolean showValidationResult(ProcessConditionValidator.Result result) {
		if (result == null) {
			return true;
		}
		showValidationWarning(result.message());
		focusValidationField(result.field());
		return false;
	}

	private void showValidationWarning(String message) {
		JOptionPane.showMessageDialog(
				frame,
				message,
				null,
				JOptionPane.WARNING_MESSAGE
				);
	}

	private void focusValidationField(ProcessConditionValidator.Field field) {
		JTextField textField = switch (field) {
		case SOURCE_FOLDER -> txtSrcRootDirPath;
		case DESTINATION_FOLDER -> txtDestRootDirPath;
		case FILE_PATTERN -> txtFilePattern;
		case DESTINATION_SUBFOLDER -> txtDestSubPathPattern;
		case NONE -> null;
		};
		if (textField != null) {
			textField.requestFocusInWindow();
			textField.selectAll();
		}
	}

	private static Long convertSize(Long size, FileSizeUnit unit) {
		if (size == null) {
			return null;
		}
		return size * unit.getUnitBytes();
	}

	private static Integer parseInteger(String numberText) {
		if (numberText == null || numberText.isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(numberText);
		} catch (NumberFormatException _) {
			return null;
		}
	}

	private static Long parseLong(String numberText) {
		if (numberText == null || numberText.isEmpty()) {
			return null;
		}
		try {
			return Long.valueOf(numberText);
		} catch (NumberFormatException _) {
			return null;
		}
	}

	private FilePatternSyntax getSelectedFilePatternSyntax() {
		Object selectedItem = cmbFilePatternSyntax.getSelectedItem();
		return selectedItem instanceof FilePatternSyntax ? (FilePatternSyntax)selectedItem : FilePatternSyntax.GLOB;
	}

}
