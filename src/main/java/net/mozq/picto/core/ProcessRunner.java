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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import net.mozq.picto.enums.ProcessDataStatus;

public final class ProcessRunner {

	private static final ProcessData END_OF_QUEUE = new ProcessData();

	private final ProcessCondition processCondition;
	private final Function<ProcessData, ProcessDataStatus> overwriteConfirm;
	private final Listener listener;
	private final BlockingQueue<ProcessData> processQueue = new LinkedBlockingQueue<>();
	private final AtomicBoolean started = new AtomicBoolean(false);
	private final AtomicBoolean stopRequested = new AtomicBoolean(false);

	public ProcessRunner(
			ProcessCondition processCondition,
			Function<ProcessData, ProcessDataStatus> overwriteConfirm,
			Listener listener) {
		this.processCondition = processCondition;
		this.overwriteConfirm = overwriteConfirm;
		this.listener = listener;
	}

	public void start() {
		if (!started.compareAndSet(false, true)) {
			throw new IllegalStateException("ProcessRunner already started.");
		}
		new Thread(this::findFiles, "Picto file finder").start();
		new Thread(this::processFiles, "Picto file processor").start();
	}

	public void stop() {
		if (stopRequested.compareAndSet(false, true)) {
			processQueue.offer(END_OF_QUEUE);
		}
	}

	private boolean isStopRequested() {
		return stopRequested.get();
	}

	private void findFiles() {
		try {
			ProcessCore.findFiles(processCondition, this::enqueueProcessData, this::isStopRequested);
		} catch (Exception e) {
			listener.findingFailed(e);
			stop();
		} finally {
			finishFinding();
		}
	}

	private void finishFinding() {
		if (!isStopRequested()) {
			processQueue.offer(END_OF_QUEUE);
		}
	}

	private void enqueueProcessData(ProcessData processData) {
		if (isStopRequested()) {
			return;
		}
		listener.processDataFound(processData);
		processQueue.offer(processData);
	}

	private void processFiles() {
		try {
			ProcessCore.processFiles(
					processCondition,
					this::takeProcessData,
					this::processDataUpdated,
					overwriteConfirm,
					this::isStopRequested
					);
		} catch (Exception e) {
			listener.processingFailed(e);
			stop();
		} finally {
			stopRequested.set(true);
			listener.completed();
		}
	}

	private ProcessData takeProcessData() {
		try {
			ProcessData processData = processQueue.take();
			return processData == END_OF_QUEUE ? null : processData;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	private void processDataUpdated(int index, ProcessData processData) {
		listener.processDataUpdated(index);
		if (processData.getStatus() == ProcessDataStatus.Error
				|| processData.getStatus() == ProcessDataStatus.Terminated) {
			stop();
		}
	}

	public interface Listener {
		void processDataFound(ProcessData processData);

		void processDataUpdated(int index);

		void findingFailed(Exception e);

		void processingFailed(Exception e);

		void completed();
	}
}
