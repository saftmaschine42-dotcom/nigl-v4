package net.nigl.client.module.modules;

import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.setting.BoolSetting;
import net.nigl.client.setting.EnumSetting;
import net.nigl.client.setting.NumberSetting;

public class GuiSettings extends Module {
    public final EnumSetting accent = add(new EnumSetting("Accent", "White", "White", "Blue", "Mint", "Pink", "Orange", "Purple"));
    public final NumberSetting glass = add(new NumberSetting("Glass", 0.75, 0.3, 1.0, 0.05));
    public final BoolSetting rainbow = add(new BoolSetting("Rainbow", false));

    public GuiSettings() {
        super("GUI", "Design: Akzentfarbe, Glas-Transparenz, Regenbogen", Category.CLIENT);
        enabled = true;
    }

    @Override public void setEnabled(boolean e) { enabled = true; }
}
