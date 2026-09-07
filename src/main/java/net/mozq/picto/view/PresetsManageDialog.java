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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import net.mozq.picto.App;
import net.mozq.picto.view.MainFrame.PresetEntry;

public class PresetsManageDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final MainFrame frame;
	private final DefaultListModel<PresetEntry> listModel = new DefaultListModel<>();
	private final JList<PresetEntry> list = new JList<>(listModel);
	private final JButton btnRename;
	private final JButton btnDelete;

	public PresetsManageDialog(MainFrame frame) {
		this.frame = frame;

		setTitle(Messages.getString("PresetsManageDialog.title"));
		setBounds(100, 100, 360, 400);

		JPanel contentPane = new JPanel(new BorderLayout(0, 8));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(list);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

		btnRename = new JButton(Messages.getString("PresetsManageDialog.rename"));
		btnRename.setMnemonic(KeyEvent.VK_R);
		btnRename.setEnabled(false);
		btnRename.addActionListener(_ -> renameSelected());
		buttonPanel.add(btnRename);

		btnDelete = new JButton(Messages.getString("PresetsManageDialog.delete"));
		btnDelete.setMnemonic(KeyEvent.VK_D);
		btnDelete.setEnabled(false);
		btnDelete.addActionListener(_ -> deleteSelected());
		buttonPanel.add(btnDelete);

		JButton btnClose = new JButton(Messages.getString("PresetsManageDialog.close"));
		btnClose.setMnemonic(KeyEvent.VK_C);
		btnClose.addActionListener(_ -> dispose());
		buttonPanel.add(btnClose);

		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		list.addListSelectionListener(_ -> {
			boolean selected = list.getSelectedValue() != null;
			btnRename.setEnabled(selected);
			btnDelete.setEnabled(selected);
		});

		refreshList();
	}

	private void refreshList() {
		listModel.clear();
		for (PresetEntry preset : frame.listPresets()) {
			listModel.addElement(preset);
		}
	}

	private void renameSelected() {
		PresetEntry selected = list.getSelectedValue();
		if (selected == null) {
			return;
		}

		Object input = JOptionPane.showInputDialog(
				this,
				Messages.getString("message.prompt.preset.name"),
				Messages.getString("PresetsManageDialog.rename"),
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				selected.name()
				);
		if (input == null) {
			return;
		}

		String newName = input.toString().trim();
		if (newName.isEmpty()) {
			JOptionPane.showMessageDialog(this, Messages.getString("message.warn.preset.name.empty"), null, JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			frame.renamePreset(selected.fileName(), newName);
			refreshList();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					this,
					Messages.getString("message.error.preset.rename", e.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);
			App.handleError(e.getMessage(), e);
		}
	}

	private void deleteSelected() {
		PresetEntry selected = list.getSelectedValue();
		if (selected == null) {
			return;
		}

		int result = JOptionPane.showConfirmDialog(
				this,
				Messages.getString("message.confirm.preset.delete", selected.name()),
				null,
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
				);
		if (result != JOptionPane.YES_OPTION) {
			return;
		}

		try {
			frame.deletePreset(selected.fileName());
			refreshList();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(
					this,
					Messages.getString("message.error.preset.delete", e.getLocalizedMessage()),
					null,
					JOptionPane.ERROR_MESSAGE
					);
			App.handleError(e.getMessage(), e);
		}
	}
}
