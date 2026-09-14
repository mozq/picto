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

import net.mozq.picto.enums.FilePatternSyntax;

public class PictoPathFilter implements Filter<Path> {

	private PathMatcher pathMatcher = null;
	private Path rootPath = null;
	private boolean filenameMatch = false;
	private boolean includeHiddenFiles = false;
	private Range<FileTime> createdRange = null;
	private Range<FileTime> modifiedRange = null;
	private Range<FileTime> accessRange = null;
	private Range<Long> fileSizeRange = null;


	public PictoPathFilter() {
	}

	public PictoPathFilter setPathPattern(String pathPattern, Path rootPath, FilePatternSyntax syntax) {
		if (pathPattern == null || pathPattern.isEmpty()) {
			return this;
		}

		this.pathMatcher = buildPathMatcher(pathPattern, syntax);
		this.rootPath = rootPath;
		this.filenameMatch = !(pathPattern.contains(File.separator) || pathPattern.contains("/") || pathPattern.contains("\\"));
		return this;
	}

	public static PathMatcher buildPathMatcher(String pathPattern, FilePatternSyntax syntax) {
		FileSystem fileSystem = FileSystems.getDefault();
		return fileSystem.getPathMatcher((syntax == FilePatternSyntax.Regex ? "regex:" : "glob:") + pathPattern);
	}

	public PictoPathFilter setIncludeHiddenFiles(boolean includeHiddenFiles) {
		this.includeHiddenFiles = includeHiddenFiles;
		return this;
	}

	public PictoPathFilter setCreatedRange(FileTime from, FileTime to) {
		this.createdRange = Range.of(from, to);
		return this;
	}

	public PictoPathFilter setCreatedRange(Instant from, Instant to) {
		return setCreatedRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setModifiedRange(FileTime from, FileTime to) {
		this.modifiedRange = Range.of(from, to);
		return this;
	}

	public PictoPathFilter setModifiedRange(Instant from, Instant to) {
		return setModifiedRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setAccessRange(FileTime from, FileTime to) {
		this.accessRange = Range.of(from, to);
		return this;
	}

	public PictoPathFilter setAccessRange(Instant from, Instant to) {
		return setAccessRange(toFileTime(from), toFileTime(to));
	}

	public PictoPathFilter setFileSizeRange(Long from, Long to) {
		this.fileSizeRange = Range.of(from, to);
		return this;
	}

	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public boolean isIncludeHiddenFiles() {
		return includeHiddenFiles;
	}

	public Range<FileTime> getCreatedRange() {
		return createdRange;
	}

	public Range<FileTime> getModifiedRange() {
		return modifiedRange;
	}

	public Range<FileTime> getAccessRange() {
		return accessRange;
	}

	public Range<Long> getFileSizeRange() {
		return fileSizeRange;
	}

	private static FileTime toFileTime(Instant instant) {
		return instant != null ? FileTime.from(instant) : null;
	}

	@Override
	public boolean accept(Path path) throws IOException {

		BasicFileAttributes fileAttrs = null;

		if (this.createdRange != null
				|| this.modifiedRange != null
				|| this.accessRange != null
				|| this.fileSizeRange != null) {
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

		if (!this.includeHiddenFiles) {
			if (Files.isHidden(path)) {
				return false;
			}
		}

		if (fileAttrs == null && (this.createdRange != null
				|| this.modifiedRange != null
				|| this.accessRange != null
				|| this.fileSizeRange != null)) {
			fileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
		}

		if (this.createdRange != null) {
			if (!this.createdRange.contains(fileAttrs.creationTime())) {
				return false;
			}
		}

		if (this.modifiedRange != null) {
			if (!this.modifiedRange.contains(fileAttrs.lastModifiedTime())) {
				return false;
			}
		}

		if (this.accessRange != null) {
			if (!this.accessRange.contains(fileAttrs.lastAccessTime())) {
				return false;
			}
		}

		if (this.fileSizeRange != null) {
			if (!this.fileSizeRange.contains(fileAttrs.size())) {
				return false;
			}
		}

		return true;
	}
}
