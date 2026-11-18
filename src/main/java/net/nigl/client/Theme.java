package net.nigl.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.nigl.client.module.Modules;
import net.nigl.client.module.modules.GuiSettings;

public final class Theme {
    public static final int TEXT = 0xFFF4F6FF;
    public static final int GREY = 0xFFC2C8DC;
    public static final int DARK = 0xFF14161F;

    // Reihenfolge = EnumSetting in GuiSettings: White, Blue, Mint, Pink, Orange, Purple
    private static final int[][] PAL = {
            {0xFFFFFFFF, 0xFFCFE0FF},
            {0xFF0A84FF, 0xFF64D2FF},
            {0xFF66F5C8, 0xFF8AD8FF},
            {0xFFFF7AC8, 0xFFFFB38A},
            {0xFFFFA23D, 0xFFFF6B8A},
            {0xFF9D6BFF, 0xFF5CC8FF}
    };

    public static int accent() { return accent(0f); }

    public static int accent(float t) {
        GuiSettings gs = Modules.get(GuiSettings.class);
        if (gs == null) return PAL[0][0];
        if (gs.rainbow.value) return hsv(((System.currentTimeMillis() % 6000) / 6000f + t * 0.2f) % 1f, 0.45f, 1f);
        int i = Math.max(0, Math.min(PAL.length - 1, gs.accent.index));
        return lerp(PAL[i][0], PAL[i][1], t);
    }

    /** Textfarbe, die auf der Akzentfarbe lesbar ist. */
    public static int onAccent(int c) {
        double l = (0.299 * ((c >> 16) & 255) + 0.587 * ((c >> 8) & 255) + 0.114 * (c & 255)) / 255.0;
        return l > 0.6 ? DARK : 0xFFFFFFFF;
    }

    public static float glass() {
        GuiSettings gs = Modules.get(GuiSettings.class);
        return gs == null ? 0.75f : (float) gs.glass.value;
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int out = 0;
        for (int s = 24; s >= 0; s -= 8) {
            int ca = (a >>> s) & 255, cb = (b >>> s) & 255;
            out |= ((int) (ca + (cb - ca) * t) & 255) << s;
        }
        return out;
    }

    public static int withAlpha(int c, int a) { return (Math.max(0, Math.min(255, a)) << 24) | (c & 0xFFFFFF); }
    public static int scaleAlpha(int c, float f) { return withAlpha(c, (int) (((c >>> 24) & 255) * f)); }

    public static int hsv(float h, float s, float v) {
        int i = (int) (h * 6) % 6;
        float f = h * 6 - (int) (h * 6);
        float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return 0xFF000000 | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    public static void text(GuiGraphicsExtractor g, String s, int x, int y, int color) { text(g, s, x, y, color, true); }

    public static void text(GuiGraphicsExtractor g, String s, int x, int y, int color, boolean shadow) {
        g.text(Minecraft.getInstance().font, s, x, y, color, shadow);
    }

    public static void bold(GuiGraphicsExtractor g, String s, int x, int y, int color) {
        text(g, s, x, y, color);
        text(g, s, x + 1, y, color, false);
    }

    public static int width(String s) { return Minecraft.getInstance().font.width(s); }

    private Theme() { }
}
