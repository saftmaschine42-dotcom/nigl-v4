package net.nigl.client.module.modules;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.nigl.client.module.Category;
import net.nigl.client.module.Module;

public class Fullbright extends Module {
    public Fullbright() { super("Fullbright", "Alles ist hell (Nachtsicht, nur clientseitig)", Category.RENDER); }

    @Override
    public void onTick() {
        mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}
