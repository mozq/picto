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
package net.mozq.picto;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.mifmi.commons4j.io.file.FileUtilz;
import org.mifmi.commons4j.util.EnvUtilz;

public class App {
	public static final String GROUP_NAME = "Mozq";
	public static final String APP_NAME = "Picto";
	public static final String CONFIG_FILE_NAME = "settings.properties";
	public static final String WARNS_FILE_NAME = "warns.log";
	public static final String ERRORS_FILE_NAME = "errors.log";
	public static final Path WARNS_FILE_PATH = FileUtilz.getPath(EnvUtilz.getAppDataDir(), GROUP_NAME, APP_NAME, WARNS_FILE_NAME);
	public static final Path ERRORS_FILE_PATH = FileUtilz.getPath(EnvUtilz.getAppDataDir(), GROUP_NAME, APP_NAME, ERRORS_FILE_NAME);
	
	private static AppConfig config = null;

	private App() {
		// NOP
	}
	
	public static void init() throws IOException {
		// Load config
		config = new AppConfig(CONFIG_FILE_NAME, APP_NAME, GROUP_NAME);
		
		// Clear old logs
		Files.deleteIfExists(WARNS_FILE_PATH);
		Files.deleteIfExists(ERRORS_FILE_PATH);
	}

	public static AppConfig config() {
		return config;
	}

	public static void handleWarn(String message, Throwable throwable) {
		writeLog(WARNS_FILE_PATH, message, throwable);
	}

	public static void handleError(String message, Throwable throwable) {
		writeLog(ERRORS_FILE_PATH, message, throwable);
	}

	private static void writeLog(Path filePath, String message, Throwable throwable) {
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(
				filePath,
				StandardOpenOption.WRITE,
				StandardOpenOption.CREATE_NEW
				))) {
			writer.println("--");
			writer.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
			if (message == null) {
				if (throwable != null) {
					writer.println(throwable.getMessage());
				}
			} else {
				writer.println(message);
			}
			if (throwable != null) {
				throwable.printStackTrace(writer);
			}
		} catch (IOException e) {
			// NOP
		}
	}
}
