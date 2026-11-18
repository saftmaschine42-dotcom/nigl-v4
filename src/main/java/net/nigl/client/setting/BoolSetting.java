package net.nigl.client.setting;

public class BoolSetting extends Setting {
    public boolean value;
    public float anim = -1f; // GUI-Animation
    public BoolSetting(String name, boolean def) { super(name); this.value = def; this.anim = def ? 1f : 0f; }
    @Override public String save() { return Boolean.toString(value); }
    @Override public void load(String s) { value = Boolean.parseBoolean(s); }
}
