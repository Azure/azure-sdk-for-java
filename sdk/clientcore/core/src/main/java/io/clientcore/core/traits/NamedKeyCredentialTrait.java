// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.traits;

import io.clientcore.core.credentials.NamedKeyCredential;

/**
 * An {@link io.clientcore.core.traits SDK for Java trait} providing a consistent interface for setting
 * {@link NamedKeyCredential}.
 *
 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 * to return the concrete type, rather than the trait type.
 * @see io.clientcore.core.traits
 * @see NamedKeyCredential
 */
public interface NamedKeyCredentialTrait<T extends NamedKeyCredentialTrait<T>> {
    /**
     * Sets the {@link NamedKeyCredential} used for authentication.
     *
     * @param credential the {@link NamedKeyCredential} to be used for authentication.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     * operations.
     */
    T credential(NamedKeyCredential credential);
}
