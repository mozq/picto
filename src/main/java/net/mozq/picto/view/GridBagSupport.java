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

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Builds a single cell's {@link GridBagConstraints} as one fluent expression instead of the "declare a
 * variable, set 4-6 fields one at a time" block repeated at nearly every {@code add(component, constraints)}
 * call site across the option panels.
 */
final class GridBagSupport {
	private GridBagSupport() {
	}

	static Cell at(int gridx, int gridy) {
		return new Cell(gridx, gridy);
	}

	static final class Cell {
		private final GridBagConstraints constraints = new GridBagConstraints();

		private Cell(int gridx, int gridy) {
			constraints.gridx = gridx;
			constraints.gridy = gridy;
		}

		Cell gridwidth(int gridwidth) {
			constraints.gridwidth = gridwidth;
			return this;
		}

		Cell weightx(double weightx) {
			constraints.weightx = weightx;
			return this;
		}

		Cell anchor(int anchor) {
			constraints.anchor = anchor;
			return this;
		}

		Cell fill(int fill) {
			constraints.fill = fill;
			return this;
		}

		Cell insets(int top, int left, int bottom, int right) {
			constraints.insets = new Insets(top, left, bottom, right);
			return this;
		}

		GridBagConstraints build() {
			return constraints;
		}
	}
}
