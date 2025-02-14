// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models.traits;

import io.clientcore.core.credential.TokenCredential;

/**
 * An {@link io.clientcore.core.models.traits Azure SDK for Java trait} providing a consistent interface for setting
 * {@link TokenCredential}.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 * to return the concrete type, rather than the trait type.
 *
 * @see com.azure.core.client.traits
 * @see TokenCredential
 */
public interface TokenCredentialTrait<T extends TokenCredentialTrait<T>> {

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T credential(TokenCredential credential);
}
