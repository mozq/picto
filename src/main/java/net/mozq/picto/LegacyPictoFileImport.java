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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.mozq.appsettings.AppSettings;

/**
 * Backward-compatibility shim: before the *.picto archive became a ZIP (settings.conf / history.conf /
 * presets/*.conf), exporting settings produced a *.picto file that was just the live settings.properties
 * content saved under that extension - a plain key=value dump using the same legacy key names
 * {@link AppSettingsMigration} already knows how to migrate. Both formats share the *.picto extension, so
 * MainFrame's importer tries this path only when {@link #isLegacyFile} recognizes the selected file as one
 * of these old exports.
 * <p>
 * Self-contained on purpose: once nobody plausibly still has one of these old exports, this class and its
 * one call site in MainFrame can be deleted without touching anything else.
 */
public final class LegacyPictoFileImport {
	private static final String SRC_ROOT_DIR_LINE_PREFIX = "src.root.dir=";

	private LegacyPictoFileImport() {
	}

	/**
	 * Recognizes the legacy format by content rather than solely by ruling out the current one: every
	 * legacy export is a UTF-8 key=value dump that always defines {@code src.root.dir} (the source folder
	 * was mandatory even in the very first release), so a text file containing that line is confidently a
	 * legacy export, and anything else - a ZIP archive, or any other file the user might pick - confidently
	 * isn't. That also means an unrecognized file falls through to the normal ZIP import path instead of
	 * being blindly parsed as key=value pairs and merged into live settings.
	 * <p>
	 * A real ZIP archive is ruled out explicitly first, as a belt-and-suspenders check ahead of the
	 * content-based one: it means a current-format archive is never misjudged as legacy no matter what its
	 * compressed bytes might happen to decode to as text. Only once that's ruled out does a malformed or
	 * unmappable byte sequence (any other non-text file) get treated as "not this format" rather than an
	 * error - a genuine I/O problem (permission denied, a locked or mid-sync file, ...) is left for the
	 * caller to report as such either way.
	 */
	public static boolean isLegacyFile(Path path) throws IOException {
		if (isZip(path)) {
			return false;
		}
		try {
			for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
				if (line.startsWith(SRC_ROOT_DIR_LINE_PREFIX)) {
					return true;
				}
			}
			return false;
		} catch (CharacterCodingException e) {
			return false;
		}
	}

	private static boolean isZip(Path path) throws IOException {
		try (ZipFile ignored = new ZipFile(path.toFile())) {
			return true;
		} catch (ZipException e) {
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
