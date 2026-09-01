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
import java.util.List;
import java.util.function.BooleanSupplier;

import javax.swing.text.JTextComponent;

import net.mozq.picto.view.SuggestionPopup.SuggestionItem;
import net.mozq.picto.view.SuggestionPopup.SuggestionSection;

class FileNamePatternPopup {
	private static final String KEY_PREFIX = "MainFrame.filePatternPreset."; //$NON-NLS-1$
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
				POPUP_MIN_WIDTH,
				POPUP_MAX_HEIGHT,
				relatedFocusComponents);
	}

	void refresh() {
		popup.refresh();
	}

	private static List<SuggestionSection> sections(boolean regex) {
		return regex ? regexSections() : wildcardSections();
	}

	private static List<SuggestionSection> wildcardSections() {
		return List.of(
				section("images", //$NON-NLS-1$
						item("allImages", "*.{jpg,jpeg,png,gif,webp,heic,heif}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("jpegImages", "*.{jpg,jpeg}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("pngImages", "*.png"), //$NON-NLS-1$ //$NON-NLS-2$
						item("heifImages", "*.{heic,heif}")), //$NON-NLS-1$ //$NON-NLS-2$
				section("videos", //$NON-NLS-1$
						item("allVideos", "*.{mp4,mov,m4v,avi,mkv,webm}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("mp4Videos", "*.mp4"), //$NON-NLS-1$ //$NON-NLS-2$
						item("movVideos", "*.mov")), //$NON-NLS-1$ //$NON-NLS-2$
				section("camera", //$NON-NLS-1$
						item("cameraFiles", "IMG_*.*"), //$NON-NLS-1$ //$NON-NLS-2$
						item("cameraNumberedFiles", "IMG_????.*")), //$NON-NLS-1$ //$NON-NLS-2$
				section("general", //$NON-NLS-1$
						item("allFiles", "*"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static List<SuggestionSection> regexSections() {
		return List.of(
				section("images", //$NON-NLS-1$
						item("allImages", ".*\\.(jpg|jpeg|png|gif|webp|heic|heif)"), //$NON-NLS-1$ //$NON-NLS-2$
						item("jpegImages", ".*\\.(jpg|jpeg)"), //$NON-NLS-1$ //$NON-NLS-2$
						item("pngImages", ".*\\.png"), //$NON-NLS-1$ //$NON-NLS-2$
						item("heifImages", ".*\\.(heic|heif)")), //$NON-NLS-1$ //$NON-NLS-2$
				section("videos", //$NON-NLS-1$
						item("allVideos", ".*\\.(mp4|mov|m4v|avi|mkv|webm)"), //$NON-NLS-1$ //$NON-NLS-2$
						item("mp4Videos", ".*\\.mp4"), //$NON-NLS-1$ //$NON-NLS-2$
						item("movVideos", ".*\\.mov")), //$NON-NLS-1$ //$NON-NLS-2$
				section("camera", //$NON-NLS-1$
						item("cameraFiles", "IMG_.*\\..*"), //$NON-NLS-1$ //$NON-NLS-2$
						item("cameraNumberedFiles", "IMG_[0-9]{4}\\..*")), //$NON-NLS-1$ //$NON-NLS-2$
				section("general", //$NON-NLS-1$
						item("allFiles", ".*"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static SuggestionSection section(String key, SuggestionItem... items) {
		return new SuggestionSection(Messages.getString(KEY_PREFIX + key), List.of(items));
	}

	private static SuggestionItem item(String key, String value) {
		return new SuggestionItem(Messages.getString(KEY_PREFIX + key), value);
	}
}
