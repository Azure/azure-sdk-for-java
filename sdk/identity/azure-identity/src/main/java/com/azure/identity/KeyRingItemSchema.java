package com.azure.identity;

public final class KeyRingItemSchema {
    public static final KeyRingItemSchema GENERIC_SECRET = new KeyRingItemSchema("org.freedesktop.Secret.Generic");
    public static final KeyRingItemSchema NETWORK_PASSWORD = new KeyRingItemSchema("org.gnome.keyring.NetworkPassword");
    public static final KeyRingItemSchema NOTE = new KeyRingItemSchema("org.gnome.keyring.Note");

    private final String value;

    private KeyRingItemSchema(String value) {
        this.value = value;
    }

    public static KeyRingItemSchema fromString(String schema) {
        return new KeyRingItemSchema(schema);
    }

    @Override
    public String toString() {
        return value;
    }
}
