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
package com.microsoft.azure.storage;

import java.util.HashMap;
import java.util.Map;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;

public class DictionaryKeyResolver implements IKeyResolver {
    private Map<String, IKey> keys = new HashMap<String, IKey>();

    public void add(IKey key)
    {
        this.keys.put(key.getKid(), key);
    }

    @Override
    public ListenableFuture<IKey> resolveKeyAsync(String keyId)
    {
        SettableFuture<IKey> future = SettableFuture.create();
        future.set(this.keys.get(keyId));
        return future;
    }
}
