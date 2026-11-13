package com.birowo.client.platform.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

import com.birowo.client.core.util.NeighbourFaces;

/**
 * Draws the shapes the client shows in the world.
 *
 * <p>The helpers write into a buffer that the caller has taken from the render context; they do
 * not touch any state of their own, so the render state of the game is left exactly as it was
 * found.
 */
public final class ShapeRenderer {
	private ShapeRenderer() {
	}

	/**
	 * Draws the twelve edges of a box.
	 *
	 * @param consumer  the buffer the lines are written to
	 * @param entry     the transformation the lines are drawn with
	 * @param minX      the left edge of the box
	 * @param minY      the lower edge of the box
	 * @param minZ      the front edge of the box
	 * @param maxX      the right edge of the box
	 * @param maxY      the upper edge of the box
	 * @param maxZ      the back edge of the box
	 * @param argb      the colour of the lines
	 * @param lineWidth the width of the lines
	 */
	public static void drawBoxEdges(VertexConsumer consumer, MatrixStack.Entry entry, double minX, double minY,
			double minZ, double maxX, double maxY, double maxZ, int argb, float lineWidth) {
		// The four edges of the lower ring.
		segment(consumer, entry, minX, minY, minZ, maxX, minY, minZ, argb, lineWidth);
		segment(consumer, entry, maxX, minY, minZ, maxX, minY, maxZ, argb, lineWidth);
		segment(consumer, entry, maxX, minY, maxZ, minX, minY, maxZ, argb, lineWidth);
		segment(consumer, entry, minX, minY, maxZ, minX, minY, minZ, argb, lineWidth);

		// The four edges of the upper ring.
		segment(consumer, entry, minX, maxY, minZ, maxX, maxY, minZ, argb, lineWidth);
		segment(consumer, entry, maxX, maxY, minZ, maxX, maxY, maxZ, argb, lineWidth);
		segment(consumer, entry, maxX, maxY, maxZ, minX, maxY, maxZ, argb, lineWidth);
		segment(consumer, entry, minX, maxY, maxZ, minX, maxY, minZ, argb, lineWidth);

		// The four upright edges.
		segment(consumer, entry, minX, minY, minZ, minX, maxY, minZ, argb, lineWidth);
		segment(consumer, entry, maxX, minY, minZ, maxX, maxY, minZ, argb, lineWidth);
		segment(consumer, entry, maxX, minY, maxZ, maxX, maxY, maxZ, argb, lineWidth);
		segment(consumer, entry, minX, minY, maxZ, minX, maxY, maxZ, argb, lineWidth);
	}

	/**
	 * Draws the twelve edges of a box, leaving out the edges that are hidden inside a group of
	 * blocks.
	 *
	 * <p>An edge lies on two sides of the box. When both of those sides are covered by a block of
	 * the same kind, the edge is inside the group and is not drawn; when only one of them is
	 * covered, the edge is on the outside of the group and is drawn.
	 *
	 * @param consumer     the buffer the lines are written to
	 * @param entry        the transformation the lines are drawn with
	 * @param minX         the left edge of the box
	 * @param minY         the lower edge of the box
	 * @param minZ         the front edge of the box
	 * @param maxX         the right edge of the box
	 * @param maxY         the upper edge of the box
	 * @param maxZ         the back edge of the box
	 * @param argb         the colour of the lines
	 * @param lineWidth    the width of the lines
	 * @param coveredFaces the sides of the box that are covered by a block of the same kind
	 */
	public static void drawBoxEdges(VertexConsumer consumer, MatrixStack.Entry entry, double minX, double minY,
			double minZ, double maxX, double maxY, double maxZ, int argb, float lineWidth, int coveredFaces) {
		// The four edges along the horizontal axis.
		edge(consumer, entry, minX, minY, minZ, maxX, minY, minZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.DOWN | NeighbourFaces.NORTH);
		edge(consumer, entry, minX, maxY, minZ, maxX, maxY, minZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.UP | NeighbourFaces.NORTH);
		edge(consumer, entry, minX, minY, maxZ, maxX, minY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.DOWN | NeighbourFaces.SOUTH);
		edge(consumer, entry, minX, maxY, maxZ, maxX, maxY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.UP | NeighbourFaces.SOUTH);

		// The four edges along the depth.
		edge(consumer, entry, minX, minY, minZ, minX, minY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.DOWN | NeighbourFaces.WEST);
		edge(consumer, entry, maxX, minY, minZ, maxX, minY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.DOWN | NeighbourFaces.EAST);
		edge(consumer, entry, minX, maxY, minZ, minX, maxY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.UP | NeighbourFaces.WEST);
		edge(consumer, entry, maxX, maxY, minZ, maxX, maxY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.UP | NeighbourFaces.EAST);

		// The four upright edges.
		edge(consumer, entry, minX, minY, minZ, minX, maxY, minZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.WEST | NeighbourFaces.NORTH);
		edge(consumer, entry, maxX, minY, minZ, maxX, maxY, minZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.EAST | NeighbourFaces.NORTH);
		edge(consumer, entry, minX, minY, maxZ, minX, maxY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.WEST | NeighbourFaces.SOUTH);
		edge(consumer, entry, maxX, minY, maxZ, maxX, maxY, maxZ, argb, lineWidth, coveredFaces,
				NeighbourFaces.EAST | NeighbourFaces.SOUTH);
	}

	/**
	 * Draws one side of a box.
	 *
	 * @param consumer the buffer the face is written to
	 * @param entry    the transformation the face is drawn with
	 * @param face     the side of the box that is drawn
	 * @param minX     the left edge of the box
	 * @param minY     the lower edge of the box
	 * @param minZ     the front edge of the box
	 * @param maxX     the right edge of the box
	 * @param maxY     the upper edge of the box
	 * @param maxZ     the back edge of the box
	 * @param argb     the colour of the face
	 */
	public static void drawBoxFace(VertexConsumer consumer, MatrixStack.Entry entry, int face, double minX,
			double minY, double minZ, double maxX, double maxY, double maxZ, int argb) {
		switch (face) {
			case NeighbourFaces.DOWN -> face(consumer, entry, minX, minY, minZ, maxX, minY, maxZ, maxX, minY,
					maxZ, minX, minY, minZ, argb, argb);
			case NeighbourFaces.UP -> face(consumer, entry, minX, maxY, minZ, maxX, maxY, maxZ, maxX, maxY, maxZ,
					minX, maxY, minZ, argb, argb);
			case NeighbourFaces.NORTH -> face(consumer, entry, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ,
					minX, maxY, minZ, argb, argb);
			case NeighbourFaces.SOUTH -> face(consumer, entry, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ,
					minX, maxY, maxZ, argb, argb);
			case NeighbourFaces.WEST -> face(consumer, entry, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ,
					minX, maxY, minZ, argb, argb);
			case NeighbourFaces.EAST -> face(consumer, entry, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ,
					maxX, maxY, minZ, argb, argb);
			default -> {
				// A side that does not exist is not drawn.
			}
		}
	}

	/**
	 * Draws one straight line.
	 *
	 * @param consumer  the buffer the line is written to
	 * @param entry     the transformation the line is drawn with
	 * @param fromX     the horizontal start of the line
	 * @param fromY     the vertical start of the line
	 * @param fromZ     the depth of the start of the line
	 * @param toX       the horizontal end of the line
	 * @param toY       the vertical end of the line
	 * @param toZ       the depth of the end of the line
	 * @param argb      the colour of the line
	 * @param lineWidth the width of the line
	 */
	public static void drawSegment(VertexConsumer consumer, MatrixStack.Entry entry, double fromX, double fromY,
			double fromZ, double toX, double toY, double toZ, int argb, float lineWidth) {
		segment(consumer, entry, fromX, fromY, fromZ, toX, toY, toZ, argb, lineWidth);
	}

	/**
	 * Draws a circle of straight lines in a horizontal plane.
	 *
	 * @param consumer  the buffer the lines are written to
	 * @param entry     the transformation the lines are drawn with
	 * @param centerX   the centre of the circle
	 * @param centerY   the height of the circle
	 * @param centerZ   the centre of the circle
	 * @param radius    the radius of the circle
	 * @param argb      the colour of the lines
	 * @param lineWidth the width of the lines
	 * @param segments  the number of straight lines the circle is made of
	 */
	public static void drawRing(VertexConsumer consumer, MatrixStack.Entry entry, double centerX, double centerY,
			double centerZ, double radius, int argb, float lineWidth, int segments) {
		double step = Math.PI * 2.0D / segments;

		for (int segment = 0; segment < segments; segment++) {
			double from = segment * step;
			double to = (segment + 1) * step;

			segment(consumer, entry,
					centerX + Math.cos(from) * radius, centerY, centerZ + Math.sin(from) * radius,
					centerX + Math.cos(to) * radius, centerY, centerZ + Math.sin(to) * radius,
					argb, lineWidth);
		}
	}

	/**
	 * Draws a cylinder with rounded ends, which is the shape of a tube whose ends are rounded off.
	 *
	 * <p>The shape is made of three parts. The body is a ring of straight lines that stand
	 * upright. The lower end is a half sphere that is drawn as a stack of rings, the upper end
	 * is the same thing turned the other way. The rings grow smaller towards the ends, so the
	 * shape has no edges at all: it is round everywhere.
	 *
	 * <p>The rounded ends may be taller than they are wide, which makes the shape less of a tube
	 * and more of a barrel that has been rounded off at both ends.
	 *
	 * @param consumer    the buffer the lines are written to
	 * @param entry       the transformation the shape is drawn with
	 * @param centerX     the horizontal centre of the shape
	 * @param bottomY     the height of the lowest point of the shape
	 * @param centerZ     the depth of the centre of the shape
	 * @param radius      the radius of the body of the shape
	 * @param height      the height of the whole shape, from the lowest point to the highest
	 * @param argb        the colour of the lines
	 * @param lineWidth   the width of the lines
	 * @param segments    the number of straight lines the body is made of
	 * @param capSegments the number of rings each of the rounded ends is made of
	 * @param capScale    how tall the rounded ends are compared to the radius of the body
	 */
	public static void drawRoundedCylinder(VertexConsumer consumer, MatrixStack.Entry entry, double centerX,
			double bottomY, double centerZ, double radius, double height, int argb, float lineWidth, int segments,
			int capSegments, double capScale) {
		if (radius <= 0.0D || height <= 0.0D || segments < 3 || capSegments < 1) {
			return;
		}

		// The rounded ends are half spheres, so their height is the radius of the body. A shape
		// that is lower than that has no body at all, and the ends meet in the middle.
		double capHeight = Math.min(radius * Math.max(0.1D, capScale), height / 2.0D);
		double bodyBottom = bottomY + capHeight;
		double bodyTop = bottomY + height - capHeight;

		// The upright lines of the body.
		double step = Math.PI * 2.0D / segments;

		for (int segment = 0; segment < segments; segment++) {
			double angle = segment * step;

			segment(consumer, entry,
					centerX + Math.cos(angle) * radius, bodyBottom, centerZ + Math.sin(angle) * radius,
					centerX + Math.cos(angle) * radius, bodyTop, centerZ + Math.sin(angle) * radius,
					argb, lineWidth);
		}

		// The ring at the bottom of the body, which the lower end begins at.
		drawRing(consumer, entry, centerX, bodyBottom, centerZ, radius, argb, lineWidth, segments);

		// The ring at the top of the body, which the upper end begins at.
		drawRing(consumer, entry, centerX, bodyTop, centerZ, radius, argb, lineWidth, segments);

		// The lower end: a stack of rings that grow smaller as they go down. The ring of the body
		// is the one that the stack ends at, so it is not drawn twice.
		for (int ring = 0; ring < capSegments; ring++) {
			double angle = Math.PI / 2.0D * ring / capSegments;
			double y = bodyBottom - capHeight * Math.cos(angle);
			double r = radius * Math.sin(angle);

			if (r <= 0.0D) {
				continue;
			}

			drawRing(consumer, entry, centerX, y, centerZ, r, argb, lineWidth, segments);
		}

		// The upper end: the same stack of rings, turned the other way.
		for (int ring = 1; ring <= capSegments; ring++) {
			double angle = Math.PI / 2.0D * ring / capSegments;
			double y = bodyTop + capHeight * Math.sin(angle);
			double r = radius * Math.cos(angle);

			if (r <= 0.0D) {
				continue;
			}

			drawRing(consumer, entry, centerX, y, centerZ, r, argb, lineWidth, segments);
		}
	}

	/**
	 * Fills a cylinder with rounded ends, so that the shape is a solid tube rather than an outline.
	 *
	 * <p>The shape is made of three parts. The body is a ring of faces that stand upright between
	 * the two rounded ends. The lower end is a stack of rings of faces that grow smaller towards
	 * the bottom, and the upper end is the same thing turned the other way. The ends may be taller
	 * than they are wide, which makes the shape less of a tube and more of a barrel.
	 *
	 * <p>The colour fades from the bottom of the shape to the top of it, so that the shape has some
	 * depth to it rather than being one flat colour.
	 *
	 * @param consumer    the buffer the faces are written to
	 * @param entry       the transformation the shape is drawn with
	 * @param centerX     the horizontal centre of the shape
	 * @param bottomY     the height of the lowest point of the shape
	 * @param centerZ     the depth of the centre of the shape
	 * @param radius      the radius of the body of the shape
	 * @param height      the height of the whole shape, from the lowest point to the highest
	 * @param bottomColor the colour at the bottom of the shape, as an ARGB colour
	 * @param topColor    the colour at the top of the shape, as an ARGB colour
	 * @param segments    the number of straight lines the body is made of
	 * @param capSegments the number of rings each of the rounded ends is made of
	 * @param capScale    how tall the rounded ends are compared to the radius of the body
	 */
	public static void drawFilledRoundedCylinder(VertexConsumer consumer, MatrixStack.Entry entry, double centerX,
			double bottomY, double centerZ, double radius, double height, int bottomColor, int topColor,
			int segments, int capSegments, double capScale) {
		if (radius <= 0.0D || height <= 0.0D || segments < 3 || capSegments < 1) {
			return;
		}

		double capHeight = Math.min(radius * Math.max(0.1D, capScale), height / 2.0D);
		double bodyBottom = bottomY + capHeight;
		double bodyTop = bottomY + height - capHeight;

		double step = Math.PI * 2.0D / segments;

		// The body: a ring of faces that stand upright between the two rounded ends.
		for (int segment = 0; segment < segments; segment++) {
			double from = segment * step;
			double to = (segment + 1) * step;

			double fromX = centerX + Math.cos(from) * radius;
			double fromZ = centerZ + Math.sin(from) * radius;
			double toX = centerX + Math.cos(to) * radius;
			double toZ = centerZ + Math.sin(to) * radius;

			face(consumer, entry,
					fromX, bodyBottom, fromZ,
					toX, bodyBottom, toZ,
					toX, bodyTop, toZ,
					fromX, bodyTop, fromZ,
					bottomColor, topColor);
		}

		// The lower end: rings of faces that grow smaller as they go down towards the point.
		for (int ring = 0; ring < capSegments; ring++) {
			double lowerAngle = Math.PI / 2.0D * ring / capSegments;
			double upperAngle = Math.PI / 2.0D * (ring + 1) / capSegments;

			double lowerY = bodyBottom - capHeight * Math.cos(lowerAngle);
			double upperY = bodyBottom - capHeight * Math.cos(upperAngle);

			double lowerRadius = radius * Math.sin(lowerAngle);
			double upperRadius = radius * Math.sin(upperAngle);

			capBand(consumer, entry, centerX, centerZ,
					lowerRadius, lowerY, upperRadius, upperY,
					colourAt(bottomColor, topColor, lowerY, bottomY, height),
					colourAt(bottomColor, topColor, upperY, bottomY, height),
					segments, step);
		}

		// The upper end: the same stack of rings, turned the other way.
		for (int ring = 0; ring < capSegments; ring++) {
			double lowerAngle = Math.PI / 2.0D * ring / capSegments;
			double upperAngle = Math.PI / 2.0D * (ring + 1) / capSegments;

			double lowerY = bodyTop + capHeight * Math.sin(lowerAngle);
			double upperY = bodyTop + capHeight * Math.sin(upperAngle);

			double lowerRadius = radius * Math.cos(lowerAngle);
			double upperRadius = radius * Math.cos(upperAngle);

			capBand(consumer, entry, centerX, centerZ,
					lowerRadius, lowerY, upperRadius, upperY,
					colourAt(bottomColor, topColor, lowerY, bottomY, height),
					colourAt(bottomColor, topColor, upperY, bottomY, height),
					segments, step);
		}
	}

	/**
	 * Writes a ring of faces between two rings into the buffer.
	 *
	 * @param consumer     the buffer the faces are written to
	 * @param entry        the transformation the faces are drawn with
	 * @param centerX      the horizontal centre of the rings
	 * @param centerZ      the depth of the centre of the rings
	 * @param lowerRadius  the radius of the lower ring
	 * @param lowerY       the height of the lower ring
	 * @param upperRadius  the radius of the upper ring
	 * @param upperY       the height of the upper ring
	 * @param lowerColor   the colour of the lower ring, as an ARGB colour
	 * @param upperColor   the colour of the upper ring, as an ARGB colour
	 * @param segments     the number of faces the ring is made of
	 * @param step         the angle between two faces of the ring
	 */
	private static void capBand(VertexConsumer consumer, MatrixStack.Entry entry, double centerX, double centerZ,
			double lowerRadius, double lowerY, double upperRadius, double upperY, int lowerColor, int upperColor,
			int segments, double step) {
		for (int segment = 0; segment < segments; segment++) {
			double from = segment * step;
			double to = (segment + 1) * step;

			consumer.vertex(entry, (float) (centerX + Math.cos(from) * lowerRadius), (float) lowerY,
					(float) (centerZ + Math.sin(from) * lowerRadius)).color(lowerColor);
			consumer.vertex(entry, (float) (centerX + Math.cos(to) * lowerRadius), (float) lowerY,
					(float) (centerZ + Math.sin(to) * lowerRadius)).color(lowerColor);
			consumer.vertex(entry, (float) (centerX + Math.cos(to) * upperRadius), (float) upperY,
					(float) (centerZ + Math.sin(to) * upperRadius)).color(upperColor);
			consumer.vertex(entry, (float) (centerX + Math.cos(from) * upperRadius), (float) upperY,
					(float) (centerZ + Math.sin(from) * upperRadius)).color(upperColor);
		}
	}

	/**
	 * The colour of a shape at a given height.
	 *
	 * @param bottomColor the colour at the bottom of the shape, as an ARGB colour
	 * @param topColor    the colour at the top of the shape, as an ARGB colour
	 * @param y           the height the colour is wanted at
	 * @param bottomY     the height of the lowest point of the shape
	 * @param height      the height of the whole shape
	 * @return the colour at that height, as an ARGB colour
	 */
	private static int colourAt(int bottomColor, int topColor, double y, double bottomY, double height) {
		float share = (float) Math.max(0.0D, Math.min(1.0D, (y - bottomY) / Math.max(0.0001D, height)));

		int alpha = ((bottomColor >>> 24) & 0xFF)
				+ (int) ((((topColor >>> 24) & 0xFF) - ((bottomColor >>> 24) & 0xFF)) * share);
		int red = ((bottomColor >> 16) & 0xFF) + (int) ((((topColor >> 16) & 0xFF) - ((bottomColor >> 16) & 0xFF)) * share);
		int green = ((bottomColor >> 8) & 0xFF) + (int) ((((topColor >> 8) & 0xFF) - ((bottomColor >> 8) & 0xFF)) * share);
		int blue = (bottomColor & 0xFF) + (int) (((topColor & 0xFF) - (bottomColor & 0xFF)) * share);

		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * Fills a box with six faces whose colour fades from the bottom to the top.
	 *
	 * @param consumer    the buffer the faces are written to
	 * @param entry       the transformation the faces are drawn with
	 * @param minX        the left edge of the box
	 * @param minY        the lower edge of the box
	 * @param minZ        the front edge of the box
	 * @param maxX        the right edge of the box
	 * @param maxY        the upper edge of the box
	 * @param maxZ        the back edge of the box
	 * @param bottomColor the colour of the lower vertices
	 * @param topColor    the colour of the upper vertices
	 */
	public static void drawBoxFill(VertexConsumer consumer, MatrixStack.Entry entry, double minX, double minY,
			double minZ, double maxX, double maxY, double maxZ, int bottomColor, int topColor) {
		face(consumer, entry, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ,
				bottomColor, topColor);
		face(consumer, entry, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ,
				bottomColor, topColor);
		face(consumer, entry, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ,
				bottomColor, topColor);
		face(consumer, entry, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ,
				bottomColor, topColor);
		face(consumer, entry, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ,
				topColor, topColor);
		face(consumer, entry, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ,
				bottomColor, bottomColor);
	}

	/**
	 * The light of the world, as a direction the shading is built from.
	 */
	private static final double LIGHT_X = 0.40D;

	private static final double LIGHT_Y = 0.82D;

	private static final double LIGHT_Z = 0.41D;

	/**
	 * Draws a cone that stands on a horizontal circle.
	 *
	 * <p>The cone is made of triangles that share one point above the circle. The colour of every
	 * point is shaded by the light of the world, which is a fixed direction, so that the cone
	 * looks like a solid body and not like a flat silhouette.
	 *
	 * @param consumer  the buffer the triangles are written to
	 * @param entry     the transformation the cone is drawn with
	 * @param centerX   the horizontal centre of the circle
	 * @param baseY     the height of the circle
	 * @param centerZ   the depth of the centre of the circle
	 * @param radius    the radius of the circle
	 * @param height    the height of the point of the cone above the circle
	 * @param argb      the colour of the cone
	 * @param rotation  the angle the cone is turned by, in radians
	 * @param segments  the number of triangles the side of the cone is made of
	 */
	public static void drawCone(VertexConsumer consumer, MatrixStack.Entry entry, double centerX, double baseY,
			double centerZ, double radius, double height, int argb, float rotation, int segments) {
		double step = Math.PI * 2.0D / segments;
		double apexY = baseY + height;

		// The normal of every side of the cone, which is what the shading is built from.
		double[] sideX = new double[segments];
		double[] sideY = new double[segments];
		double[] sideZ = new double[segments];

		for (int segment = 0; segment < segments; segment++) {
			double from = rotation + segment * step;
			double to = from + step;

			double ax = centerX + Math.cos(from) * radius;
			double az = centerZ + Math.sin(from) * radius;
			double bx = centerX + Math.cos(to) * radius;
			double bz = centerZ + Math.sin(to) * radius;

			// The normal of the triangle that is made of the point of the cone and the two ends
			// of the side.
			double ux = ax - centerX;
			double uy = baseY - apexY;
			double uz = az - centerZ;
			double vx = bx - centerX;
			double vy = uy;
			double vz = bz - centerZ;

			double nx = uy * vz - uz * vy;
			double ny = uz * vx - ux * vz;
			double nz = ux * vy - uy * vx;

			double length = Math.sqrt(nx * nx + ny * ny + nz * nz);

			if (length > 0.0D) {
				nx /= length;
				ny /= length;
				nz /= length;
			}

			sideX[segment] = nx;
			sideY[segment] = ny;
			sideZ[segment] = nz;
		}

		double apexNormalX = 0.0D;
		double apexNormalY = 0.0D;
		double apexNormalZ = 0.0D;

		for (int segment = 0; segment < segments; segment++) {
			apexNormalX += sideX[segment];
			apexNormalY += sideY[segment];
			apexNormalZ += sideZ[segment];
		}

		double apexLength = Math.sqrt(apexNormalX * apexNormalX + apexNormalY * apexNormalY
				+ apexNormalZ * apexNormalZ);

		if (apexLength > 0.0D) {
			apexNormalX /= apexLength;
			apexNormalY /= apexLength;
			apexNormalZ /= apexLength;
		}

		int apexColor = shade(argb, light(apexNormalX, apexNormalY, apexNormalZ));

		for (int segment = 0; segment < segments; segment++) {
			int next = (segment + 1) % segments;

			double from = rotation + segment * step;
			double to = from + step;

			double ax = centerX + Math.cos(from) * radius;
			double az = centerZ + Math.sin(from) * radius;
			double bx = centerX + Math.cos(to) * radius;
			double bz = centerZ + Math.sin(to) * radius;

			// The normal of a point of the circle is the average of the two sides that meet
			// there, which keeps the shading of the cone even.
			double vertexX = sideX[segment] + sideX[next];
			double vertexY = sideY[segment] + sideY[next];
			double vertexZ = sideZ[segment] + sideZ[next];

			double vertexLength = Math.sqrt(vertexX * vertexX + vertexY * vertexY + vertexZ * vertexZ);

			if (vertexLength > 0.0D) {
				vertexX /= vertexLength;
				vertexY /= vertexLength;
				vertexZ /= vertexLength;
			}

			int colorA = shade(argb, light(vertexX, vertexY, vertexZ));
			int colorB = shade(argb, light(sideX[next], sideY[next], sideZ[next]));

			consumer.vertex(entry, (float) centerX, (float) apexY, (float) centerZ).color(apexColor);
			consumer.vertex(entry, (float) ax, (float) baseY, (float) az).color(colorA);
			consumer.vertex(entry, (float) bx, (float) baseY, (float) bz).color(colorB);
		}

		// The bottom of the cone, so that it does not look hollow when it is seen from below.
		int bottomColor = shade(argb, light(0.0D, -1.0D, 0.0D));

		for (int segment = 0; segment < segments; segment++) {
			double from = rotation + segment * step;
			double to = from + step;

			consumer.vertex(entry, (float) centerX, (float) baseY, (float) centerZ).color(bottomColor);
			consumer.vertex(entry, (float) (centerX + Math.cos(to) * radius), (float) baseY,
					(float) (centerZ + Math.sin(to) * radius)).color(bottomColor);
			consumer.vertex(entry, (float) (centerX + Math.cos(from) * radius), (float) baseY,
					(float) (centerZ + Math.sin(from) * radius)).color(bottomColor);
		}
	}

	/**
	 * @param normalX the horizontal part of the normal of a point
	 * @param normalY the vertical part of the normal of a point
	 * @param normalZ the depth of the normal of a point
	 * @return how bright the point is, between zero and one
	 */
	private static float light(double normalX, double normalY, double normalZ) {
		double dot = normalX * LIGHT_X + normalY * LIGHT_Y + normalZ * LIGHT_Z;
		double brightness = 0.34D + 0.66D * Math.max(0.0D, dot);

		return (float) Math.min(1.0D, brightness);
	}

	/**
	 * @param argb   the colour that is shaded
	 * @param factor how much of the colour is kept, between zero and one
	 * @return the shaded colour, with the opacity of the original
	 */
	private static int shade(int argb, float factor) {
		int alpha = (argb >>> 24) & 0xFF;
		int red = Math.round(((argb >> 16) & 0xFF) * factor);
		int green = Math.round(((argb >> 8) & 0xFF) * factor);
		int blue = Math.round((argb & 0xFF) * factor);

		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	/**
	 * Writes one edge of a box into the buffer, unless it is hidden inside a group of blocks.
	 *
	 * @param consumer     the buffer the line is written to
	 * @param entry        the transformation the line is drawn with
	 * @param fromX        the horizontal start of the line
	 * @param fromY        the vertical start of the line
	 * @param fromZ        the depth of the start of the line
	 * @param toX          the horizontal end of the line
	 * @param toY          the vertical end of the line
	 * @param toZ          the depth of the end of the line
	 * @param argb         the colour of the line
	 * @param lineWidth    the width of the line
	 * @param coveredFaces the sides of the box that are covered by a block of the same kind
	 * @param bothFaces    the two sides the edge lies on
	 */
	private static void edge(VertexConsumer consumer, MatrixStack.Entry entry, double fromX, double fromY,
			double fromZ, double toX, double toY, double toZ, int argb, float lineWidth, int coveredFaces,
			int bothFaces) {
		if ((coveredFaces & bothFaces) == bothFaces) {
			// Both sides of the edge are covered, so the edge is inside the group.
			return;
		}

		segment(consumer, entry, fromX, fromY, fromZ, toX, toY, toZ, argb, lineWidth);
	}

	/**
	 * Writes one straight line into the buffer.
	 *
	 * <p>The shader of the game draws the line from the point that is written to the point plus
	 * the normal that is written with it. The normal is therefore not a direction but the whole
	 * way from one end of the line to the other, and it is written without the transformation of
	 * the matrix, because it belongs to the same space as the points themselves.
	 *
	 * <p>A line whose normal is only a direction is drawn as a line of one block long, whatever
	 * its real length is: the edges of a block are one block long, so they look right, but a line
	 * that reaches from the camera to a block far away shrinks to a small mark.
	 *
	 * @param consumer  the buffer the line is written to
	 * @param entry     the transformation the line is drawn with
	 * @param fromX     the horizontal start of the line
	 * @param fromY     the vertical start of the line
	 * @param fromZ     the depth of the start of the line
	 * @param toX       the horizontal end of the line
	 * @param toY       the vertical end of the line
	 * @param toZ       the depth of the end of the line
	 * @param argb      the colour of the line
	 * @param lineWidth the width of the line
	 */
	private static void segment(VertexConsumer consumer, MatrixStack.Entry entry, double fromX, double fromY,
			double fromZ, double toX, double toY, double toZ, int argb, float lineWidth) {
		float deltaX = (float) (toX - fromX);
		float deltaY = (float) (toY - fromY);
		float deltaZ = (float) (toZ - fromZ);

		consumer.vertex(entry, (float) fromX, (float) fromY, (float) fromZ)
				.color(argb)
				.normal(deltaX, deltaY, deltaZ)
				.lineWidth(lineWidth);
		consumer.vertex(entry, (float) toX, (float) toY, (float) toZ)
				.color(argb)
				.normal(deltaX, deltaY, deltaZ)
				.lineWidth(lineWidth);
	}

	/**
	 * Writes one face of a box into the buffer.
	 *
	 * @param consumer    the buffer the face is written to
	 * @param entry       the transformation the face is drawn with
	 * @param bottomLeftX the horizontal position of the lower left corner
	 * @param bottomLeftY the vertical position of the lower left corner
	 * @param bottomLeftZ the depth of the lower left corner
	 * @param bottomRightX the horizontal position of the lower right corner
	 * @param bottomRightY the vertical position of the lower right corner
	 * @param bottomRightZ the depth of the lower right corner
	 * @param topRightX   the horizontal position of the upper right corner
	 * @param topRightY   the vertical position of the upper right corner
	 * @param topRightZ   the depth of the upper right corner
	 * @param topLeftX    the horizontal position of the upper left corner
	 * @param topLeftY    the vertical position of the upper left corner
	 * @param topLeftZ    the depth of the upper left corner
	 * @param bottomColor the colour of the lower corners
	 * @param topColor    the colour of the upper corners
	 */
	private static void face(VertexConsumer consumer, MatrixStack.Entry entry, double bottomLeftX, double bottomLeftY,
			double bottomLeftZ, double bottomRightX, double bottomRightY, double bottomRightZ, double topRightX,
			double topRightY, double topRightZ, double topLeftX, double topLeftY, double topLeftZ, int bottomColor,
			int topColor) {
		consumer.vertex(entry, (float) bottomLeftX, (float) bottomLeftY, (float) bottomLeftZ).color(bottomColor);
		consumer.vertex(entry, (float) bottomRightX, (float) bottomRightY, (float) bottomRightZ).color(bottomColor);
		consumer.vertex(entry, (float) topRightX, (float) topRightY, (float) topRightZ).color(topColor);
		consumer.vertex(entry, (float) topLeftX, (float) topLeftY, (float) topLeftZ).color(topColor);
	}
}
