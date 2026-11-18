package net.nigl.client.setting;

public class EnumSetting extends Setting {
    public final String[] options;
    public int index;

    public EnumSetting(String name, String def, String... options) {
        super(name);
        this.options = options;
        load(def);
    }

    public String get() { return options[index]; }
    public boolean is(String s) { return get().equalsIgnoreCase(s); }
    public void next() { index = (index + 1) % options.length; }
    public void prev() { index = (index - 1 + options.length) % options.length; }
    @Override public String save() { return get(); }
    @Override public void load(String s) {
        for (int i = 0; i < options.length; i++) if (options[i].equalsIgnoreCase(s)) index = i;
    }
}
