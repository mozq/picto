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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

/**
 * Covers {@link PopupSupport#shouldHidePopup}'s decision logic with an explicit, synthetic focus owner -
 * this is the exact mechanism behind two real regressions found in this app (a popup closing mid-click on
 * one of its own controls, and a popup failing to close when the user genuinely clicked away), so it is
 * exercised directly here rather than through the real (untestable without a focused window)
 * {@code KeyboardFocusManager}-reading overload.
 */
class PopupSupportTest {
	@Test
	void nullFocusOwnerHidesUnlessTheFieldItselfStillClaimsFocus() {
		JTextField field = new JTextField();
		JPanel popupPanel = new JPanel();

		// A fresh, never-focused field: null focus owner (e.g. after clearGlobalFocusOwner()) means hide.
		assertTrue(PopupSupport.shouldHidePopup(null, field, popupPanel, c -> false));
	}

	@Test
	void keepsOpenWhileTheFieldItselfIsTheFocusOwner() {
		JTextField field = new JTextField();
		JPanel popupPanel = new JPanel();

		assertFalse(PopupSupport.shouldHidePopup(field, field, popupPanel, c -> false));
	}

	@Test
	void keepsOpenWhileFocusIsInsideThePopupPanel() {
		JTextField field = new JTextField();
		JPanel popupPanel = new JPanel();
		JTextField insidePopup = new JTextField();
		popupPanel.add(insidePopup);

		assertFalse(PopupSupport.shouldHidePopup(insidePopup, field, popupPanel, c -> false));
	}

	@Test
	void hidesWhenAnUnrelatedComponentTakesFocus() {
		JTextField field = new JTextField();
		JPanel popupPanel = new JPanel();
		JTextField somewhereElse = new JTextField();

		assertTrue(PopupSupport.shouldHidePopup(somewhereElse, field, popupPanel, c -> false));
	}

	@Test
	void keepsOpenWhenTheCallerAcceptsTheUnrelatedFocusOwner() {
		JTextField field = new JTextField();
		JPanel popupPanel = new JPanel();
		JTextField relatedControl = new JTextField();

		assertFalse(PopupSupport.shouldHidePopup(relatedControl, field, popupPanel, c -> c == relatedControl));
	}

	@Test
	void installWindowFocusListenerFailsUntilTheFieldHasAWindowAncestor() {
		JTextField orphanField = new JTextField();
		assertFalse(PopupSupport.installWindowFocusListener(orphanField, () -> { }, () -> { }));

		JFrame frame = new JFrame();
		JTextField attachedField = new JTextField();
		frame.getContentPane().add(attachedField);
		assertTrue(PopupSupport.installWindowFocusListener(attachedField, () -> { }, () -> { }));
	}
}
