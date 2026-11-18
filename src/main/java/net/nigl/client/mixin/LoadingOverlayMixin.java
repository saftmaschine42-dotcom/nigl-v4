package net.nigl.client.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.nigl.client.gui.NiglSplash;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class LoadingOverlayMixin {
    /** Zeichnet nach dem Vanilla-Overlay den Nigl-Client-Ladebildschirm darueber. */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void nigl$splash(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        NiglSplash.draw(g);
    }
}
