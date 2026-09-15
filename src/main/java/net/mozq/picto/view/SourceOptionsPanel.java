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

import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Consumer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.FileSizeUnit;

class SourceOptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String[] MATCH_COUNT_SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
	private static final int MATCH_COUNT_SPINNER_INTERVAL_MS = 80;
	private static final String MATCH_COUNT_STOP_GLYPH = "■";
	private static final int MATCH_COUNT_DEBOUNCE_MS = 400;
	private static final String CARD_FIELDS = "fields";
	private static final String CARD_SUMMARY = "summary";
	private static final int FIELDS_TOP_PADDING_WITH_MATCH_COUNT = 4;

	final JLabel lblMatchCount;
	final JButton btnMatchCountStop;
	private final Timer matchCountSpinnerTimer;
	private int matchCountSpinnerFrame;
	private boolean matchCountHovered;
	private boolean matchCountScanning;
	private final TimeZone timeZone;
	private final Timer matchCountTimer;
	private boolean matchCountEnabled;
	private SourceFileScanner sourceFileScanner;
	private SourceFileScanner.MatchCountStatus lastMatchCountStatus;
	private String srcFolderText = "";
	final JLabel lblFileNamePattern;
	final JTextField txtFileNamePattern;
	final JComboBox<FilePatternSyntax> cmbFileNamePatternSyntax;
	final JLabel lblInclude;
	final JCheckBox chkIncludeSubfolders;
	final JCheckBox chkIncludeHiddenFiles;
	final JLabel lblFileSize;
	final JTextField txtFileSizeFrom;
	final JLabel lblFileSizeTo;
	final JTextField txtFileSizeTo;
	final JComboBox<FileSizeUnit> cmbFileSizeUnit;
	final JLabel lblCreated;
	final JFormattedTextField txtCreatedFrom;
	final JLabel lblCreatedTo;
	final JFormattedTextField txtCreatedTo;
	final JLabel lblModified;
	final JFormattedTextField txtModifiedFrom;
	final JLabel lblModifiedTo;
	final JFormattedTextField txtModifiedTo;

	private final JPanel fieldsView;
	private final CardLayout cardLayout;
	private final JTextArea summaryView;
	private boolean expanded;
	private Consumer<Boolean> onExpandedChanged = _ -> { };
	private Runnable onContentChanged = () -> { };

	SourceOptionsPanel(int inlineHgap, int inlineVgap, TimeZone timeZone) {
		this.timeZone = timeZone;
		fieldsView = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		fieldsView.setLayout(layout);
		PanelStyleSupport.stylizeOptionsBody(fieldsView, FIELDS_TOP_PADDING_WITH_MATCH_COUNT);

		// No horizontal gap: a gap here is dead space that belongs to neither the label nor the stop
		// button, so hovering into it drops out of both of their mouseEntered/mouseExited pairs and
		// flips the stop control back to the spinner while the pointer is still over this area.
		JPanel matchCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		matchCountPanel.setOpaque(false);

		lblMatchCount = new JLabel(Messages.getString("MainFrame.src.matchCount.prompt"));
		lblMatchCount.setFont(lblMatchCount.getFont().deriveFont(lblMatchCount.getFont().getSize2D() - 2f));
		lblMatchCount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// A single button plays both roles instead of swapping between two components: swapping components
		// (even ones sized identically via a CardLayout) hides one and shows the other, and hiding a
		// component that the mouse is currently over makes AWT immediately re-target the mouse, which fires
		// mouseExited/mouseEntered right back on this same pair of listeners - flipping the display straight
		// back before the user can see or click it. Cycling this one button's own text has no such feedback
		// loop, since neither its identity nor its visibility ever changes while the pointer is over it.
		btnMatchCountStop = new JButton(MATCH_COUNT_SPINNER_FRAMES[0]);
		btnMatchCountStop.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
		btnMatchCountStop.setFont(btnMatchCountStop.getFont().deriveFont(btnMatchCountStop.getFont().getSize2D() + 2f));
		btnMatchCountStop.setMargin(new Insets(0, 2, 0, 2));
		btnMatchCountStop.setPreferredSize(new Dimension(20, 20));
		btnMatchCountStop.setToolTipText(Messages.getString("MainFrame.src.matchCount.stop"));
		btnMatchCountStop.setVisible(false);

		matchCountSpinnerTimer = new Timer(MATCH_COUNT_SPINNER_INTERVAL_MS, _ -> {
			matchCountSpinnerFrame = (matchCountSpinnerFrame + 1) % MATCH_COUNT_SPINNER_FRAMES.length;
			if (!matchCountHovered) {
				btnMatchCountStop.setText(MATCH_COUNT_SPINNER_FRAMES[matchCountSpinnerFrame]);
			}
		});
		matchCountTimer = new Timer(MATCH_COUNT_DEBOUNCE_MS, _ -> updateMatchCountTarget());
		matchCountTimer.setRepeats(false);

		MouseAdapter matchCountHoverListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setMatchCountHovered(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setMatchCountHovered(false);
			}
		};
		lblMatchCount.addMouseListener(matchCountHoverListener);
		lblMatchCount.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				matchCountLabelClicked();
			}
		});
		btnMatchCountStop.addMouseListener(matchCountHoverListener);
		btnMatchCountStop.addActionListener(_ -> matchCountStopButtonClicked());
		btnMatchCountStop.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				setMatchCountHovered(true);
			}

			@Override
			public void focusLost(FocusEvent e) {
				setMatchCountHovered(false);
			}
		});

		matchCountPanel.add(lblMatchCount);
		matchCountPanel.add(btnMatchCountStop);

		fieldsView.add(matchCountPanel, GridBagSupport.at(0, 0).fill(GridBagConstraints.HORIZONTAL).gridwidth(2).insets(0, 0, 5, 0).build());

		lblFileNamePattern = new JLabel(Messages.getString("MainFrame.src.fileNamePattern"));

		JPanel pnlFileNamePattern = new JPanel();
		pnlFileNamePattern.setOpaque(false);
		GridBagLayout fileNamePatternLayout = new GridBagLayout();
		fileNamePatternLayout.columnWidths = new int[]{0, 0, 0};
		fileNamePatternLayout.rowHeights = new int[]{0, 0};
		fileNamePatternLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		fileNamePatternLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlFileNamePattern.setLayout(fileNamePatternLayout);

		txtFileNamePattern = new JTextField();
		txtFileNamePattern.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, UIManager.getIcon("FileView.fileIcon"));
		lblFileNamePattern.setLabelFor(txtFileNamePattern);
		InputSupport.installLabelFocusAction(lblFileNamePattern, txtFileNamePattern, LabelFocusBehavior.CARET_END);
		txtFileNamePattern.setColumns(10);

		cmbFileNamePatternSyntax = new JComboBox<>();
		cmbFileNamePatternSyntax.setModel(new DefaultComboBoxModel<>(FilePatternSyntax.values()));
		FileNamePatternPopup fileNamePatternPopup = new FileNamePatternPopup(txtFileNamePattern, this::selectedFilePatternSyntax);
		cmbFileNamePatternSyntax.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				fileNamePatternPopup.refresh();
			}
		});

		// Left inset matches the implicit left margin FlowLayout(LEFT, inlineHgap, ...) gives every other
		// row's first field (Include/Size/Created/Modified), so File name's field starts at the same x.
		pnlFileNamePattern.add(txtFileNamePattern, GridBagSupport.at(0, 0).fill(GridBagConstraints.HORIZONTAL).insets(0, inlineHgap, 0, 5).build());
		pnlFileNamePattern.add(cmbFileNamePatternSyntax, GridBagSupport.at(1, 0).anchor(GridBagConstraints.WEST).build());

		fieldsView.add(lblFileNamePattern, GridBagSupport.at(0, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		fieldsView.add(pnlFileNamePattern, GridBagSupport.at(1, 1).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());

		lblInclude = new JLabel(Messages.getString("MainFrame.src.include"));

		JPanel pnlInclude = new JPanel();
		pnlInclude.setOpaque(false);
		pnlInclude.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		chkIncludeSubfolders = new JCheckBox(Messages.getString("MainFrame.src.includeSubfolders"));
		chkIncludeHiddenFiles = new JCheckBox(Messages.getString("MainFrame.src.includeHiddenFiles"));

		pnlInclude.add(chkIncludeSubfolders);
		pnlInclude.add(chkIncludeHiddenFiles);

		fieldsView.add(lblInclude, GridBagSupport.at(0, 2).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		fieldsView.add(pnlInclude, GridBagSupport.at(1, 2).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());

		lblFileSize = new JLabel(Messages.getString("MainFrame.src.fileSize"));

		JPanel pnlFileSize = new JPanel();
		pnlFileSize.setOpaque(false);
		pnlFileSize.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		txtFileSizeFrom = new JTextField();
		InputSupport.allowDigitsOnly(txtFileSizeFrom);
		lblFileSize.setLabelFor(txtFileSizeFrom);
		InputSupport.installLabelFocusAction(lblFileSize, txtFileSizeFrom, LabelFocusBehavior.SELECT_ALL);
		txtFileSizeFrom.setColumns(5);
		txtFileSizeFrom.setHorizontalAlignment(JTextField.RIGHT);

		lblFileSizeTo = new JLabel(Messages.getString("MainFrame.src.fileSizeTo"));

		txtFileSizeTo = new JTextField();
		InputSupport.allowDigitsOnly(txtFileSizeTo);
		txtFileSizeTo.setColumns(5);
		txtFileSizeTo.setHorizontalAlignment(JTextField.RIGHT);

		cmbFileSizeUnit = new JComboBox<>();
		cmbFileSizeUnit.setModel(new DefaultComboBoxModel<>(FileSizeUnit.values()));

		pnlFileSize.add(txtFileSizeFrom);
		pnlFileSize.add(lblFileSizeTo);
		pnlFileSize.add(txtFileSizeTo);
		pnlFileSize.add(cmbFileSizeUnit);

		fieldsView.add(lblFileSize, GridBagSupport.at(0, 3).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		fieldsView.add(pnlFileSize, GridBagSupport.at(1, 3).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());

		lblCreated = new JLabel(Messages.getString("MainFrame.src.created"));

		JPanel pnlCreated = new JPanel();
		pnlCreated.setOpaque(false);
		pnlCreated.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		txtCreatedFrom = newDateTimeField(Messages.getString("MainFrame.src.createdFrom.tooltip"), false);
		lblCreated.setLabelFor(txtCreatedFrom);
		InputSupport.installLabelFocusAction(lblCreated, txtCreatedFrom, LabelFocusBehavior.CARET_START);

		lblCreatedTo = new JLabel(Messages.getString("MainFrame.src.createdTo"));

		txtCreatedTo = newDateTimeField(Messages.getString("MainFrame.src.createdTo.tooltip"), true);

		pnlCreated.add(txtCreatedFrom);
		pnlCreated.add(lblCreatedTo);
		pnlCreated.add(txtCreatedTo);

		fieldsView.add(lblCreated, GridBagSupport.at(0, 4).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		fieldsView.add(pnlCreated, GridBagSupport.at(1, 4).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());

		lblModified = new JLabel(Messages.getString("MainFrame.src.modified"));

		JPanel pnlModified = new JPanel();
		pnlModified.setOpaque(false);
		pnlModified.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		txtModifiedFrom = newDateTimeField(Messages.getString("MainFrame.src.modifiedFrom.tooltip"), false);
		lblModified.setLabelFor(txtModifiedFrom);
		InputSupport.installLabelFocusAction(lblModified, txtModifiedFrom, LabelFocusBehavior.CARET_START);

		lblModifiedTo = new JLabel(Messages.getString("MainFrame.src.modifiedTo"));

		txtModifiedTo = newDateTimeField(Messages.getString("MainFrame.src.modifiedTo.tooltip"), true);

		pnlModified.add(txtModifiedFrom);
		pnlModified.add(lblModifiedTo);
		pnlModified.add(txtModifiedTo);

		fieldsView.add(lblModified, GridBagSupport.at(0, 5).anchor(GridBagConstraints.WEST).insets(0, 0, 0, 5).build());
		fieldsView.add(pnlModified, GridBagSupport.at(1, 5).fill(GridBagConstraints.BOTH).build());

		summaryView = SummaryTextSupport.newSummaryText();
		SummaryTextSupport.installSummaryClickToExpand(summaryView, () -> {
			if (isEnabled()) {
				setExpanded(true);
			}
		});

		cardLayout = new CardLayout();
		setOpaque(false);
		setLayout(cardLayout);
		add(fieldsView, CARD_FIELDS);
		add(summaryView, CARD_SUMMARY);

		installSummaryListeners();
		refreshSummary();
	}

	void setOnExpandedChanged(Consumer<Boolean> listener) {
		onExpandedChanged = listener;
	}

	void setOnContentChanged(Runnable listener) {
		onContentChanged = listener;
	}

	boolean isExpanded() {
		return expanded;
	}

	void setExpanded(boolean expanded) {
		this.expanded = expanded;
		cardLayout.show(this, expanded ? CARD_FIELDS : CARD_SUMMARY);
		setVisible(expanded || !summaryView.getText().isEmpty());
		onExpandedChanged.accept(expanded);
		onContentChanged.run();
	}

	// CardLayout otherwise sizes the container to its largest card regardless of which one is showing,
	// which would keep this panel fields-view-tall even while only the one-line summary is displayed.
	@Override
	public Dimension getPreferredSize() {
		return isPreferredSizeSet() ? super.getPreferredSize() : sizeWithInsets((expanded ? fieldsView : summaryView).getPreferredSize());
	}

	@Override
	public Dimension getMinimumSize() {
		return isMinimumSizeSet() ? super.getMinimumSize() : sizeWithInsets((expanded ? fieldsView : summaryView).getMinimumSize());
	}

	private Dimension sizeWithInsets(Dimension size) {
		Insets insets = getInsets();
		return new Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom);
	}

	FilePatternSyntax selectedFilePatternSyntax() {
		Object selectedItem = cmbFileNamePatternSyntax.getSelectedItem();
		return selectedItem instanceof FilePatternSyntax ? (FilePatternSyntax)selectedItem : FilePatternSyntax.Glob;
	}

	/** Shows the spinning indicator/stop control while a background match count is running; hides it otherwise. */
	void setMatchCountScanning(boolean scanning) {
		matchCountScanning = scanning;
		lblMatchCount.setToolTipText(scanning ? btnMatchCountStop.getToolTipText() : null);
		btnMatchCountStop.setVisible(scanning);
		if (scanning) {
			matchCountSpinnerTimer.start();
		} else {
			matchCountSpinnerTimer.stop();
			matchCountHovered = false;
		}
	}

	/** Swaps the spinning-indicator glyph for the stop glyph while the pointer or focus is over the control. */
	private void setMatchCountHovered(boolean hovered) {
		if (!matchCountScanning) {
			return;
		}
		matchCountHovered = hovered;
		btnMatchCountStop.setText(hovered ? MATCH_COUNT_STOP_GLYPH : MATCH_COUNT_SPINNER_FRAMES[matchCountSpinnerFrame]);
	}

	private void installSummaryListeners() {
		DocumentListener documentListener = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				refreshSummary();
			}

			public void removeUpdate(DocumentEvent e) {
				refreshSummary();
			}

			public void changedUpdate(DocumentEvent e) {
				refreshSummary();
			}
		};
		for (JTextField field : new JTextField[]{txtFileNamePattern, txtFileSizeFrom, txtFileSizeTo, txtCreatedFrom, txtCreatedTo, txtModifiedFrom, txtModifiedTo}) {
			field.getDocument().addDocumentListener(documentListener);
		}
		ChangeListener changeListener = _ -> refreshSummary();
		chkIncludeSubfolders.addChangeListener(changeListener);
		chkIncludeHiddenFiles.addChangeListener(changeListener);
		cmbFileNamePatternSyntax.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				refreshSummary();
			}
		});
		cmbFileSizeUnit.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				refreshSummary();
			}
		});
	}

	private void refreshSummary() {
		String text = computeSummary();
		summaryView.setText(text);
		setVisible(expanded || !text.isEmpty());
		onContentChanged.run();
		if (matchCountEnabled) {
			matchCountTimer.restart();
		}
	}

	/** Called by {@code MainFrame} whenever the source folder text (which it owns) changes. */
	void setSrcFolder(String srcFolderText) {
		this.srcFolderText = srcFolderText;
		if (matchCountEnabled) {
			matchCountTimer.restart();
		}
	}

	void cancelMatchCountScan() {
		if (sourceFileScanner != null) {
			sourceFileScanner.cancel();
		}
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

		// Paths.get("") resolves to the current working directory (a real, existing directory) rather than
		// throwing, so a blank field has to be ruled out explicitly before it reaches Files.isDirectory -
		// otherwise it would scan and show a count for the working directory instead of "no folder".
		Path srcFolder;
		if (srcFolderText.isBlank()) {
			srcFolder = null;
		} else {
			try {
				srcFolder = Paths.get(srcFolderText).normalize();
			} catch (InvalidPathException e) {
				srcFolder = null;
			}
		}

		if (srcFolder == null || !Files.isDirectory(srcFolder)) {
			if (sourceFileScanner != null) {
				sourceFileScanner.cancel();
				sourceFileScanner = null;
			}
			matchCountEnabled = false;
			lastMatchCountStatus = null;
			lblMatchCount.setText(Messages.getString("MainFrame.src.matchCount.prompt"));
			setMatchCountScanning(false);
			return;
		}

		PictoPathFilter pathFilter;
		boolean includeSubfolders = chkIncludeSubfolders.isEnabled() && chkIncludeSubfolders.isSelected();
		try {
			pathFilter = ProcessConditionBuilder.buildPathFilter(sourceFieldsAsInput(), srcFolder, timeZone);
		} catch (Exception e) {
			lblMatchCount.setText("");
			setMatchCountScanning(false);
			return;
		}

		if (sourceFileScanner != null && !sourceFileScanner.srcFolder().equals(srcFolder)) {
			// The source folder changed while a count was enabled for the previous one; that opt-in doesn't
			// carry over to a different folder (it could be much larger), so require an explicit re-click.
			sourceFileScanner.cancel();
			sourceFileScanner = null;
			matchCountEnabled = false;
			lastMatchCountStatus = null;
			lblMatchCount.setText(Messages.getString("MainFrame.src.matchCount.prompt"));
			setMatchCountScanning(false);
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
				lblMatchCount.setText("");
				setMatchCountScanning(false);
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
		lblMatchCount.setText(text);
		setMatchCountScanning(status.state() == SourceFileScanner.MatchCountStatus.State.SCANNING);
		if (status.state() == SourceFileScanner.MatchCountStatus.State.PAUSED) {
			lblMatchCount.setToolTipText(Messages.getString("MainFrame.src.matchCount.resume"));
		}
	}

	/** A minimal {@link ProcessConditionInput}, populated only with the fields {@link ProcessConditionBuilder#buildPathFilter}
	 * actually reads - all of which live on this panel - so match counting doesn't need the full, cross-section
	 * input {@code MainFrame} collects for validation and running. */
	private ProcessConditionInput sourceFieldsAsInput() {
		ProcessConditionInput input = new ProcessConditionInput();
		input.srcFileNamePattern = SummaryTextSupport.fieldText(txtFileNamePattern);
		input.srcFileNamePatternSyntax = selectedFilePatternSyntax();
		input.includeHiddenFiles = chkIncludeHiddenFiles.isEnabled() && chkIncludeHiddenFiles.isSelected();
		input.fileSizeFrom = SummaryTextSupport.fieldText(txtFileSizeFrom);
		input.fileSizeTo = SummaryTextSupport.fieldText(txtFileSizeTo);
		input.fileSizeUnit = (FileSizeUnit)cmbFileSizeUnit.getSelectedItem();
		input.createdFrom = SummaryTextSupport.fieldText(txtCreatedFrom);
		input.createdTo = SummaryTextSupport.fieldText(txtCreatedTo);
		input.modifiedFrom = SummaryTextSupport.fieldText(txtModifiedFrom);
		input.modifiedTo = SummaryTextSupport.fieldText(txtModifiedTo);
		return input;
	}

	private String computeSummary() {
		List<String> items = new ArrayList<>();
		String pattern = SummaryTextSupport.fieldText(txtFileNamePattern);
		if (!pattern.isEmpty()) {
			if (selectedFilePatternSyntax() == FilePatternSyntax.Regex) {
				pattern += " (" + FilePatternSyntax.Regex + ")";
			}
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.src.fileNamePattern"), pattern));
		}
		if (chkIncludeSubfolders.isEnabled() && chkIncludeSubfolders.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.src.includeSubfolders")));
		}
		if (chkIncludeHiddenFiles.isEnabled() && chkIncludeHiddenFiles.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.src.includeHiddenFiles")));
		}
		String fileSizeFrom = SummaryTextSupport.fieldText(txtFileSizeFrom);
		String fileSizeTo = SummaryTextSupport.fieldText(txtFileSizeTo);
		if (!fileSizeFrom.isEmpty() || !fileSizeTo.isEmpty()) {
			String range = SummaryTextSupport.rangeText(fileSizeFrom, fileSizeTo);
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.src.fileSize"), range + " " + cmbFileSizeUnit.getSelectedItem()));
		}
		String createdFrom = SummaryTextSupport.dateFieldText(SummaryTextSupport.fieldText(txtCreatedFrom));
		String createdTo = SummaryTextSupport.dateFieldText(SummaryTextSupport.fieldText(txtCreatedTo));
		if (!createdFrom.isEmpty() || !createdTo.isEmpty()) {
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.src.created"), SummaryTextSupport.rangeText(createdFrom, createdTo)));
		}
		String modifiedFrom = SummaryTextSupport.dateFieldText(SummaryTextSupport.fieldText(txtModifiedFrom));
		String modifiedTo = SummaryTextSupport.dateFieldText(SummaryTextSupport.fieldText(txtModifiedTo));
		if (!modifiedFrom.isEmpty() || !modifiedTo.isEmpty()) {
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.src.modified"), SummaryTextSupport.rangeText(modifiedFrom, modifiedTo)));
		}
		return SummaryTextSupport.joinOptionsSummary(items);
	}

	private static JFormattedTextField newDateTimeField(String tooltip, boolean endOfRange) {
		JFormattedTextField textField = new JFormattedTextField(InputSupport.newMaskFormatter(DateTimeText.MASK_PATTERN));
		textField.setColumns(20);
		textField.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setToolTipText(tooltip);
		InputSupport.installDateTimeInputPopup(textField, endOfRange);
		InputSupport.installDateTimeNormalizeOnFocusLost(textField);
		textField.setFocusLostBehavior(JFormattedTextField.COMMIT);
		return textField;
	}
}
