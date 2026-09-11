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
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * {@link IconSupport#tint} is what lets a single black-on-transparent icon asset match whatever text color
 * the caller passes (e.g. the current look and feel's Label.foreground) instead of maintaining a separate
 * hand-drawn variant per color; this pins down that it replaces RGB with the target color while leaving
 * alpha (the icon's shape, including full transparency) untouched.
 */
class IconSupportTest {
	@Test
	void recolorsAnOpaquePixelToTheTargetColor() {
		BufferedImage image = pixel(0, 0, 0, 255);

		BufferedImage tinted = IconSupport.tint(image, new Color(40, 120, 200));

		assertPixel(tinted, 40, 120, 200, 255);
	}

	@Test
	void leavesFullyTransparentPixelsTransparent() {
		BufferedImage image = pixel(0, 0, 0, 0);

		BufferedImage tinted = IconSupport.tint(image, Color.WHITE);

		assertEquals(0, new Color(tinted.getRGB(0, 0), true).getAlpha());
	}

	@Test
	void recolorsRegardlessOfTheSourcesOriginalColor() {
		BufferedImage image = pixel(255, 255, 255, 255);

		BufferedImage tinted = IconSupport.tint(image, Color.BLACK);

		assertPixel(tinted, 0, 0, 0, 255);
	}

	private static BufferedImage pixel(int r, int g, int b, int a) {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, new Color(r, g, b, a).getRGB());
		return image;
	}

	private static void assertPixel(BufferedImage image, int r, int g, int b, int a) {
		Color pixel = new Color(image.getRGB(0, 0), true);
		assertEquals(new Color(r, g, b, a), pixel);
	}
}
