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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.picto.enums.ExistingFileMethod;

class DestinationOptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	final JLabel destSubPathPatternLabel;
	final JTextField destSubPathPatternTextField;
	final JLabel existingFileMethodLabel;
	final JComboBox<ExistingFileMethod> existingFileMethodComboBox;
	final JLabel validateFileLabel;
	final JCheckBox checkFileDigestCheckBox;

	DestinationOptionsPanel() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(layout);

		destSubPathPatternLabel = new JLabel(Messages.getString("MainFrame.destSubPathPattern"));
		GridBagConstraints destSubPathPatternLabelConstraints = new GridBagConstraints();
		destSubPathPatternLabelConstraints.anchor = GridBagConstraints.WEST;
		destSubPathPatternLabelConstraints.insets = new Insets(0, 0, 5, 5);
		destSubPathPatternLabelConstraints.gridx = 0;
		destSubPathPatternLabelConstraints.gridy = 0;
		add(destSubPathPatternLabel, destSubPathPatternLabelConstraints);

		destSubPathPatternTextField = new JTextField();
		destSubPathPatternTextField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		destSubPathPatternTextField.putClientProperty(
				FlatClientProperties.TEXT_FIELD_LEADING_ICON,
				UIManager.getIcon("FileView.fileIcon"));
		destSubPathPatternLabel.setLabelFor(destSubPathPatternTextField);
		InputSupport.installLabelFocusAction(destSubPathPatternLabel, destSubPathPatternTextField, LabelFocusBehavior.CARET_END);
		destSubPathPatternTextField.setColumns(10);
		new SubfolderTemplatePopup(destSubPathPatternTextField);
		GridBagConstraints destSubPathPatternTextFieldConstraints = new GridBagConstraints();
		destSubPathPatternTextFieldConstraints.insets = new Insets(0, 0, 5, 0);
		destSubPathPatternTextFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		destSubPathPatternTextFieldConstraints.gridx = 1;
		destSubPathPatternTextFieldConstraints.gridy = 0;
		add(destSubPathPatternTextField, destSubPathPatternTextFieldConstraints);

		existingFileMethodLabel = new JLabel(Messages.getString("MainFrame.existingFileMethod"));
		GridBagConstraints existingFileMethodLabelConstraints = new GridBagConstraints();
		existingFileMethodLabelConstraints.anchor = GridBagConstraints.WEST;
		existingFileMethodLabelConstraints.insets = new Insets(0, 0, 5, 5);
		existingFileMethodLabelConstraints.gridx = 0;
		existingFileMethodLabelConstraints.gridy = 1;
		add(existingFileMethodLabel, existingFileMethodLabelConstraints);

		existingFileMethodComboBox = new JComboBox<>();
		existingFileMethodLabel.setLabelFor(existingFileMethodComboBox);
		InputSupport.installLabelFocusAction(existingFileMethodLabel, existingFileMethodComboBox, LabelFocusBehavior.FOCUS_ONLY);
		GridBagConstraints existingFileMethodComboBoxConstraints = new GridBagConstraints();
		existingFileMethodComboBoxConstraints.insets = new Insets(0, 0, 5, 0);
		existingFileMethodComboBoxConstraints.anchor = GridBagConstraints.WEST;
		existingFileMethodComboBoxConstraints.gridx = 1;
		existingFileMethodComboBoxConstraints.gridy = 1;
		existingFileMethodComboBox.setModel(new DefaultComboBoxModel<>(ExistingFileMethod.values()));
		add(existingFileMethodComboBox, existingFileMethodComboBoxConstraints);

		validateFileLabel = new JLabel(Messages.getString("MainFrame.validateFile"));
		GridBagConstraints validateFileLabelConstraints = new GridBagConstraints();
		validateFileLabelConstraints.anchor = GridBagConstraints.WEST;
		validateFileLabelConstraints.insets = new Insets(0, 0, 0, 5);
		validateFileLabelConstraints.gridx = 0;
		validateFileLabelConstraints.gridy = 2;
		add(validateFileLabel, validateFileLabelConstraints);

		checkFileDigestCheckBox = new JCheckBox(Messages.getString("MainFrame.checkFileDigest"));
		validateFileLabel.setLabelFor(checkFileDigestCheckBox);
		InputSupport.installLabelFocusAction(validateFileLabel, checkFileDigestCheckBox, LabelFocusBehavior.FOCUS_ONLY);
		GridBagConstraints checkFileDigestConstraints = new GridBagConstraints();
		checkFileDigestConstraints.anchor = GridBagConstraints.WEST;
		checkFileDigestConstraints.gridx = 1;
		checkFileDigestConstraints.gridy = 2;
		add(checkFileDigestCheckBox, checkFileDigestConstraints);
	}
}
