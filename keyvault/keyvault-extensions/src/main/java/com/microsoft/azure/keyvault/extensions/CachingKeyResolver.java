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

package com.microsoft.azure.keyvault.extensions;

import java.util.concurrent.Future;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;

public class CachingKeyResolver implements IKeyResolver {

    private final LRUCache<String, IKey> _cache;
    private final IKeyResolver           _inner;

    public CachingKeyResolver(int capacity, IKeyResolver inner) {
        _cache = new LRUCache<String, IKey>(capacity);
        _inner = inner;
    }

    @Override
    public Future<IKey> resolveKeyAsync(String kid) {

        IKey result = _cache.get(kid);

        if (result == null) {
            return _inner.resolveKeyAsync(kid);
        } else {
            return new FutureImmediate<IKey>(result);
        }
    }
}
