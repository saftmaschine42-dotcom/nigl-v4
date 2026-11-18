package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.NumberSetting;

public class Glide extends Module {
    private final NumberSetting fall = add(new NumberSetting("Fall Speed", 0.08, 0.02, 0.3, 0.01));

    public Glide() { super("Glide", "Langsam herunterschweben", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p.getAbilities().flying) return;
        var v = p.getDeltaMovement();
        if (v.y < -fall.value) p.setDeltaMovement(v.x, -fall.value, v.z);
    }
}
