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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.text.JTextComponent;

import net.mozq.picto.App;
import net.mozq.picto.view.SuggestionPopup.SuggestionItem;
import net.mozq.picto.view.SuggestionPopup.SuggestionSection;

class FileNamePatternPopup {
	private static final String KEY_PREFIX = "MainFrame.filePatternPreset.";
	private static final int POPUP_MAX_HEIGHT = 260;
	private static final int POPUP_MIN_WIDTH = 430;

	private final SuggestionPopup popup;

	FileNamePatternPopup(JTextComponent field, BooleanSupplier regexSelected, Component... relatedFocusComponents) {
		this.popup = new SuggestionPopup(
				field,
				() -> sections(regexSelected.getAsBoolean()),
				value -> {
					field.setText(value);
					field.selectAll();
				},
				item -> InputHistory.remove(App.history(), InputHistory.FILE_PATTERN_KEY, item.value()),
				Messages.getString("MainFrame.history.remove"),
				POPUP_MIN_WIDTH,
				POPUP_MAX_HEIGHT,
				relatedFocusComponents);
	}

	void refresh() {
		popup.refresh();
	}

	private static List<SuggestionSection> sections(boolean regex) {
		List<SuggestionSection> sections = new ArrayList<>();
		List<String> history = InputHistory.load(App.history(), InputHistory.FILE_PATTERN_KEY);
		if (!history.isEmpty()) {
			sections.add(new SuggestionSection(
					Messages.getString("MainFrame.history.title"),
					history.stream().map(value -> new SuggestionItem("", value)).toList(),
					true));
		}
		sections.addAll(regex ? regexSections() : wildcardSections());
		return sections;
	}

	private static List<SuggestionSection> wildcardSections() {
		return List.of(
				section("images",
						item("allImages", "*.{jpg,jpeg,png,gif,webp,heic,heif}"),
						item("jpegImages", "*.{jpg,jpeg}"),
						item("pngImages", "*.png"),
						item("heifImages", "*.{heic,heif}")),
				section("videos",
						item("allVideos", "*.{mp4,mov,m4v,avi,mkv,webm}"),
						item("mp4Videos", "*.mp4"),
						item("movVideos", "*.mov")),
				section("camera",
						item("cameraFiles", "IMG_*.*"),
						item("cameraNumberedFiles", "IMG_????.*")),
				section("general",
						item("allFiles", "*")));
	}

	private static List<SuggestionSection> regexSections() {
		return List.of(
				section("images",
						item("allImages", ".*\\.(jpg|jpeg|png|gif|webp|heic|heif)"),
						item("jpegImages", ".*\\.(jpg|jpeg)"),
						item("pngImages", ".*\\.png"),
						item("heifImages", ".*\\.(heic|heif)")),
				section("videos",
						item("allVideos", ".*\\.(mp4|mov|m4v|avi|mkv|webm)"),
						item("mp4Videos", ".*\\.mp4"),
						item("movVideos", ".*\\.mov")),
				section("camera",
						item("cameraFiles", "IMG_.*\\..*"),
						item("cameraNumberedFiles", "IMG_[0-9]{4}\\..*")),
				section("general",
						item("allFiles", ".*")));
	}

	private static SuggestionSection section(String key, SuggestionItem... items) {
		return new SuggestionSection(Messages.getString(KEY_PREFIX + key), List.of(items));
	}

	private static SuggestionItem item(String key, String value) {
		return new SuggestionItem(Messages.getString(KEY_PREFIX + key), value);
	}
}
