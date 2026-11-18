package net.nigl.client.module;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public final class Targets {
    public static boolean match(Entity e, boolean players, boolean hostile, boolean passive) {
        if (e instanceof Player) return players;
        if (e instanceof Enemy) return hostile;
        if (e instanceof LivingEntity) return passive;
        return false;
    }
    private Targets() { }
}
