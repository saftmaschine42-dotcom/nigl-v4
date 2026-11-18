package net.nigl.client.module.modules;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.render.EspRenderer.Box;
import net.nigl.client.render.EspRenderer.Line;
import net.nigl.client.setting.BoolSetting;
import net.nigl.client.setting.EnumSetting;
import net.nigl.client.setting.ListSetting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.nigl.client.Theme;
import java.util.HashMap;
import java.util.Map;
import net.nigl.client.setting.NumberSetting;

import java.util.List;

public class EntityEsp extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile", true));
    private final BoolSetting passive = add(new BoolSetting("Passive", false));
    private final BoolSetting items = add(new BoolSetting("Items", false));
    private final ListSetting entities = add(new ListSetting("Entities", ListSetting.Kind.ENTITY));
    private final Map<EntityType<?>, Integer> specific = new HashMap<>();
    private final EnumSetting mode = add(new EnumSetting("Mode", "Both", "Box", "Filled", "Both"));
    private final BoolSetting tracers = add(new BoolSetting("Tracers", true));
    private final NumberSetting range = add(new NumberSetting("Range", 80, 16, 200, 4));
    private final NumberSetting thickness = add(new NumberSetting("Thickness", 1.5, 1, 4, 0.5));

    public EntityEsp() { super("EntityESP", "Boxen + Tracer fuer Spieler/Mobs/Items durch Waende", Category.RENDER); }

    private void rebuild() {
        specific.clear();
        for (EntityType<?> t : BuiltInRegistries.ENTITY_TYPE) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(t).toString();
            if (entities.has(id)) specific.put(t, Theme.hsv(((id.hashCode() >>> 3) & 1023) / 1023f, 0.55f, 1f) & 0xFFFFFF);
        }
        entities.dirty = false;
    }

    private int colorOf(Entity e) {
        Integer sc = specific.get(e.getType());
        if (sc != null) return sc;
        if (e instanceof Player) return players.value ? 0x7FE8FF : 0;
        if (e instanceof Enemy) return hostile.value ? 0xFF5C5C : 0;
        if (e instanceof LivingEntity) return passive.value ? 0x5CFF9A : 0;
        if (e instanceof ItemEntity) return items.value ? 0xFFD54D : 0;
        return 0;
    }

    public void collect(List<Box> boxes, List<Line> lines, float pt) {
        if (entities.dirty) rebuild();
        var p = mc.player;
        double ex = Mth.lerp(pt, p.xo, p.getX());
        double ey = Mth.lerp(pt, p.yo, p.getY()) + p.getEyeHeight();
        double ez = Mth.lerp(pt, p.zo, p.getZ());
        Vec3 view = p.getViewVector(pt);
        double sx = ex + view.x * 2, sy = ey + view.y * 2, sz = ez + view.z * 2;

        boolean fill = !mode.is("Box"), outline = !mode.is("Filled");
        float px = (float) thickness.value;

        for (Entity e : mc.level.getEntities(p, p.getBoundingBox().inflate(range.value))) {
            if (e == p || e.isRemoved() || p.distanceTo(e) > range.value) continue;
            int c = colorOf(e);
            if (c == 0) continue;
            float r = ((c >> 16) & 255) / 255f, g = ((c >> 8) & 255) / 255f, b = (c & 255) / 255f;
            AABB bb = e.getBoundingBox().move(
                    Mth.lerp(pt, e.xo, e.getX()) - e.getX(),
                    Mth.lerp(pt, e.yo, e.getY()) - e.getY(),
                    Mth.lerp(pt, e.zo, e.getZ()) - e.getZ());
            boxes.add(new Box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, r, g, b, fill, outline, px));
            if (tracers.value) {
                lines.add(new Line(sx, sy, sz, (bb.minX + bb.maxX) / 2, (bb.minY + bb.maxY) / 2, (bb.minZ + bb.maxZ) / 2,
                        r, g, b, 0.9f, px));
            }
        }
    }
}
