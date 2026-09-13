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
package net.mozq.picto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.appsettings.AppSettings;

class LegacyPictoFileImportTest {
	@TempDir
	Path tempDir;

	@Test
	void isLegacyFileIsTrueForAPlainKeyValueFileWithSrcRootDir() throws IOException {
		Path legacyFile = tempDir.resolve("settings.picto");
		Files.writeString(legacyFile, "src.root.dir=/photos");

		assertTrue(LegacyPictoFileImport.isLegacyFile(legacyFile));
	}

	@Test
	void isLegacyFileIsFalseForATextFileWithoutSrcRootDir() throws IOException {
		Path notLegacyFile = tempDir.resolve("settings.picto");
		Files.writeString(notLegacyFile, "some.other.key=value");

		assertFalse(LegacyPictoFileImport.isLegacyFile(notLegacyFile));
	}

	@Test
	void isLegacyFileIsFalseForAZipFile() throws IOException {
		Path archive = tempDir.resolve("settings.picto");
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(archive))) {
			zos.putNextEntry(new ZipEntry("settings.conf"));
			zos.write("src.folder=/photos".getBytes());
			zos.closeEntry();
		}

		assertFalse(LegacyPictoFileImport.isLegacyFile(archive));
	}

	@Test
	void isLegacyFilePropagatesAGenuineIoError() {
		// A missing file fails for a reason unrelated to format detection (NoSuchFileException) -
		// isLegacyFile must not swallow that as "not legacy" the way it does an undecodable byte sequence.
		Path missingFile = tempDir.resolve("does-not-exist.picto");

		assertThrows(IOException.class, () -> LegacyPictoFileImport.isLegacyFile(missingFile));
	}

	@Test
	void importIntoMigratesLegacyKeysIntoTheGivenSettings() throws IOException {
		Path legacyFile = tempDir.resolve("settings.picto");
		Files.writeString(legacyFile, String.join(System.lineSeparator(),
				"src.root.dir=/photos",
				"contains.subs=true"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		LegacyPictoFileImport.importInto(settings, legacyFile);

		assertEquals("/photos", settings.getString("src.folder", ""));
		assertEquals("true", settings.getString("src.include.subfolders", ""));
	}
}
