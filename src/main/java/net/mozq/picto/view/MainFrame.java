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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TimeZone;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.picto.App;
import net.mozq.picto.AppMain;
import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.core.ProcessCondition;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FileSizeUnit;
import net.mozq.picto.enums.OperationType;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final int WINDOW_PADDING = 10;
	private static final int MAIN_LABEL_WIDTH = 52;
	private static final int SECTION_PADDING = 8;
	private static final int SECTION_HEADER_GAP = 4;
	private static final int SECTION_GAP = 10;
	private static final int OPERATION_BOTTOM_GAP = 14;
	private static final int INLINE_HGAP = 6;
	static final int INLINE_VGAP = 2;
	private static final int RUN_SPLIT_BUTTON_OVERLAP = 4;
	private static final int RUN_MENU_BUTTON_WIDTH = 28;
	private static final int RUN_STATUS_ICON_SIZE = 24;
	private static final int RUN_STATUS_ICON_PADDING = 8;
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
	private MainFrameSettings mainFrameSettings;
	private PresetManager presetManager;

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
					mainFrameSettings.store();
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

		mainFrameSettings = new MainFrameSettings(txtSrcFolder, srcOpt, btngrpOperationType, txtDestFolder, destOpt, changes);
		presetManager = new PresetManager(this, mnPresets, mainFrameSettings);

		installOptionsSummaryListeners();
		mainFrameSettings.load();
		changeEnableDestConditions();
		changeEnableFileDateModConditions();
		restoreFrameState(state);
		updateRunSummary();

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
				presetManager.rebuildPresetsMenu();
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
				presetManager.promptImportData();
			}
		});

		JMenuItem mntmExportData = new JMenuItem(Messages.getString("MainFrame.menu.settings.exportData"));
		mntmExportData.setMnemonic(KeyEvent.VK_E);
		mntmExportData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
		mntmExportData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				presetManager.promptExportData();
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
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
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
		srcOpt.lblMatchCount.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				matchCountLabelClicked();
			}
		});
		srcOpt.btnMatchCountStop.addActionListener(_ -> matchCountStopButtonClicked());
		String srcOptionsTitle = Messages.getString("MainFrame.src.options");
		srcOpt.setOnExpandedChanged(expanded -> {
			btnSrcOptions.setSelected(expanded);
			setOptionsToggleButtonText(btnSrcOptions, srcOptionsTitle, expanded);
		});
		srcOpt.setOnContentChanged(() -> {
			fitWindowToContent();
			if (matchCountEnabled) {
				matchCountTimer.restart();
			}
		});
		btnSrcOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				srcOpt.setExpanded(btnSrcOptions.isSelected());
			}
		});
		srcOpt.setExpanded(false);
		pnlSrcConditions.add(srcOpt, GridBagSupport.at(1, 1).fill(GridBagConstraints.BOTH).gridwidth(2).insets(0, 0, 5, 0).build());

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
		String destOptionsTitle = Messages.getString("MainFrame.dest.options");
		destOpt.setOnExpandedChanged(expanded -> {
			btnDestOptions.setSelected(expanded);
			setOptionsToggleButtonText(btnDestOptions, destOptionsTitle, expanded);
		});
		destOpt.setOnContentChanged(this::fitWindowToContent);
		btnDestOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				destOpt.setExpanded(btnDestOptions.isSelected());
			}
		});
		destOpt.setExpanded(false);
		pnlDestConditions.add(destOpt, GridBagSupport.at(1, 1).fill(GridBagConstraints.BOTH).gridwidth(2).insets(0, 0, 5, 0).build());

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

		String changesTitle = Messages.getString("MainFrame.changes.title");
		changes.setOnExpandedChanged(expanded -> {
			btnChanges.setSelected(expanded);
			setOptionsToggleButtonText(btnChanges, changesTitle, expanded);
		});
		changes.setOnContentChanged(this::fitWindowToContent);
		changes.setExpanded(false);
		btnChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changes.setExpanded(btnChanges.isSelected());
			}
		});

		contentPane.add(btnChanges, GridBagSupport.at(0, 3).anchor(GridBagConstraints.WEST).insets(0, 0, SECTION_HEADER_GAP, 0).build());
		contentPane.add(changes, GridBagSupport.at(0, 4).insets(0, 0, SECTION_GAP, 0).fill(GridBagConstraints.BOTH).build());
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
		Color runStatusHoverBackground = PanelStyleSupport.shade(PanelStyleSupport.color("Panel.background", new Color(0xf2f2f2)));
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

		getContentPane().add(pnlControls, GridBagSupport.at(0, 5).anchor(GridBagConstraints.SOUTH).fill(GridBagConstraints.HORIZONTAL).build());
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

		for (int digit = 1; digit <= PresetManager.PRESET_SHORTCUT_COUNT; digit++) {
			int index = digit - 1;
			String actionKey = "picto.loadPreset" + digit;
			rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(PresetManager.presetShortcutKeyStroke(digit), actionKey);
			rootPane.getActionMap().put(actionKey, new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					presetManager.loadPresetByIndex(index);
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
				changes.selectedTabIndex());
	}

	private void restoreFrameState(MainFrameState state) {
		if (state == null) {
			return;
		}
		if (state.bounds != null) {
			setBounds(state.bounds);
		}
		srcOpt.setExpanded(state.srcOptionsExpanded);
		destOpt.setExpanded(state.destOptionsExpanded);
		changes.setExpanded(state.changesExpanded);
		changes.setSelectedTabIndex(state.changesTabIndex);
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
		Color textFieldBackground = PanelStyleSupport.color("TextField.background", new Color(0xffffff));
		Color buttonBackground = PanelStyleSupport.color("Button.background", new Color(0xf3f3f3));
		Color buttonHoverBackground = PanelStyleSupport.color("Button.hoverBackground", buttonBackground);
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

	private static Color blend(Color base, Color overlay, float overlayRatio) {
		float baseRatio = 1.0f - overlayRatio;
		return new Color(
				Math.round(base.getRed() * baseRatio + overlay.getRed() * overlayRatio),
				Math.round(base.getGreen() * baseRatio + overlay.getGreen() * overlayRatio),
				Math.round(base.getBlue() * baseRatio + overlay.getBlue() * overlayRatio));
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

	/** Wires the fields that no single options panel owns (the folders, the operation type) to the run
	 * summary and the match-count preview - each options panel keeps its own fields' summary and
	 * content-changed notifications to itself. */
	private void installOptionsSummaryListeners() {
		txtSrcFolder.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				srcFolderChanged();
			}

			public void removeUpdate(DocumentEvent e) {
				srcFolderChanged();
			}

			public void changedUpdate(DocumentEvent e) {
				srcFolderChanged();
			}
		});
		DocumentListener destFolderListener = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				runSummaryChanged();
			}

			public void removeUpdate(DocumentEvent e) {
				runSummaryChanged();
			}

			public void changedUpdate(DocumentEvent e) {
				runSummaryChanged();
			}
		};
		txtDestFolder.getDocument().addDocumentListener(destFolderListener);
		ChangeListener operationTypeListener = _ -> runSummaryChanged();
		rdoOperationTypeCopy.addChangeListener(operationTypeListener);
		rdoOperationTypeMove.addChangeListener(operationTypeListener);
		rdoOperationTypeOverwrite.addChangeListener(operationTypeListener);
	}

	private void srcFolderChanged() {
		runSummaryChanged();
		if (matchCountEnabled) {
			matchCountTimer.restart();
		}
	}

	private void runSummaryChanged() {
		updateRunSummary();
		fitWindowToContent();
	}

	private void updateRunSummary() {
		updateRunSummary(collectProcessConditionInput());
	}

	private void updateRunSummary(ProcessConditionInput input) {
		btnStart.setToolTipText(null);
		btnStartMenu.setToolTipText(null);
		if (showingRunSummary) {
			showRunSummary(input);
		} else {
			clearRunSummary();
		}
	}

	private void showRunSummary(ProcessConditionInput input) {
		lblRunSummary.setText(PathTextSupport.abbreviateMiddle(SummaryTextSupport.runSummary(input), lblRunSummary.getWidth() - 8, lblRunSummary));
	}

	private void clearRunSummary() {
		lblRunSummary.setText(" ");
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
		destOpt.setEnabled(enabled);
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

		ProcessConditionInput input = collectProcessConditionInput();

		ProcessConditionBuilder.BuildResult result = ProcessConditionBuilder.build(input, timeZone);
		if (!result.isValid()) {
			showValidationWarning(result.invalidResult().message());
			focusValidationField(result.invalidResult().field());
			return;
		}
		ProcessCondition processCondition = result.processCondition();

		if (!confirmDestructiveOperation(input, dryRun)) {
			return;
		}

		if (input.checkFileDigest && (input.changeFileExifDate || input.removeExifGps || input.removeExifAll)) {
			int ret = JOptionPane.showConfirmDialog(
					frame,
					Messages.getString("message.confirm.change.file.with.checkFileDigest"),
					null,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE
					);
			if (ret == JOptionPane.NO_OPTION) {
				return;
			}

			processCondition.setCheckFileDigest(false);
		}

		processCondition.setDryRun(dryRun);

		if (!dryRun) {
			InputHistory.record(InputHistory.SRC_FOLDER_KEY, input.srcFolder);
			InputHistory.record(InputHistory.SRC_FILE_NAME_PATTERN_KEY, input.srcFileNamePattern);
			if (input.operationType != OperationType.Overwrite) {
				InputHistory.record(InputHistory.DEST_FOLDER_KEY, input.destFolder);
			}
			InputHistory.record(InputHistory.DEST_SUB_FILE_PATH_PATTERN_KEY, input.destSubFilePathPattern);
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
		Color runStatusIconColor = PanelStyleSupport.color("Label.foreground", Color.BLACK);
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

	private ProcessConditionInput collectProcessConditionInput() {
		ProcessConditionInput input = new ProcessConditionInput();

		input.srcFolder = SummaryTextSupport.fieldText(txtSrcFolder);
		input.srcFileNamePattern = SummaryTextSupport.fieldText(srcOpt.txtFileNamePattern);
		input.srcFileNamePatternSyntax = srcOpt.selectedFilePatternSyntax();
		input.includeHiddenFiles = srcOpt.chkIncludeHiddenFiles.isEnabled() && srcOpt.chkIncludeHiddenFiles.isSelected();
		input.followLinks = false;
		input.includeSubfolders = srcOpt.chkIncludeSubfolders.isEnabled() && srcOpt.chkIncludeSubfolders.isSelected();

		input.operationType = MainFrameSettings.selectedEnumValue(btngrpOperationType, OperationType.class, OperationType.Copy);

		input.destFolder = SummaryTextSupport.fieldText(txtDestFolder);
		input.destSubFilePathPattern = SummaryTextSupport.fieldText(destOpt.txtSubFilePathPattern);
		input.existingFileMethod = (ExistingFileMethod)destOpt.cmbExistingFileMethod.getSelectedItem();
		input.checkFileDigest = destOpt.chkCheckFileDigest.isEnabled() && destOpt.chkCheckFileDigest.isSelected();

		input.changeFileCreationDate = changes.filedate.chkCreationDate.isEnabled() && changes.filedate.chkCreationDate.isSelected();
		input.changeFileModifiedDate = changes.filedate.chkModifiedDate.isEnabled() && changes.filedate.chkModifiedDate.isSelected();
		input.changeFileAccessDate = changes.filedate.chkAccessDate.isEnabled() && changes.filedate.chkAccessDate.isSelected();
		input.changeFileExifDate = changes.filedate.chkExifDate.isEnabled() && changes.filedate.chkExifDate.isSelected();
		input.baseDateType = (DateType)changes.filedate.cmbBaseDate.getSelectedItem();
		input.customBaseDate = SummaryTextSupport.fieldText(changes.filedate.txtCustomBaseDate);
		input.adjustmentType = (DateModType)changes.filedate.cmbAdjustmentType.getSelectedItem();
		input.adjustmentYears = SummaryTextSupport.fieldText(changes.filedate.txtAdjustmentYears);
		input.adjustmentMonths = SummaryTextSupport.fieldText(changes.filedate.txtAdjustmentMonths);
		input.adjustmentDays = SummaryTextSupport.fieldText(changes.filedate.txtAdjustmentDays);
		input.adjustmentHours = SummaryTextSupport.fieldText(changes.filedate.txtAdjustmentHours);
		input.adjustmentMinutes = SummaryTextSupport.fieldText(changes.filedate.txtAdjustmentMinutes);
		input.adjustmentSeconds = SummaryTextSupport.fieldText(changes.filedate.txtAdjustmentSeconds);

		input.removeExifGps = changes.exif.chkRemoveGps.isEnabled() && changes.exif.chkRemoveGps.isSelected();
		input.removeExifAll = changes.exif.chkRemoveAll.isEnabled() && changes.exif.chkRemoveAll.isSelected();

		input.fileSizeFrom = SummaryTextSupport.fieldText(srcOpt.txtFileSizeFrom);
		input.fileSizeTo = SummaryTextSupport.fieldText(srcOpt.txtFileSizeTo);
		input.fileSizeUnit = (FileSizeUnit)srcOpt.cmbFileSizeUnit.getSelectedItem();

		input.createdFrom = SummaryTextSupport.fieldText(srcOpt.txtCreatedFrom);
		input.createdTo = SummaryTextSupport.fieldText(srcOpt.txtCreatedTo);
		input.modifiedFrom = SummaryTextSupport.fieldText(srcOpt.txtModifiedFrom);
		input.modifiedTo = SummaryTextSupport.fieldText(srcOpt.txtModifiedTo);

		return input;
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
		ProcessConditionInput input = collectProcessConditionInput();

		Path srcFolder;
		try {
			srcFolder = Paths.get(input.srcFolder).normalize();
		} catch (InvalidPathException e) {
			srcFolder = null;
		}

		if (srcFolder == null || !Files.isDirectory(srcFolder)) {
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
		boolean includeSubfolders = input.includeSubfolders;
		try {
			pathFilter = ProcessConditionBuilder.buildPathFilter(input, srcFolder, timeZone);
		} catch (Exception e) {
			srcOpt.lblMatchCount.setText("");
			srcOpt.setMatchCountScanning(false);
			return;
		}

		if (sourceFileScanner != null && !sourceFileScanner.srcFolder().equals(srcFolder)) {
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
						srcFolder, pathFilter, includeSubfolders,
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

	private boolean confirmDestructiveOperation(ProcessConditionInput input, boolean dryRun) {
		if (dryRun || input.operationType == OperationType.Copy) {
			return true;
		}
		String messageKey = switch (input.operationType) {
		case Move -> "message.confirm.destructive.move";
		case Overwrite -> "message.confirm.destructive.overwrite";
		case Copy -> throw new IllegalStateException(input.operationType.toString());
		};
		return DialogSupport.confirmYesNo(frame, messageKey);
	}

	private void showValidationWarning(String message) {
		JOptionPane.showMessageDialog(
				frame,
				message,
				null,
				JOptionPane.WARNING_MESSAGE
				);
	}

	private void focusValidationField(ProcessConditionBuilder.Field field) {
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

}
