// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.client.traits;

import com.azure.core.credential.AzureKeyCredential;

/**
 * The interface for client builders that support a {@link AzureKeyCredential}.
 *
 * @param <TBuilder> the type of client builder.
 */
public interface AzureKeyCredentialTrait<TBuilder extends AzureKeyCredentialTrait<TBuilder>> {
    /**
     * Sets The Azure Key Credential used for authentication.
     *
     * @param azureKeyCredential the azureKeyCredential value.
     * @return the updated {@code TBuilder}.
     */
    TBuilder credential(AzureKeyCredential azureKeyCredential);
}
