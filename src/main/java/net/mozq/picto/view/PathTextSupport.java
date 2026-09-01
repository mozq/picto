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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

final class PathTextSupport {

	private PathTextSupport() {
	}

	static String shortPathText(String path, String emptyText) {
		String text = path.isEmpty() ? emptyText : path;
		return shortenPath(text);
	}

	static String abbreviateMiddle(String text, int width, JLabel label) {
		if (text == null || text.isEmpty() || width <= 0 || label.getFontMetrics(label.getFont()).stringWidth(text) <= width) {
			return text;
		}
		String ellipsis = "...";
		int ellipsisWidth = label.getFontMetrics(label.getFont()).stringWidth(ellipsis);
		if (ellipsisWidth >= width) {
			return ellipsis;
		}
		int left = text.length() / 2;
		int right = text.length() - left;
		while (left > 0 && right > 0) {
			String abbreviated = text.substring(0, left) + ellipsis + text.substring(text.length() - right);
			if (label.getFontMetrics(label.getFont()).stringWidth(abbreviated) <= width) {
				return abbreviated;
			}
			if (left >= right) {
				left--;
			} else {
				right--;
			}
		}
		return ellipsis;
	}

	private static String shortenPath(String path) {
		String text = path.strip();
		while (text.length() > 1 && isPathSeparator(text.charAt(text.length() - 1))) {
			text = text.substring(0, text.length() - 1);
		}
		char separator = text.indexOf('\\') >= 0 && text.indexOf('/') < 0 ? '\\' : '/';
		List<String> parts = pathParts(text);
		if (parts.size() <= 2) {
			return text;
		}

		String root = pathRoot(text, separator, parts);
		int prefixCount = root.startsWith("\\\\") ? 0 : Math.min(parts.size() - 1, root.isEmpty() ? 1 : 2);
		if (parts.size() <= prefixCount + 1) {
			return text;
		}

		StringBuilder shortened = new StringBuilder(root);
		for (int i = 0; i < prefixCount; i++) {
			if (shortened.length() > 0 && shortened.charAt(shortened.length() - 1) != separator) {
				shortened.append(separator);
			}
			shortened.append(parts.get(i));
		}
		if (shortened.length() > 0 && shortened.charAt(shortened.length() - 1) != separator) {
			shortened.append(separator);
		}
		shortened.append("...").append(separator).append(parts.get(parts.size() - 1));
		return shortened.toString();
	}

	private static List<String> pathParts(String path) {
		List<String> parts = new ArrayList<>();
		int start = 0;
		for (int i = 0; i <= path.length(); i++) {
			if (i == path.length() || isPathSeparator(path.charAt(i))) {
				if (i > start) {
					parts.add(path.substring(start, i));
				}
				start = i + 1;
			}
		}
		if (!parts.isEmpty() && parts.get(0).endsWith(":")) {
			parts.remove(0);
		}
		return parts;
	}

	private static String pathRoot(String path, char separator, List<String> parts) {
		if (path.length() >= 3 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':' && isPathSeparator(path.charAt(2))) {
			return path.substring(0, 2) + separator;
		}
		if (path.startsWith("\\\\") && parts.size() >= 2) {
			return "\\\\" + parts.get(0) + separator + parts.get(1) + separator;
		}
		if (!path.isEmpty() && isPathSeparator(path.charAt(0))) {
			return String.valueOf(separator);
		}
		return "";
	}

	private static boolean isPathSeparator(char c) {
		return c == '/' || c == '\\' || c == File.separatorChar;
	}
}
