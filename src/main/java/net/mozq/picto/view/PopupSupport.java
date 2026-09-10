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

import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

final class PopupSupport {

	private PopupSupport() {
	}

	static Border createPopupBorder() {
		Border border = UIManager.getBorder("PopupMenu.border");
		if (border != null) {
			return border;
		}
		Color color = UIManager.getColor("Component.borderColor");
		if (color == null) {
			color = Color.GRAY;
		}
		return BorderFactory.createLineBorder(color);
	}

	/**
	 * Hides the popup when its owning window loses focus, and repositions it when the window moves or resizes.
	 *
	 * @return {@code true} if the listeners were installed, {@code false} if {@code field} has no window
	 * ancestor yet (the caller should retry later, e.g. via {@code SwingUtilities.invokeLater}).
	 */
	static boolean installWindowFocusListener(Component field, Runnable onWindowLostFocus, Runnable onWindowChanged) {
		Window window = SwingUtilities.getWindowAncestor(field);
		if (window == null) {
			return false;
		}
		window.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				onWindowLostFocus.run();
			}
		});
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				onWindowChanged.run();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				onWindowChanged.run();
			}
		});
		return true;
	}

	/**
	 * Runs {@code hideIfFocusMovedAway} (asynchronously) whenever the application-wide focus owner changes
	 * while {@code isPopupShowing} reports the popup as visible. Field-local focus/mouse listeners alone are
	 * not enough because some focus transfers (e.g. {@code KeyboardFocusManager.clearGlobalFocusOwner()})
	 * do not fire a {@code FocusEvent} on the field that is losing focus.
	 */
	static void installFocusOwnerChangeListener(BooleanSupplier isPopupShowing, Runnable hideIfFocusMovedAway) {
		PropertyChangeListener listener = _ -> {
			if (isPopupShowing.getAsBoolean()) {
				SwingUtilities.invokeLater(hideIfFocusMovedAway);
			}
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("permanentFocusOwner", listener);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", listener);
	}

	/**
	 * Determines whether a field-anchored popup should be hidden, given the current application focus owner.
	 * The popup stays open while the field itself, a descendant of {@code popupPanel}, or a component accepted
	 * by {@code isAlsoAllowed} (e.g. a caller-specific related control) holds focus.
	 */
	static boolean shouldHidePopup(JTextComponent field, Component popupPanel, Predicate<Component> isAlsoAllowed) {
		return shouldHidePopup(
				KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(), field, popupPanel, isAlsoAllowed);
	}

	/** Same as {@link #shouldHidePopup(JTextComponent, Component, Predicate)}, with the focus owner passed in
	 * explicitly (rather than read from the ambient {@code KeyboardFocusManager}) so the decision can be
	 * exercised deterministically, e.g. from a test, without a real focused window. */
	static boolean shouldHidePopup(Component focusOwner, JTextComponent field, Component popupPanel, Predicate<Component> isAlsoAllowed) {
		if (focusOwner == null) {
			// KeyboardFocusManager.clearGlobalFocusOwner() (the click-away-clears-focus handling in
			// InputSupport) reports a null focus owner rather than transferring focus elsewhere, as does a
			// child combo box's own dropdown list momentarily during some look-and-feels' open/close
			// transition. Treat that as the field itself no longer holding focus, unless it still (rarely)
			// reports otherwise, or the caller's isAlsoAllowed check doesn't actually depend on which
			// component focus is on (e.g. "one of my own combo boxes currently has its dropdown open").
			return !field.isFocusOwner() && !isAlsoAllowed.test(null);
		}
		if (focusOwner == field || SwingUtilities.isDescendingFrom(focusOwner, popupPanel)) {
			return false;
		}
		return !isAlsoAllowed.test(focusOwner);
	}

	/**
	 * Whether {@code e} is the focus grant Swing performs automatically when a window becomes active and
	 * needs to pick a default focus owner, rather than one caused by an actual user action (a click, Tab,
	 * or explicit request). Field-anchored popups use this to avoid popping up uninvited the moment a
	 * window first opens, while still letting the field receive that initial focus normally.
	 */
	static boolean isWindowActivationFocus(FocusEvent e) {
		return e.getCause() == FocusEvent.Cause.ACTIVATION;
	}

	/**
	 * The symmetric counterpart to {@link #shouldHidePopup}: whether a field-anchored popup should be
	 * treated as currently in use for reopening/refreshing purposes. True while the field itself holds
	 * focus, the current focus owner is a descendant of {@code popupPanel}, or {@code isAlsoAllowed} accepts
	 * it (e.g. a caller-specific related control).
	 */
	static boolean isPopupFocusActive(JTextComponent field, Component popupPanel, Predicate<Component> isAlsoAllowed) {
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (field.isFocusOwner() || focusOwner == field) {
			return true;
		}
		return focusOwner != null
				&& (SwingUtilities.isDescendingFrom(focusOwner, popupPanel) || isAlsoAllowed.test(focusOwner));
	}

	/**
	 * Coalesces repeated refresh requests into a single trailing {@code invokeLater}, re-checking
	 * {@code stillActive} at delivery time in case the state that justified refreshing (e.g. the field still
	 * has focus) has changed while the request was pending. {@link SuggestionPopup} and
	 * {@link DateTimeInputPopup} each need their own instance, since the "already scheduled" flag is
	 * per-popup state.
	 */
	static final class DebouncedRefresh {
		private boolean scheduled;

		void request(BooleanSupplier stillActive, Runnable refresh) {
			if (scheduled) {
				return;
			}
			scheduled = true;
			SwingUtilities.invokeLater(() -> {
				scheduled = false;
				if (stillActive.getAsBoolean()) {
					refresh.run();
				}
			});
		}
	}

	/**
	 * Lets Escape close a field-anchored popup without moving focus off the field, for when the field
	 * already has focus but the popup itself is just in the way (the standard convention for dismissing an
	 * autocomplete/suggestion overlay - browser address bars, IDE completion, OS text-field suggestions all
	 * do this). The binding is installed only while the popup is actually showing and removed as soon as it
	 * closes, so pressing Escape with nothing showing falls through to any outer Escape handling (e.g. a
	 * dialog's own close-on-Escape) instead of being silently swallowed.
	 */
	static final class EscapeToHide {
		private static final String ACTION_KEY = "picto.hideOnEscape";
		private static final KeyStroke ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

		private final JComponent field;

		EscapeToHide(JComponent field, Runnable onEscape) {
			this.field = field;
			field.getActionMap().put(ACTION_KEY, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					onEscape.run();
				}
			});
		}

		void install() {
			field.getInputMap(JComponent.WHEN_FOCUSED).put(ESCAPE, ACTION_KEY);
		}

		void uninstall() {
			field.getInputMap(JComponent.WHEN_FOCUSED).remove(ESCAPE);
		}
	}
}
