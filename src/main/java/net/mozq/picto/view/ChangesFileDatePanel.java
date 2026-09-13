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

	ChangesFileDatePanel(int sectionPadding, int inlineHgap, int inlineVgap, Runnable enableChanged, Runnable layoutChanged) {
		setBorder(new EmptyBorder(sectionPadding, sectionPadding, sectionPadding, sectionPadding));
		GridBagLayout fileDateLayout = new GridBagLayout();
		fileDateLayout.columnWidths = new int[]{0, 0, 0};
		fileDateLayout.rowHeights = new int[]{0, 0, 0, 0};
		fileDateLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		fileDateLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(fileDateLayout);

		lblTargetDate = new JLabel(Messages.getString("MainFrame.changes.filedate.targetDate"));
		add(lblTargetDate, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());

		JPanel pnlTargetDate = new JPanel();
		FlowLayout targetDateLayout = (FlowLayout)pnlTargetDate.getLayout();
		targetDateLayout.setVgap(inlineVgap);
		targetDateLayout.setHgap(inlineHgap);
		targetDateLayout.setAlignment(FlowLayout.LEFT);
		add(pnlTargetDate, GridBagSupport.at(1, 0).fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 5, 0).build());

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				enableChanged.run();
			}
		};
		chkCreationDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.creationDate"));
		lblTargetDate.setLabelFor(chkCreationDate);
		InputSupport.installLabelFocusAction(lblTargetDate, chkCreationDate, LabelFocusBehavior.FOCUS_ONLY);
		chkCreationDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkCreationDate);

		chkModifiedDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.modifiedDate"));
		chkModifiedDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkModifiedDate);

		chkAccessDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.accessDate"));
		chkAccessDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkAccessDate);

		chkExifDate = new JCheckBox(Messages.getString("MainFrame.changes.filedate.exifDate"));
		chkExifDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkExifDate);

		lblBaseDate = new JLabel(Messages.getString("MainFrame.changes.filedate.baseDateType"));
		add(lblBaseDate, GridBagSupport.at(0, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());

		JPanel pnlBaseDate = new JPanel();
		add(pnlBaseDate, GridBagSupport.at(1, 1).fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 5, 0).build());
		pnlBaseDate.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		cmbBaseDate = new JComboBox<>();
		lblBaseDate.setLabelFor(cmbBaseDate);
		InputSupport.installLabelFocusAction(lblBaseDate, cmbBaseDate, LabelFocusBehavior.FOCUS_ONLY);
		pnlBaseDate.add(cmbBaseDate);
		cmbBaseDate.setModel(new DefaultComboBoxModel<>(DateType.values()));

		txtCustomBaseDate = new JFormattedTextField(InputSupport.newMaskFormatter(DateTimeText.MASK_PATTERN));
		InputSupport.configureDisabledBackground(txtCustomBaseDate);
		txtCustomBaseDate.setFont(new Font("Monospaced", Font.PLAIN, 13));
		txtCustomBaseDate.setHorizontalAlignment(JTextField.CENTER);
		txtCustomBaseDate.setColumns(20);
		txtCustomBaseDate.setVisible(false);
		InputSupport.installDateTimeInputPopup(txtCustomBaseDate, false);
		txtCustomBaseDate.setFocusLostBehavior(JFormattedTextField.COMMIT);
		pnlBaseDate.add(txtCustomBaseDate);

		cmbBaseDate.addItemListener(e -> {
			DateType dateType = (DateType)e.getItem();
			txtCustomBaseDate.setVisible(dateType == DateType.CustomDate);
			layoutChanged.run();
		});

		lblAdjustment = new JLabel(Messages.getString("MainFrame.changes.filedate.adjustment"));
		add(lblAdjustment, GridBagSupport.at(0, 2).anchor(GridBagConstraints.WEST).insets(0, 0, 0, 5).build());

		JPanel pnlAdjustmentType = new JPanel();
		FlowLayout adjustmentTypeLayout = (FlowLayout)pnlAdjustmentType.getLayout();
		adjustmentTypeLayout.setVgap(inlineVgap);
		adjustmentTypeLayout.setHgap(inlineHgap);
		adjustmentTypeLayout.setAlignment(FlowLayout.LEFT);
		add(pnlAdjustmentType, GridBagSupport.at(1, 2).fill(GridBagConstraints.HORIZONTAL).build());

		cmbAdjustmentType = new JComboBox<>();
		lblAdjustment.setLabelFor(cmbAdjustmentType);
		InputSupport.installLabelFocusAction(lblAdjustment, cmbAdjustmentType, LabelFocusBehavior.FOCUS_ONLY);
		cmbAdjustmentType.setModel(new DefaultComboBoxModel<>(DateModType.values()));
		cmbAdjustmentType.addItemListener(_ -> enableChanged.run());
		pnlAdjustmentType.add(cmbAdjustmentType);

		txtAdjustmentYears = newAdjustmentField(4);
		pnlAdjustmentType.add(txtAdjustmentYears);
		lblYearMonthSeparator = new JLabel("/");
		pnlAdjustmentType.add(lblYearMonthSeparator);
		txtAdjustmentMonths = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentMonths);
		lblMonthDaySeparator = new JLabel("/");
		pnlAdjustmentType.add(lblMonthDaySeparator);
		txtAdjustmentDays = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentDays);
		lblDayHourSeparator = new JLabel(" ");
		pnlAdjustmentType.add(lblDayHourSeparator);
		txtAdjustmentHours = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentHours);
		lblHourMinuteSeparator = new JLabel(":");
		pnlAdjustmentType.add(lblHourMinuteSeparator);
		txtAdjustmentMinutes = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentMinutes);
		lblMinuteSecondSeparator = new JLabel(":");
		pnlAdjustmentType.add(lblMinuteSecondSeparator);
		txtAdjustmentSeconds = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentSeconds);
	}

	private static JTextField newAdjustmentField(int columns) {
		JTextField textField = new JTextField();
		InputSupport.allowDigitsOnly(textField);
		InputSupport.configureDisabledBackground(textField);
		textField.setColumns(columns);
		return textField;
	}
}
