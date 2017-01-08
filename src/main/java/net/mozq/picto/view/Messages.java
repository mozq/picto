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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "net.mozq.picto.view.messages"; //$NON-NLS-1$

	private static Locale locale = Locale.getDefault();
	private static ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);

	private Messages() {
	}
	
	public static void load(Locale locale) {
		Messages.locale = locale;
		Messages.bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
	}

	public static String getString(String key) {
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static String getString(String key, Object... params) {
		String message = getString(key);
		MessageFormat format = new MessageFormat(message, locale);
		return format.format(params);
	}
	
	public static String getString(Enum<?> enumKey) {
		return getString("enum." + enumKey.getClass().getSimpleName() + "." + enumKey.name());
	}
}
