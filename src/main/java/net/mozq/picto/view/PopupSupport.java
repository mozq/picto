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

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;

final class PopupSupport {

	private PopupSupport() {
	}

	static Border createPopupBorder() {
		Border border = UIManager.getBorder("PopupMenu.border"); //$NON-NLS-1$
		if (border != null) {
			return border;
		}
		Color color = UIManager.getColor("Component.borderColor"); //$NON-NLS-1$
		if (color == null) {
			color = Color.GRAY;
		}
		return BorderFactory.createLineBorder(color);
	}
}
