// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Fluent credential builder for instantiating a {@link DefaultAzureCredential}.
 *
 * @see DefaultAzureCredential
 */
public class DefaultAzureCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {
    private boolean excludeEnvironmentCredential;
    private boolean excludeManagedIdentityCredential;
    private boolean excludeSharedTokenCacheCredential;
    private boolean excludeAzureCliCredential;
    private final ClientLogger logger = new ClientLogger(DefaultAzureCredentialBuilder.class);


    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    public DefaultAzureCredentialBuilder authorityHost(String authorityHost) {
        this.identityClientOptions.setAuthorityHost(authorityHost);
        return this;
    }


    /**
     * Excludes the {@link EnvironmentCredential} from the {@link DefaultAzureCredential}
     * authentication flow and disables reading authentication details from the process' environment
     * variables.
     *
     * @return An updated instance of this builder with the Environment credential exclusion set as specified.
     */
    public DefaultAzureCredentialBuilder excludeEnvironmentCredential() {
        excludeEnvironmentCredential = true;
        return this;
    }

    /**
     * Excludes the {@link ManagedIdentityCredential} from the {@link DefaultAzureCredential}
     * authentication flow and disables authenticating with managed identity endpoints.
     *
     * @return An updated instance of this builder with the Managed Identity credential exclusion set as specified.
     */
    public DefaultAzureCredentialBuilder excludeManagedIdentityCredential() {
        excludeManagedIdentityCredential = true;
        return this;
    }

    /**
     * Excludes the {@link SharedTokenCacheCredential} from the {@link DefaultAzureCredential}
     * authentication flow and disables single sign on authentication with development tools which write
     * to the shared token cache.
     *
     * @return An updated instance of this builder with the Shared Token Cache credential exclusion set as specified.
     */
    public DefaultAzureCredentialBuilder excludeSharedTokenCacheCredential() {
        excludeSharedTokenCacheCredential = true;
        return this;
    }

    /**
     * Excludes the {@link AzureCliCredential} from the {@link DefaultAzureCredential} authentication flow.
     *
     * @return An updated instance of this builder with the Azure Cli credential exclusion set as specified.
     */
    public DefaultAzureCredentialBuilder excludeAzureCliCredential() {
        excludeAzureCliCredential = true;
        return this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link ForkJoinPool#commonPool()} will be used which is
     * also shared with other application tasks. If the common pool is heavily used for other tasks, authentication
     * requests might starve and setting up this executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    public DefaultAzureCredentialBuilder executorService(ExecutorService executorService) {
        this.identityClientOptions.setExecutorService(executorService);
        return this;
    }

    /**
     * Creates new {@link DefaultAzureCredential} with the configured options set.
     *
     * @return a {@link DefaultAzureCredential} with the current configurations.
     */
    public DefaultAzureCredential build() {
        return new DefaultAzureCredential(getCredentialsChain());
    }

    private ArrayDeque<TokenCredential> getCredentialsChain() {
        ArrayDeque<TokenCredential> output = new ArrayDeque<>(4);
        if (!excludeEnvironmentCredential) {
            output.add(new EnvironmentCredential(identityClientOptions));
        }

        if (!excludeManagedIdentityCredential) {
            output.add(new ManagedIdentityCredential(null, identityClientOptions));
        }

        if (!excludeSharedTokenCacheCredential) {
            output.add(new SharedTokenCacheCredential(null, "04b07795-8ddb-461a-bbee-02f9e1bf7b46",
                null, identityClientOptions));
        }

        if (!excludeAzureCliCredential) {
            output.add(new AzureCliCredential(identityClientOptions));
        }

        if (output.size() == 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException("At least one credential type must be"
                                                                         + " included in the authentication flow."));
        }
        return output;
    }
}
