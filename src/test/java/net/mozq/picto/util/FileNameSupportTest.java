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
package net.mozq.picto.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FileNameSupportTest {

	@Test
	void testStandardFile() {
		assertEquals("image", FileNameSupport.baseName("image.jpg"));
		assertEquals("jpg", FileNameSupport.extension("image.jpg"));
	}

	@Test
	void testMultipleDots() {
		assertEquals("archive.tar", FileNameSupport.baseName("archive.tar.gz"));
		assertEquals("gz", FileNameSupport.extension("archive.tar.gz"));
	}

	@Test
	void testNoExtension() {
		assertEquals("README", FileNameSupport.baseName("README"));
		assertEquals("", FileNameSupport.extension("README"));
	}

	@Test
	void testTrailingDot() {
		assertEquals("file", FileNameSupport.baseName("file."));
		assertEquals("", FileNameSupport.extension("file."));
	}

	@Test
	void testHiddenFileStandalone() {
		assertEquals(".gitignore", FileNameSupport.baseName(".gitignore"));
		assertEquals("", FileNameSupport.extension(".gitignore"));
	}

	@Test
	void testHiddenFileWithExtensionStandalone() {
		assertEquals(".config", FileNameSupport.baseName(".config.json"));
		assertEquals("json", FileNameSupport.extension(".config.json"));
	}

	@Test
	void testPathWithStandardFileUnix() {
		assertEquals("/path/to/image", FileNameSupport.baseName("/path/to/image.jpg"));
		assertEquals("jpg", FileNameSupport.extension("/path/to/image.jpg"));
	}

	@Test
	void testPathWithStandardFileWindows() {
		assertEquals("C:\\path\\to\\image", FileNameSupport.baseName("C:\\path\\to\\image.jpg"));
		assertEquals("jpg", FileNameSupport.extension("C:\\path\\to\\image.jpg"));
	}

	@Test
	void testPathWithHiddenFileUnix() {
		assertEquals("/path/to/.gitignore", FileNameSupport.baseName("/path/to/.gitignore"));
		assertEquals("", FileNameSupport.extension("/path/to/.gitignore"));
	}

	@Test
	void testPathWithHiddenFileWindows() {
		assertEquals("C:\\Users\\user\\.bashrc", FileNameSupport.baseName("C:\\Users\\user\\.bashrc"));
		assertEquals("", FileNameSupport.extension("C:\\Users\\user\\.bashrc"));
	}

	@Test
	void testPathWithHiddenFileAndExtensionUnix() {
		assertEquals("/home/user/.settings", FileNameSupport.baseName("/home/user/.settings.conf"));
		assertEquals("conf", FileNameSupport.extension("/home/user/.settings.conf"));
	}

	@Test
	void testEmptyString() {
		assertEquals("", FileNameSupport.baseName(""));
		assertEquals("", FileNameSupport.extension(""));
	}
}
