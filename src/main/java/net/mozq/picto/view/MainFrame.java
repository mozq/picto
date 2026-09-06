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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.AbstractButton;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.appsettings.AppSettings;
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
	private JLabel lblDestRootDirPath;
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
	private JPanel pnlSrcOptionsBody;
	private JPanel pnlDestOptionsBody;
	private JTextArea txtSrcOptionsSummary;
	private JTextArea txtDestOptionsSummary;
	private JTextArea txtChangesSummary;
	private JLabel lblRunSummary;
	private boolean showingRunSummary;
	private JMenuBar menuBar;
	private JMenu mnPreferences;
	private JMenu mnLanguage;
	private JMenu mnAppearance;
	private JMenu mnHelp;
	private JMenuItem mntmHelp;
	private JMenuItem mntmImportSettings;
	private JMenuItem mntmExportSettings;
	private boolean processing;

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

		installOperationListeners();

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

		mnPreferences = new JMenu(Messages.getString("MainFrame.menu.preferences"));
		menuBar.add(mnPreferences);

		mnLanguage = new JMenu(Messages.getString("MainFrame.menu.preferences.language"));
		mnPreferences.add(mnLanguage);
		ButtonGroup languageGroup = new ButtonGroup();
		addPreferenceMenuItem(
				mnLanguage,
				languageGroup,
				Messages.getString("MainFrame.menu.preferences.language.system"),
				AppMain.PREF_LOCALE_KEY,
				AppMain.PREF_SYSTEM);
		addPreferenceMenuItem(
				mnLanguage,
				languageGroup,
				Messages.getString("MainFrame.menu.preferences.language.en"),
				AppMain.PREF_LOCALE_KEY,
				AppMain.PREF_LOCALE_EN);
		addPreferenceMenuItem(
				mnLanguage,
				languageGroup,
				Messages.getString("MainFrame.menu.preferences.language.ja"),
				AppMain.PREF_LOCALE_KEY,
				AppMain.PREF_LOCALE_JA);

		mnAppearance = new JMenu(Messages.getString("MainFrame.menu.preferences.appearance"));
		mnPreferences.add(mnAppearance);
		ButtonGroup appearanceGroup = new ButtonGroup();
		addPreferenceMenuItem(
				mnAppearance,
				appearanceGroup,
				Messages.getString("MainFrame.menu.preferences.appearance.system"),
				AppMain.PREF_APPEARANCE_KEY,
				AppMain.PREF_SYSTEM);
		addPreferenceMenuItem(
				mnAppearance,
				appearanceGroup,
				Messages.getString("MainFrame.menu.preferences.appearance.light"),
				AppMain.PREF_APPEARANCE_KEY,
				AppMain.PREF_APPEARANCE_LIGHT);
		addPreferenceMenuItem(
				mnAppearance,
				appearanceGroup,
				Messages.getString("MainFrame.menu.preferences.appearance.dark"),
				AppMain.PREF_APPEARANCE_KEY,
				AppMain.PREF_APPEARANCE_DARK);

		mnPreferences.addSeparator();

		mntmImportSettings = new JMenuItem(Messages.getString("MainFrame.menu.preferences.importSettings"));
		mntmImportSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("settings.ext.description"), SETTINGS_FILE_NAME_EXT));

				int selected = filechooser.showOpenDialog(frame);
				if (selected == JFileChooser.APPROVE_OPTION) {
					File file = filechooser.getSelectedFile();
					try {
						App.config().loadFrom(file.toPath());
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
		mnPreferences.add(mntmImportSettings);

		mntmExportSettings = new JMenuItem(Messages.getString("MainFrame.menu.preferences.exportSettings"));
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
						App.config().storeTo(file.toPath(), "");

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
		mnPreferences.add(mntmExportSettings);

		mnHelp = new JMenu(Messages.getString("MainFrame.menu.help"));
		menuBar.add(mnHelp);

		mntmHelp = new JMenuItem(Messages.getString("MainFrame.menu.help.help"));
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
		gbl_pnlSrcConditions.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlSrcConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlSrcConditions.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSrcConditions.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSrcConditions.setLayout(gbl_pnlSrcConditions);

		JLabel lblSrcConditionsTitle = newMainLabel(Messages.getString("MainFrame.srcConditionsTitle"));
		GridBagConstraints gbc_lblSrcConditionsTitle = new GridBagConstraints();
		gbc_lblSrcConditionsTitle.anchor = GridBagConstraints.WEST;
		gbc_lblSrcConditionsTitle.insets = new Insets(0, 0, 5, 8);
		gbc_lblSrcConditionsTitle.gridx = 0;
		gbc_lblSrcConditionsTitle.gridy = 0;
		pnlSrcConditions.add(lblSrcConditionsTitle, gbc_lblSrcConditionsTitle);

		JLabel lblSrcRootDirPath = new JLabel(UIManager.getIcon("FileView.directoryIcon"));
		lblSrcRootDirPath.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.srcRootDirPath"));
		lblSrcRootDirPath.setToolTipText(Messages.getString("MainFrame.srcRootDirPath"));
		GridBagConstraints gbc_lblSrcRootDirPath = new GridBagConstraints();
		gbc_lblSrcRootDirPath.anchor = GridBagConstraints.WEST;
		gbc_lblSrcRootDirPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblSrcRootDirPath.gridx = 1;
		gbc_lblSrcRootDirPath.gridy = 0;
		pnlSrcConditions.add(lblSrcRootDirPath, gbc_lblSrcRootDirPath);

		pnlSrcRootDirPath = new JPanel();
		pnlSrcRootDirPath.setBorder(null);
		GridBagConstraints gbc_pnlSrcRootDirPath = new GridBagConstraints();
		gbc_pnlSrcRootDirPath.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSrcRootDirPath.fill = GridBagConstraints.BOTH;
		gbc_pnlSrcRootDirPath.gridx = 2;
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
		lblSrcRootDirPath.setLabelFor(txtSrcRootDirPath);
		txtSrcRootDirPath.setToolTipText(Messages.getString("MainFrame.srcRootDirPath"));
		pnlSrcRootDirPath.add(txtSrcRootDirPath, BorderLayout.CENTER);
		txtSrcRootDirPath.setColumns(10);
		InputSupport.installLabelFocusAction(lblSrcRootDirPath, txtSrcRootDirPath, LabelFocusBehavior.CARET_END);
		InputSupport.installFolderDropTarget(txtSrcRootDirPath);
		btnSrcRootDirSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (!txtSrcRootDirPath.getText().isEmpty()) {
					filechooser.setCurrentDirectory(new File(txtSrcRootDirPath.getText()));
				}

				int selected = filechooser.showOpenDialog(frame);
				if (selected == JFileChooser.APPROVE_OPTION) {
					File file = filechooser.getSelectedFile();
					txtSrcRootDirPath.setText(file.getAbsolutePath());
				}
			}
		});

		pnlSrcOptionsBody = new JPanel();
		GridBagConstraints gbc_pnlSrcOptionsBody = new GridBagConstraints();
		gbc_pnlSrcOptionsBody.fill = GridBagConstraints.BOTH;
		gbc_pnlSrcOptionsBody.gridwidth = 3;
		gbc_pnlSrcOptionsBody.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSrcOptionsBody.gridx = 1;
		gbc_pnlSrcOptionsBody.gridy = 1;
		pnlSrcConditions.add(pnlSrcOptionsBody, gbc_pnlSrcOptionsBody);
		GridBagLayout gbl_pnlSrcOptionsBody = new GridBagLayout();
		gbl_pnlSrcOptionsBody.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSrcOptionsBody.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlSrcOptionsBody.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSrcOptionsBody.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSrcOptionsBody.setLayout(gbl_pnlSrcOptionsBody);
		setOptionsExpanded(btnSrcOptions, pnlSrcOptionsBody, Messages.getString("MainFrame.srcOptionsTitle"), false);
		btnSrcOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnSrcOptions, pnlSrcOptionsBody, Messages.getString("MainFrame.srcOptionsTitle"), btnSrcOptions.isSelected());
			}
		});

		txtSrcOptionsSummary = newOptionsSummaryText(btnSrcOptions, pnlSrcOptionsBody, Messages.getString("MainFrame.srcOptionsTitle"));
		GridBagConstraints gbc_txtSrcOptionsSummary = new GridBagConstraints();
		gbc_txtSrcOptionsSummary.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSrcOptionsSummary.gridwidth = 3;
		gbc_txtSrcOptionsSummary.insets = new Insets(0, 0, 5, 0);
		gbc_txtSrcOptionsSummary.gridx = 1;
		gbc_txtSrcOptionsSummary.gridy = 1;
		pnlSrcConditions.add(txtSrcOptionsSummary, gbc_txtSrcOptionsSummary);

		SourceOptionsPanel sourceOptionsPanel = new SourceOptionsPanel(INLINE_HGAP, INLINE_VGAP);
		pnlSrcOptionsBody.add(sourceOptionsPanel, fillHorizontalConstraints(0, 0, 3, 0));
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
		btngrpOpeType.add(rdoOpeTypeCopy);
		pnlOpeType.add(rdoOpeTypeCopy);

		rdoOpeTypeMove = new JRadioButton(Messages.getString("MainFrame.opeTypeMove"));
		btngrpOpeType.add(rdoOpeTypeMove);
		pnlOpeType.add(rdoOpeTypeMove);

		rdoOpeTypeOverwrite = new JRadioButton(Messages.getString("MainFrame.opeTypeOverwrite"));
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
		gbl_pnlDestConditions.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlDestConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlDestConditions.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlDestConditions.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlDestConditions.setLayout(gbl_pnlDestConditions);

		lblDestConditionsTitle = newMainLabel(Messages.getString("MainFrame.destConditionsTitle"));
		GridBagConstraints gbc_lblDestConditionsTitle = new GridBagConstraints();
		gbc_lblDestConditionsTitle.anchor = GridBagConstraints.WEST;
		gbc_lblDestConditionsTitle.insets = new Insets(0, 0, 5, 8);
		gbc_lblDestConditionsTitle.gridx = 0;
		gbc_lblDestConditionsTitle.gridy = 0;
		pnlDestConditions.add(lblDestConditionsTitle, gbc_lblDestConditionsTitle);

		lblDestRootDirPath = new JLabel(UIManager.getIcon("FileView.directoryIcon"));
		lblDestRootDirPath.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.destRootDirPath"));
		lblDestRootDirPath.setToolTipText(Messages.getString("MainFrame.destRootDirPath"));
		GridBagConstraints gbc_lblDestRootDirPath = new GridBagConstraints();
		gbc_lblDestRootDirPath.anchor = GridBagConstraints.WEST;
		gbc_lblDestRootDirPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblDestRootDirPath.gridx = 1;
		gbc_lblDestRootDirPath.gridy = 0;
		pnlDestConditions.add(lblDestRootDirPath, gbc_lblDestRootDirPath);

		pnlDestRootDirPath = new JPanel();
		pnlDestRootDirPath.setBorder(null);
		GridBagConstraints gbc_pnlDestRootDirPath = new GridBagConstraints();
		gbc_pnlDestRootDirPath.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDestRootDirPath.fill = GridBagConstraints.BOTH;
		gbc_pnlDestRootDirPath.gridx = 2;
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
		lblDestRootDirPath.setLabelFor(txtDestRootDirPath);
		txtDestRootDirPath.setToolTipText(Messages.getString("MainFrame.destRootDirPath"));
		pnlDestRootDirPath.add(txtDestRootDirPath, BorderLayout.CENTER);
		txtDestRootDirPath.setColumns(10);
		InputSupport.installLabelFocusAction(lblDestRootDirPath, txtDestRootDirPath, LabelFocusBehavior.CARET_END);
		InputSupport.installFolderDropTarget(txtDestRootDirPath);
		btnDestRootDirSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (!txtDestRootDirPath.getText().isEmpty()) {
					filechooser.setCurrentDirectory(new File(txtDestRootDirPath.getText()));
				}

				int selected = filechooser.showOpenDialog(frame);
				if (selected == JFileChooser.APPROVE_OPTION) {
					File file = filechooser.getSelectedFile();
					txtDestRootDirPath.setText(file.getAbsolutePath());
				}
			}
		});

		pnlDestOptionsBody = new JPanel();
		GridBagConstraints gbc_pnlDestOptionsBody = new GridBagConstraints();
		gbc_pnlDestOptionsBody.fill = GridBagConstraints.BOTH;
		gbc_pnlDestOptionsBody.gridwidth = 3;
		gbc_pnlDestOptionsBody.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDestOptionsBody.gridx = 1;
		gbc_pnlDestOptionsBody.gridy = 1;
		pnlDestConditions.add(pnlDestOptionsBody, gbc_pnlDestOptionsBody);
		GridBagLayout gbl_pnlDestOptionsBody = new GridBagLayout();
		gbl_pnlDestOptionsBody.columnWidths = new int[]{0, 0, 0};
		gbl_pnlDestOptionsBody.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlDestOptionsBody.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlDestOptionsBody.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlDestOptionsBody.setLayout(gbl_pnlDestOptionsBody);
		setOptionsExpanded(btnDestOptions, pnlDestOptionsBody, Messages.getString("MainFrame.destOptionsTitle"), false);
		btnDestOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnDestOptions, pnlDestOptionsBody, Messages.getString("MainFrame.destOptionsTitle"), btnDestOptions.isSelected());
			}
		});

		txtDestOptionsSummary = newOptionsSummaryText(btnDestOptions, pnlDestOptionsBody, Messages.getString("MainFrame.destOptionsTitle"));
		GridBagConstraints gbc_txtDestOptionsSummary = new GridBagConstraints();
		gbc_txtDestOptionsSummary.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDestOptionsSummary.gridwidth = 3;
		gbc_txtDestOptionsSummary.insets = new Insets(0, 0, 5, 0);
		gbc_txtDestOptionsSummary.gridx = 1;
		gbc_txtDestOptionsSummary.gridy = 1;
		pnlDestConditions.add(txtDestOptionsSummary, gbc_txtDestOptionsSummary);

		DestinationOptionsPanel destinationOptionsPanel = new DestinationOptionsPanel();
		pnlDestOptionsBody.add(destinationOptionsPanel, fillHorizontalConstraints(0, 0, 3, 0));
		lblDestSubPathPattern = destinationOptionsPanel.destSubPathPatternLabel;
		txtDestSubPathPattern = destinationOptionsPanel.destSubPathPatternTextField;
		lblExistingFileMethod = destinationOptionsPanel.existingFileMethodLabel;
		cmbExistingFileMethod = destinationOptionsPanel.existingFileMethodComboBox;
		lblValidateFile = destinationOptionsPanel.validateFileLabel;
		chkCheckFileDigest = destinationOptionsPanel.checkFileDigestCheckBox;

	}

	private void buildChangesPanel() {
		btnChanges = newOptionsToggleButton(Messages.getString("MainFrame.changesTitle"));
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
		btnStart.putClientProperty("JButton.buttonType", "default");
		btnStart.setMargin(new Insets(7, 28, 7, 28));
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runProcess(false);
			}
		});

		btnStartMenu = new JButton("\u25be");
		btnStartMenu.putClientProperty(FlatClientProperties.BUTTON_TYPE, "default");
		btnStartMenu.putClientProperty(FlatClientProperties.MINIMUM_WIDTH, 0);
		btnStartMenu.setMargin(new Insets(7, 5, 7, 5));
		Dimension btnStartMenuSize = btnStartMenu.getPreferredSize();
		btnStartMenuSize.width = RUN_MENU_BUTTON_WIDTH;
		btnStartMenu.setPreferredSize(btnStartMenuSize);
		JPopupMenu runMenu = new JPopupMenu();
		JMenuItem mntmDryRun = new JMenuItem(Messages.getString("MainFrame.dryRun"));
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

		JPanel pnlRunButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		pnlRunButton.setBorder(null);
		pnlRunButton.add(newRunSplitButtonPanel(btnStart, btnStartMenu));
		pnlControls.add(pnlRunButton, BorderLayout.CENTER);

		lblRunSummary = newRunSummaryLabel();
		installRunSummaryHover(btnStart);
		installRunSummaryHover(btnStartMenu);
		pnlControls.add(lblRunSummary, BorderLayout.SOUTH);

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
		AppSettings conf = App.config();

		txtSrcRootDirPath.setText(conf.getString("src.root.dir", ""));
		txtFilePattern.setText(conf.getString("file.pattern", ""));
		cmbFilePatternSyntax.setSelectedItem(FilePatternSyntax.of(conf.getBoolean("file.pattern.regex", false)));
		chkContainsSubs.setSelected(conf.getBoolean("contains.subs", true));
		chkContainsHiddens.setSelected(conf.getBoolean("contains.hiddens", false));

		txtFileSizeRangeFrom.setText(conf.getString("file.size.range.from", ""));
		txtFileSizeRangeTo.setText(conf.getString("file.size.range.to", ""));
		cmbFileSizeUnit.setSelectedItem(conf.getEnum("file.size.unit", FileSizeUnit.class, FileSizeUnit.MB));
		txtCreationTimeRangeFrom.setText(conf.getString("creation.time.range.from", ""));
		txtCreationTimeRangeTo.setText(conf.getString("creation.time.range.to", ""));
		txtModifiedTimeRangeFrom.setText(conf.getString("modified.time.range.from", ""));
		txtModifiedTimeRangeTo.setText(conf.getString("modified.time.range.to", ""));

		rdoOpeTypeCopy.setSelected(conf.getBoolean("ope.type.copy", true));
		rdoOpeTypeMove.setSelected(conf.getBoolean("ope.type.move", false));
		rdoOpeTypeOverwrite.setSelected(conf.getBoolean("ope.type.overwrite", false));

		txtDestRootDirPath.setText(conf.getString("dest.root.dir", ""));
		txtDestSubPathPattern.setText(conf.getString("dest.sub.path.pattern", DEFAULT_DEST_SUB_PATH_PATTERN));
		cmbExistingFileMethod.setSelectedItem(conf.getEnum("existing.file.method", ExistingFileMethod.class, ExistingFileMethod.Confirm));
		chkCheckFileDigest.setSelected(conf.getBoolean("check.file.digest", false));

		chkChangeFileCreationDate.setSelected(conf.getBoolean("change.file.creation.date", false));
		chkChangeFileModifiedDate.setSelected(conf.getBoolean("change.file.modified.date", false));
		chkChangeFileAccessDate.setSelected(conf.getBoolean("change.file.access.date", false));
		chkChangeExifDate.setSelected(conf.getBoolean("change.file.exif.date", false));
		cmbBaseDateType.setSelectedItem(conf.getEnum("base.date.type", DateType.class, DateType.FileModifiedDate));
		txtCustomBaseDate.setText(conf.getString("custom.base.date", ""));
		cmbDateModType.setSelectedItem(conf.getEnum("date.mod.type", DateModType.class, DateModType.None));
		txtDateModYears.setText(conf.getString("date.mod.year", ""));
		txtDateModMonths.setText(conf.getString("date.mod.month", ""));
		txtDateModDays.setText(conf.getString("date.mod.day", ""));
		txtDateModHours.setText(conf.getString("date.mod.hour", ""));
		txtDateModMinutes.setText(conf.getString("date.mod.minute", ""));
		txtDateModSeconds.setText(conf.getString("date.mod.second", ""));
		chkRemoveExifTagsGps.setSelected(conf.getBoolean("remove.exif.tags.gps", false));
		chkRemoveExifTagsAll.setSelected(conf.getBoolean("remove.exif.tags.all", false));

	}

	protected void storeSettings() throws IOException {
		AppSettings conf = App.config();

		conf.set(AppMain.PREF_LOCALE_KEY, conf.getString(AppMain.PREF_LOCALE_KEY, AppMain.PREF_SYSTEM));
		conf.set(AppMain.PREF_APPEARANCE_KEY, conf.getString(AppMain.PREF_APPEARANCE_KEY, AppMain.PREF_SYSTEM));

		conf.set("src.root.dir", txtSrcRootDirPath.getText());
		conf.set("file.pattern", txtFilePattern.getText());
		conf.set("file.pattern.regex", getSelectedFilePatternSyntax().isRegex());
		conf.set("contains.subs", chkContainsSubs.isSelected());
		conf.set("contains.hiddens", chkContainsHiddens.isSelected());

		conf.set("file.size.range.from", txtFileSizeRangeFrom.getText());
		conf.set("file.size.range.to", txtFileSizeRangeTo.getText());
		conf.set("file.size.unit", cmbFileSizeUnit.getSelectedItem());
		conf.set("creation.time.range.from", txtCreationTimeRangeFrom.getText());
		conf.set("creation.time.range.to", txtCreationTimeRangeTo.getText());
		conf.set("modified.time.range.from", txtModifiedTimeRangeFrom.getText());
		conf.set("modified.time.range.to", txtModifiedTimeRangeTo.getText());

		conf.set("ope.type.copy", rdoOpeTypeCopy.isSelected());
		conf.set("ope.type.move", rdoOpeTypeMove.isSelected());
		conf.set("ope.type.overwrite", rdoOpeTypeOverwrite.isSelected());

		conf.set("dest.root.dir", txtDestRootDirPath.getText());
		conf.set("dest.sub.path.pattern", txtDestSubPathPattern.getText());
		conf.set("existing.file.method", cmbExistingFileMethod.getSelectedItem());
		conf.set("check.file.digest", chkCheckFileDigest.isSelected());

		conf.set("change.file.creation.date", chkChangeFileCreationDate.isSelected());
		conf.set("change.file.modified.date", chkChangeFileModifiedDate.isSelected());
		conf.set("change.file.access.date", chkChangeFileAccessDate.isSelected());
		conf.set("change.file.exif.date", chkChangeExifDate.isSelected());
		conf.set("base.date.type", cmbBaseDateType.getSelectedItem());
		conf.set("custom.base.date", txtCustomBaseDate.getText());
		conf.set("date.mod.type", cmbDateModType.getSelectedItem());
		conf.set("date.mod.year", txtDateModYears.getText());
		conf.set("date.mod.month", txtDateModMonths.getText());
		conf.set("date.mod.day", txtDateModDays.getText());
		conf.set("date.mod.hour", txtDateModHours.getText());
		conf.set("date.mod.minute", txtDateModMinutes.getText());
		conf.set("date.mod.second", txtDateModSeconds.getText());
		conf.set("remove.exif.tags.gps", chkRemoveExifTagsGps.isSelected());
		conf.set("remove.exif.tags.all", chkRemoveExifTagsAll.isSelected());

		conf.store(null);
		App.deleteMigratedLegacySettingsIfNeeded();
	}

	private void addPreferenceMenuItem(JMenu menu, ButtonGroup group, String label, String key, String value) {
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(label);
		item.setActionCommand(value);
		item.setSelected(value.equals(App.config().getString(key, AppMain.PREF_SYSTEM)));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (processing) {
					selectCurrentPreferenceMenuItem(group, key);
					showPreferencesProcessingMessage();
					return;
				}
				if (!value.equals(App.config().getString(key, AppMain.PREF_SYSTEM))) {
					App.config().set(key, value);
					try {
						storeSettings();
						applyPreferencesImmediately();
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

	private static void selectCurrentPreferenceMenuItem(ButtonGroup group, String key) {
		String currentValue = App.config().getString(key, AppMain.PREF_SYSTEM);
		for (java.util.Enumeration<AbstractButton> e = group.getElements(); e.hasMoreElements();) {
			AbstractButton button = e.nextElement();
			if (currentValue.equals(button.getActionCommand())) {
				button.setSelected(true);
				break;
			}
		}
	}

	private void showPreferencesProcessingMessage() {
		JOptionPane.showMessageDialog(
				frame,
				Messages.getString("message.warn.preferences.processing"),
				null,
				JOptionPane.WARNING_MESSAGE
				);
	}

	private void applyPreferencesImmediately() {
		MainFrameState state = captureFrameState();
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
		setOptionsExpanded(btnSrcOptions, pnlSrcOptionsBody, Messages.getString("MainFrame.srcOptionsTitle"), state.srcOptionsExpanded);
		setOptionsExpanded(btnDestOptions, pnlDestOptionsBody, Messages.getString("MainFrame.destOptionsTitle"), state.destOptionsExpanded);
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

	private static GridBagConstraints fillHorizontalConstraints(int x, int y, int width, int bottomInset) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.insets = new Insets(0, 0, bottomInset, 0);
		return constraints;
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
		setOptionsToggleButtonText(button, title, false);
		return button;
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
		ChangeListener changeListener = e -> optionsSummaryChanged();
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
	}

	private void updateOptionsSummaries() {
		if (txtSrcOptionsSummary == null || txtDestOptionsSummary == null || txtChangesSummary == null || btnStart == null || btnStartMenu == null || lblRunSummary == null) {
			return;
		}
		updateOptionsSummary(txtSrcOptionsSummary, pnlSrcOptionsBody, sourceOptionsSummary());
		updateOptionsSummary(txtDestOptionsSummary, pnlDestOptionsBody, destinationOptionsSummary());
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
			items.add(Messages.getString("MainFrame.containsSubs"));
		}
		if (values.containsHiddens) {
			items.add(Messages.getString("MainFrame.containsHiddens"));
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
			items.add(Messages.getString("MainFrame.validateFile"));
		}
		return joinOptionsSummary(items);
	}

	private String changesSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		List<String> items = new ArrayList<>();
		boolean changesFileDate = false;
		if (values.changeFileCreationDate) {
			items.add(Messages.getString("MainFrame.changeFileCreationDate"));
			changesFileDate = true;
		}
		if (values.changeFileModifiedDate) {
			items.add(Messages.getString("MainFrame.changeFileModifiedDate"));
			changesFileDate = true;
		}
		if (values.changeFileAccessDate) {
			items.add(Messages.getString("MainFrame.changeFileAccessDate"));
			changesFileDate = true;
		}
		if (values.changeExifDate) {
			items.add(Messages.getString("MainFrame.changeFileExifDate"));
			changesFileDate = true;
		}
		if (changesFileDate && values.baseDateType != DateType.FileModifiedDate) {
			items.add(summaryItem(Messages.getString("MainFrame.changeFileBaseDateType"), baseDateTypeSummary(values)));
		}
		if (changesFileDate && values.baseDateModType != DateModType.None) {
			items.add(summaryItem(Messages.getString("MainFrame.changeFileEditBaseDate"), adjustmentSummary(values)));
		}
		if (values.removeExifTagsGps) {
			items.add(Messages.getString("MainFrame.removeExifTagsGps"));
		}
		if (values.removeExifTagsAll) {
			items.add(Messages.getString("MainFrame.removeExifTagsAll"));
		}
		return joinOptionsSummary(items);
	}

	private static boolean hasText(JTextField field) {
		return !fieldText(field).isBlank();
	}

	private static boolean hasDateText(JTextField field) {
		return DateTimeText.hasDateTimeText(field.getText());
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
		lblDestRootDirPath.setEnabled(enabled);
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
		ProcessCondition processCondition = createProcessCondition(dryRun);

		if (processCondition == null) {
			// Validation failed
			return;
		}

		ProcessDialog processDialog = new ProcessDialog(frame);
		processDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		processDialog.setLocationRelativeTo(frame);
		processing = true;
		mnPreferences.setEnabled(false);
		try {
			processDialog.doProcess(processCondition);
			processDialog.setVisible(true);
		} finally {
			processing = false;
			mnPreferences.setEnabled(true);
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

		// Validations
		if (!showValidationResult(ProcessConditionValidator.validate(values))) {
			return null;
		}

		// Information
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

		// Set values
		PictoPathFilter pathFilter = new PictoPathFilter();
		pathFilter.setPathPattern(values.filePattern, values.srcRootDirPath, values.filePatternRegex);
		pathFilter.setContainsHiddens(values.containsHiddens);
		pathFilter.setSizeRange(values.sizeRangeFrom, values.sizeRangeTo);
		pathFilter.setCreationTimeRange(values.creationTimeRangeFrom, values.creationTimeRangeTo);
		pathFilter.setModifiedTimeRange(values.modifiedTimeRangeFrom, values.modifiedTimeRangeTo);
//		pathFilter.setAccessTimeRange(from, to);


		NanoTemplate destSubPathTemplate = new NanoTemplate(values.destSubPathPattern).timeZone(timeZone);

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
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Long parseLong(String numberText) {
		if (numberText == null || numberText.isEmpty()) {
			return null;
		}
		try {
			return Long.valueOf(numberText);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private FilePatternSyntax getSelectedFilePatternSyntax() {
		Object selectedItem = cmbFilePatternSyntax.getSelectedItem();
		return selectedItem instanceof FilePatternSyntax ? (FilePatternSyntax)selectedItem : FilePatternSyntax.GLOB;
	}

}
