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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.text.JTextComponent;

import net.mozq.nanotemplate.NanoTemplate;

import net.mozq.picto.view.SuggestionPopup.SuggestionItem;
import net.mozq.picto.view.SuggestionPopup.SuggestionSection;

class SubfolderTemplatePopup {
	private static final String KEY_PREFIX = "MainFrame.destSubPathInsert.";
	private static final int POPUP_MAX_HEIGHT = 280;
	private static final int POPUP_MIN_WIDTH = 440;
	private static final Map<String, Object> SAMPLE_VALUES = Map.ofEntries(
			Map.entry("TakenDate", Date.from(Instant.parse("2023-04-05T10:20:30Z"))),
			Map.entry("SubFilePath", "Trips/Kyoto/IMG_0123.jpg"),
			Map.entry("SubFolderPath", "Trips/Kyoto"),
			Map.entry("FileName", "IMG_0123.jpg"),
			Map.entry("BaseName", "IMG_0123"),
			Map.entry("Extension", "jpg"),
			Map.entry("Make", "Canon"),
			Map.entry("Model", "EOS R8"),
			Map.entry("FNumber", new BigDecimal("2.8")),
			Map.entry("ISO", 400),
			Map.entry("GPSLat", new BigDecimal("35.681236")),
			Map.entry("GPSLon", new BigDecimal("139.767125")));

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
				section("template",
						item("keepOriginalStructure", "${SubFilePath}"),
						item("byPhotoDate", "${TakenDate:uuuu-MM-dd}/${FileName}"),
						item("byPhotoYearMonthDate", "${TakenDate:uuuu/MM/dd}/${FileName}"),
						item("byParentAndPhotoDate", "${SubFolderPath}/${TakenDate:uuuu-MM-dd}/${FileName}"),
						item("byCamera", "${Make}/${Model}/${FileName}"),
						item("byCameraAndPhotoDate", "${Make}/${Model}/${TakenDate:uuuu-MM-dd}/${FileName}"),
						item("byPhotoDateAndCamera", "${TakenDate:uuuu-MM-dd}/${Make}/${Model}/${FileName}")),
				section("date",
						item("photoTakenDate", "${TakenDate:uuuu-MM-dd}"),
						item("photoTakenYearMonthDate", "${TakenDate:uuuu/MM/dd}")),
				section("file",
						item("subFilePath", "${SubFilePath}"),
						item("subFolderPath", "${SubFolderPath}"),
						item("fileName", "${FileName}"),
						item("baseName", "${BaseName}"),
						item("extension", "${Extension}")),
				section("exif",
						item("make", "${Make}"),
						item("model", "${Model}"),
						item("fNumber", "${FNumber:0.0}"),
						item("iso", "${ISO}")),
				section("gps",
						item("latitude", "${GPSLat:0.000000}"),
						item("longitude", "${GPSLon:0.000000}")));
	}

	private static SuggestionSection section(String key, SuggestionItem... items) {
		return new SuggestionSection(Messages.getString(KEY_PREFIX + key), List.of(items));
	}

	private static SuggestionItem item(String key, String value) {
		return new SuggestionItem(Messages.getString(KEY_PREFIX + key), value, sample(value));
	}

	private static String sample(String template) {
		try {
			return new NanoTemplate(template)
					.timeZone(TimeZone.getTimeZone("UTC"))
					.render(SAMPLE_VALUES);
		} catch (RuntimeException _) {
			return null;
		}
	}
}
