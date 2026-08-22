/*!
 * Picto
 * Copyright 2016 Mozq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PictoPathFilterTest {

	@TempDir
	Path tempDir;

	@Test
	void matchesFileNameGlobWhenPatternHasNoDirectory() throws IOException {
		Path jpg = Files.write(tempDir.resolve("photo.jpg"), new byte[] { 1 });
		Path txt = Files.write(tempDir.resolve("photo.txt"), new byte[] { 1 });

		PictoPathFilter filter = new PictoPathFilter().setPathPattern("*.jpg", tempDir, false);

		assertTrue(filter.accept(jpg));
		assertFalse(filter.accept(txt));
	}

	@Test
	void matchesPathGlobRelativeToRootWhenPatternHasDirectory() throws IOException {
		Path matchingDir = Files.createDirectories(tempDir.resolve("2026"));
		Path otherDir = Files.createDirectories(tempDir.resolve("draft"));
		Path matching = Files.write(matchingDir.resolve("photo.jpg"), new byte[] { 1 });
		Path other = Files.write(otherDir.resolve("photo.jpg"), new byte[] { 1 });

		PictoPathFilter filter = new PictoPathFilter().setPathPattern("2026/*.jpg", tempDir, false);

		assertTrue(filter.accept(matching));
		assertFalse(filter.accept(other));
	}

	@Test
	void rejectsHiddenFilesByDefault() throws IOException {
		Path hidden = Files.write(tempDir.resolve(".hidden.jpg"), new byte[] { 1 });
		assumeTrue(Files.isHidden(hidden));

		PictoPathFilter filter = new PictoPathFilter();

		assertFalse(filter.accept(hidden));
	}

	@Test
	void acceptsHiddenFilesWhenEnabled() throws IOException {
		Path hidden = Files.write(tempDir.resolve(".hidden.jpg"), new byte[] { 1 });
		assumeTrue(Files.isHidden(hidden));

		PictoPathFilter filter = new PictoPathFilter().setContainsHiddens(true);

		assertTrue(filter.accept(hidden));
	}

	@Test
	void matchesFileSizeRange() throws IOException {
		Path small = Files.write(tempDir.resolve("small.jpg"), new byte[] { 1, 2 });
		Path large = Files.write(tempDir.resolve("large.jpg"), new byte[] { 1, 2, 3, 4 });

		PictoPathFilter filter = new PictoPathFilter().setSizeRange(2L, 2L);

		assertTrue(filter.accept(small));
		assertFalse(filter.accept(large));
	}

	@Test
	void matchesRegexFileName() throws IOException {
		Path matching = Files.write(tempDir.resolve("IMG_0001.JPG"), new byte[] { 1 });
		Path other = Files.write(tempDir.resolve("DSC_0001.JPG"), new byte[] { 1 });

		PictoPathFilter filter = new PictoPathFilter().setPathPattern("IMG_[0-9]{4}\\.JPG", tempDir, true);

		assertTrue(filter.accept(matching));
		assertFalse(filter.accept(other));
	}

	@Test
	void acceptsAllFilesWhenNoPatternOrRangeIsConfigured() throws IOException {
		Path file = Files.write(tempDir.resolve("photo.raw"), new byte[] { 1 });

		PictoPathFilter filter = new PictoPathFilter();

		assertTrue(filter.accept(file));
	}
}
