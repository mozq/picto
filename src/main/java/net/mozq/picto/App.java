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
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import net.mozq.appsettings.AppSettings;

public class App {
	public static final String GROUP_NAME = "mozq";
	public static final String APP_NAME = "picto";
	public static final String SETTINGS_FILE_NAME = "settings.conf";
	public static final String HISTORY_FILE_NAME = "history.conf";
	public static final String WARNS_FILE_NAME = "warns.log";
	public static final String ERRORS_FILE_NAME = "errors.log";

	private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
			.withZone(ZoneId.systemDefault());

	private static AppSettings settings = null;
	private static AppSettings history = null;
	private static AppSettingsMigration.Result settingsMigrationResult = AppSettingsMigration.Result.none();
	private static Path warnsFilePath = null;
	private static Path errorsFilePath = null;

	private App() {
		// NOP
	}

	public static void init() throws IOException {
		// Load settings
		settings = AppSettings.of(GROUP_NAME, APP_NAME, SETTINGS_FILE_NAME);
		settingsMigrationResult = AppSettingsMigration.migrateIfNeeded(settings);
		if (!settingsMigrationResult.migrated()) {
			settings.load();
		}

		// Load history (e.g. recently used folders) - kept separate from settings.conf since it's
		// recorded usage data rather than a user-chosen preference.
		history = AppSettings.of(GROUP_NAME, APP_NAME, HISTORY_FILE_NAME);
		history.load();

		Path appDirectory = settings.path().getParent();
		warnsFilePath = appDirectory.resolve(WARNS_FILE_NAME);
		errorsFilePath = appDirectory.resolve(ERRORS_FILE_NAME);

		// Clear old logs
		Files.deleteIfExists(warnsFilePath);
		Files.deleteIfExists(errorsFilePath);
	}

	public static AppSettings settings() {
		return settings;
	}

	public static AppSettings history() {
		return history;
	}

	public static void deleteMigratedLegacySettingsIfNeeded() {
		AppSettingsMigration.deleteLegacyFiles(settingsMigrationResult);
		settingsMigrationResult = AppSettingsMigration.Result.none();
	}

	public static void handleWarn(String message, Throwable throwable) {
		writeLog(warnsFilePath, message, throwable);
	}

	public static void handleError(String message, Throwable throwable) {
		writeLog(errorsFilePath, message, throwable);
	}

	private static synchronized void writeLog(Path filePath, String message, Throwable throwable) {
		if (filePath == null) {
			return;
		}
		try {
			Files.createDirectories(filePath.getParent());
		} catch (IOException _) {
			return;
		}
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(
				filePath,
				StandardOpenOption.CREATE,
				StandardOpenOption.APPEND
				))) {
			writer.println("--");
			writer.println(LOG_DATE_FORMATTER.format(Instant.now()));
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
		} catch (IOException _) {
			// NOP
		}
	}
}
