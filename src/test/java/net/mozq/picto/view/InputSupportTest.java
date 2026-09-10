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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

/**
 * Covers {@link InputSupport#isClickAway}, the decision behind "click on empty space clears focus". A real
 * regression here (fixed in this app) treated any click inside a {@code PopupFactory} popup - which renders
 * in its own plain {@link Window}, never as a descendant of the field it belongs to - as a click away,
 * clearing focus off the field mid-click and closing the popup before the click it was handling completed.
 */
class InputSupportTest {
	@Test
	void neverAwayWhenNothingCurrentlyHasFocus() {
		JTextField clicked = new JTextField();
		assertFalse(InputSupport.isClickAway(null, clicked));
	}

	@Test
	void notAwayWhenClickingTheFocusedComponentItself() {
		JTextField field = new JTextField();
		assertFalse(InputSupport.isClickAway(field, field));
	}

	@Test
	void notAwayWhenClickingADescendantOfTheFocusedComponent() {
		JPanel field = new JPanel();
		JButton descendant = new JButton();
		field.add(descendant);
		assertFalse(InputSupport.isClickAway(field, descendant));
	}

	@Test
	void notAwayWhenClickingInsideAPlainPopupWindow() {
		JTextField field = new JTextField();
		Window popupWindow = new Window(null);
		JButton dayButton = new JButton();
		popupWindow.add(dayButton);

		// This is exactly the DateTimeInputPopup/SuggestionPopup case: the popup is a plain Window, not a
		// descendant of the field, so a click inside it must still not count as "away".
		assertFalse(InputSupport.isClickAway(field, dayButton));
	}

	@Test
	void awayWhenClickingAnUnrelatedComponentInARealFrame() {
		JFrame frame = new JFrame();
		JTextField field = new JTextField();
		JButton unrelatedButton = new JButton();
		frame.getContentPane().add(field);
		frame.getContentPane().add(unrelatedButton);

		assertTrue(InputSupport.isClickAway(field, unrelatedButton));
	}

	@Test
	void awayWhenClickingAnUnrelatedComponentInARealDialog() {
		JDialog dialog = new JDialog();
		JTextField field = new JTextField();
		JButton unrelatedButton = new JButton();
		dialog.getContentPane().add(unrelatedButton);

		assertTrue(InputSupport.isClickAway(field, unrelatedButton));
	}

	@Test
	void notAwayWhenClickingAnotherPartOfTheSameEditableComboBox() {
		JComboBox<String> comboBox = new JComboBox<>(new String[] {"a", "b"});
		comboBox.setEditable(true);
		JTextField editorField = (JTextField)comboBox.getEditor().getEditorComponent();

		// The combo box's own arrow button (or any other of its parts) is a descendant of the combo box,
		// which is itself an ancestor of the currently-focused editor field.
		JButton arrowButton = new JButton();
		comboBox.add(arrowButton);

		assertFalse(InputSupport.isClickAway(editorField, arrowButton));
	}
}
