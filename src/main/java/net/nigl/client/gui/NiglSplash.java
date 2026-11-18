package net.nigl.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.nigl.client.Draw;
import net.nigl.client.NiglClient;
import net.nigl.client.Theme;

/** Ladebildschirm beim Start. Nutzt keine Schriftart, damit er schon waehrend des Ressourcen-Ladens funktioniert. */
public final class NiglSplash {
    public static void draw(GuiGraphicsExtractor g) {
        var win = Minecraft.getInstance().getWindow();
        int w = win.getGuiScaledWidth(), h = win.getGuiScaledHeight();

        Draw.backdrop(g, w, h);

        int c = Math.max(3, Math.min(10, h / 36));
        int logoY = h / 2 - 7 * c - 14;
        Draw.logo(g, (w - Draw.logoWidth(c)) / 2, logoY, c);

        int sc = Math.max(2, c / 2);
        String sub = "CLIENT 26.2";
        Draw.blockText(g, sub, (w - Draw.blockTextWidth(sub, sc)) / 2, logoY + 7 * c + 12, sc, 0xFFE9EDFF);

        // Ladebalken (laeuft hin und her)
        int bw = Math.min(180, w - 60), bh = 6;
        int bx = (w - bw) / 2, by = logoY + 7 * c + 12 + 5 * sc + 18;
        Draw.roundRect(g, bx - 1, by - 1, bw + 2, bh + 2, (bh + 2) / 2, 0x40FFFFFF);
        Draw.roundRect(g, bx, by, bw, bh, bh / 2, 0x30101420);
        double t = (System.currentTimeMillis() % 1600) / 1600.0;
        double pos = t < 0.5 ? t * 2 : (1 - t) * 2;
        int seg = bw / 3;
        int sx = bx + (int) ((bw - seg) * pos);
        Draw.roundRect(g, sx, by, seg, bh, bh / 2, Theme.withAlpha(Theme.accent((float) pos), 0xE6));
    }

    private NiglSplash() { }
}
