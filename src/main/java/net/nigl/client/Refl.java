package net.nigl.client;

import java.lang.reflect.Field;

public final class Refl {
    public static void setInt(Object target, Class<?> owner, String name, int value) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            f.setInt(target, value);
        } catch (Throwable ignored) { }
    }
    private Refl() { }
}
