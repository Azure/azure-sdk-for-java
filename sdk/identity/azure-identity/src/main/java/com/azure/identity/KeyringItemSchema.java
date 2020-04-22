// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * An expandable enum for types of item schema in a Keyring.
 */
public final class KeyringItemSchema {
    public static final KeyringItemSchema GENERIC_SECRET = new KeyringItemSchema("org.freedesktop.Secret.Generic");
    public static final KeyringItemSchema NETWORK_PASSWORD = new KeyringItemSchema(
            "org.gnome.keyring.NetworkPassword");
    public static final KeyringItemSchema NOTE = new KeyringItemSchema("org.gnome.keyring.Note");
    public static final KeyringItemSchema MSAL_CACHE = new KeyringItemSchema("msal.cache");

    private final String value;

    private KeyringItemSchema(String value) {
        this.value = value;
    }

    /**
     * Parses a String into a new Keyring schema.
     * @param schema the full name of the schema
     * @return the KeyringItemSchema enum representing this schema
     */
    public static KeyringItemSchema fromString(String schema) {
        return new KeyringItemSchema(schema);
    }

    @Override
    public String toString() {
        return value;
    }
}
