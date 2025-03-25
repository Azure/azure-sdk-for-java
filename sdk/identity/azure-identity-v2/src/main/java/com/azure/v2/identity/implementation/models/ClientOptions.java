// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import com.azure.v2.identity.AzureAuthorityHosts;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import com.azure.v2.identity.implementation.client.IdentityLogOptionsImpl;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

/**
 * Represents abstract base for Client Options used in Managed Identity OAuth Flow .
 */
public class ClientOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ClientOptions.class);
    private HttpPipelineOptions httpPipelineOptions;
    private String authorityHost;
    private ExecutorService executorService;
    private TokenCachePersistenceOptions tokenCachePersistenceOptions;
    private boolean instanceDiscovery;
    private Set<String> additionallyAllowedTenants;
    private Configuration configuration;
    private String tenantId;
    private String clientId;
    private boolean isChained;
    private boolean unsafeSupportLoggingEnabled;
    private IdentityLogOptionsImpl identityLogOptions;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public ClientOptions() {
        configuration = Configuration.getGlobalConfiguration();
        loadFromConfiguration(configuration);
        instanceDiscovery = true;
        additionallyAllowedTenants = new HashSet<>(2);
    }

    /**
     * Creates a copy of client options from provided client options instance.
     *
     * @param clientOptions the client options to copy.
     */
    public ClientOptions(ClientOptions clientOptions) {
        this.clientId = clientOptions.getClientId();
        this.additionallyAllowedTenants = clientOptions.getAdditionallyAllowedTenants();
        this.httpPipelineOptions = clientOptions.getHttpPipelineOptions();
        this.configuration = clientOptions.getConfiguration();
        this.identityLogOptions = clientOptions.getIdentityLogOptions();
        this.authorityHost = clientOptions.getAuthorityHost();
        this.executorService = clientOptions.getExecutorService();
        this.instanceDiscovery = clientOptions.isInstanceDiscoveryEnabled();
        this.isChained = clientOptions.isChained();
        this.tenantId = clientOptions.getTenantId();
        this.tokenCachePersistenceOptions = clientOptions.getTokenCacheOptions();
        this.unsafeSupportLoggingEnabled = clientOptions.isUnsafeSupportLoggingEnabled();
    }

    /**
     * Gets the configured configuration store.
     *
     * @return the configured {@link Configuration} store.
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Sets the configuration store.
     *
     * @param configuration the configuration store
     * @return the updated options
     */
    public ClientOptions setConfigurationStore(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Gets the Http pipeline options.
     * @return the http pipeline options.
     */
    public HttpPipelineOptions getHttpPipelineOptions() {
        if (this.httpPipelineOptions == null) {
            this.httpPipelineOptions = new HttpPipelineOptions();
        }
        return this.httpPipelineOptions;
    }

    /**
     * Sets the Http pipeline options.
     *
     * @param pipelineOptions the http pipeline options.
     * @return the updated options
     */
    ClientOptions setHttpPipelineOptions(HttpPipelineOptions pipelineOptions) {
        this.httpPipelineOptions = pipelineOptions;
        return this;
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
     * @return the updated options
     */
    public ClientOptions setAuthorityHost(String authorityHost) {
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
     * @return the updated options
     */
    public ClientOptions setExecutorService(ExecutorService executorService) {
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
     * Specifies the {@link TokenCachePersistenceOptions} to be used for token cache persistence.
     *
     * @param tokenCachePersistenceOptions the options configuration
     * @return the updated options
     */
    public ClientOptions setTokenCacheOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
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
    public ClientOptions disableInstanceDiscovery() {
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
     * @return the updated options
     */
    @SuppressWarnings("unchecked")
    public ClientOptions setAdditionallyAllowedTenants(List<String> additionallyAllowedTenants) {
        this.additionallyAllowedTenants = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.additionallyAllowedTenants.addAll(additionallyAllowedTenants);
        return this;
    }

    /**
     * Internal helper method for copy.
     *
     * @param additionallyAllowedTenants the additionally allowed Tenants.
     * @return the updated options
     */
    ClientOptions setAdditionallyAllowedTenants(Set<String> additionallyAllowedTenants) {
        this.additionallyAllowedTenants = additionallyAllowedTenants;
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
     * @return the updated options
     */
    public ClientOptions setClientId(String clientId) {
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
     * @return the updated options
     */
    public ClientOptions setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Checks whether credential is chained or not.
     *
     * @return the boolean flag indicating whether credential is chained or not.
     */
    public boolean isChained() {
        return isChained;
    }

    /**
     * Sets whether credential is chained or not.
     *
     * @param chained the boolean flag to indicate whether credential is chained or not
     * @return the updated options
     */
    public ClientOptions setChained(boolean chained) {
        isChained = chained;
        return this;
    }

    /**
     * Gets the identity log options.
     *
     * @return the identity log options
     */
    public IdentityLogOptionsImpl getIdentityLogOptions() {
        return identityLogOptions;
    }

    /**
     * Sets the identity log options.
     *
     * @param identityLogOptions the identity log options
     * @return the updated options
     */
    public ClientOptions setIdentityLogOptions(IdentityLogOptionsImpl identityLogOptions) {
        this.identityLogOptions = identityLogOptions;
        return this;
    }

    /**
     * Checks whether unsafe logging is enabled or not.
     *
     * @return the boolean flag indicating whether unsafe logging is enabled or not
     */
    public boolean isUnsafeSupportLoggingEnabled() {
        return unsafeSupportLoggingEnabled;
    }

    /**
     * Sets the boolean flag indicating whether unsafe logging is enabled or not.
     *
     * @param unsafeSupportLoggingEnabled the boolean flag to indicate unsafe logging enabled or not
     * @return the updated options
     */
    public ClientOptions setUnsafeSupportLoggingEnabled(boolean unsafeSupportLoggingEnabled) {
        this.unsafeSupportLoggingEnabled = unsafeSupportLoggingEnabled;
        return this;
    }
}
