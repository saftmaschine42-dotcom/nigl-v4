package net.nigl.client.module.modules;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.nigl.client.Refl;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class FastBreak extends Module {
    public FastBreak() { super("FastBreak", "Kein Delay zwischen Block-Abbau", Category.PLAYER); }

    @Override public void onTick() { if (mc.gameMode != null) Refl.setInt(mc.gameMode, MultiPlayerGameMode.class, "destroyDelay", 0); }
}
