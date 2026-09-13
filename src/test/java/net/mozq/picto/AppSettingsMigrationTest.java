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
import java.util.Set;

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

		// src.file.name.pattern.syntax must land where file.pattern.regex was defined, not get appended at
		// the end, even though migrating it also renames the key (unlike dest.sub.file.path.pattern and
		// src.include.subfolders, renamed in place).
		assertEquals(
				List.of("src.folder", "src.file.name.pattern.syntax", "dest.sub.file.path.pattern", "src.include.subfolders"),
				List.copyOf(settings.keySet()));
		assertEquals("/photos", settings.getString("src.folder", ""));
		assertEquals("Regex", settings.getString("src.file.name.pattern.syntax", ""));
		assertEquals("${FNumber:0.0}/${WhiteBalance?{0:'Auto',1:'Manual',default:'Others'}}",
				settings.getString("dest.sub.file.path.pattern", ""));
		assertEquals("true", settings.getString("src.include.subfolders", ""));
	}

	@Test
	void migratesParentSubPathVariableNameToSubFolderPath() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"dest.sub.path.pattern=${ParentSubPath}/${BaseName}.${Extension}"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("${SubFolderPath}/${BaseName}.${Extension}",
				settings.getString("dest.sub.file.path.pattern", ""));
	}

	@Test
	void migratesParentSubPathVariableNameInsideMatchExpression() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"dest.sub.path.pattern=${ParentSubPath/:Unsorted/default:Other}/${FileName}"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("${SubFolderPath?{:'Unsorted',default:'Other'}}/${FileName}",
				settings.getString("dest.sub.file.path.pattern", ""));
	}

	@Test
	void migratesPhotoTakenDateVariableNameToTakenDate() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"dest.sub.path.pattern=${ParentSubPath}/${PhotoTakenDate%uuuu/MM}/${FileName}"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("${SubFolderPath}/${TakenDate:uuuu/MM}/${FileName}",
				settings.getString("dest.sub.file.path.pattern", ""));
	}

	@Test
	void migratesFilePatternRegexTrueToTheSyntaxEnumsRegexConstant() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"file.pattern.regex=true"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("Regex", settings.getString("src.file.name.pattern.syntax", ""));
		assertFalse(settings.keySet().contains("file.pattern.regex"));
	}

	@Test
	void migratesFilePatternRegexFalseToTheSyntaxEnumsGlobConstant() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"file.pattern.regex=false"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("Glob", settings.getString("src.file.name.pattern.syntax", ""));
	}

	@Test
	void migratesOperationTypeMoveTrueToTheEnumsMoveConstant() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"ope.type.copy=false",
				"ope.type.move=true",
				"ope.type.overwrite=false"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("Move", settings.getString("operation.type", ""));
		assertFalse(settings.keySet().contains("ope.type.copy"));
		assertFalse(settings.keySet().contains("ope.type.move"));
		assertFalse(settings.keySet().contains("ope.type.overwrite"));
	}

	@Test
	void migratesOperationTypeOverwriteTrueToTheEnumsOverwriteConstant() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"ope.type.copy=false",
				"ope.type.move=false",
				"ope.type.overwrite=true"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("Overwrite", settings.getString("operation.type", ""));
	}

	@Test
	void migratesOperationTypeToCopyWhenNeitherMoveNorOverwriteIsTrue() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"ope.type.copy=true",
				"ope.type.move=false",
				"ope.type.overwrite=false"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals("Copy", settings.getString("operation.type", ""));
	}

	@Test
	void mergesTheThreeOperationTypeKeysIntoOneAtTheFirstOnesPosition() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"src.root.dir=/photos",
				"ope.type.copy=false",
				"ope.type.move=true",
				"ope.type.overwrite=false",
				"contains.subs=true"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals(
				List.of("src.folder", "operation.type", "src.include.subfolders"),
				List.copyOf(settings.keySet()));
	}

	/**
	 * Every legacy key this class knows how to migrate (the whole {@code RENAMED_KEYS} table plus the
	 * three specially-handled ones) must land on a key {@code MainFrame.settingBindings()} actually binds
	 * to a Swing field - otherwise a real 2016/2017 user's setting silently reverts to its default with no
	 * error anywhere. {@code MainFrame.settingBindings()} isn't reachable from here (private, different
	 * package), so this pins the current 32-key set as a regression baseline instead: if a future rename
	 * changes either side (a settingBindings() key, or a RENAMED_KEYS/special-cased target) without
	 * updating the other, this test's expected set stops matching and fails.
	 */
	@Test
	void migratesEveryLegacyKeyToACurrentlyBoundSettingsKey() throws IOException {
		Path legacySettings = tempDir.resolve("settings.properties");
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"src.root.dir=/photos",
				"file.pattern=*.jpg",
				"file.pattern.regex=false",
				"contains.subs=true",
				"contains.hiddens=false",
				"file.size.range.from=100",
				"file.size.range.to=200",
				"file.size.unit=MB",
				"creation.time.range.from=2020-01-01",
				"creation.time.range.to=2020-12-31",
				"modified.time.range.from=2020-01-01",
				"modified.time.range.to=2020-12-31",
				"ope.type.copy=true",
				"ope.type.move=false",
				"ope.type.overwrite=false",
				"dest.root.dir=/backup",
				"dest.sub.path.pattern=${FileName}",
				"existing.file.method=Confirm",
				"check.file.digest=false",
				"change.file.creation.date=false",
				"change.file.modified.date=false",
				"change.file.access.date=false",
				"change.file.exif.date=false",
				"base.date.type=FileModifiedDate",
				"custom.base.date=",
				"date.mod.type=None",
				"date.mod.year=",
				"date.mod.month=",
				"date.mod.day=",
				"date.mod.hour=",
				"date.mod.minute=",
				"date.mod.second=",
				"remove.exif.tags.gps=false",
				"remove.exif.tags.all=false"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals(
				Set.of(
						"src.folder", "src.file.name.pattern", "src.file.name.pattern.syntax",
						"src.include.subfolders", "src.include.hidden.files",
						"src.file.size.from", "src.file.size.to", "src.file.size.unit",
						"src.created.from", "src.created.to", "src.modified.from", "src.modified.to",
						"operation.type",
						"dest.folder", "dest.sub.file.path.pattern", "dest.existing.file.method", "dest.check.file.digest",
						"changes.filedate.creation.date", "changes.filedate.modified.date",
						"changes.filedate.access.date", "changes.filedate.exif.date",
						"changes.filedate.base.date.type", "changes.filedate.custom.base.date",
						"changes.filedate.adjustment.type",
						"changes.filedate.adjustment.years", "changes.filedate.adjustment.months",
						"changes.filedate.adjustment.days", "changes.filedate.adjustment.hours",
						"changes.filedate.adjustment.minutes", "changes.filedate.adjustment.seconds",
						"changes.exif.remove.gps", "changes.exif.remove.all"),
				settings.keySet());
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
