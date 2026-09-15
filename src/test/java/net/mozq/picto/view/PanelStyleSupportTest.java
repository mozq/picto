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

import java.awt.Color;

import org.junit.jupiter.api.Test;

/** The color math behind the options-body chrome; no Swing rendering. */
class PanelStyleSupportTest {

	@Test
	void shadeDarkensALightBaseColorByTheLightDelta() {
		assertEquals(new Color(231, 231, 231), PanelStyleSupport.shade(new Color(242, 242, 242)));
	}

	@Test
	void shadeDarkensADarkBaseColorByTheDarkDelta() {
		assertEquals(new Color(24, 24, 24), PanelStyleSupport.shade(new Color(32, 32, 32)));
	}

	@Test
	void shadeTreatsExactlyMidBrightnessAsLight() {
		// Average of 128 is the light/dark boundary itself; it must take the light (-11) delta, not dark (-8).
		assertEquals(new Color(117, 117, 117), PanelStyleSupport.shade(new Color(128, 128, 128)));
	}

	@Test
	void shadeClampsInsteadOfGoingBelowZero() {
		assertEquals(new Color(0, 0, 0), PanelStyleSupport.shade(new Color(5, 5, 5)));
	}

	@Test
	void colorFallsBackWhenTheLookAndFeelHasNoSuchKey() {
		Color fallback = new Color(1, 2, 3);
		assertEquals(fallback, PanelStyleSupport.color("Picto.NoSuchColorKey", fallback));
	}
}
