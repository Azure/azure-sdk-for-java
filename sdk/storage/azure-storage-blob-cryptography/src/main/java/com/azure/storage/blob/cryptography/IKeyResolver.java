/*
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

package com.azure.storage.blob.cryptography;

import reactor.core.publisher.Mono;

public interface IKeyResolver {
    /**
     * Retrieves an IKey implementation for the specified key identifier.
     * Implementations should check the format of the kid to ensure that it is
     * recognized. Null, rather than an exception, should be returned for
     * unrecognized key identifiers to enable chaining of key resolvers.
     *
     * @param kid
     *            The key identifier to resolve.
     * @return A ListenableFuture containing the resolved IKey
     */

    Mono<IKey> resolveKeyAsync(String kid);
}
