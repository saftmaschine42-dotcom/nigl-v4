package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.BoolSetting;
import net.nigl.client.setting.NumberSetting;

public class Flight extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 2.0, 0.5, 10, 0.1));
    public final BoolSetting antiFall = add(new BoolSetting("Anti Fall Damage", true));

    public Flight() { super("Flight", "Fliegen wie im Creative (Server: allow-flight=true)", Category.MOVEMENT); }

    @Override
    public void onTick() {
        var a = mc.player.getAbilities();
        a.mayfly = true;
        a.flying = true;
        a.setFlyingSpeed((float) (speed.value * 0.05));
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        var a = mc.player.getAbilities();
        a.setFlyingSpeed(0.05f);
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            a.mayfly = false;
            a.flying = false;
        }
    }
}
