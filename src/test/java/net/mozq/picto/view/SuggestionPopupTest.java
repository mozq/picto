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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import net.mozq.picto.view.SuggestionPopup.SuggestionItem;
import net.mozq.picto.view.SuggestionPopup.SuggestionSection;

/**
 * Covers {@link SuggestionPopup}'s row rendering and click behavior: which rows get a delete ("✕") button,
 * and whether picking a value replaces the field's whole content or is inserted at the caret. Both are real
 * regressions found in this app - a delete button that leaked onto every row instead of just the history
 * section's, and a history/template value that got inserted into partially-typed text instead of replacing
 * it - so they are pinned down here. {@code createContent()} and {@code hasAnyItems()} are package-private
 * specifically so this test can drive them directly, since {@code showPopup()}'s own focus-gated path can't
 * be exercised without a real focused window.
 */
class SuggestionPopupTest {
	@Test
	void sectionDefaultsToNotRemovableAndNotAppendOnSelect() {
		SuggestionSection section = new SuggestionSection("label", List.of());
		assertFalse(section.removable());
		assertFalse(section.appendOnSelect());
	}

	@Test
	void removableConvenienceConstructorNeverAppendsOnSelect() {
		SuggestionSection section = new SuggestionSection("label", List.of(), true);
		assertTrue(section.removable());
		assertFalse(section.appendOnSelect());
	}

	@Test
	void hasAnyItemsIsFalseWhenEverySectionIsEmpty() {
		JTextField field = new JTextField();
		SuggestionPopup popup = popupWithSections(field, List.of(new SuggestionSection("empty", List.of())));
		assertFalse(popup.hasAnyItems());
	}

	@Test
	void hasAnyItemsIsTrueWhenAnySectionHasItems() {
		JTextField field = new JTextField();
		SuggestionPopup popup = popupWithSections(field,
				List.of(new SuggestionSection("has items", List.of(new SuggestionItem("label", "value")))));
		assertTrue(popup.hasAnyItems());
	}

	@Test
	void onlyRemovableSectionsRenderADeleteButton() {
		JTextField field = new JTextField();
		List<SuggestionSection> sections = List.of(
				new SuggestionSection("Recent", List.of(new SuggestionItem("", "recent-value")), true),
				new SuggestionSection("Presets", List.of(new SuggestionItem("", "preset-value"))));
		SuggestionPopup popup = new SuggestionPopup(
				field, () -> sections, value -> { }, item -> { }, "remove", 4, 100);

		JScrollPane content = popup.createContent();
		assertTrue(hasDeleteButton(findRow(content, "recent-value")));
		assertFalse(hasDeleteButton(findRow(content, "preset-value")));
	}

	@Test
	void pickingADefaultSectionItemReplacesTheWholeField() {
		JTextField field = new JTextField("leftover-partial-text");
		field.setCaretPosition(field.getText().length());
		SuggestionPopup popup = new SuggestionPopup(
				field,
				() -> List.of(new SuggestionSection("section", List.of(new SuggestionItem("", "whole-value")))),
				value -> field.replaceSelection(value),
				4, 100);

		click(findRow(popup.createContent(), "whole-value"));

		assertEquals("whole-value", field.getText());
	}

	@Test
	void pickingAnAppendOnSelectItemInsertsAtTheCaretInstead() {
		JTextField field = new JTextField("prefix-");
		field.setCaretPosition(field.getText().length());
		SuggestionPopup popup = new SuggestionPopup(
				field,
				() -> List.of(new SuggestionSection("fragments", List.of(new SuggestionItem("", "fragment")), false, true)),
				value -> field.replaceSelection(value),
				4, 100);

		click(findRow(popup.createContent(), "fragment"));

		assertEquals("prefix-fragment", field.getText());
	}

	@Test
	void clickingDeleteInvokesTheCallbackInsteadOfPickingTheValue() {
		JTextField field = new JTextField("unchanged");
		List<String> deleted = new ArrayList<>();
		SuggestionPopup popup = new SuggestionPopup(
				field,
				() -> List.of(new SuggestionSection("Recent", List.of(new SuggestionItem("", "recent-value")), true)),
				value -> field.setText(value),
				item -> deleted.add(item.value()),
				"remove",
				4, 100);

		JButton deleteButton = findDeleteButton(findRow(popup.createContent(), "recent-value"));
		deleteButton.doClick();

		assertEquals(List.of("recent-value"), deleted);
		assertEquals("unchanged", field.getText());
		assertEquals("remove", deleteButton.getToolTipText());
	}

	private static SuggestionPopup popupWithSections(JTextField field, List<SuggestionSection> sections) {
		return new SuggestionPopup(field, () -> sections, value -> { }, 4, 100);
	}

	private static void click(Container row) {
		for (var listener : row.getMouseListeners()) {
			listener.mousePressed(new MouseEvent(row, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 0, 0, 1, false));
		}
	}

	private static boolean hasDeleteButton(Container row) {
		return findDeleteButton(row) != null;
	}

	private static JButton findDeleteButton(Container row) {
		JButton[] found = {null};
		visit(row, c -> {
			if (c instanceof JButton button && "✕".equals(button.getText())) {
				found[0] = button;
			}
		});
		return found[0];
	}

	private static Container findRow(Container root, String value) {
		JLabel label = findLabel(root, value);
		assertNotNullLabel(label, value);
		return label.getParent();
	}

	private static void assertNotNullLabel(JLabel label, String value) {
		if (label == null) {
			throw new AssertionError("no rendered row found for value [" + value + "]");
		}
	}

	private static JLabel findLabel(Container root, String text) {
		JLabel[] found = {null};
		visit(root, c -> {
			if (found[0] == null && c instanceof JLabel label && text.equals(label.getText())) {
				found[0] = label;
			}
		});
		return found[0];
	}

	private static void visit(Container root, java.util.function.Consumer<Component> visitor) {
		for (Component child : root.getComponents()) {
			visitor.accept(child);
			if (child instanceof Container childContainer) {
				visit(childContainer, visitor);
			}
		}
	}
}
