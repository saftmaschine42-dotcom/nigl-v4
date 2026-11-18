package net.nigl.client.module.modules;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.module.Targets;
import net.nigl.client.setting.BoolSetting;
import net.nigl.client.setting.EnumSetting;
import net.nigl.client.setting.NumberSetting;

public class KillAura extends Module {
    private final NumberSetting range = add(new NumberSetting("Range", 4.5, 1, 6, 0.1));
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile", true));
    private final BoolSetting passive = add(new BoolSetting("Passive", false));
    private final BoolSetting rotate = add(new BoolSetting("Rotate", true));
    private final EnumSetting priority = add(new EnumSetting("Priority", "Distance", "Distance", "Health"));

    public KillAura() { super("KillAura", "Greift Ziele in Reichweite automatisch an", Category.COMBAT); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) return;
        if (p.getAttackStrengthScale(0.5f) < 1f) return;

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity e : mc.level.getEntities(p, p.getBoundingBox().inflate(range.value))) {
            if (!(e instanceof LivingEntity le) || !le.isAlive()) continue;
            if (!Targets.match(le, players.value, hostile.value, passive.value)) continue;
            float dist = p.distanceTo(le);
            if (dist > range.value) continue;
            double score = priority.is("Health") ? le.getHealth() : dist;
            if (score < bestScore) { bestScore = score; best = le; }
        }
        if (best == null) return;

        if (rotate.value) {
            double dx = best.getX() - p.getX();
            double dz = best.getZ() - p.getZ();
            double dy = best.getY() + best.getBbHeight() / 2.0 - p.getEyeY();
            p.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
            p.setXRot((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));
        }
        mc.gameMode.attack(p, best);
        p.swing(InteractionHand.MAIN_HAND);
    }
}
