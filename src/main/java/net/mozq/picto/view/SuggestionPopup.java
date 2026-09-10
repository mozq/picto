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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.formdev.flatlaf.FlatClientProperties;

class SuggestionPopup {
	private static final int LABEL_COLUMN_PADDING = 8;
	private static SuggestionPopup activePopup;

	private final JTextComponent field;
	private final Supplier<List<SuggestionSection>> sectionsSupplier;
	private final Consumer<String> valueConsumer;
	private final Consumer<SuggestionItem> onDelete;
	private final String deleteTooltip;
	private final List<Component> relatedFocusComponents;
	private final JPanel popupPanel = new JPanel(new BorderLayout());
	private final int minWidth;
	private final int maxHeight;
	private Popup popup;
	private boolean windowFocusListenerInstalled;
	private boolean refreshScheduled;

	SuggestionPopup(
			JTextComponent field,
			Supplier<List<SuggestionSection>> sectionsSupplier,
			Consumer<String> valueConsumer,
			int minWidth,
			int maxHeight,
			Component... relatedFocusComponents) {
		this(field, sectionsSupplier, valueConsumer, null, null, minWidth, maxHeight, relatedFocusComponents);
	}

	/**
	 * @param onDelete when non-null, each item shows a small "remove" button (tooltipped with
	 * {@code deleteTooltip}) that calls this with the item instead of picking it, then refreshes the popup.
	 */
	SuggestionPopup(
			JTextComponent field,
			Supplier<List<SuggestionSection>> sectionsSupplier,
			Consumer<String> valueConsumer,
			Consumer<SuggestionItem> onDelete,
			String deleteTooltip,
			int minWidth,
			int maxHeight,
			Component... relatedFocusComponents) {
		this.field = field;
		this.sectionsSupplier = sectionsSupplier;
		this.valueConsumer = valueConsumer;
		this.onDelete = onDelete;
		this.deleteTooltip = deleteTooltip;
		this.minWidth = minWidth;
		this.maxHeight = maxHeight;
		this.relatedFocusComponents = List.of(relatedFocusComponents);
		this.popupPanel.setBorder(PopupSupport.createPopupBorder());
		installListeners();
	}

	void refresh() {
		refreshPopup();
	}

	private void installListeners() {
		PopupSupport.installFocusOwnerChangeListener(() -> popup != null, this::hidePopupIfFocusMovedAway);
		SwingUtilities.invokeLater(this::installWindowFocusListener);
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(SuggestionPopup.this::showPopup);
			}

			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(SuggestionPopup.this::hidePopupIfFocusMovedAway);
			}
		});
		field.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				showPopup();
			}
		});
	}

	private void installWindowFocusListener() {
		if (windowFocusListenerInstalled) {
			return;
		}
		windowFocusListenerInstalled = PopupSupport.installWindowFocusListener(field, this::hidePopup, this::refreshPopup);
	}

	private void showPopup() {
		if (!field.isEnabled() || !isPopupFocusActive() || popup != null || !hasAnyItems()) {
			return;
		}
		if (activePopup != null && activePopup != this) {
			activePopup.hidePopup();
		}
		activePopup = this;
		try {
			popupPanel.removeAll();
			popupPanel.setPreferredSize(null);
			popupPanel.add(createContent(), BorderLayout.CENTER);
			Dimension preferredSize = popupPanel.getPreferredSize();
			int width = Math.max(Math.max(field.getWidth(), minWidth), preferredSize.width);
			int height = Math.min(preferredSize.height, maxHeight);
			popupPanel.setPreferredSize(new Dimension(width, height));
			Point location = field.getLocationOnScreen();
			popup = PopupFactory.getSharedInstance().getPopup(field, popupPanel, location.x, location.y + field.getHeight());
			popup.show();
		} catch (IllegalComponentStateException _) {
			hidePopup();
		}
	}

	private void hidePopupIfFocusMovedAway() {
		if (PopupSupport.shouldHidePopup(field, popupPanel, this::isRelatedFocusOwner)) {
			hidePopup();
		}
	}

	private void hidePopup() {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
		if (activePopup == this) {
			activePopup = null;
		}
	}

	private void refreshPopup() {
		if (popup == null || refreshScheduled) {
			return;
		}
		refreshScheduled = true;
		SwingUtilities.invokeLater(() -> {
			refreshScheduled = false;
			if (popup == null || !field.isEnabled() || !isPopupFocusActive()) {
				return;
			}
			hidePopup();
			showPopup();
		});
	}

	private boolean hasAnyItems() {
		for (SuggestionSection section : sectionsSupplier.get()) {
			if (!section.items().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private boolean isPopupFocusActive() {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		return field.isFocusOwner() || isRelatedFocusOwner(focusOwner);
	}

	private boolean isRelatedFocusOwner(Component focusOwner) {
		if (focusOwner == null) {
			return false;
		}
		for (Component component : relatedFocusComponents) {
			if (focusOwner == component || SwingUtilities.isDescendingFrom(focusOwner, component)) {
				return true;
			}
		}
		return false;
	}

	private JScrollPane createContent() {
		List<SuggestionSection> sections = sectionsSupplier.get();
		int labelWidth = calculateLabelWidth(sections);

		JPanel content = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 0, 0);
		for (SuggestionSection section : sections) {
			addSection(content, gbc, section.label());
			addItems(content, gbc, section.items(), labelWidth);
		}

		gbc.gridy++;
		gbc.weighty = 1.0;
		content.add(new JPanel(), gbc);

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	private static void addSection(JPanel content, GridBagConstraints gbc, String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		label.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
		gbc.gridy++;
		content.add(label, gbc);
	}

	private void addItems(JPanel content, GridBagConstraints gbc, List<SuggestionItem> items, int labelWidth) {
		for (SuggestionItem item : items) {
			addItem(content, gbc, item, labelWidth);
		}
	}

	private void addItem(JPanel content, GridBagConstraints gbc, SuggestionItem item, int labelWidth) {
		JPanel row = new JPanel(new GridBagLayout());
		Color defaultBackground = row.getBackground();
		Color hoverBackground = createHoverBackground(defaultBackground);
		String tooltip = item.tooltip();
		row.setOpaque(true);
		row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		row.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 10));
		row.setToolTipText(tooltip);
		MouseAdapter rowMouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				row.setBackground(hoverBackground);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				row.setBackground(defaultBackground);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				valueConsumer.accept(item.value());
				hidePopup();
				field.requestFocusInWindow();
			}
		};
		row.addMouseListener(rowMouseListener);

		GridBagConstraints labelGbc = new GridBagConstraints();
		labelGbc.gridx = 0;
		labelGbc.gridy = 0;
		labelGbc.anchor = GridBagConstraints.WEST;
		labelGbc.insets = new Insets(0, 0, 0, 16);
		JLabel label = new JLabel(item.label());
		label.setPreferredSize(new Dimension(labelWidth, label.getPreferredSize().height));
		label.setToolTipText(tooltip);
		label.addMouseListener(rowMouseListener);
		row.add(label, labelGbc);

		JLabel valueLabel = new JLabel(item.value());
		valueLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, valueLabel.getFont().getSize()));
		valueLabel.setToolTipText(tooltip);
		valueLabel.addMouseListener(rowMouseListener);
		GridBagConstraints valueGbc = new GridBagConstraints();
		valueGbc.gridx = 1;
		valueGbc.gridy = 0;
		valueGbc.anchor = GridBagConstraints.WEST;
		valueGbc.weightx = 1.0;
		row.add(valueLabel, valueGbc);

		if (onDelete != null) {
			JButton deleteButton = new JButton("✕");
			deleteButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
			deleteButton.setMargin(new Insets(0, 4, 0, 4));
			deleteButton.setToolTipText(deleteTooltip);
			deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			deleteButton.addActionListener(_ -> {
				onDelete.accept(item);
				// Clicking the button shifts focus to it; refresh()'s reopen check only keeps the popup
				// open while the field (or a declared related component) has focus, so hand focus back
				// to the field first or the popup would simply vanish instead of redrawing without this item.
				field.requestFocusInWindow();
				refresh();
			});
			GridBagConstraints deleteGbc = new GridBagConstraints();
			deleteGbc.gridx = 2;
			deleteGbc.gridy = 0;
			deleteGbc.anchor = GridBagConstraints.EAST;
			deleteGbc.insets = new Insets(0, 8, 0, 0);
			row.add(deleteButton, deleteGbc);
		}

		gbc.gridy++;
		content.add(row, gbc);
	}

	private static int calculateLabelWidth(List<SuggestionSection> sections) {
		int width = 0;
		for (SuggestionSection section : sections) {
			for (SuggestionItem item : section.items()) {
				JLabel label = new JLabel(item.label());
				width = Math.max(width, label.getPreferredSize().width);
			}
		}
		return width + LABEL_COLUMN_PADDING;
	}

	private static Color createHoverBackground(Color defaultBackground) {
		Color color = UIManager.getColor("List.hoverBackground");
		if (color != null) {
			return color;
		}
		color = UIManager.getColor("MenuItem.selectionBackground");
		if (color != null) {
			return color;
		}
		return new Color(defaultBackground.getRed(), defaultBackground.getGreen(), defaultBackground.getBlue(), 24);
	}

	record SuggestionSection(String label, List<SuggestionItem> items) {
	}

	record SuggestionItem(String label, String value, String tooltip) {
		SuggestionItem(String label, String value) {
			this(label, value, null);
		}
	}
}
