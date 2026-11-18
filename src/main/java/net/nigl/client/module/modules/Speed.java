package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.NumberSetting;

public class Speed extends Module {
    private final NumberSetting multiplier = add(new NumberSetting("Multiplier", 1.5, 1.0, 4.0, 0.1));

    public Speed() { super("Speed", "Schnelleres Laufen", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var p = mc.player;
        if (p.isInWater() || p.getAbilities().flying) return;
        double fwd = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double str = (mc.options.keyLeft.isDown() ? 1 : 0) - (mc.options.keyRight.isDown() ? 1 : 0);
        if (fwd == 0 && str == 0) return;
        double len = Math.sqrt(fwd * fwd + str * str);
        fwd /= len; str /= len;
        double yaw = Math.toRadians(p.getYRot());
        double speed = 0.2873 * multiplier.value;
        double mx = (-Math.sin(yaw) * fwd + Math.cos(yaw) * str) * speed;
        double mz = (Math.cos(yaw) * fwd + Math.sin(yaw) * str) * speed;
        p.setDeltaMovement(mx, p.getDeltaMovement().y, mz);
    }
}
