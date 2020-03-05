// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import java.util.concurrent.ExecutorService;

/**
 * Fluent credential builder for instantiating a {@link EnvironmentCredential}.
 *
 * @see EnvironmentCredential
 */
public class EnvironmentCredentialBuilder extends CredentialBuilderBase<EnvironmentCredentialBuilder> {
    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    public EnvironmentCredentialBuilder authorityHost(String authorityHost) {
        this.identityClientOptions.setAuthorityHost(authorityHost);
        return this;
    }

    /**
     * Specifies the  ExecutorService to be used to execute the requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    public EnvironmentCredentialBuilder executorService(ExecutorService executorService) {
        this.identityClientOptions.setExecutorService(executorService);
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
