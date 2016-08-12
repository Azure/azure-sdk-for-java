/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
