package net.nigl.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.nigl.client.module.Module;
import net.nigl.client.module.Modules;
import net.nigl.client.module.modules.GuiSettings;
import net.nigl.client.module.modules.Hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {
    private static int frames, fps;
    private static long last = System.currentTimeMillis();

    private static void pill(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        Draw.glass(g, x, y, w, h, h / 2);
    }

    public static void render(GuiGraphicsExtractor g) {
        Minecraft mc = Minecraft.getInstance();
        frames++;
        long now = System.currentTimeMillis();
        if (now - last >= 1000) { fps = frames; frames = 0; last = now; }

        Hud hud = Modules.get(Hud.class);
        if (hud == null || !hud.enabled || mc.player == null) return;
        Font f = mc.font;

        if (hud.watermark.value) {
            String a = "Nigl Client", b = " " + NiglClient.MC_VERSION;
            int w = f.width(a + b) + 20;
            pill(g, 4, 4, w, 16);
            Draw.roundRect(g, 10, 9, 5, 5, 2, Theme.accent());
            Theme.bold(g, a, 19, 8, Theme.TEXT);
            Theme.text(g, b, 20 + f.width(a), 8, Theme.GREY);
        }

        if (hud.moduleList.value) {
            int sw = mc.getWindow().getGuiScaledWidth();
            List<Module> active = new ArrayList<>();
            for (Module m : Modules.ALL) if (m.enabled && !(m instanceof Hud) && !(m instanceof GuiSettings)) active.add(m);
            active.sort((x, y) -> Integer.compare(f.width(y.name), f.width(x.name)));
            int y = 4, i = 0;
            for (Module m : active) {
                int w = f.width(m.name) + 18;
                pill(g, sw - w - 4, y, w, 14);
                Draw.roundRect(g, sw - w + 2, y + 5, 4, 4, 2, Theme.accent(i / 8f));
                Theme.text(g, m.name, sw - w + 10, y + 3, Theme.TEXT);
                y += 17;
                i++;
            }
        }

        int sh = mc.getWindow().getGuiScaledHeight();
        int by = sh - 20;
        if (hud.coords.value) {
            String s = String.format(Locale.ROOT, "XYZ  %.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            pill(g, 4, by, f.width(s) + 16, 15);
            Theme.text(g, s, 12, by + 4, Theme.TEXT);
            by -= 18;
        }
        if (hud.fps.value) {
            String s = "FPS  " + fps;
            pill(g, 4, by, f.width(s) + 16, 15);
            Theme.text(g, s, 12, by + 4, Theme.TEXT);
        }
    }

    private HudRenderer() { }
}
