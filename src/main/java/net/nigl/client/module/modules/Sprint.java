package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", "Sprintet automatisch beim Vorwaertslaufen", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p == null) return;
        if (mc.options.keyUp.isDown() && !p.isShiftKeyDown() && !p.horizontalCollision) p.setSprinting(true);
    }
}
