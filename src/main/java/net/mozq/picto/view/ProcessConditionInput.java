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

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.FileSizeUnit;
import net.mozq.picto.enums.OperationType;

/**
 * A snapshot of MainFrame's condition fields, held at the same granularity as the Swing components
 * themselves (their literal text, selection, or checked state) rather than already parsed into the types
 * {@link net.mozq.picto.core.ProcessCondition} needs. That parsing happens in {@link ProcessConditionBuilder},
 * which both validates and builds from the same values - so this snapshot stays free for anything in the
 * view layer to read (e.g. rendering a summary of the current input) without reaching back into the
 * Swing fields it came from.
 */
class ProcessConditionInput {
	String srcFolder;
	String srcFileNamePattern;
	FilePatternSyntax srcFileNamePatternSyntax;
	boolean includeHiddenFiles;
	boolean followLinks;
	boolean includeSubfolders;
	OperationType operationType;
	String destFolder;
	String destSubFilePathPattern;
	ExistingFileMethod existingFileMethod;
	boolean checkFileDigest;
	boolean changeFileCreationDate;
	boolean changeFileModifiedDate;
	boolean changeFileAccessDate;
	boolean changeFileExifDate;
	DateType baseDateType;
	String customBaseDate;
	DateModType adjustmentType;
	String adjustmentYears;
	String adjustmentMonths;
	String adjustmentDays;
	String adjustmentHours;
	String adjustmentMinutes;
	String adjustmentSeconds;
	boolean removeExifGps;
	boolean removeExifAll;
	String fileSizeFrom;
	String fileSizeTo;
	FileSizeUnit fileSizeUnit;
	String createdFrom;
	String createdTo;
	String modifiedFrom;
	String modifiedTo;
}
