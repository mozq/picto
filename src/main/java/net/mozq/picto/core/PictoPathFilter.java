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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Date;

public class PictoPathFilter implements Filter<Path> {

	private PathMatcher pathMatcher = null;
	private Path rootPath = null;
	private boolean filenameMatch = false;
	private boolean containsHiddens = false;
	private Range<FileTime> creationTimeRange = null;
	private Range<FileTime> modifiedTimeRange = null;
	private Range<FileTime> accessTimeRange = null;
	private Range<Long> sizeRange = null;


	public PictoPathFilter() {
	}

	public PictoPathFilter setPathPattern(String pathPattern, Path rootPath, boolean regex) {
		if (pathPattern == null || pathPattern.isEmpty()) {
			return this;
		}

		FileSystem fileSystem = FileSystems.getDefault();
		this.pathMatcher = fileSystem.getPathMatcher(((regex) ? "regex:" : "glob:") + pathPattern);
		this.rootPath = rootPath;
		this.filenameMatch = !(pathPattern.contains(File.separator) || pathPattern.contains("/") || pathPattern.contains("\\"));
		return this;
	}

	public PictoPathFilter setContainsHiddens(boolean containsHiddens) {
		this.containsHiddens = containsHiddens;
		return this;
	}

	public PictoPathFilter setCreationTimeRange(FileTime from, FileTime to) {
		this.creationTimeRange = Range.of(from, to);
		return this;
	}

	public PictoPathFilter setCreationTimeRange(Instant from, Instant to) {
		return setCreationTimeRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setCreationTimeRange(Date from, Date to) {
		return setCreationTimeRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setModifiedTimeRange(FileTime from, FileTime to) {
		this.modifiedTimeRange = Range.of(from, to);
		return this;
	}

	public PictoPathFilter setModifiedTimeRange(Instant from, Instant to) {
		return setModifiedTimeRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setModifiedTimeRange(Date from, Date to) {
		return setModifiedTimeRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setAccessTimeRange(FileTime from, FileTime to) {
		this.accessTimeRange = Range.of(from, to);
		return this;
	}

	public PictoPathFilter setAccessTimeRange(Instant from, Instant to) {
		return setAccessTimeRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setAccessTimeRange(Date from, Date to) {
		return setAccessTimeRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setSizeRange(Long from, Long to) {
		this.sizeRange = Range.of(from, to);
		return this;
	}

	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public boolean isContainsHiddens() {
		return containsHiddens;
	}

	public Range<FileTime> getCreationTimeRange() {
		return creationTimeRange;
	}

	public Range<FileTime> getModifiedTimeRange() {
		return modifiedTimeRange;
	}

	public Range<FileTime> getAccessTimeRange() {
		return accessTimeRange;
	}

	public Range<Long> getSizeRange() {
		return sizeRange;
	}

	private static FileTime toFileTime(Instant instant) {
		return instant != null ? FileTime.from(instant) : null;
	}

	private static FileTime toFileTime(Date date) {
		return date != null ? FileTime.fromMillis(date.getTime()) : null;
	}

	@Override
	public boolean accept(Path path) throws IOException {

		BasicFileAttributes fileAttrs = null;

		if (this.creationTimeRange != null
				|| this.modifiedTimeRange != null
				|| this.accessTimeRange != null
				|| this.sizeRange != null) {
			fileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
		}

		return accept(path, fileAttrs);
	}

	public boolean accept(Path path, BasicFileAttributes fileAttrs) throws IOException {

		if (this.pathMatcher != null) {
			Path p;
			if (this.filenameMatch) {
				p = path.getFileName();
			} else if (this.rootPath != null) {
				p = this.rootPath.relativize(path);
			} else {
				p = path;
			}

			if (!this.pathMatcher.matches(p)) {
				return false;
			}
		}

		if (!this.containsHiddens) {
			if (Files.isHidden(path)) {
				return false;
			}
		}

		if (fileAttrs == null && (this.creationTimeRange != null
				|| this.modifiedTimeRange != null
				|| this.accessTimeRange != null
				|| this.sizeRange != null)) {
			fileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
		}

		if (this.creationTimeRange != null) {
			if (!this.creationTimeRange.contains(fileAttrs.creationTime())) {
				return false;
			}
		}

		if (this.modifiedTimeRange != null) {
			if (!this.modifiedTimeRange.contains(fileAttrs.lastModifiedTime())) {
				return false;
			}
		}

		if (this.accessTimeRange != null) {
			if (!this.accessTimeRange.contains(fileAttrs.lastAccessTime())) {
				return false;
			}
		}

		if (this.sizeRange != null) {
			if (!this.sizeRange.contains(fileAttrs.size())) {
				return false;
			}
		}

		return true;
	}
}
