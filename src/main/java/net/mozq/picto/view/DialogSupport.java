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

import java.awt.Component;

import javax.swing.JOptionPane;

final class DialogSupport {
	private DialogSupport() {
	}

	/** Shows a Yes/No confirmation dialog for {@code messageKey} and reports whether the user chose Yes. */
	static boolean confirmYesNo(Component parent, String messageKey, Object... args) {
		int result = JOptionPane.showConfirmDialog(
				parent,
				Messages.getString(messageKey, args),
				null,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
				);
		return result == JOptionPane.YES_OPTION;
	}
}
