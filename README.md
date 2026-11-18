# Nigl Client 26.2

Fabric-Utility-Client fuer Minecraft 26.2 (nur fuer eigene/private Server gedacht).

- GUI oeffnen: **Right Shift** (aenderbar unter Optionen > Steuerung > "Nigl Client")
- Linksklick auf Modul = an/aus, Rechtsklick = Einstellungen, Panel-Header ziehen = verschieben, Mausrad = scrollen
- Modul-Keybinds: Optionen > Steuerung > "Nigl Client"
- Design aendern: Modul "GUI" (Accent, Glass, Rainbow)
- Config: `config/nigl-client.properties`
- Fuer Flight: auf deinem Server `allow-flight=true` in server.properties

Build: Push zu GitHub -> Actions -> "Build Nigl Client" -> Artifact `nigl-client` -> .jar in `mods/`.
Benoetigt Fabric Loader + Fabric API fuer 26.2 und Java 25.

## ESP
- **EntityESP**: Boxen (Box/Filled/Both) + Tracer fuer Spieler, Hostile, Passive, Items
- **BlockESP**: Diamant, Emerald, Gold, Eisen, Kohle, Redstone, Lapis, Kupfer, Ancient Debris, Quarz, Kisten, Spawner (einzeln schaltbar), optional Tracer

## Neu in v4
- ESP-Auswahl: BlockESP / EntityESP -> Einstellung "Blocks" / "Entities" anklicken -> Suchmenue (tippen zum Suchen)
- Freecam: W/A/S/D, Space = hoch, Shift = runter, Strg = schneller
- Keybinds direkt in der GUI: **Mittelklick** auf ein Modul, dann Taste druecken (Backspace = loeschen, Esc = abbrechen)
- Eigener Ladebildschirm + eigenes Hauptmenue (Singleplayer / Multiplayer / Options / Quit)
