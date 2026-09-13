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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.picto.enums.FilePatternSyntax;
import net.mozq.picto.enums.OperationType;

class ProcessConditionValidatorTest {
	@TempDir
	Path tempDir;

	@Test
	void validatesFolderInputs() throws IOException {
		Path folder = Files.createDirectories(tempDir.resolve("src"));
		Path file = Files.writeString(tempDir.resolve("file.txt"), "content");

		assertNull(ProcessConditionValidator.validateSourceFolder(folder.toString(), "Source folder"));
		assertEquals(ProcessConditionValidator.Field.SOURCE_FOLDER,
				ProcessConditionValidator.validateSourceFolder("", "Source folder").field());
		assertEquals(ProcessConditionValidator.Field.SOURCE_FOLDER,
				ProcessConditionValidator.validateSourceFolder(file.toString(), "Source folder").field());
	}

	@Test
	void rejectsUnsafeCopyMoveFolderRelationships() throws IOException {
		Path src = Files.createDirectories(tempDir.resolve("src"));
		Path dest = Files.createDirectories(src.resolve("dest"));

		ProcessConditionValues values = values(src, dest);

		assertEquals(ProcessConditionValidator.Field.DESTINATION_FOLDER,
				ProcessConditionValidator.validate(values).field());

		values.operationType = OperationType.Overwrite;

		assertNull(ProcessConditionValidator.validate(values));
	}

	@Test
	void rejectsSameSourceAndDestinationFolders() throws IOException {
		Path folder = Files.createDirectories(tempDir.resolve("folder"));

		ProcessConditionValues values = values(folder, folder);

		assertEquals(ProcessConditionValidator.Field.NONE,
				ProcessConditionValidator.validate(values).field());
	}

	@Test
	void rejectsSourceFolderInsideDestinationFolder() throws IOException {
		Path dest = Files.createDirectories(tempDir.resolve("dest"));
		Path src = Files.createDirectories(dest.resolve("src"));

		ProcessConditionValues values = values(src, dest);

		assertEquals(ProcessConditionValidator.Field.SOURCE_FOLDER,
				ProcessConditionValidator.validate(values).field());
	}

	@Test
	void validatesTemplateSyntaxAndRanges() throws IOException {
		Path src = Files.createDirectories(tempDir.resolve("src"));
		Path dest = Files.createDirectories(tempDir.resolve("dest"));

		ProcessConditionValues values = values(src, dest);
		values.destSubFilePathPattern = "${";

		assertEquals(ProcessConditionValidator.Field.DESTINATION_SUBFOLDER,
				ProcessConditionValidator.validate(values).field());

		values.destSubFilePathPattern = "${FileName}";
		values.fileSizeFrom = 20L;
		values.fileSizeTo = 10L;

		assertEquals(ProcessConditionValidator.Field.NONE,
				ProcessConditionValidator.validate(values).field());
	}

	@Test
	void rejectsInvalidFilePattern() throws IOException {
		Path src = Files.createDirectories(tempDir.resolve("src"));
		Path dest = Files.createDirectories(tempDir.resolve("dest"));

		ProcessConditionValues values = values(src, dest);
		values.srcFileNamePattern = "[";

		assertEquals(ProcessConditionValidator.Field.FILE_PATTERN,
				ProcessConditionValidator.validate(values).field());
	}

	private static ProcessConditionValues values(Path src, Path dest) {
		ProcessConditionValues values = new ProcessConditionValues();
		values.srcFolder = src;
		values.destFolder = dest;
		values.operationType = OperationType.Copy;
		values.srcFileNamePattern = "*";
		values.srcFileNamePatternSyntax = FilePatternSyntax.Glob;
		values.destSubFilePathPattern = "${FileName}";
		values.createdFrom = new Date(0);
		values.createdTo = new Date(1);
		values.modifiedFrom = new Date(0);
		values.modifiedTo = new Date(1);
		return values;
	}
}
