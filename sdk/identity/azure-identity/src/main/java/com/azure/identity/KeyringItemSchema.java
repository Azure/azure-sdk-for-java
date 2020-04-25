// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.ExpandableStringEnum;

/**
 * An expandable enum for types of item schema in a Keyring.
 */

public final class KeyringItemSchema extends ExpandableStringEnum<KeyringItemSchema> {
    public static final KeyringItemSchema GENERIC_SECRET = fromString("org.freedesktop.Secret.Generic",
            KeyringItemSchema.class);
    public static final KeyringItemSchema NETWORK_PASSWORD = fromString("org.gnome.keyring.NetworkPassword",
            KeyringItemSchema.class);
    public static final KeyringItemSchema NOTE = fromString("org.gnome.keyring.Note",
            KeyringItemSchema.class);
    public static final KeyringItemSchema MSAL_CACHE = fromString("msal.cache", KeyringItemSchema.class);
}
