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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RangeTest {

	@Test
	void testFactoryMethod() {
		assertNull(Range.of(null, null));

		Range<Integer> lowerOnly = Range.of(10, null);
		assertEquals(10, lowerOnly.from());
		assertNull(lowerOnly.to());

		Range<Integer> upperOnly = Range.of(null, 20);
		assertNull(upperOnly.from());
		assertEquals(20, upperOnly.to());

		Range<Integer> bounded = Range.of(10, 20);
		assertEquals(10, bounded.from());
		assertEquals(20, bounded.to());
	}

	@Test
	void testContainsWithBothBounds() {
		Range<Integer> range = Range.of(10, 20);

		assertFalse(range.contains(null));
		assertFalse(range.contains(9));
		assertTrue(range.contains(10));
		assertTrue(range.contains(15));
		assertTrue(range.contains(20));
		assertFalse(range.contains(21));
	}

	@Test
	void testContainsWithLowerBoundOnly() {
		Range<Integer> range = Range.of(10, null);

		assertFalse(range.contains(null));
		assertFalse(range.contains(9));
		assertTrue(range.contains(10));
		assertTrue(range.contains(100));
	}

	@Test
	void testContainsWithUpperBoundOnly() {
		Range<Integer> range = Range.of(null, 20);

		assertFalse(range.contains(null));
		assertTrue(range.contains(-100));
		assertTrue(range.contains(20));
		assertFalse(range.contains(21));
	}

	@Test
	void testRecordEquality() {
		Range<String> r1 = Range.of("a", "z");
		Range<String> r2 = Range.of("a", "z");
		assertEquals(r1, r2);
		assertEquals(r1.hashCode(), r2.hashCode());
	}
}
