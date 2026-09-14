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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.mozq.picto.enums.OperationType;

/**
 * Shared primitives for rendering the short human-readable condition summaries shown for the run button
 * and each collapsed options/changes section - reading a raw {@link JTextField}, formatting a value pair,
 * building the summary {@link JTextArea} widget itself. Each panel that owns a set of condition fields
 * (e.g. {@link SourceOptionsPanel}) computes its own summary text from those primitives; only
 * {@link #runSummary}, which crosses the source/destination folders and the operation type, has no single
 * panel to belong to and stays here as a small standalone function.
 */
final class SummaryTextSupport {
	static final String CHECKED_ITEM_PREFIX = "✓ ";

	private SummaryTextSupport() {
	}

	static String runSummary(ProcessConditionInput input) {
		String summary = input.operationType + ": "
				+ Messages.getString("MainFrame.src.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(input.srcFolder, Messages.getString("MainFrame.src.folder"));
		if (input.operationType == OperationType.Overwrite) {
			return summary;
		}
		return summary + " -> "
				+ Messages.getString("MainFrame.dest.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(input.destFolder, Messages.getString("MainFrame.dest.folder"));
	}

	static String fieldText(JTextField field) {
		String text = field.getText();
		return text == null ? "" : text.trim();
	}

	static String dateFieldText(String text) {
		return DateTimeText.compact(text);
	}

	static String rangeText(String from, String to) {
		if (from.isEmpty()) {
			return to;
		}
		if (to.isEmpty()) {
			return from;
		}
		return from + " - " + to;
	}

	static String summaryItem(String label, String value) {
		return label + ": " + value;
	}

	static String checkedItem(String label) {
		return CHECKED_ITEM_PREFIX + label;
	}

	static String joinOptionsSummary(List<String> items) {
		return String.join(" / ", items);
	}

	static void addAdjustmentAmount(List<String> amounts, String text, String suffix) {
		if (text == null || text.isEmpty()) {
			return;
		}
		try {
			amounts.add(Integer.parseInt(text) + suffix);
		} catch (NumberFormatException e) {
			amounts.add(text + suffix);
		}
	}

	static JTextArea newSummaryText() {
		JTextArea summary = new JTextArea();
		summary.setEditable(false);
		summary.setFocusable(false);
		summary.setLineWrap(true);
		summary.setWrapStyleWord(true);
		summary.setOpaque(false);
		summary.setBorder(new EmptyBorder(1, 4, 2, 4));
		summary.setFont(summary.getFont().deriveFont(summary.getFont().getSize2D() - 1.0f));
		Color foreground = UIManager.getColor("Label.disabledForeground");
		if (foreground == null) {
			foreground = UIManager.getColor("Label.foreground");
		}
		summary.setForeground(foreground);
		return summary;
	}

	/** Wires clicking a collapsed-state summary widget back open, matching the click behavior of the
	 * options body it's standing in for. */
	static void installSummaryClickToExpand(JTextArea summaryView, Runnable onClick) {
		summaryView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		summaryView.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onClick.run();
			}
		});
	}
}
