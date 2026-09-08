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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A depth-first file tree enumerator that can be paused (simply stop calling
 * {@link #next()}) and resumed later exactly where it left off, since the
 * traversal state (open directory streams and their iterators) is held as
 * instance state rather than on the call stack.
 */
public final class ResumableFileWalker implements Closeable {
	private final Deque<DirectoryStream<Path>> streams = new ArrayDeque<>();
	private final Deque<Iterator<Path>> iterators = new ArrayDeque<>();
	private boolean exhausted;

	public ResumableFileWalker(Path root) throws IOException {
		push(root);
	}

	private void push(Path dir) throws IOException {
		DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
		streams.push(stream);
		iterators.push(stream.iterator());
	}

	/**
	 * Returns the next file found, or {@code null} once the whole tree has
	 * been traversed.
	 */
	public ProcessCore.CachedFile next() throws IOException {
		while (!iterators.isEmpty()) {
			Iterator<Path> it = iterators.peek();
			if (!it.hasNext()) {
				iterators.pop();
				streams.pop().close();
				continue;
			}
			Path path = it.next();
			BasicFileAttributes attrs;
			try {
				attrs = Files.readAttributes(path, BasicFileAttributes.class);
			} catch (IOException _) {
				continue;
			}
			if (attrs.isDirectory()) {
				push(path);
				continue;
			}
			return new ProcessCore.CachedFile(path, attrs);
		}
		exhausted = true;
		return null;
	}

	public boolean isExhausted() {
		return exhausted;
	}

	@Override
	public void close() throws IOException {
		for (DirectoryStream<Path> stream : streams) {
			stream.close();
		}
	}
}
