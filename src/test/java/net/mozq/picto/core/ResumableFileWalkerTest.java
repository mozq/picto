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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * {@link ResumableFileWalker} backs {@code SourceFileScanner}'s live match-count preview, which needs to
 * pause and resume a folder tree walk across many condition changes without ever restarting it from
 * scratch - so what matters most here is that stopping and restarting {@link ResumableFileWalker#next()}
 * part-way through never misses or repeats an entry.
 */
class ResumableFileWalkerTest {
	@TempDir
	Path tempDir;

	@Test
	void returnsNullOnceAnEmptyTreeIsFullyWalked() throws IOException {
		try (ResumableFileWalker walker = new ResumableFileWalker(tempDir)) {
			assertNull(walker.next());
			assertTrue(walker.isExhausted());
		}
	}

	@Test
	void isNotExhaustedUntilNextReturnsNull() throws IOException {
		Files.writeString(tempDir.resolve("a.txt"), "content");

		try (ResumableFileWalker walker = new ResumableFileWalker(tempDir)) {
			assertFalse(walker.isExhausted());
			walker.next();
			assertFalse(walker.isExhausted(), "the file was returned, but exhaustion isn't known until the following next() returns null");
			assertNull(walker.next());
			assertTrue(walker.isExhausted());
		}
	}

	@Test
	void findsEveryFileInAFlatDirectoryExactlyOnce() throws IOException {
		Set<String> expected = createFiles(tempDir, "a.txt", "b.txt", "c.txt");

		assertEquals(expected, walkToCompletion(tempDir));
	}

	@Test
	void recursesIntoSubdirectoriesWithoutReturningTheDirectoriesThemselves() throws IOException {
		Path sub = Files.createDirectories(tempDir.resolve("sub"));
		Set<String> expected = new HashSet<>();
		expected.addAll(createFiles(tempDir, "top.txt"));
		expected.addAll(createFiles(sub, "nested.txt"));
		Files.createDirectories(tempDir.resolve("empty-sub"));

		assertEquals(expected, walkToCompletion(tempDir));
	}

	@Test
	void resumingAfterPausingPartWayThroughStillFindsEveryFileExactlyOnce() throws IOException {
		Set<String> expected = new HashSet<>();
		for (int dir = 1; dir <= 5; dir++) {
			Path sub = Files.createDirectories(tempDir.resolve("dir" + dir));
			for (int file = 1; file <= 5; file++) {
				expected.addAll(createFiles(sub, "file" + file + ".txt"));
			}
		}

		Set<String> found = new HashSet<>();
		try (ResumableFileWalker walker = new ResumableFileWalker(tempDir)) {
			// Pull a few entries, stop calling next() (as SourceFileScanner does when paused), then resume -
			// simulating exactly the "walk a bit, pause, walk some more" pattern this class exists for.
			for (int i = 0; i < 7; i++) {
				addNext(walker, found);
			}
			for (int i = 0; i < 10; i++) {
				addNext(walker, found);
			}
			ProcessCore.CachedFile next;
			while ((next = walker.next()) != null) {
				found.add(next.path().toString());
			}
		}

		assertEquals(expected, found);
	}

	@Test
	void closeClosesTheStillOpenDirectoryStreamsWithoutThrowing() throws IOException {
		Files.createDirectories(tempDir.resolve("sub"));
		ResumableFileWalker walker = new ResumableFileWalker(tempDir);
		walker.close();
	}

	private static void addNext(ResumableFileWalker walker, Set<String> found) throws IOException {
		ProcessCore.CachedFile next = walker.next();
		if (next != null) {
			found.add(next.path().toString());
		}
	}

	private static Set<String> walkToCompletion(Path root) throws IOException {
		Set<String> found = new HashSet<>();
		try (ResumableFileWalker walker = new ResumableFileWalker(root)) {
			ProcessCore.CachedFile next;
			while ((next = walker.next()) != null) {
				found.add(next.path().toString());
			}
		}
		return found;
	}

	private static Set<String> createFiles(Path dir, String... names) throws IOException {
		Set<String> paths = new HashSet<>();
		for (String name : names) {
			Path file = Files.writeString(dir.resolve(name), "content");
			paths.add(file.toString());
		}
		return paths;
	}
}
