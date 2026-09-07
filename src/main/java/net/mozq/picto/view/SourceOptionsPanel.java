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

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.picto.enums.FileSizeUnit;

class SourceOptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	final JLabel filePatternLabel;
	final JTextField filePatternTextField;
	final JComboBox<FilePatternSyntax> filePatternSyntaxComboBox;
	final JCheckBox containsSubsCheckBox;
	final JCheckBox containsHiddensCheckBox;
	final JLabel fileSizeRangeLabel;
	final JTextField fileSizeRangeFromTextField;
	final JLabel fileSizeRangeToLabel;
	final JTextField fileSizeRangeToTextField;
	final JComboBox<FileSizeUnit> fileSizeUnitComboBox;
	final JLabel creationTimeRangeLabel;
	final JFormattedTextField creationTimeRangeFromTextField;
	final JLabel creationTimeRangeToLabel;
	final JFormattedTextField creationTimeRangeToTextField;
	final JLabel modifiedTimeRangeLabel;
	final JFormattedTextField modifiedTimeRangeFromTextField;
	final JLabel modifiedTimeRangeToLabel;
	final JFormattedTextField modifiedTimeRangeToTextField;

	SourceOptionsPanel(int inlineHgap, int inlineVgap) {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(layout);

		filePatternLabel = new JLabel(Messages.getString("MainFrame.filePattern"));
		GridBagConstraints filePatternLabelConstraints = new GridBagConstraints();
		filePatternLabelConstraints.anchor = GridBagConstraints.WEST;
		filePatternLabelConstraints.insets = new Insets(0, 0, 5, 5);
		filePatternLabelConstraints.gridx = 0;
		filePatternLabelConstraints.gridy = 0;
		add(filePatternLabel, filePatternLabelConstraints);

		JPanel filePatternPanel = new JPanel();
		filePatternPanel.setOpaque(false);
		GridBagConstraints filePatternPanelConstraints = new GridBagConstraints();
		filePatternPanelConstraints.fill = GridBagConstraints.BOTH;
		filePatternPanelConstraints.insets = new Insets(0, 0, 5, 0);
		filePatternPanelConstraints.gridx = 1;
		filePatternPanelConstraints.gridy = 0;
		add(filePatternPanel, filePatternPanelConstraints);
		GridBagLayout filePatternLayout = new GridBagLayout();
		filePatternLayout.columnWidths = new int[]{0, 0, 0};
		filePatternLayout.rowHeights = new int[]{0, 0};
		filePatternLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		filePatternLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		filePatternPanel.setLayout(filePatternLayout);

		filePatternTextField = new JTextField();
		filePatternTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, UIManager.getIcon("FileView.fileIcon"));
		filePatternLabel.setLabelFor(filePatternTextField);
		InputSupport.installLabelFocusAction(filePatternLabel, filePatternTextField, LabelFocusBehavior.CARET_END);
		GridBagConstraints filePatternTextFieldConstraints = new GridBagConstraints();
		filePatternTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		filePatternTextFieldConstraints.insets = new Insets(0, 0, 0, 5);
		filePatternTextFieldConstraints.gridx = 0;
		filePatternTextFieldConstraints.gridy = 0;
		filePatternPanel.add(filePatternTextField, filePatternTextFieldConstraints);
		filePatternTextField.setColumns(10);

		filePatternSyntaxComboBox = new JComboBox<>();
		filePatternSyntaxComboBox.setModel(new DefaultComboBoxModel<>(FilePatternSyntax.values()));
		FileNamePatternPopup fileNamePatternPopup = new FileNamePatternPopup(filePatternTextField, () -> selectedFilePatternSyntax().isRegex());
		filePatternSyntaxComboBox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				fileNamePatternPopup.refresh();
			}
		});
		GridBagConstraints filePatternSyntaxComboBoxConstraints = new GridBagConstraints();
		filePatternSyntaxComboBoxConstraints.anchor = GridBagConstraints.WEST;
		filePatternSyntaxComboBoxConstraints.gridx = 1;
		filePatternSyntaxComboBoxConstraints.gridy = 0;
		filePatternPanel.add(filePatternSyntaxComboBox, filePatternSyntaxComboBoxConstraints);

		containsSubsCheckBox = new JCheckBox(Messages.getString("MainFrame.containsSubs"));
		GridBagConstraints containsSubsConstraints = new GridBagConstraints();
		containsSubsConstraints.anchor = GridBagConstraints.WEST;
		containsSubsConstraints.insets = new Insets(0, 0, 5, 0);
		containsSubsConstraints.gridx = 1;
		containsSubsConstraints.gridy = 1;
		add(containsSubsCheckBox, containsSubsConstraints);

		containsHiddensCheckBox = new JCheckBox(Messages.getString("MainFrame.containsHiddens"));
		GridBagConstraints containsHiddensConstraints = new GridBagConstraints();
		containsHiddensConstraints.fill = GridBagConstraints.BOTH;
		containsHiddensConstraints.insets = new Insets(0, 0, 5, 0);
		containsHiddensConstraints.gridx = 1;
		containsHiddensConstraints.gridy = 2;
		add(containsHiddensCheckBox, containsHiddensConstraints);

		fileSizeRangeLabel = new JLabel(Messages.getString("MainFrame.fileSizeRange"));
		GridBagConstraints fileSizeRangeLabelConstraints = new GridBagConstraints();
		fileSizeRangeLabelConstraints.anchor = GridBagConstraints.WEST;
		fileSizeRangeLabelConstraints.insets = new Insets(0, 0, 5, 5);
		fileSizeRangeLabelConstraints.gridx = 0;
		fileSizeRangeLabelConstraints.gridy = 3;
		add(fileSizeRangeLabel, fileSizeRangeLabelConstraints);

		JPanel fileSizeRangePanel = new JPanel();
		fileSizeRangePanel.setOpaque(false);
		GridBagConstraints fileSizeRangePanelConstraints = new GridBagConstraints();
		fileSizeRangePanelConstraints.fill = GridBagConstraints.BOTH;
		fileSizeRangePanelConstraints.insets = new Insets(0, 0, 5, 0);
		fileSizeRangePanelConstraints.gridx = 1;
		fileSizeRangePanelConstraints.gridy = 3;
		add(fileSizeRangePanel, fileSizeRangePanelConstraints);
		fileSizeRangePanel.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		fileSizeRangeFromTextField = new JTextField();
		InputSupport.allowDigitsOnly(fileSizeRangeFromTextField);
		fileSizeRangeLabel.setLabelFor(fileSizeRangeFromTextField);
		InputSupport.installLabelFocusAction(fileSizeRangeLabel, fileSizeRangeFromTextField, LabelFocusBehavior.SELECT_ALL);
		fileSizeRangeFromTextField.setColumns(5);
		fileSizeRangeFromTextField.setHorizontalAlignment(JTextField.RIGHT);
		fileSizeRangePanel.add(fileSizeRangeFromTextField);

		fileSizeRangeToLabel = new JLabel(Messages.getString("MainFrame.fileSizeRangeTo"));
		fileSizeRangePanel.add(fileSizeRangeToLabel);

		fileSizeRangeToTextField = new JTextField();
		InputSupport.allowDigitsOnly(fileSizeRangeToTextField);
		fileSizeRangeToTextField.setColumns(5);
		fileSizeRangeToTextField.setHorizontalAlignment(JTextField.RIGHT);
		fileSizeRangePanel.add(fileSizeRangeToTextField);

		fileSizeUnitComboBox = new JComboBox<>();
		fileSizeRangePanel.add(fileSizeUnitComboBox);
		fileSizeUnitComboBox.setModel(new DefaultComboBoxModel<>(FileSizeUnit.values()));

		creationTimeRangeLabel = new JLabel(Messages.getString("MainFrame.creationTimeRange"));
		GridBagConstraints creationTimeLabelConstraints = new GridBagConstraints();
		creationTimeLabelConstraints.anchor = GridBagConstraints.WEST;
		creationTimeLabelConstraints.insets = new Insets(0, 0, 5, 5);
		creationTimeLabelConstraints.gridx = 0;
		creationTimeLabelConstraints.gridy = 4;
		add(creationTimeRangeLabel, creationTimeLabelConstraints);

		JPanel creationTimeRangePanel = new JPanel();
		creationTimeRangePanel.setOpaque(false);
		GridBagConstraints creationTimePanelConstraints = new GridBagConstraints();
		creationTimePanelConstraints.fill = GridBagConstraints.BOTH;
		creationTimePanelConstraints.insets = new Insets(0, 0, 5, 0);
		creationTimePanelConstraints.gridx = 1;
		creationTimePanelConstraints.gridy = 4;
		add(creationTimeRangePanel, creationTimePanelConstraints);
		creationTimeRangePanel.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		creationTimeRangeFromTextField = newDateTimeField(Messages.getString("MainFrame.creationTimeRangeFrom.tooltip"), false);
		creationTimeRangeLabel.setLabelFor(creationTimeRangeFromTextField);
		InputSupport.installLabelFocusAction(creationTimeRangeLabel, creationTimeRangeFromTextField, LabelFocusBehavior.CARET_START);
		creationTimeRangePanel.add(creationTimeRangeFromTextField);

		creationTimeRangeToLabel = new JLabel(Messages.getString("MainFrame.creationTimeRangeTo"));
		creationTimeRangePanel.add(creationTimeRangeToLabel);

		creationTimeRangeToTextField = newDateTimeField(Messages.getString("MainFrame.creationTimeRangeTo.tooltip"), true);
		creationTimeRangePanel.add(creationTimeRangeToTextField);

		modifiedTimeRangeLabel = new JLabel(Messages.getString("MainFrame.modifiedTimeRange"));
		GridBagConstraints modifiedTimeLabelConstraints = new GridBagConstraints();
		modifiedTimeLabelConstraints.anchor = GridBagConstraints.WEST;
		modifiedTimeLabelConstraints.insets = new Insets(0, 0, 0, 5);
		modifiedTimeLabelConstraints.gridx = 0;
		modifiedTimeLabelConstraints.gridy = 5;
		add(modifiedTimeRangeLabel, modifiedTimeLabelConstraints);

		JPanel modifiedTimeRangePanel = new JPanel();
		modifiedTimeRangePanel.setOpaque(false);
		GridBagConstraints modifiedTimePanelConstraints = new GridBagConstraints();
		modifiedTimePanelConstraints.fill = GridBagConstraints.BOTH;
		modifiedTimePanelConstraints.gridx = 1;
		modifiedTimePanelConstraints.gridy = 5;
		add(modifiedTimeRangePanel, modifiedTimePanelConstraints);
		modifiedTimeRangePanel.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		modifiedTimeRangeFromTextField = newDateTimeField(Messages.getString("MainFrame.modifiedTimeRangeFrom.tooltip"), false);
		modifiedTimeRangeLabel.setLabelFor(modifiedTimeRangeFromTextField);
		InputSupport.installLabelFocusAction(modifiedTimeRangeLabel, modifiedTimeRangeFromTextField, LabelFocusBehavior.CARET_START);
		modifiedTimeRangePanel.add(modifiedTimeRangeFromTextField);

		modifiedTimeRangeToLabel = new JLabel(Messages.getString("MainFrame.modifiedTimeRangeTo"));
		modifiedTimeRangePanel.add(modifiedTimeRangeToLabel);

		modifiedTimeRangeToTextField = newDateTimeField(Messages.getString("MainFrame.modifiedTimeRangeTo.tooltip"), true);
		modifiedTimeRangePanel.add(modifiedTimeRangeToTextField);
	}

	FilePatternSyntax selectedFilePatternSyntax() {
		Object selectedItem = filePatternSyntaxComboBox.getSelectedItem();
		return selectedItem instanceof FilePatternSyntax ? (FilePatternSyntax)selectedItem : FilePatternSyntax.GLOB;
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
