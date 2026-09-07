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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import javax.swing.BorderFactory;
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
		PropertyChangeListener listener = evt -> {
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
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focusOwner == null) {
			return !field.isFocusOwner();
		}
		if (focusOwner == field || SwingUtilities.isDescendingFrom(focusOwner, popupPanel)) {
			return false;
		}
		return !isAlsoAllowed.test(focusOwner);
	}
}
