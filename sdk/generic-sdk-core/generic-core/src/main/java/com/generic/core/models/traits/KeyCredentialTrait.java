// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models.traits;

import com.generic.core.credential.KeyCredential;

/**
 * A {@link com.generic.core.models.traits trait} providing a consistent interface for setting {@link KeyCredential}.
 * Refer to the SDK for Java
 * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a> documentation for more details on
 * proper usage of the {@link KeyCredential} type.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue to
 * return the concrete type, rather than the trait type.
 *
 * @see com.generic.core.models.traits
 * @see KeyCredential
 */
public interface KeyCredentialTrait<T extends KeyCredentialTrait<T>> {
    /**
     * Sets the {@link KeyCredential} used for authentication. Refer to the SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a> documentation for more details
     * on proper usage of the {@link KeyCredential} type.
     *
     * @param credential The {@link KeyCredential} to be used for authentication.
     *
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T credential(KeyCredential credential);
}
