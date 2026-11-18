package net.nigl.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.nigl.client.gui.ClickGui;
import net.nigl.client.module.Modules;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NiglClient implements ClientModInitializer {
    public static final String NAME = "Nigl Client";
    public static final String MC_VERSION = "26.2";
    public static final String ID = "nigl";
    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    public static KeyMapping.Category CATEGORY;
    public static KeyMapping guiKey;

    @Override
    public void onInitializeClient() {
        CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "client"));
        guiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.nigl.clickgui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));

        Modules.init();
        Config.load();
        net.nigl.client.render.EspRenderer.init();

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player != null && client.level != null) Modules.preTick();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) {
                while (guiKey.consumeClick()) { }
                Modules.worldLeft();
                return;
            }
            while (guiKey.consumeClick()) client.setScreenAndShow(new ClickGui());
            Modules.tick();
        });

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(ID, "hud"),
                (graphics, tickCounter) -> HudRenderer.render(graphics));

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Config.save());
        LOG.info("{} {} geladen", NAME, MC_VERSION);
    }
}
