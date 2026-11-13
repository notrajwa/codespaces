package com.birowo.client.platform.screen;

import net.minecraft.client.gui.DrawContext;

import com.birowo.client.core.gui.GuiColors;
import com.birowo.client.platform.render.GuiGeometry;

/**
 * The drawing primitives the interface is built from.
 *
 * <p>The shapes are drawn as a mesh of triangles rather than as a picture of squares, so that the
 * edge of a shape follows the curve it is meant to follow instead of stepping along the pixels.
 * The mesh of a shape is one piece of work for the game however many triangles it is made of, so
 * the interface stays quick as well as smooth.
 *
 * <p>Nothing here allocates while it draws, because the interface is drawn again on every frame.
 */
public final class GuiShapes {
	/**
	 * How many layers the soft shadow of a panel is made of.
	 */
	public static final int SHADOW_LAYERS = 3;

	private GuiShapes() {
	}

	/**
	 * Fills a rectangle.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param color   the ARGB colour to fill with
	 */
	public static void fillRect(DrawContext context, int x, int y, int width, int height, int color) {
		GuiGeometry.fillRect(context, x, y, width, height, color);
	}

	/**
	 * Fills a rectangle with rounded corners.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param radius  the corner radius, in scaled GUI pixels
	 * @param color   the ARGB colour to fill with
	 */
	public static void fillRounded(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		GuiGeometry.fillRounded(context, x, y, width, height, radius, color);
	}

	/**
	 * Fills a rectangle with rounded corners, with a colour that fades from the top to the bottom.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param radius  the corner radius, in scaled GUI pixels
	 * @param top     the colour at the top, as an ARGB colour
	 * @param bottom  the colour at the bottom, as an ARGB colour
	 */
	public static void fillRoundedGradient(DrawContext context, int x, int y, int width, int height, int radius,
			int top, int bottom) {
		GuiGeometry.fillRoundedGradient(context, x, y, width, height, radius, top, bottom);
	}

	/**
	 * Fills a rectangle whose bottom corners are square and whose top corners are rounded, with a
	 * colour that fades from the top to the bottom.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param radius  the corner radius of the top corners, in scaled GUI pixels
	 * @param top     the colour at the top, as an ARGB colour
	 * @param bottom  the colour at the bottom, as an ARGB colour
	 */
	public static void fillRoundedTopGradient(DrawContext context, int x, int y, int width, int height, int radius,
			int top, int bottom) {
		if (width <= 0 || height <= 0) {
			return;
		}

		GuiGeometry.fillRoundedGradient(context, x, y, width, height, radius, top, bottom);

		float limit = Math.min(Math.min(radius, width / 2.0F), height / 2.0F);

		if (limit < 1.0F) {
			return;
		}

		// The bottom corners of the shape are square, so the round shape is covered at the bottom
		// with two squares of the colour it has there.
		int upper = GuiColors.lerp(top, bottom, (height - limit) / (float) height);

		GuiGeometry.fillVerticalGradient(context, x, y + height - limit, limit, limit, upper, bottom);
		GuiGeometry.fillVerticalGradient(context, x + width - limit, y + height - limit, limit, limit, upper, bottom);
	}

	/**
	 * Fills a rectangle with a colour that fades from the left to the right.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param left    the colour at the left, as an ARGB colour
	 * @param right   the colour at the right, as an ARGB colour
	 */
	public static void fillHorizontalGradient(DrawContext context, int x, int y, int width, int height, int left,
			int right) {
		GuiGeometry.fillHorizontalGradient(context, x, y, width, height, left, right);
	}

	/**
	 * Draws a one pixel outline around a rectangle with rounded corners.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param radius  the corner radius, in scaled GUI pixels
	 * @param color   the ARGB colour of the outline
	 */
	public static void strokeRounded(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		GuiGeometry.strokeRounded(context, x, y, width, height, radius, 1.0F, color);
	}

	/**
	 * Fills a circle.
	 *
	 * @param context  the draw context of the current frame
	 * @param centerX  the horizontal centre of the circle, in scaled GUI pixels
	 * @param centerY  the vertical centre of the circle, in scaled GUI pixels
	 * @param radius   the radius of the circle, in scaled GUI pixels
	 * @param color    the ARGB colour to fill with
	 */
	public static void fillCircle(DrawContext context, int centerX, int centerY, int radius, int color) {
		GuiGeometry.fillCircle(context, centerX, centerY, radius, color);
	}

	/**
	 * Draws a soft glow around a rectangle with rounded corners.
	 *
	 * <p>The glow is a few outlines drawn on top of each other, each one further out and each one
	 * fainter than the last. It is not a real blur, but it reads as one.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param radius  the corner radius, in scaled GUI pixels
	 * @param colour  the colour of the glow, as an ARGB colour
	 * @param layers  how many outlines the glow is made of
	 */
	public static void glowRounded(DrawContext context, int x, int y, int width, int height, int radius, int colour,
			int layers) {
		for (int layer = layers; layer >= 1; layer--) {
			float factor = layer / (float) layers;

			GuiGeometry.strokeRounded(context,
					x - layer, y - layer, width + layer * 2, height + layer * 2,
					radius + layer, 1.0F,
					GuiColors.withAlpha(colour, 1.0F - factor * 0.75F));
		}
	}

	/**
	 * Draws a soft shadow under a rectangle with rounded corners.
	 *
	 * <p>The shadow is a few shapes drawn on top of each other, each one further out and each one
	 * fainter than the last, so that it fades away instead of ending in a hard edge.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param radius  the corner radius, in scaled GUI pixels
	 * @param colour  the colour of the shadow, as an ARGB colour
	 * @param layers  how many layers the shadow is made of
	 * @param offsetY how far the shadow sits below the shape, in scaled GUI pixels
	 */
	public static void softShadow(DrawContext context, int x, int y, int width, int height, int radius, int colour,
			int layers, int offsetY) {
		for (int layer = layers; layer >= 1; layer--) {
			float factor = 1.0F - layer / (float) (layers + 1);

			GuiGeometry.fillRounded(context, x - layer, y - layer + offsetY, width + layer * 2, height + layer * 2,
					radius + layer, GuiColors.withAlpha(colour, factor));
		}
	}

	/**
	 * Draws a soft glow around a rectangle with rounded corners.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param radius  the corner radius, in scaled GUI pixels
	 * @param colour  the colour of the glow, as an ARGB colour
	 * @param layers  how many layers the glow is made of
	 */
	public static void softGlow(DrawContext context, int x, int y, int width, int height, int radius, int colour,
			int layers) {
		softShadow(context, x, y, width, height, radius, colour, layers, 0);
	}

	/**
	 * Draws a straight line of a given thickness.
	 *
	 * @param context   the draw context of the current frame
	 * @param x1        the left end of the line, in scaled GUI pixels
	 * @param y1        the top end of the line, in scaled GUI pixels
	 * @param x2        the right end of the line, in scaled GUI pixels
	 * @param y2        the bottom end of the line, in scaled GUI pixels
	 * @param thickness how wide the line is, in scaled GUI pixels
	 * @param color     the ARGB colour to draw with
	 */
	public static void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int thickness, int color) {
		GuiGeometry.drawLine(context, x1, y1, x2, y2, thickness, color);
	}

	/**
	 * Draws a bitmap glyph.
	 *
	 * <p>Each bit of a row marks one pixel, with the least significant bit on the left, so a mask
	 * reads the same way it is drawn.
	 *
	 * @param context the draw context of the current frame
	 * @param mask    the rows of the glyph, one integer per row
	 * @param width   the width of the glyph, in pixels
	 * @param height  the height of the glyph, in pixels
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param color   the ARGB colour to draw with
	 */
	public static void drawMask(DrawContext context, int[] mask, int width, int height, int x, int y, int color) {
		GuiGeometry.fillMask(context, mask, width, height, x, y, color);
	}
}
