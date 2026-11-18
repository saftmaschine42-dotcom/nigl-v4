package net.nigl.client.module.modules;

import net.minecraft.client.Minecraft;
import net.nigl.client.Refl;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class FastPlace extends Module {
    public FastPlace() { super("FastPlace", "Bloecke ohne Verzoegerung platzieren", Category.PLAYER); }

    @Override public void onTick() { Refl.setInt(mc, Minecraft.class, "rightClickDelay", 0); }
}
