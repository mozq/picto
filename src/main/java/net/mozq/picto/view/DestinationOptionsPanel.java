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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatClientProperties;

import net.mozq.picto.enums.ExistingFileMethod;

class DestinationOptionsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String CARD_FIELDS = "fields";
	private static final String CARD_SUMMARY = "summary";

	final JLabel lblSubFilePathPattern;
	final JTextField txtSubFilePathPattern;
	final JLabel lblExistingFileMethod;
	final JComboBox<ExistingFileMethod> cmbExistingFileMethod;
	final JLabel lblValidateFile;
	final JCheckBox chkCheckFileDigest;

	private final JPanel fieldsView;
	private final CardLayout cardLayout;
	private final JTextArea summaryView;
	private boolean expanded;
	private Consumer<Boolean> onExpandedChanged = _ -> { };
	private Runnable onContentChanged = () -> { };

	DestinationOptionsPanel() {
		fieldsView = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		fieldsView.setLayout(layout);
		PanelStyleSupport.stylizeOptionsBody(fieldsView);

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

		fieldsView.add(lblSubFilePathPattern, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		fieldsView.add(txtSubFilePathPattern, GridBagSupport.at(1, 0).fill(GridBagConstraints.HORIZONTAL).insets(0, 0, 5, 0).build());

		lblExistingFileMethod = new JLabel(Messages.getString("MainFrame.dest.existingFileMethod"));

		cmbExistingFileMethod = new JComboBox<>();
		lblExistingFileMethod.setLabelFor(cmbExistingFileMethod);
		InputSupport.installLabelFocusAction(lblExistingFileMethod, cmbExistingFileMethod, LabelFocusBehavior.FOCUS_ONLY);
		cmbExistingFileMethod.setModel(new DefaultComboBoxModel<>(ExistingFileMethod.values()));

		fieldsView.add(lblExistingFileMethod, GridBagSupport.at(0, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 5).build());
		fieldsView.add(cmbExistingFileMethod, GridBagSupport.at(1, 1).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 0).build());

		lblValidateFile = new JLabel(Messages.getString("MainFrame.dest.validateFile"));

		chkCheckFileDigest = new JCheckBox(Messages.getString("MainFrame.dest.checkFileDigest"));
		lblValidateFile.setLabelFor(chkCheckFileDigest);
		InputSupport.installLabelFocusAction(lblValidateFile, chkCheckFileDigest, LabelFocusBehavior.FOCUS_ONLY);

		fieldsView.add(lblValidateFile, GridBagSupport.at(0, 2).anchor(GridBagConstraints.WEST).insets(0, 0, 0, 5).build());
		fieldsView.add(chkCheckFileDigest, GridBagSupport.at(1, 2).anchor(GridBagConstraints.WEST).build());

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

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		summaryView.setEnabled(enabled);
		lblSubFilePathPattern.setEnabled(enabled);
		txtSubFilePathPattern.setEnabled(enabled);
		lblExistingFileMethod.setEnabled(enabled);
		cmbExistingFileMethod.setEnabled(enabled);
		lblValidateFile.setEnabled(enabled);
		chkCheckFileDigest.setEnabled(enabled);
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

	private void installSummaryListeners() {
		txtSubFilePathPattern.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				refreshSummary();
			}

			public void removeUpdate(DocumentEvent e) {
				refreshSummary();
			}

			public void changedUpdate(DocumentEvent e) {
				refreshSummary();
			}
		});
		chkCheckFileDigest.addChangeListener(_ -> refreshSummary());
		cmbExistingFileMethod.addItemListener(e -> {
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
	}

	private String computeSummary() {
		List<String> items = new ArrayList<>();
		String pattern = SummaryTextSupport.fieldText(txtSubFilePathPattern);
		if (!pattern.isBlank() && !MainFrameSettings.DEFAULT_DEST_SUB_FILE_PATH_PATTERN.equals(pattern)) {
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.dest.subFilePathPattern"), pattern));
		}
		ExistingFileMethod existingFileMethod = (ExistingFileMethod)cmbExistingFileMethod.getSelectedItem();
		if (existingFileMethod != ExistingFileMethod.Confirm) {
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.dest.existingFileMethod"), String.valueOf(existingFileMethod)));
		}
		if (chkCheckFileDigest.isEnabled() && chkCheckFileDigest.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.dest.validateFile")));
		}
		return SummaryTextSupport.joinOptionsSummary(items);
	}
}
