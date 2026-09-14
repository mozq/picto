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

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

final class DateTimeText {

	static final String MASK_PATTERN = "****/**/** **:**:**";
	static final String MASK_PLACEHOLDER = "_";
	static final char MASK_PLACEHOLDER_CHAR = '_';
	static final String MASK_VALID_CHARS = "0123456789_";
	static final String MASK_DEFAULT_VALUE = "____/__/__ __:__:__";
	static final int DATE_PART_END = 10;
	static final int TIME_PART_START = 11;
	static final int[] DATE_INDEXES = {0, 1, 2, 3, 5, 6, 8, 9};
	static final int[] TIME_INDEXES = {11, 12, 14, 15, 17, 18};

	private DateTimeText() {
	}

	static boolean hasDateTimeText(String text) {
		return text != null && containsDigit(text);
	}

	static String compact(String text) {
		if (!hasDateTimeText(text)) {
			return "";
		}
		String[] dateTimeParts = text.trim().split(" ", -1);
		String date = compactMaskedParts(dateTimeParts.length > 0 ? dateTimeParts[0] : "", "/", "/");
		String time = compactMaskedParts(dateTimeParts.length > 1 ? dateTimeParts[1] : "", ":", ":");
		if (date.isEmpty()) {
			return time;
		}
		if (time.isEmpty()) {
			return date;
		}
		return date + " " + time;
	}

	static String ensureMaskText(String text) {
		if (text == null || text.length() < TIME_INDEXES[TIME_INDEXES.length - 1] + 1) {
			return MASK_DEFAULT_VALUE;
		}
		return text;
	}

	static DateParts parseParts(String text) {
		if (text == null) {
			return new DateParts(null, null, null);
		}
		String[] dateTime = text.split(" ", 2);
		String[] dateParts = dateTime.length > 0 ? dateTime[0].split("/", -1) : new String[0];
		Integer year = number(dateParts, 0);
		Integer month = number(dateParts, 1);
		Integer day = number(dateParts, 2);
		if (month != null) {
			month = Integer.valueOf(Math.max(1, Math.min(12, month.intValue())));
		}
		if (year != null && month != null && day != null) {
			day = Integer.valueOf(Math.max(1, Math.min(YearMonth.of(year.intValue(), month.intValue()).lengthOfMonth(), day.intValue())));
		}
		return new DateParts(year, month, day);
	}

	static Instant parseDate(
			String text,
			TimeZone timeZone,
			int defaultYear,
			int defaultMonth,
			int defaultDay,
			int defaultHour,
			int defaultMin,
			int defaultSec,
			int defaultMsec) {
		if (text == null || MASK_DEFAULT_VALUE.equals(text) || !containsDigit(text)) {
			return null;
		}

		String[] dateTimeParts = text.split(" ", 2);
		String dateText = dateTimeParts.length > 0 ? dateTimeParts[0] : "";
		if (!containsDigit(dateText)) {
			return null;
		}
		String timeText = dateTimeParts.length > 1 ? dateTimeParts[1] : "";
		String[] dateParts = dateText.split("/", -1);
		String[] timeParts = timeText.split(":", -1);

		int year = number(dateParts, 0, defaultYear);
		int month = normalize(number(dateParts, 1, defaultMonth), 1, 12);
		int day = number(dateParts, 2, defaultDay);
		int hour = normalize(number(timeParts, 0, defaultHour), 0, 23);
		int min = normalize(number(timeParts, 1, defaultMin), 0, 59);
		int sec = normalize(number(timeParts, 2, defaultSec), 0, 59);

		// Mirrors Calendar's own lenient day-of-month clamping (e.g. Feb 30 -> Feb 28/29); floored at 1 since
		// ZonedDateTime.of, unlike Calendar, rejects a day below 1 instead of rolling into the previous month.
		int dayOfMonth = Math.max(1, Math.min(day, YearMonth.of(year, month).lengthOfMonth()));
		return ZonedDateTime.of(year, month, dayOfMonth, hour, min, sec, defaultMsec * 1_000_000, timeZone.toZoneId()).toInstant();
	}

	static boolean containsDigit(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (Character.isDigit(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	static int number(String[] values, int index, int defaultValue) {
		if (index >= values.length) {
			return defaultValue;
		}
		String value = values[index].replace(MASK_PLACEHOLDER, "");
		if (value.isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException _) {
			return defaultValue;
		}
	}

	private static int normalize(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static Integer number(String[] values, int index) {
		if (index >= values.length) {
			return null;
		}
		String value = values[index].replace(MASK_PLACEHOLDER, "");
		if (value.isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException _) {
			return null;
		}
	}

	private static String compactMaskedParts(String text, String regexDelimiter, String displayDelimiter) {
		List<String> parts = new ArrayList<>();
		for (String part : text.split(regexDelimiter, -1)) {
			if (containsDigit(part)) {
				parts.add(part);
			}
		}
		return String.join(displayDelimiter, parts);
	}

	static final class DateParts {
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
}
