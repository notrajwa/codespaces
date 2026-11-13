package com.birowo.client.platform.render;

import net.minecraft.client.gui.DrawContext;

import com.birowo.client.core.gui.GuiColors;

/**
 * The shapes the interface is built from, drawn as a mesh.
 *
 * <p>Every shape is built out of triangles whose corners may lie anywhere, so the edge of a shape
 * follows the curve it is meant to follow instead of stepping along the pixels. The edge is
 * smooth because the colour of the points along it fades: the outermost ring of a shape carries
 * no colour at all, the ring just inside it carries the full colour, and the triangles between
 * the two blend the one into the other.
 *
 * <p>The shapes are drawn in the space of the screen: the places they are given in are turned by
 * the transformation of the moment before they are written down, so a shape keeps its place even
 * when the interface has been moved or turned.
 */
public final class GuiGeometry {
	private GuiGeometry() {
	}

	/**
	 * Fills a rectangle.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param colour  the ARGB colour to fill with
	 */
	public static void fillRect(DrawContext context, float x, float y, float width, float height, int colour) {
		if (width <= 0.0F || height <= 0.0F) {
			return;
		}

		GuiMesh mesh = begin(context);

		mesh.quad(x, y, colour,
				x + width, y, colour,
				x + width, y + height, colour,
				x, y + height, colour);

		finish(context, mesh);
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
	 * @param colour  the ARGB colour to fill with
	 */
	public static void fillRounded(DrawContext context, float x, float y, float width, float height, float radius,
			int colour) {
		if (width <= 0.0F || height <= 0.0F) {
			return;
		}

		float limit = Math.min(Math.min(radius, width / 2.0F), height / 2.0F);

		if (limit < 1.0F) {
			fillRect(context, x, y, width, height, colour);
			return;
		}

		GuiMesh mesh = begin(context);

		rounded(mesh, x, y, width, height, limit, colour, colour, false);

		finish(context, mesh);
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
	public static void fillRoundedGradient(DrawContext context, float x, float y, float width, float height,
			float radius, int top, int bottom) {
		if (width <= 0.0F || height <= 0.0F) {
			return;
		}

		float limit = Math.min(Math.min(radius, width / 2.0F), height / 2.0F);

		if (limit < 1.0F) {
			fillRect(context, x, y, width, height, top);
			return;
		}

		GuiMesh mesh = begin(context);

		rounded(mesh, x, y, width, height, limit, top, bottom, false);

		finish(context, mesh);
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
	public static void fillHorizontalGradient(DrawContext context, float x, float y, float width, float height,
			int left, int right) {
		if (width <= 0.0F || height <= 0.0F) {
			return;
		}

		GuiMesh mesh = begin(context);

		mesh.quad(x, y, left,
				x + width, y, right,
				x + width, y + height, right,
				x, y + height, left);

		finish(context, mesh);
	}

	/**
	 * Fills a rectangle with a colour that fades from the top to the bottom.
	 *
	 * @param context the draw context of the current frame
	 * @param x       the left edge, in scaled GUI pixels
	 * @param y       the top edge, in scaled GUI pixels
	 * @param width   the width, in scaled GUI pixels
	 * @param height  the height, in scaled GUI pixels
	 * @param top     the colour at the top, as an ARGB colour
	 * @param bottom  the colour at the bottom, as an ARGB colour
	 */
	public static void fillVerticalGradient(DrawContext context, float x, float y, float width, float height, int top,
			int bottom) {
		if (width <= 0.0F || height <= 0.0F) {
			return;
		}

		GuiMesh mesh = begin(context);

		mesh.quad(x, y, top,
				x + width, y, top,
				x + width, y + height, bottom,
				x, y + height, bottom);

		finish(context, mesh);
	}

	/**
	 * Draws an outline around a rectangle with rounded corners.
	 *
	 * @param context   the draw context of the current frame
	 * @param x         the left edge, in scaled GUI pixels
	 * @param y         the top edge, in scaled GUI pixels
	 * @param width     the width, in scaled GUI pixels
	 * @param height    the height, in scaled GUI pixels
	 * @param radius    the corner radius, in scaled GUI pixels
	 * @param thickness how wide the outline is, in scaled GUI pixels
	 * @param colour    the ARGB colour of the outline
	 */
	public static void strokeRounded(DrawContext context, float x, float y, float width, float height, float radius,
			float thickness, int colour) {
		if (width <= 0.0F || height <= 0.0F || thickness <= 0.0F) {
			return;
		}

		float limit = Math.min(Math.min(radius, width / 2.0F), height / 2.0F);

		if (limit < 1.0F) {
			fillRect(context, x, y, width, height, colour);
			return;
		}

		GuiMesh mesh = begin(context);

		float[] centreX = { x + limit, x + width - limit, x + width - limit, x + limit };
		float[] centreY = { y + limit, y + limit, y + height - limit, y + height - limit };

		int segments = segments(limit);
		float half = thickness / 2.0F;

		// The outline is a band that follows the shape: the outer ring of it carries no colour, the
		// ring just inside it carries the full colour, and the inner ring of it carries no colour
		// again, so that the outline fades into the shape on both of its sides.
		//
		// The band lies in the middle of the edge of the shape: half of the outline is outside the
		// shape and half of it is inside it, and the fading part lies a quarter of a pixel further
		// out on either side, so that the outline is the width it was asked for and no more.
		Ring outer = ring(centreX, centreY, limit + half + 0.5F, segments);
		Ring edge = ring(centreX, centreY, limit + half, segments);
		Ring inner = ring(centreX, centreY, limit - half, segments);
		Ring last = ring(centreX, centreY, limit - half - 0.5F, segments);

		band(mesh, outer, edge, 0.0F, 1.0F, colour, colour, false, x, y, x + width, y + height);
		band(mesh, edge, inner, 1.0F, 1.0F, colour, colour, false, x, y, x + width, y + height);
		band(mesh, inner, last, 1.0F, 0.0F, colour, colour, false, x, y, x + width, y + height);

		finish(context, mesh);
	}

	/**
	 * Fills a circle.
	 *
	 * @param context  the draw context of the current frame
	 * @param centreX  the horizontal centre of the circle, in scaled GUI pixels
	 * @param centreY  the vertical centre of the circle, in scaled GUI pixels
	 * @param radius   the radius of the circle, in scaled GUI pixels
	 * @param colour   the ARGB colour to fill with
	 */
	public static void fillCircle(DrawContext context, float centreX, float centreY, float radius, int colour) {
		if (radius <= 0.0F) {
			return;
		}

		if (radius < 1.0F) {
			fillRect(context, centreX - radius, centreY - radius, radius * 2.0F, radius * 2.0F, colour);
			return;
		}

		GuiMesh mesh = begin(context);

		// A circle is a whole turn round, where a corner of a rectangle is only a quarter of one,
		// so a circle needs four times as many straight lines to look as smooth as a corner does.
		int segments = circleSegments(radius);

		Ring outer = circleRing(centreX, centreY, radius + 0.5F, segments);
		Ring inner = circleRing(centreX, centreY, radius - 0.5F, segments);

		band(mesh, outer, inner, 0.0F, 1.0F, colour, colour, false,
				centreX - radius, centreY - radius, centreX + radius, centreY + radius);

		// The middle of the circle is filled with strips that lie one above the other.
		float innerRadius = radius - 0.5F;
		float y = centreY - innerRadius;

		while (y < centreY + innerRadius - 0.0001F) {
			float next = Math.min(y + 0.5F, centreY + innerRadius);
			float upperReach = reachCircle(y, centreY, innerRadius);
			float lowerReach = reachCircle(next, centreY, innerRadius);

			mesh.quad(centreX - upperReach, y, colour,
					centreX + upperReach, y, colour,
					centreX + lowerReach, next, colour,
					centreX - lowerReach, next, colour);

			y = next;
		}

		finish(context, mesh);
	}

	/**
	 * How far a circle reaches to the side at a given height.
	 *
	 * @param y        the height
	 * @param centreY  the vertical centre of the circle
	 * @param radius   the radius of the circle
	 * @return the distance from the centre of the circle to its edge
	 */
	private static float reachCircle(float y, float centreY, float radius) {
		float depth = Math.abs(y - centreY);

		if (depth >= radius) {
			return 0.0F;
		}

		return (float) Math.sqrt(radius * radius - depth * depth);
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
	 * @param colour    the ARGB colour to draw with
	 */
	public static void drawLine(DrawContext context, float x1, float y1, float x2, float y2, float thickness,
			int colour) {
		if (thickness <= 0.0F) {
			return;
		}

		float deltaX = x2 - x1;
		float deltaY = y2 - y1;
		float length = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		float half = thickness / 2.0F;

		if (length < 0.0001F) {
			fillRect(context, x1 - half, y1 - half, thickness, thickness, colour);
			return;
		}

		// The line is a rectangle that is turned so that it lies along the line, so that the line
		// is the same width wherever it goes.
		float alongX = deltaX / length;
		float alongY = deltaY / length;
		float acrossX = -alongY * half;
		float acrossY = alongX * half;

		GuiMesh mesh = begin(context);

		mesh.quad(x1 + acrossX, y1 + acrossY, colour,
				x1 - acrossX, y1 - acrossY, colour,
				x2 - acrossX, y2 - acrossY, colour,
				x2 + acrossX, y2 + acrossY, colour);

		finish(context, mesh);
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
	 * @param colour  the ARGB colour to draw with
	 */
	public static void fillMask(DrawContext context, int[] mask, int width, int height, float x, float y, int colour) {
		int rows = Math.min(height, mask.length);

		GuiMesh mesh = begin(context);

		for (int row = 0; row < rows; row++) {
			int bits = mask[row];
			int column = 0;

			while (column < width) {
				if ((bits & (1 << column)) == 0) {
					column++;
					continue;
				}

				// The pixels that stand next to each other are drawn as one piece.
				int start = column;

				while (column < width && (bits & (1 << column)) != 0) {
					column++;
				}

				mesh.quad(x + start, y + row, colour,
						x + column, y + row, colour,
						x + column, y + row + 1, colour,
						x + start, y + row + 1, colour);
			}
		}

		finish(context, mesh);
	}

	// Geometry ---------------------------------------------------------------------------------

	/**
	 * Begins a shape.
	 *
	 * @param context the draw context of the current frame
	 * @return the mesh the shape is drawn into
	 */
	private static GuiMesh begin(DrawContext context) {
		return new GuiMesh(context.getMatrices(), context.scissorStack.peekLast());
	}

	/**
	 * Ends a shape and hands it to the game to be drawn.
	 *
	 * @param context the draw context of the current frame
	 * @param mesh    the mesh the shape was drawn into
	 */
	private static void finish(DrawContext context, GuiMesh mesh) {
		context.state.addSimpleElement(mesh);
	}

	/**
	 * Fills a rectangle with rounded corners, with a colour that fades from one side to the other.
	 *
	 * @param mesh     the mesh the shape is drawn into
	 * @param x        the left edge, in scaled GUI pixels
	 * @param y        the top edge, in scaled GUI pixels
	 * @param width    the width, in scaled GUI pixels
	 * @param height   the height, in scaled GUI pixels
	 * @param radius   the corner radius, in scaled GUI pixels
	 * @param first    the colour at the top, or at the left when the fade is sideways
	 * @param second   the colour at the bottom, or at the right when the fade is sideways
	 * @param sideways whether the colour fades from the left to the right
	 */
	private static void rounded(GuiMesh mesh, float x, float y, float width, float height, float radius, int first,
			int second, boolean sideways) {
		float left = x;
		float top = y;
		float right = x + width;
		float bottom = y + height;

		float[] centreX = { left + radius, right - radius, right - radius, left + radius };
		float[] centreY = { top + radius, top + radius, bottom - radius, bottom - radius };

		int segments = segments(radius);

		// The outer ring is the shape grown by half a pixel and with no colour at all, and the
		// inner ring is the shape shrunk by half a pixel and with the full colour. The band
		// between the two is what makes the edge of the shape smooth.
		Ring outer = ring(centreX, centreY, radius + 0.5F, segments);
		Ring inner = ring(centreX, centreY, radius - 0.5F, segments);

		band(mesh, outer, inner, 0.0F, 1.0F, first, second, sideways, left, top, right, bottom);

		interior(mesh, left + 0.5F, top + 0.5F, right - 0.5F, bottom - 0.5F, radius - 0.5F, first, second, sideways,
				left, top, right, bottom);
	}

	/**
	 * Fills the middle of a rounded rectangle.
	 *
	 * <p>The middle is filled with strips that lie one above the other. The strips in the corners
	 * are half a pixel high, because the shape turns quickly there.
	 *
	 * @param mesh    the mesh the shape is drawn into
	 * @param left    the left edge of the middle
	 * @param top     the top edge of the middle
	 * @param right   the right edge of the middle
	 * @param bottom  the bottom edge of the middle
	 * @param radius  the corner radius of the middle
	 * @param first   the colour at the top, or at the left when the fade is sideways
	 * @param second  the colour at the bottom, or at the right when the fade is sideways
	 * @param sideways whether the colour fades from the left to the right
	 * @param colourLeft  the left edge of the shape, where the fade begins
	 * @param colourTop   the top edge of the shape, where the fade begins
	 * @param colourRight  the right edge of the shape, where the fade ends
	 * @param colourBottom the bottom edge of the shape, where the fade ends
	 */
	private static void interior(GuiMesh mesh, float left, float top, float right, float bottom, float radius,
			int first, int second, boolean sideways, float colourLeft, float colourTop, float colourRight,
			float colourBottom) {
		float y = top;

		while (y < bottom - 0.0001F) {
			float next;

			if (y < top + radius - 0.0001F) {
				next = Math.min(y + 0.5F, top + radius);
			} else if (y > bottom - radius - 0.0001F) {
				next = Math.min(y + 0.5F, bottom);
			} else {
				next = bottom - radius;
			}

			// The strip is exactly as wide as the shape is at its top and at its bottom, so that it
			// neither leaves a gap nor reaches out beyond the edge of the shape.
			float upperReach = reach(y, top, bottom, radius);
			float lowerReach = reach(next, top, bottom, radius);

			float upperLeft = left + radius - upperReach;
			float upperRight = right - radius + upperReach;
			float lowerLeft = left + radius - lowerReach;
			float lowerRight = right - radius + lowerReach;

			mesh.quad(upperLeft, y, shade(first, second, sideways, upperLeft, y, colourLeft, colourTop, colourRight,
						colourBottom, 1.0F),
					upperRight, y, shade(first, second, sideways, upperRight, y, colourLeft, colourTop, colourRight,
							colourBottom, 1.0F),
					lowerRight, next, shade(first, second, sideways, lowerRight, next, colourLeft, colourTop,
							colourRight, colourBottom, 1.0F),
					lowerLeft, next, shade(first, second, sideways, lowerLeft, next, colourLeft, colourTop, colourRight,
							colourBottom, 1.0F));

			y = next;
		}
	}

	/**
	 * How far the shape reaches to the side at a given height.
	 *
	 * @param y      the height
	 * @param top    the top edge of the shape
	 * @param bottom the bottom edge of the shape
	 * @param radius the corner radius of the shape
	 * @return the distance from the straight side of the shape to its edge
	 */
	private static float reach(float y, float top, float bottom, float radius) {
		float depth = 0.0F;

		if (y < top + radius) {
			depth = Math.max(depth, top + radius - y);
		}

		if (y > bottom - radius) {
			depth = Math.max(depth, y - (bottom - radius));
		}

		if (depth <= 0.0F) {
			return radius;
		}

		if (depth >= radius) {
			return 0.0F;
		}

		return (float) Math.sqrt(radius * radius - depth * depth);
	}

	/**
	 * Draws the band between two rings of a shape.
	 *
	 * @param mesh     the mesh the shape is drawn into
	 * @param outer    the outer ring
	 * @param inner    the inner ring
	 * @param outerFade the opacity of the outer ring
	 * @param innerFade the opacity of the inner ring
	 * @param first    the colour at the top, or at the left when the fade is sideways
	 * @param second   the colour at the bottom, or at the right when the fade is sideways
	 * @param sideways whether the colour fades from the left to the right
	 * @param left     the left edge of the shape
	 * @param top      the top edge of the shape
	 * @param right    the right edge of the shape
	 * @param bottom   the bottom edge of the shape
	 */
	private static void band(GuiMesh mesh, Ring outer, Ring inner, float outerFade, float innerFade, int first,
			int second, boolean sideways, float left, float top, float right, float bottom) {
		for (int point = 0; point < outer.count; point++) {
			int next = (point + 1) % outer.count;

			mesh.quad(outer.xs[point], outer.ys[point],
					shade(first, second, sideways, outer.xs[point], outer.ys[point], left, top, right, bottom, outerFade),
					outer.xs[next], outer.ys[next],
					shade(first, second, sideways, outer.xs[next], outer.ys[next], left, top, right, bottom, outerFade),
					inner.xs[next], inner.ys[next],
					shade(first, second, sideways, inner.xs[next], inner.ys[next], left, top, right, bottom, innerFade),
					inner.xs[point], inner.ys[point],
					shade(first, second, sideways, inner.xs[point], inner.ys[point], left, top, right, bottom, innerFade));
		}
	}

	/**
	 * The colour of a point of a shape.
	 *
	 * @param first    the colour at the top, or at the left when the fade is sideways
	 * @param second   the colour at the bottom, or at the right when the fade is sideways
	 * @param sideways whether the colour fades from the left to the right
	 * @param x        the horizontal place of the point
	 * @param y        the vertical place of the point
	 * @param left     the left edge of the shape, where the fade begins
	 * @param top      the top edge of the shape, where the fade begins
	 * @param right    the right edge of the shape, where the fade ends
	 * @param bottom   the bottom edge of the shape, where the fade ends
	 * @param alpha    the opacity of the point
	 * @return the colour of the point
	 */
	private static int shade(int first, int second, boolean sideways, float x, float y, float left, float top,
			float right, float bottom, float alpha) {
		float share = sideways
				? (x - left) / Math.max(0.0001F, right - left)
				: (y - top) / Math.max(0.0001F, bottom - top);

		return GuiColors.withAlpha(GuiColors.lerp(first, second, share), alpha);
	}

	/**
	 * The ring of a rounded rectangle.
	 *
	 * <p>The centres of the corners do not move when the radius changes, so that the rings of a
	 * shape can be laid on top of one another.
	 *
	 * @param centreX the horizontal places of the centres of the corners
	 * @param centreY the vertical places of the centres of the corners
	 * @param radius  the radius of the corners
	 * @param segments the number of straight lines each corner is made of
	 * @return the ring
	 */
	private static Ring ring(float[] centreX, float[] centreY, float radius, int segments) {
		int perCorner = segments + 1;
		int total = perCorner * 4;

		float[] xs = new float[total];
		float[] ys = new float[total];

		int index = 0;

		// The top left corner, from the top of the shape to the left of it.
		index = arc(xs, ys, index, centreX[0], centreY[0], radius, 270.0D, 180.0D, segments);
		// The bottom left corner, from the left of the shape to the bottom of it.
		index = arc(xs, ys, index, centreX[3], centreY[3], radius, 180.0D, 90.0D, segments);
		// The bottom right corner, from the bottom of the shape to the right of it.
		index = arc(xs, ys, index, centreX[2], centreY[2], radius, 90.0D, 0.0D, segments);
		// The top right corner, from the right of the shape to the top of it.
		index = arc(xs, ys, index, centreX[1], centreY[1], radius, 0.0D, -90.0D, segments);

		return new Ring(xs, ys, total);
	}

	/**
	 * The ring of a circle.
	 *
	 * @param centreX the horizontal centre of the circle
	 * @param centreY the vertical centre of the circle
	 * @param radius  the radius of the circle
	 * @param segments the number of straight lines the circle is made of
	 * @return the ring
	 */
	private static Ring circleRing(float centreX, float centreY, float radius, int segments) {
		float[] xs = new float[segments];
		float[] ys = new float[segments];

		for (int point = 0; point < segments; point++) {
			double angle = Math.PI * 2.0D * point / segments;

			xs[point] = centreX + (float) Math.cos(angle) * radius;
			ys[point] = centreY + (float) Math.sin(angle) * radius;
		}

		return new Ring(xs, ys, segments);
	}

	/**
	 * The points of one corner of a ring.
	 *
	 * @param xs      the horizontal places of the points
	 * @param ys      the vertical places of the points
	 * @param index   the place the points are written at
	 * @param centreX the horizontal place of the centre of the corner
	 * @param centreY the vertical place of the centre of the corner
	 * @param radius  the radius of the corner
	 * @param from    the angle the corner begins at, in degrees
	 * @param to      the angle the corner ends at, in degrees
	 * @param segments the number of straight lines the corner is made of
	 * @return the place the points were written up to
	 */
	private static int arc(float[] xs, float[] ys, int index, float centreX, float centreY, float radius, double from,
			double to, int segments) {
		for (int step = 0; step <= segments; step++) {
			double angle = Math.toRadians(from + (to - from) * (double) step / segments);

			xs[index] = centreX + (float) Math.cos(angle) * radius;
			ys[index] = centreY + (float) Math.sin(angle) * radius;
			index++;
		}

		return index;
	}

	/**
	 * How many straight lines a corner of the given radius is made of.
	 *
	 * @param radius the radius of the corner
	 * @return the number of straight lines
	 */
	private static int segments(float radius) {
		int count = (int) Math.ceil(radius * 1.6D);

		return Math.max(3, Math.min(16, count));
	}

	/**
	 * How many straight lines a circle of the given radius is made of.
	 *
	 * <p>A circle is a whole turn round, so it needs many more straight lines than a corner of a
	 * rectangle, which is only a quarter of a turn. Too few of them and the circle looks like a
	 * many sided figure rather than a circle.
	 *
	 * @param radius the radius of the circle
	 * @return the number of straight lines
	 */
	private static int circleSegments(float radius) {
		int count = (int) Math.ceil(radius * 6.0D);

		return Math.max(24, Math.min(96, count));
	}

	/**
	 * A ring of points.
	 */
	private static final class Ring {
		/**
		 * The horizontal places of the points.
		 */
		private final float[] xs;

		/**
		 * The vertical places of the points.
		 */
		private final float[] ys;

		/**
		 * How many points the ring has.
		 */
		private final int count;

		private Ring(float[] xs, float[] ys, int count) {
			this.xs = xs;
			this.ys = ys;
			this.count = count;
		}
	}
}
