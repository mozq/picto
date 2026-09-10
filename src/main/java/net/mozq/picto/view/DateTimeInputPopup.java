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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.mozq.picto.view.DateTimeText.DateParts;

class DateTimeInputPopup {

	private static final int FIRST_YEAR = 1900;
	private static final int FUTURE_YEARS = 5;
	private static final String DAY_PROPERTY = "picto.day";
	private static DateTimeInputPopup activePopup;

	private final JFormattedTextField field;
	private final boolean endOfRange;
	private final Locale locale;
	private final JPanel popupPanel = new JPanel(new BorderLayout());
	private final JComboBox<Integer> cmbYear = new JComboBox<>();
	private final JComboBox<MonthItem> cmbMonth = new JComboBox<>();
	private final JPanel pnlDays = new JPanel(new GridLayout(7, 7, 2, 2));
	private final JLabel[] weekdayLabels = new JLabel[7];
	// Package-private (not private): lets a test locate a specific day's rendered button directly, e.g. to
	// pin down that refreshDatePopup() leaves an already-correct, mid-click button's armed/pressed state alone.
	final JButton[] dayButtons = new JButton[42];
	private JPanel datePanel;
	private Popup popup;
	private boolean updating;
	private boolean windowFocusListenerInstalled;
	private final PopupSupport.DebouncedRefresh debouncedRefresh = new PopupSupport.DebouncedRefresh();
	private PopupMode popupMode;
	// Package-private (not private): lets a test drive refreshDatePopup() for a specific month.
	YearMonth visibleMonth;

	DateTimeInputPopup(JFormattedTextField field, boolean endOfRange, Locale locale) {
		this.field = field;
		this.endOfRange = endOfRange;
		this.locale = locale;
		this.visibleMonth = representativeMonth(DateTimeText.parseParts(field.getText()));
		this.popupPanel.setBorder(PopupSupport.createPopupBorder());
		buildDatePopup();
		installListeners();
	}

	private void installListeners() {
		PopupSupport.installFocusOwnerChangeListener(() -> popup != null, this::hidePopupIfFocusMovedAway);
		SwingUtilities.invokeLater(this::installWindowFocusListener);
		field.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(DateTimeInputPopup.this::showPopupForCaret);
			}

			@Override
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(DateTimeInputPopup.this::hidePopupIfFocusMovedAway);
			}
		});
		field.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupForCaret();
			}
		});
		field.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				if (!updating && field.hasFocus()) {
					showPopupForCaret();
				}
			}
		});
		field.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				refreshFromField();
			}

			public void removeUpdate(DocumentEvent e) {
				refreshFromField();
			}

			public void changedUpdate(DocumentEvent e) {
				refreshFromField();
			}
		});
	}

	private void installWindowFocusListener() {
		if (windowFocusListenerInstalled) {
			return;
		}
		windowFocusListenerInstalled = PopupSupport.installWindowFocusListener(field, this::hidePopup, this::refreshPopupLocation);
	}

	private void showPopupForCaret() {
		if (!field.isEnabled() || !field.isFocusOwner()) {
			hidePopup();
			return;
		}
		DateParts parts = DateTimeText.parseParts(field.getText());
		if (isTimePart(field.getCaretPosition()) && parts.hasDate()) {
			showTimePopup();
		} else {
			showDatePopup(parts);
		}
	}

	private void showDatePopup(DateParts parts) {
		if (popupMode != PopupMode.DATE) {
			setPopupContent(datePanel, PopupMode.DATE);
		}
		visibleMonth = representativeMonth(parts);
		refreshDatePopup(parts);
		showPopup();
	}

	private void showTimePopup() {
		if (popupMode == PopupMode.TIME) {
			showPopup();
			return;
		}
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		addTimeButton(panel, Messages.getString("DateTimeInputPopup.currentTime"), LocalTime.now());
		addTimeButton(panel, "00:00:00", LocalTime.MIDNIGHT);
		addTimeButton(panel, "12:00:00", LocalTime.NOON);
		addTimeButton(panel, "23:59:59", LocalTime.of(23, 59, 59));
		setPopupContent(panel, PopupMode.TIME);
		showPopup();
	}

	private void addTimeButton(JPanel panel, String label, LocalTime time) {
		JButton button = new JButton(label);
		button.addActionListener(_ -> {
			setTime(time);
			hidePopup();
		});
		panel.add(button);
	}

	private void showPopup() {
		if (popup != null) {
			return;
		}
		if (activePopup != null && activePopup != this) {
			activePopup.hidePopup();
		}
		activePopup = this;
		try {
			Point location = field.getLocationOnScreen();
			popup = PopupFactory.getSharedInstance().getPopup(field, popupPanel, location.x, location.y + field.getHeight());
			popup.show();
		} catch (IllegalComponentStateException _) {
			hidePopup();
		}
	}

	private void setPopupContent(JPanel content, PopupMode mode) {
		hidePopupInstance();
		popupPanel.removeAll();
		popupPanel.add(content, BorderLayout.CENTER);
		popupPanel.revalidate();
		popupPanel.repaint();
		popupMode = mode;
	}

	private void hidePopup() {
		hidePopupInstance();
		popupMode = null;
		if (activePopup == this) {
			activePopup = null;
		}
	}

	private void hidePopupIfFocusMovedAway() {
		// The year/month combo boxes' own dropdown lists render in their own floating windows, not as
		// descendants of popupPanel or field, so a focus transition while one is open (including a
		// transient null focus owner some look-and-feels report mid-transition) would otherwise be
		// misread by shouldHidePopup as focus having left the popup entirely, closing the whole calendar
		// out from under the user's in-progress year/month selection. isAlsoAllowed here ignores the focus
		// owner it's given and checks isPopupVisible() directly instead, since it must also accept the
		// transient-null case shouldHidePopup's null branch consults it for.
		if (PopupSupport.shouldHidePopup(field, popupPanel, _ -> cmbYear.isPopupVisible() || cmbMonth.isPopupVisible())) {
			hidePopup();
		}
	}

	private void hidePopupInstance() {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
	}

	private void refreshPopupLocation() {
		if (popup == null) {
			return;
		}
		debouncedRefresh.request(
				() -> popup != null && field.isEnabled() && isPopupFocusActive(),
				() -> {
					hidePopupInstance();
					showPopup();
				});
	}

	private boolean isPopupFocusActive() {
		return PopupSupport.isPopupFocusActive(field, popupPanel, _ -> false);
	}

	private void buildDatePopup() {
		datePanel = new JPanel(new BorderLayout(4, 4));
		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		JButton btnPrev = new JButton("<");
		JButton btnNext = new JButton(">");
		btnPrev.addActionListener(_ -> moveMonth(-1));
		btnNext.addActionListener(_ -> moveMonth(1));
		for (int year = FIRST_YEAR; year <= LocalDate.now().getYear() + FUTURE_YEARS; year++) {
			cmbYear.addItem(Integer.valueOf(year));
		}
		for (int month = 1; month <= 12; month++) {
			cmbMonth.addItem(new MonthItem(month, locale));
		}
		cmbYear.addActionListener(_ -> {
			if (!updating && cmbYear.getSelectedItem() instanceof Integer) {
				int year = ((Integer)cmbYear.getSelectedItem()).intValue();
				visibleMonth = YearMonth.of(year, visibleMonth.getMonthValue());
				updateDateFromHeader(year, null);
			}
		});
		cmbMonth.addActionListener(_ -> {
			if (!updating && cmbMonth.getSelectedItem() instanceof MonthItem) {
				MonthItem month = (MonthItem)cmbMonth.getSelectedItem();
				visibleMonth = YearMonth.of(visibleMonth.getYear(), month.value);
				updateDateFromHeader(visibleMonth.getYear(), visibleMonth.getMonthValue());
			}
		});
		JButton btnToday = new JButton(Messages.getString("DateTimeInputPopup.today"));
		btnToday.addActionListener(_ -> {
			LocalDate today = LocalDate.now();
			visibleMonth = YearMonth.from(today);
			setDateAndShowTimePopup(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
		});
		header.add(btnPrev);
		header.add(cmbYear);
		header.add(new JLabel("/"));
		header.add(cmbMonth);
		header.add(btnNext);
		header.add(btnToday);
		datePanel.add(header, BorderLayout.NORTH);
		buildDayCells();
		datePanel.add(pnlDays, BorderLayout.CENTER);
		refreshDatePopup(DateTimeText.parseParts(field.getText()));
	}

	// Package-private (not private): see dayButtons.
	void moveMonth(int months) {
		visibleMonth = visibleMonth.plusMonths(months);
		refreshDatePopup(DateTimeText.parseParts(field.getText()));
	}

	private void refreshFromField() {
		if (updating || popup == null || popupMode != PopupMode.DATE) {
			return;
		}
		DateParts parts = DateTimeText.parseParts(field.getText());
		if (!isTimePart(field.getCaretPosition()) || !parts.hasDate()) {
			visibleMonth = representativeMonth(parts);
			refreshDatePopup(parts);
		}
	}

	// Package-private (not private): see dayButtons.
	void refreshDatePopup(DateParts parts) {
		updating = true;
		try {
			cmbYear.setSelectedItem(Integer.valueOf(visibleMonth.getYear()));
			cmbMonth.setSelectedItem(new MonthItem(visibleMonth.getMonthValue(), locale));
			String[] weekdays = Messages.getString("DateTimeInputPopup.weekdays").split(",");
			for (int i = 0; i < weekdayLabels.length; i++) {
				weekdayLabels[i].setText(i < weekdays.length ? weekdays[i] : "");
			}
			LocalDate firstDay = visibleMonth.atDay(1);
			int leadingDays = firstDay.getDayOfWeek().getValue() % 7;
			int lastActiveIndex = leadingDays + visibleMonth.lengthOfMonth() - 1;
			for (int i = 0; i < dayButtons.length; i++) {
				JButton button = dayButtons[i];
				if (i < leadingDays || i > lastActiveIndex) {
					if (button.getClientProperty(DAY_PROPERTY) != null) {
						setEmptyDayButton(button);
					}
					continue;
				}
				int day = i - leadingDays + 1;
				// Only touch a cell whose displayed state actually needs to change: refreshDatePopup() can run
				// re-entrantly (e.g. a focus event re-showing the popup) while a day button's mouse press is
				// still in progress, and JButton.setEnabled(false) unconditionally clears the button's
				// armed/pressed state as a side effect - disarming a button the user is mid-click on and
				// silently swallowing that click.
				if (!Integer.valueOf(day).equals(button.getClientProperty(DAY_PROPERTY))) {
					button.setText(Integer.toString(day));
					button.putClientProperty(DAY_PROPERTY, Integer.valueOf(day));
				}
				if (!button.isEnabled()) {
					button.setEnabled(true);
					button.setBorderPainted(true);
					button.setContentAreaFilled(true);
				}
				boolean selected = parts.matches(visibleMonth.getYear(), visibleMonth.getMonthValue(), day);
				if (button.isSelected() != selected) {
					button.setSelected(selected);
				}
			}
			pnlDays.revalidate();
			pnlDays.repaint();
			popupPanel.revalidate();
			popupPanel.repaint();
		} finally {
			updating = false;
		}
	}

	private void updateDateFromHeader(int year, Integer month) {
		updating = true;
		try {
			setDateText(year, month, null);
		} finally {
			updating = false;
		}
		refreshDatePopup(DateTimeText.parseParts(field.getText()));
	}

	private void buildDayCells() {
		for (int i = 0; i < weekdayLabels.length; i++) {
			weekdayLabels[i] = new JLabel("", JLabel.CENTER);
			pnlDays.add(weekdayLabels[i]);
		}
		for (int i = 0; i < dayButtons.length; i++) {
			JButton button = new JButton();
			button.setMargin(new java.awt.Insets(2, 4, 2, 4));
			button.addActionListener(_ -> {
				Object day = button.getClientProperty(DAY_PROPERTY);
				if (day instanceof Integer) {
					setDateAndShowTimePopup(visibleMonth.getYear(), visibleMonth.getMonthValue(), ((Integer)day).intValue());
				}
			});
			dayButtons[i] = button;
			pnlDays.add(button);
		}
	}

	private static void setEmptyDayButton(JButton button) {
		button.setText("");
		button.putClientProperty(DAY_PROPERTY, null);
		button.setSelected(false);
		button.setEnabled(false);
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
	}

	private void setDateText(int year, Integer month, Integer day) {
		StringBuilder text = new StringBuilder();
		text.append(String.format(Locale.ROOT, "%04d", Integer.valueOf(year)));
		if (month != null) {
			text.append('/').append(String.format(Locale.ROOT, "%02d", month));
		}
		if (day != null) {
			YearMonth yearMonth = YearMonth.of(year, month.intValue());
			int normalizedDay = Math.min(day.intValue(), yearMonth.lengthOfMonth());
			text.append('/').append(String.format(Locale.ROOT, "%02d", Integer.valueOf(normalizedDay)));
		}
		setDatePart(text.toString());
	}

	private void setDateAndShowTimePopup(int year, int month, int day) {
		setDateText(year, Integer.valueOf(month), Integer.valueOf(day));
		field.setCaretPosition(DateTimeText.TIME_PART_START);
		showTimePopup();
	}

	private void setDatePart(String dateText) {
		char[] chars = DateTimeText.ensureMaskText(field.getText()).toCharArray();
		for (int index : DateTimeText.DATE_INDEXES) {
			chars[index] = '_';
		}
		String digits = dateText.replace("/", "");
		for (int i = 0; i < DateTimeText.DATE_INDEXES.length && i < digits.length(); i++) {
			chars[DateTimeText.DATE_INDEXES[i]] = digits.charAt(i);
		}
		setFieldText(new String(chars));
		field.setCaretPosition(Math.min(dateText.length(), DateTimeText.DATE_PART_END));
	}

	private void setTime(LocalTime time) {
		String current = DateTimeText.ensureMaskText(field.getText());
		char[] chars = current.toCharArray();
		String formattedTime = String.format(Locale.ROOT, "%02d%02d%02d",
				Integer.valueOf(time.getHour()), Integer.valueOf(time.getMinute()), Integer.valueOf(time.getSecond()));
		for (int i = 0; i < DateTimeText.TIME_INDEXES.length && i < formattedTime.length(); i++) {
			if (DateTimeText.TIME_INDEXES[i] < chars.length) {
				chars[DateTimeText.TIME_INDEXES[i]] = formattedTime.charAt(i);
			}
		}
		setFieldText(new String(chars));
		field.setCaretPosition(Math.min(current.length(), DateTimeText.TIME_PART_START + 8));
	}

	private void setFieldText(String text) {
		field.setValue(text);
		field.setText(text);
	}

	private YearMonth representativeMonth(DateParts parts) {
		LocalDate today = LocalDate.now();
		if (parts.year == null) {
			return YearMonth.from(today);
		}
		int month = parts.month != null ? parts.month.intValue() : (endOfRange ? 12 : 1);
		return YearMonth.of(parts.year.intValue(), month);
	}

	private static boolean isTimePart(int caretPosition) {
		return caretPosition >= DateTimeText.TIME_PART_START;
	}

	private static class MonthItem {
		final int value;
		private final String name;

		MonthItem(int value, Locale locale) {
			this.value = value;
			this.name = java.time.Month.of(value).getDisplayName(TextStyle.FULL, locale);
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof MonthItem && ((MonthItem)obj).value == value;
		}

		@Override
		public int hashCode() {
			return Integer.hashCode(value);
		}
	}

	private enum PopupMode {
		DATE,
		TIME
	}
}
