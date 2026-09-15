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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.picto.core.exception.PictoInvalidDestinationPathException;

/** Resolves the {@code ${...}} destination-path template variables against a single file's condition/attrs. */
class TemplateVariablesTest {

	@TempDir
	Path tempDir;

	@Test
	void resolvesFileNameBaseNameAndExtension() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(srcRoot.resolve("photo.JPG"), "data");
		TemplateVariables vars = newTemplateVariables(srcRoot, file, file.getParent());

		assertEquals("photo.JPG", vars.resolve("FileName"));
		assertEquals("photo", vars.resolve("BaseName"));
		assertEquals("JPG", vars.resolve("Extension"));
	}

	@Test
	void resolvesSubFilePathAndSubFolderPathRelativeToTheSourceRoot() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path subDir = Files.createDirectories(srcRoot.resolve("2026").resolve("08"));
		Path file = Files.writeString(subDir.resolve("photo.jpg"), "data");
		TemplateVariables vars = newTemplateVariables(srcRoot, file, subDir);

		assertEquals(Path.of("2026", "08", "photo.jpg").toString(), vars.resolve("SubFilePath"));
		assertEquals(Path.of("2026", "08").toString(), vars.resolve("SubFolderPath"));
	}

	@Test
	void resolvesSizeFromFileAttributes() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(srcRoot.resolve("photo.jpg"), "hello");
		TemplateVariables vars = newTemplateVariables(srcRoot, file, file.getParent());

		assertEquals(Long.valueOf(5), vars.resolve("Size"));
	}

	@Test
	void nowResolvesToTheCurrentInstant() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(srcRoot.resolve("photo.jpg"), "data");
		TemplateVariables vars = newTemplateVariables(srcRoot, file, file.getParent());

		Instant before = Instant.now();
		Object now = vars.resolve("Now");
		Instant after = Instant.now();

		assertTrue(now instanceof Instant);
		assertFalse(((Instant)now).isBefore(before));
		assertFalse(((Instant)now).isAfter(after));
	}

	@Test
	void creationModifiedAndAccessDateUseTheFileAttributeUnlessThatAttributeIsBeingChanged() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(srcRoot.resolve("photo.jpg"), "data");
		BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
		Instant baseDate = Instant.parse("2020-01-01T00:00:00Z");

		ProcessCondition unchangedCondition = new ProcessCondition();
		unchangedCondition.setSrcFolder(srcRoot);
		TemplateVariables unchanged = new TemplateVariables(unchangedCondition, file, attrs, srcRoot.relativize(file.getParent()), () -> null, baseDate);
		assertEquals(attrs.creationTime().toInstant(), unchanged.resolve("CreationDate"));
		assertEquals(attrs.lastModifiedTime().toInstant(), unchanged.resolve("ModifiedDate"));
		assertEquals(attrs.lastAccessTime().toInstant(), unchanged.resolve("AccessDate"));

		ProcessCondition changedCondition = new ProcessCondition();
		changedCondition.setSrcFolder(srcRoot);
		changedCondition.setChangeFileCreationDate(true);
		changedCondition.setChangeFileModifiedDate(true);
		changedCondition.setChangeFileAccessDate(true);
		TemplateVariables changed = new TemplateVariables(changedCondition, file, attrs, srcRoot.relativize(file.getParent()), () -> null, baseDate);
		assertEquals(baseDate, changed.resolve("CreationDate"));
		assertEquals(baseDate, changed.resolve("ModifiedDate"));
		assertEquals(baseDate, changed.resolve("AccessDate"));
	}

	@Test
	void takenDateFallsBackToFileModifiedTimeWhenExifIsMissingAndUsesBaseDateOnceExifDateIsBeingChanged() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(srcRoot.resolve("photo.jpg"), "data");
		BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
		Instant baseDate = Instant.parse("2020-01-01T00:00:00Z");

		ProcessCondition condition = new ProcessCondition();
		condition.setSrcFolder(srcRoot);
		TemplateVariables vars = new TemplateVariables(condition, file, attrs, srcRoot.relativize(file.getParent()), () -> null, baseDate);
		// No EXIF metadata (the supplier returns null) - falls back to the file's own last-modified time.
		assertEquals(ExifMetadataSupport.photoTakenDate(file, null), vars.resolve("TakenDate"));

		condition.setChangeFileExifDate(true);
		assertEquals(baseDate, vars.resolve("TakenDate"));
	}

	@Test
	void exifBasedVariablesResolveToNullWhenThereIsNoImageMetadata() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(srcRoot.resolve("photo.jpg"), "data");
		TemplateVariables vars = newTemplateVariables(srcRoot, file, file.getParent());

		assertNull(vars.resolve("Width"));
		assertNull(vars.resolve("Make"));
		assertNull(vars.resolve("GPSLat"));
	}

	@Test
	void resolveThrowsForAnUnknownVariableName() throws IOException {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(srcRoot.resolve("photo.jpg"), "data");
		TemplateVariables vars = newTemplateVariables(srcRoot, file, file.getParent());

		assertThrows(PictoInvalidDestinationPathException.class, () -> vars.resolve("NoSuchVariable"));
	}

	private TemplateVariables newTemplateVariables(Path srcRoot, Path file, Path parent) throws IOException {
		ProcessCondition condition = new ProcessCondition();
		condition.setSrcFolder(srcRoot);
		BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
		return new TemplateVariables(condition, file, attrs, srcRoot.relativize(parent), () -> null, Instant.now());
	}
}
