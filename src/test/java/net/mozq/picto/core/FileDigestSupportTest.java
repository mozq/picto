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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileDigestSupportTest {

	@TempDir
	Path tempDir;

	@Test
	void copyAndVerifyCopiesFileContentExactly() throws IOException {
		Path source = Files.writeString(tempDir.resolve("source.txt"), "hello world");
		Path dest = tempDir.resolve("dest.txt");

		FileDigestSupport.copyAndVerify(source, dest);

		assertEquals("hello world", Files.readString(dest));
	}

	@Test
	void copyAndVerifyOverwritesAnExistingDestination() throws IOException {
		Path source = Files.writeString(tempDir.resolve("source.txt"), "new content");
		Path dest = Files.writeString(tempDir.resolve("dest.txt"), "stale content");

		FileDigestSupport.copyAndVerify(source, dest);

		assertEquals("new content", Files.readString(dest));
	}
}
