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
import java.util.Locale;

import javax.swing.JOptionPane;

import net.mozq.picto.core.exception.PictoException;
import net.mozq.picto.view.MainFrame;
import net.mozq.picto.view.Messages;

public class AppMain {
	public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
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
}
