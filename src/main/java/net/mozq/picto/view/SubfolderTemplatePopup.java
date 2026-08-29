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
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import net.mozq.nanotemplate.NanoTemplate;
import net.mozq.nanotemplate.NanoTemplateException;

class SubfolderTemplatePopup {
	private static final String KEY_PREFIX = "MainFrame.destSubPathInsert."; //$NON-NLS-1$
	private static final int POPUP_MAX_HEIGHT = 280;
	private static final int POPUP_MIN_WIDTH = 440;
	private static final int LABEL_COLUMN_PADDING = 8;
	private static final Map<String, Object> SAMPLE_VALUES = Map.ofEntries(
			Map.entry("ParentSubPath", "events/summer"), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("FileName", "IMG_0001.jpg"), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("BaseName", "IMG_0001"), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("Extension", "jpg"), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("PhotoTakenDate", Date.from(Instant.parse("2023-04-05T12:34:56Z"))), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("Make", "Canon"), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("Model", "EOS R50"), //$NON-NLS-1$ //$NON-NLS-2$
			Map.entry("FNumber", 5.6), //$NON-NLS-1$
			Map.entry("ISO", 200), //$NON-NLS-1$
			Map.entry("GPSLat", 35.681236), //$NON-NLS-1$
			Map.entry("GPSLon", 139.767125)); //$NON-NLS-1$
	private static final TimeZone SAMPLE_TIME_ZONE = TimeZone.getTimeZone("UTC"); //$NON-NLS-1$
	private static SubfolderTemplatePopup activePopup;

	private final JTextComponent field;
	private final JPanel popupPanel = new JPanel(new BorderLayout());
	private Popup popup;
	private boolean windowFocusListenerInstalled;
	private boolean refreshScheduled;

	SubfolderTemplatePopup(JTextComponent field) {
		this.field = field;
		this.popupPanel.setBorder(createPopupBorder());
		this.popupPanel.add(createContent(), BorderLayout.CENTER);
		installListeners();
	}

	private static Border createPopupBorder() {
		Border border = UIManager.getBorder("PopupMenu.border"); //$NON-NLS-1$
		if (border != null) {
			return border;
		}
		Color color = UIManager.getColor("Component.borderColor"); //$NON-NLS-1$
		if (color == null) {
			color = Color.GRAY;
		}
		return BorderFactory.createLineBorder(color);
	}

	private void installListeners() {
		SwingUtilities.invokeLater(this::installWindowFocusListener);
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(SubfolderTemplatePopup.this::showPopup);
			}

			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(SubfolderTemplatePopup.this::hidePopupIfFocusMovedAway);
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
		Window window = SwingUtilities.getWindowAncestor(field);
		if (window == null) {
			return;
		}
		window.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				hidePopup();
			}
		});
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				refreshPopup();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				refreshPopup();
			}
		});
		windowFocusListenerInstalled = true;
	}

	private void showPopup() {
		if (!field.isEnabled() || !field.isFocusOwner() || popup != null) {
			return;
		}
		if (activePopup != null && activePopup != this) {
			activePopup.hidePopup();
		}
		activePopup = this;
		try {
			Dimension preferredSize = popupPanel.getPreferredSize();
			int width = Math.max(Math.max(field.getWidth(), POPUP_MIN_WIDTH), preferredSize.width);
			int height = Math.min(preferredSize.height, POPUP_MAX_HEIGHT);
			popupPanel.setPreferredSize(new Dimension(width, height));
			Point location = field.getLocationOnScreen();
			popup = PopupFactory.getSharedInstance().getPopup(field, popupPanel, location.x, location.y + field.getHeight());
			popup.show();
		} catch (IllegalComponentStateException e) {
			hidePopup();
		}
	}

	private void hidePopupIfFocusMovedAway() {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focusOwner == null || focusOwner == field || SwingUtilities.isDescendingFrom(focusOwner, popupPanel)) {
			return;
		}
		hidePopup();
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
			if (popup == null || !field.isEnabled() || !field.isFocusOwner()) {
				return;
			}
			hidePopup();
			showPopup();
		});
	}

	private JScrollPane createContent() {
		List<SnippetItem> templateItems = List.of(
				new SnippetItem("byPhotoDate", "${PhotoTakenDate:uuuu-MM-dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("byPhotoYearMonthDate", "${PhotoTakenDate:uuuu/MM/dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("byParentAndPhotoDate", "${ParentSubPath}/${PhotoTakenDate:uuuu-MM-dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("byCamera", "${Make}/${Model}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("byCameraAndPhotoDate", "${Make}/${Model}/${PhotoTakenDate:uuuu-MM-dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("byPhotoDateAndCamera", "${PhotoTakenDate:uuuu-MM-dd}/${Make}/${Model}/${FileName}")); //$NON-NLS-1$ //$NON-NLS-2$
		List<SnippetItem> dateItems = List.of(
				new SnippetItem("photoTakenDate", "${PhotoTakenDate:uuuu-MM-dd}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("photoTakenYearMonthDate", "${PhotoTakenDate:uuuu/MM/dd}")); //$NON-NLS-1$ //$NON-NLS-2$
		List<SnippetItem> fileItems = List.of(
				new SnippetItem("parentSubfolder", "${ParentSubPath}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("fileName", "${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("baseName", "${BaseName}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("extension", "${Extension}")); //$NON-NLS-1$ //$NON-NLS-2$
		List<SnippetItem> exifItems = List.of(
				new SnippetItem("make", "${Make}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("model", "${Model}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("fNumber", "${FNumber:0.0}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("iso", "${ISO}")); //$NON-NLS-1$ //$NON-NLS-2$
		List<SnippetItem> gpsItems = List.of(
				new SnippetItem("latitude", "${GPSLat:0.000000}"), //$NON-NLS-1$ //$NON-NLS-2$
				new SnippetItem("longitude", "${GPSLon:0.000000}")); //$NON-NLS-1$ //$NON-NLS-2$
		int labelWidth = calculateLabelWidth(templateItems, dateItems, fileItems, exifItems, gpsItems);

		JPanel content = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 0, 0);
		addSection(content, gbc, "template"); //$NON-NLS-1$
		addItems(content, gbc, templateItems, labelWidth);
		addSection(content, gbc, "date"); //$NON-NLS-1$
		addItems(content, gbc, dateItems, labelWidth);
		addSection(content, gbc, "file"); //$NON-NLS-1$
		addItems(content, gbc, fileItems, labelWidth);
		addSection(content, gbc, "exif"); //$NON-NLS-1$
		addItems(content, gbc, exifItems, labelWidth);
		addSection(content, gbc, "gps"); //$NON-NLS-1$
		addItems(content, gbc, gpsItems, labelWidth);

		gbc.gridy++;
		gbc.weighty = 1.0;
		content.add(new JPanel(), gbc);

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	private void addSection(JPanel content, GridBagConstraints gbc, String key) {
		JLabel label = new JLabel(Messages.getString(KEY_PREFIX + key));
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		label.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
		gbc.gridy++;
		content.add(label, gbc);
	}

	private void addItems(JPanel content, GridBagConstraints gbc, List<SnippetItem> items, int labelWidth) {
		for (SnippetItem item : items) {
			addItem(content, gbc, item, labelWidth);
		}
	}

	private void addItem(JPanel content, GridBagConstraints gbc, SnippetItem item, int labelWidth) {
		JPanel row = new JPanel(new GridBagLayout());
		Color defaultBackground = row.getBackground();
		Color hoverBackground = createHoverBackground(defaultBackground);
		row.setOpaque(true);
		row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		row.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 10));
		row.setToolTipText(Messages.getString(KEY_PREFIX + "sample", renderSample(item.snippet()))); //$NON-NLS-1$
		row.addMouseListener(new MouseAdapter() {
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
				field.replaceSelection(item.snippet());
				hidePopup();
				field.requestFocusInWindow();
			}
		});

		GridBagConstraints labelGbc = new GridBagConstraints();
		labelGbc.gridx = 0;
		labelGbc.gridy = 0;
		labelGbc.anchor = GridBagConstraints.WEST;
		labelGbc.insets = new Insets(0, 0, 0, 16);
		JLabel label = new JLabel(Messages.getString(KEY_PREFIX + item.key()));
		label.setPreferredSize(new Dimension(labelWidth, label.getPreferredSize().height));
		row.add(label, labelGbc);

		JLabel valueLabel = new JLabel(item.snippet());
		valueLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, valueLabel.getFont().getSize()));
		GridBagConstraints valueGbc = new GridBagConstraints();
		valueGbc.gridx = 1;
		valueGbc.gridy = 0;
		valueGbc.anchor = GridBagConstraints.WEST;
		valueGbc.weightx = 1.0;
		row.add(valueLabel, valueGbc);

		gbc.gridy++;
		content.add(row, gbc);
	}

	@SafeVarargs
	private static int calculateLabelWidth(List<SnippetItem>... itemGroups) {
		int width = 0;
		for (List<SnippetItem> group : itemGroups) {
			for (SnippetItem item : group) {
				JLabel label = new JLabel(Messages.getString(KEY_PREFIX + item.key()));
				width = Math.max(width, label.getPreferredSize().width);
			}
		}
		return width + LABEL_COLUMN_PADDING;
	}

	private static String renderSample(String snippet) {
		try {
			return new NanoTemplate(snippet)
					.timeZone(SAMPLE_TIME_ZONE)
					.render(SAMPLE_VALUES);
		} catch (NanoTemplateException e) {
			return snippet;
		}
	}

	private static Color createHoverBackground(Color defaultBackground) {
		Color color = UIManager.getColor("List.hoverBackground"); //$NON-NLS-1$
		if (color != null) {
			return color;
		}
		color = UIManager.getColor("MenuItem.selectionBackground"); //$NON-NLS-1$
		if (color != null) {
			return color;
		}
		return new Color(defaultBackground.getRed(), defaultBackground.getGreen(), defaultBackground.getBlue(), 24);
	}

	private record SnippetItem(String key, String snippet) {
	}
}
