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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.mozq.appsettings.AppSettings;
import net.mozq.picto.App;

/**
 * Most-recently-used text input history (recent folders, file name patterns, Subfolder templates, ...),
 * persisted in {@value net.mozq.picto.App#HISTORY_FILE_NAME}. The core cap/dedupe/persist logic takes the
 * backing {@link AppSettings} explicitly, so it can be exercised in a test against an isolated,
 * temp-file-backed settings instance instead of the user's real history file; every production call site
 * instead uses the zero-arg-settings overloads below, which bind to {@link App#history()} once here instead
 * of each caller repeating that binding.
 */
final class InputHistory {
	static final String SRC_FOLDER_KEY = "src.folder";
	static final String SRC_FILE_NAME_PATTERN_KEY = "src.file.name.pattern";
	static final String DEST_FOLDER_KEY = "dest.folder";
	static final String DEST_SUB_FILE_PATH_PATTERN_KEY = "dest.sub.file.path.pattern";

	private static final int MAX_ENTRIES = 5;

	private InputHistory() {
	}

	static List<String> load(String key) {
		return load(App.history(), key);
	}

	static List<String> load(AppSettings settings, String key) {
		return settings.getList(key, String.class, List.of());
	}

	static void record(String key, Path path) {
		record(App.history(), key, path);
	}

	static void record(AppSettings settings, String key, Path path) {
		record(settings, key, path.toString());
	}

	static void record(String key, String value) {
		record(App.history(), key, value);
	}

	static void record(AppSettings settings, String key, String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		List<String> entries = new ArrayList<>(load(settings, key));
		entries.remove(value);
		entries.add(0, value);
		while (entries.size() > MAX_ENTRIES) {
			entries.remove(entries.size() - 1);
		}
		save(settings, key, entries);
	}

	static void remove(String key, String value) {
		remove(App.history(), key, value);
	}

	static void remove(AppSettings settings, String key, String value) {
		List<String> entries = new ArrayList<>(load(settings, key));
		if (entries.remove(value)) {
			save(settings, key, entries);
		}
	}

	private static void save(AppSettings settings, String key, List<String> entries) {
		settings.set(key, entries);
		try {
			settings.store();
		} catch (IOException e) {
			App.handleWarn("Failed to save input history", e);
		}
	}
}
