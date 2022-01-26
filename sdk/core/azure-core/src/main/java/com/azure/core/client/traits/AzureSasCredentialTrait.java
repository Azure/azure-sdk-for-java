// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.credential.AzureSasCredential;

/**
 * The interface for client builders that support a {@link AzureSasCredential}.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface AzureSasCredentialTrait<TBuilder extends AzureSasCredentialTrait<TBuilder>> {
    /**
     * Sets the {@link AzureSasCredential} used to authorize requests sent to the service.
     *
     * @param credential {@link AzureSasCredential} used to authorize requests sent to the service.
     * @return the updated {@code TBuilder}.
     */
    TBuilder credential(AzureSasCredential credential);
}
