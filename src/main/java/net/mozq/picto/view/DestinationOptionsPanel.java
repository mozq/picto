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
	final JCheckBox chkCheckFileDigest;

	DestinationOptionsPanel() {
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(layout);

		lblSubFilePathPattern = new JLabel(Messages.getString("MainFrame.dest.subFilePathPattern"));

		txtSubFilePathPattern = new JTextField();
		txtSubFilePathPattern.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		txtSubFilePathPattern.putClientProperty(
				FlatClientProperties.TEXT_FIELD_LEADING_ICON,
				UIManager.getIcon("FileView.fileIcon"));
		lblSubFilePathPattern.setLabelFor(txtSubFilePathPattern);
		InputSupport.installLabelFocusAction(lblSubFilePathPattern, txtSubFilePathPattern, LabelFocusBehavior.CARET_END);
		txtSubFilePathPattern.setColumns(10);
		new SubfolderTemplatePopup(txtSubFilePathPattern);

		add(lblSubFilePathPattern, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		add(txtSubFilePathPattern, GridBagSupport.at(1, 0).fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 5, 0).build());

		lblExistingFileMethod = new JLabel(Messages.getString("MainFrame.dest.existingFileMethod"));

		cmbExistingFileMethod = new JComboBox<>();
		lblExistingFileMethod.setLabelFor(cmbExistingFileMethod);
		InputSupport.installLabelFocusAction(lblExistingFileMethod, cmbExistingFileMethod, LabelFocusBehavior.FOCUS_ONLY);
		cmbExistingFileMethod.setModel(new DefaultComboBoxModel<>(ExistingFileMethod.values()));

		add(lblExistingFileMethod, GridBagSupport.at(0, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		add(cmbExistingFileMethod, GridBagSupport.at(1, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 0).build());

		lblValidateFile = new JLabel(Messages.getString("MainFrame.dest.validateFile"));

		chkCheckFileDigest = new JCheckBox(Messages.getString("MainFrame.dest.checkFileDigest"));
		lblValidateFile.setLabelFor(chkCheckFileDigest);
		InputSupport.installLabelFocusAction(lblValidateFile, chkCheckFileDigest, LabelFocusBehavior.FOCUS_ONLY);

		add(lblValidateFile, GridBagSupport.at(0, 2).anchor(GridBagConstraints.WEST).insets(0, 0, 0, 5).build());
		add(chkCheckFileDigest, GridBagSupport.at(1, 2).anchor(GridBagConstraints.WEST).build());
	}
}
