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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.appsettings.AppSettings;
import net.mozq.appsettings.AppSettingsDirectory;

class DataArchiveSupportTest {
	@TempDir
	Path tempDir;

	@Test
	void readsWhichCategoriesAreInTheArchive() throws IOException {
		Path zipPath = tempDir.resolve("archive.picto");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
			zos.putNextEntry(new ZipEntry(DataArchiveSupport.SETTINGS_ENTRY_NAME));
			zos.closeEntry();
			zos.putNextEntry(new ZipEntry(DataArchiveSupport.presetEntryName(1)));
			zos.closeEntry();
		}

		DataArchiveSupport.DataCategories available = DataArchiveSupport.readAvailableCategories(zipPath);

		assertTrue(available.settings());
		assertTrue(available.presets());
		assertFalse(available.history());
		assertTrue(available.any());
	}

	@Test
	void reportsNoCategoriesForAnArchiveWithNoneOfTheRecognizedEntries() throws IOException {
		Path zipPath = tempDir.resolve("empty.picto");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
			zos.putNextEntry(new ZipEntry("unrelated.txt"));
			zos.closeEntry();
		}

		DataArchiveSupport.DataCategories available = DataArchiveSupport.readAvailableCategories(zipPath);

		assertFalse(available.any());
	}

	@Test
	void roundTripsASettingsEntryThroughWriteAndRead() throws IOException {
		AppSettings settings = AppSettings.of(tempDir.resolve("settings.conf"));
		settings.set("foo", "bar");

		Path zipPath = tempDir.resolve("archive.picto");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
			DataArchiveSupport.writeSettingsEntry(zos, DataArchiveSupport.SETTINGS_ENTRY_NAME, settings);
		}

		try (ZipFile zip = new ZipFile(zipPath.toFile())) {
			Map<String, Object> values = DataArchiveSupport.readEntryValues(zip, DataArchiveSupport.SETTINGS_ENTRY_NAME);
			assertEquals("bar", String.valueOf(values.get("foo")));
		}
	}

	@Test
	void readingAMissingEntryThrows() throws IOException {
		Path zipPath = tempDir.resolve("archive.picto");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
			zos.putNextEntry(new ZipEntry("unrelated.txt"));
			zos.closeEntry();
		}

		try (ZipFile zip = new ZipFile(zipPath.toFile())) {
			assertThrows(IOException.class, () -> DataArchiveSupport.readEntryValues(zip, DataArchiveSupport.SETTINGS_ENTRY_NAME));
		}
	}

	@Test
	void readsPresetEntriesInAscendingNumericOrderRegardlessOfZipEntryOrder() throws IOException {
		Path zipPath = tempDir.resolve("archive.picto");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
			writePresetEntry(zos, 10, "Tenth");
			writePresetEntry(zos, 2, "Second");
		}

		try (ZipFile zip = new ZipFile(zipPath.toFile())) {
			List<DataArchiveSupport.ParsedPreset> parsed = DataArchiveSupport.readPresetEntries(zip);
			assertEquals(List.of("Second", "Tenth"), parsed.stream().map(DataArchiveSupport.ParsedPreset::name).toList());
		}
	}

	private void writePresetEntry(ZipOutputStream zos, int index, String name) throws IOException {
		Path temp = tempDir.resolve("preset-src-" + index + ".conf");
		AppSettings settings = AppSettings.of(temp);
		settings.addComment(name);
		settings.set("key", "value-" + index);
		settings.store();
		DataArchiveSupport.writeFileEntry(zos, DataArchiveSupport.presetEntryName(index), temp);
	}

	@Test
	void keepsAnAlreadyUniqueNameUnchanged() {
		Set<String> namesInUse = new HashSet<>();

		assertEquals("Foo", DataArchiveSupport.uniqueDisplayName("Foo", namesInUse));
		assertTrue(namesInUse.contains("Foo"));
	}

	@Test
	void appendsAnIncrementingSuffixOnCollisionAndReservesIt() {
		Set<String> namesInUse = new HashSet<>(Set.of("Foo", "Foo (2)"));

		String resolved = DataArchiveSupport.uniqueDisplayName("Foo", namesInUse);

		assertEquals("Foo (3)", resolved);
		assertTrue(namesInUse.contains("Foo (3)"));
	}

	@Test
	void allocatesDistinctFileNamesWithoutTouchingTheDisk() {
		Set<String> fileNamesInUse = new HashSet<>();

		String first = DataArchiveSupport.allocatePresetFileName(fileNamesInUse);
		String second = DataArchiveSupport.allocatePresetFileName(fileNamesInUse);

		assertNotEquals(first, second);
		assertTrue(fileNamesInUse.containsAll(List.of(first, second)));
	}

	@Test
	void buildsANewPresetOnTopOfTheGivenBaseValues() throws IOException {
		AppSettingsDirectory dir = new AppSettingsDirectory(tempDir, "mozq-test", "picto-test");

		AppSettings settings = DataArchiveSupport.buildPresetSettings(
				dir,
				"preset-1.conf",
				conf -> conf.set("base.key", "default"),
				Map.of("override.key", "imported"),
				"My Preset");

		assertEquals("default", settings.getString("base.key", null));
		assertEquals("imported", settings.getString("override.key", null));
		assertEquals(List.of("My Preset"), settings.comments());
	}

	@Test
	void replacingAnExistingFileDiscardsItsOwnContentInsteadOfMergingOntoIt() throws IOException {
		AppSettings existing = AppSettings.of(tempDir.resolve("existing.conf"));
		existing.set("base.key", "original");
		existing.set("stale.key", "should-not-survive");
		existing.addComment("Original Name");
		existing.store();

		AppSettingsDirectory dir = new AppSettingsDirectory(tempDir, "mozq-test", "picto-test");
		AppSettings replaced = DataArchiveSupport.buildPresetSettings(
				dir,
				"existing.conf",
				conf -> conf.set("base.key", "default"),
				Map.of("base.key", "imported"),
				"Original Name");

		assertEquals("imported", replaced.getString("base.key", null));
		assertEquals(0, replaced.keySet().stream().filter("stale.key"::equals).count());
	}
}
