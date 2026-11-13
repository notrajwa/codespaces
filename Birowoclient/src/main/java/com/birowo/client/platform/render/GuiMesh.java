package com.birowo.client.platform.render;

import java.util.Optional;

import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;

/**
 * A shape of the interface, drawn as a mesh of triangles rather than as a picture of squares.
 *
 * <p>The game draws the interface out of rectangles, and a rectangle always has whole pixels as
 * its edges. A shape that is made of such rectangles therefore steps along the pixels: the edge
 * of a rounded corner is a staircase, and a circle is a shape with flat sides. The mesh here is
 * made of triangles whose corners may lie anywhere, so the edge of a shape follows the curve it is
 * meant to follow.
 *
 * <p>The edge of a shape is smooth because the colour of the points along it fades: the outermost
 * ring of a shape is drawn with no colour at all and the ring just inside it with the full colour,
 * and the triangles between the two rings blend the one into the other.
 *
 * <p>Every shape is one element of the interface and therefore one piece of work for the game to
 * sort, however many triangles it is made of.
 */
public final class GuiMesh implements SimpleGuiElementRenderState {
	/**
	 * The pipeline the mesh is drawn with.
	 *
	 * <p>It is the pipeline of the game with one change: the faces of the mesh are not culled, so
	 * that the triangles of a shape may be wound in whichever way is the simplest to write.
	 */
	private static final RenderPipeline PIPELINE = buildPipeline();

	/**
	 * The point that the points of the mesh are worked out with.
	 */
	private static final Vector2f POINT = new Vector2f();

	/**
	 * How many points the mesh can hold before it has to grow.
	 */
	private static final int INITIAL_POINTS = 256;

	/**
	 * The transformation the shape is drawn with.
	 */
	private final Matrix3x2fc pose;

	/**
	 * The part of the screen the shape is cut to.
	 */
	private final ScreenRect scissor;

	/**
	 * The horizontal places of the points of the mesh.
	 */
	private float[] xs = new float[INITIAL_POINTS];

	/**
	 * The vertical places of the points of the mesh.
	 */
	private float[] ys = new float[INITIAL_POINTS];

	/**
	 * The colours of the points of the mesh.
	 */
	private int[] colours = new int[INITIAL_POINTS];

	/**
	 * How many points the mesh holds.
	 */
	private int count;

	/**
	 * The left edge of the shape.
	 */
	private float minX = Float.MAX_VALUE;

	/**
	 * The top edge of the shape.
	 */
	private float minY = Float.MAX_VALUE;

	/**
	 * The right edge of the shape.
	 */
	private float maxX = -Float.MAX_VALUE;

	/**
	 * The bottom edge of the shape.
	 */
	private float maxY = -Float.MAX_VALUE;

	GuiMesh(Matrix3x2fc pose, ScreenRect scissor) {
		this.pose = pose;
		this.scissor = scissor;
	}

	/**
	 * Builds the pipeline the mesh is drawn with.
	 *
	 * @return the pipeline
	 */
	private static RenderPipeline buildPipeline() {
		RenderPipeline gui = RenderPipelines.GUI;

		RenderPipeline.Snippet snippet = new RenderPipeline.Snippet(
				Optional.of(gui.getVertexShader()),
				Optional.of(gui.getFragmentShader()),
				Optional.of(gui.getShaderDefines()),
				Optional.of(gui.getSamplers()),
				Optional.of(gui.getUniforms()),
				gui.getBlendFunction(),
				Optional.of(gui.getDepthTestFunction()),
				Optional.of(gui.getPolygonMode()),
				Optional.of(Boolean.FALSE),
				Optional.of(gui.isWriteColor()),
				Optional.of(gui.isWriteAlpha()),
				Optional.of(gui.isWriteDepth()),
				Optional.of(gui.getColorLogic()),
				Optional.of(gui.getVertexFormat()),
				Optional.of(gui.getVertexFormatMode()));

		return RenderPipeline.builder(snippet)
				.withLocation(Identifier.of("birowo", "gui_mesh"))
				.withCull(false)
				.build();
	}

	@Override
	public void setupVertices(VertexConsumer consumer) {
		for (int point = 0; point < count; point++) {
			consumer.vertex(xs[point], ys[point], 0.0F).color(colours[point]);
		}
	}

	@Override
	public RenderPipeline pipeline() {
		return PIPELINE;
	}

	@Override
	public TextureSetup textureSetup() {
		return TextureSetup.empty();
	}

	@Override
	public ScreenRect scissorArea() {
		return scissor;
	}

	@Override
	public ScreenRect bounds() {
		if (count == 0) {
			return ScreenRect.empty();
		}

		int left = (int) Math.floor(minX);
		int top = (int) Math.floor(minY);

		return new ScreenRect(left, top, (int) Math.ceil(maxX) - left, (int) Math.ceil(maxY) - top);
	}

	/**
	 * Adds a triangle to the mesh.
	 *
	 * <p>The three points are given in the space of the shape and are turned into the space of the
	 * screen before they are stored.
	 *
	 * @param x0    the horizontal place of the first point
	 * @param y0    the vertical place of the first point
	 * @param c0    the colour of the first point
	 * @param x1    the horizontal place of the second point
	 * @param y1    the vertical place of the second point
	 * @param c1    the colour of the second point
	 * @param x2    the horizontal place of the third point
	 * @param y2    the vertical place of the third point
	 * @param c2    the colour of the third point
	 */
	public void triangle(float x0, float y0, int c0, float x1, float y1, int c1, float x2, float y2, int c2) {
		// The game draws four points at a time, so a triangle is written as a quadrilateral whose
		// last two points are the same. The half of it that is left over covers no area at all.
		point(x0, y0, c0);
		point(x1, y1, c1);
		point(x2, y2, c2);
		point(x2, y2, c2);
	}

	/**
	 * Adds a quadrilateral to the mesh.
	 *
	 * @param x0 the horizontal place of the first point
	 * @param y0 the vertical place of the first point
	 * @param c0 the colour of the first point
	 * @param x1 the horizontal place of the second point
	 * @param y1 the vertical place of the second point
	 * @param c1 the colour of the second point
	 * @param x2 the horizontal place of the third point
	 * @param y2 the vertical place of the third point
	 * @param c2 the colour of the third point
	 * @param x3 the horizontal place of the fourth point
	 * @param y3 the vertical place of the fourth point
	 * @param c3 the colour of the fourth point
	 */
	public void quad(float x0, float y0, int c0, float x1, float y1, int c1, float x2, float y2, int c2, float x3,
			float y3, int c3) {
		point(x0, y0, c0);
		point(x1, y1, c1);
		point(x2, y2, c2);
		point(x3, y3, c3);
	}

	/**
	 * Adds one point to the mesh.
	 *
	 * @param x      the horizontal place of the point
	 * @param y      the vertical place of the point
	 * @param colour the colour of the point
	 */
	private void point(float x, float y, int colour) {
		if (count == xs.length) {
			grow();
		}

		pose.transformPosition(x, y, POINT);

		xs[count] = POINT.x;
		ys[count] = POINT.y;
		colours[count] = colour;
		count++;

		minX = Math.min(minX, POINT.x);
		minY = Math.min(minY, POINT.y);
		maxX = Math.max(maxX, POINT.x);
		maxY = Math.max(maxY, POINT.y);
	}

	/**
	 * Makes the mesh twice as big.
	 */
	private void grow() {
		int size = xs.length * 2;

		float[] grownXs = new float[size];
		float[] grownYs = new float[size];
		int[] grownColours = new int[size];

		System.arraycopy(xs, 0, grownXs, 0, count);
		System.arraycopy(ys, 0, grownYs, 0, count);
		System.arraycopy(colours, 0, grownColours, 0, count);

		xs = grownXs;
		ys = grownYs;
		colours = grownColours;
	}
}
