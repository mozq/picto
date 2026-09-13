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
import java.awt.GridLayout;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

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
import net.mozq.picto.LegacyPictoFileImport;
import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.core.ProcessCondition;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FilePatternSyntax;
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

	private static final String DEFAULT_DEST_SUB_FILE_PATH_PATTERN = "${FileName}";
	private static final String EMPTY_DEST_SUB_FILE_PATH_PATTERN = "${SubFilePath}";
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
	static final String PRESET_FILE_NAME_EXT = "conf";
	private static final int PRESET_SHORTCUT_COUNT = 9;
	private static final String PICTO_FILE_NAME_EXT = "picto";
	private static final int MATCH_COUNT_DEBOUNCE_MS = 400;

	private TimeZone timeZone = TimeZone.getDefault();

	private final JFrame frame;
	private boolean windowLayoutReady;

	private JPanel contentPane;

	private JPanel pnlSrcConditions;
	private JPanel pnlSrcFolder;
	private JTextField txtSrcFolder;
	private JButton btnSrcFolderSelect;

	private JPanel pnlDestConditions;
	private JPanel pnlOperation;
	private JPanel pnlOperationType;
	private ButtonGroup btngrpOperationType = new ButtonGroup();
	private JRadioButton rdoOperationTypeCopy;
	private JRadioButton rdoOperationTypeMove;
	private JRadioButton rdoOperationTypeOverwrite;
	private JPanel pnlControls;
	private JTextField txtDestFolder;
	private JButton btnDestFolderSelect;
	private JPanel pnlDestFolder;

	private ChangesPanel changes;

	private JButton btnStart;
	private JButton btnStartMenu;
	private JToggleButton btnSrcOptions;
	private JToggleButton btnDestOptions;
	private JToggleButton btnChanges;
	private JLabel lblDestConditionsTitle;
	private SourceOptionsPanel srcOpt;
	private DestinationOptionsPanel destOpt;
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

		mnSettings = new JMenu(Messages.getString("MainFrame.menu.settings"));
		mnSettings.setMnemonic(KeyEvent.VK_S);

		mnLanguage = new JMenu(Messages.getString("MainFrame.menu.settings.language"));
		mnLanguage.setMnemonic(KeyEvent.VK_L);
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

		int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

		JMenuItem mntmImportData = new JMenuItem(Messages.getString("MainFrame.menu.settings.importData"));
		mntmImportData.setMnemonic(KeyEvent.VK_I);
		mntmImportData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
		mntmImportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				promptImportData();
			}
		});

		JMenuItem mntmExportData = new JMenuItem(Messages.getString("MainFrame.menu.settings.exportData"));
		mntmExportData.setMnemonic(KeyEvent.VK_E);
		mntmExportData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
		mntmExportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				promptExportData();
			}
		});

		mnSettings.add(mnLanguage);
		mnSettings.add(mnAppearance);
		mnSettings.addSeparator();
		mnSettings.add(mntmImportData);
		mnSettings.add(mntmExportData);

		mnHelp = new JMenu(Messages.getString("MainFrame.menu.help"));
		mnHelp.setMnemonic(KeyEvent.VK_H);

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

		menuBar.add(mnPresets);
		menuBar.add(mnSettings);
		menuBar.add(mnHelp);

		setJMenuBar(menuBar);
	}

	private void buildContentPane() {
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING));
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{427, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		setContentPane(contentPane);
	}

	private void buildSourcePanel() {
		pnlSrcConditions = new JPanel();
		GridBagLayout gbl_pnlSrcConditions = new GridBagLayout();
		gbl_pnlSrcConditions.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSrcConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlSrcConditions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSrcConditions.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSrcConditions.setLayout(gbl_pnlSrcConditions);

		JLabel lblSrcConditionsTitle = newMainLabel(Messages.getString("MainFrame.src.conditionsTitle"));
		lblSrcConditionsTitle.setDisplayedMnemonic(KeyEvent.VK_F);

		pnlSrcFolder = new JPanel();
		pnlSrcFolder.setBorder(null);
		pnlSrcFolder.setLayout(new BorderLayout(INLINE_HGAP, 0));

		btnSrcFolderSelect = new JButton(Messages.getString("MainFrame.src.folderSelect"));
		configureFolderSelectButton(btnSrcFolderSelect);
		btnSrcOptions = newOptionsToggleButton(Messages.getString("MainFrame.src.options"));
		JPanel pnlSrcFolderActions = new JPanel(new BorderLayout(0, 0));
		pnlSrcFolderActions.add(btnSrcOptions, BorderLayout.EAST);
		pnlSrcFolder.add(pnlSrcFolderActions, BorderLayout.EAST);

		txtSrcFolder = new JTextField();
		txtSrcFolder.putClientProperty("JTextField.trailingComponent", btnSrcFolderSelect);
		txtSrcFolder.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, UIManager.getIcon("FileView.directoryIcon"));
		lblSrcConditionsTitle.setLabelFor(txtSrcFolder);
		txtSrcFolder.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.src.folder"));
		txtSrcFolder.setToolTipText(Messages.getString("MainFrame.src.folder"));
		txtSrcFolder.setColumns(10);
		InputSupport.installLabelFocusAction(lblSrcConditionsTitle, txtSrcFolder, LabelFocusBehavior.CARET_END);
		InputSupport.installFolderDropTarget(txtSrcFolder);
		new FolderHistoryPopup(txtSrcFolder, InputHistory.SRC_FOLDER_KEY);
		installFolderChooserButton(btnSrcFolderSelect, txtSrcFolder);
		pnlSrcFolder.add(txtSrcFolder, BorderLayout.CENTER);

		pnlSrcConditions.add(lblSrcConditionsTitle, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 8).build());
		pnlSrcConditions.add(pnlSrcFolder, GridBagSupport.at(1, 0).insets(0, 0, 5, 0).fill(GridBagConstraints.BOTH).build());

		srcOpt = new SourceOptionsPanel(INLINE_HGAP, INLINE_VGAP);
		stylizeOptionsBody(srcOpt, OPTIONS_BODY_TOP_PADDING_WITH_MATCH_COUNT);
		srcOpt.lblMatchCount.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				matchCountLabelClicked();
			}
		});
		srcOpt.btnMatchCountStop.addActionListener(_ -> matchCountStopButtonClicked());
		btnSrcOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnSrcOptions, srcOpt, Messages.getString("MainFrame.src.options"), btnSrcOptions.isSelected());
			}
		});
		setOptionsExpanded(btnSrcOptions, srcOpt, Messages.getString("MainFrame.src.options"), false);
		pnlSrcConditions.add(srcOpt, GridBagSupport.at(1, 1).fill(GridBagConstraints.BOTH).gridwidth(2).insets(0, 0, 5, 0).build());

		txtSrcOptionsSummary = newOptionsSummaryText(btnSrcOptions, srcOpt, Messages.getString("MainFrame.src.options"));
		pnlSrcConditions.add(txtSrcOptionsSummary, GridBagSupport.at(1, 1).fill(GridBagConstraints.HORIZONTAL).gridwidth(2).insets(0, 0, 5, 0).build());

		getContentPane().add(pnlSrcConditions, GridBagSupport.at(0, 0).fill(GridBagConstraints.HORIZONTAL).anchor(GridBagConstraints.NORTH).insets(0, 0, SECTION_GAP, 0).build());
	}

	private void buildOperationPanel() {
		pnlOperation = new JPanel();
		GridBagLayout gbl_pnlOperation = new GridBagLayout();
		gbl_pnlOperation.columnWidths = new int[]{0, 0, 0};
		gbl_pnlOperation.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlOperation.rowHeights = new int[]{0, 0};
		gbl_pnlOperation.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlOperation.setLayout(gbl_pnlOperation);

		pnlOperationType = new JPanel();
		pnlOperationType.setBorder(null);
		pnlOperationType.setLayout(new FlowLayout(FlowLayout.LEFT, INLINE_HGAP, 0));

		rdoOperationTypeCopy = new JRadioButton(Messages.getString("MainFrame.operationType.copy"));
		rdoOperationTypeCopy.setMnemonic(KeyEvent.VK_C);
		rdoOperationTypeCopy.setActionCommand(OperationType.Copy.name());
		btngrpOperationType.add(rdoOperationTypeCopy);

		rdoOperationTypeMove = new JRadioButton(Messages.getString("MainFrame.operationType.move"));
		rdoOperationTypeMove.setMnemonic(KeyEvent.VK_M);
		rdoOperationTypeMove.setActionCommand(OperationType.Move.name());
		btngrpOperationType.add(rdoOperationTypeMove);

		rdoOperationTypeOverwrite = new JRadioButton(Messages.getString("MainFrame.operationType.overwrite"));
		rdoOperationTypeOverwrite.setMnemonic(KeyEvent.VK_O);
		rdoOperationTypeOverwrite.setActionCommand(OperationType.Overwrite.name());
		btngrpOperationType.add(rdoOperationTypeOverwrite);

		pnlOperationType.add(rdoOperationTypeCopy);
		pnlOperationType.add(rdoOperationTypeMove);
		pnlOperationType.add(rdoOperationTypeOverwrite);

		pnlOperation.add(pnlOperationType, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).fill(GridBagConstraints.HORIZONTAL).build());

		getContentPane().add(pnlOperation, GridBagSupport.at(0, 1).insets(0, MAIN_LABEL_WIDTH + 8, OPERATION_BOTTOM_GAP, 0).anchor(GridBagConstraints.NORTH).fill(GridBagConstraints.HORIZONTAL).build());
	}

	private void buildDestinationPanel() {
		pnlDestConditions = new JPanel();
		GridBagLayout gbl_pnlDestConditions = new GridBagLayout();
		gbl_pnlDestConditions.columnWidths = new int[]{0, 0, 0};
		gbl_pnlDestConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlDestConditions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlDestConditions.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlDestConditions.setLayout(gbl_pnlDestConditions);

		lblDestConditionsTitle = newMainLabel(Messages.getString("MainFrame.dest.conditionsTitle"));
		lblDestConditionsTitle.setDisplayedMnemonic(KeyEvent.VK_T);

		pnlDestFolder = new JPanel();
		pnlDestFolder.setBorder(null);
		pnlDestFolder.setLayout(new BorderLayout(INLINE_HGAP, 0));

		btnDestFolderSelect = new JButton(Messages.getString("MainFrame.dest.folderSelect"));
		configureFolderSelectButton(btnDestFolderSelect);
		btnDestOptions = newOptionsToggleButton(Messages.getString("MainFrame.dest.options"));
		JPanel pnlDestFolderActions = new JPanel(new BorderLayout(0, 0));
		pnlDestFolderActions.add(btnDestOptions, BorderLayout.EAST);
		pnlDestFolder.add(pnlDestFolderActions, BorderLayout.EAST);

		txtDestFolder = new JTextField();
		txtDestFolder.putClientProperty("JTextField.trailingComponent", btnDestFolderSelect);
		txtDestFolder.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, UIManager.getIcon("FileView.directoryIcon"));
		lblDestConditionsTitle.setLabelFor(txtDestFolder);
		txtDestFolder.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.dest.folder"));
		txtDestFolder.setToolTipText(Messages.getString("MainFrame.dest.folder"));
		txtDestFolder.setColumns(10);
		InputSupport.installLabelFocusAction(lblDestConditionsTitle, txtDestFolder, LabelFocusBehavior.CARET_END);
		InputSupport.installFolderDropTarget(txtDestFolder);
		new FolderHistoryPopup(txtDestFolder, InputHistory.DEST_FOLDER_KEY);
		installFolderChooserButton(btnDestFolderSelect, txtDestFolder);
		pnlDestFolder.add(txtDestFolder, BorderLayout.CENTER);

		pnlDestConditions.add(lblDestConditionsTitle, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 8).build());
		pnlDestConditions.add(pnlDestFolder, GridBagSupport.at(1, 0).insets(0, 0, 5, 0).fill(GridBagConstraints.BOTH).build());

		destOpt = new DestinationOptionsPanel();
		stylizeOptionsBody(destOpt);
		btnDestOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnDestOptions, destOpt, Messages.getString("MainFrame.dest.options"), btnDestOptions.isSelected());
			}
		});
		setOptionsExpanded(btnDestOptions, destOpt, Messages.getString("MainFrame.dest.options"), false);
		pnlDestConditions.add(destOpt, GridBagSupport.at(1, 1).fill(GridBagConstraints.BOTH).gridwidth(2).insets(0, 0, 5, 0).build());

		txtDestOptionsSummary = newOptionsSummaryText(btnDestOptions, destOpt, Messages.getString("MainFrame.dest.options"));
		pnlDestConditions.add(txtDestOptionsSummary, GridBagSupport.at(1, 1).fill(GridBagConstraints.HORIZONTAL).gridwidth(2).insets(0, 0, 5, 0).build());

		getContentPane().add(pnlDestConditions, GridBagSupport.at(0, 2).insets(0, 0, SECTION_GAP, 0).anchor(GridBagConstraints.NORTH).fill(GridBagConstraints.HORIZONTAL).build());
	}

	private void buildChangesPanel() {
		btnChanges = newOptionsToggleButton(Messages.getString("MainFrame.changes.title"));
		btnChanges.setMnemonic(KeyEvent.VK_G);
		btnChanges.setFont(btnChanges.getFont().deriveFont(Font.BOLD, btnChanges.getFont().getSize2D() + 1.0f));

		changes = new ChangesPanel(
				SECTION_PADDING,
				INLINE_HGAP,
				INLINE_VGAP,
				this::changeEnableFileDateModConditions,
				this::fitWindowToContent);

		txtChangesSummary = newOptionsSummaryText(btnChanges, changes, Messages.getString("MainFrame.changes.title"));

		setOptionsExpanded(btnChanges, changes, Messages.getString("MainFrame.changes.title"), false);
		btnChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnChanges, changes, Messages.getString("MainFrame.changes.title"), btnChanges.isSelected());
			}
		});

		contentPane.add(btnChanges, GridBagSupport.at(0, 3).anchor(GridBagConstraints.WEST).insets(0, 0, SECTION_HEADER_GAP, 0).build());
		contentPane.add(changes, GridBagSupport.at(0, 5).insets(0, 0, SECTION_GAP, 0).fill(GridBagConstraints.BOTH).build());
		contentPane.add(txtChangesSummary, GridBagSupport.at(0, 4).fill(GridBagConstraints.HORIZONTAL).insets(0, MAIN_LABEL_WIDTH + 8, SECTION_GAP, 0).build());
	}

	private void buildControlsPanel() {
		pnlControls = new JPanel();
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
		installRunSummaryHover(btnStart);

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
		installRunSummaryHover(btnStartMenu);

		JPanel pnlRunButton = new JPanel(new FlowLayout(FlowLayout.CENTER, INLINE_HGAP, 0));
		pnlRunButton.setBorder(null);

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

		lblRunSummary = newRunSummaryLabel();

		pnlRunButton.add(newRunSplitButtonPanel(btnStart, btnStartMenu));
		pnlRunButton.add(lblRunStatus);

		pnlControls.add(pnlRunButton, BorderLayout.CENTER);
		pnlControls.add(lblRunSummary, BorderLayout.SOUTH);

		getContentPane().add(pnlControls, GridBagSupport.at(0, 6).anchor(GridBagConstraints.SOUTH).fill(GridBagConstraints.HORIZONTAL).build());
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
		rdoOperationTypeCopy.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableDestConditions();
			}
		});
		rdoOperationTypeMove.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableDestConditions();
			}
		});
		rdoOperationTypeOverwrite.addChangeListener(new ChangeListener() {
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

	private static <E extends Enum<E>> E selectedEnumValue(ButtonGroup group, Class<E> type, E defaultValue) {
		for (java.util.Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements();) {
			AbstractButton button = e.nextElement();
			if (button.isSelected()) {
				return Enum.valueOf(type, button.getActionCommand());
			}
		}
		return defaultValue;
	}

	private List<SettingBinding> settingBindings() {
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

	private void applySettingsFrom(AppSettings conf) {
		for (SettingBinding binding : settingBindings()) {
			binding.applyFrom().accept(conf);
		}
	}

	protected void storeSettings() throws IOException {
		AppSettings conf = App.settings();

		captureSettingsInto(conf);

		conf.store();
		App.deleteMigratedLegacySettingsIfNeeded();
	}

	private void captureSettingsInto(AppSettings conf) {
		for (SettingBinding binding : settingBindings()) {
			binding.captureInto().accept(conf);
		}
	}

	/**
	 * Writes each bound setting's schema default into {@code conf}, without reading or touching any Swing
	 * component. Used as the base for a newly imported preset, so a key the import doesn't mention still
	 * ends up with the same value a never-customized preset would have, rather than being left undefined.
	 */
	void applyDefaultSettings(AppSettings conf) {
		for (SettingBinding binding : settingBindings()) {
			binding.applyDefault().accept(conf);
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
		if (alreadyExists && !confirmYesNo("message.confirm.preset.overwrite", name)) {
			return;
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
		if (!confirmYesNo("message.confirm.preset.load", preset.name())) {
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
		applySettingsSource(presetSettings);
	}

	private void confirmAndResetToDefaultSettings() {
		if (!confirmYesNo("message.confirm.settings.default")) {
			return;
		}

		applyDefaultSettings(App.settings());
		loadSettings();
	}

	/** Copies every key from {@code source} into the live settings, then refreshes the UI from it. */
	private void applySettingsSource(AppSettings source) {
		AppSettings conf = App.settings();
		for (String key : source.keySet()) {
			conf.set(key, source.get(key));
		}

		loadSettings();
	}

	/** Shows a Yes/No confirmation dialog for {@code messageKey} and reports whether the user chose Yes. */
	private boolean confirmYesNo(String messageKey, Object... args) {
		int result = JOptionPane.showConfirmDialog(
				frame,
				Messages.getString(messageKey, args),
				null,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
				);
		return result == JOptionPane.YES_OPTION;
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

	private enum PresetConflictResolution {
		RENAME, REPLACE, SKIP
	}

	private void promptExportData() {
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
			JOptionPane.showMessageDialog(frame, Messages.getString("message.warn.dataSelection.empty"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("data.ext.description"), PICTO_FILE_NAME_EXT));

		int selected = filechooser.showSaveDialog(frame);
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
					frame,
					Messages.getString("message.info.export.data"),
					null,
					JOptionPane.INFORMATION_MESSAGE
					);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					frame,
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

	private void promptImportData() {
		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("data.ext.description"), PICTO_FILE_NAME_EXT));

		int selected = filechooser.showOpenDialog(frame);
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
					frame,
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
					frame,
					Messages.getString("message.error.import.data", e1.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);
			App.handleError(e1.getMessage(), e1);
			return;
		}
		if (!available.any()) {
			JOptionPane.showMessageDialog(frame, Messages.getString("message.warn.importData.none"), null, JOptionPane.WARNING_MESSAGE);
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
			JOptionPane.showMessageDialog(frame, Messages.getString("message.warn.dataSelection.empty"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			if (!importData(zipPath, selection)) {
				return;
			}
			if (selection.settings()) {
				loadSettings();
			}

			JOptionPane.showMessageDialog(
					frame,
					Messages.getString("message.info.import.data"),
					null,
					JOptionPane.INFORMATION_MESSAGE
					);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					frame,
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
			loadSettings();

			JOptionPane.showMessageDialog(
					frame,
					Messages.getString("message.info.import.data"),
					null,
					JOptionPane.INFORMATION_MESSAGE
					);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(
					frame,
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
		return DataArchiveSupport.buildPresetSettings(dir, fileName, this::applyDefaultSettings, values, name);
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

		JPanel panel = new JPanel(new GridLayout(0, 1, 0, INLINE_VGAP));
		panel.add(chkSettings);
		panel.add(chkPresets);
		panel.add(chkHistory);

		int result = JOptionPane.showConfirmDialog(frame, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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

		JPanel panel = new JPanel(new BorderLayout(0, INLINE_VGAP));
		panel.add(new JLabel(Messages.getString("MainFrame.presetConflict.message", names.toString())), BorderLayout.NORTH);
		JPanel radios = new JPanel(new GridLayout(0, 1));
		radios.add(rdoRename);
		radios.add(rdoReplace);
		radios.add(rdoSkip);
		panel.add(radios, BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(
				frame,
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

	private void addSettingsMenuItem(JMenu menu, ButtonGroup group, String label, String key, String value, int mnemonic) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
		if (mnemonic != 0) {
			item.setMnemonic(mnemonic);
		}
		item.setActionCommand(value);
		item.setSelected(value.equals(App.preferences().getString(key, AppMain.PREF_SYSTEM)));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (processing) {
					selectCurrentSettingsMenuItem(group, key);
					showSettingsProcessingMessage();
					return;
				}
				if (!value.equals(App.preferences().getString(key, AppMain.PREF_SYSTEM))) {
					App.preferences().set(key, value);
					try {
						App.preferences().store();
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
		String currentValue = App.preferences().getString(key, AppMain.PREF_SYSTEM);
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
				changes.getSelectedIndex());
	}

	private void restoreFrameState(MainFrameState state) {
		if (state == null) {
			return;
		}
		if (state.bounds != null) {
			setBounds(state.bounds);
		}
		setOptionsExpanded(btnSrcOptions, srcOpt, Messages.getString("MainFrame.src.options"), state.srcOptionsExpanded);
		setOptionsExpanded(btnDestOptions, destOpt, Messages.getString("MainFrame.dest.options"), state.destOptionsExpanded);
		setOptionsExpanded(btnChanges, changes, Messages.getString("MainFrame.changes.title"), state.changesExpanded);
		if (state.changesTabIndex >= 0 && state.changesTabIndex < changes.getTabCount()) {
			changes.setSelectedIndex(state.changesTabIndex);
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
				srcOpt.txtFileNamePattern,
				srcOpt.txtFileSizeFrom,
				srcOpt.txtFileSizeTo,
				srcOpt.txtCreatedFrom,
				srcOpt.txtCreatedTo,
				srcOpt.txtModifiedFrom,
				srcOpt.txtModifiedTo,
				txtSrcFolder,
				txtDestFolder,
				destOpt.txtSubFilePathPattern,
				changes.filedate.txtCustomBaseDate,
				changes.filedate.txtAdjustmentYears,
				changes.filedate.txtAdjustmentMonths,
				changes.filedate.txtAdjustmentDays,
				changes.filedate.txtAdjustmentHours,
				changes.filedate.txtAdjustmentMinutes,
				changes.filedate.txtAdjustmentSeconds);
		addChangeListener(changeListener,
				srcOpt.chkIncludeSubfolders,
				srcOpt.chkIncludeHiddenFiles,
				destOpt.chkCheckFileDigest,
				changes.filedate.chkCreationDate,
				changes.filedate.chkModifiedDate,
				changes.filedate.chkAccessDate,
				changes.filedate.chkExifDate,
				changes.exif.chkRemoveGps,
				changes.exif.chkRemoveAll,
				rdoOperationTypeCopy,
				rdoOperationTypeMove,
				rdoOperationTypeOverwrite);
		srcOpt.cmbFileNamePatternSyntax.addItemListener(itemListener);
		srcOpt.cmbFileSizeUnit.addItemListener(itemListener);
		destOpt.cmbExistingFileMethod.addItemListener(itemListener);
		changes.filedate.cmbBaseDate.addItemListener(itemListener);
		changes.filedate.cmbAdjustmentType.addItemListener(itemListener);
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
		ProcessConditionValues values = collectProcessConditionValues();
		updateOptionsSummary(txtSrcOptionsSummary, srcOpt, sourceOptionsSummary(values));
		updateOptionsSummary(txtDestOptionsSummary, destOpt, destinationOptionsSummary(values));
		updateOptionsSummary(txtChangesSummary, changes, changesSummary(values));
		updateRunSummary(values);
	}

	private void updateRunSummary() {
		updateRunSummary(collectProcessConditionValues());
	}

	private void updateRunSummary(ProcessConditionValues values) {
		btnStart.setToolTipText(null);
		btnStartMenu.setToolTipText(null);
		if (showingRunSummary) {
			showRunSummary(values);
		} else {
			clearRunSummary();
		}
	}

	private void showRunSummary(ProcessConditionValues values) {
		lblRunSummary.setText(PathTextSupport.abbreviateMiddle(runSummary(values), lblRunSummary.getWidth() - 8, lblRunSummary));
	}

	private void clearRunSummary() {
		lblRunSummary.setText(" ");
	}

	private String runSummary(ProcessConditionValues values) {
		String summary = values.operationType + ": "
				+ Messages.getString("MainFrame.src.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(fieldText(txtSrcFolder), Messages.getString("MainFrame.src.folder"));
		if (values.operationType == OperationType.Overwrite) {
			return summary;
		}
		return summary + " -> "
				+ Messages.getString("MainFrame.dest.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(fieldText(txtDestFolder), Messages.getString("MainFrame.dest.folder"));
	}

	private static void updateOptionsSummary(JTextArea summary, JComponent optionsBody, String text) {
		summary.setText(text);
		summary.setVisible(!optionsBody.isVisible() && !text.isEmpty());
	}

	private String sourceOptionsSummary(ProcessConditionValues values) {
		List<String> items = new ArrayList<>();
		if (!values.srcFileNamePattern.isEmpty()) {
			String pattern = values.srcFileNamePattern;
			if (values.srcFileNamePatternSyntax == FilePatternSyntax.Regex) {
				pattern += " (" + FilePatternSyntax.Regex + ")";
			}
			items.add(summaryItem(Messages.getString("MainFrame.src.fileNamePattern"), pattern));
		}
		if (values.depth != 1) {
			items.add(checkedItem(Messages.getString("MainFrame.src.includeSubfolders")));
		}
		if (values.includeHiddenFiles) {
			items.add(checkedItem(Messages.getString("MainFrame.src.includeHiddenFiles")));
		}
		if (values.fileSizeFrom != null || values.fileSizeTo != null) {
			String range = rangeText(fieldText(srcOpt.txtFileSizeFrom), fieldText(srcOpt.txtFileSizeTo));
			items.add(summaryItem(Messages.getString("MainFrame.src.fileSize"), range + " " + srcOpt.cmbFileSizeUnit.getSelectedItem()));
		}
		if (values.createdFrom != null || values.createdTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.src.created"),
					rangeText(dateFieldText(srcOpt.txtCreatedFrom), dateFieldText(srcOpt.txtCreatedTo))));
		}
		if (values.modifiedFrom != null || values.modifiedTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.src.modified"),
					rangeText(dateFieldText(srcOpt.txtModifiedFrom), dateFieldText(srcOpt.txtModifiedTo))));
		}
		return joinOptionsSummary(items);
	}

	private String destinationOptionsSummary(ProcessConditionValues values) {
		List<String> items = new ArrayList<>();
		if (!values.destSubFilePathPattern.isBlank() && !DEFAULT_DEST_SUB_FILE_PATH_PATTERN.equals(values.destSubFilePathPattern)) {
			items.add(summaryItem(Messages.getString("MainFrame.dest.subFilePathPattern"), values.destSubFilePathPattern));
		}
		if (values.existingFileMethod != ExistingFileMethod.Confirm) {
			items.add(summaryItem(Messages.getString("MainFrame.dest.existingFileMethod"), String.valueOf(values.existingFileMethod)));
		}
		if (values.checkFileDigest) {
			items.add(checkedItem(Messages.getString("MainFrame.dest.validateFile")));
		}
		return joinOptionsSummary(items);
	}

	private String changesSummary(ProcessConditionValues values) {
		List<String> items = new ArrayList<>();
		boolean changesFileDate = false;
		if (values.changeFileCreationDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.creationDate")));
			changesFileDate = true;
		}
		if (values.changeFileModifiedDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.modifiedDate")));
			changesFileDate = true;
		}
		if (values.changeFileAccessDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.accessDate")));
			changesFileDate = true;
		}
		if (values.changeFileExifDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.exifDate")));
			changesFileDate = true;
		}
		if (changesFileDate) {
			items.add(summaryItem(Messages.getString("MainFrame.changes.filedate.baseDateType"), baseDateTypeSummary(values)));
		}
		if (changesFileDate && values.adjustmentType != DateModType.None) {
			items.add(summaryItem(Messages.getString("MainFrame.changes.filedate.adjustment"), adjustmentSummary(values)));
		}
		if (values.removeExifGps) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.exif.removeGps")));
		}
		if (values.removeExifAll) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.exif.removeAll")));
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
		String customDate = dateFieldText(changes.filedate.txtCustomBaseDate);
		if (values.baseDateType == DateType.CustomDate && values.customBaseDate != null && !customDate.isEmpty()) {
			value += " " + customDate;
		}
		return value;
	}

	private String adjustmentSummary(ProcessConditionValues values) {
		String value = String.valueOf(values.adjustmentType);
		List<String> amounts = new ArrayList<>();
		addAdjustmentAmount(amounts, values.adjustmentYears, "Y");
		addAdjustmentAmount(amounts, values.adjustmentMonths, "M");
		addAdjustmentAmount(amounts, values.adjustmentDays, "D");
		addAdjustmentAmount(amounts, values.adjustmentHours, "h");
		addAdjustmentAmount(amounts, values.adjustmentMinutes, "m");
		addAdjustmentAmount(amounts, values.adjustmentSeconds, "s");
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
		setEnableDestConditions(!rdoOperationTypeOverwrite.isSelected());
	}

	private void setEnableDestConditions(boolean enabled) {
		lblDestConditionsTitle.setEnabled(enabled);
		txtDestFolder.setEnabled(enabled);
		btnDestFolderSelect.setEnabled(enabled);
		btnDestOptions.setEnabled(enabled);
		if (txtDestOptionsSummary != null) {
			txtDestOptionsSummary.setEnabled(enabled);
		}
		destOpt.lblSubFilePathPattern.setEnabled(enabled);
		destOpt.txtSubFilePathPattern.setEnabled(enabled);
		destOpt.lblExistingFileMethod.setEnabled(enabled);
		destOpt.cmbExistingFileMethod.setEnabled(enabled);
		destOpt.lblValidateFile.setEnabled(enabled);
		destOpt.chkCheckFileDigest.setEnabled(enabled);
	}

	private void changeEnableFileDateModConditions() {
		if (changes.filedate.chkCreationDate.isSelected()
				|| changes.filedate.chkModifiedDate.isSelected()
				|| changes.filedate.chkAccessDate.isSelected()
				|| changes.filedate.chkExifDate.isSelected()) {
			setEnableFileDateModConditions(true);
		} else {
			setEnableFileDateModConditions(false);
		}
	}

	private void setEnableFileDateModConditions(boolean enabled) {
		changes.filedate.lblBaseDate.setEnabled(enabled);
		changes.filedate.cmbBaseDate.setEnabled(enabled);
		InputSupport.setTextFieldEnabled(changes.filedate.txtCustomBaseDate, enabled);
		changes.filedate.lblAdjustment.setEnabled(enabled);
		changes.filedate.cmbAdjustmentType.setEnabled(enabled);

		boolean adjustmentEnabled = enabled && changes.filedate.cmbAdjustmentType.getSelectedItem() != DateModType.None;
		InputSupport.setTextFieldEnabled(changes.filedate.txtAdjustmentYears, adjustmentEnabled);
		changes.filedate.lblYearMonthSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(changes.filedate.txtAdjustmentMonths, adjustmentEnabled);
		changes.filedate.lblMonthDaySeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(changes.filedate.txtAdjustmentDays, adjustmentEnabled);
		changes.filedate.lblDayHourSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(changes.filedate.txtAdjustmentHours, adjustmentEnabled);
		changes.filedate.lblHourMinuteSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(changes.filedate.txtAdjustmentMinutes, adjustmentEnabled);
		changes.filedate.lblMinuteSecondSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(changes.filedate.txtAdjustmentSeconds, adjustmentEnabled);
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
			InputHistory.record(InputHistory.SRC_FOLDER_KEY, processCondition.getSrcFolder());
			InputHistory.record(InputHistory.SRC_FILE_NAME_PATTERN_KEY, fieldText(srcOpt.txtFileNamePattern));
			if (processCondition.getOperationType() != OperationType.Overwrite) {
				InputHistory.record(InputHistory.DEST_FOLDER_KEY, processCondition.getDestFolder());
			}
			InputHistory.record(InputHistory.DEST_SUB_FILE_PATH_PATTERN_KEY, fieldText(destOpt.txtSubFilePathPattern));
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

		values.srcFolder = Paths.get(txtSrcFolder.getText()).normalize();
		values.srcFileNamePattern = fieldText(srcOpt.txtFileNamePattern);
		values.srcFileNamePatternSyntax = srcOpt.selectedFilePatternSyntax();
		values.includeHiddenFiles = srcOpt.chkIncludeHiddenFiles.isEnabled() && srcOpt.chkIncludeHiddenFiles.isSelected();
		values.followLinks = false;
		values.depth = (srcOpt.chkIncludeSubfolders.isEnabled() && srcOpt.chkIncludeSubfolders.isSelected()) ? Integer.MAX_VALUE : 1;

		values.operationType = selectedEnumValue(btngrpOperationType, OperationType.class, OperationType.Copy);

		values.destFolder = Paths.get(txtDestFolder.getText()).normalize();
		values.destSubFilePathPattern = fieldText(destOpt.txtSubFilePathPattern);
		values.existingFileMethod = (ExistingFileMethod)destOpt.cmbExistingFileMethod.getSelectedItem();
		values.checkFileDigest = destOpt.chkCheckFileDigest.isEnabled() && destOpt.chkCheckFileDigest.isSelected();

		values.changeFileCreationDate = changes.filedate.chkCreationDate.isEnabled() && changes.filedate.chkCreationDate.isSelected();
		values.changeFileModifiedDate = changes.filedate.chkModifiedDate.isEnabled() && changes.filedate.chkModifiedDate.isSelected();
		values.changeFileAccessDate = changes.filedate.chkAccessDate.isEnabled() && changes.filedate.chkAccessDate.isSelected();
		values.changeFileExifDate = changes.filedate.chkExifDate.isEnabled() && changes.filedate.chkExifDate.isSelected();
		values.baseDateType = (DateType)changes.filedate.cmbBaseDate.getSelectedItem();
		values.customBaseDate = DateTimeText.parseDate(changes.filedate.txtCustomBaseDate.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.adjustmentType = (DateModType)changes.filedate.cmbAdjustmentType.getSelectedItem();
		values.adjustmentYears = parseInteger(changes.filedate.txtAdjustmentYears.getText());
		values.adjustmentMonths = parseInteger(changes.filedate.txtAdjustmentMonths.getText());
		values.adjustmentDays = parseInteger(changes.filedate.txtAdjustmentDays.getText());
		values.adjustmentHours = parseInteger(changes.filedate.txtAdjustmentHours.getText());
		values.adjustmentMinutes = parseInteger(changes.filedate.txtAdjustmentMinutes.getText());
		values.adjustmentSeconds = parseInteger(changes.filedate.txtAdjustmentSeconds.getText());

		values.removeExifGps = changes.exif.chkRemoveGps.isEnabled() && changes.exif.chkRemoveGps.isSelected();
		values.removeExifAll = changes.exif.chkRemoveAll.isEnabled() && changes.exif.chkRemoveAll.isSelected();

		FileSizeUnit fileSizeUnit = (FileSizeUnit)srcOpt.cmbFileSizeUnit.getSelectedItem();
		values.fileSizeFrom = convertSize(parseLong(srcOpt.txtFileSizeFrom.getText()), fileSizeUnit);
		values.fileSizeTo = convertSize(parseLong(srcOpt.txtFileSizeTo.getText()), fileSizeUnit);

		values.createdFrom = DateTimeText.parseDate(srcOpt.txtCreatedFrom.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.createdTo = DateTimeText.parseDate(srcOpt.txtCreatedTo.getText(), timeZone, Year.now().getValue(), 12, 31, 23, 59, 59, 999);
		values.modifiedFrom = DateTimeText.parseDate(srcOpt.txtModifiedFrom.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.modifiedTo = DateTimeText.parseDate(srcOpt.txtModifiedTo.getText(), timeZone, Year.now().getValue(), 12, 31, 23, 59, 59, 999);

		return values;
	}

	private ProcessCondition createProcessCondition(boolean dryRun) {

		if (!showValidationResult(ProcessConditionValidator.validateSourceFolder(
				fieldText(txtSrcFolder),
				Messages.getString("MainFrame.src.folder")))) {
			return null;
		}
		if (!rdoOperationTypeOverwrite.isSelected()
				&& !showValidationResult(ProcessConditionValidator.validateDestinationFolder(
						fieldText(txtDestFolder),
						Messages.getString("MainFrame.dest.folder")))) {
			return null;
		}

		ProcessConditionValues values = collectProcessConditionValues();

		if (!showValidationResult(ProcessConditionValidator.validate(values))) {
			return null;
		}

		if (!confirmDestructiveOperation(values, dryRun)) {
			return null;
		}

		if (values.checkFileDigest && (values.changeFileExifDate || values.removeExifGps || values.removeExifAll)) {
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

			values.checkFileDigest = false;
		}

		PictoPathFilter pathFilter = buildPathFilter(values);


		String destSubFilePathPattern = values.destSubFilePathPattern.isBlank()
				? EMPTY_DEST_SUB_FILE_PATH_PATTERN
				: values.destSubFilePathPattern;
		NanoTemplate destSubFilePathTemplate = new NanoTemplate(destSubFilePathPattern).timeZone(timeZone);

		ProcessCondition processCondition = new ProcessCondition();
		processCondition.setTimeZone(timeZone);
		processCondition.setSrcFolder(values.srcFolder);
		processCondition.setDestFolder(values.destFolder);
		processCondition.setDepth(values.depth);
		processCondition.setPathFilter(pathFilter);
		processCondition.setFollowLinks(values.followLinks);
		processCondition.setDestSubFilePathTemplate(destSubFilePathTemplate);
		processCondition.setOperationType(values.operationType);
		processCondition.setExistingFileMethod(values.existingFileMethod);
		processCondition.setCheckFileDigest(values.checkFileDigest);
		processCondition.setChangeFileCreationDate(values.changeFileCreationDate);
		processCondition.setChangeFileModifiedDate(values.changeFileModifiedDate);
		processCondition.setChangeFileAccessDate(values.changeFileAccessDate);
		processCondition.setChangeFileExifDate(values.changeFileExifDate);
		processCondition.setBaseDateType(values.baseDateType);
		processCondition.setCustomBaseDate(values.customBaseDate);
		processCondition.setAdjustmentType(values.adjustmentType);
		processCondition.setAdjustmentYears(values.adjustmentYears);
		processCondition.setAdjustmentMonths(values.adjustmentMonths);
		processCondition.setAdjustmentDays(values.adjustmentDays);
		processCondition.setAdjustmentHours(values.adjustmentHours);
		processCondition.setAdjustmentMinutes(values.adjustmentMinutes);
		processCondition.setAdjustmentSeconds(values.adjustmentSeconds);
		processCondition.setRemoveExifGps(values.removeExifGps);
		processCondition.setRemoveExifAll(values.removeExifAll);
		processCondition.setDryRun(dryRun);

		return processCondition;
	}

	private static PictoPathFilter buildPathFilter(ProcessConditionValues values) {
		PictoPathFilter pathFilter = new PictoPathFilter();
		pathFilter.setPathPattern(values.srcFileNamePattern, values.srcFolder, values.srcFileNamePatternSyntax);
		pathFilter.setIncludeHiddenFiles(values.includeHiddenFiles);
		pathFilter.setFileSizeRange(values.fileSizeFrom, values.fileSizeTo);
		pathFilter.setCreatedRange(values.createdFrom, values.createdTo);
		pathFilter.setModifiedRange(values.modifiedFrom, values.modifiedTo);
//		pathFilter.setAccessRange(from, to);
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

		if (!Files.isDirectory(values.srcFolder)) {
			if (sourceFileScanner != null) {
				sourceFileScanner.cancel();
				sourceFileScanner = null;
			}
			matchCountEnabled = false;
			lastMatchCountStatus = null;
			srcOpt.lblMatchCount.setText(Messages.getString("MainFrame.src.matchCount.prompt"));
			srcOpt.setMatchCountScanning(false);
			return;
		}

		PictoPathFilter pathFilter;
		boolean includeSubfolders = values.depth != 1;
		try {
			pathFilter = buildPathFilter(values);
		} catch (Exception e) {
			srcOpt.lblMatchCount.setText("");
			srcOpt.setMatchCountScanning(false);
			return;
		}

		if (sourceFileScanner != null && !sourceFileScanner.srcFolder().equals(values.srcFolder)) {
			// The source folder changed while a count was enabled for the previous one; that opt-in doesn't
			// carry over to a different folder (it could be much larger), so require an explicit re-click.
			sourceFileScanner.cancel();
			sourceFileScanner = null;
			matchCountEnabled = false;
			lastMatchCountStatus = null;
			srcOpt.lblMatchCount.setText(Messages.getString("MainFrame.src.matchCount.prompt"));
			srcOpt.setMatchCountScanning(false);
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
						values.srcFolder, pathFilter, includeSubfolders,
						status -> SwingUtilities.invokeLater(() -> {
							if (sourceFileScanner == holder[0]) {
								renderMatchCount(status);
							}
						}));
			} catch (IOException e) {
				sourceFileScanner = null;
				srcOpt.lblMatchCount.setText("");
				srcOpt.setMatchCountScanning(false);
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
		case SCANNING -> Messages.getString("MainFrame.src.matchCount.scanning", status.count());
		case PAUSED -> Messages.getString("MainFrame.src.matchCount.paused", status.count());
		case EXACT -> Messages.getString("MainFrame.src.matchCount", status.count());
		};
		srcOpt.lblMatchCount.setText(text);
		srcOpt.setMatchCountScanning(status.state() == SourceFileScanner.MatchCountStatus.State.SCANNING);
		if (status.state() == SourceFileScanner.MatchCountStatus.State.PAUSED) {
			srcOpt.lblMatchCount.setToolTipText(Messages.getString("MainFrame.src.matchCount.resume"));
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
		return confirmYesNo(messageKey);
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
		case SOURCE_FOLDER -> txtSrcFolder;
		case DESTINATION_FOLDER -> txtDestFolder;
		case FILE_NAME_PATTERN -> srcOpt.txtFileNamePattern;
		case DESTINATION_SUBFOLDER -> destOpt.txtSubFilePathPattern;
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

}
