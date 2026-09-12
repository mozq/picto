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

	final JLabel lblSubFilePathPattern;
	final JTextField txtSubFilePathPattern;
	final JLabel lblExistingFileMethod;
	final JComboBox<ExistingFileMethod> cmbExistingFileMethod;
	final JLabel lblValidateFile;
	final JCheckBox chkCompareFileDigest;

	DestinationOptionsPanel() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(layout);

		lblSubFilePathPattern = new JLabel(Messages.getString("MainFrame.dest.subFilePathPattern"));
		GridBagConstraints lblSubFilePathPatternConstraints = new GridBagConstraints();
		lblSubFilePathPatternConstraints.anchor = GridBagConstraints.WEST;
		lblSubFilePathPatternConstraints.insets = new Insets(0, 0, 5, 5);
		lblSubFilePathPatternConstraints.gridx = 0;
		lblSubFilePathPatternConstraints.gridy = 0;
		add(lblSubFilePathPattern, lblSubFilePathPatternConstraints);

		txtSubFilePathPattern = new JTextField();
		txtSubFilePathPattern.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		txtSubFilePathPattern.putClientProperty(
				FlatClientProperties.TEXT_FIELD_LEADING_ICON,
				UIManager.getIcon("FileView.fileIcon"));
		lblSubFilePathPattern.setLabelFor(txtSubFilePathPattern);
		InputSupport.installLabelFocusAction(lblSubFilePathPattern, txtSubFilePathPattern, LabelFocusBehavior.CARET_END);
		txtSubFilePathPattern.setColumns(10);
		new SubfolderTemplatePopup(txtSubFilePathPattern);
		GridBagConstraints txtSubFilePathPatternConstraints = new GridBagConstraints();
		txtSubFilePathPatternConstraints.insets = new Insets(0, 0, 5, 0);
		txtSubFilePathPatternConstraints.fill = GridBagConstraints.HORIZONTAL;
		txtSubFilePathPatternConstraints.gridx = 1;
		txtSubFilePathPatternConstraints.gridy = 0;
		add(txtSubFilePathPattern, txtSubFilePathPatternConstraints);

		lblExistingFileMethod = new JLabel(Messages.getString("MainFrame.dest.existingFileMethod"));
		GridBagConstraints lblExistingFileMethodConstraints = new GridBagConstraints();
		lblExistingFileMethodConstraints.anchor = GridBagConstraints.WEST;
		lblExistingFileMethodConstraints.insets = new Insets(0, 0, 5, 5);
		lblExistingFileMethodConstraints.gridx = 0;
		lblExistingFileMethodConstraints.gridy = 1;
		add(lblExistingFileMethod, lblExistingFileMethodConstraints);

		cmbExistingFileMethod = new JComboBox<>();
		lblExistingFileMethod.setLabelFor(cmbExistingFileMethod);
		InputSupport.installLabelFocusAction(lblExistingFileMethod, cmbExistingFileMethod, LabelFocusBehavior.FOCUS_ONLY);
		GridBagConstraints cmbExistingFileMethodConstraints = new GridBagConstraints();
		cmbExistingFileMethodConstraints.insets = new Insets(0, 0, 5, 0);
		cmbExistingFileMethodConstraints.anchor = GridBagConstraints.WEST;
		cmbExistingFileMethodConstraints.gridx = 1;
		cmbExistingFileMethodConstraints.gridy = 1;
		cmbExistingFileMethod.setModel(new DefaultComboBoxModel<>(ExistingFileMethod.values()));
		add(cmbExistingFileMethod, cmbExistingFileMethodConstraints);

		lblValidateFile = new JLabel(Messages.getString("MainFrame.dest.validateFile"));
		GridBagConstraints lblValidateFileConstraints = new GridBagConstraints();
		lblValidateFileConstraints.anchor = GridBagConstraints.WEST;
		lblValidateFileConstraints.insets = new Insets(0, 0, 0, 5);
		lblValidateFileConstraints.gridx = 0;
		lblValidateFileConstraints.gridy = 2;
		add(lblValidateFile, lblValidateFileConstraints);

		chkCompareFileDigest = new JCheckBox(Messages.getString("MainFrame.dest.compareFileDigest"));
		lblValidateFile.setLabelFor(chkCompareFileDigest);
		InputSupport.installLabelFocusAction(lblValidateFile, chkCompareFileDigest, LabelFocusBehavior.FOCUS_ONLY);
		GridBagConstraints chkCompareFileDigestConstraints = new GridBagConstraints();
		chkCompareFileDigestConstraints.anchor = GridBagConstraints.WEST;
		chkCompareFileDigestConstraints.gridx = 1;
		chkCompareFileDigestConstraints.gridy = 2;
		add(chkCompareFileDigest, chkCompareFileDigestConstraints);
	}
}
