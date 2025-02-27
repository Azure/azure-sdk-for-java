// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import com.azure.identity.v2.AzureAuthorityHosts;
import com.azure.identity.v2.TokenCachePersistenceOptions;
import com.azure.identity.v2.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

/**
 * Options to configure the IdentityClient.
 */
public class MsalConfigurationOptions implements Cloneable {
    private static final ClientLogger LOGGER = new ClientLogger(MsalConfigurationOptions.class);

    private String authorityHost;
    private ExecutorService executorService;
    private boolean allowUnencryptedCache;
    private TokenCachePersistenceOptions tokenCachePersistenceOptions;
    private boolean instanceDiscovery;
    private Set<String> additionallyAllowedTenants;
    private Configuration configuration;

    private String tenantId;
    private String clientId;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public MsalConfigurationOptions(Configuration configuration) {
        loadFromConfiguration(configuration);
        instanceDiscovery = true;
        additionallyAllowedTenants = new HashSet<>();
    }

    /**
     * @return the Microsoft Entra endpoint to acquire tokens.
     */
    public String getAuthorityHost() {
        return authorityHost;
    }

    /**
     * Specifies the Microsoft Entra endpoint to acquire tokens.
     * @param authorityHost the Microsoft Entra endpoint
     * @return IdentityClientOptions
     */
    public MsalConfigurationOptions setAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
        return this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return IdentityClientOptions
     */
    public MsalConfigurationOptions setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * @return the ExecutorService to execute authentication requests.
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @param allowUnencryptedCache the flag to indicate if unencrypted persistent cache is allowed for use or not.
     * @return The updated identity client options.
     */
    public MsalConfigurationOptions setAllowUnencryptedCache(boolean allowUnencryptedCache) {
        this.allowUnencryptedCache = allowUnencryptedCache;
        return this;
    }

    public boolean getAllowUnencryptedCache() {
        return this.allowUnencryptedCache;
    }


    /**
     * Specifies the {@link TokenCachePersistenceOptions} to be used for token cache persistence.
     *
     * @param tokenCachePersistenceOptions the options configuration
     * @return the updated identity client options
     */
    public MsalConfigurationOptions setTokenCacheOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        this.tokenCachePersistenceOptions = tokenCachePersistenceOptions;
        return this;
    }

    /**
     * Get the configured {@link TokenCachePersistenceOptions}
     *
     * @return the {@link TokenCachePersistenceOptions}
     */
    public TokenCachePersistenceOptions getTokenCacheOptions() {
        return this.tokenCachePersistenceOptions;
    }

    /**
     * Disables authority validation and instance discovery.
     * Instance discovery is acquiring metadata about an authority from https://login.microsoft.com
     * to validate that authority. This may need to be disabled in private cloud or ADFS scenarios.
     *
     * @return the updated client options
     */
    public MsalConfigurationOptions disableInstanceDiscovery() {
        this.instanceDiscovery = false;
        return this;
    }

    /**
     * Gets the instance discovery policy.
     * @return boolean indicating if instance discovery is enabled.
     */
    public boolean isInstanceDiscoveryEnabled() {
        return this.instanceDiscovery;
    }

    /**
     * Loads the details from the specified Configuration Store.
     */
    private void loadFromConfiguration(Configuration configuration) {
        this.configuration = configuration;
        authorityHost = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        ValidationUtil.validateAuthHost(authorityHost, LOGGER);
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the application is installed.
     *
     * @param additionallyAllowedTenants the additionally allowed Tenants.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public MsalConfigurationOptions setAdditionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        this.additionallyAllowedTenants = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.additionallyAllowedTenants.addAll(additionallyAllowedTenants);
        return this;
    }

    /**
     * Get the Additionally Allowed Tenants.
     * @return the List containing additionally allowed tenants.
     */
    public Set<String> getAdditionallyAllowedTenants() {
        return this.additionallyAllowedTenants;
    }

    /**
     * Gets the Client ID.
     * @return The Client ID.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * Sets the Client ID.
     * @param clientId The client ID.
     * @return the MsalConfigurationOptions itself.
     */
    public MsalConfigurationOptions setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Gets the Tenant ID.
     * @return The Tenant ID.
     */
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * Sets the Tenant ID.
     * @param tenantId The tenant ID.
     * @return the MsalConfigurationOptions itself.
     */
    public MsalConfigurationOptions setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public MsalConfigurationOptions clone() {
        MsalConfigurationOptions clone = new MsalConfigurationOptions(this.configuration)
            .setAllowUnencryptedCache(this.allowUnencryptedCache)
            .setExecutorService(this.executorService)
            .setAuthorityHost(this.authorityHost)
            .setAdditionallyAllowedTenants(this.additionallyAllowedTenants.stream().toList())
            .setTokenCacheOptions(this.tokenCachePersistenceOptions);
        return clone;
    }
}
