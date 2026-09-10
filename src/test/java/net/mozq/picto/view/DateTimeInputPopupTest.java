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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.YearMonth;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;

import org.junit.jupiter.api.Test;

import net.mozq.picto.view.DateTimeText.DateParts;

/**
 * Covers {@link DateTimeInputPopup#refreshDatePopup}, the calendar grid renderer. A real regression here
 * (fixed in this app) called {@code JButton.setEnabled(false)} unconditionally on every one of the 42 day
 * cells on every refresh; that call also clears a button's armed/pressed state as a side effect, and
 * refreshDatePopup() can run re-entrantly (e.g. a focus event re-showing the popup) while the user's mouse
 * press on a day button is still in progress - silently swallowing that click. {@code refreshDatePopup},
 * {@code dayButtons}, {@code visibleMonth}, and {@code moveMonth} are package-private specifically so this
 * can be driven directly, since the popup's own showPopupForCaret()/isPopupFocusActive() paths need a real
 * focused window that a test doesn't have.
 */
class DateTimeInputPopupTest {
	private static DateTimeInputPopup popup(JFormattedTextField field) {
		return new DateTimeInputPopup(field, false, Locale.US);
	}

	private static JButton dayButton(DateTimeInputPopup popup, int day) {
		for (JButton button : popup.dayButtons) {
			if (String.valueOf(day).equals(button.getText())) {
				return button;
			}
		}
		throw new AssertionError("no button rendered for day " + day);
	}

	@Test
	void rendersTheCorrectDaysForTheMonthAndDisablesTheRest() {
		JFormattedTextField field = new JFormattedTextField();
		DateTimeInputPopup popup = popup(field);
		popup.visibleMonth = YearMonth.of(2026, 9); // September has 30 days

		popup.refreshDatePopup(new DateParts(2026, 9, null));

		assertTrue(dayButton(popup, 1).isEnabled());
		assertTrue(dayButton(popup, 30).isEnabled());
		for (JButton button : popup.dayButtons) {
			assertFalse("31".equals(button.getText()), "September has no 31st, so no button should show it");
		}
	}

	@Test
	void marksTheDayMatchingThePartsAsSelected() {
		JFormattedTextField field = new JFormattedTextField();
		DateTimeInputPopup popup = popup(field);
		popup.visibleMonth = YearMonth.of(2026, 9);

		popup.refreshDatePopup(new DateParts(2026, 9, 8));

		assertTrue(dayButton(popup, 8).isSelected());
		assertFalse(dayButton(popup, 9).isSelected());
	}

	@Test
	void doesNotDisarmAButtonMidClickWhenRefreshedAgainWithTheSameTarget() {
		JFormattedTextField field = new JFormattedTextField();
		DateTimeInputPopup popup = popup(field);
		popup.visibleMonth = YearMonth.of(2026, 9);
		DateParts parts = new DateParts(2026, 9, 8);
		popup.refreshDatePopup(parts);

		JButton pressedButton = dayButton(popup, 8);
		pressedButton.getModel().setArmed(true);
		pressedButton.getModel().setPressed(true);

		// Simulates a focus event re-showing the popup while the user's mouse is still down on day 8.
		popup.refreshDatePopup(parts);

		assertTrue(pressedButton.getModel().isArmed(), "refreshing with an unchanged target must not disarm a mid-click button");
		assertTrue(pressedButton.getModel().isPressed());
	}

	@Test
	void aChangedTargetStillUpdatesTheSelectedDay() {
		JFormattedTextField field = new JFormattedTextField();
		DateTimeInputPopup popup = popup(field);
		popup.visibleMonth = YearMonth.of(2026, 9);
		popup.refreshDatePopup(new DateParts(2026, 9, 8));

		popup.refreshDatePopup(new DateParts(2026, 9, 9));

		assertFalse(dayButton(popup, 8).isSelected());
		assertTrue(dayButton(popup, 9).isSelected());
	}

	@Test
	void movingToAnotherMonthReassignsTheDayCells() {
		JFormattedTextField field = new JFormattedTextField();
		DateTimeInputPopup popup = popup(field);
		popup.visibleMonth = YearMonth.of(2026, 9);
		popup.refreshDatePopup(new DateParts(2026, 9, null));

		popup.moveMonth(1); // -> October, which has a 31st that September didn't

		assertEquals(YearMonth.of(2026, 10), popup.visibleMonth);
		boolean has31 = false;
		for (JButton button : popup.dayButtons) {
			has31 |= "31".equals(button.getText()) && button.isEnabled();
		}
		assertTrue(has31, "October has 31 days, so some cell must now show an enabled '31' button");
	}

	@Test
	void clickingADayButtonWritesTheDateIntoTheField() {
		JFormattedTextField field = new JFormattedTextField();
		field.setText(DateTimeText.MASK_DEFAULT_VALUE);
		DateTimeInputPopup popup = popup(field);
		popup.visibleMonth = YearMonth.of(2026, 9);
		popup.refreshDatePopup(new DateParts(2026, 9, null));

		dayButton(popup, 8).doClick();

		assertEquals(2026, DateTimeText.parseParts(field.getText()).year);
		assertEquals(9, DateTimeText.parseParts(field.getText()).month);
		assertEquals(8, DateTimeText.parseParts(field.getText()).day);
	}
}
