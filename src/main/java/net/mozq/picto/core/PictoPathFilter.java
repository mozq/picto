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

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.mifmi.commons4j.matcher.DateMatcher;
import org.mifmi.commons4j.matcher.IMatcher;
import org.mifmi.commons4j.matcher.NumberMatcher;

public class PictoPathFilter implements Filter<Path> {

	private PathMatcher pathMatcher = null;
	private boolean containsHiddens = false;
	private IMatcher<Date> creationTimeMatcher = null;
	private IMatcher<Date> modifiedTimeMatcher = null;
	private IMatcher<Date> accessTimeMatcher = null;
	private IMatcher<Number> sizeMatcher = null;
	
	
	public PictoPathFilter() {
	}
	
	public PictoPathFilter setPathPattern(String pathPattern, boolean regex) {
		if (pathPattern == null || pathPattern.isEmpty()) {
			return this;
		}
		
		final FileSystem fileSystem = FileSystems.getDefault();
		this.pathMatcher = fileSystem.getPathMatcher(((regex) ? "regex:" : "glob:") + pathPattern);
		return this;
	}
	
	public PictoPathFilter setContainsHiddens(boolean containsHiddens) {
		this.containsHiddens = containsHiddens;
		return this;
	}
	
	public PictoPathFilter setCreationTimeRange(Date from, Date to) {
		this.creationTimeMatcher = DateMatcher.between(from, to);
		return this;
	}
	
	public PictoPathFilter setModifiedTimeRange(Date from, Date to) {
		this.modifiedTimeMatcher = DateMatcher.between(from, to);
		return this;
	}
	
	public PictoPathFilter setAccessTimeRange(Date from, Date to) {
		this.accessTimeMatcher = DateMatcher.between(from, to);
		return this;
	}
	
	public PictoPathFilter setSizeRange(Long from, Long to) {
		this.sizeMatcher = NumberMatcher.between(from, to);
		return this;
	}
	
	public PathMatcher getPathMatcher() {
		return pathMatcher;
	}

	public boolean isContainsHiddens() {
		return containsHiddens;
	}

	public IMatcher<Date> getCreationTimeMatcher() {
		return creationTimeMatcher;
	}

	public IMatcher<Date> getModifiedTimeMatcher() {
		return modifiedTimeMatcher;
	}

	public IMatcher<Date> getAccessTimeMatcher() {
		return accessTimeMatcher;
	}

	public IMatcher<Number> getSizeMatcher() {
		return sizeMatcher;
	}

	@Override
	public boolean accept(Path path) throws IOException {

		BasicFileAttributes fileAttrs = null;
		
		if (this.creationTimeMatcher != null
				|| this.modifiedTimeMatcher != null
				|| this.accessTimeMatcher != null) {
			fileAttrs = Files.readAttributes(path, BasicFileAttributes.class);
		}
		
		return accept(path, fileAttrs);
	}
	
	public boolean accept(Path path, BasicFileAttributes fileAttrs) throws IOException {
		
		if (this.pathMatcher != null && !this.pathMatcher.matches(path)) {
			return false;
		}

		if (!this.containsHiddens) {
			if (Files.isHidden(path)) {
				return false;
			}
		}

		if (this.creationTimeMatcher != null) {
			if (!this.creationTimeMatcher.matches(new Date(fileAttrs.creationTime().toMillis()))) {
				return false;
			}
		}
		
		if (this.modifiedTimeMatcher != null) {
			if (!this.modifiedTimeMatcher.matches(new Date(fileAttrs.lastModifiedTime().toMillis()))) {
				return false;
			}
		}
		
		if (this.accessTimeMatcher != null) {
			if (!this.accessTimeMatcher.matches(new Date(fileAttrs.lastAccessTime().toMillis()))) {
				return false;
			}
		}
		
		if (this.sizeMatcher != null) {
			long size = Files.size(path);
			if (!this.sizeMatcher.matches(size)) {
				return false;
			}
		}
		
		return true;
	}
}
