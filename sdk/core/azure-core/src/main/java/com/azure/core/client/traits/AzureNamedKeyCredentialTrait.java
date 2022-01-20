// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.credential.AzureNamedKeyCredential;

/**
 * The interface for client builders that support a {@link AzureNamedKeyCredential}.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface AzureNamedKeyCredentialTrait<TBuilder extends AzureNamedKeyCredentialTrait<TBuilder>> {
    /**
     * Sets The Azure Named Key Credential used for authentication.
     *
     * @param azureNamedKeyCredential the azureNamedKeyCredential value.
     * @return the updated {@code TBuilder}.
     */
    TBuilder credential(AzureNamedKeyCredential azureNamedKeyCredential);
}
