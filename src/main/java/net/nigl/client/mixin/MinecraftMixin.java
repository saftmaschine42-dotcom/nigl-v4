package net.nigl.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.nigl.client.gui.NiglTitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    /** Ersetzt das Vanilla-Hauptmenue durch das Nigl-Client-Hauptmenue. */
    @ModifyVariable(method = "setScreenAndShow", at = @At("HEAD"), argsOnly = true)
    private Screen nigl$replaceTitle(Screen screen) {
        if (screen != null && screen.getClass() == TitleScreen.class) return new NiglTitleScreen();
        return screen;
    }
}
