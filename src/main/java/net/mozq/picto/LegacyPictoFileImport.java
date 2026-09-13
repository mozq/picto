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

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import net.mozq.appsettings.AppSettings;

/**
 * Backward-compatibility shim: before the *.picto archive became a ZIP (settings.conf / history.conf /
 * presets/*.conf), exporting settings produced a *.picto file that was just the live settings.properties
 * content saved under that extension - a plain key=value dump using the same legacy key names
 * {@link AppSettingsMigration} already knows how to migrate. Both formats share the *.picto extension, so
 * MainFrame's importer tries this path only when {@link #isZip} says the selected file isn't a ZIP.
 * <p>
 * Self-contained on purpose: once nobody plausibly still has one of these old exports, this class and its
 * one call site in MainFrame can be deleted without touching anything else.
 */
public final class LegacyPictoFileImport {
	private LegacyPictoFileImport() {
	}

	public static boolean isZip(Path path) {
		try (ZipFile ignored = new ZipFile(path.toFile())) {
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Takes the target {@link AppSettings} explicitly (rather than binding to {@link App#settings()}
	 * itself) so this can be exercised in a test against an isolated, temp-file-backed settings instance,
	 * matching how {@link AppSettingsMigration} is tested.
	 */
	public static void importInto(AppSettings settings, Path legacyFile) throws IOException {
		AppSettingsMigration.migrate(settings, legacyFile);
		settings.store();
	}
}
