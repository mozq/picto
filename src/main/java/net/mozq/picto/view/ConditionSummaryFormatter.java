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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.OperationType;

/** Renders a {@link ProcessConditionValues} snapshot into the short human-readable summary text shown for
 * the run button, and each collapsed options/changes section, in {@link MainFrame}. */
final class ConditionSummaryFormatter {
	private static final String CHECKED_ITEM_PREFIX = "✓ ";

	private final JTextField txtSrcFolder;
	private final JTextField txtDestFolder;
	private final SourceOptionsPanel srcOpt;
	private final ChangesPanel changes;

	ConditionSummaryFormatter(JTextField txtSrcFolder, JTextField txtDestFolder, SourceOptionsPanel srcOpt, ChangesPanel changes) {
		this.txtSrcFolder = txtSrcFolder;
		this.txtDestFolder = txtDestFolder;
		this.srcOpt = srcOpt;
		this.changes = changes;
	}

	String runSummary(ProcessConditionValues values) {
		String summary = values.operationType + ": "
				+ Messages.getString("MainFrame.src.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(MainFrame.fieldText(txtSrcFolder), Messages.getString("MainFrame.src.folder"));
		if (values.operationType == OperationType.Overwrite) {
			return summary;
		}
		return summary + " -> "
				+ Messages.getString("MainFrame.dest.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(MainFrame.fieldText(txtDestFolder), Messages.getString("MainFrame.dest.folder"));
	}

	String sourceOptionsSummary(ProcessConditionValues values) {
		List<String> items = new ArrayList<>();
		if (!values.srcFileNamePattern.isEmpty()) {
			String pattern = values.srcFileNamePattern;
			if (values.srcFileNamePatternSyntax == FilePatternSyntax.Regex) {
				pattern += " (" + FilePatternSyntax.Regex + ")";
			}
			items.add(summaryItem(Messages.getString("MainFrame.src.fileNamePattern"), pattern));
		}
		if (values.depth != 1) {
			items.add(checkedItem(Messages.getString("MainFrame.src.includeSubfolders")));
		}
		if (values.includeHiddenFiles) {
			items.add(checkedItem(Messages.getString("MainFrame.src.includeHiddenFiles")));
		}
		if (values.fileSizeFrom != null || values.fileSizeTo != null) {
			String range = rangeText(MainFrame.fieldText(srcOpt.txtFileSizeFrom), MainFrame.fieldText(srcOpt.txtFileSizeTo));
			items.add(summaryItem(Messages.getString("MainFrame.src.fileSize"), range + " " + srcOpt.cmbFileSizeUnit.getSelectedItem()));
		}
		if (values.createdFrom != null || values.createdTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.src.created"),
					rangeText(dateFieldText(srcOpt.txtCreatedFrom), dateFieldText(srcOpt.txtCreatedTo))));
		}
		if (values.modifiedFrom != null || values.modifiedTo != null) {
			items.add(summaryItem(Messages.getString("MainFrame.src.modified"),
					rangeText(dateFieldText(srcOpt.txtModifiedFrom), dateFieldText(srcOpt.txtModifiedTo))));
		}
		return joinOptionsSummary(items);
	}

	String destinationOptionsSummary(ProcessConditionValues values) {
		List<String> items = new ArrayList<>();
		if (!values.destSubFilePathPattern.isBlank() && !MainFrameSettings.DEFAULT_DEST_SUB_FILE_PATH_PATTERN.equals(values.destSubFilePathPattern)) {
			items.add(summaryItem(Messages.getString("MainFrame.dest.subFilePathPattern"), values.destSubFilePathPattern));
		}
		if (values.existingFileMethod != ExistingFileMethod.Confirm) {
			items.add(summaryItem(Messages.getString("MainFrame.dest.existingFileMethod"), String.valueOf(values.existingFileMethod)));
		}
		if (values.checkFileDigest) {
			items.add(checkedItem(Messages.getString("MainFrame.dest.validateFile")));
		}
		return joinOptionsSummary(items);
	}

	String changesSummary(ProcessConditionValues values) {
		List<String> items = new ArrayList<>();
		boolean changesFileDate = false;
		if (values.changeFileCreationDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.creationDate")));
			changesFileDate = true;
		}
		if (values.changeFileModifiedDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.modifiedDate")));
			changesFileDate = true;
		}
		if (values.changeFileAccessDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.accessDate")));
			changesFileDate = true;
		}
		if (values.changeFileExifDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.exifDate")));
			changesFileDate = true;
		}
		if (changesFileDate) {
			items.add(summaryItem(Messages.getString("MainFrame.changes.filedate.baseDateType"), baseDateTypeSummary(values)));
		}
		if (changesFileDate && values.adjustmentType != DateModType.None) {
			items.add(summaryItem(Messages.getString("MainFrame.changes.filedate.adjustment"), adjustmentSummary(values)));
		}
		if (values.removeExifGps) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.exif.removeGps")));
		}
		if (values.removeExifAll) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.exif.removeAll")));
		}
		return joinOptionsSummary(items);
	}

	private String baseDateTypeSummary(ProcessConditionValues values) {
		String value = String.valueOf(values.baseDateType);
		String customDate = dateFieldText(changes.filedate.txtCustomBaseDate);
		if (values.baseDateType == DateType.CustomDate && values.customBaseDate != null && !customDate.isEmpty()) {
			value += " " + customDate;
		}
		return value;
	}

	private static String adjustmentSummary(ProcessConditionValues values) {
		String value = String.valueOf(values.adjustmentType);
		List<String> amounts = new ArrayList<>();
		addAdjustmentAmount(amounts, values.adjustmentYears, "Y");
		addAdjustmentAmount(amounts, values.adjustmentMonths, "M");
		addAdjustmentAmount(amounts, values.adjustmentDays, "D");
		addAdjustmentAmount(amounts, values.adjustmentHours, "h");
		addAdjustmentAmount(amounts, values.adjustmentMinutes, "m");
		addAdjustmentAmount(amounts, values.adjustmentSeconds, "s");
		if (!amounts.isEmpty()) {
			value += " " + String.join(" ", amounts);
		}
		return value;
	}

	private static void addAdjustmentAmount(List<String> amounts, Integer value, String suffix) {
		if (value != null) {
			amounts.add(value + suffix);
		}
	}

	private static String dateFieldText(JTextField field) {
		return DateTimeText.compact(field.getText());
	}

	private static String rangeText(String from, String to) {
		if (from.isEmpty()) {
			return to;
		}
		if (to.isEmpty()) {
			return from;
		}
		return from + " - " + to;
	}

	private static String summaryItem(String label, String value) {
		return label + ": " + value;
	}

	private static String checkedItem(String label) {
		return CHECKED_ITEM_PREFIX + label;
	}

	private static String joinOptionsSummary(List<String> items) {
		return String.join(" / ", items);
	}
}
