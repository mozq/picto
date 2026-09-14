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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.FileSizeUnit;
import net.mozq.picto.enums.OperationType;

class ProcessConditionBuilderTest {
	private static final TimeZone TIME_ZONE = TimeZone.getDefault();

	@TempDir
	Path tempDir;

	@Test
	void validatesFolderInputs() throws IOException {
		Path folder = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(tempDir.resolve("file.txt"), "content");

		assertNull(ProcessConditionBuilder.validateSourceFolder(folder.toString(), "Source folder"));
		assertEquals(ProcessConditionBuilder.Field.SOURCE_FOLDER,
				ProcessConditionBuilder.validateSourceFolder("", "Source folder").field());
		assertEquals(ProcessConditionBuilder.Field.SOURCE_FOLDER,
				ProcessConditionBuilder.validateSourceFolder(file.toString(), "Source folder").field());
	}

	@Test
	void rejectsUnsafeCopyMoveFolderRelationships() throws IOException {
		Path src = Files.createDirectories(tempDir.resolve("src"));
		Path dest = Files.createDirectories(src.resolve("dest"));

		ProcessConditionInput input = input(src, dest);

		assertEquals(ProcessConditionBuilder.Field.DESTINATION_FOLDER,
				build(input).invalidResult().field());

		input.operationType = OperationType.Overwrite;

		assertTrue(build(input).isValid());
	}

	@Test
	void rejectsSameSourceAndDestinationFolders() throws IOException {
		Path folder = Files.createDirectories(tempDir.resolve("folder"));

		ProcessConditionInput input = input(folder, folder);

		assertEquals(ProcessConditionBuilder.Field.NONE,
				build(input).invalidResult().field());
	}

	@Test
	void rejectsSourceFolderInsideDestinationFolder() throws IOException {
		Path dest = Files.createDirectories(tempDir.resolve("dest"));
		Path src = Files.createDirectories(dest.resolve("src"));

		ProcessConditionInput input = input(src, dest);

		assertEquals(ProcessConditionBuilder.Field.SOURCE_FOLDER,
				build(input).invalidResult().field());
	}

	@Test
	void validatesTemplateSyntaxAndRanges() throws IOException {
		Path src = Files.createDirectories(tempDir.resolve("src"));
		Path dest = Files.createDirectories(tempDir.resolve("dest"));

		ProcessConditionInput input = input(src, dest);
		input.destSubFilePathPattern = "${";

		assertEquals(ProcessConditionBuilder.Field.DESTINATION_SUBFOLDER,
				build(input).invalidResult().field());

		input.destSubFilePathPattern = "${FileName}";
		input.fileSizeFrom = "20";
		input.fileSizeTo = "10";

		assertEquals(ProcessConditionBuilder.Field.NONE,
				build(input).invalidResult().field());
	}

	@Test
	void rejectsInvalidFilePattern() throws IOException {
		Path src = Files.createDirectories(tempDir.resolve("src"));
		Path dest = Files.createDirectories(tempDir.resolve("dest"));

		ProcessConditionInput input = input(src, dest);
		input.srcFileNamePattern = "[";

		assertEquals(ProcessConditionBuilder.Field.FILE_NAME_PATTERN,
				build(input).invalidResult().field());
	}

	private static ProcessConditionBuilder.BuildResult build(ProcessConditionInput input) {
		return ProcessConditionBuilder.build(input, TIME_ZONE);
	}

	private static ProcessConditionInput input(Path src, Path dest) {
		ProcessConditionInput input = new ProcessConditionInput();
		input.srcFolder = src.toString();
		input.destFolder = dest.toString();
		input.operationType = OperationType.Copy;
		input.srcFileNamePattern = "*";
		input.srcFileNamePatternSyntax = FilePatternSyntax.Glob;
		input.destSubFilePathPattern = "${FileName}";
		input.fileSizeFrom = "";
		input.fileSizeTo = "";
		input.fileSizeUnit = FileSizeUnit.Bytes;
		input.createdFrom = "";
		input.createdTo = "";
		input.modifiedFrom = "";
		input.modifiedTo = "";
		return input;
	}
}
