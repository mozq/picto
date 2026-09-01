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
		Path legacySettings = tempDir.resolve("settings.properties"); //$NON-NLS-1$
		Files.writeString(legacySettings, String.join(System.lineSeparator(),
				"# Legacy settings", //$NON-NLS-1$
				"src.root.dir=/photos", //$NON-NLS-1$
				"dest.sub.path.pattern=${FNumber%0.0}/${WhiteBalance/0:Auto/1:Manual/default:Others}", //$NON-NLS-1$
				"contains.subs=true")); //$NON-NLS-1$
		AppSettings settings = AppSettings.of(null, "picto-test", "settings.conf"); //$NON-NLS-1$ //$NON-NLS-2$

		AppSettingsMigration.migrate(settings, legacySettings);

		assertEquals(List.of("locale", "appearance", "src.root.dir", "dest.sub.path.pattern", "contains.subs"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				List.copyOf(settings.asStringMap().keySet()));
		assertEquals("system", settings.getString("locale", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("system", settings.getString("appearance", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("/photos", settings.getString("src.root.dir", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("${FNumber:0.0}/${WhiteBalance?{0:'Auto',1:'Manual',default:'Others'}}", //$NON-NLS-1$
				settings.getString("dest.sub.path.pattern", "")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("true", settings.getString("contains.subs", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	void deletesLegacySettingsLogsAndEmptyDirectories() throws IOException {
		Path legacyDirectory = tempDir.resolve("Mozq").resolve("Picto"); //$NON-NLS-1$ //$NON-NLS-2$
		Files.createDirectories(legacyDirectory);
		Path legacySettings = Files.writeString(legacyDirectory.resolve("settings.properties"), "theme=dark"); //$NON-NLS-1$ //$NON-NLS-2$
		Path warnsLog = Files.writeString(legacyDirectory.resolve(App.WARNS_FILE_NAME), "warn"); //$NON-NLS-1$
		Path errorsLog = Files.writeString(legacyDirectory.resolve(App.ERRORS_FILE_NAME), "error"); //$NON-NLS-1$

		AppSettingsMigration.deleteLegacyFiles(new AppSettingsMigration.Result(true, legacySettings));

		assertFalse(Files.exists(legacySettings));
		assertFalse(Files.exists(warnsLog));
		assertFalse(Files.exists(errorsLog));
		assertFalse(Files.exists(legacyDirectory));
		assertFalse(Files.exists(legacyDirectory.getParent()));
	}
}
