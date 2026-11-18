package net.nigl.client.module.modules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.nigl.client.Theme;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.render.EspRenderer.Box;
import net.nigl.client.render.EspRenderer.Line;
import net.nigl.client.setting.BoolSetting;
import net.nigl.client.setting.ListSetting;
import net.nigl.client.setting.NumberSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockEsp extends Module {
    private final ListSetting blocks = add(new ListSetting("Blocks", ListSetting.Kind.BLOCK,
            "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
            "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
            "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore",
            "minecraft:ancient_debris",
            "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:barrel",
            "minecraft:spawner"));
    private final NumberSetting range = add(new NumberSetting("Range", 24, 8, 48, 2));
    private final NumberSetting max = add(new NumberSetting("Max Blocks", 120, 20, 300, 10));
    private final BoolSetting fill = add(new BoolSetting("Fill", true));
    private final BoolSetting tracers = add(new BoolSetting("Tracers", false));
    private final NumberSetting thickness = add(new NumberSetting("Thickness", 1.5, 1, 4, 0.5));

    private record Found(int x, int y, int z, int color) { }

    private final Map<Block, Integer> active = new HashMap<>();
    private static final int CYCLE = 10;
    private volatile List<Found> result = List.of();
    private List<Found> building = new ArrayList<>();
    private int phase = 0, cx, cy, cz;

    public BlockEsp() { super("BlockESP", "Markiert ausgewaehlte Bloecke durch Waende (Auswahl mit Suche)", Category.RENDER); }

    private static int colorFor(String id) {
        String n = id.substring(id.indexOf(':') + 1);
        if (n.contains("diamond")) return 0x4DE1FF;
        if (n.contains("emerald")) return 0x4DFF7A;
        if (n.contains("gold")) return 0xFFD54D;
        if (n.contains("iron")) return 0xE8C4A8;
        if (n.contains("coal")) return 0x9A9AA8;
        if (n.contains("redstone")) return 0xFF3B3B;
        if (n.contains("lapis")) return 0x4D6BFF;
        if (n.contains("copper")) return 0xFF8A4D;
        if (n.contains("debris")) return 0xD08A5C;
        if (n.contains("quartz")) return 0xFFFFFF;
        if (n.contains("chest") || n.contains("barrel")) return 0xFFB74D;
        if (n.contains("spawner")) return 0xB04DFF;
        return Theme.hsv(((id.hashCode() >>> 3) & 1023) / 1023f, 0.55f, 1f) & 0xFFFFFF;
    }

    private void rebuild() {
        active.clear();
        for (Block b : BuiltInRegistries.BLOCK) {
            String id = BuiltInRegistries.BLOCK.getKey(b).toString();
            if (blocks.has(id)) active.put(b, colorFor(id));
        }
        blocks.dirty = false;
        building = new ArrayList<>();
        phase = 0;
    }

    @Override
    public void onTick() {
        if (blocks.dirty) rebuild();
        var p = mc.player;
        int R = (int) range.value;
        if (phase == 0) {
            building = new ArrayList<>();
            cx = Mth.floor(p.getX()); cy = Mth.floor(p.getY()); cz = Mth.floor(p.getZ());
        }
        int limit = (int) max.value;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -R; dx <= R; dx++) {
            if (Math.floorMod(dx + R, CYCLE) != phase) continue;
            for (int dz = -R; dz <= R; dz++) {
                for (int dy = -R; dy <= R; dy++) {
                    if (building.size() >= limit) break;
                    pos.set(cx + dx, cy + dy, cz + dz);
                    var state = mc.level.getBlockState(pos);
                    if (state.isAir()) continue;
                    Integer c = active.get(state.getBlock());
                    if (c != null) building.add(new Found(pos.getX(), pos.getY(), pos.getZ(), c));
                }
            }
        }
        if (++phase >= CYCLE) { phase = 0; result = building; }
    }

    @Override public void onDisable() { result = List.of(); building = new ArrayList<>(); phase = 0; }

    public void collect(List<Box> boxes, List<Line> lines, float pt) {
        List<Found> found = result;
        if (found.isEmpty()) return;
        var p = mc.player;
        double ex = Mth.lerp(pt, p.xo, p.getX());
        double ey = Mth.lerp(pt, p.yo, p.getY()) + p.getEyeHeight();
        double ez = Mth.lerp(pt, p.zo, p.getZ());
        Vec3 view = p.getViewVector(pt);
        double sx = ex + view.x * 2, sy = ey + view.y * 2, sz = ez + view.z * 2;
        float px = (float) thickness.value;
        for (Found f : found) {
            float r = ((f.color() >> 16) & 255) / 255f, g = ((f.color() >> 8) & 255) / 255f, b = (f.color() & 255) / 255f;
            boxes.add(new Box(f.x() - 0.002, f.y() - 0.002, f.z() - 0.002, f.x() + 1.002, f.y() + 1.002, f.z() + 1.002,
                    r, g, b, fill.value, true, px));
            if (tracers.value) lines.add(new Line(sx, sy, sz, f.x() + 0.5, f.y() + 0.5, f.z() + 0.5, r, g, b, 0.9f, px));
        }
    }
}
