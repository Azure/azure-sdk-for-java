// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link AzurePowerShellCredential}.
 *
 * @see AzurePowerShellCredential
 */
public class AzurePowerShellCredentialBuilder extends CredentialBuilderBase<AzurePowerShellCredentialBuilder> {

     /**
     * Creates a new {@link AzurePowerShellCredential} with the current configurations.
     *
     * @return a {@link AzurePowerShellCredential} with the current configurations.
     */
    public AzurePowerShellCredential build() {
        return new AzurePowerShellCredential(identityClientOptions);
    }
}
