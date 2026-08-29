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
package net.mozq.picto.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

class DestinationPathTemplateTest {

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Test
	void resolvesVariables() {
		DestinationPathTemplate template = new DestinationPathTemplate("${ParentSubPath}/${FileName}");

		String text = template.format(Map.of(
				"ParentSubPath", "photos",
				"FileName", "IMG_0001.JPG"
				)::get);

		assertEquals("photos/IMG_0001.JPG", text);
	}

	@Test
	void formatsDateWithPipeSyntax() {
		DestinationPathTemplate template = new DestinationPathTemplate("${PhotoTakenDate|format(\"uuuu/MM\")}");
		template.setTimeZone(UTC);

		String text = template.format(Map.of("PhotoTakenDate", new Date(1_766_320_496_000L))::get);

		assertEquals("2025/12", text);
	}

	@Test
	void formatsNumberWithPipeSyntax() {
		DestinationPathTemplate template = new DestinationPathTemplate("${FNumber|format(\"0.0\")}");

		String text = template.format(Map.of("FNumber", Double.valueOf(8))::get);

		assertEquals("8.0", text);
	}

	@Test
	void choosesValueWithPipeSyntax() {
		DestinationPathTemplate template = new DestinationPathTemplate("${WhiteBalance|choice(0=\"Auto\", 1=\"Manual\", default=\"Others\")}");

		String text = template.format(Map.of("WhiteBalance", Integer.valueOf(1))::get);

		assertEquals("Manual", text);
	}

	@Test
	void choosesDefaultWithPipeSyntax() {
		DestinationPathTemplate template = new DestinationPathTemplate("${WhiteBalance|choice(0=\"Auto\",1=\"Manual\",default=\"Others\")}");

		String text = template.format(Map.of("WhiteBalance", Integer.valueOf(9))::get);

		assertEquals("Others", text);
	}

	@Test
	void returnsOriginalValueWhenChoiceDefaultIsMissing() {
		DestinationPathTemplate template = new DestinationPathTemplate("${WhiteBalance|choice(0=\"Auto\",1=\"Manual\")}");

		String text = template.format(Map.of("WhiteBalance", Integer.valueOf(9))::get);

		assertEquals("9", text);
	}

	@Test
	void appliesFiltersFromLeftToRight() {
		DestinationPathTemplate template = new DestinationPathTemplate("${FNumber|format(\"0.0\")|choice(8.0=\"F8\",default=\"Other\")}");

		String text = template.format(Map.of("FNumber", Double.valueOf(8))::get);

		assertEquals("F8", text);
	}

	@Test
	void usesDefaultValueWhenTextIsEmpty() {
		DestinationPathTemplate template = new DestinationPathTemplate("${Lens|default(\"Unknown\")}");

		String text = template.format(Map.of()::get);

		assertEquals("Unknown", text);
	}

	@Test
	void formatsValueWithShortSyntax() {
		DestinationPathTemplate template = new DestinationPathTemplate("${FNumber:0.0}");

		String text = template.format(Map.of("FNumber", Double.valueOf(5.6))::get);

		assertEquals("5.6", text);
	}

	@Test
	void choosesValueWithShortSyntax() {
		DestinationPathTemplate template = new DestinationPathTemplate("${WhiteBalance?0=Auto,1=Manual,default=Others}");

		String text = template.format(Map.of("WhiteBalance", Integer.valueOf(0))::get);

		assertEquals("Auto", text);
	}

	@Test
	void supportsEscapedTemplateExpression() {
		DestinationPathTemplate template = new DestinationPathTemplate("\\${FileName}/${FileName}");

		String text = template.format(Map.of("FileName", "IMG_0001.JPG")::get);

		assertEquals("${FileName}/IMG_0001.JPG", text);
	}

	@Test
	void supportsEscapedChoiceSeparators() {
		DestinationPathTemplate template = new DestinationPathTemplate("${CameraModel?ILCE\\=7C=Sony\\,Alpha,default=Other}");

		String text = template.format(Map.of("CameraModel", "ILCE=7C")::get);

		assertEquals("Sony,Alpha", text);
	}

	@Test
	void supportsQuotedChoiceSeparators() {
		DestinationPathTemplate template = new DestinationPathTemplate("${CameraModel|choice(\"ILCE=7C\"=\"Sony, Alpha\", default=\"Other\")}");

		String text = template.format(Map.of("CameraModel", "ILCE=7C")::get);

		assertEquals("Sony, Alpha", text);
	}

	@Test
	void resolvesMissingValueAsEmptyText() {
		DestinationPathTemplate template = new DestinationPathTemplate("${Missing}");

		String text = template.format(Map.of()::get);

		assertEquals("", text);
	}
}
