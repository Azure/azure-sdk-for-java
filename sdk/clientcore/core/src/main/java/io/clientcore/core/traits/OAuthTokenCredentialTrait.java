// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.traits;

import io.clientcore.core.credentials.oauth.OAuthTokenCredential;

/**
 * A {@link io.clientcore.core.traits trait}  providing a consistent interface for setting
 * {@link OAuthTokenCredential}.

 * @param <T> The concrete type that implements the trait. This is required so that fluent operations can continue
 * to return the concrete type, rather than the trait type.
 *
 * @see io.clientcore.core.traits
 * @see OAuthTokenCredential
 */
public interface OAuthTokenCredentialTrait<T extends OAuthTokenCredentialTrait<T>> {
    /**
     * Sets the {@link OAuthTokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link OAuthTokenCredential} used to authorize requests sent to the service.
     * @return Returns the same concrete type with the appropriate properties updated, to allow for fluent chaining of
     *      operations.
     */
    T credential(OAuthTokenCredential credential);
}
