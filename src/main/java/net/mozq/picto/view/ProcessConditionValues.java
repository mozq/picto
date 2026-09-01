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
import net.mozq.picto.enums.OperationType;

class ProcessConditionValues {
	Path srcRootDirPath;
	String filePattern;
	boolean filePatternRegex;
	boolean containsHiddens;
	boolean followLinks;
	int depth;
	OperationType operationType;
	Path destRootDirPath;
	String destSubPathPattern;
	ExistingFileMethod existingFileMethod;
	boolean checkDigest;
	boolean changeFileCreationDate;
	boolean changeFileModifiedDate;
	boolean changeFileAccessDate;
	boolean changeExifDate;
	DateType baseDateType;
	Date customBaseDate;
	DateModType baseDateModType;
	Integer baseDateModYears;
	Integer baseDateModMonths;
	Integer baseDateModDays;
	Integer baseDateModHours;
	Integer baseDateModMinutes;
	Integer baseDateModSeconds;
	boolean removeExifTagsGps;
	boolean removeExifTagsAll;
	Long sizeRangeFrom;
	Long sizeRangeTo;
	Date creationTimeRangeFrom;
	Date creationTimeRangeTo;
	Date modifiedTimeRangeFrom;
	Date modifiedTimeRangeTo;
}
