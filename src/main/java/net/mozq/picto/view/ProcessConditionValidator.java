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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import net.mozq.nanotemplate.NanoTemplate;
import net.mozq.nanotemplate.NanoTemplateException;
import net.mozq.picto.enums.OperationType;

final class ProcessConditionValidator {
	enum Field {
		NONE,
		SOURCE_FOLDER,
		DESTINATION_FOLDER,
		FILE_PATTERN,
		DESTINATION_SUBFOLDER
	}

	record Result(String message, Field field) {
	}

	private ProcessConditionValidator() {
	}

	static Result validateSourceFolder(String pathText, String label) {
		return validateFolder(pathText, label, Field.SOURCE_FOLDER);
	}

	static Result validateDestinationFolder(String pathText, String label) {
		return validateFolder(pathText, label, Field.DESTINATION_FOLDER);
	}

	static Result validate(ProcessConditionValues values) {
		Result result = validateFolderRelationship(values);
		if (result != null) {
			return result;
		}
		result = validateDestSubPathPattern(values);
		if (result != null) {
			return result;
		}
		result = validateFilePattern(values);
		if (result != null) {
			return result;
		}
		result = validateRanges(values);
		if (result != null) {
			return result;
		}
		return null;
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

	private static Result validateFolderRelationship(ProcessConditionValues values) {
		if (values.operationType == OperationType.Overwrite) {
			return null;
		}
		Path srcRootDirPath = realPath(values.srcRootDirPath);
		Path destRootDirPath = realPath(values.destRootDirPath);
		if (srcRootDirPath.equals(destRootDirPath)) {
			return new Result(Messages.getString("message.warn.folder.same"), Field.NONE);
		}
		if (destRootDirPath.startsWith(srcRootDirPath)) {
			return new Result(Messages.getString("message.warn.folder.dest.under.src"), Field.DESTINATION_FOLDER);
		}
		if (srcRootDirPath.startsWith(destRootDirPath)) {
			return new Result(Messages.getString("message.warn.folder.src.under.dest"), Field.SOURCE_FOLDER);
		}
		return null;
	}

	private static Result validateDestSubPathPattern(ProcessConditionValues values) {
		try {
			new NanoTemplate(values.destSubPathPattern).render(Map.of());
			return null;
		} catch (NanoTemplateException _) {
			return new Result(Messages.getString("message.warn.invalid.destSubPath.pattern"), Field.DESTINATION_SUBFOLDER);
		}
	}

	private static Result validateFilePattern(ProcessConditionValues values) {
		try {
			FileSystems.getDefault().getPathMatcher(((values.filePatternRegex) ? "regex:" : "glob:") + values.filePattern);
			return null;
		} catch (Exception e) {
			return new Result(Messages.getString("message.warn.invalid.filePattern", e.getLocalizedMessage()), Field.FILE_PATTERN);
		}
	}

	private static Result validateRanges(ProcessConditionValues values) {
		if (values.sizeRangeFrom != null && values.sizeRangeTo != null
				&& values.sizeRangeFrom.longValue() > values.sizeRangeTo.longValue()) {
			return new Result(Messages.getString("message.warn.sizeRange.is.invalid.range"), Field.NONE);
		}
		if (values.creationTimeRangeFrom != null && values.creationTimeRangeTo != null
				&& values.creationTimeRangeFrom.compareTo(values.creationTimeRangeTo) > 0) {
			return new Result(Messages.getString("message.warn.creationTimeRange.is.invalid.range"), Field.NONE);
		}
		if (values.modifiedTimeRangeFrom != null && values.modifiedTimeRangeTo != null
				&& values.modifiedTimeRangeFrom.compareTo(values.modifiedTimeRangeTo) > 0) {
			return new Result(Messages.getString("message.warn.modifiedTimeRange.is.invalid.range"), Field.NONE);
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
}
