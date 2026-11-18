package net.nigl.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Map;

/** Zeichenhelfer: runde Formen, iPhone-artiges "Liquid Glass", Block-Logo. */
public final class Draw {
    private static final String[][] LOGO = {
            {"10001", "11001", "10101", "10101", "10011", "10001", "10001"}, // N
            {"11111", "00100", "00100", "00100", "00100", "00100", "11111"}, // I
            {"01110", "10001", "10000", "10111", "10001", "10001", "01110"}, // G
            {"10000", "10000", "10000", "10000", "10000", "10000", "11111"}  // L
    };

    private static final Map<Character, String[]> FONT = Map.of(
            'C', new String[]{"111", "100", "100", "100", "111"},
            'L', new String[]{"100", "100", "100", "100", "111"},
            'I', new String[]{"111", "010", "010", "010", "111"},
            'E', new String[]{"111", "100", "111", "100", "111"},
            'N', new String[]{"101", "111", "111", "101", "101"},
            'T', new String[]{"111", "010", "010", "010", "010"},
            '2', new String[]{"111", "001", "111", "100", "111"},
            '6', new String[]{"111", "100", "111", "101", "111"},
            '.', new String[]{"0", "0", "0", "0", "1"});

    public static int blockTextWidth(String s, int cell) {
        int w = 0;
        for (char ch : s.toCharArray()) {
            if (ch == ' ') { w += 3 * cell; continue; }
            String[] gl = FONT.get(ch);
            if (gl != null) w += (gl[0].length() + 1) * cell;
        }
        return Math.max(0, w - cell);
    }

    /** Text aus Bloecken (braucht keine Schriftart, funktioniert auch im Ladebildschirm). */
    public static void blockText(GuiGraphicsExtractor g, String s, int x, int y, int cell, int color) {
        int cx = x;
        for (char ch : s.toCharArray()) {
            if (ch == ' ') { cx += 3 * cell; continue; }
            String[] gl = FONT.get(ch);
            if (gl == null) continue;
            for (int row = 0; row < 5; row++)
                for (int col = 0; col < gl[0].length(); col++)
                    if (gl[row].charAt(col) == '1') g.fill(cx + col * cell, y + row * cell, cx + (col + 1) * cell, y + (row + 1) * cell, color);
            cx += (gl[0].length() + 1) * cell;
        }
    }

    /** Animierter Hintergrund: dunkler Verlauf + weich schwebende Lichtkugeln. */
    public static void backdrop(GuiGraphicsExtractor g, int w, int h) {
        for (int i = 0; i < h; i += 4) {
            float t = i / (float) h;
            g.fill(0, i, w, Math.min(h, i + 4), Theme.lerp(0xFF080C18, 0xFF18284A, t));
        }
        double t = System.currentTimeMillis() / 1000.0;
        orb(g, (int) (w * 0.22 + Math.sin(t * 0.35) * 40), (int) (h * 0.30 + Math.cos(t * 0.27) * 30), Math.max(50, h / 4), 0x7FA8FF);
        orb(g, (int) (w * 0.78 + Math.cos(t * 0.31) * 40), (int) (h * 0.35 + Math.sin(t * 0.22) * 35), Math.max(55, h / 4), 0xC7B8FF);
        orb(g, (int) (w * 0.55 + Math.sin(t * 0.25) * 60), (int) (h * 0.82 + Math.cos(t * 0.33) * 20), Math.max(45, h / 5), 0x66F5D8);
    }

    private static void orb(GuiGraphicsExtractor g, int cx, int cy, int radius, int rgb) {
        for (int k = 0; k < 6; k++) {
            int rr = radius - k * radius / 7;
            roundRect(g, cx - rr, cy - rr, rr * 2, rr * 2, rr, Theme.withAlpha(rgb, 0x12));
        }
    }

    public static void roundRect(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * (double) r - dy * dy));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, color);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
        if (h - 2 * r > 0) g.fill(x, y + r, x + w, y + h - r, color);
    }

    /** Durchsichtiges Milchglas: heller Rand, Frost-Schicht, Glanz oben, Specular-Linie. */
    public static void glass(GuiGraphicsExtractor g, int x, int y, int w, int h, int r) {
        float o = Theme.glass();
        roundRect(g, x - 1, y - 1, w + 2, h + 2, r + 1, Theme.scaleAlpha(0x78FFFFFF, o));   // Rand
        roundRect(g, x, y, w, h, r, 0x38101420);                                              // dunkle Basis (Lesbarkeit)
        roundRect(g, x, y, w, h, r, Theme.scaleAlpha(0x2EFFFFFF, o));                         // Frost
        roundRect(g, x + 1, y + 1, w - 2, Math.max(3, (int) (h * 0.42f)), Math.max(2, r - 1), Theme.scaleAlpha(0x1EFFFFFF, o)); // Glanz
        g.fill(x + r, y + h - 2, x + w - r, y + h - 1, 0x1CFFFFFF);                           // unterer Innenschein
        g.fill(x + r, y, x + w - r, y + 1, 0xC0FFFFFF);                                       // Specular oben
    }

    /** Wandernder Lichtreflex auf der Oberkante. */
    public static void shimmer(GuiGraphicsExtractor g, int x, int y, int w, int r) {
        int span = Math.max(1, w * 2);
        int t = (int) ((System.currentTimeMillis() / 14) % span);
        int a = Math.max(x + r, x + t - 26), b = Math.min(x + w - r, x + t);
        if (b > a) g.fill(a, y, b, y + 1, 0xFFFFFFFF);
    }

    public static int logoWidth(int cell) { return 23 * cell; }

    public static void logo(GuiGraphicsExtractor g, int x, int y, int c) {
        for (int pass = 0; pass < 3; pass++) {
            int cx = 0;
            for (String[] letter : LOGO) {
                for (int row = 0; row < 7; row++) {
                    for (int col = 0; col < 5; col++) {
                        if (letter[row].charAt(col) != '1') continue;
                        int px = x + (cx + col) * c, py = y + row * c;
                        int color = Theme.accent((cx + col) / 22f);
                        switch (pass) {
                            case 0 -> g.fill(px - 2, py - 2, px + c + 2, py + c + 2, Theme.withAlpha(color, 0x20));
                            case 1 -> g.fill(px + 2, py + 3, px + c + 2, py + c + 3, 0x55000000);
                            default -> {
                                g.fill(px, py, px + c, py + c, Theme.withAlpha(color, 0xE8));
                                g.fill(px, py, px + c, py + Math.max(1, c / 4), 0x80FFFFFF);
                            }
                        }
                    }
                }
                cx += 6;
            }
        }
    }

    private Draw() { }
}
