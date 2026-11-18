package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.BoolSetting;

public class Hud extends Module {
    public final BoolSetting watermark = add(new BoolSetting("Watermark", true));
    public final BoolSetting moduleList = add(new BoolSetting("Module List", true));
    public final BoolSetting coords = add(new BoolSetting("Coordinates", true));
    public final BoolSetting fps = add(new BoolSetting("FPS", true));

    public Hud() {
        super("HUD", "Overlay: Watermark, Modulliste, Koordinaten, FPS", Category.CLIENT);
        enabled = true;
    }
}
