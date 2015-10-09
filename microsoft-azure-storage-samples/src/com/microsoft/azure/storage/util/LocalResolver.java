/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.util;

import java.util.HashMap;
import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;

/**
 * A sample key resolver class
 * 
 */
public class LocalResolver implements IKeyResolver {

    private HashMap<String, IKey> keys = new HashMap<String, IKey>();

    /**
     * Map from a keyID to a key. This will be called when decrypting. The data
     * to decrypt will include the keyID used to encrypt it.
     * 
     * @param keyId
     *            The KeyID to map to a key
     */
    @Override
    public Future<IKey> resolveKeyAsync(String keyId) {
        return ConcurrentUtils.constantFuture(this.keys.get(keyId));
    }

    /**
     * Add a key to the local resolver.
     * 
     * @param key
     *            The key to add to the local resolver.
     */
    public void add(IKey key) {
        keys.put(key.getKid(), key);
    }
}
