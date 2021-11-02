// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityConstants;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Fluent credential builder for instantiating a {@link DefaultAzureCredential}.
 *
 * @see DefaultAzureCredential
 */
public class DefaultAzureCredentialBuilder extends CredentialBuilderBase<DefaultAzureCredentialBuilder> {
    private String tenantId;
    private String managedIdentityClientId;
    private final ClientLogger logger = new ClientLogger(DefaultAzureCredentialBuilder.class);

    /**
     * Creates an instance of a DefaultAzureCredentialBuilder.
     */
    public DefaultAzureCredentialBuilder() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        tenantId = configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        managedIdentityClientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
    }

    /**
     * Sets the tenant id of the user to authenticate through the {@link DefaultAzureCredential}. If unset, the value
     * in the AZURE_TENANT_ID environment variable will be used. If neither is set, the default is null
     * and will authenticate users to their default tenant.
     *
     * @param tenantId the tenant ID to set.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    public DefaultAzureCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }


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
     * Specifies the KeePass database path to read the cached credentials of Azure toolkit for IntelliJ plugin.
     * The {@code databasePath} is required on Windows platform. For macOS and Linux platform native key chain /
     * key ring will be accessed respectively to retrieve the cached credentials.
     *
     * <p>This path can be located in the IntelliJ IDE.
     * Windows: File -&gt; Settings -&gt; Appearance &amp; Behavior -&gt; System Settings -&gt; Passwords. </p>
     *
     * @param databasePath the path to the KeePass database.
     * @throws IllegalArgumentException if {@code databasePath} is either not specified or is empty.
     * @return An updated instance of this builder with the KeePass database path set as specified.
     */
    public DefaultAzureCredentialBuilder intelliJKeePassDatabasePath(String databasePath) {
        if (CoreUtils.isNullOrEmpty(databasePath)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("The KeePass database path is either empty or not configured."
                                                   + " Please configure it on the builder."));
        }
        this.identityClientOptions.setIntelliJKeePassDatabasePath(databasePath);
        return this;
    }

    /**
     * Specifies the client ID of user assigned or system assigned identity, when this credential is running
     * in an environment with managed identities. If unset, the value in the AZURE_CLIENT_ID environment variable
     * will be used. If neither is set, the default value is null and will only work with system assigned
     * managed identities and not user assigned managed identities.
     *
     * @param clientId the client ID
     * @return the DefaultAzureCredentialBuilder itself
     */
    public DefaultAzureCredentialBuilder managedIdentityClientId(String clientId) {
        this.managedIdentityClientId = clientId;
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

    private ArrayList<TokenCredential> getCredentialsChain() {
        ArrayList<TokenCredential> output = new ArrayList<TokenCredential>(6);
        output.add(new EnvironmentCredential(identityClientOptions));
        output.add(new ManagedIdentityCredential(managedIdentityClientId, identityClientOptions));
        output.add(new SharedTokenCacheCredential(null, IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID,
            tenantId, identityClientOptions));
        output.add(new IntelliJCredential(tenantId, identityClientOptions));
        output.add(new VisualStudioCodeCredential(tenantId, identityClientOptions));
        output.add(new AzureCliCredential(tenantId, identityClientOptions));
        output.add(new AzurePowerShellCredential(tenantId, identityClientOptions));
        return output;
    }
}
