// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link AzureCliCredential}.
 *
 * @see AzureCliCredential
 */
public class AzureCliCredentialBuilder extends CredentialBuilderBase<AzureCliCredentialBuilder> {
     /**
     * Creates a new {@link AzureCliCredential} with the current configurations.
     *
     * @return a {@link AzureCliCredential} with the current configurations.
     */
    public AzureCliCredential build() {
        return new AzureCliCredential(identityClientOptions);
    }
}
