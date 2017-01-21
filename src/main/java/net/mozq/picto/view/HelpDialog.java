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
import java.io.InputStreamReader;

import javax.swing.JDialog;
import javax.swing.JEditorPane;

import net.mozq.picto.App;
import javax.swing.JScrollPane;

public class HelpDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	public HelpDialog() {
		setTitle(Messages.getString("HelpDialog.title")); //$NON-NLS-1$
		setBounds(100, 100, 700, 530);
		
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setContentType("text/html"); //$NON-NLS-1$
		editorPane.setText(readResourceText(Messages.getString("HelpDialog.help.path"))); //$NON-NLS-1$
		editorPane.setCaretPosition(0);
		
		JScrollPane scrollPane = new JScrollPane(editorPane);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	private String readResourceText(String resourceName) {
		StringBuilder sb = new StringBuilder();
		try (InputStreamReader br = new InputStreamReader(this.getClass().getResourceAsStream(resourceName), "UTF-8")) { //$NON-NLS-1$
			int ch;
			while ((ch = br.read()) != -1) {
				sb.append((char)ch);
			}
		} catch (Exception e) {
			App.handleError(e.getMessage(), e);
			
			return Messages.getString("message.error.load.help"); //$NON-NLS-1$
		}
		return sb.toString();
	}
}
