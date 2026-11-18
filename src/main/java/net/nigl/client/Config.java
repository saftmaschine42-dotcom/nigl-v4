package net.nigl.client;

import net.fabricmc.loader.api.FabricLoader;
import net.nigl.client.module.Module;
import net.nigl.client.module.Modules;
import net.nigl.client.setting.Setting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private static Path file() { return FabricLoader.getInstance().getConfigDir().resolve("nigl-client.properties"); }

    public static void save() {
        Properties p = new Properties();
        for (Module m : Modules.ALL) {
            p.setProperty(m.id() + ".enabled", Boolean.toString(m.enabled));
            for (Setting s : m.settings) p.setProperty(m.id() + "." + s.key(), s.save());
        }
        try (OutputStream o = Files.newOutputStream(file())) {
            p.store(o, "Nigl Client " + NiglClient.MC_VERSION);
        } catch (IOException e) {
            NiglClient.LOG.error("Config speichern fehlgeschlagen", e);
        }
    }

    public static void load() {
        if (!Files.exists(file())) return;
        Properties p = new Properties();
        try (InputStream i = Files.newInputStream(file())) {
            p.load(i);
        } catch (IOException e) {
            NiglClient.LOG.error("Config laden fehlgeschlagen", e);
            return;
        }
        for (Module m : Modules.ALL) {
            String en = p.getProperty(m.id() + ".enabled");
            if (en != null) m.enabled = Boolean.parseBoolean(en);
            for (Setting s : m.settings) {
                String v = p.getProperty(m.id() + "." + s.key());
                if (v != null) s.load(v);
            }
        }
    }

    private Config() { }
}
