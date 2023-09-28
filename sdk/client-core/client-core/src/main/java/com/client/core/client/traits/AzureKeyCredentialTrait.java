// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.client.traits;

import com.client.core.credential.ClientKeyCredential;

/**
 * An {@link com.client.core.client.traits Client SDK for Java trait} providing a consistent interface for setting
 * {@link ClientKeyCredential}. Refer to the Client SDK for Java
 * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
 * documentation for more details on proper usage of the {@link ClientKeyCredential} type.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 *           to return the concrete type, rather than the trait type.
 * @see com.client.core.client.traits
 * @see ClientKeyCredential
 */
public interface ClientKeyCredentialTrait<T extends ClientKeyCredentialTrait<T>> {
    /**
     * Sets the {@link ClientKeyCredential} used for authentication. Refer to the Client SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link ClientKeyCredential} type.
     *
     * @param credential the {@link ClientKeyCredential} to be used for authentication.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T credential(ClientKeyCredential credential);
}
