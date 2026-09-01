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

import java.util.List;

import javax.swing.text.JTextComponent;

import net.mozq.picto.view.SuggestionPopup.SuggestionItem;
import net.mozq.picto.view.SuggestionPopup.SuggestionSection;

class SubfolderTemplatePopup {
	private static final String KEY_PREFIX = "MainFrame.destSubPathInsert."; //$NON-NLS-1$
	private static final int POPUP_MAX_HEIGHT = 280;
	private static final int POPUP_MIN_WIDTH = 440;

	SubfolderTemplatePopup(JTextComponent field) {
		new SuggestionPopup(
				field,
				SubfolderTemplatePopup::sections,
				value -> field.replaceSelection(value),
				POPUP_MIN_WIDTH,
				POPUP_MAX_HEIGHT);
	}

	private static List<SuggestionSection> sections() {
		return List.of(
				section("template", //$NON-NLS-1$
						item("byPhotoDate", "${PhotoTakenDate:uuuu-MM-dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("byPhotoYearMonthDate", "${PhotoTakenDate:uuuu/MM/dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("byParentAndPhotoDate", "${ParentSubPath}/${PhotoTakenDate:uuuu-MM-dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("byCamera", "${Make}/${Model}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("byCameraAndPhotoDate", "${Make}/${Model}/${PhotoTakenDate:uuuu-MM-dd}/${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("byPhotoDateAndCamera", "${PhotoTakenDate:uuuu-MM-dd}/${Make}/${Model}/${FileName}")), //$NON-NLS-1$ //$NON-NLS-2$
				section("date", //$NON-NLS-1$
						item("photoTakenDate", "${PhotoTakenDate:uuuu-MM-dd}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("photoTakenYearMonthDate", "${PhotoTakenDate:uuuu/MM/dd}")), //$NON-NLS-1$ //$NON-NLS-2$
				section("file", //$NON-NLS-1$
						item("parentSubfolder", "${ParentSubPath}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("fileName", "${FileName}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("baseName", "${BaseName}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("extension", "${Extension}")), //$NON-NLS-1$ //$NON-NLS-2$
				section("exif", //$NON-NLS-1$
						item("make", "${Make}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("model", "${Model}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("fNumber", "${FNumber:0.0}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("iso", "${ISO}")), //$NON-NLS-1$ //$NON-NLS-2$
				section("gps", //$NON-NLS-1$
						item("latitude", "${GPSLat:0.000000}"), //$NON-NLS-1$ //$NON-NLS-2$
						item("longitude", "${GPSLon:0.000000}"))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static SuggestionSection section(String key, SuggestionItem... items) {
		return new SuggestionSection(Messages.getString(KEY_PREFIX + key), List.of(items));
	}

	private static SuggestionItem item(String key, String value) {
		return new SuggestionItem(Messages.getString(KEY_PREFIX + key), value);
	}
}
