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
				"dest.sub.path.pattern=${FNumber%0.0}/${WhiteBalance/0:Auto/1:Manual/default:Others}",
				"contains.subs=true"));
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf");

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals(List.of("locale", "appearance", "src.root.dir", "dest.sub.path.pattern", "contains.subs"),
				List.copyOf(settings.keySet()));
		assertEquals("system", settings.getString("locale", ""));
		assertEquals("system", settings.getString("appearance", ""));
		assertEquals("/photos", settings.getString("src.root.dir", ""));
		assertEquals("${FNumber:0.0}/${WhiteBalance?{0:'Auto',1:'Manual',default:'Others'}}",
				settings.getString("dest.sub.path.pattern", ""));
		assertEquals("true", settings.getString("contains.subs", ""));
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
