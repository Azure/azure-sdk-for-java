// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.client.traits;

import com.typespec.core.credential.KeyCredential;

/**
 * An {@link com.typespec.core.client.traits Azure SDK for Java trait} providing a consistent interface for setting
 * {@link KeyCredential}. Refer to the Azure SDK for Java
 * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
 * documentation for more details on proper usage of the {@link KeyCredential} type.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 * to return the concrete type, rather than the trait type.
 * @see com.typespec.core.client.traits
 * @see KeyCredential
 */
public interface KeyCredentialTrait<T> {
    /**
     * Sets the {@link KeyCredential} used for authentication. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link KeyCredential} type.
     *
     * @param credential the {@link KeyCredential} to be used for authentication.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T credential(KeyCredential credential);
}
