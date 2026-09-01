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

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;

public class DestinationPathTemplate {

	private final String template;
	private TimeZone timeZone = TimeZone.getDefault();

	public DestinationPathTemplate(String template) {
		this.template = template;
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public String format(Function<String, Object> valueResolver) {
		StringBuilder text = new StringBuilder();

		for (int index = 0; index < template.length(); index++) {
			char current = template.charAt(index);
			if (current == '\\' && startsWith(index + 1, "${")) {
				text.append("${");
				index += 2;
			} else if (current == '$' && startsWith(index + 1, "{")) {
				int endIndex = findExpressionEnd(index + 2);
				if (endIndex < 0) {
					throw new IllegalArgumentException("Missing template expression terminator.");
				}
				text.append(formatExpression(template.substring(index + 2, endIndex), valueResolver));
				index = endIndex;
			} else {
				text.append(current);
			}
		}

		return text.toString();
	}

	private boolean startsWith(int index, String text) {
		return template.startsWith(text, index);
	}

	private int findExpressionEnd(int startIndex) {
		boolean escaped = false;
		boolean quoted = false;
		for (int index = startIndex; index < template.length(); index++) {
			char current = template.charAt(index);
			if (escaped) {
				escaped = false;
			} else if (current == '\\') {
				escaped = true;
			} else if (current == '"') {
				quoted = !quoted;
			} else if (!quoted && current == '}') {
				return index;
			}
		}
		return -1;
	}

	private String formatExpression(String expression, Function<String, Object> valueResolver) {
		int separatorIndex = findTopLevelSeparator(expression, '|');
		if (separatorIndex >= 0) {
			List<String> parts = splitTopLevel(expression, '|');
			Object value = resolveValue(unescape(parts.get(0).trim()), valueResolver);
			for (int index = 1; index < parts.size(); index++) {
				value = applyFilter(value, parts.get(index).trim());
			}
			return stringify(value);
		}

		separatorIndex = findTopLevelSeparator(expression, ':');
		if (separatorIndex >= 0) {
			return formatValue(
					resolveValue(unescape(expression.substring(0, separatorIndex).trim()), valueResolver),
					unescape(expression.substring(separatorIndex + 1).trim())
					);
		}

		separatorIndex = findTopLevelSeparator(expression, '?');
		if (separatorIndex >= 0) {
			return chooseValue(
					resolveValue(unescape(expression.substring(0, separatorIndex).trim()), valueResolver),
					parseChoiceArguments(expression.substring(separatorIndex + 1).trim())
					);
		}

		return stringify(resolveValue(unescape(expression.trim()), valueResolver));
	}

	private Object applyFilter(Object value, String filterExpression) {
		if (filterExpression.startsWith("format(") && filterExpression.endsWith(")")) {
			return formatValue(value, parseSingleArgument(filterExpression, "format"));
		}
		if (filterExpression.startsWith("choice(") && filterExpression.endsWith(")")) {
			return chooseValue(value, parseChoiceArguments(filterExpression.substring("choice(".length(), filterExpression.length() - 1)));
		}
		if (filterExpression.startsWith("default(") && filterExpression.endsWith(")")) {
			String text = stringify(value);
			return text.isEmpty() ? parseSingleArgument(filterExpression, "default") : text;
		}
		throw new IllegalArgumentException("Unknown template filter: " + filterExpression);
	}

	private Object resolveValue(String variableName, Function<String, Object> valueResolver) {
		return valueResolver.apply(variableName);
	}

	private String formatValue(Object value, String pattern) {
		if (value == null) {
			return "";
		}
		if (value instanceof Date) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(timeZone.toZoneId());
			return formatter.format(((Date)value).toInstant());
		}
		if (value instanceof Number) {
			return new DecimalFormat(pattern).format(value);
		}
		return stringify(value);
	}

	private String chooseValue(Object value, List<Choice> choices) {
		String text = stringify(value);
		String defaultValue = null;
		for (Choice choice : choices) {
			if ("default".equals(choice.value)) {
				defaultValue = choice.label;
			} else if (choice.value.equals(text)) {
				return choice.label;
			}
		}
		return defaultValue != null ? defaultValue : text;
	}

	private String stringify(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	private String parseSingleArgument(String filterExpression, String filterName) {
		String arguments = filterExpression.substring(filterName.length() + 1, filterExpression.length() - 1).trim();
		return unquote(arguments);
	}

	private List<Choice> parseChoiceArguments(String arguments) {
		List<String> parts = splitTopLevel(arguments, ',');
		List<Choice> choices = new ArrayList<>();
		for (String part : parts) {
			int separatorIndex = findTopLevelSeparator(part, '=');
			if (separatorIndex < 0) {
				throw new IllegalArgumentException("Missing choice separator: " + part);
			}
			choices.add(new Choice(
					unquote(part.substring(0, separatorIndex).trim()),
					unquote(part.substring(separatorIndex + 1).trim())
					));
		}
		return choices;
	}

	private List<String> splitTopLevel(String text, char separator) {
		List<String> parts = new ArrayList<>();
		boolean escaped = false;
		boolean quoted = false;
		int startIndex = 0;
		for (int index = 0; index < text.length(); index++) {
			char current = text.charAt(index);
			if (escaped) {
				escaped = false;
			} else if (current == '\\') {
				escaped = true;
			} else if (current == '"') {
				quoted = !quoted;
			} else if (!quoted && current == separator) {
				parts.add(text.substring(startIndex, index).trim());
				startIndex = index + 1;
			}
		}
		parts.add(text.substring(startIndex).trim());
		return parts;
	}

	private int findTopLevelSeparator(String text, char separator) {
		boolean escaped = false;
		boolean quoted = false;
		int depth = 0;
		for (int index = 0; index < text.length(); index++) {
			char current = text.charAt(index);
			if (escaped) {
				escaped = false;
			} else if (current == '\\') {
				escaped = true;
			} else if (current == '"') {
				quoted = !quoted;
			} else if (!quoted && current == '(') {
				depth++;
			} else if (!quoted && current == ')') {
				depth--;
			} else if (!quoted && depth == 0 && current == separator) {
				return index;
			}
		}
		return -1;
	}

	private String unquote(String text) {
		if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
			return unescape(text.substring(1, text.length() - 1));
		}
		return unescape(text);
	}

	private String unescape(String text) {
		StringBuilder unescaped = new StringBuilder();
		boolean escaped = false;
		for (int index = 0; index < text.length(); index++) {
			char current = text.charAt(index);
			if (escaped) {
				unescaped.append(current);
				escaped = false;
			} else if (current == '\\') {
				escaped = true;
			} else {
				unescaped.append(current);
			}
		}
		if (escaped) {
			unescaped.append('\\');
		}
		return unescaped.toString();
	}

	private static class Choice {
		private final String value;
		private final String label;

		Choice(String value, String label) {
			this.value = value;
			this.label = label;
		}
	}
}
