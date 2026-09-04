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
package net.mozq.picto.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExifMetadataSupportTest {

	@Test
	void testParseSubSecToMillis() {
		// Null and empty
		assertEquals(0, ExifMetadataSupport.parseSubSecToMillis(null));
		assertEquals(0, ExifMetadataSupport.parseSubSecToMillis(""));
		assertEquals(0, ExifMetadataSupport.parseSubSecToMillis("   "));

		// 1 digit: fractional tenths -> 100ms multiplier
		assertEquals(500, ExifMetadataSupport.parseSubSecToMillis("5"));
		assertEquals(100, ExifMetadataSupport.parseSubSecToMillis("1"));
		assertEquals(0, ExifMetadataSupport.parseSubSecToMillis("0"));

		// 2 digits: fractional hundredths -> 10ms multiplier
		assertEquals(500, ExifMetadataSupport.parseSubSecToMillis("50"));
		assertEquals(50, ExifMetadataSupport.parseSubSecToMillis("05"));
		assertEquals(990, ExifMetadataSupport.parseSubSecToMillis("99"));

		// 3 digits: fractional thousandths -> 1ms multiplier
		assertEquals(123, ExifMetadataSupport.parseSubSecToMillis("123"));
		assertEquals(500, ExifMetadataSupport.parseSubSecToMillis("500"));
		assertEquals(5, ExifMetadataSupport.parseSubSecToMillis("005"));

		// 4 or more digits: truncate to 3 digits (milliseconds)
		assertEquals(123, ExifMetadataSupport.parseSubSecToMillis("1234"));
		assertEquals(987, ExifMetadataSupport.parseSubSecToMillis("987654"));

		// Trimming whitespace
		assertEquals(420, ExifMetadataSupport.parseSubSecToMillis("  42  "));

		// Invalid non-digit characters
		assertEquals(0, ExifMetadataSupport.parseSubSecToMillis("abc"));
		assertEquals(0, ExifMetadataSupport.parseSubSecToMillis("12a"));
		assertEquals(0, ExifMetadataSupport.parseSubSecToMillis("-5"));
	}
}
