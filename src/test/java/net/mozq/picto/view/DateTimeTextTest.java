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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import net.mozq.picto.view.DateTimeText.DateParts;

/** The masked date/time text parsing and formatting behind {@link DateTimeInputPopup}; pure logic, no Swing. */
class DateTimeTextTest {
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(UTC.toZoneId());

	@Test
	void hasDateTimeTextRequiresAtLeastOneDigit() {
		assertFalse(DateTimeText.hasDateTimeText(null));
		assertFalse(DateTimeText.hasDateTimeText(DateTimeText.MASK_DEFAULT_VALUE));
		assertTrue(DateTimeText.hasDateTimeText("2026/__/__ __:__:__"));
	}

	@Test
	void compactDropsPlaceholderOnlySegmentsAndKeepsRealOnes() {
		assertEquals("", DateTimeText.compact(DateTimeText.MASK_DEFAULT_VALUE));
		assertEquals("2026", DateTimeText.compact("2026/__/__ __:__:__"));
		assertEquals("2026/09", DateTimeText.compact("2026/09/__ __:__:__"));
		assertEquals("2026/09/08 10:20", DateTimeText.compact("2026/09/08 10:20:__"));
	}

	@Test
	void ensureMaskTextFallsBackToTheDefaultForShortOrNullInput() {
		assertEquals(DateTimeText.MASK_DEFAULT_VALUE, DateTimeText.ensureMaskText(null));
		assertEquals(DateTimeText.MASK_DEFAULT_VALUE, DateTimeText.ensureMaskText("2026"));
		assertEquals("2026/09/08 10:20:30", DateTimeText.ensureMaskText("2026/09/08 10:20:30"));
	}

	@Test
	void parsePartsExtractsYearMonthDayClampingMonthAndDay() {
		DateParts parts = DateTimeText.parseParts("2026/09/08 __:__:__");
		assertEquals(Integer.valueOf(2026), parts.year);
		assertEquals(Integer.valueOf(9), parts.month);
		assertEquals(Integer.valueOf(8), parts.day);
		assertTrue(parts.hasDate());
		assertTrue(parts.matches(2026, 9, 8));
		assertFalse(parts.matches(2026, 9, 9));
	}

	@Test
	void parsePartsClampsAnOutOfRangeDayToTheMonthLength() {
		// September has 30 days; a day of 31 must clamp down rather than overflow into October.
		DateParts parts = DateTimeText.parseParts("2026/09/31 __:__:__");
		assertEquals(Integer.valueOf(30), parts.day);
	}

	@Test
	void parsePartsReturnsAllNullFieldsForBlankOrNullText() {
		assertFalse(DateTimeText.parseParts(DateTimeText.MASK_DEFAULT_VALUE).hasDate());
		assertFalse(DateTimeText.parseParts(null).hasDate());
	}

	@Test
	void parseDateReturnsNullForBlankOrPlaceholderOnlyText() {
		assertNull(DateTimeText.parseDate(null, UTC, 2026, 1, 1, 0, 0, 0, 0));
		assertNull(DateTimeText.parseDate(DateTimeText.MASK_DEFAULT_VALUE, UTC, 2026, 1, 1, 0, 0, 0, 0));
	}

	@Test
	void parseDateFillsInMissingFieldsFromTheDefaults() throws Exception {
		Instant instant = DateTimeText.parseDate("2026/09/__ 10:__:__", UTC, 2000, 1, 15, 0, 30, 45, 0);

		assertEquals("2026-09-15 10:30:45", FORMAT.format(instant));
	}

	@Test
	void parseDateClampsOutOfRangeHourMinuteSecond() throws Exception {
		Instant instant = DateTimeText.parseDate("2026/09/08 99:99:99", UTC, 2026, 1, 1, 0, 0, 0, 0);

		assertEquals("2026-09-08 23:59:59", FORMAT.format(instant));
	}

	@Test
	void numberIgnoresPlaceholderCharactersAndFallsBackOnInvalidInput() {
		String[] values = {"20_6", "__", "abc"};
		assertEquals(206, DateTimeText.number(values, 0, -1)); // the placeholder is stripped, not treated as a digit
		assertEquals(-1, DateTimeText.number(values, 1, -1)); // placeholders only -> empty -> default
		assertEquals(-1, DateTimeText.number(values, 2, -1)); // not a number -> default
		assertEquals(-1, DateTimeText.number(values, 5, -1)); // out of bounds -> default
	}
}
