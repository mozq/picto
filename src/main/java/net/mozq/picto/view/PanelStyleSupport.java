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

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

/**
 * The rounded, shaded box chrome used for an options panel's expanded fields - and the small color math
 * behind it, reused by {@link MainFrame} for a couple of unrelated hover-background computations.
 */
final class PanelStyleSupport {
	private static final int OPTIONS_BODY_PADDING = 12;
	private static final int OPTIONS_BODY_ARC = 12;
	private static final int OPTIONS_BODY_SHADE_LIGHT = -11;
	private static final int OPTIONS_BODY_SHADE_DARK = -8;
	private static final int MID_BRIGHTNESS = 128;

	private PanelStyleSupport() {
	}

	static void stylizeOptionsBody(JPanel optionsBody) {
		stylizeOptionsBody(optionsBody, OPTIONS_BODY_PADDING);
	}

	static void stylizeOptionsBody(JPanel optionsBody, int topPadding) {
		Color panelBackground = color("Panel.background", new Color(0xf2f2f2));
		optionsBody.setOpaque(true);
		optionsBody.setBackground(shade(panelBackground));
		optionsBody.setBorder(BorderFactory.createEmptyBorder(
				topPadding, OPTIONS_BODY_PADDING, OPTIONS_BODY_PADDING, OPTIONS_BODY_PADDING));
		optionsBody.putClientProperty(FlatClientProperties.STYLE, "arc: " + OPTIONS_BODY_ARC);
	}

	static Color shade(Color base) {
		boolean isLight = (base.getRed() + base.getGreen() + base.getBlue()) / 3 >= MID_BRIGHTNESS;
		int delta = isLight ? OPTIONS_BODY_SHADE_LIGHT : OPTIONS_BODY_SHADE_DARK;
		return new Color(
				clamp(base.getRed() + delta),
				clamp(base.getGreen() + delta),
				clamp(base.getBlue() + delta));
	}

	static Color color(String key, Color fallback) {
		Color color = UIManager.getColor(key);
		return color != null ? color : fallback;
	}

	private static int clamp(int value) {
		return Math.max(0, Math.min(255, value));
	}
}
