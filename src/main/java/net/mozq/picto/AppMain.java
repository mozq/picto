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
package net.mozq.picto;

import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Insets;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import net.mozq.picto.core.exception.PictoException;
import net.mozq.picto.view.MainFrame;
import net.mozq.picto.view.Messages;

public class AppMain {
	public static final String PREF_LOCALE_KEY = "locale";
	public static final String PREF_APPEARANCE_KEY = "appearance";
	public static final String PREF_SYSTEM = "system";
	public static final String PREF_LOCALE_EN = "en";
	public static final String PREF_LOCALE_JA = "ja";
	public static final String PREF_APPEARANCE_LIGHT = "light";
	public static final String PREF_APPEARANCE_DARK = "dark";
	private static final Locale SYSTEM_LOCALE = Locale.getDefault();

	public static void main(String[] args) {
		System.setProperty("apple.awt.application.appearance", "system");

		try {
			// Load config outside EDT
			App.init();
		} catch (Exception e) {
			App.handleError(e.getMessage(), e);
			String message = e.getLocalizedMessage();
			if (!(e instanceof PictoException)) {
				message = Messages.getString("message.error", message);
			}
			final String errorMessage = message;
			EventQueue.invokeLater(() -> {
				JOptionPane.showMessageDialog(null, errorMessage, null, JOptionPane.ERROR_MESSAGE);
			});
			return;
		}

		EventQueue.invokeLater(() -> {
			try {
				// Initialize settings
				applyConfiguredUiSettings();

				MainFrame frame = new MainFrame();
				frame.setVisible(true);
			} catch (Exception e) {
				String message = e.getLocalizedMessage();
				if (!(e instanceof PictoException)) {
					message = Messages.getString("message.error", message);
				}

				JOptionPane.showMessageDialog(null, message, null, JOptionPane.ERROR_MESSAGE);

				App.handleError(e.getMessage(), e);
			}
		});
	}

	public static void applyConfiguredUiSettings() {
		Locale.setDefault(configuredLocale());
		Messages.load(Locale.getDefault());
		configureLookAndFeel();
		SwingUtilities.invokeLater(com.formdev.flatlaf.FlatLaf::updateUI);
	}

	private static Locale configuredLocale() {
		String locale = App.preferences().getString(PREF_LOCALE_KEY, PREF_SYSTEM);
		if (PREF_LOCALE_EN.equals(locale)) {
			return Locale.ENGLISH;
		}
		if (PREF_LOCALE_JA.equals(locale)) {
			return Locale.JAPANESE;
		}
		return SYSTEM_LOCALE;
	}

	private static void configureLookAndFeel() {
		boolean darkMode = configuredDarkMode();
		if (darkMode) {
			FlatDarkLaf.setup();
		} else {
			FlatLightLaf.setup();
		}

		UIManager.put("Component.arc", 6);
		UIManager.put("TextComponent.arc", 6);
		UIManager.put("Button.arc", 6);
		UIManager.put("Component.focusWidth", 1);
		UIManager.put("Component.innerFocusWidth", 1);
		UIManager.put("TextComponent.margin", new Insets(3, 6, 3, 6));

		Color inputBackground = darkMode ? new Color(0x2f3337) : new Color(0xffffff);
		Color disabledInputBackground = darkMode ? new Color(0x3a3d40) : new Color(0xf2f2f2);
		Color inputBorder = darkMode ? new Color(0x6b7178) : new Color(0xb8bec4);
		UIManager.put("TextField.background", inputBackground);
		UIManager.put("TextField.disabledBackground", disabledInputBackground);
		UIManager.put("TextField.inactiveBackground", disabledInputBackground);
		UIManager.put("FormattedTextField.background", inputBackground);
		UIManager.put("FormattedTextField.disabledBackground", disabledInputBackground);
		UIManager.put("FormattedTextField.inactiveBackground", disabledInputBackground);
		UIManager.put("TextComponent.disabledBackground", disabledInputBackground);
		UIManager.put("TextComponent.inactiveBackground", disabledInputBackground);
		UIManager.put("CheckBox.icon.borderColor", inputBorder);
		UIManager.put("CheckBox.icon.focusWidth", 1);
		UIManager.put("CheckBox.icon.borderWidth", 1.2f);
	}

	private static boolean configuredDarkMode() {
		String appearance = App.preferences().getString(PREF_APPEARANCE_KEY, PREF_SYSTEM);
		if (PREF_APPEARANCE_DARK.equals(appearance)) {
			return true;
		}
		if (PREF_APPEARANCE_LIGHT.equals(appearance)) {
			return false;
		}
		return isSystemDarkMode();
	}

	private static boolean isSystemDarkMode() {
		String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
		if (osName.contains("mac")) {
			return isMacDarkMode();
		}
		if (osName.contains("windows")) {
			return isWindowsDarkMode();
		}
		if (osName.contains("linux")) {
			return isLinuxDarkMode();
		}
		return false;
	}

	private static boolean isMacDarkMode() {
		try {
			Process process = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start();
			return process.waitFor() == 0;
		} catch (IOException | InterruptedException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return false;
		}
	}

	private static boolean isWindowsDarkMode() {
		try {
			Process process = new ProcessBuilder(
					"reg",
					"query",
					"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
					"/v",
					"AppsUseLightTheme")
					.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			return process.waitFor() == 0 && output.contains("0x0");
		} catch (IOException | InterruptedException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return false;
		}
	}

	private static boolean isLinuxDarkMode() {
		try {
			Process process = new ProcessBuilder(
					"gsettings",
					"get",
					"org.gnome.desktop.interface",
					"color-scheme")
					.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
					.toLowerCase(Locale.ROOT);
			return process.waitFor() == 0 && output.contains("prefer-dark");
		} catch (IOException | InterruptedException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return false;
		}
	}
}
