// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link AzureDeveloperCliCredential}.
 *
 * @see AzureDeveloperCliCredential
 */
public class AzureDeveloperCliCredentialBuilder extends CredentialBuilderBase<AzureDeveloperCliCredentialBuilder> {
     /**
     * Creates a new {@link AzureDeveloperCliCredential} with the current configurations.
     *
     * @return a {@link AzureDeveloperCliCredential} with the current configurations.
     */
    public AzureDeveloperCliCredential build() {
        return new AzureDeveloperCliCredential(identityClientOptions);
    }
}
