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
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.MaskFormatter;

import org.mifmi.commons4j.io.file.FileUtilz;
import org.mifmi.commons4j.swing.WindowUtilz;
import org.mifmi.commons4j.text.format.NamedFormatter;
import org.mifmi.commons4j.util.DateUtilz;

import net.mozq.picto.App;
import net.mozq.picto.AppConfig;
import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.core.ProcessCondition;
import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FileSizeUnit;
import net.mozq.picto.enums.OperationType;

import javax.swing.JSeparator;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private static final String NUMBER_PATTERN = "#"; //$NON-NLS-1$
	private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$
	private static final String DATE_MASK_PATTERN = "****/**/** **:**:**"; //$NON-NLS-1$
	private static final String DATE_MASK_PLACEHOLDER = "_"; //$NON-NLS-1$
	private static final char DATE_MASK_PLACEHOLDER_CHAR = '_';
	private static final String DATE_MASK_VALID_CHARS = "0123456789_"; //$NON-NLS-1$
	private static final String DATE_NASK_DEFAULT_VALUE = "____/__/__ __:__:__"; //$NON-NLS-1$

	private TimeZone timeZone = TimeZone.getDefault();
	private DateTimeFormatter[] dateTimeFormatters = {
			DateTimeFormatter
					.ofPattern("u/[M]/[d] [H]:[m]:[s]") //$NON-NLS-1$
					.withZone(timeZone.toZoneId())
					.withLocale(Locale.ENGLISH)
					.withResolverStyle(ResolverStyle.SMART),
			DateTimeFormatter
					.ofPattern("/[M]/[d] [H]:[m]:[s]") //$NON-NLS-1$
					.withZone(timeZone.toZoneId())
					.withLocale(Locale.ENGLISH)
					.withResolverStyle(ResolverStyle.SMART),
			DateTimeFormatter
					.ofPattern("//d [H]:[m]:[s]") //$NON-NLS-1$
					.withZone(timeZone.toZoneId())
					.withLocale(Locale.ENGLISH)
					.withResolverStyle(ResolverStyle.SMART)
	};
	
	private static final String SETTINGS_FILE_NAME_EXT = "picto"; //$NON-NLS-1$
	
	private final JFrame frame;
	
	private JPanel contentPane;
	
	private JPanel pnlSrcConditions;
	private JPanel pnlSrcRootDirPath;
	private JTextField txtSrcRootDirPath;
	private JButton btnSrcRootDirSelect;
	private JLabel lblFilePattern;
	private JPanel pnlFilePattern;
	private JTextField txtFilePattern;
	private JCheckBox chkFilePatternRegex;
	private JCheckBox chkContainsSubs;
	private JCheckBox chkContainsHiddens;
	private JLabel lblFileSizeRange;
	private JPanel pnlFileSizeRange;
	private JFormattedTextField txtFileSizeRangeFrom;
	private JLabel lblFileSizeRangeTo;
	private JFormattedTextField txtFileSizeRangeTo;
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
	private JLabel lblOpeType;
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
	private JFormattedTextField txtDateModYears;
	private JLabel lblSepYM;
	private JFormattedTextField txtDateModMonths;
	private JLabel lblSepMD;
	private JFormattedTextField txtDateModDays;
	private JLabel lblSepDH;
	private JFormattedTextField txtDateModHours;
	private JLabel lblSepHM;
	private JFormattedTextField txtDateModMinutes;
	private JLabel lblSepMS;
	private JFormattedTextField txtDateModSeconds;
	
	private JPanel pnlModExif;
	private JCheckBox chkRemoveExifTagsGps;
	private JCheckBox chkRemoveExifTagsAll;
	
	private JSeparator sprSrcOptionsL;
	private JSeparator sprDestOptionsL;
	private JCheckBox chkDryRun;
	private JButton btnStart;
	private JLabel lblSrcOptions;
	private JLabel lblDestOptions;
	private JPanel pnlSrcOptions;
	private JPanel pnlDestOptions;
	private JSeparator sprSrcOptionsR;
	private JSeparator sprDestOptionsR;
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
						App.config().loadFromFile(file.toPath());
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
					
					if (!SETTINGS_FILE_NAME_EXT.equals(FileUtilz.getExt(file.getName()))) {
						file = new File(file.getParentFile(), file.getName() + "." + SETTINGS_FILE_NAME_EXT); //$NON-NLS-1$
					}
					
					try {
						App.config().storeToFile(file.toPath(), ""); //$NON-NLS-1$
						
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
				WindowUtilz.setPositionCenter(helpDialog, frame);
				helpDialog.setVisible(true);
			}
		});
		mnHelp.add(mntmHelp);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{427, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);

		pnlSrcConditions = new JPanel();
		pnlSrcConditions.setBorder(new TitledBorder(null, Messages.getString("MainFrame.srcConditionsTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		GridBagConstraints gbc_pnlSrcConditions = new GridBagConstraints();
		gbc_pnlSrcConditions.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlSrcConditions.anchor = GridBagConstraints.NORTH;
		gbc_pnlSrcConditions.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSrcConditions.gridx = 0;
		gbc_pnlSrcConditions.gridy = 0;
		getContentPane().add(pnlSrcConditions, gbc_pnlSrcConditions);
		GridBagLayout gbl_pnlSrcConditions = new GridBagLayout();
		gbl_pnlSrcConditions.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSrcConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlSrcConditions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSrcConditions.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSrcConditions.setLayout(gbl_pnlSrcConditions);
		
		JLabel lblSrcRootDirPath = new JLabel(Messages.getString("MainFrame.srcRootDirPath")); //$NON-NLS-1$
		GridBagConstraints gbc_lblSrcRootDirPath = new GridBagConstraints();
		gbc_lblSrcRootDirPath.anchor = GridBagConstraints.WEST;
		gbc_lblSrcRootDirPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblSrcRootDirPath.gridx = 0;
		gbc_lblSrcRootDirPath.gridy = 0;
		pnlSrcConditions.add(lblSrcRootDirPath, gbc_lblSrcRootDirPath);
		
		pnlSrcRootDirPath = new JPanel();
		pnlSrcRootDirPath.setBorder(null);
		GridBagConstraints gbc_pnlSrcRootDirPath = new GridBagConstraints();
		gbc_pnlSrcRootDirPath.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSrcRootDirPath.fill = GridBagConstraints.BOTH;
		gbc_pnlSrcRootDirPath.gridx = 1;
		gbc_pnlSrcRootDirPath.gridy = 0;
		pnlSrcConditions.add(pnlSrcRootDirPath, gbc_pnlSrcRootDirPath);
		pnlSrcRootDirPath.setLayout(new BorderLayout(0, 0));
		
		btnSrcRootDirSelect = new JButton(Messages.getString("MainFrame.srcRootDirSelect")); //$NON-NLS-1$
		pnlSrcRootDirPath.add(btnSrcRootDirSelect, BorderLayout.EAST);
		
		txtSrcRootDirPath = new JTextField();
		lblSrcRootDirPath.setLabelFor(txtSrcRootDirPath);
		pnlSrcRootDirPath.add(txtSrcRootDirPath, BorderLayout.CENTER);
		txtSrcRootDirPath.setColumns(10);
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
		
		pnlSrcOptions = new JPanel();
		GridBagConstraints gbc_pnlSrcOptions = new GridBagConstraints();
		gbc_pnlSrcOptions.fill = GridBagConstraints.BOTH;
		gbc_pnlSrcOptions.gridwidth = 2;
		gbc_pnlSrcOptions.insets = new Insets(0, 0, 5, 5);
		gbc_pnlSrcOptions.gridx = 0;
		gbc_pnlSrcOptions.gridy = 1;
		pnlSrcConditions.add(pnlSrcOptions, gbc_pnlSrcOptions);
		GridBagLayout gbl_pnlSrcOptions = new GridBagLayout();
		gbl_pnlSrcOptions.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlSrcOptions.rowHeights = new int[]{0, 0};
		gbl_pnlSrcOptions.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSrcOptions.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSrcOptions.setLayout(gbl_pnlSrcOptions);
		
		sprSrcOptionsL = new JSeparator();
		GridBagConstraints gbc_sprSrcOptionsL = new GridBagConstraints();
		gbc_sprSrcOptionsL.insets = new Insets(0, 0, 0, 5);
		gbc_sprSrcOptionsL.fill = GridBagConstraints.HORIZONTAL;
		gbc_sprSrcOptionsL.gridx = 0;
		gbc_sprSrcOptionsL.gridy = 0;
		pnlSrcOptions.add(sprSrcOptionsL, gbc_sprSrcOptionsL);
		
		lblSrcOptions = new JLabel(Messages.getString("MainFrame.srcOptionsTitle")); //$NON-NLS-1$
		GridBagConstraints gbc_lblSrcOptions = new GridBagConstraints();
		gbc_lblSrcOptions.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcOptions.gridx = 1;
		gbc_lblSrcOptions.gridy = 0;
		pnlSrcOptions.add(lblSrcOptions, gbc_lblSrcOptions);
		lblSrcOptions.setForeground(Color.DARK_GRAY);
		
		sprSrcOptionsR = new JSeparator();
		GridBagConstraints gbc_sprSrcOptionsR = new GridBagConstraints();
		gbc_sprSrcOptionsR.fill = GridBagConstraints.HORIZONTAL;
		gbc_sprSrcOptionsR.gridx = 2;
		gbc_sprSrcOptionsR.gridy = 0;
		pnlSrcOptions.add(sprSrcOptionsR, gbc_sprSrcOptionsR);
		
		lblFilePattern = new JLabel(Messages.getString("MainFrame.filePattern")); //$NON-NLS-1$
		lblFilePattern.setToolTipText(Messages.getString("MainFrame.filePattern.tooltip")); //$NON-NLS-1$
		GridBagConstraints gbc_lblFilePattern = new GridBagConstraints();
		gbc_lblFilePattern.anchor = GridBagConstraints.WEST;
		gbc_lblFilePattern.insets = new Insets(0, 0, 5, 5);
		gbc_lblFilePattern.gridx = 0;
		gbc_lblFilePattern.gridy = 2;
		pnlSrcConditions.add(lblFilePattern, gbc_lblFilePattern);
		
		pnlFilePattern = new JPanel();
		GridBagConstraints gbc_pnlFilePattern = new GridBagConstraints();
		gbc_pnlFilePattern.fill = GridBagConstraints.BOTH;
		gbc_pnlFilePattern.insets = new Insets(0, 0, 5, 0);
		gbc_pnlFilePattern.gridx = 1;
		gbc_pnlFilePattern.gridy = 2;
		pnlSrcConditions.add(pnlFilePattern, gbc_pnlFilePattern);
		GridBagLayout gbl_pnlFilePattern = new GridBagLayout();
		gbl_pnlFilePattern.columnWidths = new int[]{0, 0, 0};
		gbl_pnlFilePattern.rowHeights = new int[]{0, 0};
		gbl_pnlFilePattern.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlFilePattern.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlFilePattern.setLayout(gbl_pnlFilePattern);
		
		txtFilePattern = new JTextField();
		lblFilePattern.setLabelFor(txtFilePattern);
		GridBagConstraints gbc_txtFilePattern = new GridBagConstraints();
		gbc_txtFilePattern.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFilePattern.insets = new Insets(0, 0, 0, 5);
		gbc_txtFilePattern.gridx = 0;
		gbc_txtFilePattern.gridy = 0;
		pnlFilePattern.add(txtFilePattern, gbc_txtFilePattern);
		txtFilePattern.setColumns(10);
		
		chkFilePatternRegex = new JCheckBox(Messages.getString("MainFrame.filePatternRegex")); //$NON-NLS-1$
		GridBagConstraints gbc_chkFilePatternRegex = new GridBagConstraints();
		gbc_chkFilePatternRegex.anchor = GridBagConstraints.WEST;
		gbc_chkFilePatternRegex.gridx = 1;
		gbc_chkFilePatternRegex.gridy = 0;
		pnlFilePattern.add(chkFilePatternRegex, gbc_chkFilePatternRegex);
		
		chkContainsSubs = new JCheckBox(Messages.getString("MainFrame.containsSubs")); //$NON-NLS-1$
		GridBagConstraints gbc_chkContainsSubs = new GridBagConstraints();
		gbc_chkContainsSubs.anchor = GridBagConstraints.WEST;
		gbc_chkContainsSubs.insets = new Insets(0, 0, 5, 0);
		gbc_chkContainsSubs.gridx = 1;
		gbc_chkContainsSubs.gridy = 3;
		pnlSrcConditions.add(chkContainsSubs, gbc_chkContainsSubs);
		
		chkContainsHiddens = new JCheckBox(Messages.getString("MainFrame.containsHiddens")); //$NON-NLS-1$
		GridBagConstraints gbc_chkContainsHiddens = new GridBagConstraints();
		gbc_chkContainsHiddens.fill = GridBagConstraints.BOTH;
		gbc_chkContainsHiddens.insets = new Insets(0, 0, 5, 0);
		gbc_chkContainsHiddens.gridx = 1;
		gbc_chkContainsHiddens.gridy = 4;
		pnlSrcConditions.add(chkContainsHiddens, gbc_chkContainsHiddens);
		
		lblFileSizeRange = new JLabel(Messages.getString("MainFrame.fileSizeRange")); //$NON-NLS-1$
		GridBagConstraints gbc_lblFileSizeRange = new GridBagConstraints();
		gbc_lblFileSizeRange.anchor = GridBagConstraints.WEST;
		gbc_lblFileSizeRange.insets = new Insets(0, 0, 5, 5);
		gbc_lblFileSizeRange.gridx = 0;
		gbc_lblFileSizeRange.gridy = 5;
		pnlSrcConditions.add(lblFileSizeRange, gbc_lblFileSizeRange);
		
		pnlFileSizeRange = new JPanel();
		GridBagConstraints gbc_pnlFileSizeRange = new GridBagConstraints();
		gbc_pnlFileSizeRange.fill = GridBagConstraints.BOTH;
		gbc_pnlFileSizeRange.insets = new Insets(0, 0, 5, 0);
		gbc_pnlFileSizeRange.gridx = 1;
		gbc_pnlFileSizeRange.gridy = 5;
		pnlSrcConditions.add(pnlFileSizeRange, gbc_pnlFileSizeRange);
		pnlFileSizeRange.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		txtFileSizeRangeFrom = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		lblFileSizeRange.setLabelFor(txtFileSizeRangeFrom);
		txtFileSizeRangeFrom.setColumns(5);
		pnlFileSizeRange.add(txtFileSizeRangeFrom);
		txtFileSizeRangeFrom.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		lblFileSizeRangeTo = new JLabel(Messages.getString("MainFrame.fileSizeRangeTo")); //$NON-NLS-1$
		pnlFileSizeRange.add(lblFileSizeRangeTo);
		
		txtFileSizeRangeTo = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		txtFileSizeRangeTo.setColumns(5);
		pnlFileSizeRange.add(txtFileSizeRangeTo);
		txtFileSizeRangeTo.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		cmbFileSizeUnit = new JComboBox<>();
		pnlFileSizeRange.add(cmbFileSizeUnit);
		cmbFileSizeUnit.setModel(new DefaultComboBoxModel<>(FileSizeUnit.values()));
		
		lblCreationTimeRange = new JLabel(Messages.getString("MainFrame.creationTimeRange")); //$NON-NLS-1$
		GridBagConstraints gbc_lblCreationTimeRange = new GridBagConstraints();
		gbc_lblCreationTimeRange.anchor = GridBagConstraints.WEST;
		gbc_lblCreationTimeRange.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreationTimeRange.gridx = 0;
		gbc_lblCreationTimeRange.gridy = 6;
		pnlSrcConditions.add(lblCreationTimeRange, gbc_lblCreationTimeRange);
		
		pnlCreationTimeRange = new JPanel();
		GridBagConstraints gbc_pnlCreationTimeRange = new GridBagConstraints();
		gbc_pnlCreationTimeRange.fill = GridBagConstraints.BOTH;
		gbc_pnlCreationTimeRange.insets = new Insets(0, 0, 5, 0);
		gbc_pnlCreationTimeRange.gridx = 1;
		gbc_pnlCreationTimeRange.gridy = 6;
		pnlSrcConditions.add(pnlCreationTimeRange, gbc_pnlCreationTimeRange);
		pnlCreationTimeRange.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		txtCreationTimeRangeFrom = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		lblCreationTimeRange.setLabelFor(txtCreationTimeRangeFrom);
		txtCreationTimeRangeFrom.setColumns(20);
		txtCreationTimeRangeFrom.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtCreationTimeRangeFrom.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent ev) {
				JFormattedTextField field = (JFormattedTextField)ev.getSource();
				formatDateField(field, dateTimeFormatters, timeZone, Year.now().get(ChronoField.YEAR), 1, 1, 0, 0, 0, 0);
			}
		});
		txtCreationTimeRangeFrom.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlCreationTimeRange.add(txtCreationTimeRangeFrom);
		
		lblCreationTimeRangeTo = new JLabel(Messages.getString("MainFrame.creationTimeRangeTo")); //$NON-NLS-1$
		pnlCreationTimeRange.add(lblCreationTimeRangeTo);
		
		txtCreationTimeRangeTo = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		txtCreationTimeRangeTo.setColumns(20);
		txtCreationTimeRangeTo.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtCreationTimeRangeTo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent ev) {
				JFormattedTextField field = (JFormattedTextField)ev.getSource();
				formatDateField(field, dateTimeFormatters, timeZone, Year.now().get(ChronoField.YEAR), 12, 31, 23, 59, 59, 999);
			}
		});
		txtCreationTimeRangeTo.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlCreationTimeRange.add(txtCreationTimeRangeTo);
		
		lblModifiedTimeRange = new JLabel(Messages.getString("MainFrame.modifiedTimeRange")); //$NON-NLS-1$
		GridBagConstraints gbc_lblModifiedTimeRange = new GridBagConstraints();
		gbc_lblModifiedTimeRange.anchor = GridBagConstraints.WEST;
		gbc_lblModifiedTimeRange.insets = new Insets(0, 0, 0, 5);
		gbc_lblModifiedTimeRange.gridx = 0;
		gbc_lblModifiedTimeRange.gridy = 7;
		pnlSrcConditions.add(lblModifiedTimeRange, gbc_lblModifiedTimeRange);
		
		pnlModifiedTimeRange = new JPanel();
		GridBagConstraints gbc_pnlModifiedTimeRange = new GridBagConstraints();
		gbc_pnlModifiedTimeRange.fill = GridBagConstraints.BOTH;
		gbc_pnlModifiedTimeRange.gridx = 1;
		gbc_pnlModifiedTimeRange.gridy = 7;
		pnlSrcConditions.add(pnlModifiedTimeRange, gbc_pnlModifiedTimeRange);
		pnlModifiedTimeRange.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		txtModifiedTimeRangeFrom = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		lblModifiedTimeRange.setLabelFor(txtModifiedTimeRangeFrom);
		txtModifiedTimeRangeFrom.setColumns(20);
		txtModifiedTimeRangeFrom.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtModifiedTimeRangeFrom.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent ev) {
				JFormattedTextField field = (JFormattedTextField)ev.getSource();
				formatDateField(field, dateTimeFormatters, timeZone, Year.now().get(ChronoField.YEAR), 1, 1, 0, 0, 0, 0);
			}
		});
		txtModifiedTimeRangeFrom.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlModifiedTimeRange.add(txtModifiedTimeRangeFrom);
		
		lblModifiedTimeRangeTo = new JLabel(Messages.getString("MainFrame.modifiedTimeRangeTo")); //$NON-NLS-1$
		pnlModifiedTimeRange.add(lblModifiedTimeRangeTo);
		
		txtModifiedTimeRangeTo = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		txtModifiedTimeRangeTo.setColumns(20);
		txtModifiedTimeRangeTo.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtModifiedTimeRangeTo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent ev) {
				JFormattedTextField field = (JFormattedTextField)ev.getSource();
				formatDateField(field, dateTimeFormatters, timeZone, Year.now().get(ChronoField.YEAR), 12, 31, 23, 59, 59, 999);
			}
		});
		txtModifiedTimeRangeTo.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlModifiedTimeRange.add(txtModifiedTimeRangeTo);
		
		pnlDestConditions = new JPanel();
		pnlDestConditions.setBorder(new TitledBorder(null, Messages.getString("MainFrame.destConditionsTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		GridBagConstraints gbc_pnlDestConditions = new GridBagConstraints();
		gbc_pnlDestConditions.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDestConditions.anchor = GridBagConstraints.NORTH;
		gbc_pnlDestConditions.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlDestConditions.gridx = 0;
		gbc_pnlDestConditions.gridy = 1;
		getContentPane().add(pnlDestConditions, gbc_pnlDestConditions);
		GridBagLayout gbl_pnlDestConditions = new GridBagLayout();
		gbl_pnlDestConditions.columnWidths = new int[]{0, 0, 0};
		gbl_pnlDestConditions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlDestConditions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlDestConditions.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlDestConditions.setLayout(gbl_pnlDestConditions);
		
		lblOpeType = new JLabel(Messages.getString("MainFrame.opeType")); //$NON-NLS-1$
		GridBagConstraints gbc_lblOpeType = new GridBagConstraints();
		gbc_lblOpeType.anchor = GridBagConstraints.WEST;
		gbc_lblOpeType.insets = new Insets(0, 0, 5, 5);
		gbc_lblOpeType.gridx = 0;
		gbc_lblOpeType.gridy = 0;
		pnlDestConditions.add(lblOpeType, gbc_lblOpeType);
		
		pnlOpeType = new JPanel();
		pnlOpeType.setBorder(null);
		GridBagConstraints gbc_pnlOpeType = new GridBagConstraints();
		gbc_pnlOpeType.insets = new Insets(0, 0, 5, 0);
		gbc_pnlOpeType.fill = GridBagConstraints.BOTH;
		gbc_pnlOpeType.gridx = 1;
		gbc_pnlOpeType.gridy = 0;
		pnlDestConditions.add(pnlOpeType, gbc_pnlOpeType);
		pnlOpeType.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		rdoOpeTypeCopy = new JRadioButton(Messages.getString("MainFrame.opeTypeCopy")); //$NON-NLS-1$
		lblOpeType.setLabelFor(rdoOpeTypeCopy);
		btngrpOpeType.add(rdoOpeTypeCopy);
		pnlOpeType.add(rdoOpeTypeCopy);
		
		rdoOpeTypeMove = new JRadioButton(Messages.getString("MainFrame.opeTypeMove")); //$NON-NLS-1$
		btngrpOpeType.add(rdoOpeTypeMove);
		pnlOpeType.add(rdoOpeTypeMove);
		
		rdoOpeTypeOverwrite = new JRadioButton(Messages.getString("MainFrame.opeTypeOverwrite")); //$NON-NLS-1$
		btngrpOpeType.add(rdoOpeTypeOverwrite);
		pnlOpeType.add(rdoOpeTypeOverwrite);
		
		lblDestRootDirPath = new JLabel(Messages.getString("MainFrame.destRootDirPath")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDestRootDirPath = new GridBagConstraints();
		gbc_lblDestRootDirPath.anchor = GridBagConstraints.WEST;
		gbc_lblDestRootDirPath.insets = new Insets(0, 0, 5, 5);
		gbc_lblDestRootDirPath.gridx = 0;
		gbc_lblDestRootDirPath.gridy = 1;
		pnlDestConditions.add(lblDestRootDirPath, gbc_lblDestRootDirPath);
		
		pnlDestRootDirPath = new JPanel();
		pnlDestRootDirPath.setBorder(null);
		GridBagConstraints gbc_pnlDestRootDirPath = new GridBagConstraints();
		gbc_pnlDestRootDirPath.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDestRootDirPath.fill = GridBagConstraints.BOTH;
		gbc_pnlDestRootDirPath.gridx = 1;
		gbc_pnlDestRootDirPath.gridy = 1;
		pnlDestConditions.add(pnlDestRootDirPath, gbc_pnlDestRootDirPath);
		pnlDestRootDirPath.setLayout(new BorderLayout(0, 0));
		
		btnDestRootDirSelect = new JButton(Messages.getString("MainFrame.destRootDirSelect")); //$NON-NLS-1$
		pnlDestRootDirPath.add(btnDestRootDirSelect, BorderLayout.EAST);
		
		txtDestRootDirPath = new JTextField();
		lblDestRootDirPath.setLabelFor(txtDestRootDirPath);
		pnlDestRootDirPath.add(txtDestRootDirPath, BorderLayout.CENTER);
		txtDestRootDirPath.setColumns(10);
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
		
		pnlDestOptions = new JPanel();
		GridBagConstraints gbc_pnlDestOptions = new GridBagConstraints();
		gbc_pnlDestOptions.fill = GridBagConstraints.BOTH;
		gbc_pnlDestOptions.gridwidth = 2;
		gbc_pnlDestOptions.insets = new Insets(0, 0, 5, 5);
		gbc_pnlDestOptions.gridx = 0;
		gbc_pnlDestOptions.gridy = 2;
		pnlDestConditions.add(pnlDestOptions, gbc_pnlDestOptions);
		GridBagLayout gbl_pnlDestOptions = new GridBagLayout();
		gbl_pnlDestOptions.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlDestOptions.rowHeights = new int[]{0, 0};
		gbl_pnlDestOptions.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlDestOptions.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlDestOptions.setLayout(gbl_pnlDestOptions);
		
		sprDestOptionsL = new JSeparator();
		GridBagConstraints gbc_sprDestOptionsL = new GridBagConstraints();
		gbc_sprDestOptionsL.insets = new Insets(0, 0, 0, 5);
		gbc_sprDestOptionsL.fill = GridBagConstraints.HORIZONTAL;
		gbc_sprDestOptionsL.gridx = 0;
		gbc_sprDestOptionsL.gridy = 0;
		pnlDestOptions.add(sprDestOptionsL, gbc_sprDestOptionsL);
		
		lblDestOptions = new JLabel(Messages.getString("MainFrame.destOptionsTitle")); //$NON-NLS-1$
		lblDestOptions.setForeground(Color.DARK_GRAY);
		GridBagConstraints gbc_lblDestOptions = new GridBagConstraints();
		gbc_lblDestOptions.insets = new Insets(0, 0, 0, 5);
		gbc_lblDestOptions.gridx = 1;
		gbc_lblDestOptions.gridy = 0;
		pnlDestOptions.add(lblDestOptions, gbc_lblDestOptions);
		
		sprDestOptionsR = new JSeparator();
		GridBagConstraints gbc_sprDestOptionsR = new GridBagConstraints();
		gbc_sprDestOptionsR.fill = GridBagConstraints.HORIZONTAL;
		gbc_sprDestOptionsR.gridx = 2;
		gbc_sprDestOptionsR.gridy = 0;
		pnlDestOptions.add(sprDestOptionsR, gbc_sprDestOptionsR);
		
		lblDestSubPathPattern = new JLabel(Messages.getString("MainFrame.destSubPathPattern")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDestSubPathPattern = new GridBagConstraints();
		gbc_lblDestSubPathPattern.anchor = GridBagConstraints.WEST;
		gbc_lblDestSubPathPattern.insets = new Insets(0, 0, 5, 5);
		gbc_lblDestSubPathPattern.gridx = 0;
		gbc_lblDestSubPathPattern.gridy = 3;
		pnlDestConditions.add(lblDestSubPathPattern, gbc_lblDestSubPathPattern);
		
		txtDestSubPathPattern = new JTextField();
		lblDestSubPathPattern.setLabelFor(txtDestSubPathPattern);
		txtDestSubPathPattern.setColumns(10);
		GridBagConstraints gbc_txtDestSubPathPattern = new GridBagConstraints();
		gbc_txtDestSubPathPattern.insets = new Insets(0, 0, 5, 0);
		gbc_txtDestSubPathPattern.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDestSubPathPattern.gridx = 1;
		gbc_txtDestSubPathPattern.gridy = 3;
		pnlDestConditions.add(txtDestSubPathPattern, gbc_txtDestSubPathPattern);
		
		lblExistingFileMethod = new JLabel(Messages.getString("MainFrame.existingFileMethod")); //$NON-NLS-1$
		GridBagConstraints gbc_lblExistingFileMethod = new GridBagConstraints();
		gbc_lblExistingFileMethod.anchor = GridBagConstraints.WEST;
		gbc_lblExistingFileMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblExistingFileMethod.gridx = 0;
		gbc_lblExistingFileMethod.gridy = 4;
		pnlDestConditions.add(lblExistingFileMethod, gbc_lblExistingFileMethod);
		
		cmbExistingFileMethod = new JComboBox<>();
		lblExistingFileMethod.setLabelFor(cmbExistingFileMethod);
		GridBagConstraints gbc_cmbExistingFileMethod = new GridBagConstraints();
		gbc_cmbExistingFileMethod.insets = new Insets(0, 0, 5, 0);
		gbc_cmbExistingFileMethod.anchor = GridBagConstraints.WEST;
		gbc_cmbExistingFileMethod.gridx = 1;
		gbc_cmbExistingFileMethod.gridy = 4;
		cmbExistingFileMethod.setModel(new DefaultComboBoxModel<>(ExistingFileMethod.values()));
		pnlDestConditions.add(cmbExistingFileMethod, gbc_cmbExistingFileMethod);
		
		lblValidateFile = new JLabel(Messages.getString("MainFrame.validateFile")); //$NON-NLS-1$
		GridBagConstraints gbc_lblValidateFile = new GridBagConstraints();
		gbc_lblValidateFile.anchor = GridBagConstraints.WEST;
		gbc_lblValidateFile.insets = new Insets(0, 0, 0, 5);
		gbc_lblValidateFile.gridx = 0;
		gbc_lblValidateFile.gridy = 5;
		pnlDestConditions.add(lblValidateFile, gbc_lblValidateFile);
		
		chkCheckFileDigest = new JCheckBox(Messages.getString("MainFrame.checkFileDigest")); //$NON-NLS-1$
		lblValidateFile.setLabelFor(chkCheckFileDigest);
		GridBagConstraints gbc_chkCheckFileDigest = new GridBagConstraints();
		gbc_chkCheckFileDigest.anchor = GridBagConstraints.WEST;
		gbc_chkCheckFileDigest.gridx = 1;
		gbc_chkCheckFileDigest.gridy = 5;
		pnlDestConditions.add(chkCheckFileDigest, gbc_chkCheckFileDigest);
		
		tabModConditions = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabModConditions = new GridBagConstraints();
		gbc_tabModConditions.insets = new Insets(0, 0, 5, 0);
		gbc_tabModConditions.fill = GridBagConstraints.BOTH;
		gbc_tabModConditions.gridx = 0;
		gbc_tabModConditions.gridy = 2;
		contentPane.add(tabModConditions, gbc_tabModConditions);
		
		pnlChangeFileDate = new JPanel();
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
		fl_pnlTargetDate.setVgap(0);
		fl_pnlTargetDate.setHgap(0);
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
		pnlBaseDate.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		cmbBaseDateType = new JComboBox<>();
		lblBaseDateType.setLabelFor(cmbBaseDateType);
		cmbBaseDateType.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				DateType dateType = (DateType)e.getItem();
				if (dateType == DateType.CustomDate) {
					txtCustomBaseDate.setVisible(true);
					pnlBaseDate.invalidate();
					pnlBaseDate.validate();
				} else {
					txtCustomBaseDate.setVisible(false);
					pnlBaseDate.invalidate();
					pnlBaseDate.validate();
				}
			}
		});
		pnlBaseDate.add(cmbBaseDateType);
		cmbBaseDateType.setModel(new DefaultComboBoxModel<>(DateType.values()));
		
		txtCustomBaseDate = new JFormattedTextField(newMaskFormatter(DATE_MASK_PATTERN));
		txtCustomBaseDate.setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		txtCustomBaseDate.setColumns(20);
		txtCustomBaseDate.setVisible(false);
		txtCustomBaseDate.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent ev) {
				JFormattedTextField field = (JFormattedTextField)ev.getSource();
				formatDateField(field, dateTimeFormatters, timeZone, Year.now().get(ChronoField.YEAR), 1, 1, 0, 0, 0, 0);
			}
		});
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
		fl_pnlDateModType.setVgap(0);
		fl_pnlDateModType.setHgap(0);
		fl_pnlDateModType.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_pnlDateModType = new GridBagConstraints();
		gbc_pnlDateModType.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlDateModType.gridx = 1;
		gbc_pnlDateModType.gridy = 2;
		pnlChangeFileDate.add(pnlDateModType, gbc_pnlDateModType);
		
		cmbDateModType = new JComboBox<DateModType>();
		lblEditBaseDate.setLabelFor(cmbDateModType);
		cmbDateModType.setModel(new DefaultComboBoxModel<>(DateModType.values()));
		pnlDateModType.add(cmbDateModType);
		
		txtDateModYears = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		pnlDateModType.add(txtDateModYears);
		txtDateModYears.setColumns(4);
		txtDateModYears.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		lblSepYM = new JLabel("/"); //$NON-NLS-1$
		pnlDateModType.add(lblSepYM);
		
		txtDateModMonths = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		pnlDateModType.add(txtDateModMonths);
		txtDateModMonths.setColumns(2);
		txtDateModMonths.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		lblSepMD = new JLabel("/"); //$NON-NLS-1$
		pnlDateModType.add(lblSepMD);
		
		txtDateModDays = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		pnlDateModType.add(txtDateModDays);
		txtDateModDays.setColumns(2);
		txtDateModDays.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		lblSepDH = new JLabel(" "); //$NON-NLS-1$
		pnlDateModType.add(lblSepDH);
		
		txtDateModHours = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		pnlDateModType.add(txtDateModHours);
		txtDateModHours.setColumns(2);
		txtDateModHours.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		lblSepHM = new JLabel(":"); //$NON-NLS-1$
		pnlDateModType.add(lblSepHM);
		
		txtDateModMinutes = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		pnlDateModType.add(txtDateModMinutes);
		txtDateModMinutes.setColumns(2);
		txtDateModMinutes.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		lblSepMS = new JLabel(":"); //$NON-NLS-1$
		pnlDateModType.add(lblSepMS);
		
		txtDateModSeconds = new JFormattedTextField(newNumberFormatter(NUMBER_PATTERN));
		pnlDateModType.add(txtDateModSeconds);
		txtDateModSeconds.setColumns(2);
		txtDateModSeconds.setFocusLostBehavior(JFormattedTextField.COMMIT);
		
		pnlModExif = new JPanel();
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
		
		pnlControls = new JPanel();
		GridBagConstraints gbc_pnlControls = new GridBagConstraints();
		gbc_pnlControls.anchor = GridBagConstraints.SOUTH;
		gbc_pnlControls.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlControls.gridx = 0;
		gbc_pnlControls.gridy = 3;
		getContentPane().add(pnlControls, gbc_pnlControls);
		pnlControls.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		btnStart = new JButton(Messages.getString("MainFrame.start")); //$NON-NLS-1$
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProcessCondition processCondition = createProcessCondition();
				
				if (processCondition == null) {
					// Validation failed
					return;
				}
				
				ProcessDialog processDialog = new ProcessDialog(frame);
				processDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
				WindowUtilz.setPositionCenter(processDialog, frame);
				processDialog.doProcess(processCondition);
				processDialog.setVisible(true);
			}
		});
		pnlControls.add(btnStart);
		
		chkDryRun = new JCheckBox(Messages.getString("MainFrame.dryRun")); //$NON-NLS-1$
		pnlControls.add(chkDryRun);
		

		rdoOpeTypeCopy.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setEnableDestConditions(true);
			}
		});
		rdoOpeTypeMove.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setEnableDestConditions(true);
			}
		});
		rdoOpeTypeOverwrite.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setEnableDestConditions(false);
			}
		});
		
		loadSettings();
		changeEnableFileDateModConditions();
		
		frame = this;
	}

	protected void loadSettings() {
		AppConfig conf = App.config();

		txtSrcRootDirPath.setText(conf.get("src.root.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtFilePattern.setText(conf.get("file.pattern", "")); //$NON-NLS-1$ //$NON-NLS-2$
		chkFilePatternRegex.setSelected(conf.getBoolean("file.pattern.regex", false)); //$NON-NLS-1$
		chkContainsSubs.setSelected(conf.getBoolean("contains.subs", true)); //$NON-NLS-1$
		chkContainsHiddens.setSelected(conf.getBoolean("contains.hiddens", false)); //$NON-NLS-1$
		
		txtFileSizeRangeFrom.setText(conf.get("file.size.range.from", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtFileSizeRangeTo.setText(conf.get("file.size.range.to", "")); //$NON-NLS-1$ //$NON-NLS-2$
		cmbFileSizeUnit.setSelectedItem(conf.getEnum("file.size.unit", FileSizeUnit.class, FileSizeUnit.MB)); //$NON-NLS-1$
		txtCreationTimeRangeFrom.setText(conf.get("creation.time.range.from", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtCreationTimeRangeTo.setText(conf.get("creation.time.range.to", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtModifiedTimeRangeFrom.setText(conf.get("modified.time.range.from", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtModifiedTimeRangeTo.setText(conf.get("modified.time.range.to", "")); //$NON-NLS-1$ //$NON-NLS-2$
		
		rdoOpeTypeCopy.setSelected(conf.getBoolean("ope.type.copy", true)); //$NON-NLS-1$
		rdoOpeTypeMove.setSelected(conf.getBoolean("ope.type.move", false)); //$NON-NLS-1$
		rdoOpeTypeOverwrite.setSelected(conf.getBoolean("ope.type.overwrite", false)); //$NON-NLS-1$
		
		txtDestRootDirPath.setText(conf.get("dest.root.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDestSubPathPattern.setText(conf.get("dest.sub.path.pattern", "${FileName}")); //$NON-NLS-1$ //$NON-NLS-2$
		cmbExistingFileMethod.setSelectedItem(conf.getEnum("existing.file.method", ExistingFileMethod.class, ExistingFileMethod.Confirm)); //$NON-NLS-1$
		chkCheckFileDigest.setSelected(conf.getBoolean("check.file.digest", false)); //$NON-NLS-1$
		
		chkChangeFileCreationDate.setSelected(conf.getBoolean("change.file.creation.date", false)); //$NON-NLS-1$
		chkChangeFileModifiedDate.setSelected(conf.getBoolean("change.file.modified.date", false)); //$NON-NLS-1$
		chkChangeFileAccessDate.setSelected(conf.getBoolean("change.file.access.date", false)); //$NON-NLS-1$
		chkChangeExifDate.setSelected(conf.getBoolean("change.file.exif.date", false)); //$NON-NLS-1$
		cmbBaseDateType.setSelectedItem(conf.getEnum("base.date.type", DateType.class, DateType.FileModifiedDate)); //$NON-NLS-1$
		txtCustomBaseDate.setText(conf.get("custom.base.date", "")); //$NON-NLS-1$ //$NON-NLS-2$
		cmbDateModType.setSelectedItem(conf.getEnum("date.mod.type", DateModType.class, DateModType.None)); //$NON-NLS-1$
		txtDateModYears.setText(conf.get("date.mod.year", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModMonths.setText(conf.get("date.mod.month", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModDays.setText(conf.get("date.mod.day", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModHours.setText(conf.get("date.mod.hour", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModMinutes.setText(conf.get("date.mod.minute", "")); //$NON-NLS-1$ //$NON-NLS-2$
		txtDateModSeconds.setText(conf.get("date.mod.second", "")); //$NON-NLS-1$ //$NON-NLS-2$
		chkRemoveExifTagsGps.setSelected(conf.getBoolean("remove.exif.tags.gps", false)); //$NON-NLS-1$
		chkRemoveExifTagsAll.setSelected(conf.getBoolean("remove.exif.tags.all", false)); //$NON-NLS-1$
		
		chkDryRun.setSelected(conf.getBoolean("dry.run", false)); //$NON-NLS-1$
	}

	protected void storeSettings() throws IOException {
		AppConfig conf = App.config();
		
		conf.set("src.root.dir", txtSrcRootDirPath.getText()); //$NON-NLS-1$
		conf.set("file.pattern", txtFilePattern.getText()); //$NON-NLS-1$
		conf.setBoolean("file.pattern.regex", chkFilePatternRegex.isSelected()); //$NON-NLS-1$
		conf.setBoolean("contains.subs", chkContainsSubs.isSelected()); //$NON-NLS-1$
		conf.setBoolean("contains.hiddens", chkContainsHiddens.isSelected()); //$NON-NLS-1$
		
		conf.set("file.size.range.from", txtFileSizeRangeFrom.getText()); //$NON-NLS-1$
		conf.set("file.size.range.to", txtFileSizeRangeTo.getText()); //$NON-NLS-1$
		conf.setEnum("file.size.unit", (Enum<?>)cmbFileSizeUnit.getSelectedItem()); //$NON-NLS-1$
		conf.set("creation.time.range.from", txtCreationTimeRangeFrom.getText()); //$NON-NLS-1$
		conf.set("creation.time.range.to", txtCreationTimeRangeTo.getText()); //$NON-NLS-1$
		conf.set("modified.time.range.from", txtModifiedTimeRangeFrom.getText()); //$NON-NLS-1$
		conf.set("modified.time.range.to", txtModifiedTimeRangeTo.getText()); //$NON-NLS-1$
		
		conf.setBoolean("ope.type.copy", rdoOpeTypeCopy.isSelected()); //$NON-NLS-1$
		conf.setBoolean("ope.type.move", rdoOpeTypeMove.isSelected()); //$NON-NLS-1$
		conf.setBoolean("ope.type.overwrite", rdoOpeTypeOverwrite.isSelected()); //$NON-NLS-1$
		
		conf.set("dest.root.dir", txtDestRootDirPath.getText()); //$NON-NLS-1$
		conf.set("dest.sub.path.pattern", txtDestSubPathPattern.getText()); //$NON-NLS-1$
		conf.setEnum("existing.file.method", (Enum<?>)cmbExistingFileMethod.getSelectedItem()); //$NON-NLS-1$
		conf.setBoolean("check.file.digest", chkCheckFileDigest.isSelected()); //$NON-NLS-1$
		
		conf.setBoolean("change.file.creation.date", chkChangeFileCreationDate.isSelected()); //$NON-NLS-1$
		conf.setBoolean("change.file.modified.date", chkChangeFileModifiedDate.isSelected()); //$NON-NLS-1$
		conf.setBoolean("change.file.access.date", chkChangeFileAccessDate.isSelected()); //$NON-NLS-1$
		conf.setBoolean("change.file.exif.date", chkChangeExifDate.isSelected()); //$NON-NLS-1$
		conf.setEnum("base.date.type", (Enum<?>)cmbBaseDateType.getSelectedItem()); //$NON-NLS-1$
		conf.set("custom.base.date", txtCustomBaseDate.getText()); //$NON-NLS-1$
		conf.setEnum("date.mod.type", (Enum<?>)cmbDateModType.getSelectedItem()); //$NON-NLS-1$
		conf.set("date.mod.year", txtDateModYears.getText()); //$NON-NLS-1$
		conf.set("date.mod.month", txtDateModMonths.getText()); //$NON-NLS-1$
		conf.set("date.mod.day", txtDateModDays.getText()); //$NON-NLS-1$
		conf.set("date.mod.hour", txtDateModHours.getText()); //$NON-NLS-1$
		conf.set("date.mod.minute", txtDateModMinutes.getText()); //$NON-NLS-1$
		conf.set("date.mod.second", txtDateModSeconds.getText()); //$NON-NLS-1$
		conf.setBoolean("remove.exif.tags.gps", chkRemoveExifTagsGps.isSelected()); //$NON-NLS-1$
		conf.setBoolean("remove.exif.tags.all", chkRemoveExifTagsAll.isSelected()); //$NON-NLS-1$
		
		conf.setBoolean("dry.run", chkDryRun.isSelected()); //$NON-NLS-1$
		
		conf.store(null);
	}

	private void setEnableDestConditions(boolean enabled) {
		lblDestRootDirPath.setEnabled(enabled);
		txtDestRootDirPath.setEnabled(enabled);
		btnDestRootDirSelect.setEnabled(enabled);
		lblDestSubPathPattern.setEnabled(enabled);
		txtDestSubPathPattern.setEnabled(enabled);
		lblExistingFileMethod.setEnabled(enabled);
		cmbExistingFileMethod.setEnabled(enabled);
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
		txtCustomBaseDate.setEnabled(enabled);
		lblEditBaseDate.setEnabled(enabled);
		cmbDateModType.setEnabled(enabled);
		txtDateModYears.setEnabled(enabled);
		lblSepYM.setEnabled(enabled);
		txtDateModMonths.setEnabled(enabled);
		lblSepMD.setEnabled(enabled);
		txtDateModDays.setEnabled(enabled);
		lblSepDH.setEnabled(enabled);
		txtDateModHours.setEnabled(enabled);
		lblSepHM.setEnabled(enabled);
		txtDateModMinutes.setEnabled(enabled);
		lblSepMS.setEnabled(enabled);
		txtDateModSeconds.setEnabled(enabled);
	}

	private ProcessCondition createProcessCondition() {

		// Get values
		Path srcRootDirPath = Paths.get(txtSrcRootDirPath.getText()).normalize();
		String filePattern = txtFilePattern.getText();
		boolean filePatternRegex = chkFilePatternRegex.isEnabled() && chkFilePatternRegex.isSelected();
		boolean containsHiddens = chkContainsHiddens.isEnabled() && chkContainsHiddens.isSelected();
		boolean followLinks = false;
		int dept = (chkContainsSubs.isEnabled() && chkContainsSubs.isSelected()) ? Integer.MAX_VALUE : 1;
		
		OperationType operationType;
		if (rdoOpeTypeMove.isSelected()) {
			operationType = OperationType.Move;
		} else if (rdoOpeTypeOverwrite.isSelected()) {
			operationType = OperationType.Overwrite;
		} else {
			operationType = OperationType.Copy;
		}
		
		Path destRootDirPath = Paths.get(txtDestRootDirPath.getText()).normalize();
		String destSubPathPattern = txtDestSubPathPattern.getText();
		ExistingFileMethod existingFileMethod = (ExistingFileMethod)cmbExistingFileMethod.getSelectedItem();
		
		boolean checkDigest = chkCheckFileDigest.isEnabled() && chkCheckFileDigest.isSelected();
		
		boolean changeFileCreationDate = chkChangeFileCreationDate.isEnabled() && chkChangeFileCreationDate.isSelected();
		boolean changeFileModifiedDate = chkChangeFileModifiedDate.isEnabled() && chkChangeFileModifiedDate.isSelected();
		boolean changeFileAccessDate = chkChangeFileAccessDate.isEnabled() && chkChangeFileAccessDate.isSelected();
		boolean changeExifDate = chkChangeExifDate.isEnabled() && chkChangeExifDate.isSelected();
		DateType baseDateType = (DateType)cmbBaseDateType.getSelectedItem();
		Date customBaseDate = parseDate(txtCustomBaseDate.getText(), timeZone);
		DateModType baseDateModType = (DateModType)cmbDateModType.getSelectedItem();
		Integer baseDateModYears = parseInteger(txtDateModYears.getText());
		Integer baseDateModMonths = parseInteger(txtDateModMonths.getText());
		Integer baseDateModDays = parseInteger(txtDateModDays.getText());
		Integer baseDateModHours = parseInteger(txtDateModHours.getText());
		Integer baseDateModMinutes = parseInteger(txtDateModMinutes.getText());
		Integer baseDateModSeconds = parseInteger(txtDateModSeconds.getText());
		
		boolean removeExifTagsGps = chkRemoveExifTagsGps.isEnabled() && chkRemoveExifTagsGps.isSelected();
		boolean removeExifTagsAll = chkRemoveExifTagsAll.isEnabled() && chkRemoveExifTagsAll.isSelected();
		
		boolean dryRun = chkDryRun.isEnabled() && chkDryRun.isSelected();
		
		Long sizeRangeFrom = convertSize(parseLong(txtFileSizeRangeFrom.getText()), (FileSizeUnit)cmbFileSizeUnit.getSelectedItem());
		Long sizeRangeTo = convertSize(parseLong(txtFileSizeRangeTo.getText()), (FileSizeUnit)cmbFileSizeUnit.getSelectedItem());
		
		Date creationTimeRangeFrom = parseDate(txtCreationTimeRangeFrom.getText(), timeZone);
		Date creationTimeRangeTo = setMillisec(parseDate(txtCreationTimeRangeTo.getText(), timeZone), 999, timeZone);
		
		Date modifiedTimeRangeFrom = parseDate(txtModifiedTimeRangeFrom.getText(), timeZone);
		Date modifiedTimeRangeTo = setMillisec(parseDate(txtModifiedTimeRangeTo.getText(), timeZone), 999, timeZone);
		
		// Validations
		if (!Files.exists(srcRootDirPath)) {
			JOptionPane.showMessageDialog(
					frame,
					Messages.getString("message.warn.srcRootPath.not.exists"), //$NON-NLS-1$
					null,
					JOptionPane.WARNING_MESSAGE
					);
			return null;
		}
		
		try {
			FileSystems.getDefault().getPathMatcher(((filePatternRegex) ? "regex:" : "glob:") + filePattern); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					frame,
					Messages.getString("message.warn.invalid.filePattern", e.getLocalizedMessage()), //$NON-NLS-1$
					null,
					JOptionPane.WARNING_MESSAGE
					);
			return null;
		}
		
		if (sizeRangeFrom != null && sizeRangeTo != null) {
			if (sizeRangeFrom.longValue() > sizeRangeTo.longValue()) {
				JOptionPane.showMessageDialog(
						frame,
						Messages.getString("message.warn.sizeRange.is.invalid.range"), //$NON-NLS-1$
						null,
						JOptionPane.WARNING_MESSAGE
						);
				return null;
			}
		}
		
		if (creationTimeRangeFrom != null && creationTimeRangeTo != null) {
			if (DateUtilz.compare(creationTimeRangeFrom, creationTimeRangeTo, false) > 0) {
				JOptionPane.showMessageDialog(
						frame,
						Messages.getString("message.warn.creationTimeRange.is.invalid.range"), //$NON-NLS-1$
						null,
						JOptionPane.WARNING_MESSAGE
						);
				return null;
			}
		}
		
		if (modifiedTimeRangeFrom != null && modifiedTimeRangeTo != null) {
			if (DateUtilz.compare(modifiedTimeRangeFrom, modifiedTimeRangeTo, false) > 0) {
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
		if (checkDigest && (changeExifDate || removeExifTagsGps || removeExifTagsAll)) {
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
			
			checkDigest = false;
		}
		
		// Set values
		PictoPathFilter pathFilter = new PictoPathFilter();
		pathFilter.setPathPattern(filePattern, srcRootDirPath, filePatternRegex);
		pathFilter.setContainsHiddens(containsHiddens);
		pathFilter.setSizeRange(sizeRangeFrom, sizeRangeTo);
		pathFilter.setCreationTimeRange(creationTimeRangeFrom, creationTimeRangeTo);
		pathFilter.setModifiedTimeRange(modifiedTimeRangeFrom, modifiedTimeRangeTo);
//		pathFilter.setAccessTimeRange(from, to);
		
		
		NamedFormatter destSubPathFormat = new NamedFormatter(destSubPathPattern);
		
		ProcessCondition processCondition = new ProcessCondition();
		processCondition.setTimeZone(timeZone);
		processCondition.setSrcRootPath(srcRootDirPath);
		processCondition.setDestRootPath(destRootDirPath);
		processCondition.setDept(dept);
		processCondition.setPathFilter(pathFilter);
		processCondition.setFollowLinks(followLinks);
		processCondition.setDestSubPathFormat(destSubPathFormat);
		processCondition.setOperationType(operationType);
		processCondition.setExistingFileMethod(existingFileMethod);
		processCondition.setCheckDigest(checkDigest);
		processCondition.setChangeFileCreationDate(changeFileCreationDate);
		processCondition.setChangeFileModifiedDate(changeFileModifiedDate);
		processCondition.setChangeFileAccessDate(changeFileAccessDate);
		processCondition.setChangeExifDate(changeExifDate);
		processCondition.setBaseDateType(baseDateType);
		processCondition.setCustomBaseDate(customBaseDate);
		processCondition.setBaseDateModType(baseDateModType);
		processCondition.setBaseDateModYears(baseDateModYears);
		processCondition.setBaseDateModMonths(baseDateModMonths);
		processCondition.setBaseDateModDays(baseDateModDays);
		processCondition.setBaseDateModHours(baseDateModHours);
		processCondition.setBaseDateModMinutes(baseDateModMinutes);
		processCondition.setBaseDateModSeconds(baseDateModSeconds);
		processCondition.setRemveExifTagsGps(removeExifTagsGps);
		processCondition.setRemveExifTagsAll(removeExifTagsAll);
		processCondition.setDryRun(dryRun);
		
		return processCondition;
	}

	
	private static NumberFormat newNumberFormatter(String pattern) {
		DecimalFormat df = new DecimalFormat(pattern);
		df.setParseBigDecimal(false);
		df.setParseIntegerOnly(true);
		return df;
	}
	
	private static DateFormat newDateFormatter(String pattern, TimeZone timeZone) {
		DateFormat df = new SimpleDateFormat(pattern);
		df.setTimeZone(timeZone);
		return df;
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
	
	private static void formatDateField(JFormattedTextField field, DateTimeFormatter[] dateTimeFormatters, TimeZone timeZone, int defaultYear, int defaultMonth, int defaultDay, int defaultHour, int defaultMin, int defaultSec, int defaultMsec) {
		String text = field.getText();
		if (text.equals(DATE_NASK_DEFAULT_VALUE)) {
			return;
		}
		text = text.replace(DATE_MASK_PLACEHOLDER, ""); //$NON-NLS-1$
		Date date = parseDate(text, dateTimeFormatters, timeZone, defaultYear, defaultMonth, defaultDay, defaultHour, defaultMin, defaultSec, defaultMsec);
		field.setText(newDateFormatter(DATE_PATTERN, timeZone).format(date));
	}
	
	private static Date parseDate(String strDate, TimeZone timeZone) {
		Date date;
		try {
			date = newDateFormatter(DATE_PATTERN, timeZone).parse(strDate);
		} catch (ParseException e) {
			date = null;
		}
		return date;
	}
	
	private static Date setMillisec(Date date, int millisec, TimeZone timeZone) {
		if (date == null) {
			return null;
		}
		
		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, millisec);
		
		return cal.getTime();
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
	
	private static Date parseDate(String text, DateTimeFormatter[] dateTimeFormatters, TimeZone timeZone, int defaultYear, int defaultMonth, int defaultDay, int defaultHour, int defaultMin, int defaultSec, int defaultMsec) {
		TemporalAccessor parsedDateAccessor = null;
		for (DateTimeFormatter dateTimeFormatter : dateTimeFormatters) {
			try {
				parsedDateAccessor = dateTimeFormatter.parse(text);
				break;
			} catch (DateTimeParseException e) {
				// NOP
			}
		}
		
		int year = getField(parsedDateAccessor, ChronoField.YEAR, defaultYear);
		int month = getField(parsedDateAccessor, ChronoField.MONTH_OF_YEAR, defaultMonth);
		int day = getField(parsedDateAccessor, ChronoField.DAY_OF_MONTH, defaultDay);
		int hour = getField(parsedDateAccessor, ChronoField.HOUR_OF_DAY, defaultHour);
		int min = getField(parsedDateAccessor, ChronoField.MINUTE_OF_HOUR, defaultMin);
		int sec = getField(parsedDateAccessor, ChronoField.SECOND_OF_MINUTE, defaultSec);
		int msec = getField(parsedDateAccessor, ChronoField.MILLI_OF_SECOND, defaultMsec);
		
		Calendar cal = Calendar.getInstance(timeZone);
		cal.setTimeInMillis(msec);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month - 1);
		cal.set(Calendar.DAY_OF_MONTH, Math.min(day, cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		return cal.getTime();
	}
	
	private static int getField(TemporalAccessor accessor, TemporalField field, int defaultValue) {
		if (accessor == null) {
			return defaultValue;
		}
		
		if (accessor.isSupported(field)) {
			return accessor.get(field);
		} else {
			return defaultValue;
		}
	}
}
