// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.client.traits;

import com.client.core.credential.ClientNamedKeyCredential;

/**
 * An {@link com.client.core.client.traits Client SDK for Java trait} providing a consistent interface for setting
 * {@link ClientNamedKeyCredential}. Refer to the Client SDK for Java
 * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
 * documentation for more details on proper usage of the {@link ClientNamedKeyCredential} type.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 *           to return the concrete type, rather than the trait type.
 * @see com.client.core.client.traits
 * @see ClientNamedKeyCredential
 */
public interface ClientNamedKeyCredentialTrait<T extends ClientNamedKeyCredentialTrait<T>> {
    /**
     * Sets the {@link ClientNamedKeyCredential} used for authentication. Refer to the Client SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link ClientNamedKeyCredential} type.
     *
     * @param credential the {@link ClientNamedKeyCredential} to be used for authentication.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T credential(ClientNamedKeyCredential credential);
}
