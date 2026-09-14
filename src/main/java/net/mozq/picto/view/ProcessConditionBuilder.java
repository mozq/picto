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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.Map;
import java.util.TimeZone;

import net.mozq.nanotemplate.NanoTemplate;
import net.mozq.nanotemplate.NanoTemplateException;
import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.core.ProcessCondition;
import net.mozq.picto.core.Range;
import net.mozq.picto.enums.FileSizeUnit;
import net.mozq.picto.enums.OperationType;

/**
 * Turns a screen-shaped {@link ProcessConditionInput} into a core {@link ProcessCondition}, or a
 * validation {@link Result} if it can't. The parsing this needs (folder paths, the file-name pattern, the
 * destination template, size/date ranges) either succeeds and becomes part of the built condition, or
 * fails and becomes the reported result - one pass instead of a separate pre-check and a separate real
 * build that could drift apart.
 */
final class ProcessConditionBuilder {
	private static final String EMPTY_DEST_SUB_FILE_PATH_PATTERN = "${SubFilePath}";

	enum Field {
		NONE,
		SOURCE_FOLDER,
		DESTINATION_FOLDER,
		FILE_NAME_PATTERN,
		DESTINATION_SUBFOLDER
	}

	record Result(String message, Field field) {
	}

	record BuildResult(ProcessCondition processCondition, Result invalidResult) {
		boolean isValid() {
			return invalidResult == null;
		}
	}

	private ProcessConditionBuilder() {
	}

	static Result validateSourceFolder(String pathText, String label) {
		return validateFolder(pathText, label, Field.SOURCE_FOLDER);
	}

	static Result validateDestinationFolder(String pathText, String label) {
		return validateFolder(pathText, label, Field.DESTINATION_FOLDER);
	}

	static BuildResult build(ProcessConditionInput input, TimeZone timeZone) {
		Result result = validateSourceFolder(input.srcFolder, Messages.getString("MainFrame.src.folder"));
		if (result != null) {
			return new BuildResult(null, result);
		}
		Path srcFolder = Paths.get(input.srcFolder).normalize();

		if (input.operationType != OperationType.Overwrite) {
			result = validateDestinationFolder(input.destFolder, Messages.getString("MainFrame.dest.folder"));
			if (result != null) {
				return new BuildResult(null, result);
			}
		}
		Path destFolder = Paths.get(input.destFolder).normalize();

		result = validateFolderRelationship(input.operationType, srcFolder, destFolder);
		if (result != null) {
			return new BuildResult(null, result);
		}

		result = validateDestSubPathPattern(input.destSubFilePathPattern);
		if (result != null) {
			return new BuildResult(null, result);
		}

		PictoPathFilter pathFilter;
		try {
			pathFilter = buildPathFilter(input, srcFolder, timeZone);
		} catch (Exception e) {
			return new BuildResult(null, new Result(Messages.getString("message.warn.invalid.fileNamePattern", e.getLocalizedMessage()), Field.FILE_NAME_PATTERN));
		}

		result = validateRanges(pathFilter);
		if (result != null) {
			return new BuildResult(null, result);
		}

		String destSubFilePathPattern = input.destSubFilePathPattern.isBlank()
				? EMPTY_DEST_SUB_FILE_PATH_PATTERN
				: input.destSubFilePathPattern;
		NanoTemplate destSubFilePathTemplate = new NanoTemplate(destSubFilePathPattern).timeZone(timeZone);

		ProcessCondition processCondition = new ProcessCondition();
		processCondition.setTimeZone(timeZone);
		processCondition.setSrcFolder(srcFolder);
		processCondition.setDestFolder(destFolder);
		processCondition.setDepth(input.includeSubfolders ? Integer.MAX_VALUE : 1);
		processCondition.setPathFilter(pathFilter);
		processCondition.setFollowLinks(input.followLinks);
		processCondition.setDestSubFilePathTemplate(destSubFilePathTemplate);
		processCondition.setOperationType(input.operationType);
		processCondition.setExistingFileMethod(input.existingFileMethod);
		processCondition.setCheckFileDigest(input.checkFileDigest);
		processCondition.setChangeFileCreationDate(input.changeFileCreationDate);
		processCondition.setChangeFileModifiedDate(input.changeFileModifiedDate);
		processCondition.setChangeFileAccessDate(input.changeFileAccessDate);
		processCondition.setChangeFileExifDate(input.changeFileExifDate);
		processCondition.setBaseDateType(input.baseDateType);
		processCondition.setCustomBaseDate(parseDate(input.customBaseDate, timeZone, 1, 1, 0, 0, 0, 0));
		processCondition.setAdjustmentType(input.adjustmentType);
		processCondition.setAdjustmentYears(parseInteger(input.adjustmentYears));
		processCondition.setAdjustmentMonths(parseInteger(input.adjustmentMonths));
		processCondition.setAdjustmentDays(parseInteger(input.adjustmentDays));
		processCondition.setAdjustmentHours(parseInteger(input.adjustmentHours));
		processCondition.setAdjustmentMinutes(parseInteger(input.adjustmentMinutes));
		processCondition.setAdjustmentSeconds(parseInteger(input.adjustmentSeconds));
		processCondition.setRemoveExifGps(input.removeExifGps);
		processCondition.setRemoveExifAll(input.removeExifAll);

		return new BuildResult(processCondition, null);
	}

	/**
	 * Builds just the {@link PictoPathFilter} for {@code input} against the already-resolved
	 * {@code srcFolder}, for callers (the live match-count preview) that only need it and not a full
	 * {@link ProcessCondition}. Throws if the file-name pattern is invalid, same as {@link #build} does
	 * internally - callers that don't want that surfaced as a validation {@link Result} handle it themselves.
	 */
	static PictoPathFilter buildPathFilter(ProcessConditionInput input, Path srcFolder, TimeZone timeZone) {
		PictoPathFilter pathFilter = new PictoPathFilter();
		pathFilter.setPathPattern(input.srcFileNamePattern, srcFolder, input.srcFileNamePatternSyntax);
		pathFilter.setIncludeHiddenFiles(input.includeHiddenFiles);
		pathFilter.setFileSizeRange(
				convertSize(parseLong(input.fileSizeFrom), input.fileSizeUnit),
				convertSize(parseLong(input.fileSizeTo), input.fileSizeUnit));
		pathFilter.setCreatedRange(
				parseDate(input.createdFrom, timeZone, 1, 1, 0, 0, 0, 0),
				parseDate(input.createdTo, timeZone, 12, 31, 23, 59, 59, 999));
		pathFilter.setModifiedRange(
				parseDate(input.modifiedFrom, timeZone, 1, 1, 0, 0, 0, 0),
				parseDate(input.modifiedTo, timeZone, 12, 31, 23, 59, 59, 999));
		return pathFilter;
	}

	private static Result validateFolder(String pathText, String label, Field field) {
		if (pathText.isBlank()) {
			return new Result(Messages.getString("message.warn.folder.empty", label), field);
		}
		Path path;
		try {
			path = Paths.get(pathText).normalize();
		} catch (InvalidPathException e) {
			return new Result(Messages.getString("message.warn.folder.invalid", label, e.getLocalizedMessage()), field);
		}
		if (!Files.isDirectory(path)) {
			return new Result(Messages.getString("message.warn.folder.not.directory", label, path), field);
		}
		return null;
	}

	private static Result validateFolderRelationship(OperationType operationType, Path srcFolder, Path destFolder) {
		if (operationType == OperationType.Overwrite) {
			return null;
		}
		Path realSrcFolder = realPath(srcFolder);
		Path realDestFolder = realPath(destFolder);
		if (realSrcFolder.equals(realDestFolder)) {
			return new Result(Messages.getString("message.warn.folder.same"), Field.NONE);
		}
		if (realDestFolder.startsWith(realSrcFolder)) {
			return new Result(Messages.getString("message.warn.folder.dest.under.src"), Field.DESTINATION_FOLDER);
		}
		if (realSrcFolder.startsWith(realDestFolder)) {
			return new Result(Messages.getString("message.warn.folder.src.under.dest"), Field.SOURCE_FOLDER);
		}
		return null;
	}

	private static Result validateDestSubPathPattern(String destSubFilePathPattern) {
		try {
			new NanoTemplate(destSubFilePathPattern).render(Map.of());
			return null;
		} catch (NanoTemplateException _) {
			return new Result(Messages.getString("message.warn.invalid.destSubFilePath.pattern"), Field.DESTINATION_SUBFOLDER);
		}
	}

	private static Result validateRanges(PictoPathFilter pathFilter) {
		Result result = validateRange(pathFilter.getFileSizeRange(), "message.warn.fileSize.is.invalid.range");
		if (result != null) {
			return result;
		}
		result = validateRange(pathFilter.getCreatedRange(), "message.warn.created.is.invalid.range");
		if (result != null) {
			return result;
		}
		return validateRange(pathFilter.getModifiedRange(), "message.warn.modified.is.invalid.range");
	}

	private static <T extends Comparable<? super T>> Result validateRange(Range<T> range, String messageKey) {
		if (range == null || range.from() == null || range.to() == null) {
			return null;
		}
		if (range.from().compareTo(range.to()) > 0) {
			return new Result(Messages.getString(messageKey), Field.NONE);
		}
		return null;
	}

	private static Path realPath(Path path) {
		try {
			return path.toRealPath();
		} catch (IOException _) {
			return path.toAbsolutePath().normalize();
		}
	}

	private static java.util.Date parseDate(
			String text, TimeZone timeZone, int defaultMonth, int defaultDay, int defaultHour, int defaultMin, int defaultSec, int defaultMsec) {
		return DateTimeText.parseDate(text, timeZone, Year.now().getValue(), defaultMonth, defaultDay, defaultHour, defaultMin, defaultSec, defaultMsec);
	}

	private static Long convertSize(Long size, FileSizeUnit unit) {
		if (size == null) {
			return null;
		}
		return size * unit.getUnitBytes();
	}

	private static Integer parseInteger(String numberText) {
		if (numberText == null || numberText.isEmpty()) {
			return null;
		}
		try {
			return Integer.valueOf(numberText);
		} catch (NumberFormatException _) {
			return null;
		}
	}

	private static Long parseLong(String numberText) {
		if (numberText == null || numberText.isEmpty()) {
			return null;
		}
		try {
			return Long.valueOf(numberText);
		} catch (NumberFormatException _) {
			return null;
		}
	}
}
