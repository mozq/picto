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

public final class FileNameSupport {

	private FileNameSupport() {
	}

	public static String baseName(String filename) {
		int extensionSeparator = extensionSeparator(filename);
		if (extensionSeparator < 0) {
			return filename;
		}
		return filename.substring(0, extensionSeparator);
	}

	public static String extension(String filename) {
		int extensionSeparator = extensionSeparator(filename);
		if (extensionSeparator < 0 || extensionSeparator == filename.length() - 1) {
			return "";
		}
		return filename.substring(extensionSeparator + 1);
	}

	private static int extensionSeparator(String filename) {
		int separator = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
		int extensionSeparator = filename.lastIndexOf('.');
		if (extensionSeparator <= separator || extensionSeparator < 1) {
			return -1;
		}
		return extensionSeparator;
	}
}
