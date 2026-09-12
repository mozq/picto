/*!
 * Picto
 * Copyright 2016 Mozq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.appsettings.AppSettings;

class AppSettingsMigrationTest {
	@TempDir
	Path tempDir;

	@Test
	void migratesLegacyPropertiesInDefinitionOrder() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"# Legacy settings",
				"src.root.dir=/photos",
				"file.pattern.regex=true",
				"dest.sub.path.pattern=${FNumber%0.0}/${WhiteBalance/0:Auto/1:Manual/default:Others}",
				"contains.subs=true"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		// file.pattern.syntax must land where file.pattern.regex was defined, not get appended at the end,
		// even though migrating it also renames the key (unlike dest.sub.path.pattern, migrated in place).
		assertEquals(
				List.of("src.root.dir", "file.pattern.syntax", "dest.sub.path.pattern", "contains.subs"),
				List.copyOf(settings.keySet()));
		assertEquals("/photos", settings.getString("src.root.dir", ""));
		assertEquals("REGEX", settings.getString("file.pattern.syntax", ""));
		assertEquals("${FNumber:0.0}/${WhiteBalance?{0:'Auto',1:'Manual',default:'Others'}}",
				settings.getString("dest.sub.path.pattern", ""));
		assertEquals("true", settings.getString("contains.subs", ""));
	}

	@Test
	void migratesParentSubPathVariableNameToSubFolderPath() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"dest.sub.path.pattern=${ParentSubPath}/${BaseName}.${Extension}"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("${SubFolderPath}/${BaseName}.${Extension}",
				settings.getString("dest.sub.path.pattern", ""));
	}

	@Test
	void migratesParentSubPathVariableNameInsideMatchExpression() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"dest.sub.path.pattern=${ParentSubPath/:Unsorted/default:Other}/${FileName}"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("${SubFolderPath?{:'Unsorted',default:'Other'}}/${FileName}",
				settings.getString("dest.sub.path.pattern", ""));
	}

	@Test
	void migratesPhotoTakenDateVariableNameToTakenDate() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"dest.sub.path.pattern=${ParentSubPath}/${PhotoTakenDate%uuuu/MM}/${FileName}"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("${SubFolderPath}/${TakenDate:uuuu/MM}/${FileName}",
				settings.getString("dest.sub.path.pattern", ""));
	}

	@Test
	void migratesFilePatternRegexTrueToTheSyntaxEnumsRegexConstant() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"file.pattern.regex=true"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("REGEX", settings.getString("file.pattern.syntax", ""));
		assertFalse(settings.keySet().contains("file.pattern.regex"));
	}

	@Test
	void migratesFilePatternRegexFalseToTheSyntaxEnumsGlobConstant() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"file.pattern.regex=false"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("GLOB", settings.getString("file.pattern.syntax", ""));
	}

	@Test
	void deletesLegacySettingsLogsAndEmptyDirectories() throws IOException {
		Path legacyDirectory = tempDir.resolve("Mozq").resolve("Picto");
		Files.createDirectories(legacyDirectory);
		Path legacySettings = Files.writeString(legacyDirectory.resolve("settings.properties"), "theme=dark");
		Path warnsLog = Files.writeString(legacyDirectory.resolve(App.WARNS_FILE_NAME), "warn");
		Path errorsLog = Files.writeString(legacyDirectory.resolve(App.ERRORS_FILE_NAME), "error");

		AppSettingsMigration.deleteLegacyFiles(new AppSettingsMigration.Result(true, legacySettings));

		assertFalse(Files.exists(legacySettings));
		assertFalse(Files.exists(warnsLog));
		assertFalse(Files.exists(errorsLog));
		assertFalse(Files.exists(legacyDirectory));
		assertFalse(Files.exists(legacyDirectory.getParent()));
	}
}
