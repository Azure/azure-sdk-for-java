// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link EnvironmentCredential}.
 *
 * @see EnvironmentCredential
 */
public class EnvironmentCredentialBuilder extends CredentialBuilderBase<EnvironmentCredentialBuilder> {
    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return itself
     */
    public EnvironmentCredentialBuilder authorityHost(String authorityHost) {
        this.identityClientOptions.setAuthorityHost(authorityHost);
        return this;
    }

    /**
     * Creates a new {@link EnvironmentCredential} with the current configurations.
     *
     * @return a {@link EnvironmentCredential} with the current configurations.
     */
    public EnvironmentCredential build() {
        return new EnvironmentCredential(identityClientOptions);
    }
}
