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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;

import org.mifmi.commons4j.swing.AdjustableImageIcon;
import org.mifmi.commons4j.swing.ObjectTableModel;
import org.mifmi.commons4j.swing.TableUtilz;

import net.mozq.picto.App;
import net.mozq.picto.core.ProcessCondition;
import net.mozq.picto.core.ProcessCore;
import net.mozq.picto.core.ProcessData;
import net.mozq.picto.core.ProcessStatus;
import net.mozq.picto.core.exception.PictoException;
import net.mozq.picto.enums.ExistingFileOption;
import net.mozq.picto.enums.ProcessDataStatus;

import java.awt.FlowLayout;

public class ProcessDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final ImageIcon ICON_IGNORED = loadImageIcon("net/mozq/picto/resources/icons/picto_icon_ignored.png", ProcessDataStatus.Ignored.toString());
	private static final ImageIcon ICON_PROCESSIG = loadImageIcon("net/mozq/picto/resources/icons/picto_icon_processing.png", ProcessDataStatus.Processing.toString());
	private static final ImageIcon ICON_SKIPPED = loadImageIcon("net/mozq/picto/resources/icons/picto_icon_skipped.png", ProcessDataStatus.Skipped.toString());
	private static final ImageIcon ICON_TERMINATED = loadImageIcon("net/mozq/picto/resources/icons/picto_icon_terminated.png", ProcessDataStatus.Terminated.toString());
	private static final ImageIcon ICON_SUCCESS = loadImageIcon("net/mozq/picto/resources/icons/picto_icon_success.png", ProcessDataStatus.Success.toString());
	private static final ImageIcon ICON_ERROR = loadImageIcon("net/mozq/picto/resources/icons/picto_icon_error.png", ProcessDataStatus.Error.toString());

	private ProcessCondition processCondition;
	private final ProcessStatus processStatus = new ProcessStatus();
	private ExistingFileOption overwriteConfirmResult = null;

	private final JDialog dialog;
	private JPanel contentPane;
	private JTable table;
	private ObjectTableModel<ProcessData> tableModel;
	private final JProgressBar progressBar;
	private final JButton btnStop;
	private JPanel pnlControls;
	private JButton btnClose;

	/**
	 * Create the dialog.
	 */
	public ProcessDialog(Window owner) {
		super(owner);

		setTitle(Messages.getString("ProcessDialog.title"));
		setBounds(100, 100, 850, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		tableModel = new ObjectTableModel<ProcessData>(
				new String[]{
						Messages.getString("ProcessDialog.table.no"),
						Messages.getString("ProcessDialog.table.status"),
						Messages.getString("ProcessDialog.table.srcPath"),
						Messages.getString("ProcessDialog.table.destPath"),
						Messages.getString("ProcessDialog.table.message")
						},
				new Class<?>[]{Integer.class, ImageIcon.class, String.class, String.class, String.class},
				new ArrayList<ProcessData>(),
				(data, rowIndex, columnIndex) -> {
					switch (columnIndex) {
					case 0: return rowIndex + 1;
					case 1:
						ProcessDataStatus status = data.getStatus();
						if (status == null) {
							return null;
						}
						switch (status) {
						case Ignored: return ICON_IGNORED;
						case Processing: return ICON_PROCESSIG;
						case Waiting: return null;
						case Skipped: return ICON_SKIPPED;
						case Terminated: return ICON_TERMINATED;
						case Success: return ICON_SUCCESS;
						case Error: return ICON_ERROR;
						default: throw new IllegalStateException();
						}
					case 2: return processCondition.getSrcRootPath().relativize(data.getSrcPath()).toString();
					case 3: return processCondition.getDestRootPath().relativize(data.getDestPath()).toString();
					case 4: return data.getMessage();
					}
					return null;
				});
		
		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent ev) {
				Point p = ev.getPoint();
				int rowIndex = rowAtPoint(p);
				int columnIndex = columnAtPoint(p);
				Object value = getValueAt(rowIndex, columnIndex);
				if (value == null) {
					value = "";
				}
				return value.toString();
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableUtilz.setColumnWidth(table, 0, 40, 40, 40);
		TableUtilz.setColumnWidth(table, 1, 40);
		TableUtilz.setColumnWidth(table, 2, 250);
		TableUtilz.setColumnWidth(table, 3, 250);
		TableUtilz.setColumnWidth(table, 4, 250);
		scrollPane.setViewportView(table);
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(0);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setString("");
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		getContentPane().add(progressBar, gbc_progressBar);
		
		pnlControls = new JPanel();
		GridBagConstraints gbc_pnlControls = new GridBagConstraints();
		gbc_pnlControls.fill = GridBagConstraints.BOTH;
		gbc_pnlControls.gridx = 0;
		gbc_pnlControls.gridy = 2;
		contentPane.add(pnlControls, gbc_pnlControls);
		pnlControls.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnStop = new JButton(Messages.getString("ProcessDialog.stop"));
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStop.setEnabled(false);
				processStatus.setStopRequests(true);
			}
		});
		pnlControls.add(btnStop);
		
		btnClose = new JButton(Messages.getString("ProcessDialog.close"));
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		btnClose.setVisible(false);
		pnlControls.add(btnClose);

		tableModel.addTableModelListener(e -> {
			int currentCount = processStatus.getCurrentProcessDataIndex() + 1;
			int totalCount = tableModel.getRowCount();
			progressBar.setString(String.format("%d / %d (%d%%)", currentCount, totalCount, (currentCount * 100 / totalCount)));
			progressBar.setValue(currentCount);
			progressBar.setMaximum(totalCount);
		});
		
		dialog = this;
	}

	public void doProcess(ProcessCondition processCondition) {
		this.processCondition = processCondition;
		this.processStatus.init();

		// Find files thread
		final Thread findFilesThread = new Thread(() -> {
			try {
				ProcessCore.findFiles(processCondition, this::addProcessData, this::isStopRequests);
				processStatus.setEndFindingFiles(true);
			} catch (Exception e) {
				String message = e.getLocalizedMessage();
				if (!(e instanceof PictoException)) {
					message = Messages.getString("message.error.find.files", message);
				}
				
				JOptionPane.showMessageDialog(dialog, message, null, JOptionPane.ERROR_MESSAGE);
				
				App.handleError(e.getMessage(), e);
			}
		});
		findFilesThread.start();

		// Process files thread
		final Thread processFilesThread = new Thread(() -> {
			try {
				ProcessCore.processFiles(processCondition, this::getProcessData, this::updateProcessData, this::confirmOverwrite, this::isProcessCompleted);
				btnStop.setVisible(false);
				btnClose.setVisible(true);
				progressBar.setForeground(Color.LIGHT_GRAY);
				progressBar.setUI(new BasicProgressBarUI());
			} catch (Exception e) {
				String message = e.getLocalizedMessage();
				if (!(e instanceof PictoException)) {
					message = Messages.getString("message.error.process.files", message);
				}
				
				JOptionPane.showMessageDialog(dialog, message, null, JOptionPane.ERROR_MESSAGE);
				
				App.handleError(e.getMessage(), e);
			}
		});
		processFilesThread.start();
	}

	public void addProcessData(ProcessData processData) {
		tableModel.addRow(processData);
	}

	public ProcessData getProcessData(int index) {
		if (tableModel.getRowCount() <= index) {
			return null;
		}
		return tableModel.getRow(index);
	}

	public void updateProcessData(int index) {
		tableModel.updateRow(index);
		processStatus.setCurrentProcessDataIndex(index);
	}
	
	public ProcessDataStatus confirmOverwrite(ProcessData processData) {
		ExistingFileOption confirmResult;
		if (overwriteConfirmResult != null) {
			confirmResult = overwriteConfirmResult;
		} else {
			ExistingFileOption[] options = new ExistingFileOption[]{
					ExistingFileOption.Yes,
					ExistingFileOption.No,
					ExistingFileOption.YesToAll,
					ExistingFileOption.NoToAll,
					ExistingFileOption.Cancel,
			};
			
			int ret = JOptionPane.showOptionDialog(
					dialog,
					Messages.getString("message.confirm.file.exists", processData.getDestPath()),
					null,
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					ExistingFileOption.No
					);
			confirmResult = options[ret];
		}
		
		switch (confirmResult) {
		case Yes:
			return ProcessDataStatus.Processing;
		case No:
			return ProcessDataStatus.Skipped;
		case YesToAll:
			overwriteConfirmResult = ExistingFileOption.YesToAll;
			return ProcessDataStatus.Processing;
		case NoToAll:
			overwriteConfirmResult = ExistingFileOption.NoToAll;
			return ProcessDataStatus.Skipped;
		case Cancel: // FALLTHRU
		default:
			return ProcessDataStatus.Terminated;
		}
	}
	
	public boolean isStopRequests() {
		return processStatus.isStopRequests();
	}
	
	public boolean isProcessCompleted() {
		if (isStopRequests()) {
			return true;
		}
		if (processStatus.isEndFindingFiles()) {
			if (tableModel.getRowCount() - 1 <= processStatus.getCurrentProcessDataIndex()) {
				return true;
			}
		}
		return false;
	}
	
	private static ImageIcon loadImageIcon(String filename, String description) {
		URL imageUrl = ProcessDialog.class.getClassLoader().getResource(filename);

		if (imageUrl != null) {
			return new AdjustableImageIcon(imageUrl, description);
		}
		
		return new AdjustableImageIcon(filename, description);
	}
}
