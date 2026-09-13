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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.FileSizeUnit;

class SourceOptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String[] MATCH_COUNT_SPINNER_FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
	private static final int MATCH_COUNT_SPINNER_INTERVAL_MS = 80;
	private static final String MATCH_COUNT_STOP_GLYPH = "■";

	final JLabel lblMatchCount;
	final JButton btnMatchCountStop;
	private final Timer matchCountSpinnerTimer;
	private int matchCountSpinnerFrame;
	private boolean matchCountHovered;
	private boolean matchCountScanning;
	final JLabel lblFileNamePattern;
	final JTextField txtFileNamePattern;
	final JComboBox<FilePatternSyntax> cmbFileNamePatternSyntax;
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

	SourceOptionsPanel(int inlineHgap, int inlineVgap) {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(layout);

		// No horizontal gap: a gap here is dead space that belongs to neither the label nor the stop
		// button, so hovering into it drops out of both of their mouseEntered/mouseExited pairs and
		// flips the stop control back to the spinner while the pointer is still over this area.
		JPanel matchCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		matchCountPanel.setOpaque(false);
		add(matchCountPanel, GridBagSupport.at(0, 0).fill(GridBagConstraints.HORIZONTAL).gridwidth(2).insets(0, 0, 5, 0).build());

		lblMatchCount = new JLabel(Messages.getString("MainFrame.src.matchCount.prompt"));
		lblMatchCount.setFont(lblMatchCount.getFont().deriveFont(lblMatchCount.getFont().getSize2D() - 2f));
		lblMatchCount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		matchCountPanel.add(lblMatchCount);

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
		matchCountPanel.add(btnMatchCountStop);

		matchCountSpinnerTimer = new Timer(MATCH_COUNT_SPINNER_INTERVAL_MS, _ -> {
			matchCountSpinnerFrame = (matchCountSpinnerFrame + 1) % MATCH_COUNT_SPINNER_FRAMES.length;
			if (!matchCountHovered) {
				btnMatchCountStop.setText(MATCH_COUNT_SPINNER_FRAMES[matchCountSpinnerFrame]);
			}
		});

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
		btnMatchCountStop.addMouseListener(matchCountHoverListener);
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

		lblFileNamePattern = new JLabel(Messages.getString("MainFrame.src.fileNamePattern"));
		add(lblFileNamePattern, GridBagSupport.at(0, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());

		JPanel pnlFileNamePattern = new JPanel();
		pnlFileNamePattern.setOpaque(false);
		add(pnlFileNamePattern, GridBagSupport.at(1, 1).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());
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
		pnlFileNamePattern.add(txtFileNamePattern, GridBagSupport.at(0, 0).fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 0, 5).build());
		txtFileNamePattern.setColumns(10);

		cmbFileNamePatternSyntax = new JComboBox<>();
		cmbFileNamePatternSyntax.setModel(new DefaultComboBoxModel<>(FilePatternSyntax.values()));
		FileNamePatternPopup fileNamePatternPopup = new FileNamePatternPopup(txtFileNamePattern, this::selectedFilePatternSyntax);
		cmbFileNamePatternSyntax.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				fileNamePatternPopup.refresh();
			}
		});
		pnlFileNamePattern.add(cmbFileNamePatternSyntax, GridBagSupport.at(1, 0).anchor(GridBagConstraints.WEST).build());

		chkIncludeSubfolders = new JCheckBox(Messages.getString("MainFrame.src.includeSubfolders"));
		add(chkIncludeSubfolders, GridBagSupport.at(1, 2).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 0).build());

		chkIncludeHiddenFiles = new JCheckBox(Messages.getString("MainFrame.src.includeHiddenFiles"));
		add(chkIncludeHiddenFiles, GridBagSupport.at(1, 3).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());

		lblFileSize = new JLabel(Messages.getString("MainFrame.src.fileSize"));
		add(lblFileSize, GridBagSupport.at(0, 4).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());

		JPanel pnlFileSize = new JPanel();
		pnlFileSize.setOpaque(false);
		add(pnlFileSize, GridBagSupport.at(1, 4).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());
		pnlFileSize.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		txtFileSizeFrom = new JTextField();
		InputSupport.allowDigitsOnly(txtFileSizeFrom);
		lblFileSize.setLabelFor(txtFileSizeFrom);
		InputSupport.installLabelFocusAction(lblFileSize, txtFileSizeFrom, LabelFocusBehavior.SELECT_ALL);
		txtFileSizeFrom.setColumns(5);
		txtFileSizeFrom.setHorizontalAlignment(JTextField.RIGHT);
		pnlFileSize.add(txtFileSizeFrom);

		lblFileSizeTo = new JLabel(Messages.getString("MainFrame.src.fileSizeTo"));
		pnlFileSize.add(lblFileSizeTo);

		txtFileSizeTo = new JTextField();
		InputSupport.allowDigitsOnly(txtFileSizeTo);
		txtFileSizeTo.setColumns(5);
		txtFileSizeTo.setHorizontalAlignment(JTextField.RIGHT);
		pnlFileSize.add(txtFileSizeTo);

		cmbFileSizeUnit = new JComboBox<>();
		pnlFileSize.add(cmbFileSizeUnit);
		cmbFileSizeUnit.setModel(new DefaultComboBoxModel<>(FileSizeUnit.values()));

		lblCreated = new JLabel(Messages.getString("MainFrame.src.created"));
		add(lblCreated, GridBagSupport.at(0, 5).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());

		JPanel pnlCreated = new JPanel();
		pnlCreated.setOpaque(false);
		add(pnlCreated, GridBagSupport.at(1, 5).fill(GridBagConstraints.BOTH).insets(0, 0, 5, 0).build());
		pnlCreated.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		txtCreatedFrom = newDateTimeField(Messages.getString("MainFrame.src.createdFrom.tooltip"), false);
		lblCreated.setLabelFor(txtCreatedFrom);
		InputSupport.installLabelFocusAction(lblCreated, txtCreatedFrom, LabelFocusBehavior.CARET_START);
		pnlCreated.add(txtCreatedFrom);

		lblCreatedTo = new JLabel(Messages.getString("MainFrame.src.createdTo"));
		pnlCreated.add(lblCreatedTo);

		txtCreatedTo = newDateTimeField(Messages.getString("MainFrame.src.createdTo.tooltip"), true);
		pnlCreated.add(txtCreatedTo);

		lblModified = new JLabel(Messages.getString("MainFrame.src.modified"));
		add(lblModified, GridBagSupport.at(0, 6).anchor(GridBagConstraints.WEST).insets(0, 0, 0, 5).build());

		JPanel pnlModified = new JPanel();
		pnlModified.setOpaque(false);
		add(pnlModified, GridBagSupport.at(1, 6).fill(GridBagConstraints.BOTH).build());
		pnlModified.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		txtModifiedFrom = newDateTimeField(Messages.getString("MainFrame.src.modifiedFrom.tooltip"), false);
		lblModified.setLabelFor(txtModifiedFrom);
		InputSupport.installLabelFocusAction(lblModified, txtModifiedFrom, LabelFocusBehavior.CARET_START);
		pnlModified.add(txtModifiedFrom);

		lblModifiedTo = new JLabel(Messages.getString("MainFrame.src.modifiedTo"));
		pnlModified.add(lblModifiedTo);

		txtModifiedTo = newDateTimeField(Messages.getString("MainFrame.src.modifiedTo.tooltip"), true);
		pnlModified.add(txtModifiedTo);
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

	private static JFormattedTextField newDateTimeField(String tooltip, boolean endOfRange) {
		JFormattedTextField textField = new JFormattedTextField(InputSupport.newMaskFormatter(DateTimeText.MASK_PATTERN));
		textField.setColumns(20);
		textField.setFont(new Font("Monospaced", Font.PLAIN, 13));
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setToolTipText(tooltip);
		InputSupport.installDateTimeInputPopup(textField, endOfRange);
		textField.setFocusLostBehavior(JFormattedTextField.COMMIT);
		return textField;
	}
}
