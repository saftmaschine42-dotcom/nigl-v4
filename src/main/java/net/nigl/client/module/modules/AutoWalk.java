package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class AutoWalk extends Module {
    public AutoWalk() { super("AutoWalk", "Laeuft automatisch vorwaerts", Category.MOVEMENT); }

    @Override public void onTick() { mc.options.keyUp.setDown(true); }
    @Override public void onDisable() { mc.options.keyUp.setDown(false); }
}
