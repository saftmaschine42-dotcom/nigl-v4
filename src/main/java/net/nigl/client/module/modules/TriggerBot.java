package net.nigl.client.module.modules;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.module.Targets;
import net.nigl.client.setting.BoolSetting;

public class TriggerBot extends Module {
    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting hostile = add(new BoolSetting("Hostile", true));
    private final BoolSetting passive = add(new BoolSetting("Passive", false));

    public TriggerBot() { super("TriggerBot", "Schlaegt automatisch, wenn dein Fadenkreuz ein Ziel trifft", Category.COMBAT); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (mc.gameMode == null || p.getAttackStrengthScale(0.5f) < 1f) return;
        if (mc.hitResult instanceof EntityHitResult hr && hr.getEntity() instanceof LivingEntity le && le.isAlive()
                && Targets.match(le, players.value, hostile.value, passive.value)) {
            mc.gameMode.attack(p, le);
            p.swing(InteractionHand.MAIN_HAND);
        }
    }
}
