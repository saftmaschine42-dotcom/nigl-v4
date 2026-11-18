package net.nigl.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.nigl.client.NiglClient;
import net.nigl.client.module.Modules;
import net.nigl.client.module.modules.BlockEsp;
import net.nigl.client.module.modules.EntityEsp;
import net.nigl.client.module.modules.Freecam;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Zeichnet Boxen und Linien (Tracer) durch Waende. Daten kommen von EntityEsp / BlockEsp. */
public final class EspRenderer {
    public record Box(double x1, double y1, double z1, double x2, double y2, double z2,
                      float r, float g, float b, boolean fill, boolean outline, float px) { }

    public record Line(double ax, double ay, double az, double bx, double by, double bz,
                       float r, float g, float b, float a, float px) { }

    private record Frame(List<Box> boxes, List<Line> lines) { }

    private static final Frame EMPTY = new Frame(List.of(), List.of());
    private static volatile Frame frame = EMPTY;

    private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(NiglClient.ID, "pipeline/esp_through_walls"))
                    .withDepthStencilState(Optional.empty())
                    .build());

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final StagedVertexBuffer BUFFER =
            new StagedVertexBuffer(() -> "Nigl ESP Buffer", RenderType.SMALL_BUFFER_SIZE);
    private static final int MAX_VERTS = 20000;
    private static final float FILL_ALPHA = 0.22f;

    public static void init() {
        LevelExtractionEvents.END_EXTRACTION.register(EspRenderer::extract);
        LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(EspRenderer::render);
    }

    public static void close() { BUFFER.close(); }

    // ---------------- Extraction ----------------
    private static void extract(LevelExtractionContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) { frame = EMPTY; return; }
        Freecam fc = Modules.get(Freecam.class);
        if (fc != null && fc.enabled) fc.frame();
        List<Box> boxes = new ArrayList<>();
        List<Line> lines = new ArrayList<>();
        float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        EntityEsp ee = Modules.get(EntityEsp.class);
        if (ee != null && ee.enabled) ee.collect(boxes, lines, pt);
        BlockEsp be = Modules.get(BlockEsp.class);
        if (be != null && be.enabled) be.collect(boxes, lines, pt);

        frame = new Frame(boxes, lines);
    }

    // ---------------- Drawing ----------------
    private static void render(LevelRenderContext ctx) {
        Frame f = frame;
        if (f.boxes().isEmpty() && f.lines().isEmpty()) return;

        VertexFormat fmt = PIPELINE.getVertexFormatBinding(0);
        if (fmt == null) return;
        PrimitiveTopology prim = PIPELINE.getPrimitiveTopology();
        StagedVertexBuffer.Draw d = BUFFER.appendDraw(fmt, prim,
                prim == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null);

        PoseStack ps = ctx.poseStack();
        Vec3 cam = ctx.levelState().cameraRenderState.pos;
        ps.pushPose();
        Matrix4fc m = ps.last().pose();
        VertexConsumer vc = BUFFER.getVertexBuilder(d);

        int verts = 0;
        for (Box b : f.boxes()) {
            int need = (b.fill() ? 24 : 0) + (b.outline() ? 96 : 0);
            if (verts + need > MAX_VERTS) break;
            if (b.fill()) fillBox(vc, m, b, cam);
            if (b.outline()) edges(vc, m, b, cam);
            verts += need;
        }
        for (Line l : f.lines()) {
            if (verts + 8 > MAX_VERTS) break;
            ribbon(vc, m, l.ax(), l.ay(), l.az(), l.bx(), l.by(), l.bz(), cam, l.px(), l.r(), l.g(), l.b(), l.a());
            verts += 8;
        }
        ps.popPose();

        BUFFER.upload();
        StagedVertexBuffer.ExecuteInfo info = BUFFER.getExecuteInfo(d);
        if (info != null) submit(Minecraft.getInstance(), info);
        BUFFER.endFrame();
    }

    private static void v(VertexConsumer vc, Matrix4fc m, double x, double y, double z, float r, float g, float b, float a) {
        vc.addVertex(m, (float) x, (float) y, (float) z).setColor(r, g, b, a);
    }

    private static void fillBox(VertexConsumer vc, Matrix4fc m, Box bx, Vec3 cam) {
        double x1 = bx.x1() - cam.x, y1 = bx.y1() - cam.y, z1 = bx.z1() - cam.z;
        double x2 = bx.x2() - cam.x, y2 = bx.y2() - cam.y, z2 = bx.z2() - cam.z;
        float r = bx.r(), g = bx.g(), b = bx.b(), a = FILL_ALPHA;
        // Front
        v(vc, m, x1, y1, z2, r, g, b, a); v(vc, m, x2, y1, z2, r, g, b, a); v(vc, m, x2, y2, z2, r, g, b, a); v(vc, m, x1, y2, z2, r, g, b, a);
        // Back
        v(vc, m, x2, y1, z1, r, g, b, a); v(vc, m, x1, y1, z1, r, g, b, a); v(vc, m, x1, y2, z1, r, g, b, a); v(vc, m, x2, y2, z1, r, g, b, a);
        // Left
        v(vc, m, x1, y1, z1, r, g, b, a); v(vc, m, x1, y1, z2, r, g, b, a); v(vc, m, x1, y2, z2, r, g, b, a); v(vc, m, x1, y2, z1, r, g, b, a);
        // Right
        v(vc, m, x2, y1, z2, r, g, b, a); v(vc, m, x2, y1, z1, r, g, b, a); v(vc, m, x2, y2, z1, r, g, b, a); v(vc, m, x2, y2, z2, r, g, b, a);
        // Top
        v(vc, m, x1, y2, z2, r, g, b, a); v(vc, m, x2, y2, z2, r, g, b, a); v(vc, m, x2, y2, z1, r, g, b, a); v(vc, m, x1, y2, z1, r, g, b, a);
        // Bottom
        v(vc, m, x1, y1, z1, r, g, b, a); v(vc, m, x2, y1, z1, r, g, b, a); v(vc, m, x2, y1, z2, r, g, b, a); v(vc, m, x1, y1, z2, r, g, b, a);
    }

    private static void edges(VertexConsumer vc, Matrix4fc m, Box b, Vec3 cam) {
        double[] X = {b.x1(), b.x2()}, Y = {b.y1(), b.y2()}, Z = {b.z1(), b.z2()};
        for (int i = 0; i < 8; i++) {
            for (int bit = 1; bit <= 4; bit <<= 1) {
                if ((i & bit) != 0) continue;
                int j = i | bit;
                ribbon(vc, m, X[i & 1], Y[(i >> 1) & 1], Z[(i >> 2) & 1],
                        X[j & 1], Y[(j >> 1) & 1], Z[(j >> 2) & 1], cam, b.px(), b.r(), b.g(), b.b(), 1f);
            }
        }
    }

    /** Linie als kamerazugewandtes Band, doppelseitig, mit konstanter Pixelbreite. */
    private static void ribbon(VertexConsumer vc, Matrix4fc m, double ax, double ay, double az,
                               double bx, double by, double bz, Vec3 cam, float px,
                               float r, float g, float b, float a) {
        double rax = ax - cam.x, ray = ay - cam.y, raz = az - cam.z;
        double rbx = bx - cam.x, rby = by - cam.y, rbz = bz - cam.z;
        double dx = rbx - rax, dy = rby - ray, dz = rbz - raz;
        double mx = (rax + rbx) / 2, my = (ray + rby) / 2, mz = (raz + rbz) / 2;
        double nx = dy * mz - dz * my, ny = dz * mx - dx * mz, nz = dx * my - dy * mx;
        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1e-6) return;
        nx /= len; ny /= len; nz /= len;
        double ka = 0.00065 * px * Math.sqrt(rax * rax + ray * ray + raz * raz);
        double kb = 0.00065 * px * Math.sqrt(rbx * rbx + rby * rby + rbz * rbz);
        double a1x = rax - nx * ka, a1y = ray - ny * ka, a1z = raz - nz * ka;
        double a2x = rax + nx * ka, a2y = ray + ny * ka, a2z = raz + nz * ka;
        double b2x = rbx + nx * kb, b2y = rby + ny * kb, b2z = rbz + nz * kb;
        double b1x = rbx - nx * kb, b1y = rby - ny * kb, b1z = rbz - nz * kb;
        v(vc, m, a1x, a1y, a1z, r, g, b, a); v(vc, m, a2x, a2y, a2z, r, g, b, a);
        v(vc, m, b2x, b2y, b2z, r, g, b, a); v(vc, m, b1x, b1y, b1z, r, g, b, a);
        v(vc, m, b1x, b1y, b1z, r, g, b, a); v(vc, m, b2x, b2y, b2z, r, g, b, a);
        v(vc, m, a2x, a2y, a2z, r, g, b, a); v(vc, m, a1x, a1y, a1z, r, g, b, a);
    }

    private static void submit(Minecraft client, StagedVertexBuffer.ExecuteInfo info) {
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrixCopy(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        GpuTextureView colorTexture = mainTarget.getColorTextureView();
        if (colorTexture == null) return;

        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> NiglClient.ID + " esp", colorTexture, Optional.empty(),
                        mainTarget.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setVertexBuffer(0, info.vertexBuffer().slice());
            pass.setIndexBuffer(info.indexBuffer(), info.indexType());
            pass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }

    private EspRenderer() { }
}
