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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.mozq.nanotemplate.NanoTemplate;
import net.mozq.picto.enums.ExistingFileMethod;
import net.mozq.picto.enums.OperationType;
import net.mozq.picto.enums.ProcessDataStatus;

class ProcessRunnerTest {

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@TempDir
	Path tempDir;

	@Test
	void processesFoundFilesThroughQueue() throws Exception {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		Files.writeString(srcRoot.resolve("source.txt"), "source");
		CountDownLatch completed = new CountDownLatch(1);
		AtomicInteger foundCount = new AtomicInteger();
		AtomicInteger updatedCount = new AtomicInteger();

		ProcessRunner runner = new ProcessRunner(
				condition(srcRoot, destRoot),
				ignored -> ProcessDataStatus.Terminated,
				new TestListener(completed) {
					@Override
					public void processDataFound(ProcessData processData) {
						foundCount.incrementAndGet();
					}

					@Override
					public void processDataUpdated(int index) {
						updatedCount.incrementAndGet();
					}
				});

		runner.start();

		assertTrue(completed.await(5, TimeUnit.SECONDS), "ProcessRunner did not complete.");
		assertEquals(1, foundCount.get());
		assertEquals(2, updatedCount.get());
		assertEquals("source", Files.readString(destRoot.resolve("source.txt")));
	}

	@Test
	void completesProcessingThreadWhenFindingFails() throws Exception {
		Path srcRoot = tempDir.resolve("missing");
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		CountDownLatch completed = new CountDownLatch(1);
		AtomicReference<Exception> findingFailure = new AtomicReference<>();

		ProcessRunner runner = new ProcessRunner(
				condition(srcRoot, destRoot),
				ignored -> ProcessDataStatus.Terminated,
				new TestListener(completed) {
					@Override
					public void findingFailed(Exception e) {
						findingFailure.set(e);
					}
				});

		runner.start();

		assertTrue(completed.await(5, TimeUnit.SECONDS), "ProcessRunner did not complete after finding failed.");
		assertTrue(findingFailure.get() instanceof NoSuchFileException);
		assertFalse(Files.exists(destRoot.resolve("source.txt")));
	}

	@Test
	void rejectsMultipleStarts() throws Exception {
		Path srcRoot = Files.createDirectories(tempDir.resolve("src"));
		Path destRoot = Files.createDirectories(tempDir.resolve("dest"));
		CountDownLatch completed = new CountDownLatch(1);
		ProcessRunner runner = new ProcessRunner(
				condition(srcRoot, destRoot),
				ignored -> ProcessDataStatus.Terminated,
				new TestListener(completed));

		runner.start();

		assertThrows(IllegalStateException.class, runner::start);
		assertTrue(completed.await(5, TimeUnit.SECONDS), "ProcessRunner did not complete.");
	}

	private ProcessCondition condition(Path srcRoot, Path destRoot) {
		ProcessCondition condition = new ProcessCondition();
		condition.setTimeZone(UTC);
		condition.setSrcRootPath(srcRoot);
		condition.setDestRootPath(destRoot);
		condition.setPathFilter(new PictoPathFilter());
		condition.setDestSubPathTemplate(new NanoTemplate("${FileName}").timeZone(UTC));
		condition.setDepth(Integer.MAX_VALUE);
		condition.setOperationType(OperationType.Copy);
		condition.setExistingFileMethod(ExistingFileMethod.Overwrite);
		return condition;
	}

	private static class TestListener implements ProcessRunner.Listener {
		private final CountDownLatch completed;

		TestListener(CountDownLatch completed) {
			this.completed = completed;
		}

		@Override
		public void processDataFound(ProcessData processData) {
		}

		@Override
		public void processDataUpdated(int index) {
		}

		@Override
		public void findingFailed(Exception e) {
		}

		@Override
		public void processingFailed(Exception e) {
		}

		@Override
		public void completed() {
			completed.countDown();
		}
	}
}
