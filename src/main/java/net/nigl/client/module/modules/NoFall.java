package net.nigl.client.module.modules;

import net.minecraft.client.Minecraft;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;
import net.nigl.client.module.Modules;

/** Die eigentliche Arbeit macht MovePacketMixin: onGround wird in jedes Bewegungspaket auf true gesetzt. */
public class NoFall extends Module {
    public NoFall() { super("NoFall", "Kein Fallschaden (auch im Flight-Modus)", Category.PLAYER); }

    public static boolean shouldSpoof() {
        var p = Minecraft.getInstance().player;
        if (p == null || p.isFallFlying()) return false;
        NoFall nf = Modules.get(NoFall.class);
        Flight fl = Modules.get(Flight.class);
        boolean flight = fl != null && fl.enabled && fl.antiFall.value;
        boolean nofall = nf != null && nf.enabled && p.fallDistance > 2;
        return flight || nofall;
    }
}
