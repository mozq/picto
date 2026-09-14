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

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.OperationType;

/** Renders a {@link ProcessConditionInput} snapshot into the short human-readable summary text shown for
 * the run button, and each collapsed options/changes section, in {@link MainFrame}. */
final class ConditionSummaryFormatter {
	private static final String CHECKED_ITEM_PREFIX = "✓ ";

	private ConditionSummaryFormatter() {
	}

	static String runSummary(ProcessConditionInput input) {
		String summary = input.operationType + ": "
				+ Messages.getString("MainFrame.src.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(input.srcFolder, Messages.getString("MainFrame.src.folder"));
		if (input.operationType == OperationType.Overwrite) {
			return summary;
		}
		return summary + " -> "
				+ Messages.getString("MainFrame.dest.conditionsTitle") + " "
				+ PathTextSupport.shortPathText(input.destFolder, Messages.getString("MainFrame.dest.folder"));
	}

	static String sourceOptionsSummary(ProcessConditionInput input) {
		List<String> items = new ArrayList<>();
		if (!input.srcFileNamePattern.isEmpty()) {
			String pattern = input.srcFileNamePattern;
			if (input.srcFileNamePatternSyntax == FilePatternSyntax.Regex) {
				pattern += " (" + FilePatternSyntax.Regex + ")";
			}
			items.add(summaryItem(Messages.getString("MainFrame.src.fileNamePattern"), pattern));
		}
		if (input.includeSubfolders) {
			items.add(checkedItem(Messages.getString("MainFrame.src.includeSubfolders")));
		}
		if (input.includeHiddenFiles) {
			items.add(checkedItem(Messages.getString("MainFrame.src.includeHiddenFiles")));
		}
		if (!input.fileSizeFrom.isEmpty() || !input.fileSizeTo.isEmpty()) {
			String range = rangeText(input.fileSizeFrom, input.fileSizeTo);
			items.add(summaryItem(Messages.getString("MainFrame.src.fileSize"), range + " " + input.fileSizeUnit));
		}
		String createdFrom = dateFieldText(input.createdFrom);
		String createdTo = dateFieldText(input.createdTo);
		if (!createdFrom.isEmpty() || !createdTo.isEmpty()) {
			items.add(summaryItem(Messages.getString("MainFrame.src.created"), rangeText(createdFrom, createdTo)));
		}
		String modifiedFrom = dateFieldText(input.modifiedFrom);
		String modifiedTo = dateFieldText(input.modifiedTo);
		if (!modifiedFrom.isEmpty() || !modifiedTo.isEmpty()) {
			items.add(summaryItem(Messages.getString("MainFrame.src.modified"), rangeText(modifiedFrom, modifiedTo)));
		}
		return joinOptionsSummary(items);
	}

	static String destinationOptionsSummary(ProcessConditionInput input) {
		List<String> items = new ArrayList<>();
		if (!input.destSubFilePathPattern.isBlank() && !MainFrameSettings.DEFAULT_DEST_SUB_FILE_PATH_PATTERN.equals(input.destSubFilePathPattern)) {
			items.add(summaryItem(Messages.getString("MainFrame.dest.subFilePathPattern"), input.destSubFilePathPattern));
		}
		if (input.existingFileMethod != ExistingFileMethod.Confirm) {
			items.add(summaryItem(Messages.getString("MainFrame.dest.existingFileMethod"), String.valueOf(input.existingFileMethod)));
		}
		if (input.checkFileDigest) {
			items.add(checkedItem(Messages.getString("MainFrame.dest.validateFile")));
		}
		return joinOptionsSummary(items);
	}

	static String changesSummary(ProcessConditionInput input) {
		List<String> items = new ArrayList<>();
		boolean changesFileDate = false;
		if (input.changeFileCreationDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.creationDate")));
			changesFileDate = true;
		}
		if (input.changeFileModifiedDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.modifiedDate")));
			changesFileDate = true;
		}
		if (input.changeFileAccessDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.accessDate")));
			changesFileDate = true;
		}
		if (input.changeFileExifDate) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.filedate.exifDate")));
			changesFileDate = true;
		}
		if (changesFileDate) {
			items.add(summaryItem(Messages.getString("MainFrame.changes.filedate.baseDateType"), baseDateTypeSummary(input)));
		}
		if (changesFileDate && input.adjustmentType != DateModType.None) {
			items.add(summaryItem(Messages.getString("MainFrame.changes.filedate.adjustment"), adjustmentSummary(input)));
		}
		if (input.removeExifGps) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.exif.removeGps")));
		}
		if (input.removeExifAll) {
			items.add(checkedItem(Messages.getString("MainFrame.changes.exif.removeAll")));
		}
		return joinOptionsSummary(items);
	}

	private static String baseDateTypeSummary(ProcessConditionInput input) {
		String value = String.valueOf(input.baseDateType);
		String customDate = dateFieldText(input.customBaseDate);
		if (input.baseDateType == DateType.CustomDate && !customDate.isEmpty()) {
			value += " " + customDate;
		}
		return value;
	}

	private static String adjustmentSummary(ProcessConditionInput input) {
		String value = String.valueOf(input.adjustmentType);
		List<String> amounts = new ArrayList<>();
		addAdjustmentAmount(amounts, input.adjustmentYears, "Y");
		addAdjustmentAmount(amounts, input.adjustmentMonths, "M");
		addAdjustmentAmount(amounts, input.adjustmentDays, "D");
		addAdjustmentAmount(amounts, input.adjustmentHours, "h");
		addAdjustmentAmount(amounts, input.adjustmentMinutes, "m");
		addAdjustmentAmount(amounts, input.adjustmentSeconds, "s");
		if (!amounts.isEmpty()) {
			value += " " + String.join(" ", amounts);
		}
		return value;
	}

	private static void addAdjustmentAmount(List<String> amounts, String text, String suffix) {
		if (text == null || text.isEmpty()) {
			return;
		}
		try {
			amounts.add(Integer.parseInt(text) + suffix);
		} catch (NumberFormatException e) {
			amounts.add(text + suffix);
		}
	}

	private static String dateFieldText(String text) {
		return DateTimeText.compact(text);
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
