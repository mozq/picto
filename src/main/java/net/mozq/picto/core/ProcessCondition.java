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
package net.mozq.picto.core;

import java.nio.file.Path;
import java.time.Instant;
import java.util.TimeZone;

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.OperationType;
import net.mozq.nanotemplate.NanoTemplate;

public class ProcessCondition {

	private Path srcFolder;
	private PictoPathFilter pathFilter;
	private boolean followLinks = false;
	private int depth = Integer.MAX_VALUE;

	private Path destFolder;
	private NanoTemplate destSubFilePathTemplate;

	private OperationType operationType;
	private ExistingFileMethod existingFileMethod;

	private boolean checkFileDigest;

	private boolean changeFileCreationDate = false;
	private boolean changeFileModifiedDate = false;
	private boolean changeFileAccessDate = false;
	private boolean changeFileExifDate = false;
	private DateType baseDateType;
	private Instant customBaseDate = null;
	private DateModType adjustmentType = DateModType.None;
	private Integer adjustmentYears = null;
	private Integer adjustmentMonths = null;
	private Integer adjustmentDays = null;
	private Integer adjustmentHours = null;
	private Integer adjustmentMinutes = null;
	private Integer adjustmentSeconds = null;

	private boolean removeExifAll = false;
	private boolean removeExifGps = false;


	private TimeZone timeZone;

	private boolean dryRun = false;

	public ProcessCondition() {
	}

	public Path getSrcFolder() {
		return srcFolder;
	}

	public void setSrcFolder(Path srcFolder) {
		this.srcFolder = srcFolder;
	}

	public PictoPathFilter getPathFilter() {
		return pathFilter;
	}

	public void setPathFilter(PictoPathFilter pathFilter) {
		this.pathFilter = pathFilter;
	}

	public boolean isFollowLinks() {
		return followLinks;
	}

	public void setFollowLinks(boolean followLinks) {
		this.followLinks = followLinks;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public Path getDestFolder() {
		return destFolder;
	}

	public void setDestFolder(Path destFolder) {
		this.destFolder = destFolder;
	}

	public NanoTemplate getDestSubFilePathTemplate() {
		return destSubFilePathTemplate;
	}

	public void setDestSubFilePathTemplate(NanoTemplate destSubFilePathTemplate) {
		this.destSubFilePathTemplate = destSubFilePathTemplate;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	public ExistingFileMethod getExistingFileMethod() {
		return existingFileMethod;
	}

	public void setExistingFileMethod(ExistingFileMethod existingFileMethod) {
		this.existingFileMethod = existingFileMethod;
	}

	public boolean isCheckFileDigest() {
		return checkFileDigest;
	}

	public void setCheckFileDigest(boolean checkFileDigest) {
		this.checkFileDigest = checkFileDigest;
	}

	public boolean isChangeFileCreationDate() {
		return changeFileCreationDate;
	}

	public void setChangeFileCreationDate(boolean changeFileCreationDate) {
		this.changeFileCreationDate = changeFileCreationDate;
	}

	public boolean isChangeFileModifiedDate() {
		return changeFileModifiedDate;
	}

	public void setChangeFileModifiedDate(boolean changeFileModifiedDate) {
		this.changeFileModifiedDate = changeFileModifiedDate;
	}

	public boolean isChangeFileAccessDate() {
		return changeFileAccessDate;
	}

	public void setChangeFileAccessDate(boolean changeFileAccessDate) {
		this.changeFileAccessDate = changeFileAccessDate;
	}

	public boolean isChangeFileExifDate() {
		return changeFileExifDate;
	}

	public void setChangeFileExifDate(boolean changeFileExifDate) {
		this.changeFileExifDate = changeFileExifDate;
	}

	public DateType getBaseDateType() {
		return baseDateType;
	}

	public void setBaseDateType(DateType baseDateType) {
		this.baseDateType = baseDateType;
	}

	public Instant getCustomBaseDate() {
		return customBaseDate;
	}

	public void setCustomBaseDate(Instant customBaseDate) {
		this.customBaseDate = customBaseDate;
	}

	public DateModType getAdjustmentType() {
		return adjustmentType;
	}

	public void setAdjustmentType(DateModType adjustmentType) {
		this.adjustmentType = adjustmentType;
	}

	public Integer getAdjustmentYears() {
		return adjustmentYears;
	}

	public void setAdjustmentYears(Integer adjustmentYears) {
		this.adjustmentYears = adjustmentYears;
	}

	public Integer getAdjustmentMonths() {
		return adjustmentMonths;
	}

	public void setAdjustmentMonths(Integer adjustmentMonths) {
		this.adjustmentMonths = adjustmentMonths;
	}

	public Integer getAdjustmentDays() {
		return adjustmentDays;
	}

	public void setAdjustmentDays(Integer adjustmentDays) {
		this.adjustmentDays = adjustmentDays;
	}

	public Integer getAdjustmentHours() {
		return adjustmentHours;
	}

	public void setAdjustmentHours(Integer adjustmentHours) {
		this.adjustmentHours = adjustmentHours;
	}

	public Integer getAdjustmentMinutes() {
		return adjustmentMinutes;
	}

	public void setAdjustmentMinutes(Integer adjustmentMinutes) {
		this.adjustmentMinutes = adjustmentMinutes;
	}

	public Integer getAdjustmentSeconds() {
		return adjustmentSeconds;
	}

	public void setAdjustmentSeconds(Integer adjustmentSeconds) {
		this.adjustmentSeconds = adjustmentSeconds;
	}

	public boolean isRemoveExifAll() {
		return removeExifAll;
	}

	public void setRemoveExifAll(boolean removeExifAll) {
		this.removeExifAll = removeExifAll;
	}

	public boolean isRemoveExifGps() {
		return removeExifGps;
	}

	public void setRemoveExifGps(boolean removeExifGps) {
		this.removeExifGps = removeExifGps;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}
}
