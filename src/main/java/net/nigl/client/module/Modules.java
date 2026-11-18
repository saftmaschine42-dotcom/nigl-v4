package net.nigl.client.module;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.nigl.client.NiglClient;
import net.nigl.client.module.modules.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    public static final List<Module> ALL = new ArrayList<>();

    public static void init() {
        // Combat
        add(new KillAura());
        add(new TriggerBot());
        // Movement
        add(new Sprint());
        add(new Flight());
        add(new Speed());
        add(new Spider());
        add(new Step());
        add(new Glide());
        add(new AutoWalk());
        add(new NoJumpDelay());
        // Render
        add(new Fullbright());
        add(new Freecam());
        add(new EntityEsp());
        add(new BlockEsp());
        add(new Zoom());
        // Player
        add(new NoFall());
        add(new AutoRespawn());
        add(new FastPlace());
        add(new FastBreak());
        // Client
        add(new Hud());
        add(new GuiSettings());
    }

    private static void add(Module m) {
        m.key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.nigl.module." + m.id(), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, NiglClient.CATEGORY));
        ALL.add(m);
    }

    public static void preTick() {
        for (Module m : ALL) if (m.enabled) {
            try { m.onPreTick(); } catch (Throwable t) { NiglClient.LOG.error("Fehler in Modul " + m.name, t); }
        }
    }

    public static void worldLeft() {
        for (Module m : ALL) if (m.enabled) m.onWorldLeave();
    }

    public static void tick() {
        for (Module m : ALL) {
            while (m.key.consumeClick()) m.toggle();
            if (!m.enabled) continue;
            try {
                m.onTick();
            } catch (Throwable t) {
                NiglClient.LOG.error("Fehler in Modul " + m.name, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> c) {
        for (Module m : ALL) if (c.isInstance(m)) return (T) m;
        return null;
    }

    public static List<Module> inCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == c) out.add(m);
        return out;
    }

    private Modules() { }
}
