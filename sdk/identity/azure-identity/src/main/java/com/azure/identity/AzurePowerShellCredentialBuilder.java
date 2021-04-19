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
     * Specifies if legacy Azure Power Shell should be used for Azure Power Shell based authentication.
     *
     * @param useLegacyPowerShell the flag indicating if legacy Azure Power Shell should be used for authentication.
     * @return An updated instance of the AzurePowerShellCredentialBuilder.
     */
    public AzurePowerShellCredentialBuilder useLegacyPowerShell(boolean useLegacyPowerShell) {
        this.identityClientOptions.setUseLegacyPowerShell(useLegacyPowerShell);
        return this;
    }

     /**
     * Creates a new {@link AzurePowerShellCredential} with the current configurations.
     *
     * @return a {@link AzurePowerShellCredential} with the current configurations.
     */
    public AzurePowerShellCredential build() {
        return new AzurePowerShellCredential(identityClientOptions);
    }
}
