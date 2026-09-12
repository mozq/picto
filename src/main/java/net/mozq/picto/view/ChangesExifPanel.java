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
import java.awt.Insets;

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

		chkRemoveGps = new JCheckBox(Messages.getString("MainFrame.removeExifTagsGps"));
		GridBagConstraints removeGpsConstraints = new GridBagConstraints();
		removeGpsConstraints.anchor = GridBagConstraints.WEST;
		removeGpsConstraints.insets = new Insets(0, 0, 5, 0);
		removeGpsConstraints.gridx = 0;
		removeGpsConstraints.gridy = 0;
		add(chkRemoveGps, removeGpsConstraints);

		chkRemoveAll = new JCheckBox(Messages.getString("MainFrame.removeExifTagsAll"));
		GridBagConstraints removeAllConstraints = new GridBagConstraints();
		removeAllConstraints.anchor = GridBagConstraints.WEST;
		removeAllConstraints.gridx = 0;
		removeAllConstraints.gridy = 1;
		add(chkRemoveAll, removeAllConstraints);
	}
}
