package net.nigl.client.module;

public enum Category {
    COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), PLAYER("Player"), CLIENT("Client");
    public final String label;
    Category(String label) { this.label = label; }
}
