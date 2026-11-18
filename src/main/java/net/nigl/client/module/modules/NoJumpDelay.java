package net.nigl.client.module.modules;

import net.minecraft.world.entity.LivingEntity;
import net.nigl.client.Refl;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class NoJumpDelay extends Module {
    public NoJumpDelay() { super("NoJumpDelay", "Kein Delay zwischen Spruengen (Bunny-Hop)", Category.MOVEMENT); }

    @Override public void onTick() { Refl.setInt(mc.player, LivingEntity.class, "noJumpDelay", 0); }
}
