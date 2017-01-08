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
import java.util.Date;
import java.util.TimeZone;

import org.mifmi.commons4j.text.format.NamedFormatter;

import net.mozq.picto.enums.DateModType;
import net.mozq.picto.enums.DateType;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.OperationType;

public class ProcessCondition {

	private Path srcRootPath;
	private PictoPathFilter pathFilter;
	boolean followLinks = false;
	int dept = Integer.MAX_VALUE;

	private Path destRootPath;
	private NamedFormatter destSubPathFormat;

	private OperationType operationType;
	private ExistingFileMethod existingFileMethod;
	
	private boolean checkDigest;
	
	private boolean changeFileCreationDate = false;
	private boolean changeFileModifiedDate = false;
	private boolean changeFileAccessDate = false;
	private boolean changeExifDate = false;
	private DateType baseDateType;
	private Date customBaseDate = null;
	private DateModType baseDateModType = DateModType.None;
	private Integer baseDateModYears = null;
	private Integer baseDateModMonths = null;
	private Integer baseDateModDays = null;
	private Integer baseDateModHours = null;
	private Integer baseDateModMinutes = null;
	private Integer baseDateModSeconds = null;
	
	private boolean remveExifTagsAll = false;
	private boolean remveExifTagsGps = false;
	
	
	private TimeZone timeZone;
	
	boolean dryRun = false;
	
	public ProcessCondition() {
	}

	public Path getSrcRootPath() {
		return srcRootPath;
	}

	public void setSrcRootPath(Path srcRootPath) {
		this.srcRootPath = srcRootPath;
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

	public int getDept() {
		return dept;
	}

	public void setDept(int dept) {
		this.dept = dept;
	}

	public Path getDestRootPath() {
		return destRootPath;
	}

	public void setDestRootPath(Path destRootPath) {
		this.destRootPath = destRootPath;
	}

	public NamedFormatter getDestSubPathFormat() {
		return destSubPathFormat;
	}

	public void setDestSubPathFormat(NamedFormatter destSubPathFormat) {
		this.destSubPathFormat = destSubPathFormat;
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

	public boolean isCheckDigest() {
		return checkDigest;
	}

	public void setCheckDigest(boolean checkDigest) {
		this.checkDigest = checkDigest;
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

	public boolean isChangeExifDate() {
		return changeExifDate;
	}

	public void setChangeExifDate(boolean changeExifDate) {
		this.changeExifDate = changeExifDate;
	}

	public DateType getBaseDateType() {
		return baseDateType;
	}

	public void setBaseDateType(DateType baseDateType) {
		this.baseDateType = baseDateType;
	}

	public Date getCustomBaseDate() {
		return customBaseDate;
	}

	public void setCustomBaseDate(Date customBaseDate) {
		this.customBaseDate = customBaseDate;
	}

	public DateModType getBaseDateModType() {
		return baseDateModType;
	}

	public void setBaseDateModType(DateModType baseDateModType) {
		this.baseDateModType = baseDateModType;
	}

	public Integer getBaseDateModYears() {
		return baseDateModYears;
	}

	public void setBaseDateModYears(Integer baseDateModYears) {
		this.baseDateModYears = baseDateModYears;
	}

	public Integer getBaseDateModMonths() {
		return baseDateModMonths;
	}

	public void setBaseDateModMonths(Integer baseDateModMonths) {
		this.baseDateModMonths = baseDateModMonths;
	}

	public Integer getBaseDateModDays() {
		return baseDateModDays;
	}

	public void setBaseDateModDays(Integer baseDateModDays) {
		this.baseDateModDays = baseDateModDays;
	}

	public Integer getBaseDateModHours() {
		return baseDateModHours;
	}

	public void setBaseDateModHours(Integer baseDateModHours) {
		this.baseDateModHours = baseDateModHours;
	}

	public Integer getBaseDateModMinutes() {
		return baseDateModMinutes;
	}

	public void setBaseDateModMinutes(Integer baseDateModMinutes) {
		this.baseDateModMinutes = baseDateModMinutes;
	}

	public Integer getBaseDateModSeconds() {
		return baseDateModSeconds;
	}

	public void setBaseDateModSeconds(Integer baseDateModSeconds) {
		this.baseDateModSeconds = baseDateModSeconds;
	}

	public boolean isRemveExifTagsAll() {
		return remveExifTagsAll;
	}

	public void setRemveExifTagsAll(boolean remveExifTagsAll) {
		this.remveExifTagsAll = remveExifTagsAll;
	}

	public boolean isRemveExifTagsGps() {
		return remveExifTagsGps;
	}

	public void setRemveExifTagsGps(boolean remveExifTagsGps) {
		this.remveExifTagsGps = remveExifTagsGps;
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
