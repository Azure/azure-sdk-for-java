/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault;

/**
 * The Key Vault key identifier.
 */
public final class KeyIdentifier extends ObjectIdentifier {
    
    /**
     * Verifies whether the identifier belongs to a key vault key. 
     * @param identifier the key vault key identifier.
     * @return true if the identifier belongs to a key vault key. False otherwise.
     */
    public static boolean isKeyIdentifier(String identifier) {
        return ObjectIdentifier.isObjectIdentifier("keys", identifier);
    }

    /**
     * Constructor.
     * @param vault the vault url.
     * @param name the name of key.
     */
    public KeyIdentifier(String vault, String name) {
        this(vault, name, "");
    }

    /**
     * Constructor.
     * @param vault the vault url.
     * @param name the name of key.
     * @param version the key version.
     */
    public KeyIdentifier(String vault, String name, String version) {
        super(vault, "keys", name, version);
    }

    /**
     * Constructor.
     * @param identifier the key vault key identifier.
     */
    public KeyIdentifier(String identifier) {
        super("keys", identifier);
    }
}
