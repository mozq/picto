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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class DateTimeInputPopup {

	private static final int FIRST_YEAR = 1900;
	private static final int FUTURE_YEARS = 5;
	private static final int DATE_PART_END = 10;
	private static final int TIME_PART_START = 11;
	private static final int[] DATE_INDEXES = {0, 1, 2, 3, 5, 6, 8, 9};
	private static final int[] TIME_INDEXES = {11, 12, 14, 15, 17, 18};
	private static DateTimeInputPopup activePopup;

	private final JFormattedTextField field;
	private final boolean endOfRange;
	private final Locale locale;
	private final JPanel popupPanel = new JPanel(new BorderLayout());
	private final JComboBox<Integer> cmbYear = new JComboBox<>();
	private final JComboBox<MonthItem> cmbMonth = new JComboBox<>();
	private final JPanel pnlDays = new JPanel(new GridLayout(7, 7, 2, 2));
	private JPanel datePanel;
	private Popup popup;
	private boolean updating;
	private boolean windowFocusListenerInstalled;
	private PopupMode popupMode;
	private YearMonth visibleMonth;

	DateTimeInputPopup(JFormattedTextField field, boolean endOfRange, Locale locale) {
		this.field = field;
		this.endOfRange = endOfRange;
		this.locale = locale;
		this.visibleMonth = representativeMonth(parseParts(field.getText()));
		this.popupPanel.setBorder(createPopupBorder());
		buildDatePopup();
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
				if (field.hasFocus()) {
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
		windowFocusListenerInstalled = true;
	}

	private void showPopupForCaret() {
		if (!field.isEnabled() || !field.isFocusOwner()) {
			hidePopup();
			return;
		}
		DateParts parts = parseParts(field.getText());
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
		addTimeButton(panel, Messages.getString("DateTimeInputPopup.currentTime"), LocalTime.now()); //$NON-NLS-1$
		addTimeButton(panel, "00:00:00", LocalTime.MIDNIGHT); //$NON-NLS-1$
		addTimeButton(panel, "12:00:00", LocalTime.NOON); //$NON-NLS-1$
		addTimeButton(panel, "23:59:59", LocalTime.of(23, 59, 59)); //$NON-NLS-1$
		setPopupContent(panel, PopupMode.TIME);
		showPopup();
	}

	private void addTimeButton(JPanel panel, String label, LocalTime time) {
		JButton button = new JButton(label);
		button.addActionListener((ActionEvent e) -> {
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
		} catch (IllegalComponentStateException e) {
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
		if (cmbYear.isPopupVisible() || cmbMonth.isPopupVisible()) {
			return;
		}
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focusOwner == null || focusOwner == field || SwingUtilities.isDescendingFrom(focusOwner, popupPanel)) {
			return;
		}
		hidePopup();
	}
	
	private void hidePopupInstance() {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
	}

	private void buildDatePopup() {
		datePanel = new JPanel(new BorderLayout(4, 4));
		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		JButton btnPrev = new JButton("<"); //$NON-NLS-1$
		JButton btnNext = new JButton(">"); //$NON-NLS-1$
		btnPrev.addActionListener((ActionEvent e) -> moveMonth(-1));
		btnNext.addActionListener((ActionEvent e) -> moveMonth(1));
		for (int year = FIRST_YEAR; year <= LocalDate.now().getYear() + FUTURE_YEARS; year++) {
			cmbYear.addItem(Integer.valueOf(year));
		}
		for (int month = 1; month <= 12; month++) {
			cmbMonth.addItem(new MonthItem(month, locale));
		}
		cmbYear.addActionListener((ActionEvent e) -> {
			if (!updating && cmbYear.getSelectedItem() instanceof Integer) {
				int year = ((Integer)cmbYear.getSelectedItem()).intValue();
				visibleMonth = YearMonth.of(year, visibleMonth.getMonthValue());
				setDateText(year, null, null);
				refreshDatePopup(parseParts(field.getText()));
			}
		});
		cmbMonth.addActionListener((ActionEvent e) -> {
			if (!updating && cmbMonth.getSelectedItem() instanceof MonthItem) {
				MonthItem month = (MonthItem)cmbMonth.getSelectedItem();
				visibleMonth = YearMonth.of(visibleMonth.getYear(), month.value);
				setDateText(visibleMonth.getYear(), visibleMonth.getMonthValue(), null);
				refreshDatePopup(parseParts(field.getText()));
			}
		});
		JButton btnToday = new JButton(Messages.getString("DateTimeInputPopup.today")); //$NON-NLS-1$
		btnToday.addActionListener((ActionEvent e) -> {
			LocalDate today = LocalDate.now();
			visibleMonth = YearMonth.from(today);
			setDateAndShowTimePopup(today.getYear(), today.getMonthValue(), today.getDayOfMonth());
		});
		header.add(btnPrev);
		header.add(cmbYear);
		header.add(new JLabel("/")); //$NON-NLS-1$
		header.add(cmbMonth);
		header.add(btnNext);
		header.add(btnToday);
		datePanel.add(header, BorderLayout.NORTH);
		datePanel.add(pnlDays, BorderLayout.CENTER);
		refreshDatePopup(parseParts(field.getText()));
	}

	private void moveMonth(int months) {
		visibleMonth = visibleMonth.plusMonths(months);
		refreshDatePopup(parseParts(field.getText()));
	}

	private void refreshFromField() {
		if (updating || popup == null || popupMode != PopupMode.DATE) {
			return;
		}
		DateParts parts = parseParts(field.getText());
		if (!isTimePart(field.getCaretPosition()) || !parts.hasDate()) {
			visibleMonth = representativeMonth(parts);
			refreshDatePopup(parts);
		}
	}

	private void refreshDatePopup(DateParts parts) {
		updating = true;
		try {
			cmbYear.setSelectedItem(Integer.valueOf(visibleMonth.getYear()));
			cmbMonth.setSelectedItem(new MonthItem(visibleMonth.getMonthValue(), locale));
			pnlDays.removeAll();
			String[] weekdays = Messages.getString("DateTimeInputPopup.weekdays").split(","); //$NON-NLS-1$ //$NON-NLS-2$
			for (String weekday : weekdays) {
				pnlDays.add(new JLabel(weekday, JLabel.CENTER));
			}
			LocalDate firstDay = visibleMonth.atDay(1);
			int leadingDays = firstDay.getDayOfWeek().getValue() % 7;
			for (int i = 0; i < leadingDays; i++) {
				pnlDays.add(new JLabel()); //$NON-NLS-1$
			}
			for (int day = 1; day <= visibleMonth.lengthOfMonth(); day++) {
				JButton button = new JButton(Integer.toString(day));
				button.setMargin(new java.awt.Insets(2, 4, 2, 4));
				button.setSelected(parts.matches(visibleMonth.getYear(), visibleMonth.getMonthValue(), day));
				final int selectedDay = day;
				button.addActionListener((ActionEvent e) -> {
					setDateAndShowTimePopup(visibleMonth.getYear(), visibleMonth.getMonthValue(), selectedDay);
				});
				pnlDays.add(button);
			}
			for (int i = pnlDays.getComponentCount(); i < 49; i++) {
				pnlDays.add(new JLabel()); //$NON-NLS-1$
			}
			pnlDays.revalidate();
			pnlDays.repaint();
			popupPanel.revalidate();
			popupPanel.repaint();
		} finally {
			updating = false;
		}
	}

	private void setDateText(int year, Integer month, Integer day) {
		StringBuilder text = new StringBuilder();
		text.append(String.format(Locale.ROOT, "%04d", Integer.valueOf(year))); //$NON-NLS-1$
		if (month != null) {
			text.append('/').append(String.format(Locale.ROOT, "%02d", month)); //$NON-NLS-1$
		}
		if (day != null) {
			YearMonth yearMonth = YearMonth.of(year, month.intValue());
			int normalizedDay = Math.min(day.intValue(), yearMonth.lengthOfMonth());
			text.append('/').append(String.format(Locale.ROOT, "%02d", Integer.valueOf(normalizedDay))); //$NON-NLS-1$
		}
		setDatePart(text.toString());
	}
	
	private void setDateAndShowTimePopup(int year, int month, int day) {
		setDateText(year, Integer.valueOf(month), Integer.valueOf(day));
		field.setCaretPosition(TIME_PART_START);
		showTimePopup();
	}

	private void setDatePart(String dateText) {
		char[] chars = ensureMaskText(field.getText()).toCharArray();
		for (int index : DATE_INDEXES) {
			chars[index] = '_';
		}
		String digits = dateText.replace("/", ""); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < DATE_INDEXES.length && i < digits.length(); i++) {
			chars[DATE_INDEXES[i]] = digits.charAt(i);
		}
		setFieldText(new String(chars));
		field.setCaretPosition(Math.min(dateText.length(), DATE_PART_END));
	}

	private void setTime(LocalTime time) {
		String current = ensureMaskText(field.getText());
		char[] chars = current.toCharArray();
		String formattedTime = String.format(Locale.ROOT, "%02d%02d%02d", //$NON-NLS-1$
				Integer.valueOf(time.getHour()), Integer.valueOf(time.getMinute()), Integer.valueOf(time.getSecond()));
		for (int i = 0; i < TIME_INDEXES.length && i < formattedTime.length(); i++) {
			if (TIME_INDEXES[i] < chars.length) {
				chars[TIME_INDEXES[i]] = formattedTime.charAt(i);
			}
		}
		setFieldText(new String(chars));
		field.setCaretPosition(Math.min(current.length(), TIME_PART_START + 8));
	}
	
	private void setFieldText(String text) {
		field.setValue(text);
		field.setText(text);
	}

	private static String ensureMaskText(String text) {
		if (text == null || text.length() < TIME_INDEXES[TIME_INDEXES.length - 1] + 1) {
			return "____/__/__ __:__:__"; //$NON-NLS-1$
		}
		return text;
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
		return caretPosition >= TIME_PART_START;
	}

	private static DateParts parseParts(String text) {
		if (text == null) {
			return new DateParts(null, null, null);
		}
		String[] dateTime = text.split(" ", 2); //$NON-NLS-1$
		String[] dateParts = dateTime.length > 0 ? dateTime[0].split("/", -1) : new String[0]; //$NON-NLS-1$
		Integer year = getNumber(dateParts, 0);
		Integer month = getNumber(dateParts, 1);
		Integer day = getNumber(dateParts, 2);
		if (month != null) {
			month = Integer.valueOf(Math.max(1, Math.min(12, month.intValue())));
		}
		if (year != null && month != null && day != null) {
			day = Integer.valueOf(Math.max(1, Math.min(YearMonth.of(year.intValue(), month.intValue()).lengthOfMonth(), day.intValue())));
		}
		return new DateParts(year, month, day);
	}

	private static Integer getNumber(String[] values, int index) {
		if (index >= values.length) {
			return null;
		}
		String value = values[index].replace("_", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (value.isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static class DateParts {
		final Integer year;
		final Integer month;
		final Integer day;

		DateParts(Integer year, Integer month, Integer day) {
			this.year = year;
			this.month = month;
			this.day = day;
		}

		boolean hasDate() {
			return year != null || month != null || day != null;
		}

		boolean matches(int year, int month, int day) {
			return this.year != null && this.year.intValue() == year
					&& this.month != null && this.month.intValue() == month
					&& this.day != null && this.day.intValue() == day;
		}
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
