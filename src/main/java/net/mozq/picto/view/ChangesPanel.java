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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;

class ChangesPanel extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	final JLabel targetDateLabel;
	final JCheckBox changeFileCreationDateCheckBox;
	final JCheckBox changeFileModifiedDateCheckBox;
	final JCheckBox changeFileAccessDateCheckBox;
	final JCheckBox changeExifDateCheckBox;
	final JLabel baseDateTypeLabel;
	final JComboBox<DateType> baseDateTypeComboBox;
	final JFormattedTextField customBaseDateTextField;
	final JLabel editBaseDateLabel;
	final JComboBox<DateModType> dateModTypeComboBox;
	final JTextField dateModYearsTextField;
	final JLabel yearMonthSeparatorLabel;
	final JTextField dateModMonthsTextField;
	final JLabel monthDaySeparatorLabel;
	final JTextField dateModDaysTextField;
	final JLabel dayHourSeparatorLabel;
	final JTextField dateModHoursTextField;
	final JLabel hourMinuteSeparatorLabel;
	final JTextField dateModMinutesTextField;
	final JLabel minuteSecondSeparatorLabel;
	final JTextField dateModSecondsTextField;
	final JCheckBox removeExifTagsGpsCheckBox;
	final JCheckBox removeExifTagsAllCheckBox;

	ChangesPanel(int sectionPadding, int inlineHgap, int inlineVgap, Runnable enableChanged, Runnable layoutChanged) {
		super(JTabbedPane.TOP);
		putClientProperty("JTabbedPane.tabType", "card");

		JPanel fileDatePanel = new JPanel();
		fileDatePanel.setBorder(new EmptyBorder(sectionPadding, sectionPadding, sectionPadding, sectionPadding));
		addTab(Messages.getString("MainFrame.changeFileDateTitle"), null, fileDatePanel, null);
		GridBagLayout fileDateLayout = new GridBagLayout();
		fileDateLayout.columnWidths = new int[]{0, 0, 0};
		fileDateLayout.rowHeights = new int[]{0, 0, 0, 0};
		fileDateLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		fileDateLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		fileDatePanel.setLayout(fileDateLayout);

		targetDateLabel = new JLabel(Messages.getString("MainFrame.changeFileTargetDate"));
		GridBagConstraints targetDateLabelConstraints = new GridBagConstraints();
		targetDateLabelConstraints.anchor = GridBagConstraints.WEST;
		targetDateLabelConstraints.insets = new Insets(0, 0, 5, 5);
		targetDateLabelConstraints.gridx = 0;
		targetDateLabelConstraints.gridy = 0;
		fileDatePanel.add(targetDateLabel, targetDateLabelConstraints);

		JPanel targetDatePanel = new JPanel();
		FlowLayout targetDateLayout = (FlowLayout)targetDatePanel.getLayout();
		targetDateLayout.setVgap(inlineVgap);
		targetDateLayout.setHgap(inlineHgap);
		targetDateLayout.setAlignment(FlowLayout.LEFT);
		GridBagConstraints targetDatePanelConstraints = new GridBagConstraints();
		targetDatePanelConstraints.fill = GridBagConstraints.HORIZONTAL;
		targetDatePanelConstraints.insets = new Insets(0, 0, 5, 0);
		targetDatePanelConstraints.gridx = 1;
		targetDatePanelConstraints.gridy = 0;
		fileDatePanel.add(targetDatePanel, targetDatePanelConstraints);

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				enableChanged.run();
			}
		};
		changeFileCreationDateCheckBox = new JCheckBox(Messages.getString("MainFrame.changeFileCreationDate"));
		targetDateLabel.setLabelFor(changeFileCreationDateCheckBox);
		InputSupport.installLabelFocusAction(targetDateLabel, changeFileCreationDateCheckBox, LabelFocusBehavior.FOCUS_ONLY);
		changeFileCreationDateCheckBox.addChangeListener(changeListener);
		targetDatePanel.add(changeFileCreationDateCheckBox);

		changeFileModifiedDateCheckBox = new JCheckBox(Messages.getString("MainFrame.changeFileModifiedDate"));
		changeFileModifiedDateCheckBox.addChangeListener(changeListener);
		targetDatePanel.add(changeFileModifiedDateCheckBox);

		changeFileAccessDateCheckBox = new JCheckBox(Messages.getString("MainFrame.changeFileAccessDate"));
		changeFileAccessDateCheckBox.addChangeListener(changeListener);
		targetDatePanel.add(changeFileAccessDateCheckBox);

		changeExifDateCheckBox = new JCheckBox(Messages.getString("MainFrame.changeFileExifDate"));
		changeExifDateCheckBox.addChangeListener(changeListener);
		targetDatePanel.add(changeExifDateCheckBox);

		baseDateTypeLabel = new JLabel(Messages.getString("MainFrame.changeFileBaseDateType"));
		GridBagConstraints baseDateTypeLabelConstraints = new GridBagConstraints();
		baseDateTypeLabelConstraints.anchor = GridBagConstraints.WEST;
		baseDateTypeLabelConstraints.insets = new Insets(0, 0, 5, 5);
		baseDateTypeLabelConstraints.gridx = 0;
		baseDateTypeLabelConstraints.gridy = 1;
		fileDatePanel.add(baseDateTypeLabel, baseDateTypeLabelConstraints);

		JPanel baseDatePanel = new JPanel();
		GridBagConstraints baseDatePanelConstraints = new GridBagConstraints();
		baseDatePanelConstraints.fill = GridBagConstraints.HORIZONTAL;
		baseDatePanelConstraints.insets = new Insets(0, 0, 5, 0);
		baseDatePanelConstraints.gridx = 1;
		baseDatePanelConstraints.gridy = 1;
		fileDatePanel.add(baseDatePanel, baseDatePanelConstraints);
		baseDatePanel.setLayout(new FlowLayout(FlowLayout.LEFT, inlineHgap, inlineVgap));

		baseDateTypeComboBox = new JComboBox<>();
		baseDateTypeLabel.setLabelFor(baseDateTypeComboBox);
		InputSupport.installLabelFocusAction(baseDateTypeLabel, baseDateTypeComboBox, LabelFocusBehavior.FOCUS_ONLY);
		baseDatePanel.add(baseDateTypeComboBox);
		baseDateTypeComboBox.setModel(new DefaultComboBoxModel<>(DateType.values()));

		customBaseDateTextField = new JFormattedTextField(InputSupport.newMaskFormatter(DateTimeText.MASK_PATTERN));
		InputSupport.configureDisabledBackground(customBaseDateTextField);
		customBaseDateTextField.setFont(new Font("Monospaced", Font.PLAIN, 13));
		customBaseDateTextField.setHorizontalAlignment(JTextField.CENTER);
		customBaseDateTextField.setColumns(20);
		customBaseDateTextField.setVisible(false);
		InputSupport.installDateTimeInputPopup(customBaseDateTextField, false);
		customBaseDateTextField.setFocusLostBehavior(JFormattedTextField.COMMIT);
		baseDatePanel.add(customBaseDateTextField);

		baseDateTypeComboBox.addItemListener(e -> {
			DateType dateType = (DateType)e.getItem();
			customBaseDateTextField.setVisible(dateType == DateType.CustomDate);
			layoutChanged.run();
		});

		editBaseDateLabel = new JLabel(Messages.getString("MainFrame.changeFileEditBaseDate"));
		GridBagConstraints editBaseDateLabelConstraints = new GridBagConstraints();
		editBaseDateLabelConstraints.anchor = GridBagConstraints.WEST;
		editBaseDateLabelConstraints.insets = new Insets(0, 0, 0, 5);
		editBaseDateLabelConstraints.gridx = 0;
		editBaseDateLabelConstraints.gridy = 2;
		fileDatePanel.add(editBaseDateLabel, editBaseDateLabelConstraints);

		JPanel dateModTypePanel = new JPanel();
		FlowLayout dateModTypeLayout = (FlowLayout)dateModTypePanel.getLayout();
		dateModTypeLayout.setVgap(inlineVgap);
		dateModTypeLayout.setHgap(inlineHgap);
		dateModTypeLayout.setAlignment(FlowLayout.LEFT);
		GridBagConstraints dateModTypePanelConstraints = new GridBagConstraints();
		dateModTypePanelConstraints.fill = GridBagConstraints.HORIZONTAL;
		dateModTypePanelConstraints.gridx = 1;
		dateModTypePanelConstraints.gridy = 2;
		fileDatePanel.add(dateModTypePanel, dateModTypePanelConstraints);

		dateModTypeComboBox = new JComboBox<>();
		editBaseDateLabel.setLabelFor(dateModTypeComboBox);
		InputSupport.installLabelFocusAction(editBaseDateLabel, dateModTypeComboBox, LabelFocusBehavior.FOCUS_ONLY);
		dateModTypeComboBox.setModel(new DefaultComboBoxModel<>(DateModType.values()));
		dateModTypeComboBox.addItemListener(e -> enableChanged.run());
		dateModTypePanel.add(dateModTypeComboBox);

		dateModYearsTextField = newAdjustmentField(4);
		dateModTypePanel.add(dateModYearsTextField);
		yearMonthSeparatorLabel = new JLabel("/");
		dateModTypePanel.add(yearMonthSeparatorLabel);
		dateModMonthsTextField = newAdjustmentField(2);
		dateModTypePanel.add(dateModMonthsTextField);
		monthDaySeparatorLabel = new JLabel("/");
		dateModTypePanel.add(monthDaySeparatorLabel);
		dateModDaysTextField = newAdjustmentField(2);
		dateModTypePanel.add(dateModDaysTextField);
		dayHourSeparatorLabel = new JLabel(" ");
		dateModTypePanel.add(dayHourSeparatorLabel);
		dateModHoursTextField = newAdjustmentField(2);
		dateModTypePanel.add(dateModHoursTextField);
		hourMinuteSeparatorLabel = new JLabel(":");
		dateModTypePanel.add(hourMinuteSeparatorLabel);
		dateModMinutesTextField = newAdjustmentField(2);
		dateModTypePanel.add(dateModMinutesTextField);
		minuteSecondSeparatorLabel = new JLabel(":");
		dateModTypePanel.add(minuteSecondSeparatorLabel);
		dateModSecondsTextField = newAdjustmentField(2);
		dateModTypePanel.add(dateModSecondsTextField);

		JPanel exifPanel = new JPanel();
		exifPanel.setBorder(new EmptyBorder(sectionPadding, sectionPadding, sectionPadding, sectionPadding));
		addTab(Messages.getString("MainFrame.changeExifTitle"), null, exifPanel, null);
		GridBagLayout exifLayout = new GridBagLayout();
		exifLayout.columnWidths = new int[]{0, 0};
		exifLayout.rowHeights = new int[]{0, 0, 0};
		exifLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		exifLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		exifPanel.setLayout(exifLayout);

		removeExifTagsGpsCheckBox = new JCheckBox(Messages.getString("MainFrame.removeExifTagsGps"));
		GridBagConstraints removeGpsConstraints = new GridBagConstraints();
		removeGpsConstraints.anchor = GridBagConstraints.WEST;
		removeGpsConstraints.insets = new Insets(0, 0, 5, 0);
		removeGpsConstraints.gridx = 0;
		removeGpsConstraints.gridy = 0;
		exifPanel.add(removeExifTagsGpsCheckBox, removeGpsConstraints);

		removeExifTagsAllCheckBox = new JCheckBox(Messages.getString("MainFrame.removeExifTagsAll"));
		GridBagConstraints removeAllConstraints = new GridBagConstraints();
		removeAllConstraints.anchor = GridBagConstraints.WEST;
		removeAllConstraints.gridx = 0;
		removeAllConstraints.gridy = 1;
		exifPanel.add(removeExifTagsAllCheckBox, removeAllConstraints);
	}

	private static JTextField newAdjustmentField(int columns) {
		JTextField textField = new JTextField();
		InputSupport.allowDigitsOnly(textField);
		InputSupport.configureDisabledBackground(textField);
		textField.setColumns(columns);
		return textField;
	}
}
