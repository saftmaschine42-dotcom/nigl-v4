package net.nigl.client.module.modules;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.NumberSetting;

public class Step extends Module {
    private final NumberSetting height = add(new NumberSetting("Height", 1.0, 0.6, 2.5, 0.1));

    public Step() { super("Step", "Automatisch hoehere Bloecke hochlaufen", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var a = mc.player.getAttribute(Attributes.STEP_HEIGHT);
        if (a != null) a.setBaseValue(height.value);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        var a = mc.player.getAttribute(Attributes.STEP_HEIGHT);
        if (a != null) a.setBaseValue(0.6);
    }
}
