// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.credential.TokenCredential;

/**
 * The interface for client builders that support a {@link TokenCredential}.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface TokenCredentialTrait<TBuilder extends TokenCredentialTrait<TBuilder>> {

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link TokenCredential} used to authorize requests sent to the service.
     * @return The updated {@code TBuilder} object.
     */
    TBuilder credential(TokenCredential credential);
}
