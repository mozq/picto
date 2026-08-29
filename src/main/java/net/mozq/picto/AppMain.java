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
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import net.mozq.picto.core.exception.PictoException;
import net.mozq.picto.view.MainFrame;
import net.mozq.picto.view.Messages;

public class AppMain {
	public static void main(String[] args) throws Exception {
		System.setProperty("apple.awt.application.appearance", "system"); //$NON-NLS-1$ //$NON-NLS-2$
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					configureLookAndFeel();
					
					// Load config
					App.init();
					
					// Initialize settings
					Locale.setDefault(App.config().getLocale("locale", Locale.getDefault()));
					Messages.load(Locale.getDefault());
					
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
			}
		});
	}
	
	private static void configureLookAndFeel() {
		boolean darkMode = isSystemDarkMode();
		if (darkMode) {
			FlatDarkLaf.setup();
		} else {
			FlatLightLaf.setup();
		}
		
		UIManager.put("Component.arc", 6); //$NON-NLS-1$
		UIManager.put("TextComponent.arc", 6); //$NON-NLS-1$
		UIManager.put("Button.arc", 6); //$NON-NLS-1$
		UIManager.put("Component.focusWidth", 1); //$NON-NLS-1$
		UIManager.put("Component.innerFocusWidth", 1); //$NON-NLS-1$
		UIManager.put("TextComponent.margin", new Insets(3, 6, 3, 6)); //$NON-NLS-1$
		
		Color inputBackground = darkMode ? new Color(0x2f3337) : new Color(0xffffff);
		Color disabledInputBackground = darkMode ? new Color(0x3a3d40) : new Color(0xf2f2f2);
		Color inputBorder = darkMode ? new Color(0x6b7178) : new Color(0xb8bec4);
		UIManager.put("TextField.background", inputBackground); //$NON-NLS-1$
		UIManager.put("TextField.disabledBackground", disabledInputBackground); //$NON-NLS-1$
		UIManager.put("TextField.inactiveBackground", disabledInputBackground); //$NON-NLS-1$
		UIManager.put("FormattedTextField.background", inputBackground); //$NON-NLS-1$
		UIManager.put("FormattedTextField.disabledBackground", disabledInputBackground); //$NON-NLS-1$
		UIManager.put("FormattedTextField.inactiveBackground", disabledInputBackground); //$NON-NLS-1$
		UIManager.put("TextComponent.disabledBackground", disabledInputBackground); //$NON-NLS-1$
		UIManager.put("TextComponent.inactiveBackground", disabledInputBackground); //$NON-NLS-1$
		UIManager.put("CheckBox.icon.borderColor", inputBorder); //$NON-NLS-1$
		UIManager.put("CheckBox.icon.focusWidth", 1); //$NON-NLS-1$
		UIManager.put("CheckBox.icon.borderWidth", 1.2f); //$NON-NLS-1$
	}
	
	private static boolean isSystemDarkMode() {
		String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT); //$NON-NLS-1$
		if (osName.contains("mac")) { //$NON-NLS-1$
			return isMacDarkMode();
		}
		if (osName.contains("windows")) { //$NON-NLS-1$
			return isWindowsDarkMode();
		}
		if (osName.contains("linux")) { //$NON-NLS-1$
			return isLinuxDarkMode();
		}
		return false;
	}
	
	private static boolean isMacDarkMode() {
		try {
			Process process = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
					"reg", //$NON-NLS-1$
					"query", //$NON-NLS-1$
					"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", //$NON-NLS-1$
					"/v", //$NON-NLS-1$
					"AppsUseLightTheme") //$NON-NLS-1$
					.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			return process.waitFor() == 0 && output.contains("0x0"); //$NON-NLS-1$
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
					"gsettings", //$NON-NLS-1$
					"get", //$NON-NLS-1$
					"org.gnome.desktop.interface", //$NON-NLS-1$
					"color-scheme") //$NON-NLS-1$
					.start();
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
					.toLowerCase(Locale.ROOT);
			return process.waitFor() == 0 && output.contains("prefer-dark"); //$NON-NLS-1$
		} catch (IOException | InterruptedException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			return false;
		}
	}
}
