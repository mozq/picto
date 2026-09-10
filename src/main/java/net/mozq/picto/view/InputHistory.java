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

import net.mozq.picto.App;

/**
 * Most-recently-used text input history (recent folders, file name patterns, Subfolder templates, ...),
 * persisted in {@value net.mozq.picto.App#HISTORY_FILE_NAME}.
 */
final class InputHistory {
	static final String SRC_ROOT_DIR_KEY = "src.root.dir";
	static final String DEST_ROOT_DIR_KEY = "dest.root.dir";
	static final String FILE_PATTERN_KEY = "file.pattern";
	static final String DEST_SUB_PATH_PATTERN_KEY = "dest.sub.path.pattern";

	private static final int MAX_ENTRIES = 5;

	private InputHistory() {
	}

	static List<String> load(String key) {
		return App.history().getList(key, String.class, List.of());
	}

	static void record(String key, Path path) {
		record(key, path.toString());
	}

	static void record(String key, String value) {
		if (value == null || value.isBlank()) {
			return;
		}
		List<String> entries = new ArrayList<>(load(key));
		entries.remove(value);
		entries.add(0, value);
		while (entries.size() > MAX_ENTRIES) {
			entries.remove(entries.size() - 1);
		}
		save(key, entries);
	}

	static void remove(String key, String value) {
		List<String> entries = new ArrayList<>(load(key));
		if (entries.remove(value)) {
			save(key, entries);
		}
	}

	private static void save(String key, List<String> entries) {
		App.history().set(key, entries);
		try {
			App.history().store();
		} catch (IOException e) {
			App.handleWarn("Failed to save input history", e);
		}
	}
}
