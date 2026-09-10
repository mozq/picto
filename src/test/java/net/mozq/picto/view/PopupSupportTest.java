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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

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
	void keepsOpenOnANullFocusOwnerWhenTheCallerAcceptsItRegardless() {
		JTextField field = new JTextField();
		JPanel popupPanel = new JPanel();

		// A transient null focus owner (e.g. some look-and-feels report this mid-transition while a child
		// combo box's own dropdown is opening) must not close the popup when the caller's isAlsoAllowed
		// check doesn't actually depend on which component focus is on - the mechanism
		// DateTimeInputPopup relies on for its year/month combo boxes instead of a separate ad-hoc guard.
		assertFalse(PopupSupport.shouldHidePopup(null, field, popupPanel, c -> true));
	}

	@Test
	void windowActivationFocusIsRecognizedRegardlessOfUserDrivenFocusCauses() {
		JTextField field = new JTextField();

		// Swing reports the automatic focus grant a window makes when it becomes active (no user action
		// involved) as ACTIVATION - this is exactly the transition a popup must not react to, e.g. by
		// popping up uninvited the moment a window first opens. A real click or Tab traversal reports a
		// different cause and must still be treated as a genuine focus gain.
		assertTrue(PopupSupport.isWindowActivationFocus(
				new FocusEvent(field, FocusEvent.FOCUS_GAINED, false, null, FocusEvent.Cause.ACTIVATION)));
		assertFalse(PopupSupport.isWindowActivationFocus(
				new FocusEvent(field, FocusEvent.FOCUS_GAINED, false, null, FocusEvent.Cause.MOUSE_EVENT)));
		assertFalse(PopupSupport.isWindowActivationFocus(
				new FocusEvent(field, FocusEvent.FOCUS_GAINED, false, null, FocusEvent.Cause.TRAVERSAL_FORWARD)));
	}

	@Test
	void escapeToHideOnlyInterceptsEscapeWhileInstalled() {
		JTextField field = new JTextField();
		AtomicBoolean hidden = new AtomicBoolean();
		PopupSupport.EscapeToHide escapeToHide = new PopupSupport.EscapeToHide(field, () -> hidden.set(true));
		KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

		assertNull(field.getInputMap(JComponent.WHEN_FOCUSED).get(escapeKey),
				"must not intercept Escape before install(), so it falls through to any outer handling");

		escapeToHide.install();
		Object actionKey = field.getInputMap(JComponent.WHEN_FOCUSED).get(escapeKey);
		assertNotNull(actionKey);
		field.getActionMap().get(actionKey).actionPerformed(new ActionEvent(field, ActionEvent.ACTION_PERFORMED, null));
		assertTrue(hidden.get(), "the bound action must invoke the onEscape callback");

		escapeToHide.uninstall();
		assertNull(field.getInputMap(JComponent.WHEN_FOCUSED).get(escapeKey),
				"must stop intercepting Escape once uninstalled");
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
