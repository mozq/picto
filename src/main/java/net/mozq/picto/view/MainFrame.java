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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.MaskFormatter;

import net.mozq.appsettings.AppSettings;
import net.mozq.nanotemplate.NanoTemplate;
import net.mozq.picto.App;
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

	private static final String DATE_MASK_PATTERN = "****/**/** **:**:**"; //$NON-NLS-1$
	private static final String DATE_MASK_PLACEHOLDER = "_"; //$NON-NLS-1$
	private static final char DATE_MASK_PLACEHOLDER_CHAR = '_';
	private static final String DATE_MASK_VALID_CHARS = "0123456789_"; //$NON-NLS-1$
	private static final String DATE_NASK_DEFAULT_VALUE = "____/__/__ __:__:__"; //$NON-NLS-1$
	private static final String DEFAULT_DEST_SUB_PATH_PATTERN = "${FileName}"; //$NON-NLS-1$
	private static final String CLIENT_PROPERTY_ENABLED_BACKGROUND = "picto.enabledBackground"; //$NON-NLS-1$
	private static final String CLIENT_PROPERTY_DISABLED_BACKGROUND = "picto.disabledBackground"; //$NON-NLS-1$
	private static final int WINDOW_PADDING = 10;
	private static final int MAIN_LABEL_WIDTH = 52;
	private static final int SECTION_PADDING = 8;
	private static final int SECTION_HEADER_GAP = 4;
	private static final int SECTION_GAP = 10;
	private static final int OPERATION_BOTTOM_GAP = 14;
	private static final int FIELD_BUTTON_GAP = 6;
	private static final int INLINE_HGAP = 6;
	private static final int INLINE_VGAP = 2;

	private TimeZone timeZone = TimeZone.getDefault();

	private static final String SETTINGS_FILE_NAME_EXT = "conf"; //$NON-NLS-1$

	private final JFrame frame;
	private boolean windowLayoutReady;

	private JPanel contentPane;

	private JPanel pnlSrcConditions;
	private JPanel pnlSrcRootDirPath;
	private JTextField txtSrcRootDirPath;
	private JButton btnSrcRootDirSelect;
	private JLabel lblFilePattern;
	private JPanel pnlFilePattern;
	private JTextField txtFilePattern;
	private JComboBox<FilePatternSyntax> cmbFilePatternSyntax;
	private JCheckBox chkContainsSubs;
	private JCheckBox chkContainsHiddens;
	private JLabel lblFileSizeRange;
	private JPanel pnlFileSizeRange;
	private JTextField txtFileSizeRangeFrom;
	private JLabel lblFileSizeRangeTo;
	private JTextField txtFileSizeRangeTo;
	private JComboBox<FileSizeUnit> cmbFileSizeUnit;
	private JLabel lblCreationTimeRange;
	private JPanel pnlCreationTimeRange;
	private JFormattedTextField txtCreationTimeRangeFrom;
	private JLabel lblCreationTimeRangeTo;
	private JFormattedTextField txtCreationTimeRangeTo;
	private JLabel lblModifiedTimeRange;
	private JPanel pnlModifiedTimeRange;
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
	private JPanel pnlChangeFileDate;
	private JLabel lblTargetDate;
	private JPanel pnlTargetDate;
	private JCheckBox chkChangeFileCreationDate;
	private JCheckBox chkChangeFileModifiedDate;
	private JCheckBox chkChangeFileAccessDate;
	private JCheckBox chkChangeExifDate;
	private JLabel lblBaseDateType;
	private JComboBox<DateType> cmbBaseDateType;
	private JPanel pnlBaseDate;
	private JFormattedTextField txtCustomBaseDate;
	private JLabel lblEditBaseDate;
	private JPanel pnlDateModType;
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

	private JPanel pnlModExif;
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
	private JMenu mnFile;
	private JMenu mnHelp;
	private JMenuItem mntmHelp;
	private JMenuItem mntmImportSettings;
	private JMenuItem mntmExportSettings;


	/**
	 * Create the frame.
	 */
	public MainFrame() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					storeSettings();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(
							null,
							Messages.getString("message.error.store.settings", e1.getLocalizedMessage()), //$NON-NLS-1$
							null,
							JOptionPane.ERROR_MESSAGE
							);

					App.handleError(e1.getMessage(), e1);
				}
			}
		});

		setTitle(Messages.getString("MainFrame.title")); //$NON-NLS-1$
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 700);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu(Messages.getString("MainFrame.menu.file")); //$NON-NLS-1$
		menuBar.add(mnFile);

		mntmImportSettings = new JMenuItem(Messages.getString("MainFrame.menu.file.importSettings")); //$NON-NLS-1$
		mntmImportSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("settings.ext.description"), SETTINGS_FILE_NAME_EXT)); //$NON-NLS-1$

				int selected = filechooser.showOpenDialog(frame);
				if (selected == JFileChooser.APPROVE_OPTION) {
					File file = filechooser.getSelectedFile();
					try {
						App.config().loadFrom(file.toPath());
						loadSettings();

						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.info.import.settings"), //$NON-NLS-1$
								null,
								JOptionPane.INFORMATION_MESSAGE
								);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.error.import.settings", e1.getLocalizedMessage()), //$NON-NLS-1$
								null,
								JOptionPane.ERROR_MESSAGE
								);

						App.handleError(e1.getMessage(), e1);
					}
				}
			}
		});
		mnFile.add(mntmImportSettings);

		mntmExportSettings = new JMenuItem(Messages.getString("MainFrame.menu.file.exportSettings")); //$NON-NLS-1$
		mntmExportSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser filechooser = new JFileChooser();
				filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				filechooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("settings.ext.description"), SETTINGS_FILE_NAME_EXT)); //$NON-NLS-1$

				int selected = filechooser.showSaveDialog(frame);
				if (selected == JFileChooser.APPROVE_OPTION) {
					File file = filechooser.getSelectedFile();

					if (!SETTINGS_FILE_NAME_EXT.equals(getExtension(file.getName()))) {
						file = new File(file.getParentFile(), file.getName() + "." + SETTINGS_FILE_NAME_EXT); //$NON-NLS-1$
					}

					try {
						App.config().storeTo(file.toPath(), ""); //$NON-NLS-1$

						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.info.export.settings"), //$NON-NLS-1$
								null,
								JOptionPane.INFORMATION_MESSAGE
								);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(
								null,
								Messages.getString("message.error.export.settings", e1.getLocalizedMessage()), //$NON-NLS-1$
								null,
								JOptionPane.ERROR_MESSAGE
								);

						App.handleError(e1.getMessage(), e1);
					}
				}
			}
		});
		mnFile.add(mntmExportSettings);

		mnHelp = new JMenu(Messages.getString("MainFrame.menu.help")); //$NON-NLS-1$
		menuBar.add(mnHelp);

		mntmHelp = new JMenuItem(Messages.getString("MainFrame.menu.help.help")); //$NON-NLS-1$
		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HelpDialog helpDialog = new HelpDialog();
				helpDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
				helpDialog.setLocationRelativeTo(frame);
				helpDialog.setVisible(true);
			}
		});
		mnHelp.add(mntmHelp);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING, WINDOW_PADDING));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{427, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

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

		JLabel lblSrcConditionsTitle = newMainLabel(Messages.getString("MainFrame.srcConditionsTitle")); //$NON-NLS-1$
		GridBagConstraints gbc_lblSrcConditionsTitle = new GridBagConstraints();
		gbc_lblSrcConditionsTitle.anchor = GridBagConstraints.WEST;
		gbc_lblSrcConditionsTitle.insets = new Insets(0, 0, 5, 8);
		gbc_lblSrcConditionsTitle.gridx = 0;
		gbc_lblSrcConditionsTitle.gridy = 0;
		pnlSrcConditions.add(lblSrcConditionsTitle, gbc_lblSrcConditionsTitle);

		JLabel lblSrcRootDirPath = new JLabel(UIManager.getIcon("FileView.directoryIcon")); //$NON-NLS-1$
		lblSrcRootDirPath.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.srcRootDirPath")); //$NON-NLS-1$
		lblSrcRootDirPath.setToolTipText(Messages.getString("MainFrame.srcRootDirPath")); //$NON-NLS-1$
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
		pnlSrcRootDirPath.setLayout(new BorderLayout(0, 0));

		btnSrcRootDirSelect = new JButton(Messages.getString("MainFrame.srcRootDirSelect")); //$NON-NLS-1$
		btnSrcOptions = newOptionsToggleButton(Messages.getString("MainFrame.srcOptionsTitle")); //$NON-NLS-1$
		JPanel pnlSrcRootDirActions = new JPanel(new BorderLayout(FIELD_BUTTON_GAP, 0));
		pnlSrcRootDirActions.add(btnSrcRootDirSelect, BorderLayout.CENTER);
		pnlSrcRootDirActions.add(btnSrcOptions, BorderLayout.EAST);
		pnlSrcRootDirPath.add(pnlSrcRootDirActions, BorderLayout.EAST);

		txtSrcRootDirPath = new JTextField();
		lblSrcRootDirPath.setLabelFor(txtSrcRootDirPath);
		txtSrcRootDirPath.setToolTipText(Messages.getString("MainFrame.srcRootDirPath")); //$NON-NLS-1$
		pnlSrcRootDirPath.add(txtSrcRootDirPath, BorderLayout.CENTER);
		txtSrcRootDirPath.setColumns(10);
		lblSrcRootDirPath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtSrcRootDirPath.requestFocusInWindow();
			}
		});
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
			setOptionsExpanded(btnSrcOptions, pnlSrcOptionsBody, Messages.getString("MainFrame.srcOptionsTitle"), false); //$NON-NLS-1$
			btnSrcOptions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setOptionsExpanded(btnSrcOptions, pnlSrcOptionsBody, Messages.getString("MainFrame.srcOptionsTitle"), btnSrcOptions.isSelected()); //$NON-NLS-1$
				}
			});

			txtSrcOptionsSummary = newOptionsSummaryText(btnSrcOptions, pnlSrcOptionsBody, Messages.getString("MainFrame.srcOptionsTitle")); //$NON-NLS-1$
			GridBagConstraints gbc_txtSrcOptionsSummary = new GridBagConstraints();
			gbc_txtSrcOptionsSummary.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtSrcOptionsSummary.gridwidth = 3;
			gbc_txtSrcOptionsSummary.insets = new Insets(0, 0, 5, 0);
			gbc_txtSrcOptionsSummary.gridx = 1;
			gbc_txtSrcOptionsSummary.gridy = 1;
			pnlSrcConditions.add(txtSrcOptionsSummary, gbc_txtSrcOptionsSummary);

		lblFilePattern = new JLabel(Messages.getString("MainFrame.filePattern")); //$NON-NLS-1$
		lblFilePattern.setToolTipText(Messages.getString("MainFrame.filePattern.tooltip")); //$NON-NLS-1$
			GridBagConstraints gbc_lblFilePattern = new GridBagConstraints();
			gbc_lblFilePattern.anchor = GridBagConstraints.WEST;
			gbc_lblFilePattern.insets = new Insets(0, 0, 5, 5);
			gbc_lblFilePattern.gridx = 0;
			gbc_lblFilePattern.gridy = 0;
			pnlSrcOptionsBody.add(lblFilePattern, gbc_lblFilePattern);

		pnlFilePattern = new JPanel();
		GridBagConstraints gbc_pnlFilePattern = new GridBagConstraints();
		gbc_pnlFilePattern.fill = GridBagConstraints.BOTH;
			gbc_pnlFilePattern.insets = new Insets(0, 0, 5, 0);
			gbc_pnlFilePattern.gridx = 1;
			gbc_pnlFilePattern.gridy = 0;
			pnlSrcOptionsBody.add(pnlFilePattern, gbc_pnlFilePattern);
		GridBagLayout gbl_pnlFilePattern = new GridBagLayout();
		gbl_pnlFilePattern.columnWidths = new int[]{0, 0, 0};
		gbl_pnlFilePattern.rowHeights = new int[]{0, 0};
		gbl_pnlFilePattern.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlFilePattern.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlFilePattern.setLayout(gbl_pnlFilePattern);

		txtFilePattern = new JTextField();
		lblFilePattern.setLabelFor(txtFilePattern);
		txtFilePattern.setToolTipText(Messages.getString("MainFrame.filePattern.tooltip")); //$NON-NLS-1$
		GridBagConstraints gbc_txtFilePattern = new GridBagConstraints();
		gbc_txtFilePattern.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFilePattern.insets = new Insets(0, 0, 0, 5);
		gbc_txtFilePattern.gridx = 0;
		gbc_txtFilePattern.gridy = 0;
		pnlFilePattern.add(txtFilePattern, gbc_txtFilePattern);
		txtFilePattern.setColumns(10);

		cmbFilePatternSyntax = new JComboBox<>();
		cmbFilePatternSyntax.setToolTipText(Messages.getString("MainFrame.filePattern.tooltip")); //$NON-NLS-1$
		cmbFilePatternSyntax.setModel(new DefaultComboBoxModel<>(FilePatternSyntax.values()));
		GridBagConstraints gbc_cmbFilePatternSyntax = new GridBagConstraints();
		gbc_cmbFilePatternSyntax.anchor = GridBagConstraints.WEST;
		gbc_cmbFilePatternSyntax.gridx = 1;
		gbc_cmbFilePatternSyntax.gridy = 0;
		pnlFilePattern.add(cmbFilePatternSyntax, gbc_cmbFilePatternSyntax);

		chkContainsSubs = new JCheckBox(Messages.getString("MainFrame.containsSubs")); //$NON-NLS-1$
		GridBagConstraints gbc_chkContainsSubs = new GridBagConstraints();
		gbc_chkContainsSubs.anchor = GridBagConstraints.WEST;
			gbc_chkContainsSubs.insets = new Insets(0, 0, 5, 0);
			gbc_chkContainsSubs.gridx = 1;
			gbc_chkContainsSubs.gridy = 1;
			pnlSrcOptionsBody.add(chkContainsSubs, gbc_chkContainsSubs);

		chkContainsHiddens = new JCheckBox(Messages.getString("MainFrame.containsHiddens")); //$NON-NLS-1$
		GridBagConstraints gbc_chkContainsHiddens = new GridBagConstraints();
		gbc_chkContainsHiddens.fill = GridBagConstraints.BOTH;
			gbc_chkContainsHiddens.insets = new Insets(0, 0, 5, 0);
			gbc_chkContainsHiddens.gridx = 1;
			gbc_chkContainsHiddens.gridy = 2;
			pnlSrcOptionsBody.add(chkContainsHiddens, gbc_chkContainsHiddens);

		lblFileSizeRange = new JLabel(Messages.getString("MainFrame.fileSizeRange")); //$NON-NLS-1$
		GridBagConstraints gbc_lblFileSizeRange = new GridBagConstraints();
		gbc_lblFileSizeRange.anchor = GridBagConstraints.WEST;
			gbc_lblFileSizeRange.insets = new Insets(0, 0, 5, 5);
			gbc_lblFileSizeRange.gridx = 0;
			gbc_lblFileSizeRange.gridy = 3;
			pnlSrcOptionsBody.add(lblFileSizeRange, gbc_lblFileSizeRange);

		pnlFileSizeRange = new JPanel();
		GridBagConstraints gbc_pnlFileSizeRange = new GridBagConstraints();
		gbc_pnlFileSizeRange.fill = GridBagConstraints.BOTH;
			gbc_pnlFileSizeRange.insets = new Insets(0, 0, 5, 0);
			gbc_pnlFileSizeRange.gridx = 1;
			gbc_pnlFileSizeRange.gridy = 3;
			pnlSrcOptionsBody.add(pnlFileSizeRange, gbc_pnlFileSizeRange);
		pnlFileSizeRange.setLayout(new FlowLayout(FlowLayout.LEFT, INLINE_HGAP, INLINE_VGAP));

		txtFileSizeRangeFrom = new JTextField();
		allowDigitsOnly(txtFileSizeRangeFrom);
		lblFileSizeRange.setLabelFor(txtFileSizeRangeFrom);
		txtFileSizeRangeFrom.setColumns(5);
		pnlFileSizeRange.add(txtFileSizeRangeFrom);

		lblFileSizeRangeTo = new JLabel(Messages.getString("MainFrame.fileSizeRangeTo")); //$NON-NLS-1$
		pnlFileSizeRange.add(lblFileSizeRangeTo);

		txtFileSizeRangeTo = new JTextField();
		allowDigitsOnly(txtFileSizeRangeTo);
		txtFileSizeRangeTo.setColumns(5);
		pnlFileSizeRange.add(txtFileSizeRangeTo);

		cmbFileSizeUnit = new JComboBox<>();
		pnlFileSizeRange.add(cmbFileSizeUnit);
		cmbFileSizeUnit.setModel(new DefaultComboBoxModel<>(FileSizeUnit.values()));

		lblCreationTimeRange = new JLabel(Messages.getString("MainFrame.creationTimeRange")); //$NON-NLS-1$
		GridBagConstraints gbc_lblCreationTimeRange = new GridBagConstraints();
		gbc_lblCreationTimeRange.anchor = GridBagConstraints.WEST;
			gbc_lblCreationTimeRange.insets = new Insets(0, 0, 5, 5);
			gbc_lblCreationTimeRange.gridx = 0;
			gbc_lblCreationTimeRange.gridy = 4;
			pnlSrcOptionsBody.add(lblCreationTimeRange, gbc_lblCreationTimeRange);

		pnlCreationTimeRange = new JPanel();
		GridBagConstraints gbc_pnlCreationTimeRange = new GridBagConstraints();
		gbc_pnlCreationTimeRange.fill = GridBagConstraints.BOTH;
			gbc_pnlCreationTimeRange.insets = new Insets(0, 0, 5, 0);
			gbc_pnlCreationTimeRange.gridx = 1;
			gbc_pnlCreationTimeRange.gridy = 4;
			pnlSrcOptionsBody.add(pnlCreationTimeRange, gbc_pnlCreationTimeRange);
		pnlCreationTimeRange.setLayout(new FlowLayout(FlowLayout.LEFT, INLINE_HGAP, INLINE_VGAP));

		txtCreationTimeRangeFrom = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		lblCreationTimeRange.setLabelFor(txtCreationTimeRangeFrom);
		txtCreationTimeRangeFrom.setColumns(20);
		txtCreationTimeRangeFrom.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtCreationTimeRangeFrom.setToolTipText(Messages.getString("MainFrame.creationTimeRangeFrom.tooltip")); //$NON-NLS-1$
		installDateTimeInputPopup(txtCreationTimeRangeFrom, false);
		txtCreationTimeRangeFrom.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlCreationTimeRange.add(txtCreationTimeRangeFrom);

		lblCreationTimeRangeTo = new JLabel(Messages.getString("MainFrame.creationTimeRangeTo")); //$NON-NLS-1$
		pnlCreationTimeRange.add(lblCreationTimeRangeTo);

		txtCreationTimeRangeTo = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		txtCreationTimeRangeTo.setColumns(20);
		txtCreationTimeRangeTo.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtCreationTimeRangeTo.setToolTipText(Messages.getString("MainFrame.creationTimeRangeTo.tooltip")); //$NON-NLS-1$
		installDateTimeInputPopup(txtCreationTimeRangeTo, true);
		txtCreationTimeRangeTo.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlCreationTimeRange.add(txtCreationTimeRangeTo);

		lblModifiedTimeRange = new JLabel(Messages.getString("MainFrame.modifiedTimeRange")); //$NON-NLS-1$
		GridBagConstraints gbc_lblModifiedTimeRange = new GridBagConstraints();
			gbc_lblModifiedTimeRange.anchor = GridBagConstraints.WEST;
			gbc_lblModifiedTimeRange.insets = new Insets(0, 0, 0, 5);
			gbc_lblModifiedTimeRange.gridx = 0;
			gbc_lblModifiedTimeRange.gridy = 5;
			pnlSrcOptionsBody.add(lblModifiedTimeRange, gbc_lblModifiedTimeRange);

		pnlModifiedTimeRange = new JPanel();
		GridBagConstraints gbc_pnlModifiedTimeRange = new GridBagConstraints();
			gbc_pnlModifiedTimeRange.fill = GridBagConstraints.BOTH;
			gbc_pnlModifiedTimeRange.gridx = 1;
			gbc_pnlModifiedTimeRange.gridy = 5;
			pnlSrcOptionsBody.add(pnlModifiedTimeRange, gbc_pnlModifiedTimeRange);
		pnlModifiedTimeRange.setLayout(new FlowLayout(FlowLayout.LEFT, INLINE_HGAP, INLINE_VGAP));

		txtModifiedTimeRangeFrom = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		lblModifiedTimeRange.setLabelFor(txtModifiedTimeRangeFrom);
		txtModifiedTimeRangeFrom.setColumns(20);
		txtModifiedTimeRangeFrom.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtModifiedTimeRangeFrom.setToolTipText(Messages.getString("MainFrame.modifiedTimeRangeFrom.tooltip")); //$NON-NLS-1$
		installDateTimeInputPopup(txtModifiedTimeRangeFrom, false);
		txtModifiedTimeRangeFrom.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlModifiedTimeRange.add(txtModifiedTimeRangeFrom);

		lblModifiedTimeRangeTo = new JLabel(Messages.getString("MainFrame.modifiedTimeRangeTo")); //$NON-NLS-1$
		pnlModifiedTimeRange.add(lblModifiedTimeRangeTo);

		txtModifiedTimeRangeTo = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		txtModifiedTimeRangeTo.setColumns(20);
		txtModifiedTimeRangeTo.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtModifiedTimeRangeTo.setToolTipText(Messages.getString("MainFrame.modifiedTimeRangeTo.tooltip")); //$NON-NLS-1$
		installDateTimeInputPopup(txtModifiedTimeRangeTo, true);
		txtModifiedTimeRangeTo.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlModifiedTimeRange.add(txtModifiedTimeRangeTo);

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

		rdoOpeTypeCopy = new JRadioButton(Messages.getString("MainFrame.opeTypeCopy")); //$NON-NLS-1$
		btngrpOpeType.add(rdoOpeTypeCopy);
		pnlOpeType.add(rdoOpeTypeCopy);

		rdoOpeTypeMove = new JRadioButton(Messages.getString("MainFrame.opeTypeMove")); //$NON-NLS-1$
		btngrpOpeType.add(rdoOpeTypeMove);
		pnlOpeType.add(rdoOpeTypeMove);

		rdoOpeTypeOverwrite = new JRadioButton(Messages.getString("MainFrame.opeTypeOverwrite")); //$NON-NLS-1$
		btngrpOpeType.add(rdoOpeTypeOverwrite);
		pnlOpeType.add(rdoOpeTypeOverwrite);

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

		lblDestConditionsTitle = newMainLabel(Messages.getString("MainFrame.destConditionsTitle")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDestConditionsTitle = new GridBagConstraints();
		gbc_lblDestConditionsTitle.anchor = GridBagConstraints.WEST;
		gbc_lblDestConditionsTitle.insets = new Insets(0, 0, 5, 8);
		gbc_lblDestConditionsTitle.gridx = 0;
		gbc_lblDestConditionsTitle.gridy = 0;
		pnlDestConditions.add(lblDestConditionsTitle, gbc_lblDestConditionsTitle);

		lblDestRootDirPath = new JLabel(UIManager.getIcon("FileView.directoryIcon")); //$NON-NLS-1$
		lblDestRootDirPath.getAccessibleContext().setAccessibleName(Messages.getString("MainFrame.destRootDirPath")); //$NON-NLS-1$
		lblDestRootDirPath.setToolTipText(Messages.getString("MainFrame.destRootDirPath")); //$NON-NLS-1$
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
		pnlDestRootDirPath.setLayout(new BorderLayout(0, 0));

		btnDestRootDirSelect = new JButton(Messages.getString("MainFrame.destRootDirSelect")); //$NON-NLS-1$
		btnDestOptions = newOptionsToggleButton(Messages.getString("MainFrame.destOptionsTitle")); //$NON-NLS-1$
		JPanel pnlDestRootDirActions = new JPanel(new BorderLayout(FIELD_BUTTON_GAP, 0));
		pnlDestRootDirActions.add(btnDestRootDirSelect, BorderLayout.CENTER);
		pnlDestRootDirActions.add(btnDestOptions, BorderLayout.EAST);
		pnlDestRootDirPath.add(pnlDestRootDirActions, BorderLayout.EAST);

		txtDestRootDirPath = new JTextField();
		lblDestRootDirPath.setLabelFor(txtDestRootDirPath);
		txtDestRootDirPath.setToolTipText(Messages.getString("MainFrame.destRootDirPath")); //$NON-NLS-1$
		pnlDestRootDirPath.add(txtDestRootDirPath, BorderLayout.CENTER);
		txtDestRootDirPath.setColumns(10);
		lblDestRootDirPath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				txtDestRootDirPath.requestFocusInWindow();
			}
		});
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
			setOptionsExpanded(btnDestOptions, pnlDestOptionsBody, Messages.getString("MainFrame.destOptionsTitle"), false); //$NON-NLS-1$
			btnDestOptions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setOptionsExpanded(btnDestOptions, pnlDestOptionsBody, Messages.getString("MainFrame.destOptionsTitle"), btnDestOptions.isSelected()); //$NON-NLS-1$
				}
			});

			txtDestOptionsSummary = newOptionsSummaryText(btnDestOptions, pnlDestOptionsBody, Messages.getString("MainFrame.destOptionsTitle")); //$NON-NLS-1$
			GridBagConstraints gbc_txtDestOptionsSummary = new GridBagConstraints();
			gbc_txtDestOptionsSummary.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtDestOptionsSummary.gridwidth = 3;
			gbc_txtDestOptionsSummary.insets = new Insets(0, 0, 5, 0);
			gbc_txtDestOptionsSummary.gridx = 1;
			gbc_txtDestOptionsSummary.gridy = 1;
			pnlDestConditions.add(txtDestOptionsSummary, gbc_txtDestOptionsSummary);

		lblDestSubPathPattern = new JLabel(Messages.getString("MainFrame.destSubPathPattern")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDestSubPathPattern = new GridBagConstraints();
		gbc_lblDestSubPathPattern.anchor = GridBagConstraints.WEST;
			gbc_lblDestSubPathPattern.insets = new Insets(0, 0, 5, 5);
			gbc_lblDestSubPathPattern.gridx = 0;
			gbc_lblDestSubPathPattern.gridy = 0;
			pnlDestOptionsBody.add(lblDestSubPathPattern, gbc_lblDestSubPathPattern);

		txtDestSubPathPattern = new JTextField();
		lblDestSubPathPattern.setLabelFor(txtDestSubPathPattern);
		txtDestSubPathPattern.setColumns(10);
		new SubfolderTemplatePopup(txtDestSubPathPattern);
		GridBagConstraints gbc_txtDestSubPathPattern = new GridBagConstraints();
			gbc_txtDestSubPathPattern.insets = new Insets(0, 0, 5, 0);
			gbc_txtDestSubPathPattern.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtDestSubPathPattern.gridx = 1;
			gbc_txtDestSubPathPattern.gridy = 0;
			pnlDestOptionsBody.add(txtDestSubPathPattern, gbc_txtDestSubPathPattern);

		lblExistingFileMethod = new JLabel(Messages.getString("MainFrame.existingFileMethod")); //$NON-NLS-1$
		GridBagConstraints gbc_lblExistingFileMethod = new GridBagConstraints();
		gbc_lblExistingFileMethod.anchor = GridBagConstraints.WEST;
			gbc_lblExistingFileMethod.insets = new Insets(0, 0, 5, 5);
			gbc_lblExistingFileMethod.gridx = 0;
			gbc_lblExistingFileMethod.gridy = 1;
			pnlDestOptionsBody.add(lblExistingFileMethod, gbc_lblExistingFileMethod);

		cmbExistingFileMethod = new JComboBox<>();
		lblExistingFileMethod.setLabelFor(cmbExistingFileMethod);
		GridBagConstraints gbc_cmbExistingFileMethod = new GridBagConstraints();
			gbc_cmbExistingFileMethod.insets = new Insets(0, 0, 5, 0);
			gbc_cmbExistingFileMethod.anchor = GridBagConstraints.WEST;
			gbc_cmbExistingFileMethod.gridx = 1;
			gbc_cmbExistingFileMethod.gridy = 1;
			cmbExistingFileMethod.setModel(new DefaultComboBoxModel<>(ExistingFileMethod.values()));
			pnlDestOptionsBody.add(cmbExistingFileMethod, gbc_cmbExistingFileMethod);

		lblValidateFile = new JLabel(Messages.getString("MainFrame.validateFile")); //$NON-NLS-1$
		GridBagConstraints gbc_lblValidateFile = new GridBagConstraints();
		gbc_lblValidateFile.anchor = GridBagConstraints.WEST;
			gbc_lblValidateFile.insets = new Insets(0, 0, 0, 5);
			gbc_lblValidateFile.gridx = 0;
			gbc_lblValidateFile.gridy = 2;
			pnlDestOptionsBody.add(lblValidateFile, gbc_lblValidateFile);

		chkCheckFileDigest = new JCheckBox(Messages.getString("MainFrame.checkFileDigest")); //$NON-NLS-1$
		lblValidateFile.setLabelFor(chkCheckFileDigest);
			GridBagConstraints gbc_chkCheckFileDigest = new GridBagConstraints();
			gbc_chkCheckFileDigest.anchor = GridBagConstraints.WEST;
			gbc_chkCheckFileDigest.gridx = 1;
			gbc_chkCheckFileDigest.gridy = 2;
			pnlDestOptionsBody.add(chkCheckFileDigest, gbc_chkCheckFileDigest);

		btnChanges = newOptionsToggleButton(Messages.getString("MainFrame.changesTitle")); //$NON-NLS-1$
		btnChanges.setFont(btnChanges.getFont().deriveFont(Font.BOLD, btnChanges.getFont().getSize2D() + 1.0f));
		GridBagConstraints gbc_btnChanges = new GridBagConstraints();
		gbc_btnChanges.anchor = GridBagConstraints.WEST;
		gbc_btnChanges.insets = new Insets(0, 0, SECTION_HEADER_GAP, 0);
		gbc_btnChanges.gridx = 0;
		gbc_btnChanges.gridy = 3;
		contentPane.add(btnChanges, gbc_btnChanges);

		tabModConditions = new JTabbedPane(JTabbedPane.TOP);
		tabModConditions.putClientProperty("JTabbedPane.tabType", "card"); //$NON-NLS-1$ //$NON-NLS-2$
		GridBagConstraints gbc_tabModConditions = new GridBagConstraints();
		gbc_tabModConditions.insets = new Insets(0, 0, SECTION_GAP, 0);
		gbc_tabModConditions.fill = GridBagConstraints.BOTH;
		gbc_tabModConditions.gridx = 0;
		gbc_tabModConditions.gridy = 5;
		contentPane.add(tabModConditions, gbc_tabModConditions);

		txtChangesSummary = newOptionsSummaryText(btnChanges, tabModConditions, Messages.getString("MainFrame.changesTitle")); //$NON-NLS-1$
		GridBagConstraints gbc_txtChangesSummary = new GridBagConstraints();
		gbc_txtChangesSummary.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtChangesSummary.insets = new Insets(0, MAIN_LABEL_WIDTH + 8, SECTION_GAP, 0);
		gbc_txtChangesSummary.gridx = 0;
		gbc_txtChangesSummary.gridy = 4;
		contentPane.add(txtChangesSummary, gbc_txtChangesSummary);

		pnlChangeFileDate = new JPanel();
		pnlChangeFileDate.setBorder(new EmptyBorder(SECTION_PADDING, SECTION_PADDING, SECTION_PADDING, SECTION_PADDING));
		tabModConditions.addTab(Messages.getString("MainFrame.changeFileDateTitle"), null, pnlChangeFileDate, null); //$NON-NLS-1$
		GridBagLayout gbl_pnlChangeFileDate = new GridBagLayout();
		gbl_pnlChangeFileDate.columnWidths = new int[]{0, 0, 0};
		gbl_pnlChangeFileDate.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlChangeFileDate.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlChangeFileDate.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlChangeFileDate.setLayout(gbl_pnlChangeFileDate);

		lblTargetDate = new JLabel(Messages.getString("MainFrame.changeFileTargetDate")); //$NON-NLS-1$
		GridBagConstraints gbc_lblTargetDate = new GridBagConstraints();
		gbc_lblTargetDate.anchor = GridBagConstraints.WEST;
		gbc_lblTargetDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblTargetDate.gridx = 0;
		gbc_lblTargetDate.gridy = 0;
		pnlChangeFileDate.add(lblTargetDate, gbc_lblTargetDate);

		pnlTargetDate = new JPanel();
		FlowLayout fl_pnlTargetDate = (FlowLayout) pnlTargetDate.getLayout();
		fl_pnlTargetDate.setVgap(INLINE_VGAP);
		fl_pnlTargetDate.setHgap(INLINE_HGAP);
		fl_pnlTargetDate.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_pnlTargetDate = new GridBagConstraints();
		gbc_pnlTargetDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlTargetDate.insets = new Insets(0, 0, 5, 0);
		gbc_pnlTargetDate.gridx = 1;
		gbc_pnlTargetDate.gridy = 0;
		pnlChangeFileDate.add(pnlTargetDate, gbc_pnlTargetDate);

		chkChangeFileCreationDate = new JCheckBox(Messages.getString("MainFrame.changeFileCreationDate")); //$NON-NLS-1$
		lblTargetDate.setLabelFor(chkChangeFileCreationDate);
		chkChangeFileCreationDate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableFileDateModConditions();
			}
		});
		pnlTargetDate.add(chkChangeFileCreationDate);

		chkChangeFileModifiedDate = new JCheckBox(Messages.getString("MainFrame.changeFileModifiedDate")); //$NON-NLS-1$
		chkChangeFileModifiedDate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableFileDateModConditions();
			}
		});
		pnlTargetDate.add(chkChangeFileModifiedDate);

		chkChangeFileAccessDate = new JCheckBox(Messages.getString("MainFrame.changeFileAccessDate")); //$NON-NLS-1$
		chkChangeFileAccessDate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableFileDateModConditions();
			}
		});
		pnlTargetDate.add(chkChangeFileAccessDate);

		chkChangeExifDate = new JCheckBox(Messages.getString("MainFrame.changeFileExifDate")); //$NON-NLS-1$
		chkChangeExifDate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeEnableFileDateModConditions();
			}
		});
		pnlTargetDate.add(chkChangeExifDate);

		lblBaseDateType = new JLabel(Messages.getString("MainFrame.changeFileBaseDateType")); //$NON-NLS-1$
		GridBagConstraints gbc_lblBaseDateType = new GridBagConstraints();
		gbc_lblBaseDateType.anchor = GridBagConstraints.WEST;
		gbc_lblBaseDateType.insets = new Insets(0, 0, 5, 5);
		gbc_lblBaseDateType.gridx = 0;
		gbc_lblBaseDateType.gridy = 1;
		pnlChangeFileDate.add(lblBaseDateType, gbc_lblBaseDateType);

		pnlBaseDate = new JPanel();
		GridBagConstraints gbc_pnlBaseDate = new GridBagConstraints();
		gbc_pnlBaseDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlBaseDate.insets = new Insets(0, 0, 5, 0);
		gbc_pnlBaseDate.gridx = 1;
		gbc_pnlBaseDate.gridy = 1;
		pnlChangeFileDate.add(pnlBaseDate, gbc_pnlBaseDate);
		pnlBaseDate.setLayout(new FlowLayout(FlowLayout.LEFT, INLINE_HGAP, INLINE_VGAP));

		cmbBaseDateType = new JComboBox<>();
		lblBaseDateType.setLabelFor(cmbBaseDateType);
		cmbBaseDateType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				DateType dateType = (DateType)e.getItem();
					if (dateType == DateType.CustomDate) {
						txtCustomBaseDate.setVisible(true);
					} else {
						txtCustomBaseDate.setVisible(false);
					}
					fitWindowToContent();
				}
			});
		pnlBaseDate.add(cmbBaseDateType);
		cmbBaseDateType.setModel(new DefaultComboBoxModel<>(DateType.values()));

		txtCustomBaseDate = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		configureDisabledBackground(txtCustomBaseDate);
		txtCustomBaseDate.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtCustomBaseDate.setColumns(20);
		txtCustomBaseDate.setVisible(false);
		installDateTimeInputPopup(txtCustomBaseDate, false);
		txtCustomBaseDate.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlBaseDate.add(txtCustomBaseDate);

		lblEditBaseDate = new JLabel(Messages.getString("MainFrame.changeFileEditBaseDate")); //$NON-NLS-1$
		GridBagConstraints gbc_lblEditBaseDate = new GridBagConstraints();
		gbc_lblEditBaseDate.anchor = GridBagConstraints.WEST;
		gbc_lblEditBaseDate.insets = new Insets(0, 0, 0, 5);
		gbc_lblEditBaseDate.gridx = 0;
		gbc_lblEditBaseDate.gridy = 2;
		pnlChangeFileDate.add(lblEditBaseDate, gbc_lblEditBaseDate);

		pnlDateModType = new JPanel();
		FlowLayout fl_pnlDateModType = (FlowLayout) pnlDateModType.getLayout();
		fl_pnlDateModType.setVgap(INLINE_VGAP);
		fl_pnlDateModType.setHgap(INLINE_HGAP);
		fl_pnlDateModType.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_pnlDateModType = new GridBagConstraints();
		gbc_pnlDateModType.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlDateModType.gridx = 1;
		gbc_pnlDateModType.gridy = 2;
		pnlChangeFileDate.add(pnlDateModType, gbc_pnlDateModType);

		cmbDateModType = new JComboBox<DateModType>();
		lblEditBaseDate.setLabelFor(cmbDateModType);
		cmbDateModType.setModel(new DefaultComboBoxModel<>(DateModType.values()));
		cmbDateModType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				changeEnableFileDateModConditions();
			}
		});
		pnlDateModType.add(cmbDateModType);

		txtDateModYears = new JTextField();
		allowDigitsOnly(txtDateModYears);
		configureDisabledBackground(txtDateModYears);
		pnlDateModType.add(txtDateModYears);
		txtDateModYears.setColumns(4);

		lblSepYM = new JLabel("/"); //$NON-NLS-1$
		pnlDateModType.add(lblSepYM);

		txtDateModMonths = new JTextField();
		allowDigitsOnly(txtDateModMonths);
		configureDisabledBackground(txtDateModMonths);
		pnlDateModType.add(txtDateModMonths);
		txtDateModMonths.setColumns(2);

		lblSepMD = new JLabel("/"); //$NON-NLS-1$
		pnlDateModType.add(lblSepMD);

		txtDateModDays = new JTextField();
		allowDigitsOnly(txtDateModDays);
		configureDisabledBackground(txtDateModDays);
		pnlDateModType.add(txtDateModDays);
		txtDateModDays.setColumns(2);

		lblSepDH = new JLabel(" "); //$NON-NLS-1$
		pnlDateModType.add(lblSepDH);

		txtDateModHours = new JTextField();
		allowDigitsOnly(txtDateModHours);
		configureDisabledBackground(txtDateModHours);
		pnlDateModType.add(txtDateModHours);
		txtDateModHours.setColumns(2);

		lblSepHM = new JLabel(":"); //$NON-NLS-1$
		pnlDateModType.add(lblSepHM);

		txtDateModMinutes = new JTextField();
		allowDigitsOnly(txtDateModMinutes);
		configureDisabledBackground(txtDateModMinutes);
		pnlDateModType.add(txtDateModMinutes);
		txtDateModMinutes.setColumns(2);

		lblSepMS = new JLabel(":"); //$NON-NLS-1$
		pnlDateModType.add(lblSepMS);

		txtDateModSeconds = new JTextField();
		allowDigitsOnly(txtDateModSeconds);
		configureDisabledBackground(txtDateModSeconds);
		pnlDateModType.add(txtDateModSeconds);
		txtDateModSeconds.setColumns(2);

		pnlModExif = new JPanel();
		pnlModExif.setBorder(new EmptyBorder(SECTION_PADDING, SECTION_PADDING, SECTION_PADDING, SECTION_PADDING));
		tabModConditions.addTab(Messages.getString("MainFrame.changeExifTitle"), null, pnlModExif, null); //$NON-NLS-1$
		GridBagLayout gbl_pnlModExif = new GridBagLayout();
		gbl_pnlModExif.columnWidths = new int[]{0, 0};
		gbl_pnlModExif.rowHeights = new int[]{0, 0, 0};
		gbl_pnlModExif.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_pnlModExif.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlModExif.setLayout(gbl_pnlModExif);

		chkRemoveExifTagsGps = new JCheckBox(Messages.getString("MainFrame.removeExifTagsGps")); //$NON-NLS-1$
		GridBagConstraints gbc_chkRemoveExifTagsGps = new GridBagConstraints();
		gbc_chkRemoveExifTagsGps.anchor = GridBagConstraints.WEST;
		gbc_chkRemoveExifTagsGps.insets = new Insets(0, 0, 5, 0);
		gbc_chkRemoveExifTagsGps.gridx = 0;
		gbc_chkRemoveExifTagsGps.gridy = 0;
		pnlModExif.add(chkRemoveExifTagsGps, gbc_chkRemoveExifTagsGps);

		chkRemoveExifTagsAll = new JCheckBox(Messages.getString("MainFrame.removeExifTagsAll")); //$NON-NLS-1$
		GridBagConstraints gbc_chkRemoveExifTagsAll = new GridBagConstraints();
		gbc_chkRemoveExifTagsAll.anchor = GridBagConstraints.WEST;
		gbc_chkRemoveExifTagsAll.gridx = 0;
		gbc_chkRemoveExifTagsAll.gridy = 1;
		pnlModExif.add(chkRemoveExifTagsAll, gbc_chkRemoveExifTagsAll);
		setOptionsExpanded(btnChanges, tabModConditions, Messages.getString("MainFrame.changesTitle"), false); //$NON-NLS-1$
		btnChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setOptionsExpanded(btnChanges, tabModConditions, Messages.getString("MainFrame.changesTitle"), btnChanges.isSelected()); //$NON-NLS-1$
			}
		});

		pnlControls = new JPanel();
		GridBagConstraints gbc_pnlControls = new GridBagConstraints();
		gbc_pnlControls.anchor = GridBagConstraints.SOUTH;
		gbc_pnlControls.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlControls.gridx = 0;
		gbc_pnlControls.gridy = 6;
		getContentPane().add(pnlControls, gbc_pnlControls);
		pnlControls.setLayout(new BorderLayout(0, INLINE_VGAP));

		btnStart = new JButton(Messages.getString("MainFrame.start")); //$NON-NLS-1$
		btnStart.putClientProperty("JButton.buttonType", "default"); //$NON-NLS-1$ //$NON-NLS-2$
		btnStart.setMargin(new Insets(7, 28, 7, 28));
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runProcess(false);
			}
		});

		btnStartMenu = new JButton("\u25be"); //$NON-NLS-1$
		btnStartMenu.setMargin(new Insets(7, 10, 7, 10));
		JPopupMenu runMenu = new JPopupMenu();
		JMenuItem mntmDryRun = new JMenuItem(Messages.getString("MainFrame.dryRun")); //$NON-NLS-1$
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
		pnlRunButton.add(btnStart);
		pnlRunButton.add(btnStartMenu);
		pnlControls.add(pnlRunButton, BorderLayout.CENTER);

		lblRunSummary = newRunSummaryLabel();
		installRunSummaryHover(btnStart);
		installRunSummaryHover(btnStartMenu);
		pnlControls.add(lblRunSummary, BorderLayout.SOUTH);

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

			installOptionsSummaryListeners();
			loadSettings();
			changeEnableDestConditions();
			changeEnableFileDateModConditions();
			updateOptionsSummaries();

			frame = this;
			windowLayoutReady = true;
			fitWindowToContent();
		}

	protected void loadSettings() {
		AppSettings conf = App.config();

		txtSrcRootDirPath.setText(conf.getString("src.root.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtFilePattern.setText(conf.getString("file.pattern", "")); //$NON-NLS-1$ //$NON-NLS-2$
		cmbFilePatternSyntax.setSelectedItem(FilePatternSyntax.of(conf.getBoolean("file.pattern.regex", false))); //$NON-NLS-1$
		chkContainsSubs.setSelected(conf.getBoolean("contains.subs", true)); //$NON-NLS-1$
		chkContainsHiddens.setSelected(conf.getBoolean("contains.hiddens", false)); //$NON-NLS-1$

		txtFileSizeRangeFrom.setText(conf.getString("file.size.range.from", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtFileSizeRangeTo.setText(conf.getString("file.size.range.to", "")); //$NON-NLS-1$ //$NON-NLS-2$
		cmbFileSizeUnit.setSelectedItem(conf.getEnum("file.size.unit", FileSizeUnit.class, FileSizeUnit.MB)); //$NON-NLS-1$
		txtCreationTimeRangeFrom.setText(conf.getString("creation.time.range.from", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtCreationTimeRangeTo.setText(conf.getString("creation.time.range.to", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtModifiedTimeRangeFrom.setText(conf.getString("modified.time.range.from", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtModifiedTimeRangeTo.setText(conf.getString("modified.time.range.to", "")); //$NON-NLS-1$ //$NON-NLS-2$

		rdoOpeTypeCopy.setSelected(conf.getBoolean("ope.type.copy", true)); //$NON-NLS-1$
		rdoOpeTypeMove.setSelected(conf.getBoolean("ope.type.move", false)); //$NON-NLS-1$
		rdoOpeTypeOverwrite.setSelected(conf.getBoolean("ope.type.overwrite", false)); //$NON-NLS-1$

		txtDestRootDirPath.setText(conf.getString("dest.root.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDestSubPathPattern.setText(conf.getString("dest.sub.path.pattern", DEFAULT_DEST_SUB_PATH_PATTERN)); //$NON-NLS-1$
		cmbExistingFileMethod.setSelectedItem(conf.getEnum("existing.file.method", ExistingFileMethod.class, ExistingFileMethod.Confirm)); //$NON-NLS-1$
		chkCheckFileDigest.setSelected(conf.getBoolean("check.file.digest", false)); //$NON-NLS-1$

		chkChangeFileCreationDate.setSelected(conf.getBoolean("change.file.creation.date", false)); //$NON-NLS-1$
		chkChangeFileModifiedDate.setSelected(conf.getBoolean("change.file.modified.date", false)); //$NON-NLS-1$
		chkChangeFileAccessDate.setSelected(conf.getBoolean("change.file.access.date", false)); //$NON-NLS-1$
		chkChangeExifDate.setSelected(conf.getBoolean("change.file.exif.date", false)); //$NON-NLS-1$
		cmbBaseDateType.setSelectedItem(conf.getEnum("base.date.type", DateType.class, DateType.FileModifiedDate)); //$NON-NLS-1$
		txtCustomBaseDate.setText(conf.getString("custom.base.date", "")); //$NON-NLS-1$ //$NON-NLS-2$
		cmbDateModType.setSelectedItem(conf.getEnum("date.mod.type", DateModType.class, DateModType.None)); //$NON-NLS-1$
		txtDateModYears.setText(conf.getString("date.mod.year", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModMonths.setText(conf.getString("date.mod.month", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModDays.setText(conf.getString("date.mod.day", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModHours.setText(conf.getString("date.mod.hour", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModMinutes.setText(conf.getString("date.mod.minute", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModSeconds.setText(conf.getString("date.mod.second", "")); //$NON-NLS-1$ //$NON-NLS-2$
		chkRemoveExifTagsGps.setSelected(conf.getBoolean("remove.exif.tags.gps", false)); //$NON-NLS-1$
		chkRemoveExifTagsAll.setSelected(conf.getBoolean("remove.exif.tags.all", false)); //$NON-NLS-1$

	}

	protected void storeSettings() throws IOException {
		AppSettings conf = App.config();

		conf.set("src.root.dir", txtSrcRootDirPath.getText()); //$NON-NLS-1$
		conf.set("file.pattern", txtFilePattern.getText()); //$NON-NLS-1$
		conf.set("file.pattern.regex", getSelectedFilePatternSyntax().isRegex()); //$NON-NLS-1$
		conf.set("contains.subs", chkContainsSubs.isSelected()); //$NON-NLS-1$
		conf.set("contains.hiddens", chkContainsHiddens.isSelected()); //$NON-NLS-1$

		conf.set("file.size.range.from", txtFileSizeRangeFrom.getText()); //$NON-NLS-1$
		conf.set("file.size.range.to", txtFileSizeRangeTo.getText()); //$NON-NLS-1$
		conf.set("file.size.unit", cmbFileSizeUnit.getSelectedItem()); //$NON-NLS-1$
		conf.set("creation.time.range.from", txtCreationTimeRangeFrom.getText()); //$NON-NLS-1$
		conf.set("creation.time.range.to", txtCreationTimeRangeTo.getText()); //$NON-NLS-1$
		conf.set("modified.time.range.from", txtModifiedTimeRangeFrom.getText()); //$NON-NLS-1$
		conf.set("modified.time.range.to", txtModifiedTimeRangeTo.getText()); //$NON-NLS-1$

		conf.set("ope.type.copy", rdoOpeTypeCopy.isSelected()); //$NON-NLS-1$
		conf.set("ope.type.move", rdoOpeTypeMove.isSelected()); //$NON-NLS-1$
		conf.set("ope.type.overwrite", rdoOpeTypeOverwrite.isSelected()); //$NON-NLS-1$

		conf.set("dest.root.dir", txtDestRootDirPath.getText()); //$NON-NLS-1$
		conf.set("dest.sub.path.pattern", txtDestSubPathPattern.getText()); //$NON-NLS-1$
		conf.set("existing.file.method", cmbExistingFileMethod.getSelectedItem()); //$NON-NLS-1$
		conf.set("check.file.digest", chkCheckFileDigest.isSelected()); //$NON-NLS-1$

		conf.set("change.file.creation.date", chkChangeFileCreationDate.isSelected()); //$NON-NLS-1$
		conf.set("change.file.modified.date", chkChangeFileModifiedDate.isSelected()); //$NON-NLS-1$
		conf.set("change.file.access.date", chkChangeFileAccessDate.isSelected()); //$NON-NLS-1$
		conf.set("change.file.exif.date", chkChangeExifDate.isSelected()); //$NON-NLS-1$
		conf.set("base.date.type", cmbBaseDateType.getSelectedItem()); //$NON-NLS-1$
		conf.set("custom.base.date", txtCustomBaseDate.getText()); //$NON-NLS-1$
		conf.set("date.mod.type", cmbDateModType.getSelectedItem()); //$NON-NLS-1$
		conf.set("date.mod.year", txtDateModYears.getText()); //$NON-NLS-1$
		conf.set("date.mod.month", txtDateModMonths.getText()); //$NON-NLS-1$
		conf.set("date.mod.day", txtDateModDays.getText()); //$NON-NLS-1$
		conf.set("date.mod.hour", txtDateModHours.getText()); //$NON-NLS-1$
		conf.set("date.mod.minute", txtDateModMinutes.getText()); //$NON-NLS-1$
		conf.set("date.mod.second", txtDateModSeconds.getText()); //$NON-NLS-1$
		conf.set("remove.exif.tags.gps", chkRemoveExifTagsGps.isSelected()); //$NON-NLS-1$
		conf.set("remove.exif.tags.all", chkRemoveExifTagsAll.isSelected()); //$NON-NLS-1$

		conf.store(null);
		App.deleteMigratedLegacySettingsIfNeeded();
	}

	private JLabel newMainLabel(String title) {
		JLabel label = new JLabel(title);
		label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D() + 1.0f));
		Dimension size = new Dimension(MAIN_LABEL_WIDTH, label.getPreferredSize().height);
		label.setMinimumSize(size);
		label.setPreferredSize(size);
		return label;
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
		Color foreground = UIManager.getColor("Label.disabledForeground"); //$NON-NLS-1$
		if (foreground == null) {
			foreground = UIManager.getColor("Label.foreground"); //$NON-NLS-1$
		}
		summary.setForeground(foreground);
		return summary;
	}

	private JLabel newRunSummaryLabel() {
		JLabel label = new JLabel(" "); //$NON-NLS-1$
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(label.getFont().getSize2D() - 1.0f));
		Color foreground = UIManager.getColor("Label.disabledForeground"); //$NON-NLS-1$
		if (foreground == null) {
			foreground = UIManager.getColor("Label.foreground"); //$NON-NLS-1$
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
		lblRunSummary.setText(abbreviateMiddle(runSummary(), lblRunSummary.getWidth() - 8, lblRunSummary));
	}

	private void clearRunSummary() {
		lblRunSummary.setText(" "); //$NON-NLS-1$
	}

	private String runSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		String summary = values.operationType + ": " //$NON-NLS-1$
				+ Messages.getString("MainFrame.srcConditionsTitle") + " " //$NON-NLS-1$ //$NON-NLS-2$
				+ shortPathText(fieldText(txtSrcRootDirPath), Messages.getString("MainFrame.srcRootDirPath")); //$NON-NLS-1$
		if (values.operationType == OperationType.Overwrite) {
			return summary;
		}
		return summary + " -> " //$NON-NLS-1$
				+ Messages.getString("MainFrame.destConditionsTitle") + " " //$NON-NLS-1$ //$NON-NLS-2$
				+ shortPathText(fieldText(txtDestRootDirPath), Messages.getString("MainFrame.destRootDirPath")); //$NON-NLS-1$
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
				pattern += " (" + FilePatternSyntax.REGEX + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			items.add(summaryItem(Messages.getString("MainFrame.filePattern"), pattern)); //$NON-NLS-1$
		}
		if (values.dept != 1) {
			items.add(Messages.getString("MainFrame.containsSubs")); //$NON-NLS-1$
		}
		if (values.containsHiddens) {
			items.add(Messages.getString("MainFrame.containsHiddens")); //$NON-NLS-1$
		}
		if (values.sizeRangeFrom != null || values.sizeRangeTo != null) {
			String range = rangeText(fieldText(txtFileSizeRangeFrom), fieldText(txtFileSizeRangeTo));
			items.add(summaryItem(Messages.getString("MainFrame.fileSizeRange"), range + " " + cmbFileSizeUnit.getSelectedItem())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (values.creationTimeRangeFrom != null || values.creationTimeRangeTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.creationTimeRange"), //$NON-NLS-1$
					rangeText(dateFieldText(txtCreationTimeRangeFrom), dateFieldText(txtCreationTimeRangeTo))));
		}
		if (values.modifiedTimeRangeFrom != null || values.modifiedTimeRangeTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.modifiedTimeRange"), //$NON-NLS-1$
					rangeText(dateFieldText(txtModifiedTimeRangeFrom), dateFieldText(txtModifiedTimeRangeTo))));
		}
		return joinOptionsSummary(items);
	}

	private String destinationOptionsSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		List<String> items = new ArrayList<>();
		if (!values.destSubPathPattern.isBlank() && !DEFAULT_DEST_SUB_PATH_PATTERN.equals(values.destSubPathPattern)) {
			items.add(summaryItem(Messages.getString("MainFrame.destSubPathPattern"), values.destSubPathPattern)); //$NON-NLS-1$
		}
		if (values.existingFileMethod != ExistingFileMethod.Confirm) {
			items.add(summaryItem(Messages.getString("MainFrame.existingFileMethod"), String.valueOf(values.existingFileMethod))); //$NON-NLS-1$
		}
		if (values.checkDigest) {
			items.add(Messages.getString("MainFrame.validateFile")); //$NON-NLS-1$
		}
		return joinOptionsSummary(items);
	}

	private String changesSummary() {
		ProcessConditionValues values = collectProcessConditionValues();
		List<String> items = new ArrayList<>();
		boolean changesFileDate = false;
		if (values.changeFileCreationDate) {
			items.add(Messages.getString("MainFrame.changeFileCreationDate")); //$NON-NLS-1$
			changesFileDate = true;
		}
		if (values.changeFileModifiedDate) {
			items.add(Messages.getString("MainFrame.changeFileModifiedDate")); //$NON-NLS-1$
			changesFileDate = true;
		}
		if (values.changeFileAccessDate) {
			items.add(Messages.getString("MainFrame.changeFileAccessDate")); //$NON-NLS-1$
			changesFileDate = true;
		}
		if (values.changeExifDate) {
			items.add(Messages.getString("MainFrame.changeFileExifDate")); //$NON-NLS-1$
			changesFileDate = true;
		}
		if (changesFileDate && values.baseDateType != DateType.FileModifiedDate) {
			items.add(summaryItem(Messages.getString("MainFrame.changeFileBaseDateType"), baseDateTypeSummary(values))); //$NON-NLS-1$
		}
		if (changesFileDate && values.baseDateModType != DateModType.None) {
			items.add(summaryItem(Messages.getString("MainFrame.changeFileEditBaseDate"), adjustmentSummary(values))); //$NON-NLS-1$
		}
		if (values.removeExifTagsGps) {
			items.add(Messages.getString("MainFrame.removeExifTagsGps")); //$NON-NLS-1$
		}
		if (values.removeExifTagsAll) {
			items.add(Messages.getString("MainFrame.removeExifTagsAll")); //$NON-NLS-1$
		}
		return joinOptionsSummary(items);
	}

	private static boolean hasText(JTextField field) {
		return !fieldText(field).isBlank();
	}

	private static boolean hasDateText(JTextField field) {
		String text = field.getText();
		return text != null && containsDigit(text);
	}

	private static String fieldText(JTextField field) {
		String text = field.getText();
		return text == null ? "" : text.trim(); //$NON-NLS-1$
	}

	private static String dateFieldText(JTextField field) {
		if (!hasDateText(field)) {
			return ""; //$NON-NLS-1$
		}
		String[] dateTimeParts = fieldText(field).split(" ", -1); //$NON-NLS-1$
		String date = compactMaskedParts(dateTimeParts.length > 0 ? dateTimeParts[0] : "", "/", "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String time = compactMaskedParts(dateTimeParts.length > 1 ? dateTimeParts[1] : "", ":", ":"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (date.isEmpty()) {
			return time;
		}
		if (time.isEmpty()) {
			return date;
		}
		return date + " " + time; //$NON-NLS-1$
	}

	private static String compactMaskedParts(String text, String regexDelimiter, String displayDelimiter) {
		List<String> parts = new ArrayList<>();
		for (String part : text.split(regexDelimiter, -1)) {
			if (containsDigit(part)) {
				parts.add(part);
			}
		}
		return String.join(displayDelimiter, parts);
	}

	private String baseDateTypeSummary(ProcessConditionValues values) {
		String value = String.valueOf(values.baseDateType);
		String customDate = dateFieldText(txtCustomBaseDate);
		if (values.baseDateType == DateType.CustomDate && values.customBaseDate != null && !customDate.isEmpty()) {
			value += " " + customDate; //$NON-NLS-1$
		}
		return value;
	}

	private String adjustmentSummary(ProcessConditionValues values) {
		String value = String.valueOf(values.baseDateModType);
		List<String> amounts = new ArrayList<>();
		addAdjustmentAmount(amounts, values.baseDateModYears, "Y"); //$NON-NLS-1$
		addAdjustmentAmount(amounts, values.baseDateModMonths, "M"); //$NON-NLS-1$
		addAdjustmentAmount(amounts, values.baseDateModDays, "D"); //$NON-NLS-1$
		addAdjustmentAmount(amounts, values.baseDateModHours, "h"); //$NON-NLS-1$
		addAdjustmentAmount(amounts, values.baseDateModMinutes, "m"); //$NON-NLS-1$
		addAdjustmentAmount(amounts, values.baseDateModSeconds, "s"); //$NON-NLS-1$
		if (!amounts.isEmpty()) {
			value += " " + String.join(" ", amounts); //$NON-NLS-1$ //$NON-NLS-2$
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
		return from + " - " + to; //$NON-NLS-1$
	}

	private static String summaryItem(String label, String value) {
		return label + ": " + value; //$NON-NLS-1$
	}

	private static String joinOptionsSummary(List<String> items) {
		return String.join(" / ", items); //$NON-NLS-1$
	}

	private static String shortPathText(String path, String emptyText) {
		String text = path.isEmpty() ? emptyText : path;
		return shortenPath(text);
	}

	private static String shortenPath(String path) {
		String text = path.strip();
		while (text.length() > 1 && isPathSeparator(text.charAt(text.length() - 1))) {
			text = text.substring(0, text.length() - 1);
		}
		char separator = text.indexOf('\\') >= 0 && text.indexOf('/') < 0 ? '\\' : '/';
		List<String> parts = pathParts(text);
		if (parts.size() <= 2) {
			return text;
		}

		String root = pathRoot(text, separator, parts);
		int prefixCount = root.startsWith("\\\\") ? 0 : Math.min(parts.size() - 1, root.isEmpty() ? 1 : 2); //$NON-NLS-1$
		if (parts.size() <= prefixCount + 1) {
			return text;
		}

		StringBuilder shortened = new StringBuilder(root);
		for (int i = 0; i < prefixCount; i++) {
			if (shortened.length() > 0 && shortened.charAt(shortened.length() - 1) != separator) {
				shortened.append(separator);
			}
			shortened.append(parts.get(i));
		}
		if (shortened.length() > 0 && shortened.charAt(shortened.length() - 1) != separator) {
			shortened.append(separator);
		}
		shortened.append("...").append(separator).append(parts.get(parts.size() - 1)); //$NON-NLS-1$
		return shortened.toString();
	}

	private static List<String> pathParts(String path) {
		List<String> parts = new ArrayList<>();
		int start = 0;
		for (int i = 0; i <= path.length(); i++) {
			if (i == path.length() || isPathSeparator(path.charAt(i))) {
				if (i > start) {
					parts.add(path.substring(start, i));
				}
				start = i + 1;
			}
		}
		if (!parts.isEmpty() && parts.get(0).endsWith(":")) { //$NON-NLS-1$
			parts.remove(0);
		}
		return parts;
	}

	private static String pathRoot(String path, char separator, List<String> parts) {
		if (path.length() >= 3 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':' && isPathSeparator(path.charAt(2))) {
			return path.substring(0, 2) + separator;
		}
		if (path.startsWith("\\\\") && parts.size() >= 2) { //$NON-NLS-1$
			return "\\\\" + parts.get(0) + separator + parts.get(1) + separator; //$NON-NLS-1$
		}
		if (!path.isEmpty() && isPathSeparator(path.charAt(0))) {
			return String.valueOf(separator);
		}
		return ""; //$NON-NLS-1$
	}

	private static boolean isPathSeparator(char c) {
		return c == '/' || c == '\\' || c == File.separatorChar;
	}

	private static String abbreviateMiddle(String text, int width, JLabel label) {
		if (text == null || text.isEmpty() || width <= 0 || label.getFontMetrics(label.getFont()).stringWidth(text) <= width) {
			return text;
		}
		String ellipsis = "..."; //$NON-NLS-1$
		int ellipsisWidth = label.getFontMetrics(label.getFont()).stringWidth(ellipsis);
		if (ellipsisWidth >= width) {
			return ellipsis;
		}
		int left = text.length() / 2;
		int right = text.length() - left;
		while (left > 0 && right > 0) {
			String abbreviated = text.substring(0, left) + ellipsis + text.substring(text.length() - right);
			if (label.getFontMetrics(label.getFont()).stringWidth(abbreviated) <= width) {
				return abbreviated;
			}
			if (left >= right) {
				left--;
			} else {
				right--;
			}
		}
		return ellipsis;
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
		button.setText((expanded ? "\u25be " : "\u25b8 ") + title); //$NON-NLS-1$ //$NON-NLS-2$
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
		setTextFieldEnabled(txtCustomBaseDate, enabled);
		lblEditBaseDate.setEnabled(enabled);
		cmbDateModType.setEnabled(enabled);
		
		boolean adjustmentEnabled = enabled && cmbDateModType.getSelectedItem() != DateModType.None;
		setTextFieldEnabled(txtDateModYears, adjustmentEnabled);
		lblSepYM.setEnabled(adjustmentEnabled);
		setTextFieldEnabled(txtDateModMonths, adjustmentEnabled);
		lblSepMD.setEnabled(adjustmentEnabled);
		setTextFieldEnabled(txtDateModDays, adjustmentEnabled);
		lblSepDH.setEnabled(adjustmentEnabled);
		setTextFieldEnabled(txtDateModHours, adjustmentEnabled);
		lblSepHM.setEnabled(adjustmentEnabled);
		setTextFieldEnabled(txtDateModMinutes, adjustmentEnabled);
		lblSepMS.setEnabled(adjustmentEnabled);
		setTextFieldEnabled(txtDateModSeconds, adjustmentEnabled);
	}
	
	private static void configureDisabledBackground(JTextField textField) {
		Color enabledBackground = textField.getBackground();
		Color disabledBackground = UIManager.getColor(textField instanceof JFormattedTextField
				? "FormattedTextField.disabledBackground" //$NON-NLS-1$
				: "TextField.disabledBackground"); //$NON-NLS-1$
		if (disabledBackground == null) {
			disabledBackground = UIManager.getColor("TextComponent.disabledBackground"); //$NON-NLS-1$
		}
		textField.putClientProperty(CLIENT_PROPERTY_ENABLED_BACKGROUND, enabledBackground);
		textField.putClientProperty(CLIENT_PROPERTY_DISABLED_BACKGROUND, disabledBackground);
	}
	
	private static void setTextFieldEnabled(JTextField textField, boolean enabled) {
		textField.setEnabled(enabled);
		String backgroundProperty = enabled
				? CLIENT_PROPERTY_ENABLED_BACKGROUND
				: CLIENT_PROPERTY_DISABLED_BACKGROUND;
		Object background = textField.getClientProperty(backgroundProperty);
		if (background instanceof Color) {
			textField.setBackground((Color)background);
		}
	}
	
	private void installDateTimeInputPopup(JFormattedTextField field, boolean endOfRange) {
		new DateTimeInputPopup(field, endOfRange, Locale.getDefault());
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
		processDialog.doProcess(processCondition);
		processDialog.setVisible(true);
	}

	private ProcessConditionValues collectProcessConditionValues() {
		ProcessConditionValues values = new ProcessConditionValues();

		values.srcRootDirPath = Paths.get(txtSrcRootDirPath.getText()).normalize();
		values.filePattern = fieldText(txtFilePattern);
		values.filePatternRegex = getSelectedFilePatternSyntax().isRegex();
		values.containsHiddens = chkContainsHiddens.isEnabled() && chkContainsHiddens.isSelected();
		values.followLinks = false;
		values.dept = (chkContainsSubs.isEnabled() && chkContainsSubs.isSelected()) ? Integer.MAX_VALUE : 1;

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
		values.customBaseDate = parseDate(txtCustomBaseDate.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
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

		values.creationTimeRangeFrom = parseDate(txtCreationTimeRangeFrom.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.creationTimeRangeTo = parseDate(txtCreationTimeRangeTo.getText(), timeZone, Year.now().getValue(), 12, 31, 23, 59, 59, 999);
		values.modifiedTimeRangeFrom = parseDate(txtModifiedTimeRangeFrom.getText(), timeZone, Year.now().getValue(), 1, 1, 0, 0, 0, 0);
		values.modifiedTimeRangeTo = parseDate(txtModifiedTimeRangeTo.getText(), timeZone, Year.now().getValue(), 12, 31, 23, 59, 59, 999);

		return values;
	}

	private ProcessCondition createProcessCondition(boolean dryRun) {

		ProcessConditionValues values = collectProcessConditionValues();

		// Validations
		if (!Files.exists(values.srcRootDirPath)) {
			JOptionPane.showMessageDialog(
					frame,
					Messages.getString("message.warn.srcRootPath.not.exists"), //$NON-NLS-1$
					null,
					JOptionPane.WARNING_MESSAGE
					);
			return null;
		}

		try {
			FileSystems.getDefault().getPathMatcher(((values.filePatternRegex) ? "regex:" : "glob:") + values.filePattern); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					frame,
					Messages.getString("message.warn.invalid.filePattern", e.getLocalizedMessage()), //$NON-NLS-1$
					null,
					JOptionPane.WARNING_MESSAGE
					);
			return null;
		}

		if (values.sizeRangeFrom != null && values.sizeRangeTo != null) {
			if (values.sizeRangeFrom.longValue() > values.sizeRangeTo.longValue()) {
				JOptionPane.showMessageDialog(
						frame,
						Messages.getString("message.warn.sizeRange.is.invalid.range"), //$NON-NLS-1$
						null,
						JOptionPane.WARNING_MESSAGE
						);
				return null;
			}
		}

		if (values.creationTimeRangeFrom != null && values.creationTimeRangeTo != null) {
			if (values.creationTimeRangeFrom.compareTo(values.creationTimeRangeTo) > 0) {
				JOptionPane.showMessageDialog(
						frame,
						Messages.getString("message.warn.creationTimeRange.is.invalid.range"), //$NON-NLS-1$
						null,
						JOptionPane.WARNING_MESSAGE
						);
				return null;
			}
		}

		if (values.modifiedTimeRangeFrom != null && values.modifiedTimeRangeTo != null) {
			if (values.modifiedTimeRangeFrom.compareTo(values.modifiedTimeRangeTo) > 0) {
				JOptionPane.showMessageDialog(
						frame,
						Messages.getString("message.warn.modifiedTimeRange.is.invalid.range"), //$NON-NLS-1$
						null,
						JOptionPane.WARNING_MESSAGE
						);
				return null;
			}
		}

		// Information
		if (values.checkDigest && (values.changeExifDate || values.removeExifTagsGps || values.removeExifTagsAll)) {
			int ret = JOptionPane.showConfirmDialog(
					frame,
					Messages.getString("message.confirm.change.file.with.checkFileDigest"), //$NON-NLS-1$
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
		processCondition.setDept(values.dept);
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
		processCondition.setRemveExifTagsGps(values.removeExifTagsGps);
		processCondition.setRemveExifTagsAll(values.removeExifTagsAll);
		processCondition.setDryRun(dryRun);

		return processCondition;
	}

	private static class ProcessConditionValues {
		private Path srcRootDirPath;
		private String filePattern;
		private boolean filePatternRegex;
		private boolean containsHiddens;
		private boolean followLinks;
		private int dept;
		private OperationType operationType;
		private Path destRootDirPath;
		private String destSubPathPattern;
		private ExistingFileMethod existingFileMethod;
		private boolean checkDigest;
		private boolean changeFileCreationDate;
		private boolean changeFileModifiedDate;
		private boolean changeFileAccessDate;
		private boolean changeExifDate;
		private DateType baseDateType;
		private Date customBaseDate;
		private DateModType baseDateModType;
		private Integer baseDateModYears;
		private Integer baseDateModMonths;
		private Integer baseDateModDays;
		private Integer baseDateModHours;
		private Integer baseDateModMinutes;
		private Integer baseDateModSeconds;
		private boolean removeExifTagsGps;
		private boolean removeExifTagsAll;
		private Long sizeRangeFrom;
		private Long sizeRangeTo;
		private Date creationTimeRangeFrom;
		private Date creationTimeRangeTo;
		private Date modifiedTimeRangeFrom;
		private Date modifiedTimeRangeTo;
	}

	private static void allowDigitsOnly(JTextField textField) {
		if (textField.getDocument() instanceof AbstractDocument) {
			((AbstractDocument)textField.getDocument()).setDocumentFilter(new DocumentFilter() {
				@Override
				public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
					if (isDigits(string)) {
						super.insertString(fb, offset, string, attr);
					}
				}
				
				@Override
				public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
					if (isDigits(text)) {
						super.replace(fb, offset, length, text, attrs);
					}
				}
			});
		}
	}
	
	private static boolean isDigits(String text) {
		if (text == null) {
			return true;
		}
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isDigit(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static MaskFormatter newMaskFormatter(String mask) {
		try {
			MaskFormatter maskFormatter = new MaskFormatter(mask);
			maskFormatter.setPlaceholderCharacter(DATE_MASK_PLACEHOLDER_CHAR);
			maskFormatter.setValidCharacters(DATE_MASK_VALID_CHARS);
			return maskFormatter;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private static Long convertSize(Long size, FileSizeUnit unit) {
		if (size == null) {
			return null;
		}
		return size * unit.getUnitBytes();
	}

	private static String getExtension(String filename) {
		int separator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
		int extensionSeparator = filename.lastIndexOf('.');
		if (extensionSeparator <= separator || extensionSeparator < 1 || extensionSeparator == filename.length() - 1) {
			return ""; //$NON-NLS-1$
		}
		return filename.substring(extensionSeparator + 1);
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

	private static Date parseDate(String text, TimeZone timeZone, int defaultYear, int defaultMonth, int defaultDay, int defaultHour, int defaultMin, int defaultSec, int defaultMsec) {
		if (text == null || DATE_NASK_DEFAULT_VALUE.equals(text) || !containsDigit(text)) {
			return null;
		}

		String[] dateTimeParts = text.split(" ", 2); //$NON-NLS-1$
		String dateText = dateTimeParts.length > 0 ? dateTimeParts[0] : ""; //$NON-NLS-1$
		if (!containsDigit(dateText)) {
			return null;
		}
		String timeText = dateTimeParts.length > 1 ? dateTimeParts[1] : ""; //$NON-NLS-1$
		String[] dateParts = dateText.split("/", -1); //$NON-NLS-1$
		String[] timeParts = timeText.split(":", -1); //$NON-NLS-1$

		int year = getNumber(dateParts, 0, defaultYear);
		int month = normalize(getNumber(dateParts, 1, defaultMonth), 1, 12);
		int day = getNumber(dateParts, 2, defaultDay);
		int hour = normalize(getNumber(timeParts, 0, defaultHour), 0, 23);
		int min = normalize(getNumber(timeParts, 1, defaultMin), 0, 59);
		int sec = normalize(getNumber(timeParts, 2, defaultSec), 0, 59);

		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTimeInMillis(defaultMsec);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, Math.min(day, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		return cal.getTime();
	}

	private static boolean containsDigit(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (Character.isDigit(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private static int getNumber(String[] values, int index, int defaultValue) {
		if (index >= values.length) {
			return defaultValue;
		}
		String value = values[index].replace(DATE_MASK_PLACEHOLDER, ""); //$NON-NLS-1$
		if (value.isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	private static int normalize(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
	
	private FilePatternSyntax getSelectedFilePatternSyntax() {
		Object selectedItem = cmbFilePatternSyntax.getSelectedItem();
		return selectedItem instanceof FilePatternSyntax ? (FilePatternSyntax)selectedItem : FilePatternSyntax.GLOB;
	}
	
	private enum FilePatternSyntax {
		GLOB(false, "MainFrame.filePatternSyntax.glob"), //$NON-NLS-1$
		REGEX(true, "MainFrame.filePatternSyntax.regex"); //$NON-NLS-1$
		
		private final boolean regex;
		private final String labelKey;
		
		FilePatternSyntax(boolean regex, String labelKey) {
			this.regex = regex;
			this.labelKey = labelKey;
		}
		
		boolean isRegex() {
			return regex;
		}
		
		static FilePatternSyntax of(boolean regex) {
			return regex ? REGEX : GLOB;
		}
		
		@Override
		public String toString() {
			return Messages.getString(labelKey);
		}
	}
}
