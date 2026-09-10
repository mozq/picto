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

import javax.swing.text.JTextComponent;

import net.mozq.picto.view.SuggestionPopup.SuggestionSection;

/** Shows a dropdown of recently used folder paths for a root-folder text field, backed by {@link InputHistory}. */
class FolderHistoryPopup {
	private static final int POPUP_MAX_HEIGHT = 220;
	private static final int POPUP_MIN_WIDTH = 380;

	private final SuggestionPopup popup;
	private final String historyKey;

	FolderHistoryPopup(JTextComponent field, String historyKey, Component... relatedFocusComponents) {
		this.historyKey = historyKey;
		this.popup = new SuggestionPopup(
				field,
				this::sections,
				value -> {
					field.setText(value);
					field.selectAll();
				},
				item -> InputHistory.remove(historyKey, item.value()),
				Messages.getString("MainFrame.history.remove"),
				POPUP_MIN_WIDTH,
				POPUP_MAX_HEIGHT,
				relatedFocusComponents);
	}

	void refresh() {
		popup.refresh();
	}

	private List<SuggestionSection> sections() {
		return SuggestionPopup.historySection(historyKey);
	}
}
