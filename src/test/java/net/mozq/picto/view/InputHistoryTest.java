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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.appsettings.AppSettings;

/**
 * Exercises the most-recently-used cap/dedupe/persist logic against an isolated, temp-file-backed
 * {@link AppSettings} instance (via {@code AppSettings.of(Path)}) rather than the user's real history file,
 * which is what {@link InputHistory} previously read internally before being refactored to take the
 * settings instance as a parameter.
 */
class InputHistoryTest {
	@TempDir
	Path tempDir;

	private AppSettings settings() {
		return AppSettings.of(tempDir.resolve("history-test.conf"));
	}

	@Test
	void recordsMostRecentFirst() {
		AppSettings settings = settings();
		InputHistory.record(settings, "key", "a");
		InputHistory.record(settings, "key", "b");

		assertEquals(List.of("b", "a"), InputHistory.load(settings, "key"));
	}

	@Test
	void capsAtFiveEntriesDroppingTheOldest() {
		AppSettings settings = settings();
		for (int i = 1; i <= 6; i++) {
			InputHistory.record(settings, "key", "value" + i);
		}

		List<String> entries = InputHistory.load(settings, "key");
		assertEquals(List.of("value6", "value5", "value4", "value3", "value2"), entries);
	}

	@Test
	void reRecordingAnExistingEntryMovesItToFrontInsteadOfDuplicating() {
		AppSettings settings = settings();
		InputHistory.record(settings, "key", "a");
		InputHistory.record(settings, "key", "b");
		InputHistory.record(settings, "key", "a");

		assertEquals(List.of("a", "b"), InputHistory.load(settings, "key"));
	}

	@Test
	void ignoresBlankAndNullValues() {
		AppSettings settings = settings();
		InputHistory.record(settings, "key", "");
		InputHistory.record(settings, "key", "   ");
		InputHistory.record(settings, "key", (String)null);

		assertTrue(InputHistory.load(settings, "key").isEmpty());
	}

	@Test
	void removeDeletesOnlyTheGivenEntry() {
		AppSettings settings = settings();
		InputHistory.record(settings, "key", "a");
		InputHistory.record(settings, "key", "b");

		InputHistory.remove(settings, "key", "a");

		assertEquals(List.of("b"), InputHistory.load(settings, "key"));
	}

	@Test
	void keysAreIndependentOfEachOther() {
		AppSettings settings = settings();
		InputHistory.record(settings, InputHistory.SRC_FOLDER_KEY, "/src");
		InputHistory.record(settings, InputHistory.DEST_FOLDER_KEY, "/dest");

		assertEquals(List.of("/src"), InputHistory.load(settings, InputHistory.SRC_FOLDER_KEY));
		assertEquals(List.of("/dest"), InputHistory.load(settings, InputHistory.DEST_FOLDER_KEY));
	}

	@Test
	void persistsToDiskAndReloadsFromAFreshInstance() throws Exception {
		AppSettings settings = settings();
		InputHistory.record(settings, "key", "a");

		AppSettings reloaded = AppSettings.of(settings.path()).load();

		assertEquals(List.of("a"), InputHistory.load(reloaded, "key"));
	}
}
