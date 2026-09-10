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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.view.SourceFileScanner.MatchCountStatus;
import net.mozq.picto.view.SourceFileScanner.MatchCountStatus.State;

/**
 * {@link SourceFileScanner} backs the Source options match-count preview: it walks a folder on a
 * background thread, reports {@code SCANNING}/{@code PAUSED}/{@code EXACT} progress, and lets the walk be
 * paused ({@link SourceFileScanner#stop()}) and resumed ({@link SourceFileScanner#resume()}) without
 * restarting it. A real regression here (fixed in this app) had {@code updateTarget} unconditionally clear
 * a user-initiated pause on every condition change, silently resuming a scan the user had explicitly
 * stopped - covered below by {@code updateTargetPreservesAUserInitiatedPause}.
 *
 * <p>Tests that need to catch the scanner mid-walk do so by calling {@link SourceFileScanner#stop()} from
 * inside the very first status callback (which runs synchronously on the scanner's own background thread)
 * rather than racing a real sleep against the walk's speed, so they stop deterministically after exactly
 * one file regardless of how fast the walk happens to run.
 */
class SourceFileScannerTest {
	@TempDir
	Path tempDir;

	@Test
	void scansToExactWhenTheFolderIsFullyWalked() throws Exception {
		createFiles(5);
		StatusRecorder statuses = new StatusRecorder();
		SourceFileScanner scanner = newScanner(matchAll(), true, statuses);

		scanner.start();
		MatchCountStatus exact = statuses.awaitState(State.EXACT);

		assertEquals(5, exact.count());
		scanner.cancel();
	}

	@Test
	void stopPausesAfterTheCurrentCountAndResumeContinuesToExact() throws Exception {
		createFiles(20);
		StatusRecorder statuses = new StatusRecorder();
		AtomicReference<SourceFileScanner> scannerRef = new AtomicReference<>();
		AtomicBoolean stoppedOnce = new AtomicBoolean();
		SourceFileScanner scanner = new SourceFileScanner(tempDir, matchAll(), true, status -> {
			// Stop as soon as the very first progress callback arrives, from the callback itself (which
			// runs on the scanner's own pump thread) - this deterministically catches it after exactly one
			// file, with no dependency on how fast the walk happens to run.
			if (status.state() == State.SCANNING && stoppedOnce.compareAndSet(false, true)) {
				scannerRef.get().stop();
			}
			statuses.accept(status);
		});
		scannerRef.set(scanner);

		scanner.start();
		MatchCountStatus paused = statuses.awaitState(State.PAUSED);
		assertEquals(1, paused.count());

		scanner.resume();
		MatchCountStatus exact = statuses.awaitState(State.EXACT);
		assertEquals(20, exact.count());

		scanner.cancel();
	}

	@Test
	void updateTargetRecountsTheCacheWithoutDiskIo() throws Exception {
		createFiles(3, "keep");
		createFiles(2, "skip");
		StatusRecorder statuses = new StatusRecorder();
		SourceFileScanner scanner = newScanner(matchAll(), true, statuses);
		scanner.start();
		statuses.awaitState(State.EXACT);

		scanner.updateTarget(patternFilter("keep*"), true);
		MatchCountStatus updated = statuses.awaitState(State.EXACT, s -> s.count() == 3);

		assertEquals(3, updated.count());
		scanner.cancel();
	}

	@Test
	void updateTargetPreservesAUserInitiatedPause() throws Exception {
		createFiles(20);
		StatusRecorder statuses = new StatusRecorder();
		AtomicReference<SourceFileScanner> scannerRef = new AtomicReference<>();
		AtomicBoolean stoppedOnce = new AtomicBoolean();
		SourceFileScanner scanner = new SourceFileScanner(tempDir, matchAll(), true, status -> {
			if (status.state() == State.SCANNING && stoppedOnce.compareAndSet(false, true)) {
				scannerRef.get().stop();
			}
			statuses.accept(status);
		});
		scannerRef.set(scanner);
		scanner.start();
		statuses.awaitState(State.PAUSED);

		// A condition change (e.g. the user editing an unrelated field) must not silently resume a scan
		// the user explicitly stopped.
		scanner.updateTarget(matchAll(), true);
		MatchCountStatus stillPaused = statuses.awaitState(State.PAUSED, s -> true);

		assertEquals(State.PAUSED, stillPaused.state());
		scanner.cancel();
	}

	@Test
	void excludesFilesInSubfoldersWhenNotIncludingSubfolders() throws Exception {
		Files.writeString(tempDir.resolve("top.txt"), "content");
		Path sub = Files.createDirectories(tempDir.resolve("sub"));
		Files.writeString(sub.resolve("nested.txt"), "content");

		StatusRecorder statuses = new StatusRecorder();
		SourceFileScanner scanner = newScanner(matchAll(), false, statuses);
		scanner.start();

		MatchCountStatus exact = statuses.awaitState(State.EXACT);
		assertEquals(1, exact.count());
		scanner.cancel();
	}

	@Test
	void includesFilesInSubfoldersWhenIncludingSubfolders() throws Exception {
		Files.writeString(tempDir.resolve("top.txt"), "content");
		Path sub = Files.createDirectories(tempDir.resolve("sub"));
		Files.writeString(sub.resolve("nested.txt"), "content");

		StatusRecorder statuses = new StatusRecorder();
		SourceFileScanner scanner = newScanner(matchAll(), true, statuses);
		scanner.start();

		MatchCountStatus exact = statuses.awaitState(State.EXACT);
		assertEquals(2, exact.count());
		scanner.cancel();
	}

	private SourceFileScanner newScanner(PictoPathFilter filter, boolean includeSubfolders, StatusRecorder statuses)
			throws IOException {
		return new SourceFileScanner(tempDir, filter, includeSubfolders, statuses);
	}

	private void createFiles(int count) throws IOException {
		createFiles(count, "file");
	}

	private void createFiles(int count, String prefix) throws IOException {
		for (int i = 1; i <= count; i++) {
			Files.writeString(tempDir.resolve(prefix + i + ".txt"), "content");
		}
	}

	private PictoPathFilter matchAll() {
		return new PictoPathFilter();
	}

	private PictoPathFilter patternFilter(String globPattern) {
		return new PictoPathFilter().setPathPattern(globPattern, tempDir, false);
	}

	/** Collects status callbacks off the scanner's background thread and lets a test wait for a specific one. */
	private static final class StatusRecorder implements Consumer<MatchCountStatus> {
		private final BlockingQueue<MatchCountStatus> queue = new ArrayBlockingQueue<>(1000);
		private final List<MatchCountStatus> received = Collections.synchronizedList(new ArrayList<>());

		@Override
		public void accept(MatchCountStatus status) {
			received.add(status);
			queue.add(status);
		}

		MatchCountStatus awaitState(State state) throws InterruptedException {
			return awaitState(state, s -> true);
		}

		MatchCountStatus awaitState(State state, Predicate<MatchCountStatus> also) throws InterruptedException {
			long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
			while (System.nanoTime() < deadline) {
				MatchCountStatus status = queue.poll(200, TimeUnit.MILLISECONDS);
				if (status != null && status.state() == state && also.test(status)) {
					return status;
				}
			}
			fail("timed out waiting for state " + state + "; received: " + received);
			throw new AssertionError("unreachable");
		}
	}
}
