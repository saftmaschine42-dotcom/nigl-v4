package net.nigl.client.setting;

import java.util.LinkedHashSet;
import java.util.Set;

/** Auswahlliste (Bloecke oder Entities), wird im Picker-Screen mit Suche bearbeitet. */
public class ListSetting extends Setting {
    public enum Kind { BLOCK, ENTITY }

    public final Kind kind;
    public final Set<String> ids = new LinkedHashSet<>();
    public volatile boolean dirty = true;

    public ListSetting(String name, Kind kind, String... defaults) {
        super(name);
        this.kind = kind;
        for (String d : defaults) ids.add(d);
    }

    public boolean has(String id) { return ids.contains(id); }
    public void toggle(String id) { if (!ids.remove(id)) ids.add(id); dirty = true; }
    public void add(String id) { ids.add(id); dirty = true; }
    public void clear() { ids.clear(); dirty = true; }

    @Override public String save() { return String.join(",", ids); }

    @Override public void load(String s) {
        ids.clear();
        for (String p : s.split(",")) if (!p.isBlank()) ids.add(p.trim());
        dirty = true;
    }
}
