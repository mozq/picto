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

public class ProcessStatus {

	private boolean endFindingFiles;
	private boolean stopRequests;
	private int currentProcessDataIndex;
	
	public ProcessStatus() {
		init();
	}
	
	public void init() {
		endFindingFiles = false;
		stopRequests = false;
		currentProcessDataIndex = -1;
	}

	public boolean isEndFindingFiles() {
		return endFindingFiles;
	}

	public void setEndFindingFiles(boolean endFindingFiles) {
		this.endFindingFiles = endFindingFiles;
	}

	public boolean isStopRequests() {
		return stopRequests;
	}

	public void setStopRequests(boolean stopRequests) {
		this.stopRequests = stopRequests;
	}

	public int getCurrentProcessDataIndex() {
		return currentProcessDataIndex;
	}

	public void setCurrentProcessDataIndex(int currentProcessDataIndex) {
		this.currentProcessDataIndex = currentProcessDataIndex;
	}
}
