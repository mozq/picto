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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import net.mozq.appsettings.AppSettings;

final class AppSettingsMigration {
	private static final String LEGACY_GROUP_NAME = "Mozq";
	private static final String LEGACY_APP_NAME = "Picto";
	private static final String LEGACY_CONFIG_FILE_NAME = "settings.properties";
	private static final String SUBFOLDER_PATTERN_KEY = "dest.sub.path.pattern";
	private static final Map<String, String> RENAMED_VAR_NAMES = Map.of(
			"ParentSubPath", "SubFolderPath",
			"PhotoTakenDate", "TakenDate"
			);

	private AppSettingsMigration() {
		// NOP
	}

	static Result migrateIfNeeded(AppSettings settings) throws IOException {
		if (Files.exists(settings.path())) {
			return Result.none();
		}

		AppSettings legacySettings = AppSettings.of(LEGACY_GROUP_NAME, LEGACY_APP_NAME, LEGACY_CONFIG_FILE_NAME);
		Path legacySettingsFile = legacySettings.path();
		if (!Files.exists(legacySettingsFile)) {
			return Result.none();
		}

		return migrate(settings, legacySettingsFile);
	}

	static Result migrate(AppSettings settings, Path legacySettingsFile) throws IOException {
		settings.set(AppMain.PREF_LOCALE_KEY, AppMain.PREF_SYSTEM);
		settings.set(AppMain.PREF_APPEARANCE_KEY, AppMain.PREF_SYSTEM);

		for (Map.Entry<String, String> entry : readLegacyProperties(legacySettingsFile).entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (SUBFOLDER_PATTERN_KEY.equals(key)) {
				value = migrateTemplate(value);
			}
			settings.set(key, value);
		}

		return new Result(true, legacySettingsFile);
	}

	static void deleteLegacyFiles(Result result) {
		if (!result.migrated()) {
			return;
		}

		Path legacyDirectory = result.legacySettingsFile().getParent();
		deleteIfExists(result.legacySettingsFile());
		deleteIfExists(legacyDirectory.resolve(App.WARNS_FILE_NAME));
		deleteIfExists(legacyDirectory.resolve(App.ERRORS_FILE_NAME));
		deleteDirectoryIfEmpty(legacyDirectory);
		deleteDirectoryIfEmpty(legacyDirectory.getParent());
	}

	private static LinkedHashMap<String, String> readLegacyProperties(Path file) throws IOException {
		LinkedHashMap<String, String> values = new LinkedHashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			String logicalLine;
			while ((logicalLine = readLogicalLine(reader)) != null) {
				String trimmed = logicalLine.stripLeading();
				if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) {
					continue;
				}
				int separator = findSeparator(logicalLine);
				if (separator < 0) {
					values.put(unescape(logicalLine.strip()), "");
				} else {
					String key = logicalLine.substring(0, separator).strip();
					String value = logicalLine.substring(separator + 1);
					values.put(unescape(key), unescape(value.stripLeading()));
				}
			}
		}
		return values;
	}

	private static String readLogicalLine(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		if (line == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder(line);
		while (endsWithContinuation(builder)) {
			builder.setLength(builder.length() - 1);
			String nextLine = reader.readLine();
			if (nextLine == null) {
				break;
			}
			builder.append(nextLine.stripLeading());
		}
		return builder.toString();
	}

	private static boolean endsWithContinuation(CharSequence line) {
		int backslashes = 0;
		for (int i = line.length() - 1; i >= 0 && line.charAt(i) == '\\'; i--) {
			backslashes++;
		}
		return backslashes % 2 == 1;
	}

	private static int findSeparator(String line) {
		boolean escaped = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (escaped) {
				escaped = false;
			} else if (c == '\\') {
				escaped = true;
			} else if (c == '=' || c == ':') {
				return i;
			}
		}
		return -1;
	}

	private static String unescape(String value) {
		StringBuilder result = new StringBuilder();
		boolean escaped = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (!escaped) {
				if (c == '\\') {
					escaped = true;
				} else {
					result.append(c);
				}
				continue;
			}

			switch (c) {
			case 't': result.append('\t'); break;
			case 'r': result.append('\r'); break;
			case 'n': result.append('\n'); break;
			case 'f': result.append('\f'); break;
			case 'u':
				if (i + 4 < value.length()) {
					result.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16));
					i += 4;
				} else {
					result.append('u');
				}
				break;
			default:
				result.append(c);
			}
			escaped = false;
		}
		if (escaped) {
			result.append('\\');
		}
		return result.toString();
	}

	static String migrateTemplate(String template) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < template.length(); i++) {
			if (template.charAt(i) == '$' && i + 1 < template.length() && template.charAt(i + 1) == '{') {
				int end = template.indexOf('}', i + 2);
				if (end > i) {
					result.append(migratePlaceholder(template.substring(i + 2, end)));
					i = end;
					continue;
				}
			}
			result.append(template.charAt(i));
		}
		return result.toString();
	}

	private static String migratePlaceholder(String expression) {
		int formatIndex = expression.indexOf('%');
		int matchIndex = expression.indexOf('/');
		if (formatIndex >= 0 && (matchIndex < 0 || formatIndex < matchIndex)) {
			return "${" + migrateVarName(expression.substring(0, formatIndex)) + ':' + expression.substring(formatIndex + 1) + '}';
		}
		if (matchIndex >= 0) {
			return "${" + migrateVarName(expression.substring(0, matchIndex)) + "?{" + migrateMatchCases(expression.substring(matchIndex + 1)) + "}}";
		}
		return "${" + migrateVarName(expression) + '}';
	}

	private static String migrateVarName(String varName) {
		return RENAMED_VAR_NAMES.getOrDefault(varName, varName);
	}

	private static String migrateMatchCases(String cases) {
		StringBuilder result = new StringBuilder();
		String[] entries = cases.split("/");
		for (String entry : entries) {
			if (entry.isEmpty()) {
				continue;
			}
			int separator = entry.indexOf(':');
			if (separator < 0) {
				return cases;
			}
			if (result.length() > 0) {
				result.append(',');
			}
			result.append(entry, 0, separator)
					.append(':')
					.append('\'')
					.append(entry.substring(separator + 1).replace("'", "''"))
					.append('\'');
		}
		return result.toString();
	}

	private static void deleteIfExists(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			// Best effort cleanup only.
		}
	}

	private static void deleteDirectoryIfEmpty(Path directory) {
		if (directory == null) {
			return;
		}
		try {
			Files.delete(directory);
		} catch (DirectoryNotEmptyException e) {
			// Keep directories that still contain user files.
		} catch (IOException e) {
			// Best effort cleanup only.
		}
	}

	record Result(boolean migrated, Path legacySettingsFile) {
		static Result none() {
			return new Result(false, null);
		}
	}
}
