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

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import net.mozq.picto.App;
import net.mozq.picto.core.ProcessCondition;
import net.mozq.picto.core.ProcessData;
import net.mozq.picto.core.ProcessRunner;
import net.mozq.picto.core.exception.PictoException;
import net.mozq.picto.enums.ExistingFileOption;
import net.mozq.picto.enums.ProcessDataStatus;

public class ProcessDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int STATUS_ICON_SIZE = 16;

	private static final ImageIcon ICON_IGNORED = loadImageIcon("net/mozq/picto/resources/icons/icon-ignored.png", ProcessDataStatus.Ignored.toString());
	private static final ImageIcon ICON_PROCESSING = loadImageIcon("net/mozq/picto/resources/icons/icon-processing.png", ProcessDataStatus.Processing.toString());
	private static final ImageIcon ICON_SKIPPED = loadImageIcon("net/mozq/picto/resources/icons/icon-skipped.png", ProcessDataStatus.Skipped.toString());
	private static final ImageIcon ICON_TERMINATED = loadImageIcon("net/mozq/picto/resources/icons/icon-terminated.png", ProcessDataStatus.Terminated.toString());
	private static final ImageIcon ICON_SUCCESS = loadImageIcon("net/mozq/picto/resources/icons/icon-success.png", ProcessDataStatus.Success.toString());
	private static final ImageIcon ICON_ERROR = loadImageIcon("net/mozq/picto/resources/icons/icon-error.png", ProcessDataStatus.Error.toString());

	private final ConcurrentLinkedQueue<ProcessData> pendingProcessData = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean batchDispatchScheduled = new AtomicBoolean(false);

	private ProcessCondition processCondition;
	private ProcessRunner processRunner;
	private volatile int currentProcessDataIndex = -1;
	private volatile ExistingFileOption overwriteConfirmResult = null;

	private final JDialog dialog;
	private JPanel contentPane;
	private JTable table;
	private ProcessDataTableModel tableModel;
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

		tableModel = new ProcessDataTableModel(
				new String[]{
						Messages.getString("ProcessDialog.table.no"),
						Messages.getString("ProcessDialog.table.status"),
						Messages.getString("ProcessDialog.table.srcPath"),
						Messages.getString("ProcessDialog.table.destPath"),
						Messages.getString("ProcessDialog.table.message")
						});

		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			@Override
			public String getToolTipText(MouseEvent ev) {
				Point p = ev.getPoint();
				int rowIndex = rowAtPoint(p);
				int columnIndex = columnAtPoint(p);
				if (rowIndex < 0 || columnIndex < 0) {
					return null;
				}
				Object value = getValueAt(rowIndex, columnIndex);
				if (value == null) {
					value = "";
				}
				return value.toString();
			}
		};
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setColumnWidth(table, 0, 40, 40, 40);
		setColumnWidth(table, 1, 40);
		setColumnWidth(table, 2, 250);
		setColumnWidth(table, 3, 250);
		setColumnWidth(table, 4, 250);
		scrollPane.setViewportView(table);

		TableRowSorter<ProcessDataTableModel> sorter = new TableRowSorter<>(tableModel);
		sorter.setComparator(1, Comparator.comparingInt(ProcessDialog::statusSortRank));
		table.setRowSorter(sorter);

		JPopupMenu tablePopupMenu = new JPopupMenu();
		JMenuItem mntmOpenLocation = new JMenuItem(Messages.getString("ProcessDialog.table.menu.openLocation"));
		mntmOpenLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int viewRow = table.getSelectedRow();
				if (viewRow >= 0) {
					revealRow(table.convertRowIndexToModel(viewRow));
				}
			}
		});
		tablePopupMenu.add(mntmOpenLocation);
		JMenuItem mntmCopy = new JMenuItem(Messages.getString("ProcessDialog.table.menu.copy"));
		mntmCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copySelectedRowsToClipboard();
			}
		});
		tablePopupMenu.add(mntmCopy);
		table.setComponentPopupMenu(tablePopupMenu);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
					int viewRow = table.rowAtPoint(e.getPoint());
					if (viewRow >= 0) {
						revealRow(table.convertRowIndexToModel(viewRow));
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				selectRowForPopupIfNeeded(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				selectRowForPopupIfNeeded(e);
			}

			private void selectRowForPopupIfNeeded(MouseEvent e) {
				if (!e.isPopupTrigger()) {
					return;
				}
				int viewRow = table.rowAtPoint(e.getPoint());
				if (viewRow >= 0 && !table.isRowSelected(viewRow)) {
					table.setRowSelectionInterval(viewRow, viewRow);
				}
			}
		});

		KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(copyKeyStroke, "picto.copySelection");
		table.getActionMap().put("picto.copySelection", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				copySelectedRowsToClipboard();
			}
		});

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
				if (processRunner != null) {
					processRunner.stop();
				}
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

		tableModel.addTableModelListener(_ -> {
			int currentCount = currentProcessDataIndex + 1;
			int totalCount = tableModel.getRowCount();
			progressBar.setString(String.format("%d / %d (%d%%)", currentCount, totalCount, (currentCount * 100 / totalCount)));
			progressBar.setValue(currentCount);
			progressBar.setMaximum(totalCount);
		});

		dialog = this;
	}

	public void doProcess(ProcessCondition processCondition) {
		this.processCondition = processCondition;
		this.currentProcessDataIndex = -1;
		this.processRunner = new ProcessRunner(processCondition, this::confirmOverwrite, new ProcessRunner.Listener() {
			@Override
			public void processDataFound(ProcessData processData) {
				addProcessData(processData);
			}

			@Override
			public void processDataUpdated(int index) {
				updateProcessData(index);
			}

			@Override
			public void findingFailed(Exception e) {
				handleError(e, "message.error.find.files");
			}

			@Override
			public void processingFailed(Exception e) {
				handleError(e, "message.error.process.files");
			}

			@Override
			public void completed() {
				processCompleted();
			}
		});
		processRunner.start();
	}

	public void addProcessData(ProcessData processData) {
		pendingProcessData.add(processData);
		scheduleBatchDispatch();
	}

	private void scheduleBatchDispatch() {
		if (batchDispatchScheduled.compareAndSet(false, true)) {
			SwingUtilities.invokeLater(this::flushPendingProcessData);
		}
	}

	private void flushPendingProcessData() {
		batchDispatchScheduled.set(false);
		List<ProcessData> batch = new ArrayList<>();
		ProcessData data;
		while ((data = pendingProcessData.poll()) != null) {
			batch.add(data);
		}
		if (!batch.isEmpty()) {
			tableModel.addRows(batch);
		}
		if (!pendingProcessData.isEmpty()) {
			scheduleBatchDispatch();
		}
	}

	public void updateProcessData(int index) {
		currentProcessDataIndex = index;
		tableModel.updateRow(index);
	}

	public ProcessDataStatus confirmOverwrite(ProcessData processData) {
		if (!SwingUtilities.isEventDispatchThread()) {
			AtomicReference<ProcessDataStatus> result = new AtomicReference<>();
			try {
				SwingUtilities.invokeAndWait(() -> result.set(confirmOverwrite(processData)));
			} catch (InterruptedException _) {
				Thread.currentThread().interrupt();
				return ProcessDataStatus.Terminated;
			} catch (InvocationTargetException e) {
				throw new IllegalStateException(e.getCause());
			}
			return result.get();
		}

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
			confirmResult = ret >= 0 ? options[ret] : ExistingFileOption.Cancel;
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

	private void showErrorMessage(String message) {
		runOnEventDispatchThread(() -> JOptionPane.showMessageDialog(dialog, message, null, JOptionPane.ERROR_MESSAGE));
	}

	private void handleError(Exception e, String fallbackMessageKey) {
		String message = e.getLocalizedMessage();
		if (!(e instanceof PictoException)) {
			message = Messages.getString(fallbackMessageKey, message);
		}

		showErrorMessage(message);
		App.handleError(e.getMessage(), e);
	}

	private void processCompleted() {
		runOnEventDispatchThread(() -> {
			flushPendingProcessData();
			btnStop.setVisible(false);
			btnClose.setVisible(true);
		});
	}

	private static void runOnEventDispatchThread(Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			SwingUtilities.invokeLater(runnable);
		}
	}

	private static ImageIcon loadImageIcon(String filename, String description) {
		URL imageUrl = ProcessDialog.class.getClassLoader().getResource(filename);

		if (imageUrl != null) {
			return createScaledImageIcon(new ImageIcon(imageUrl, description));
		}

		return createScaledImageIcon(new ImageIcon(filename, description));
	}

	private static ImageIcon createScaledImageIcon(ImageIcon icon) {
		Image image = icon.getImage().getScaledInstance(STATUS_ICON_SIZE, STATUS_ICON_SIZE, Image.SCALE_SMOOTH);
		return new ImageIcon(image, icon.getDescription());
	}

	private static void setColumnWidth(JTable table, int columnIndex, int width) {
		TableColumn column = table.getColumnModel().getColumn(columnIndex);
		column.setPreferredWidth(width);
	}

	private static void setColumnWidth(JTable table, int columnIndex, int minWidth, int preferredWidth, int maxWidth) {
		TableColumn column = table.getColumnModel().getColumn(columnIndex);
		column.setMinWidth(minWidth);
		column.setPreferredWidth(preferredWidth);
		column.setMaxWidth(maxWidth);
	}

	private static int statusSortRank(ImageIcon icon) {
		if (icon == null) {
			return 0;
		} else if (icon == ICON_PROCESSING) {
			return 1;
		} else if (icon == ICON_SKIPPED) {
			return 2;
		} else if (icon == ICON_IGNORED) {
			return 3;
		} else if (icon == ICON_TERMINATED) {
			return 4;
		} else if (icon == ICON_SUCCESS) {
			return 5;
		} else if (icon == ICON_ERROR) {
			return 6;
		} else {
			return -1;
		}
	}

	private void revealRow(int modelRow) {
		ProcessData data = tableModel.getProcessDataAt(modelRow);
		Path path = resolveExistingPath(data);
		if (path != null) {
			openInFileManager(path);
		}
	}

	private static Path resolveExistingPath(ProcessData data) {
		Path destPath = data.getDestPath();
		if (destPath != null && Files.exists(destPath)) {
			return destPath;
		}
		Path srcPath = data.getSrcPath();
		if (srcPath != null && Files.exists(srcPath)) {
			return srcPath;
		}
		return null;
	}

	private static void openInFileManager(Path path) {
		if (!Desktop.isDesktopSupported()) {
			return;
		}
		Desktop desktop = Desktop.getDesktop();
		try {
			if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
				desktop.browseFileDirectory(path.toFile());
			} else if (desktop.isSupported(Desktop.Action.OPEN)) {
				File folder = Files.isDirectory(path) ? path.toFile() : path.getParent().toFile();
				desktop.open(folder);
			}
		} catch (IOException _) {
			// Best effort only; ignore failures opening the file manager.
		}
	}

	private void copySelectedRowsToClipboard() {
		int[] viewRows = table.getSelectedRows();
		if (viewRows.length == 0) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		for (int col = 0; col < tableModel.getColumnCount(); col++) {
			if (col > 0) {
				sb.append('\t');
			}
			sb.append(tableModel.getColumnName(col));
		}
		sb.append('\n');

		for (int viewRow : viewRows) {
			int modelRow = table.convertRowIndexToModel(viewRow);
			for (int col = 0; col < tableModel.getColumnCount(); col++) {
				if (col > 0) {
					sb.append('\t');
				}
				sb.append(sanitizeForClipboard(cellText(modelRow, col)));
			}
			sb.append('\n');
		}

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
	}

	private String cellText(int modelRow, int columnIndex) {
		if (columnIndex == 1) {
			ProcessDataStatus status = tableModel.getProcessDataAt(modelRow).getStatus();
			return status != null ? status.toString() : "";
		}
		Object value = tableModel.getValueAt(modelRow, columnIndex);
		return value != null ? value.toString() : "";
	}

	private static String sanitizeForClipboard(String text) {
		return text.replace('\t', ' ').replace('\r', ' ').replace('\n', ' ');
	}

	private class ProcessDataTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private final String[] columnNames;
		private final Class<?>[] columnClasses = new Class<?>[]{
				Integer.class,
				ImageIcon.class,
				String.class,
				String.class,
				String.class
		};
		private final ArrayList<ProcessData> rows = new ArrayList<>();

		ProcessDataTableModel(String[] columnNames) {
			this.columnNames = columnNames;
		}

		@Override
		public int getRowCount() {
			return rows.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return columnNames[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return columnClasses[columnIndex];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			ProcessData data = rows.get(rowIndex);
			switch (columnIndex) {
			case 0: return rowIndex + 1;
			case 1: return getStatusIcon(data.getStatus());
			case 2:
				String srcRel = data.getSrcRelativePath();
				return srcRel != null ? srcRel : processCondition.getSrcRootPath().relativize(data.getSrcPath()).toString();
			case 3:
				String destRel = data.getDestRelativePath();
				return destRel != null ? destRel : processCondition.getDestRootPath().relativize(data.getDestPath()).toString();
			case 4: return data.getMessage();
			default: throw new IllegalArgumentException(Integer.toString(columnIndex));
			}
		}

		void addRows(List<ProcessData> batch) {
			if (batch.isEmpty()) {
				return;
			}
			int start = rows.size();
			rows.addAll(batch);
			fireTableRowsInserted(start, rows.size() - 1);
		}

		void updateRow(int rowIndex) {
			runOnEventDispatchThread(() -> fireTableRowsUpdated(rowIndex, rowIndex));
		}

		ProcessData getProcessDataAt(int rowIndex) {
			return rows.get(rowIndex);
		}

		private ImageIcon getStatusIcon(ProcessDataStatus status) {
			if (status == null) {
				return null;
			}
			switch (status) {
			case Ignored: return ICON_IGNORED;
			case Processing: return ICON_PROCESSING;
			case Waiting: return null;
			case Skipped: return ICON_SKIPPED;
			case Terminated: return ICON_TERMINATED;
			case Success: return ICON_SUCCESS;
			case Error: return ICON_ERROR;
			default: throw new IllegalStateException(status.toString());
			}
		}
	}
}
