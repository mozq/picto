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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Loads a monochrome (black-on-transparent) icon and recolors it to match a caller-supplied color, instead
 * of maintaining separate hand-drawn variants per theme/foreground color. Only the icons authored this way
 * (currently the Run-button status indicators, tinted to the current look and feel's text color) go through
 * this loader; the colored per-row status icons in {@link ProcessDialog} are unaffected by theme and keep
 * loading directly.
 */
final class IconSupport {
	private IconSupport() {
	}

	static ImageIcon loadIcon(String resourcePath, int size, Color color) {
		BufferedImage image = tint(readImage(resourcePath), color);
		Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}

	/**
	 * Recolors every pixel to {@code color} while keeping the source's alpha (its shape) exactly as-is, via
	 * the standard "draw the shape, then flood it with a solid color under SrcIn compositing" technique.
	 * Package-private so it can be exercised directly against a small synthetic image, without needing a
	 * real resource.
	 */
	static BufferedImage tint(BufferedImage source, Color color) {
		BufferedImage tinted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tinted.createGraphics();
		try {
			g.drawImage(source, 0, 0, null);
			g.setComposite(AlphaComposite.SrcIn);
			g.setColor(color);
			g.fillRect(0, 0, source.getWidth(), source.getHeight());
		} finally {
			g.dispose();
		}
		return tinted;
	}

	private static BufferedImage readImage(String resourcePath) {
		URL url = IconSupport.class.getClassLoader().getResource(resourcePath);
		try {
			return ImageIO.read(url);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
