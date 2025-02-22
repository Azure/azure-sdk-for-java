// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation;

import com.azure.identity.v2.AzureAuthorityHosts;
import com.azure.identity.v2.TokenCachePersistenceOptions;
import com.azure.identity.v2.implementation.util.ValidationUtil;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.pipeline.HttpRedirectOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

/**
 * Options to configure the IdentityClient.
 */
public class IdentityClientOptions implements Cloneable {
    private static final ClientLogger LOGGER = new ClientLogger(IdentityClientOptions.class);

    private String authorityHost;
    private HttpPipeline httpPipeline;
    private ExecutorService executorService;
    private HttpClient httpClient;
    private HttpInstrumentationOptions httpInstrumentationOptions;
    private HttpRetryOptions httpRetryOptions;
    private HttpRedirectOptions httpRedirectOptions;
    private List<HttpPipelinePolicy> httpPipelinePolicy;
    private boolean allowUnencryptedCache;
    private TokenCachePersistenceOptions tokenCachePersistenceOptions;
    private boolean multiTenantAuthDisabled;
    private Configuration configuration;
    private boolean instanceDiscovery;
    private Set<String> additionallyAllowedTenants;


    private Duration credentialProcessTimeout = Duration.ofSeconds(10);

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public IdentityClientOptions() {
        Configuration configuration = Configuration.getGlobalConfiguration();
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
    public IdentityClientOptions setAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
        return this;
    }


    /**
     * @return the HttpPipeline to send all requests
     */
    public HttpPipeline getHttpPipeline() {
        return httpPipeline;
    }

    /**
     * @return the HttpClient to use for requests
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Specifies the HttpPipeline to send all requests. This setting overrides the others.
     * @param httpPipeline the HttpPipeline to send all requests
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setHttpPipeline(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
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
    public IdentityClientOptions setExecutorService(ExecutorService executorService) {
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
     * Specifies the HttpClient to send use for requests.
     * @param httpClient the http client to use for requests
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @param allowUnencryptedCache the flag to indicate if unencrypted persistent cache is allowed for use or not.
     * @return The updated identity client options.
     */
    public IdentityClientOptions setAllowUnencryptedCache(boolean allowUnencryptedCache) {
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
    public IdentityClientOptions setTokenCacheOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
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
     * Sets the specified configuration store.
     *
     * @param configuration the configuration store to be used to read env variables and/or system properties.
     * @return the updated identity client options
     */
    public IdentityClientOptions setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        loadFromConfiguration(configuration);
        return this;
    }

    /**
     * Gets the configured configuration store.
     *
     * @return the configured {@link Configuration} store.
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }


    IdentityClientOptions setConfigurationStore(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Disables authority validation and instance discovery.
     * Instance discovery is acquiring metadata about an authority from https://login.microsoft.com
     * to validate that authority. This may need to be disabled in private cloud or ADFS scenarios.
     *
     * @return the updated client options
     */
    public IdentityClientOptions disableInstanceDiscovery() {
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
        authorityHost = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD;
        ValidationUtil.validateAuthHost(authorityHost, LOGGER);
    }

    /**
     * Gets the Http Instrumentation options.
     *
     * @return the Http Instrumentation options.
     */
    public HttpInstrumentationOptions getHttpInstrumentationOptions() {
        return httpInstrumentationOptions;
    }

    /**
     * Sets the Http Instrumentation Options.
     *
     * @param httpInstrumentationOptions the Http instrumentation options to set.
     */
    public IdentityClientOptions setHttpInstrumentationOptions(HttpInstrumentationOptions httpInstrumentationOptions) {
        this.httpInstrumentationOptions = httpInstrumentationOptions;
        return this;
    }

    /**
     * Gets the Http Retry Options.
     *
     * @return The Http Retry Options.
     */
    public HttpRetryOptions getHttpRetryOptions() {
        return httpRetryOptions;
    }

    /**
     * Sets the Http Retry Options.
     *
     * @param httpRetryOptions the Http Retry Options.
     */
    public IdentityClientOptions setHttpRetryOptions(HttpRetryOptions httpRetryOptions) {
        this.httpRetryOptions = httpRetryOptions;
        return this;
    }

    /**
     * Gets the Http Redirect Options.
     *
     * @return The Http Redirect Options.
     */
    public HttpRedirectOptions getHttpRedirectOptions() {
        return httpRedirectOptions;
    }

    /**
     * Sets the Http Redirect Options.
     *
     * @param httpRedirectOptions the Http Redirect Options.
     */
    public IdentityClientOptions setHttpRedirectOptions(HttpRedirectOptions httpRedirectOptions) {
        this.httpRedirectOptions = httpRedirectOptions;
        return this;
    }

    /**
     * Gets the Http Pipeline Policy list.
     *
     * @return the Http Pipeline Policy List.
     */
    public List<HttpPipelinePolicy> getHttpPipelinePolicy() {
        return httpPipelinePolicy;
    }

    public IdentityClientOptions addHttpPipelinePolicy(HttpPipelinePolicy httpPipelinePolicy) {
        this.httpPipelinePolicy.add(httpPipelinePolicy);
        return this;
    }

    /**
     * For multi-tenant applications, specifies additional tenants for which the credential may acquire tokens.
     * Add the wildcard value "*" to allow the credential to acquire tokens for any tenant the application is installed.
     *
     * @param additionallyAllowedTenants the additionally allowed Tenants.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public IdentityClientOptions setAdditionallyAllowedTenants(List<String> additionallyAllowedTenants) {
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


    public IdentityClientOptions clone() {
        IdentityClientOptions clone
            = new IdentityClientOptions()
                .setAllowUnencryptedCache(this.allowUnencryptedCache)
                .setHttpClient(this.httpClient)
                .setExecutorService(this.executorService)
                .setTokenCacheOptions(this.tokenCachePersistenceOptions)
                .setHttpPipeline(this.httpPipeline)
                .setAuthorityHost(this.authorityHost)
                .setConfigurationStore(this.configuration);
        return clone;
    }
}
