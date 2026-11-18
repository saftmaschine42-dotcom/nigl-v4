package net.nigl.client.module;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.nigl.client.setting.Setting;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class Module {
    public final String name, description;
    public final Category category;
    public boolean enabled;
    public final List<Setting> settings = new ArrayList<>();
    public KeyMapping key;
    public float anim, hover; // GUI-Animationen
    protected final Minecraft mc = Minecraft.getInstance();

    protected Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }

    public String id() { return name.toLowerCase(Locale.ROOT).replace(" ", ""); }
    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        if (e) onEnable(); else onDisable();
    }

    protected <S extends Setting> S add(S s) { settings.add(s); return s; }

    public void onEnable() { }
    public void onDisable() { }
    public void onTick() { }
    public void onPreTick() { }
    public void onWorldLeave() { }
}
