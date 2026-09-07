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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;

final class InputSupport {

	private static final String CLIENT_PROPERTY_ENABLED_BACKGROUND = "picto.enabledBackground";
	private static final String CLIENT_PROPERTY_DISABLED_BACKGROUND = "picto.disabledBackground";

	private static boolean clickAwayFocusClearInstalled;

	private InputSupport() {
	}

	static synchronized void installClickAwayFocusClear() {
		if (clickAwayFocusClearInstalled) {
			return;
		}
		clickAwayFocusClearInstalled = true;

		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			if (isClickAwayFromFocusedComponent(event)) {
				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			}
		}, AWTEvent.MOUSE_EVENT_MASK);
	}

	private static boolean isClickAwayFromFocusedComponent(AWTEvent event) {
		if (event.getID() != MouseEvent.MOUSE_PRESSED) {
			return false;
		}
		if (MenuSelectionManager.defaultManager().getSelectedPath().length > 0) {
			// A JPopupMenu-based UI (e.g. a JComboBox dropdown) is currently open/being
			// interacted with; let it finish committing the selection undisturbed.
			return false;
		}
		Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		if (focusOwner == null) {
			return false;
		}
		if (!(event.getSource() instanceof Component)) {
			return false;
		}
		Component clicked = (Component)event.getSource();
		return clicked != focusOwner && !SwingUtilities.isDescendingFrom(clicked, focusOwner);
	}

	static void installLabelFocusAction(JLabel label, Component target, LabelFocusBehavior behavior) {
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				target.requestFocusInWindow();
				if (target instanceof JTextComponent) {
					SwingUtilities.invokeLater(() ->
							SwingUtilities.invokeLater(() -> applyTextFocusBehavior((JTextComponent)target, behavior)));
				}
			}
		});
	}

	static void allowDigitsOnly(JTextField textField) {
		if (textField.getDocument() instanceof AbstractDocument) {
			((AbstractDocument)textField.getDocument()).setDocumentFilter(new DocumentFilter() {
				@Override
				public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
					if (isDigits(string)) {
						super.insertString(fb, offset, string, attr);
					}
				}

				@Override
				public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
					if (isDigits(text)) {
						super.replace(fb, offset, length, text, attrs);
					}
				}
			});
		}
	}

	static void configureDisabledBackground(JTextField textField) {
		Color enabledBackground = textField.getBackground();
		Color disabledBackground = UIManager.getColor(textField instanceof JFormattedTextField
				? "FormattedTextField.disabledBackground"
				: "TextField.disabledBackground");
		if (disabledBackground == null) {
			disabledBackground = UIManager.getColor("TextComponent.disabledBackground");
		}
		textField.putClientProperty(CLIENT_PROPERTY_ENABLED_BACKGROUND, enabledBackground);
		textField.putClientProperty(CLIENT_PROPERTY_DISABLED_BACKGROUND, disabledBackground);
	}

	static void setTextFieldEnabled(JTextField textField, boolean enabled) {
		textField.setEnabled(enabled);
		String backgroundProperty = enabled
				? CLIENT_PROPERTY_ENABLED_BACKGROUND
				: CLIENT_PROPERTY_DISABLED_BACKGROUND;
		Object background = textField.getClientProperty(backgroundProperty);
		if (background instanceof Color) {
			textField.setBackground((Color)background);
		}
	}

	static void installFolderDropTarget(JTextField textField) {
		textField.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean canImport(TransferSupport support) {
				return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
						|| support.isDataFlavorSupported(DataFlavor.stringFlavor);
			}

			@Override
			public boolean importData(TransferSupport support) {
				File droppedFile = extractFirstFile(support);
				if (droppedFile == null) {
					return false;
				}
				File folder = droppedFile.isDirectory() ? droppedFile : droppedFile.getParentFile();
				if (folder == null) {
					return false;
				}
				textField.setText(folder.getAbsolutePath());
				return true;
			}
		});
	}

	static void installDateTimeInputPopup(JFormattedTextField field, boolean endOfRange) {
		new DateTimeInputPopup(field, endOfRange, Locale.getDefault());
	}

	static MaskFormatter newMaskFormatter(String mask) {
		try {
			MaskFormatter maskFormatter = new MaskFormatter(mask);
			maskFormatter.setPlaceholderCharacter(DateTimeText.MASK_PLACEHOLDER_CHAR);
			maskFormatter.setValidCharacters(DateTimeText.MASK_VALID_CHARS);
			return maskFormatter;
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private static File extractFirstFile(TransferHandler.TransferSupport support) {
		try {
			if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				List<?> files = (List<?>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
				return files.isEmpty() ? null : (File)files.get(0);
			}
			if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String data = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
				return firstFileFromUriList(data);
			}
		} catch (UnsupportedFlavorException | IOException _) {
			// Ignore a drop whose data can no longer be read.
		}
		return null;
	}

	private static File firstFileFromUriList(String data) {
		for (String line : data.split("\\r?\\n")) {
			String trimmed = line.strip();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				continue;
			}
			try {
				URI uri = new URI(trimmed);
				if ("file".equalsIgnoreCase(uri.getScheme())) {
					return new File(uri);
				}
			} catch (URISyntaxException _) {
				// Ignore a malformed entry and try the next line.
			}
		}
		return null;
	}

	private static void applyTextFocusBehavior(JTextComponent textComponent, LabelFocusBehavior behavior) {
		switch (behavior) {
		case CARET_START:
			textComponent.select(0, 0);
			break;
		case CARET_END:
			int end = textComponent.getDocument().getLength();
			textComponent.select(end, end);
			break;
		case SELECT_ALL:
			textComponent.selectAll();
			break;
		case FOCUS_ONLY:
		default:
			break;
		}
	}

	private static boolean isDigits(String text) {
		if (text == null) {
			return true;
		}
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isDigit(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
