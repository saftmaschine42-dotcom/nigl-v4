package net.nigl.client.setting;

import java.util.Locale;

public abstract class Setting {
    public final String name;
    protected Setting(String name) { this.name = name; }
    public String key() { return name.toLowerCase(Locale.ROOT).replace(" ", ""); }
    public abstract String save();
    public abstract void load(String s);
}
