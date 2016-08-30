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
