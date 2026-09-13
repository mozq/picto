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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import net.mozq.picto.core.PictoPathFilter;
import net.mozq.picto.core.ProcessCore;
import net.mozq.picto.core.ResumableFileWalker;

/**
 * Maintains a resumable, incrementally-growing cache of every file under one
 * source folder and reports how many currently match a (changeable) filter,
 * without re-scanning the folder from scratch each time the filter changes.
 * The scan keeps going on its own until the folder is fully walked, unless
 * the user pauses it via {@link #stop()}.
 */
final class SourceFileScanner {
	private static final long IDLE_POLL_MS = 100;
	private static final long STATUS_THROTTLE_MS = 100;
	private static final long STATUS_THROTTLE_NANOS = TimeUnit.MILLISECONDS.toNanos(STATUS_THROTTLE_MS);

	private final Path srcFolder;
	private final List<ProcessCore.CachedFile> cache = new ArrayList<>();
	private final ResumableFileWalker walker;
	private final AtomicBoolean cancelled = new AtomicBoolean();
	private boolean exhausted;
	private boolean stopped;
	// Pump-thread-only state (no synchronization needed: pump() is the sole writer/reader of these two).
	private boolean statusPosted;
	private long lastStatusPostNanos;

	private volatile PictoPathFilter targetFilter;
	private volatile boolean targetIncludeSubfolders;
	private int currentMatches;

	private final Consumer<MatchCountStatus> statusListener;

	SourceFileScanner(
			Path srcFolder,
			PictoPathFilter initialFilter,
			boolean initialIncludeSubfolders,
			Consumer<MatchCountStatus> statusListener
			) throws IOException {
		this.srcFolder = srcFolder;
		this.walker = new ResumableFileWalker(srcFolder);
		this.targetFilter = initialFilter;
		this.targetIncludeSubfolders = initialIncludeSubfolders;
		this.statusListener = statusListener;
	}

	Path srcFolder() {
		return srcFolder;
	}

	void start() {
		Thread.ofVirtual().name("Picto source scan").start(this::pump);
	}

	void cancel() {
		cancelled.set(true);
	}

	/**
	 * Re-filters the existing cache against a changed condition (no disk I/O). Does not touch a user-initiated
	 * pause: if the scan is currently stopped, it stays stopped (just with the count updated for the new
	 * condition) until the user explicitly resumes it via {@link #resume()}.
	 */
	synchronized void updateTarget(PictoPathFilter pathFilter, boolean includeSubfolders) {
		targetFilter = pathFilter;
		targetIncludeSubfolders = includeSubfolders;
		currentMatches = countCache(pathFilter, includeSubfolders);
		statusListener.accept(status());
	}

	/** Pauses the walk; the count so far is kept and shown with a trailing "+". */
	synchronized void stop() {
		stopped = true;
		statusListener.accept(status());
	}

	/** Resumes a paused walk. */
	synchronized void resume() {
		stopped = false;
		statusListener.accept(status());
	}

	private void pump() {
		while (!cancelled.get()) {
			if (isSatisfiedOrDone()) {
				if (isExhausted()) {
					return;
				}
				sleep(IDLE_POLL_MS);
				continue;
			}

			ProcessCore.CachedFile next;
			try {
				next = walker.next();
			} catch (IOException _) {
				markExhausted();
				continue;
			}
			if (next == null) {
				markExhausted();
				continue;
			}

			synchronized (this) {
				cache.add(next);
				if (matches(next, targetFilter, targetIncludeSubfolders)) {
					currentMatches++;
				}
			}
			postStatusIfDue();
		}
	}

	/**
	 * Posts progress at most once every {@link #STATUS_THROTTLE_MS} while scanning, instead of once per file
	 * found: on a large folder, posting unthrottled floods the EDT (each post is wrapped in an
	 * invokeLater by the caller) with far more UI updates than a human can perceive, competing with
	 * keystroke and other UI event handling. The first post is never throttled, so a caller that reacts to
	 * the very first progress update (e.g. to pause the scan) still sees one promptly. Terminal/user-driven
	 * transitions (exhaustion, stop/resume, a condition change) always post immediately via their own
	 * statusListener.accept(status()) calls, bypassing this throttle entirely.
	 */
	private void postStatusIfDue() {
		long now = System.nanoTime();
		if (!statusPosted || now - lastStatusPostNanos >= STATUS_THROTTLE_NANOS) {
			statusPosted = true;
			lastStatusPostNanos = now;
			statusListener.accept(status());
		}
	}

	private synchronized boolean isSatisfiedOrDone() {
		return stopped || exhausted;
	}

	private synchronized boolean isExhausted() {
		return exhausted;
	}

	private synchronized void markExhausted() {
		exhausted = true;
		statusListener.accept(status());
	}

	private int countCache(PictoPathFilter pathFilter, boolean includeSubfolders) {
		int matchCount = 0;
		for (ProcessCore.CachedFile file : cache) {
			if (matches(file, pathFilter, includeSubfolders)) {
				matchCount++;
			}
		}
		return matchCount;
	}

	private boolean matches(ProcessCore.CachedFile file, PictoPathFilter pathFilter, boolean includeSubfolders) {
		if (!includeSubfolders && srcFolder.relativize(file.path()).getNameCount() > 1) {
			return false;
		}
		try {
			return pathFilter.accept(file.path(), file.attrs());
		} catch (IOException _) {
			return false;
		}
	}

	private synchronized MatchCountStatus status() {
		if (exhausted) {
			return new MatchCountStatus(currentMatches, MatchCountStatus.State.EXACT);
		}
		if (stopped) {
			return new MatchCountStatus(currentMatches, MatchCountStatus.State.PAUSED);
		}
		return new MatchCountStatus(currentMatches, MatchCountStatus.State.SCANNING);
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	record MatchCountStatus(int count, State state) {
		enum State { SCANNING, PAUSED, EXACT }
	}
}
