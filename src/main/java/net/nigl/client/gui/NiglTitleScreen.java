package net.nigl.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.nigl.client.Draw;
import net.nigl.client.NiglClient;
import net.nigl.client.Theme;

/** Eigenes Hauptmenue: Singleplayer / Multiplayer / Options / Quit im Nigl-Client-Glas-Design. */
public class NiglTitleScreen extends Screen {
    private static final String[] LABELS = {"Singleplayer", "Multiplayer", "Options", "Quit Game"};
    private final float[] hover = new float[LABELS.length];
    private long lastNs = System.nanoTime();

    public NiglTitleScreen() { super(Component.literal(NiglClient.NAME)); }

    private int cell() { return Math.max(3, Math.min(9, height / 42)); }
    private int bw() { return Math.min(190, width - 40); }

    private int topY() {
        int c = cell();
        int total = 7 * c + 12 + 17 + 22 + LABELS.length * 26 + 20;
        return Math.max(8, (height - total) / 2);
    }

    private int[] button(int i) {
        int c = cell();
        int y0 = topY() + 7 * c + 12 + 17 + 22;
        return new int[]{(width - bw()) / 2, y0 + i * 26, bw(), 21};
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float delta) {
        long now = System.nanoTime();
        float k = Math.min(1f, Math.min(0.1f, (now - lastNs) / 1e9f) * 14f);
        lastNs = now;

        Draw.backdrop(g, width, height);

        int c = cell(), ty = topY();
        Draw.logo(g, (width - Draw.logoWidth(c)) / 2, ty, c);

        String s1 = "C L I E N T", v = NiglClient.MC_VERSION;
        int tw = Theme.width(s1) + Theme.width(v) + 34;
        int px = (width - tw) / 2, sy = ty + 7 * c + 10;
        Draw.glass(g, px, sy, tw, 17, 8);
        Theme.text(g, s1, px + 11, sy + 5, Theme.TEXT);
        int vx = px + 11 + Theme.width(s1) + 7;
        Draw.roundRect(g, vx - 4, sy + 3, Theme.width(v) + 8, 11, 5, 0x55FFFFFF);
        Theme.text(g, v, vx, sy + 5, 0xFFFFFFFF);

        // Panel hinter den Buttons
        int[] first = button(0);
        Draw.glass(g, first[0] - 12, first[1] - 10, first[2] + 24, LABELS.length * 26 + 14, 14);
        Draw.shimmer(g, first[0] - 12, first[1] - 10, first[2] + 24, 14);

        int acc = Theme.accent(0.2f);
        for (int i = 0; i < LABELS.length; i++) {
            int[] b = button(i);
            boolean hov = mx >= b[0] && mx < b[0] + b[2] && my >= b[1] && my < b[1] + b[3];
            hover[i] += ((hov ? 1f : 0f) - hover[i]) * k;
            if (hover[i] > 0.03f) Draw.roundRect(g, b[0] - 2, b[1] - 2, b[2] + 4, b[3] + 4, 12, Theme.withAlpha(acc, (int) (0x30 * hover[i])));
            Draw.roundRect(g, b[0], b[1], b[2], b[3], 10, Theme.lerp(0x22FFFFFF, Theme.withAlpha(acc, 0xE0), hover[i]));
            g.fill(b[0] + 10, b[1], b[0] + b[2] - 10, b[1] + 1, Theme.withAlpha(0xFFFFFF, (int) (0x50 + 0x80 * hover[i])));
            int col = Theme.lerp(0xFFF4F6FF, Theme.onAccent(acc), hover[i]);
            Theme.text(g, LABELS[i], b[0] + (b[2] - Theme.width(LABELS[i])) / 2, b[1] + 7, col, hover[i] < 0.5f);
        }

        String foot = "Nigl Client " + NiglClient.MC_VERSION + "  |  Fabric";
        Theme.text(g, foot, (width - Theme.width(foot)) / 2, height - 14, Theme.GREY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (int i = 0; i < LABELS.length; i++) {
                int[] b = button(i);
                if (event.x() >= b[0] && event.x() < b[0] + b[2] && event.y() >= b[1] && event.y() < b[1] + b[3]) {
                    Minecraft mc = Minecraft.getInstance();
                    switch (i) {
                        case 0 -> mc.setScreenAndShow(new SelectWorldScreen(this));
                        case 1 -> mc.setScreenAndShow(new JoinMultiplayerScreen(this));
                        case 2 -> mc.setScreenAndShow(new OptionsScreen(this, mc.options));
                        default -> mc.stop();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) return true; // Esc soll das Hauptmenue nicht schliessen
        return super.keyPressed(event);
    }
}
