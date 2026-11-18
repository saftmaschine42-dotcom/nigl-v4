package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.NumberSetting;

public class Spider extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.2, 0.1, 0.6, 0.05));

    public Spider() { super("Spider", "An Waenden hochklettern", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p.horizontalCollision) {
            var v = p.getDeltaMovement();
            p.setDeltaMovement(v.x, speed.value, v.z);
        }
    }
}
