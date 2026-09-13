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

import java.nio.file.Path;
import java.util.Date;

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.OperationType;

class ProcessConditionValues {
	Path srcFolder;
	String srcFileNamePattern;
	FilePatternSyntax srcFileNamePatternSyntax;
	boolean includeHiddenFiles;
	boolean followLinks;
	int depth;
	OperationType operationType;
	Path destFolder;
	String destSubFilePathPattern;
	ExistingFileMethod existingFileMethod;
	boolean compareFileDigest;
	boolean changeFileCreationDate;
	boolean changeFileModifiedDate;
	boolean changeFileAccessDate;
	boolean changeFileExifDate;
	DateType baseDateType;
	Date customBaseDate;
	DateModType adjustmentType;
	Integer adjustmentYears;
	Integer adjustmentMonths;
	Integer adjustmentDays;
	Integer adjustmentHours;
	Integer adjustmentMinutes;
	Integer adjustmentSeconds;
	boolean removeExifGps;
	boolean removeExifAll;
	Long fileSizeFrom;
	Long fileSizeTo;
	Date createdFrom;
	Date createdTo;
	Date modifiedFrom;
	Date modifiedTo;
}
