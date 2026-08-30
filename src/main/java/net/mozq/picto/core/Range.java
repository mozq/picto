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

public final class Range<T extends Comparable<? super T>> {
	private final T from;
	private final T to;

	public Range(T from, T to) {
		this.from = from;
		this.to = to;
	}

	public static <T extends Comparable<? super T>> Range<T> of(T from, T to) {
		if (from == null && to == null) {
			return null;
		}
		return new Range<>(from, to);
	}

	public T getFrom() {
		return from;
	}

	public T getTo() {
		return to;
	}

	public boolean contains(T value) {
		if (value == null) {
			return false;
		}
		if (from != null && value.compareTo(from) < 0) {
			return false;
		}
		return to == null || value.compareTo(to) <= 0;
	}
}
