// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

/**
 * Fluent credential builder for instantiating a {@link GitHubActionsCredential}.
 *
 * <!-- src_embed com.azure.identity.credential.githubactionscredential.construct -->
 * <pre>
 * TokenCredential credential = new GitHubActionsCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.githubactionscredential.construct -->
 *
 * @see GitHubActionsCredential
 */
public class GitHubActionsCredentialBuilder extends AadCredentialBuilderBase<GitHubActionsCredentialBuilder> {

    /**
     * Creates an instance of {@link GitHubActionsCredentialBuilder}.
     */
    public GitHubActionsCredentialBuilder() {
        super();
    }

    /**
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public GitHubActionsCredentialBuilder
        tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Creates a new {@link GitHubActionsCredential} with the current configurations.
     *
     * @return a {@link GitHubActionsCredential} with the current configurations.
     * @throws CredentialUnavailableException if required configuration or environment variables are missing
     */
    public GitHubActionsCredential build() {
        Configuration configuration = identityClientOptions.getConfiguration() == null
            ? Configuration.getGlobalConfiguration().clone()
            : identityClientOptions.getConfiguration();

        String tenantIdInput
            = CoreUtils.isNullOrEmpty(tenantId) ? configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID) : tenantId;

        String clientIdInput
            = CoreUtils.isNullOrEmpty(clientId) ? configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID) : clientId;

        String oidcRequestUrl = configuration.get(GitHubActionsCredential.ACTIONS_ID_TOKEN_REQUEST_URL);
        String oidcRequestToken = configuration.get(GitHubActionsCredential.ACTIONS_ID_TOKEN_REQUEST_TOKEN);

        return new GitHubActionsCredential(clientIdInput, tenantIdInput, oidcRequestUrl, oidcRequestToken,
            identityClientOptions.clone());
    }
}
