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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;

public class CachingKeyResolver implements IKeyResolver {

    private final LoadingCache<String, ListenableFuture<IKey>> _cache;
    private final IKeyResolver                       _inner;

    public CachingKeyResolver(int capacity, IKeyResolver inner) {
        _cache = CacheBuilder.newBuilder().maximumSize(capacity)
        		.build( new CacheLoader<String, ListenableFuture<IKey>>(){

					@Override
					public ListenableFuture<IKey> load(String kid) {
						return _inner.resolveKeyAsync(kid);
					}});
        
        _inner = inner;
    }

    @Override
    public ListenableFuture<IKey> resolveKeyAsync(String kid) {
    	return _cache.getUnchecked(kid);
    }
}
