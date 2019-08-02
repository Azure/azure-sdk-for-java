// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

/**
 * Fluent credential builder for instantiating a {@link DefaultAzureCredential}.
 *
 * @see DefaultAzureCredential
 */
public class DefaultAzureCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {
    /**
     * @return a {@link DefaultAzureCredential} with the current configurations.
     */
    public DefaultAzureCredential build() {
        return new DefaultAzureCredential(identityClientOptions);
    }
}
