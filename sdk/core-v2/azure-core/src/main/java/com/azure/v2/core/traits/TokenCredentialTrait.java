// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.traits;

import com.azure.v2.core.credentials.TokenCredential;

/**
 * A {@link com.azure.v2.core.traits Core V2 Trait} providing a consistent interface for setting
 * {@link TokenCredential}.

 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 * to return the concrete type, rather than the trait type.
 *
 * @see com.azure.v2.core.traits
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
