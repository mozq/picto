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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;

class ChangesFileDatePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	final JLabel lblTargetDate;
	final JCheckBox chkCreationDate;
	final JCheckBox chkModifiedDate;
	final JCheckBox chkAccessDate;
	final JCheckBox chkExifDate;
	final JLabel lblBaseDate;
	final JComboBox<DateType> cmbBaseDate;
	final JFormattedTextField txtCustomBaseDate;
	final JLabel lblAdjustment;
	final JComboBox<DateModType> cmbAdjustmentType;
	final JTextField txtAdjustmentYears;
	final JLabel lblYearMonthSeparator;
	final JTextField txtAdjustmentMonths;
	final JLabel lblMonthDaySeparator;
	final JTextField txtAdjustmentDays;
	final JLabel lblDayHourSeparator;
	final JTextField txtAdjustmentHours;
	final JLabel lblHourMinuteSeparator;
	final JTextField txtAdjustmentMinutes;
	final JLabel lblMinuteSecondSeparator;
	final JTextField txtAdjustmentSeconds;

	ChangesFileDatePanel(int sectionPadding, int inlineHgap, int inlineVgap, Runnable layoutChanged) {
		setBorder(new EmptyBorder(sectionPadding, sectionPadding, sectionPadding, sectionPadding));
		GridBagLayout fileDateLayout = new GridBagLayout();
		fileDateLayout.columnWidths = new int[]{0, 0, 0};
		fileDateLayout.rowHeights = new int[]{0, 0, 0, 0};
		fileDateLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		fileDateLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(fileDateLayout);

		lblTargetDate = new JLabel(Messages.getString("MainFrame.changes.filedate.targetDate"));

		JPanel pnlTargetDate = new JPanel();
		FlowLayout targetDateLayout = (FlowLayout)pnlTargetDate.getLayout();
		targetDateLayout.setVgap(inlineVgap);
		targetDateLayout.setHgap(inlineHgap);
		targetDateLayout.setAlignment(FlowLayout.LEFT);

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateEnabledState();
			}
		};
		chkCreationDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.creationDate"));
		lblTargetDate.setLabelFor(chkCreationDate);
		InputSupport.installLabelFocusAction(lblTargetDate, chkCreationDate, LabelFocusBehavior.FOCUS_ONLY);
		chkCreationDate.addChangeListener(changeListener);

		chkModifiedDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.modifiedDate"));
		chkModifiedDate.addChangeListener(changeListener);

		chkAccessDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.accessDate"));
		chkAccessDate.addChangeListener(changeListener);

		chkExifDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.exifDate"));
		chkExifDate.addChangeListener(changeListener);

		pnlTargetDate.add(chkCreationDate);
		pnlTargetDate.add(chkModifiedDate);
		pnlTargetDate.add(chkAccessDate);
		pnlTargetDate.add(chkExifDate);

		add(lblTargetDate, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		add(pnlTargetDate, GridBagSupport.at(1, 0).fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 5, 0).build());

		lblBaseDate = new JLabel(Messages.getString("MainFrame.changes.filedate.baseDateType"));

		JPanel pnlBaseDate = new JPanel();
		pnlBaseDate.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		cmbBaseDate = new JComboBox<>();
		lblBaseDate.setLabelFor(cmbBaseDate);
		InputSupport.installLabelFocusAction(lblBaseDate, cmbBaseDate, LabelFocusBehavior.FOCUS_ONLY);
		cmbBaseDate.setModel(new DefaultComboBoxModel<>(DateType.values()));

		txtCustomBaseDate = new JFormattedTextField(InputSupport.newMaskFormatter(DateTimeText.MASK_PATTERN));
		InputSupport.configureDisabledBackground(txtCustomBaseDate);
		txtCustomBaseDate.setFont(new Font("Monospaced", Font.PLAIN, 13));
		txtCustomBaseDate.setHorizontalAlignment(JTextField.CENTER);
		txtCustomBaseDate.setColumns(20);
		txtCustomBaseDate.setVisible(false);
		InputSupport.installDateTimeInputPopup(txtCustomBaseDate, false);
		InputSupport.installDateTimeNormalizeOnFocusLost(txtCustomBaseDate);
		txtCustomBaseDate.setFocusLostBehavior(JFormattedTextField.COMMIT);

		cmbBaseDate.addItemListener(e -> {
			DateType dateType = (DateType)e.getItem();
			txtCustomBaseDate.setVisible(dateType == DateType.CustomDate);
			layoutChanged.run();
		});

		pnlBaseDate.add(cmbBaseDate);
		pnlBaseDate.add(txtCustomBaseDate);

		add(lblBaseDate, GridBagSupport.at(0, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		add(pnlBaseDate, GridBagSupport.at(1, 1).fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 5, 0).build());

		lblAdjustment = new JLabel(Messages.getString("MainFrame.changes.filedate.adjustment"));

		JPanel pnlAdjustmentType = new JPanel();
		FlowLayout adjustmentTypeLayout = (FlowLayout)pnlAdjustmentType.getLayout();
		adjustmentTypeLayout.setVgap(inlineVgap);
		adjustmentTypeLayout.setHgap(inlineHgap);
		adjustmentTypeLayout.setAlignment(FlowLayout.LEFT);

		cmbAdjustmentType = new JComboBox<>();
		lblAdjustment.setLabelFor(cmbAdjustmentType);
		InputSupport.installLabelFocusAction(lblAdjustment, cmbAdjustmentType, LabelFocusBehavior.FOCUS_ONLY);
		cmbAdjustmentType.setModel(new DefaultComboBoxModel<>(DateModType.values()));
		cmbAdjustmentType.addItemListener(_ -> updateEnabledState());

		txtAdjustmentYears = newAdjustmentField(4);
		lblYearMonthSeparator = new JLabel("/");
		txtAdjustmentMonths = newAdjustmentField(2);
		lblMonthDaySeparator = new JLabel("/");
		txtAdjustmentDays = newAdjustmentField(2);
		lblDayHourSeparator = new JLabel(" ");
		txtAdjustmentHours = newAdjustmentField(2);
		lblHourMinuteSeparator = new JLabel(":");
		txtAdjustmentMinutes = newAdjustmentField(2);
		lblMinuteSecondSeparator = new JLabel(":");
		txtAdjustmentSeconds = newAdjustmentField(2);

		pnlAdjustmentType.add(cmbAdjustmentType);
		pnlAdjustmentType.add(txtAdjustmentYears);
		pnlAdjustmentType.add(lblYearMonthSeparator);
		pnlAdjustmentType.add(txtAdjustmentMonths);
		pnlAdjustmentType.add(lblMonthDaySeparator);
		pnlAdjustmentType.add(txtAdjustmentDays);
		pnlAdjustmentType.add(lblDayHourSeparator);
		pnlAdjustmentType.add(txtAdjustmentHours);
		pnlAdjustmentType.add(lblHourMinuteSeparator);
		pnlAdjustmentType.add(txtAdjustmentMinutes);
		pnlAdjustmentType.add(lblMinuteSecondSeparator);
		pnlAdjustmentType.add(txtAdjustmentSeconds);

		add(lblAdjustment, GridBagSupport.at(0, 2).anchor(GridBagConstraints.WEST).insets(0, 0, 0, 5).build());
		add(pnlAdjustmentType, GridBagSupport.at(1, 2).fill(GridBagConstraints.HORIZONTAL).build());

		updateEnabledState();
	}

	private void updateEnabledState() {
		boolean enabled = chkCreationDate.isSelected() || chkModifiedDate.isSelected() || chkAccessDate.isSelected() || chkExifDate.isSelected();

		lblBaseDate.setEnabled(enabled);
		cmbBaseDate.setEnabled(enabled);
		InputSupport.setTextFieldEnabled(txtCustomBaseDate, enabled);
		lblAdjustment.setEnabled(enabled);
		cmbAdjustmentType.setEnabled(enabled);

		boolean adjustmentEnabled = enabled && cmbAdjustmentType.getSelectedItem() != DateModType.None;
		InputSupport.setTextFieldEnabled(txtAdjustmentYears, adjustmentEnabled);
		lblYearMonthSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtAdjustmentMonths, adjustmentEnabled);
		lblMonthDaySeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtAdjustmentDays, adjustmentEnabled);
		lblDayHourSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtAdjustmentHours, adjustmentEnabled);
		lblHourMinuteSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtAdjustmentMinutes, adjustmentEnabled);
		lblMinuteSecondSeparator.setEnabled(adjustmentEnabled);
		InputSupport.setTextFieldEnabled(txtAdjustmentSeconds, adjustmentEnabled);
	}

	private static JTextField newAdjustmentField(int columns) {
		JTextField textField = new JTextField();
		InputSupport.allowDigitsOnly(textField);
		InputSupport.configureDisabledBackground(textField);
		textField.setColumns(columns);
		return textField;
	}
}
