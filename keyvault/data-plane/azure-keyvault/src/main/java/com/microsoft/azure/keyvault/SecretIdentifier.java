/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault;

/**
 * Key Vault secret identifier.
 */
public final class SecretIdentifier extends ObjectIdentifier {
    
    /**
     * Verifies whether the identifier belongs to a key vault secret. 
     * @param identifier the key vault secret identifier.
     * @return true if the identifier belongs to a key vault secret. False otherwise.
     */
    public static boolean isSecretIdentifier(String identifier) {
        return ObjectIdentifier.isObjectIdentifier("secrets", identifier);
    }

    /**
     * Constructor.
     * @param vault the vault url.
     * @param name the name of secret.
     */
    public SecretIdentifier(String vault, String name) {
        this(vault, name, "");
    }

    /**
     * Constructor.
     * @param vault the vault url.
     * @param name the name of secret.
     * @param version the secret version.
     */
    public SecretIdentifier(String vault, String name, String version) {
        super(vault, "secrets", name, version);
    }

    /**
     * Constructor.
     * @param identifier the object identifier.
     */
    public SecretIdentifier(String identifier) {
        super("secrets", identifier);
    }
}
