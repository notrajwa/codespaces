package com.birowo.client.features.render;

import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import com.birowo.client.core.config.ConfigValue;
import com.birowo.client.core.config.ConfigValues;
import com.birowo.client.core.feature.Feature;
import com.birowo.client.core.feature.FeatureCategory;
import com.birowo.client.platform.render.ShapeRenderer;
import com.birowo.client.platform.render.WorldLayers;
import com.birowo.client.platform.render.WorldRenderable;

/**
 * Marks the target the player is looking at.
 *
 * <p>The target is marked with a tube that is drawn around it: a cylinder with rounded ends, so
 * that the shape has no edges of its own and the target stands inside it rather than being
 * cornered by it. The tube is as wide as the target is and a little wider, and it is as tall as
 * the target is and a little taller, so that it never cuts the target in half. The tube is filled
 * with a faint colour, so that it reads as a solid tube rather than as a cage of lines, and the
 * target is still seen through it.
 *
 * <p>A blue circle is drawn around the target as well, and that circle moves up and down
 * smoothly between the feet and the head of the target. The movement is a wave, so it never
 * jumps and it is the same whatever the number of frames is: the circle is at the same place
 * at the same moment, whether the game runs at thirty frames a second or at three hundred.
 *
 * <p>The shape is drawn in the world without a depth test, so that the target is seen even when
 * it is behind a wall. The test may be turned off, in which case the shape is hidden by what is
 * in front of it.
 */
public final class TargetEspFeature extends Feature implements WorldRenderable {
	/**
	 * How far the shape stands away from the body of the target, in blocks.
	 */
	private static final double MARGIN = 0.22D;

	/**
	 * How many straight lines the round part of the shape is made of.
	 */
	private static final int SEGMENTS = 24;

	/**
	 * How many rings each of the rounded ends is made of.
	 */
	private static final int CAP_SEGMENTS = 6;

	/**
	 * How much taller the rounded ends are than the radius of the body of the shape.
	 *
	 * <p>The ends of the shape are rounded off like the end of a barrel, and not like the end of
	 * a tube: they are taller than they are wide, so that the shape is round all the way down
	 * and all the way up.
	 */
	private static final double ROUNDNESS = 2.0D;

	/**
	 * How many times the circle goes up and down in one second.
	 */
	private static final double CIRCLES_PER_SECOND = 1.0D;

	private final ConfigValue<Integer> colour = setting(ConfigValues.ofColor("colour", 0x007BFF));

	private final ConfigValue<Integer> lineWidth = setting(ConfigValues.ofInt("line_width", 2));

	private final ConfigValue<Double> roundness = setting(ConfigValues.ofDouble("roundness", ROUNDNESS));

	private final ConfigValue<Boolean> fill = setting(ConfigValues.ofBoolean("fill", true));

	private final ConfigValue<Double> fillOpacity = setting(ConfigValues.ofDouble("fill_opacity", 0.22D));

	private final ConfigValue<Boolean> circle = setting(ConfigValues.ofBoolean("circle", true));

	private final ConfigValue<Integer> circleColour = setting(ConfigValues.ofColor("circle_colour", 0x0000FF));

	private final ConfigValue<Double> circleSpeed = setting(ConfigValues.ofDouble("circle_speed", 0.5D));

	private final ConfigValue<Boolean> throughWalls = setting(ConfigValues.ofBoolean("through_walls", true));

	/**
	 * The moment the movement of the circle began, in nanoseconds.
	 */
	private long startNanos;

	public TargetEspFeature() {
		super("targetesp", "Target ESP", "Marks the target the player is looking at.",
				FeatureCategory.RENDER);

		circleSpeed.format(value -> value + " per second");
		roundness.format(value -> value + " times the radius");
		fillOpacity.format(value -> Math.round(value * 100.0D) + "%");
	}

	@Override
	protected void onEnable() {
		startNanos = System.nanoTime();
	}

	@Override
	public void onWorldRender(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client.player == null || client.world == null) {
			return;
		}

		Entity target = target(client);

		if (target == null) {
			return;
		}

		Camera camera = client.gameRenderer.getCamera();
		Vec3d eye = camera.getCameraPos();
		MatrixStack.Entry entry = context.matrices().peek();
		VertexConsumer consumer = context.consumers()
				.getBuffer(throughWalls.value() ? WorldLayers.linesNoDepth() : WorldLayers.linesWithDepth());

		// The box of the target is moved along with it, so that it follows it smoothly between
		// two ticks. The way the game itself does it is followed exactly.
		float delta = client.getRenderTickCounter().getTickProgress(false);
		double offsetX = MathHelper.lerp((double) delta, target.lastRenderX, target.getX()) - target.getX();
		double offsetY = MathHelper.lerp((double) delta, target.lastRenderY, target.getY()) - target.getY();
		double offsetZ = MathHelper.lerp((double) delta, target.lastRenderZ, target.getZ()) - target.getZ();

		Box box = target.getBoundingBox().offset(offsetX, offsetY, offsetZ);

		double centreX = (box.minX + box.maxX) / 2.0D;
		double bottom = box.minY - MARGIN;
		double centreZ = (box.minZ + box.maxZ) / 2.0D;
		double radius = Math.max(box.maxX - box.minX, box.maxZ - box.minZ) / 2.0D + MARGIN;
		double height = (box.maxY - box.minY) + MARGIN * 2.0D;

		int argb = colour.value() | 0xFF000000;
		float width = Math.max(1.0F, lineWidth.value());

		// The shape is drawn around the target, and the world is drawn relative to the camera, so
		// the place of the camera is taken off every point of it.
		if (fill.value()) {
			// The tube is filled, so that the target stands inside it rather than being cornered
			// by a set of lines. The fill is faint, so that the target is still seen through it.
			double opacity = Math.max(0.0D, Math.min(1.0D, fillOpacity.value()));

			int fillColour = ((int) Math.round(opacity * 255.0D) << 24) | (argb & 0xFFFFFF);

			ShapeRenderer.drawFilledRoundedCylinder(
					context.consumers().getBuffer(throughWalls.value()
							? WorldLayers.filledBoxNoDepth()
							: WorldLayers.filledBox()),
					entry,
					centreX - eye.x, bottom - eye.y, centreZ - eye.z,
					radius, height, fillColour, fillColour,
					SEGMENTS, CAP_SEGMENTS, roundness.value());
		}

		ShapeRenderer.drawRoundedCylinder(consumer, entry,
				centreX - eye.x, bottom - eye.y, centreZ - eye.z,
				radius, height, argb, width, SEGMENTS, CAP_SEGMENTS, roundness.value());

		if (circle.value()) {
			drawCircle(consumer, entry, eye, centreX, bottom, centreZ, radius, height, width);
		}
	}

	/**
	 * Draws the circle around the target, which moves up and down.
	 *
	 * @param consumer the buffer the circle is written to
	 * @param entry    the transformation the circle is drawn with
	 * @param eye      the place of the camera in the world
	 * @param centreX  the horizontal centre of the target
	 * @param bottom   the height of the feet of the target
	 * @param centreZ  the depth of the centre of the target
	 * @param radius   the radius of the shape around the target
	 * @param height   the height of the shape around the target
	 * @param width    the width of the line of the circle
	 */
	private void drawCircle(VertexConsumer consumer, MatrixStack.Entry entry, Vec3d eye, double centreX, double bottom,
			double centreZ, double radius, double height, float width) {
		// The wave is worked out from the clock of the machine rather than from the number of
		// frames, so that the circle is at the same place at the same moment whatever the number
		// of frames is.
		double seconds = (System.nanoTime() - startNanos) / 1_000_000_000.0D;
		double phase = seconds * Math.PI * 2.0D * Math.max(0.0D, circleSpeed.value()) * CIRCLES_PER_SECOND;

		// The circle goes up and down between the feet and the head of the target.
		double middle = bottom + height / 2.0D;
		double travel = height / 2.0D - MARGIN;
		double y = middle + travel * Math.sin(phase);

		// The circle lies a little outside the shape, so that it is seen rather than hidden by it.
		double circleRadius = radius + MARGIN;

		ShapeRenderer.drawRing(consumer, entry,
				centreX - eye.x, y - eye.y, centreZ - eye.z,
				circleRadius, circleColour.value() | 0xFF000000, width, SEGMENTS * 2);
	}

	/**
	 * The entity the player is looking at.
	 *
	 * @param client the client the player belongs to
	 * @return the target, or {@code null} when the player is looking at nothing that is alive
	 */
	private Entity target(MinecraftClient client) {
		HitResult hit = client.crosshairTarget;

		if (!(hit instanceof EntityHitResult entityHit)) {
			return null;
		}

		Entity target = entityHit.getEntity();

		if (target == client.player || !target.isAlive()) {
			return null;
		}

		return target;
	}
}
