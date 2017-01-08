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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.mifmi.commons4j.config.Config;
import org.mifmi.commons4j.config.OrderedProperties;
import org.mifmi.commons4j.config.PropertiesConfig;

public class AppConfig {
	
	private String configName;
	private String appName;
	private String groupName;
	
	private Config config;

	public AppConfig(String configName, String appName, String groupName) throws InvalidPropertiesFormatException, IOException {
		this.configName = configName;
		this.appName = appName;
		this.groupName = groupName;
		this.config = Config.loadFromAppConfig(configName, appName, groupName);
	}

	public String getConfigName() {
		return configName;
	}

	public String getAppName() {
		return appName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void load() throws InvalidPropertiesFormatException, IOException {
		this.config = Config.loadFromAppConfig(this.configName, this.appName, this.groupName);
	}

	public void store(String comments) throws IOException {
		this.config.storeToAppConfig(this.configName, this.appName, this.groupName, comments);
	}

	public void loadFromFile(Path filePath) throws InvalidPropertiesFormatException, IOException {
		try (BufferedReader br = Files.newBufferedReader(filePath, Charset.forName("UTF-8"))) {
			this.config = new PropertiesConfig(br);
		}
	}

	public void storeToFile(Path filePath, String comments) throws IOException {
		Properties props = new OrderedProperties();
		
		Enumeration<String> keys = this.config.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = this.config.getAsString(key);
			props.setProperty(key, value);
		}
		
		try (BufferedWriter bw = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"))) {
			props.store(bw, comments);
			bw.flush();
			bw.close();
		}
	}
	
	public String get(String key, String defaultValue) {
		return this.config.getAsString(key, defaultValue);
	}
	
	public int getInt(String key, int defaultValue) {
		return this.config.getAsInt(key, defaultValue);
	}
	
	public long getLong(String key, long defaultValue) {
		return this.config.getAsLong(key, defaultValue);
	}
	
	public double getDouble(String key, double defaultValue) {
		return this.config.getAsDouble(key, defaultValue);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		return this.config.getAsBoolean(key, defaultValue);
	}
	
	public <T extends Enum<T>> T getEnum(String key, Class<T> enumType, T defaultValue) {
		String value = get(key, null);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}
		return Enum.valueOf(enumType, value);
	}
	
	public Locale getLocale(String key, Locale defaultValue) {
		String value = get(key, null);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}
		return Locale.forLanguageTag(value);
	}
	
	public TimeZone getTimeZone(String key, TimeZone defaultValue) {
		String value = get(key, null);
		if (value == null || value.isEmpty()) {
			return defaultValue;
		}
		return TimeZone.getTimeZone(value);
	}
	
	public AppConfig set(String key, String value) {
		this.config.set(key, value);
		return this;
	}
	
	public AppConfig setInt(String key, int value) {
		this.config.set(key, value);
		return this;
	}
	
	public AppConfig setLong(String key, long value) {
		this.config.set(key, value);
		return this;
	}
	
	public AppConfig setDouble(String key, double value) {
		this.config.set(key, value);
		return this;
	}
	
	public AppConfig setBoolean(String key, boolean value) {
		this.config.set(key, value);
		return this;
	}
	
	public AppConfig setEnum(String key, Enum<?> value) {
		return set(key, (value == null) ? null : value.name());
	}
	
	public AppConfig setLocale(String key, Locale value) {
		return set(key, (value == null) ? null : value.toLanguageTag());
	}
	
	public AppConfig setTimeZone(String key, TimeZone value) {
		return set(key, (value == null) ? null : value.getID());
	}
}
