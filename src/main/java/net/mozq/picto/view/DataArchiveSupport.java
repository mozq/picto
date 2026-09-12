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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.mozq.appsettings.AppSettings;
import net.mozq.appsettings.AppSettingsDirectory;
import net.mozq.picto.App;

/**
 * Reads and writes the .picto export/import archive: a ZIP mirroring the settings directory's own
 * settings.conf / history.conf / presets/*.conf layout, containing only the categories the user picked.
 * Kept free of Swing so the parsing/merge rules (which categories are present, preset name collisions,
 * unique naming) can be exercised directly in tests instead of only through MainFrame's dialogs.
 */
final class DataArchiveSupport {
	static final String SETTINGS_ENTRY_NAME = App.SETTINGS_FILE_NAME;
	static final String HISTORY_ENTRY_NAME = App.HISTORY_FILE_NAME;
	static final String PRESETS_ENTRY_PREFIX = "presets/";

	private DataArchiveSupport() {
	}

	/**
	 * Which of settings/presets/history apply to an operation - either which ones an archive actually
	 * contains ({@link #readAvailableCategories}), or which ones the user picked in
	 * {@code MainFrame}'s category-selection dialog. Both are the same three yes/no choices, so one type
	 * serves both call sites instead of two structurally identical ones drifting apart.
	 */
	record DataCategories(boolean settings, boolean presets, boolean history) {
		boolean any() {
			return settings || presets || history;
		}
	}

	record ParsedPreset(String name, Map<String, Object> values) {
	}

	static DataCategories readAvailableCategories(Path zipPath) throws IOException {
		boolean settings = false;
		boolean presets = false;
		boolean history = false;
		try (ZipFile zip = new ZipFile(zipPath.toFile())) {
			for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
				String name = entries.nextElement().getName();
				if (name.equals(SETTINGS_ENTRY_NAME)) {
					settings = true;
				} else if (isPresetEntry(name)) {
					presets = true;
				} else if (name.equals(HISTORY_ENTRY_NAME)) {
					history = true;
				}
			}
		}
		return new DataCategories(settings, presets, history);
	}

	/**
	 * Stores {@code settings}' current in-memory content (not necessarily what's on disk yet) into the
	 * archive as {@code entryName}, mirroring how a single-file settings export always wrote the live
	 * {@code AppSettings} object rather than copying its backing file.
	 */
	static void writeSettingsEntry(ZipOutputStream zos, String entryName, AppSettings settings) throws IOException {
		Path temp = Files.createTempFile("picto-export-", "." + MainFrame.PRESET_FILE_NAME_EXT);
		try {
			settings.storeTo(temp);
			writeFileEntry(zos, entryName, temp);
		} finally {
			Files.deleteIfExists(temp);
		}
	}

	static void writeFileEntry(ZipOutputStream zos, String entryName, Path sourceFile) throws IOException {
		zos.putNextEntry(new ZipEntry(entryName));
		Files.copy(sourceFile, zos);
		zos.closeEntry();
	}

	static String presetEntryName(int index) {
		return PRESETS_ENTRY_PREFIX + "preset-" + index + "." + MainFrame.PRESET_FILE_NAME_EXT;
	}

	/**
	 * Reads every key in the given entry into a plain map, without touching any live {@link AppSettings}
	 * instance, so the caller can validate every selected category up front and only apply/persist any of
	 * them once all of them are known to parse successfully.
	 */
	static Map<String, Object> readEntryValues(ZipFile zip, String entryName) throws IOException {
		ZipEntry entry = zip.getEntry(entryName);
		if (entry == null) {
			throw new IOException("Missing entry in archive: " + entryName);
		}
		return readValues(loadFromEntry(zip, entry));
	}

	/**
	 * Reads every {@code presets/preset-<n>.conf} entry, in ascending {@code <n>} order, into its display
	 * name (the preset's first stored comment, matching {@link MainFrame#listPresets()}) and key/value map.
	 */
	static List<ParsedPreset> readPresetEntries(ZipFile zip) throws IOException {
		List<String> entryNames = new ArrayList<>();
		for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
			String name = entries.nextElement().getName();
			if (isPresetEntry(name)) {
				entryNames.add(name);
			}
		}
		entryNames.sort(Comparator.comparingInt(DataArchiveSupport::presetEntryIndex));

		List<ParsedPreset> result = new ArrayList<>();
		for (String entryName : entryNames) {
			AppSettings imported = loadFromEntry(zip, zip.getEntry(entryName));
			List<String> comments = imported.comments();
			String name = comments.isEmpty() ? entryName : comments.get(0);
			result.add(new ParsedPreset(name, readValues(imported)));
		}
		return result;
	}

	/**
	 * Extracts {@code entry} to a temp file just long enough to {@link AppSettings#load()} it - once loaded,
	 * an {@code AppSettings} holds its values/comments in memory, so the returned instance stays valid after
	 * the temp file is removed here.
	 */
	private static AppSettings loadFromEntry(ZipFile zip, ZipEntry entry) throws IOException {
		Path temp = extractToTempFile(zip, entry);
		try {
			AppSettings settings = AppSettings.of(temp);
			settings.load();
			return settings;
		} finally {
			Files.deleteIfExists(temp);
		}
	}

	private static Map<String, Object> readValues(AppSettings settings) {
		Map<String, Object> values = new LinkedHashMap<>();
		for (String key : settings.keySet()) {
			values.put(key, settings.get(key));
		}
		return values;
	}

	/**
	 * Picks {@code baseName} if it's not already in {@code namesInUse}, otherwise the first
	 * {@code "<baseName> (2)"}, {@code "(3)"}, ... that isn't. Either way the returned name is added to
	 * {@code namesInUse} before returning, so a caller resolving several imported presets in one pass can
	 * share one set and never hand out the same name twice.
	 */
	static String uniqueDisplayName(String baseName, Set<String> namesInUse) {
		if (namesInUse.add(baseName)) {
			return baseName;
		}
		for (int suffix = 2;; suffix++) {
			String candidate = baseName + " (" + suffix + ")";
			if (namesInUse.add(candidate)) {
				return candidate;
			}
		}
	}

	/**
	 * Allocates a preset file name not already in {@code fileNamesInUse}, adding it before returning. Unlike
	 * {@link AppSettingsDirectory#uniqueFileName}, this only checks the given set rather than the
	 * filesystem, since a whole batch of imported presets is prepared here before any of them is written to
	 * disk.
	 */
	static String allocatePresetFileName(Set<String> fileNamesInUse) {
		for (int attempt = 1;; attempt++) {
			String candidate = "preset-" + System.nanoTime() + "-" + attempt + "." + MainFrame.PRESET_FILE_NAME_EXT;
			if (fileNamesInUse.add(candidate)) {
				return candidate;
			}
		}
	}

	/**
	 * Builds the {@link AppSettings} to store for one imported preset: the app's schema defaults
	 * ({@code applyDefaults}), with the imported keys overlaid on top, so a key the import doesn't mention
	 * still ends up with the same value a never-customized preset would have instead of being left
	 * undefined. Every imported preset is built this way, whether it lands in a new file or replaces an
	 * existing same-named one - a name collision only changes which {@code fileName}/{@code comment} it's
	 * written under, never what the content is built from.
	 */
	static AppSettings buildPresetSettings(
			AppSettingsDirectory dir,
			String fileName,
			Consumer<AppSettings> applyDefaults,
			Map<String, Object> overrides,
			String comment) {
		AppSettings settings = AppSettings.of(dir, fileName);
		applyDefaults.accept(settings);
		for (Map.Entry<String, Object> entry : overrides.entrySet()) {
			settings.set(entry.getKey(), entry.getValue());
		}
		settings.clearComments();
		settings.addComment(comment);
		return settings;
	}

	private static boolean isPresetEntry(String entryName) {
		return entryName.startsWith(PRESETS_ENTRY_PREFIX) && entryName.endsWith("." + MainFrame.PRESET_FILE_NAME_EXT);
	}

	private static int presetEntryIndex(String entryName) {
		try {
			String base = entryName.substring(
					PRESETS_ENTRY_PREFIX.length(),
					entryName.length() - (1 + MainFrame.PRESET_FILE_NAME_EXT.length()));
			return Integer.parseInt(base.substring("preset-".length()));
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			return Integer.MAX_VALUE;
		}
	}

	private static Path extractToTempFile(ZipFile zip, ZipEntry entry) throws IOException {
		Path temp = Files.createTempFile("picto-import-", "." + MainFrame.PRESET_FILE_NAME_EXT);
		try (InputStream in = zip.getInputStream(entry)) {
			Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
		}
		return temp;
	}
}
