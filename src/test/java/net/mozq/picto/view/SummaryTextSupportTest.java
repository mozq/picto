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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;

import org.junit.jupiter.api.Test;

import net.mozq.picto.enums.OperationType;

/** The pure text-formatting primitives behind the run/options/changes summaries; no Swing rendering. */
class SummaryTextSupportTest {

	@Test
	void runSummaryOmitsDestinationForOverwrite() {
		ProcessConditionInput input = new ProcessConditionInput();
		input.operationType = OperationType.Overwrite;
		input.srcFolder = "/src";
		input.destFolder = "/dest";

		String summary = SummaryTextSupport.runSummary(input);

		assertTrue(summary.startsWith(OperationType.Overwrite + ":"));
		assertTrue(summary.contains("/src"));
		assertFalse(summary.contains("->"));
		assertFalse(summary.contains("/dest"));
	}

	@Test
	void runSummaryIncludesDestinationForCopyAndMove() {
		ProcessConditionInput input = new ProcessConditionInput();
		input.operationType = OperationType.Copy;
		input.srcFolder = "/src";
		input.destFolder = "/dest";

		String summary = SummaryTextSupport.runSummary(input);

		assertTrue(summary.contains("/src"));
		assertTrue(summary.contains("->"));
		assertTrue(summary.contains("/dest"));
	}

	@Test
	void fieldTextTrimsAndNeverReturnsNull() {
		assertEquals("hello", SummaryTextSupport.fieldText(new JTextField("  hello  ")));
		assertEquals("", SummaryTextSupport.fieldText(new JTextField("   ")));
		assertEquals("", SummaryTextSupport.fieldText(new JTextField()));
	}

	@Test
	void dateFieldTextDelegatesToDateTimeTextCompact() {
		assertEquals("", SummaryTextSupport.dateFieldText(""));
		assertEquals("2026/09/08 10:20", SummaryTextSupport.dateFieldText("2026/09/08 10:20:__"));
	}

	@Test
	void rangeTextJoinsOnlyTheSidesThatHaveAValue() {
		assertEquals("2026/01/01 - 2026/12/31", SummaryTextSupport.rangeText("2026/01/01", "2026/12/31"));
		assertEquals("2026/12/31", SummaryTextSupport.rangeText("", "2026/12/31"));
		assertEquals("2026/01/01", SummaryTextSupport.rangeText("2026/01/01", ""));
		assertEquals("", SummaryTextSupport.rangeText("", ""));
	}

	@Test
	void summaryItemJoinsLabelAndValue() {
		assertEquals("Size: 10MB", SummaryTextSupport.summaryItem("Size", "10MB"));
	}

	@Test
	void checkedItemPrefixesTheCheckmark() {
		assertEquals(SummaryTextSupport.CHECKED_ITEM_PREFIX + "Include subfolders", SummaryTextSupport.checkedItem("Include subfolders"));
	}

	@Test
	void joinOptionsSummaryJoinsWithASlashAndHandlesEmpty() {
		assertEquals("A / B", SummaryTextSupport.joinOptionsSummary(List.of("A", "B")));
		assertEquals("", SummaryTextSupport.joinOptionsSummary(List.of()));
	}

	@Test
	void addAdjustmentAmountIgnoresNullOrEmptyText() {
		List<String> amounts = new ArrayList<>();
		SummaryTextSupport.addAdjustmentAmount(amounts, null, "Y");
		SummaryTextSupport.addAdjustmentAmount(amounts, "", "Y");
		assertTrue(amounts.isEmpty());
	}

	@Test
	void addAdjustmentAmountAppendsTheSuffixToAParsedNumber() {
		List<String> amounts = new ArrayList<>();
		SummaryTextSupport.addAdjustmentAmount(amounts, "05", "Y");
		assertEquals(List.of("5Y"), amounts);
	}

	@Test
	void addAdjustmentAmountFallsBackToTheRawTextWhenNotAValidNumber() {
		List<String> amounts = new ArrayList<>();
		SummaryTextSupport.addAdjustmentAmount(amounts, "abc", "M");
		assertEquals(List.of("abcM"), amounts);
	}
}
