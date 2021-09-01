// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.*;
import com.azure.identity.implementation.util.ValidationUtil;
import com.microsoft.aad.msal4j.UserAssertion;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * Options to configure the IdentityClient.
 */
public final class IdentityClientOptions {
    private static final int MAX_RETRY_DEFAULT_LIMIT = 3;

    private String authorityHost;
    private int maxRetry;
    private Function<Duration, Duration> retryTimeout;
    private ProxyOptions proxyOptions;
    private HttpPipeline httpPipeline;
    private ExecutorService executorService;
    private HttpClient httpClient;
    private boolean allowUnencryptedCache;
    private boolean sharedTokenCacheEnabled;
    private String keePassDatabasePath;
    private boolean includeX5c;
    private AuthenticationRecord authenticationRecord;
    private TokenCachePersistenceOptions tokenCachePersistenceOptions;
    private boolean cp1Disabled;
    private RegionalAuthority regionalAuthority;
    private UserAssertion userAssertion;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public IdentityClientOptions() {
        Configuration configuration = Configuration.getGlobalConfiguration();
        authorityHost = configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST,
            AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
        cp1Disabled = configuration.get(Configuration.PROPERTY_AZURE_IDENTITY_DISABLE_CP1, false);
        ValidationUtil.validateAuthHost(getClass().getSimpleName(), authorityHost);
        maxRetry = MAX_RETRY_DEFAULT_LIMIT;
        retryTimeout = i -> Duration.ofSeconds((long) Math.pow(2, i.getSeconds() - 1));
        regionalAuthority = RegionalAuthority.fromString(
            configuration.get(Configuration.PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME));
    }

    /**
     * @return the Azure Active Directory endpoint to acquire tokens.
     */
    public String getAuthorityHost() {
        return authorityHost;
    }

    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setAuthorityHost(String authorityHost) {
        this.authorityHost = authorityHost;
        return this;
    }

    /**
     * @return the max number of retries when an authentication request fails.
     */
    public int getMaxRetry() {
        return maxRetry;
    }

    /**
     * Specifies the max number of retries when an authentication request fails.
     * @param maxRetry the number of retries
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    /**
     * @return a Function to calculate seconds of timeout on every retried request.
     */
    public Function<Duration, Duration> getRetryTimeout() {
        return retryTimeout;
    }

    /**
     * Specifies a Function to calculate seconds of timeout on every retried request.
     * @param retryTimeout the Function that returns a timeout in seconds given the number of retry
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setRetryTimeout(Function<Duration, Duration> retryTimeout) {
        this.retryTimeout = retryTimeout;
        return this;
    }

    /**
     * @return the options for proxy configuration.
     */
    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    /**
     * Specifies the options for proxy configuration.
     * @param proxyOptions the options for proxy configuration
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setProxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
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
     * Specifies the database to extract IntelliJ cached credentials from.
     * @param keePassDatabasePath the database to extract intellij credentials from.
     * @return IdentityClientOptions
     */
    public IdentityClientOptions setIntelliJKeePassDatabasePath(String keePassDatabasePath) {
        this.keePassDatabasePath = keePassDatabasePath;
        return this;
    }

    /**
     * Gets if the shared token cache is disabled.
     * @return if the shared token cache is disabled.
     */
    public boolean isSharedTokenCacheEnabled() {
        return this.sharedTokenCacheEnabled;
    }

    /**
     * Enables the shared token cache which is disabled by default. If enabled, the client will store tokens
     * in a cache persisted to the machine, protected to the current user, which can be shared by other credentials
     * and processes.
     *
     * @return The updated identity client options.
     */
    public IdentityClientOptions enablePersistentCache() {
        this.sharedTokenCacheEnabled = true;
        return this;
    }

    /*
     * Get the KeePass database path.
     * @return the KeePass database path to extract inellij credentials from.
     */
    public String getIntelliJKeePassDatabasePath() {
        return keePassDatabasePath;
    }

    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord The Authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
     */
    public IdentityClientOptions setAuthenticationRecord(AuthenticationRecord authenticationRecord) {
        this.authenticationRecord = authenticationRecord;
        return this;
    }

    /**
     * Get the status whether x5c claim (public key of the certificate) should be included as part of the authentication
     * request or not.
     * @return the status of x5c claim inclusion.
     */
    public boolean isIncludeX5c() {
        return includeX5c;
    }

    /**
     * Specifies if the x5c claim (public key of the certificate) should be sent as part of the authentication request.
     * The default value is false.
     *
     * @param includeX5c true if the x5c should be sent. Otherwise false
     * @return The updated identity client options.
     */
    public IdentityClientOptions setIncludeX5c(boolean includeX5c) {
        this.includeX5c = includeX5c;
        return this;
    }

    /**
     * Get the configured {@link AuthenticationRecord}.
     *
     * @return {@link AuthenticationRecord}.
     */
    public AuthenticationRecord getAuthenticationRecord() {
        return authenticationRecord;
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
     * Check whether CP1 client capability should be disabled.
     *
     * @return the status indicating if CP1 client capability should be disabled.
     */
    public boolean isCp1Disabled() {
        return this.cp1Disabled;
    }

    /**
     * Specifies either the specific regional authority, or use {@link RegionalAuthority#AUTO_DISCOVER_REGION} to attempt to auto-detect the region.
     *
     * @param regionalAuthority the regional authority
     * @return the updated identity client options
     */
    public IdentityClientOptions setRegionalAuthority(RegionalAuthority regionalAuthority) {
        this.regionalAuthority = regionalAuthority;
        return this;
    }

    /**
     * Gets the regional authority, or null if regional authority should not be used.
     * @return the regional authority value if specified
     */
    public RegionalAuthority getRegionalAuthority() {
        return regionalAuthority;
    }


    /**
     * Configure the User Assertion Scope to be used for OnBehalfOf Authentication request.
     *
     * @param userAssertion the user assertion to be used for On behalf Of authentication flow
     * @return the updated identity client options
     */
    public IdentityClientOptions userAssertion(UserAssertion userAssertion) {
        this.userAssertion = userAssertion;
        return this;
    }

    /**
     * Get the configured {@link UserAssertion}
     *
     * @return the configured user assertion scope
     */
    public UserAssertion getUserAssertion() {
        return this.userAssertion;
    }
}
