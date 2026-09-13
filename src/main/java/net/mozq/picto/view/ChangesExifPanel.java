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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

class ChangesExifPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	final JCheckBox chkRemoveGps;
	final JCheckBox chkRemoveAll;

	ChangesExifPanel(int sectionPadding) {
		setBorder(new EmptyBorder(sectionPadding, sectionPadding, sectionPadding, sectionPadding));
		GridBagLayout exifLayout = new GridBagLayout();
		exifLayout.columnWidths = new int[]{0, 0};
		exifLayout.rowHeights = new int[]{0, 0, 0};
		exifLayout.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		exifLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(exifLayout);

		chkRemoveGps = new JCheckBox(Messages.getString("MainFrame.changes.exif.removeGps"));
		add(chkRemoveGps, GridBagSupport.at(0, 0).anchor(GridBagConstraints.WEST).insets(0, 0, 5, 0).build());

		chkRemoveAll = new JCheckBox(Messages.getString("MainFrame.changes.exif.removeAll"));
		add(chkRemoveAll, GridBagSupport.at(0, 1).anchor(GridBagConstraints.WEST).build());
	}
}
