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

import javax.swing.JTabbedPane;

class ChangesPanel extends JTabbedPane {
	private static final long serialVersionUID = 1L;

	final ChangesFileDatePanel filedate;
	final ChangesExifPanel exif;

	ChangesPanel(int sectionPadding, int inlineHgap, int inlineVgap, Runnable enableChanged, Runnable layoutChanged) {
		super(JTabbedPane.TOP);
		putClientProperty("JTabbedPane.tabType", "card");

		filedate = new ChangesFileDatePanel(sectionPadding, inlineHgap, inlineVgap, enableChanged, layoutChanged);
		addTab(Messages.getString("MainFrame.changes.filedate.title"), null, filedate, null);

		exif = new ChangesExifPanel(sectionPadding);
		addTab(Messages.getString("MainFrame.changes.exif.title"), null, exif, null);
	}
}
