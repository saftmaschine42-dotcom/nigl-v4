package net.nigl.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.nigl.client.Config;
import net.nigl.client.Draw;
import net.nigl.client.NiglClient;
import net.nigl.client.Theme;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.module.Modules;
import net.nigl.client.setting.*;

import java.util.*;

public class ClickGui extends Screen {
    private static final int W = 118, HH = 24, MH = 19, SH = 18, SH_NUM = 21;
    private static final Map<Category, int[]> POS = new EnumMap<>(Category.class);
    private static final Set<Module> OPEN = new HashSet<>();
    private static final Map<Module, Float> FADE = new HashMap<>();
    private static int scroll = 0;

    // kind: 0 = Header, 1 = Modul, 2 = Setting, 3 = Panel
    private record Row(int kind, Category cat, Module mod, Setting set, int x, int y, int w, int h) {
        boolean hit(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private Category dragCat;
    private int dragDx, dragDy;
    private NumberSetting dragSlider;
    private int sliderX, sliderW;
    private long lastNs = System.nanoTime();
    private Module listening;

    public ClickGui() { super(Component.literal(NiglClient.NAME)); }

    private int cell() { return Math.max(3, Math.min(8, height / 55)); }
    private int top() { return 10 + 7 * cell() + 36; }

    private List<Row> layout() {
        int perRow = Math.max(1, (width - 8) / (W + 8));
        int n = Math.min(perRow, Category.values().length);
        int startX = (width - (n * (W + 8) - 8)) / 2;
        int i = 0;
        for (Category c : Category.values()) {
            if (!POS.containsKey(c))
                POS.put(c, new int[]{startX + (i % perRow) * (W + 8), top() + (i / perRow) * 160});
            i++;
        }
        List<Row> rows = new ArrayList<>();
        for (Category c : Category.values()) {
            int[] p = POS.get(c);
            int x = p[0], y = p[1] + scroll, start = rows.size();
            rows.add(new Row(0, c, null, null, x, y, W, HH));
            int cy = y + HH;
            for (Module m : Modules.inCategory(c)) {
                rows.add(new Row(1, c, m, null, x, cy, W, MH));
                cy += MH;
                if (OPEN.contains(m)) {
                    for (Setting s : m.settings) {
                        int h = s instanceof NumberSetting ? SH_NUM : SH;
                        rows.add(new Row(2, c, m, s, x, cy, W, h));
                        cy += h;
                    }
                }
            }
            cy += 6;
            rows.add(start, new Row(3, c, null, null, x, y, W, cy - y));
        }
        return rows;
    }

    private String bindLabel(Module m) {
        if (listening == m) return "...";
        if (m.key == null || m.key.isUnbound()) return "";
        String t = m.key.getTranslatedKeyMessage().getString();
        return t.length() > 4 ? t.substring(0, 4) : t;
    }

    private static String fmt(NumberSetting n) {
        return n.step >= 1 ? Integer.toString((int) n.value) : String.format(Locale.ROOT, "%.2f", n.value);
    }

    private void drawTitle(GuiGraphicsExtractor g) {
        int c = cell();
        Draw.logo(g, (width - Draw.logoWidth(c)) / 2, 10, c);

        String s1 = "C L I E N T", v = NiglClient.MC_VERSION;
        int tw = Theme.width(s1) + Theme.width(v) + 34;
        int px = (width - tw) / 2, sy = 10 + 7 * c + 9;
        Draw.glass(g, px, sy, tw, 17, 8);
        Theme.text(g, s1, px + 11, sy + 5, Theme.TEXT);
        int vx = px + 11 + Theme.width(s1) + 7;
        Draw.roundRect(g, vx - 4, sy + 3, Theme.width(v) + 8, 11, 5, 0x55FFFFFF);
        Theme.text(g, v, vx, sy + 5, 0xFFFFFFFF);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        super.extractRenderState(g, mx, my, delta); // vanilla Blur = Milchglas-Hintergrund
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastNs) / 1e9f);
        lastNs = now;
        float k = Math.min(1f, dt * 14f);

        drawTitle(g);

        String hoverDesc = null;
        for (Row r : layout()) {
            boolean hov = r.hit(mx, my);
            switch (r.kind) {
                case 3 -> {
                    Draw.glass(g, r.x, r.y, r.w, r.h, 12);
                    Draw.shimmer(g, r.x, r.y, r.w, 12);
                }
                case 0 -> {
                    String t = r.cat.label.toUpperCase(Locale.ROOT);
                    Theme.bold(g, t, r.x + (r.w - Theme.width(t)) / 2, r.y + 8, Theme.TEXT);
                    g.fill(r.x + 14, r.y + HH - 1, r.x + r.w - 14, r.y + HH, 0x30FFFFFF);
                }
                case 1 -> {
                    Module m = r.mod;
                    m.anim += ((m.enabled ? 1f : 0f) - m.anim) * k;
                    m.hover += ((hov ? 1f : 0f) - m.hover) * k;
                    int acc = Theme.accent(0.2f);
                    int x0 = r.x + 6, w0 = r.w - 12, y0 = r.y + 1, h0 = r.h - 2, rr = h0 / 2;
                    if (m.anim > 0.03f) Draw.roundRect(g, x0 - 2, y0 - 2, w0 + 4, h0 + 4, rr + 2, Theme.withAlpha(acc, (int) (0x2C * m.anim)));
                    int off = Theme.withAlpha(0xFFFFFF, (int) (0x1A + 0x22 * m.hover));
                    Draw.roundRect(g, x0, y0, w0, h0, rr, Theme.lerp(off, Theme.withAlpha(acc, 0xE8), m.anim));
                    if (m.anim > 0.05f) g.fill(x0 + rr, y0, x0 + w0 - rr, y0 + 1, Theme.withAlpha(0xFFFFFF, (int) (0xB0 * m.anim)));
                    int tcol = Theme.lerp(0xFFEEF0FA, Theme.onAccent(acc), m.anim);
                    Theme.text(g, m.name, r.x + 14, r.y + (r.h - 8) / 2, tcol, m.anim < 0.5f);
                    if (!m.settings.isEmpty())
                        Theme.text(g, OPEN.contains(m) ? "-" : "+", r.x + r.w - 17, r.y + (r.h - 8) / 2, tcol, m.anim < 0.5f);
                    String bl = bindLabel(m);
                    if (!bl.isEmpty()) {
                        int tw2 = Theme.width(bl) + 8;
                        int edge = r.x + r.w - (m.settings.isEmpty() ? 12 : 22);
                        Draw.roundRect(g, edge - tw2, y0 + 3, tw2, h0 - 6, (h0 - 6) / 2, Theme.withAlpha(tcol, 0x28));
                        Theme.text(g, bl, edge - tw2 + 4, r.y + (r.h - 8) / 2, tcol, false);
                    }
                    if (hov) hoverDesc = m.description;
                }
                case 2 -> {
                    Module m = r.mod;
                    float f = FADE.merge(m, 0f, (a, b) -> Math.min(1f, a + dt * 6f));
                    int acc = Theme.accent(0.4f);
                    int tc = Theme.scaleAlpha(Theme.TEXT, f);
                    Setting s = r.set;
                    int by = r.y + 1, bh = r.h - 3;
                    if (s instanceof BoolSetting b) {
                        b.anim += ((b.value ? 1f : 0f) - b.anim) * k;
                        Draw.roundRect(g, r.x + 8, by, r.w - 16, bh, bh / 2, Theme.scaleAlpha(0x22FFFFFF, f));
                        Theme.text(g, b.name, r.x + 15, by + (bh - 8) / 2, tc);
                        int tx = r.x + r.w - 12 - 22, ty = by + (bh - 12) / 2;
                        Draw.roundRect(g, tx, ty, 22, 12, 6, Theme.scaleAlpha(Theme.lerp(0x55FFFFFF, Theme.withAlpha(acc, 0xE8), b.anim), f));
                        Draw.roundRect(g, tx + 1 + (int) (b.anim * 10), ty + 1, 10, 10, 5, Theme.scaleAlpha(0xFFFFFFFF, f));
                    } else if (s instanceof NumberSetting n) {
                        int sx = r.x + 8, sw = r.w - 16, sy = r.y + 2, sh = r.h - 4;
                        Draw.roundRect(g, sx, sy, sw, sh, sh / 2, Theme.scaleAlpha(0x28FFFFFF, f));
                        int fw = (int) ((n.value - n.min) / (n.max - n.min) * sw);
                        if (fw > 2) Draw.roundRect(g, sx, sy, fw, sh, sh / 2, Theme.scaleAlpha(Theme.withAlpha(acc, 0x78), f));
                        Theme.text(g, n.name, sx + 7, sy + (sh - 8) / 2, tc);
                        String v = fmt(n);
                        Theme.text(g, v, sx + sw - 7 - Theme.width(v), sy + (sh - 8) / 2, tc);
                    } else if (s instanceof ListSetting ls) {
                        Draw.roundRect(g, r.x + 8, by, r.w - 16, bh, bh / 2, Theme.scaleAlpha(hov ? 0x38FFFFFF : 0x22FFFFFF, f));
                        Theme.text(g, ls.name, r.x + 15, by + (bh - 8) / 2, tc);
                        String v = ls.ids.size() + " >";
                        Theme.text(g, v, r.x + r.w - 15 - Theme.width(v), by + (bh - 8) / 2, Theme.scaleAlpha(Theme.withAlpha(acc, 0xFF), f));
                    } else if (s instanceof EnumSetting e) {
                        Draw.roundRect(g, r.x + 8, by, r.w - 16, bh, bh / 2, Theme.scaleAlpha(0x22FFFFFF, f));
                        Theme.text(g, e.name, r.x + 15, by + (bh - 8) / 2, tc);
                        String v = e.get();
                        Theme.text(g, v, r.x + r.w - 15 - Theme.width(v), by + (bh - 8) / 2, Theme.scaleAlpha(Theme.withAlpha(acc, 0xFF), f));
                    }
                }
                default -> { }
            }
        }
        for (Module m : new ArrayList<>(FADE.keySet())) if (!OPEN.contains(m)) FADE.remove(m);

        // Fussleiste
        String foot = hoverDesc != null ? hoverDesc
                : (listening != null ? "Taste druecken zum Binden  |  Backspace: Bind loeschen  |  Esc: abbrechen" : "Links: an/aus  |  Rechts: Settings  |  Mitte: Keybind  |  Right Shift: zu");
        int fw = Theme.width(foot) + 24;
        Draw.glass(g, (width - fw) / 2, height - 24, fw, 16, 8);
        Theme.text(g, foot, (width - fw) / 2 + 12, height - 20, Theme.TEXT);
    }

    private void slide(double mx) {
        double t = Math.max(0, Math.min(1, (mx - sliderX) / sliderW));
        dragSlider.set(dragSlider.min + t * (dragSlider.max - dragSlider.min));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int b = event.button();
        for (Row r : layout()) {
            if (r.kind == 3 || !r.hit(mx, my)) continue;
            switch (r.kind) {
                case 0 -> {
                    if (b == 0) { dragCat = r.cat; dragDx = (int) mx - r.x; dragDy = (int) my - r.y; }
                }
                case 1 -> {
                    if (b == 2) listening = (listening == r.mod) ? null : r.mod;
                    else if (b == 0) r.mod.toggle();
                    else if (b == 1 && !r.mod.settings.isEmpty()) {
                        if (!OPEN.remove(r.mod)) { OPEN.add(r.mod); FADE.put(r.mod, 0f); }
                    }
                }
                case 2 -> {
                    Setting s = r.set;
                    if (s instanceof BoolSetting bs && b == 0) bs.value = !bs.value;
                    else if (s instanceof ListSetting ls && b == 0) {
                        Config.save();
                        Minecraft.getInstance().setScreenAndShow(new PickerScreen(this, ls));
                    }
                    else if (s instanceof EnumSetting es) { if (b == 0) es.next(); else if (b == 1) es.prev(); }
                    else if (s instanceof NumberSetting ns && b == 0) {
                        dragSlider = ns; sliderX = r.x + 8; sliderW = r.w - 16; slide(mx);
                    }
                }
                default -> { }
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragCat != null) {
            int[] p = POS.get(dragCat);
            p[0] = (int) event.x() - dragDx;
            p[1] = (int) event.y() - dragDy - scroll;
            return true;
        }
        if (dragSlider != null) { slide(event.x()); return true; }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragCat = null;
        dragSlider = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(-600, Math.min(0, scroll + (int) (scrollY * 14)));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (listening != null) {
            int k = event.key();
            if (k != 256 && listening.key != null) {
                listening.key.setKey(k == 259 || k == 261 ? InputConstants.UNKNOWN : InputConstants.Type.KEYSYM.getOrCreate(k));
                KeyMapping.resetMapping();
                Minecraft.getInstance().options.save();
            }
            listening = null;
            return true;
        }
        if (event.key() == 344) { onClose(); return true; } // Right Shift
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }
}
