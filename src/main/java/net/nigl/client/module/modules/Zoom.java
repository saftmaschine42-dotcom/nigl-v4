package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.NumberSetting;

public class Zoom extends Module {
    private final NumberSetting fov = add(new NumberSetting("FOV", 30, 30, 90, 1));
    private Integer old;

    public Zoom() { super("Zoom", "Heranzoomen (FOV senken)", Category.RENDER); }

    @Override public void onEnable() { old = mc.options.fov().get(); }
    @Override public void onTick() { if (old == null) old = mc.options.fov().get(); mc.options.fov().set((int) fov.value); }
    @Override public void onDisable() { if (old != null) mc.options.fov().set(old); old = null; }
}
