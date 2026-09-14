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
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

import net.mozq.picto.enums.ProcessDataStatus;

public class ProcessData {

	private Path srcPath;
	private BasicFileAttributes srcFileAttributes;

	private Path destPath;
	private String srcRelativePath;
	private String destRelativePath;

	private Instant baseDate;

	private volatile ProcessDataStatus status;

	private volatile String message;

	public ProcessData() {
	}

	public Path getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(Path srcPath) {
		this.srcPath = srcPath;
	}

	public BasicFileAttributes getSrcFileAttributes() {
		return srcFileAttributes;
	}

	public void setSrcFileAttributes(BasicFileAttributes srcFileAttributes) {
		this.srcFileAttributes = srcFileAttributes;
	}

	public Path getDestPath() {
		return destPath;
	}

	public void setDestPath(Path destPath) {
		this.destPath = destPath;
	}

	public String getSrcRelativePath() {
		return srcRelativePath;
	}

	public void setSrcRelativePath(String srcRelativePath) {
		this.srcRelativePath = srcRelativePath;
	}

	public String getDestRelativePath() {
		return destRelativePath;
	}

	public void setDestRelativePath(String destRelativePath) {
		this.destRelativePath = destRelativePath;
	}

	public Instant getBaseDate() {
		return baseDate;
	}

	public void setBaseDate(Instant baseDate) {
		this.baseDate = baseDate;
	}

	public ProcessDataStatus getStatus() {
		return status;
	}

	public void setStatus(ProcessDataStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
