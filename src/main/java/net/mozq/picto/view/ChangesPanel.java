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
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;

class ChangesPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final String CARD_FIELDS = "fields";
	private static final String CARD_SUMMARY = "summary";

	final ChangesFileDatePanel filedate;
	final ChangesExifPanel exif;

	private final JTabbedPane tabbedPane;
	private final CardLayout cardLayout;
	private final JTextArea summaryView;
	private boolean expanded;
	private Consumer<Boolean> onExpandedChanged = _ -> { };
	private Runnable onContentChanged = () -> { };

	ChangesPanel(int sectionPadding, int inlineHgap, int inlineVgap, Runnable enableChanged, Runnable layoutChanged) {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.putClientProperty("JTabbedPane.tabType", "card");

		filedate = new ChangesFileDatePanel(sectionPadding, inlineHgap, inlineVgap, enableChanged, layoutChanged);
		tabbedPane.addTab(Messages.getString("MainFrame.changes.filedate.title"), null, filedate, null);

		exif = new ChangesExifPanel(sectionPadding);
		tabbedPane.addTab(Messages.getString("MainFrame.changes.exif.title"), null, exif, null);

		summaryView = SummaryTextSupport.newSummaryText();
		SummaryTextSupport.installSummaryClickToExpand(summaryView, () -> {
			if (isEnabled()) {
				setExpanded(true);
			}
		});

		cardLayout = new CardLayout();
		setLayout(cardLayout);
		add(tabbedPane, CARD_FIELDS);
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

	// CardLayout otherwise sizes the container to its largest card regardless of which one is showing,
	// which would keep this panel tabbed-pane-tall even while only the one-line summary is displayed.
	@Override
	public Dimension getPreferredSize() {
		return isPreferredSizeSet() ? super.getPreferredSize() : sizeWithInsets((expanded ? tabbedPane : summaryView).getPreferredSize());
	}

	@Override
	public Dimension getMinimumSize() {
		return isMinimumSizeSet() ? super.getMinimumSize() : sizeWithInsets((expanded ? tabbedPane : summaryView).getMinimumSize());
	}

	private Dimension sizeWithInsets(Dimension size) {
		Insets insets = getInsets();
		return new Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom);
	}

	int selectedTabIndex() {
		return tabbedPane.getSelectedIndex();
	}

	void setSelectedTabIndex(int index) {
		if (index >= 0 && index < tabbedPane.getTabCount()) {
			tabbedPane.setSelectedIndex(index);
		}
	}

	private void installSummaryListeners() {
		DocumentListener documentListener = new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				refreshSummary();
			}

			public void removeUpdate(DocumentEvent e) {
				refreshSummary();
			}

			public void changedUpdate(DocumentEvent e) {
				refreshSummary();
			}
		};
		for (JTextField field : new JTextField[]{
				filedate.txtCustomBaseDate,
				filedate.txtAdjustmentYears,
				filedate.txtAdjustmentMonths,
				filedate.txtAdjustmentDays,
				filedate.txtAdjustmentHours,
				filedate.txtAdjustmentMinutes,
				filedate.txtAdjustmentSeconds}) {
			field.getDocument().addDocumentListener(documentListener);
		}
		ChangeListener changeListener = _ -> refreshSummary();
		filedate.chkCreationDate.addChangeListener(changeListener);
		filedate.chkModifiedDate.addChangeListener(changeListener);
		filedate.chkAccessDate.addChangeListener(changeListener);
		filedate.chkExifDate.addChangeListener(changeListener);
		exif.chkRemoveGps.addChangeListener(changeListener);
		exif.chkRemoveAll.addChangeListener(changeListener);
		filedate.cmbBaseDate.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				refreshSummary();
			}
		});
		filedate.cmbAdjustmentType.addItemListener(e -> {
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
		boolean changesFileDate = false;
		if (filedate.chkCreationDate.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.changes.filedate.creationDate")));
			changesFileDate = true;
		}
		if (filedate.chkModifiedDate.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.changes.filedate.modifiedDate")));
			changesFileDate = true;
		}
		if (filedate.chkAccessDate.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.changes.filedate.accessDate")));
			changesFileDate = true;
		}
		if (filedate.chkExifDate.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.changes.filedate.exifDate")));
			changesFileDate = true;
		}
		if (changesFileDate) {
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.changes.filedate.baseDateType"), baseDateTypeSummary()));
		}
		DateModType adjustmentType = (DateModType)filedate.cmbAdjustmentType.getSelectedItem();
		if (changesFileDate && adjustmentType != DateModType.None) {
			items.add(SummaryTextSupport.summaryItem(Messages.getString("MainFrame.changes.filedate.adjustment"), adjustmentSummary(adjustmentType)));
		}
		if (exif.chkRemoveGps.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.changes.exif.removeGps")));
		}
		if (exif.chkRemoveAll.isSelected()) {
			items.add(SummaryTextSupport.checkedItem(Messages.getString("MainFrame.changes.exif.removeAll")));
		}
		return SummaryTextSupport.joinOptionsSummary(items);
	}

	private String baseDateTypeSummary() {
		DateType baseDateType = (DateType)filedate.cmbBaseDate.getSelectedItem();
		String value = String.valueOf(baseDateType);
		String customDate = SummaryTextSupport.dateFieldText(SummaryTextSupport.fieldText(filedate.txtCustomBaseDate));
		if (baseDateType == DateType.CustomDate && !customDate.isEmpty()) {
			value += " " + customDate;
		}
		return value;
	}

	private String adjustmentSummary(DateModType adjustmentType) {
		String value = String.valueOf(adjustmentType);
		List<String> amounts = new ArrayList<>();
		SummaryTextSupport.addAdjustmentAmount(amounts, SummaryTextSupport.fieldText(filedate.txtAdjustmentYears), "Y");
		SummaryTextSupport.addAdjustmentAmount(amounts, SummaryTextSupport.fieldText(filedate.txtAdjustmentMonths), "M");
		SummaryTextSupport.addAdjustmentAmount(amounts, SummaryTextSupport.fieldText(filedate.txtAdjustmentDays), "D");
		SummaryTextSupport.addAdjustmentAmount(amounts, SummaryTextSupport.fieldText(filedate.txtAdjustmentHours), "h");
		SummaryTextSupport.addAdjustmentAmount(amounts, SummaryTextSupport.fieldText(filedate.txtAdjustmentMinutes), "m");
		SummaryTextSupport.addAdjustmentAmount(amounts, SummaryTextSupport.fieldText(filedate.txtAdjustmentSeconds), "s");
		if (!amounts.isEmpty()) {
			value += " " + String.join(" ", amounts);
		}
		return value;
	}
}
