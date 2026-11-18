package net.nigl.client.setting;

public class NumberSetting extends Setting {
    public double value;
    public final double min, max, step;

    public NumberSetting(String name, double def, double min, double max, double step) {
        super(name);
        this.min = min; this.max = max; this.step = step;
        set(def);
    }

    public void set(double v) {
        v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    @Override public String save() { return Double.toString(value); }
    @Override public void load(String s) {
        try { set(Double.parseDouble(s)); } catch (NumberFormatException ignored) { }
    }
}
