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
	final JTextField txtAdjustmentYear;
	final JLabel lblYearMonthSeparator;
	final JTextField txtAdjustmentMonth;
	final JLabel lblMonthDaySeparator;
	final JTextField txtAdjustmentDay;
	final JLabel lblDayHourSeparator;
	final JTextField txtAdjustmentHour;
	final JLabel lblHourMinuteSeparator;
	final JTextField txtAdjustmentMinute;
	final JLabel lblMinuteSecondSeparator;
	final JTextField txtAdjustmentSecond;

	ChangesFileDatePanel(int sectionPadding, int inlineHgap, int inlineVgap, Runnable enableChanged, Runnable layoutChanged) {
		setBorder(new EmptyBorder(sectionPadding, sectionPadding, sectionPadding, sectionPadding));
		GridBagLayout fileDateLayout = new GridBagLayout();
		fileDateLayout.columnWidths = new int[]{0, 0, 0};
		fileDateLayout.rowHeights = new int[]{0, 0, 0, 0};
		fileDateLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		fileDateLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(fileDateLayout);

		lblTargetDate = new JLabel(Messages.getString("MainFrame.changeFileTargetDate"));
		GridBagConstraints lblTargetDateConstraints = new GridBagConstraints();
		lblTargetDateConstraints.anchor = GridBagConstraints.WEST;
		lblTargetDateConstraints.insets = new Insets(0, 0, 5, 5);
		lblTargetDateConstraints.gridx = 0;
		lblTargetDateConstraints.gridy = 0;
		add(lblTargetDate, lblTargetDateConstraints);

		JPanel pnlTargetDate = new JPanel();
		FlowLayout targetDateLayout = (FlowLayout)pnlTargetDate.getLayout();
		targetDateLayout.setVgap(inlineVgap);
		targetDateLayout.setHgap(inlineHgap);
		targetDateLayout.setAlignment(FlowLayout.LEFT);
		GridBagConstraints pnlTargetDateConstraints = new GridBagConstraints();
		pnlTargetDateConstraints.fill = GridBagConstraints.HORIZONTAL;
		pnlTargetDateConstraints.insets = new Insets(0, 0, 5, 0);
		pnlTargetDateConstraints.gridx = 1;
		pnlTargetDateConstraints.gridy = 0;
		add(pnlTargetDate, pnlTargetDateConstraints);

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				enableChanged.run();
			}
		};
		chkCreationDate = new JCheckBox(Messages.getString("MainFrame.changeFileCreationDate"));
		lblTargetDate.setLabelFor(chkCreationDate);
		InputSupport.installLabelFocusAction(lblTargetDate, chkCreationDate, LabelFocusBehavior.FOCUS_ONLY);
		chkCreationDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkCreationDate);

		chkModifiedDate = new JCheckBox(Messages.getString("MainFrame.changeFileModifiedDate"));
		chkModifiedDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkModifiedDate);

		chkAccessDate = new JCheckBox(Messages.getString("MainFrame.changeFileAccessDate"));
		chkAccessDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkAccessDate);

		chkExifDate = new JCheckBox(Messages.getString("MainFrame.changeFileExifDate"));
		chkExifDate.addChangeListener(changeListener);
		pnlTargetDate.add(chkExifDate);

		lblBaseDate = new JLabel(Messages.getString("MainFrame.changeFileBaseDateType"));
		GridBagConstraints lblBaseDateConstraints = new GridBagConstraints();
		lblBaseDateConstraints.anchor = GridBagConstraints.WEST;
		lblBaseDateConstraints.insets = new Insets(0, 0, 5, 5);
		lblBaseDateConstraints.gridx = 0;
		lblBaseDateConstraints.gridy = 1;
		add(lblBaseDate, lblBaseDateConstraints);

		JPanel pnlBaseDate = new JPanel();
		GridBagConstraints pnlBaseDateConstraints = new GridBagConstraints();
		pnlBaseDateConstraints.fill = GridBagConstraints.HORIZONTAL;
		pnlBaseDateConstraints.insets = new Insets(0, 0, 5, 0);
		pnlBaseDateConstraints.gridx = 1;
		pnlBaseDateConstraints.gridy = 1;
		add(pnlBaseDate, pnlBaseDateConstraints);
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

		lblAdjustment = new JLabel(Messages.getString("MainFrame.changeFileEditBaseDate"));
		GridBagConstraints lblAdjustmentConstraints = new GridBagConstraints();
		lblAdjustmentConstraints.anchor = GridBagConstraints.WEST;
		lblAdjustmentConstraints.insets = new Insets(0, 0, 0, 5);
		lblAdjustmentConstraints.gridx = 0;
		lblAdjustmentConstraints.gridy = 2;
		add(lblAdjustment, lblAdjustmentConstraints);

		JPanel pnlAdjustmentType = new JPanel();
		FlowLayout adjustmentTypeLayout = (FlowLayout)pnlAdjustmentType.getLayout();
		adjustmentTypeLayout.setVgap(inlineVgap);
		adjustmentTypeLayout.setHgap(inlineHgap);
		adjustmentTypeLayout.setAlignment(FlowLayout.LEFT);
		GridBagConstraints pnlAdjustmentTypeConstraints = new GridBagConstraints();
		pnlAdjustmentTypeConstraints.fill = GridBagConstraints.HORIZONTAL;
		pnlAdjustmentTypeConstraints.gridx = 1;
		pnlAdjustmentTypeConstraints.gridy = 2;
		add(pnlAdjustmentType, pnlAdjustmentTypeConstraints);

		cmbAdjustmentType = new JComboBox<>();
		lblAdjustment.setLabelFor(cmbAdjustmentType);
		InputSupport.installLabelFocusAction(lblAdjustment, cmbAdjustmentType, LabelFocusBehavior.FOCUS_ONLY);
		cmbAdjustmentType.setModel(new DefaultComboBoxModel<>(DateModType.values()));
		cmbAdjustmentType.addItemListener(_ -> enableChanged.run());
		pnlAdjustmentType.add(cmbAdjustmentType);

		txtAdjustmentYear = newAdjustmentField(4);
		pnlAdjustmentType.add(txtAdjustmentYear);
		lblYearMonthSeparator = new JLabel("/");
		pnlAdjustmentType.add(lblYearMonthSeparator);
		txtAdjustmentMonth = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentMonth);
		lblMonthDaySeparator = new JLabel("/");
		pnlAdjustmentType.add(lblMonthDaySeparator);
		txtAdjustmentDay = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentDay);
		lblDayHourSeparator = new JLabel(" ");
		pnlAdjustmentType.add(lblDayHourSeparator);
		txtAdjustmentHour = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentHour);
		lblHourMinuteSeparator = new JLabel(":");
		pnlAdjustmentType.add(lblHourMinuteSeparator);
		txtAdjustmentMinute = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentMinute);
		lblMinuteSecondSeparator = new JLabel(":");
		pnlAdjustmentType.add(lblMinuteSecondSeparator);
		txtAdjustmentSecond = newAdjustmentField(2);
		pnlAdjustmentType.add(txtAdjustmentSecond);
	}

	private static JTextField newAdjustmentField(int columns) {
		JTextField textField = new JTextField();
		InputSupport.allowDigitsOnly(textField);
		InputSupport.configureDisabledBackground(textField);
		textField.setColumns(columns);
		return textField;
	}
}
