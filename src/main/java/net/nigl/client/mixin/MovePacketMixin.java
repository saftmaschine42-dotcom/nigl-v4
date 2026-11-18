package net.nigl.client.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.nigl.client.module.modules.NoFall;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundMovePlayerPacket.class)
public abstract class MovePacketMixin {
    @Shadow @Final @Mutable protected boolean onGround;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void nigl$spoofOnGround(CallbackInfo ci) {
        if (NoFall.shouldSpoof()) this.onGround = true;
    }
}
