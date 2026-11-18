package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class AutoRespawn extends Module {
    public AutoRespawn() { super("AutoRespawn", "Respawnt sofort nach dem Tod", Category.PLAYER); }

    @Override
    public void onTick() {
        if (mc.player.isDeadOrDying()) mc.player.respawn();
    }
}
