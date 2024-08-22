// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.AuthenticationRecord;
import com.azure.identity.BrowserCustomizationOptions;
import com.azure.identity.ChainedTokenCredential;
import com.azure.identity.TokenCachePersistenceOptions;
import com.azure.identity.implementation.util.IdentityConstants;
import com.azure.identity.implementation.util.ValidationUtil;
import com.microsoft.aad.msal4j.UserAssertion;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
/**
 * Options to configure the IdentityClient.
 */
public final class IdentityClientOptions implements Cloneable {
    private static final ClientLogger LOGGER = new ClientLogger(IdentityClientOptions.class);
    private static final int MAX_RETRY_DEFAULT_LIMIT = 6;
    public static final String AZURE_IDENTITY_DISABLE_MULTI_TENANT_AUTH = "AZURE_IDENTITY_DISABLE_MULTITENANTAUTH";
    public static final String AZURE_POD_IDENTITY_AUTHORITY_HOST = "AZURE_POD_IDENTITY_AUTHORITY_HOST";

    private String authorityHost;
    private BrowserCustomizationOptions browserCustomizationOptions;
    private String imdsAuthorityHost;
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
    private RegionalAuthority regionalAuthority;
    private UserAssertion userAssertion;
    private boolean multiTenantAuthDisabled;
    private Configuration configuration;
    private IdentityLogOptionsImpl identityLogOptionsImpl;
    private boolean accountIdentifierLogging;
    private ManagedIdentityType managedIdentityType;
    private ManagedIdentityParameters managedIdentityParameters;
    private Set<String> additionallyAllowedTenants;
    private ClientOptions clientOptions;
    private HttpLogOptions httpLogOptions;
    private RetryOptions retryOptions;
    private RetryPolicy retryPolicy;
    private List<HttpPipelinePolicy> perCallPolicies;
    private List<HttpPipelinePolicy> perRetryPolicies;
    private boolean instanceDiscovery;

    private Duration credentialProcessTimeout = Duration.ofSeconds(10);

    private boolean isChained;
    private boolean enableUnsafeSupportLogging;
    private long brokerWindowHandle;
    private boolean brokerEnabled;
    private boolean enableMsaPassthrough;
    private boolean useDefaultBrokerAccount;
    private boolean useImdsRetryStrategy;
    private boolean proofOfPossessionRequired;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public IdentityClientOptions() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        loadFromConfiguration(configuration);
        identityLogOptionsImpl = new IdentityLogOptionsImpl();
        browserCustomizationOptions = new BrowserCustomizationOptions();
        maxRetry = MAX_RETRY_DEFAULT_LIMIT;
        retryTimeout = getIMDSretryTimeoutFunction();
        perCallPolicies = new ArrayList<>();
        perRetryPolicies = new ArrayList<>();
        additionallyAllowedTenants = new HashSet<>();
        regionalAuthority = RegionalAuthority.fromString(
            configuration.get(Configuration.PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME));
        instanceDiscovery = true;
    }

    private static Function<Duration, Duration> getIMDSretryTimeoutFunction() {
        return inputDuration -> {
            long retries = inputDuration.getSeconds();
            // Calculate the delay as 800ms * (2 ^ (retries - 1)), ensuring retries start at 1 for the first attempt
            long delay = (long) (800 * Math.pow(2, retries - 1));
            return Duration.ofMillis(delay);
        };
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
     * @return the AKS Pod Authority endpoint to acquire tokens.
     */
    public String getImdsAuthorityHost() {
        return imdsAuthorityHost;
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
     * If this is not configured, the {@link ForkJoinPool#commonPool() common fork join pool} will be used which is
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
     * Gets the regional authority, or null if regional authority should not be used.
     * @return the regional authority value if specified
     */
    public RegionalAuthority getRegionalAuthority() {
        return regionalAuthority;
    }


    /**
     * Configure the User Assertion Scope to be used for OnBehalfOf Authentication request.
     *
     * @param userAssertion the user assertion access token to be used for On behalf Of authentication flow
     * @return the updated identity client options
     */
    public IdentityClientOptions userAssertion(String userAssertion) {
        this.userAssertion = new UserAssertion(userAssertion);
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

    /**
     * Gets the status whether multi tenant auth is disabled or not.
     * @return the flag indicating if multi tenant is disabled or not.
     */
    public boolean isMultiTenantAuthenticationDisabled() {
        return multiTenantAuthDisabled;
    }

    /**
     * Disable the multi tenant authentication.
     * @return the updated identity client options
     */
    public IdentityClientOptions disableMultiTenantAuthentication() {
        this.multiTenantAuthDisabled = true;
        return this;
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

    /**
     * Get the configured Identity Log options.
     * @return the identity log options.
     */
    public IdentityLogOptionsImpl getIdentityLogOptionsImpl() {
        return identityLogOptionsImpl;
    }

    /**
     * Set the Identity Log options.
     * @return the identity log options.
     */
    public IdentityClientOptions setIdentityLogOptionsImpl(IdentityLogOptionsImpl identityLogOptionsImpl) {
        this.identityLogOptionsImpl = identityLogOptionsImpl;
        return this;
    }

    /**
     * Set the Managed Identity Type
     * @param managedIdentityType the Managed Identity Type
     * @return the updated identity client options
     */
    public IdentityClientOptions setManagedIdentityType(ManagedIdentityType managedIdentityType) {
        this.managedIdentityType = managedIdentityType;
        return this;
    }

    /**
     * Get the Managed Identity Type
     * @return the Managed Identity Type
     */
    public ManagedIdentityType getManagedIdentityType() {
        return managedIdentityType;
    }

    /**
     * Get the Managed Identity parameters
     * @return the Managed Identity Parameters
     */
    public ManagedIdentityParameters getManagedIdentityParameters() {
        return managedIdentityParameters;
    }

    /**
     * Configure the managed identity parameters.
     *
     * @param managedIdentityParameters the managed identity parameters to use for authentication.
     * @return the updated identity client options
     */
    public IdentityClientOptions setManagedIdentityParameters(ManagedIdentityParameters managedIdentityParameters) {
        this.managedIdentityParameters = managedIdentityParameters;
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

    /**
     * Configure the client options.
     * @param clientOptions the client options input.
     * @return the updated client options
     */
    public IdentityClientOptions setClientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /**
     * Get the configured client options.
     * @return the client options.
     */
    public ClientOptions getClientOptions() {
        return this.clientOptions;
    }

    /**
     * Configure the client options.
     * @param httpLogOptions the Http log options input.
     * @return the updated client options
     */
    public IdentityClientOptions setHttpLogOptions(HttpLogOptions httpLogOptions) {
        this.httpLogOptions = httpLogOptions;
        return this;
    }

    /**
     * Get the configured Http log options.
     * @return the Http log options.
     */
    public HttpLogOptions getHttpLogOptions() {
        return this.httpLogOptions;
    }

    /**
     * Configure the retry options.
     * @param retryOptions the retry options input.
     * @return the updated client options
     */
    public IdentityClientOptions setRetryOptions(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Get the configured retry options.
     * @return the retry options.
     */
    public RetryOptions getRetryOptions() {
        return this.retryOptions;
    }

    /**
     * Configure the retry policy.
     * @param retryPolicy the retry policy.
     * @return the updated client options
     */
    public IdentityClientOptions setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }


    /**
     * Configures the proof of possession requirement.
     * @param proofOfPossessionRequired
     * @return
     */
    public IdentityClientOptions setProofOfPossessionRequired(boolean proofOfPossessionRequired) {
        this.proofOfPossessionRequired = proofOfPossessionRequired;
        return this;
    }

    /**
     * Get the configured proof of possession requirement.
     * @return the proof of possession requirement.
     */
    public boolean isProofOfPossessionRequired() {
        return proofOfPossessionRequired;
    }

    /**
     * Get the configured retry policy.
     * @return the retry policy.
     */
    public RetryPolicy getRetryPolicy() {
        return this.retryPolicy;
    }

    public boolean getUseImdsRetryStrategy() {
        return this.useImdsRetryStrategy;
    }

    public void setUseImdsRetryStrategy() {
        this.useImdsRetryStrategy = true;
    }


    /**
     * Add a per call policy.
     * @param httpPipelinePolicy the http pipeline policy to add.
     * @return the updated client options
     */
    public IdentityClientOptions addPerCallPolicy(HttpPipelinePolicy httpPipelinePolicy) {
        this.perCallPolicies.add(httpPipelinePolicy);
        return this;
    }

    /**
     * Add a per retry policy.
     * @param httpPipelinePolicy the retry policy to be added.
     * @return the updated client options
     */
    public IdentityClientOptions addPerRetryPolicy(HttpPipelinePolicy httpPipelinePolicy) {
        this.perRetryPolicies.add(httpPipelinePolicy);
        return this;
    }

    /**
     * Get the configured per retry policies.
     * @return the per retry policies.
     */
    public List<HttpPipelinePolicy> getPerRetryPolicies() {
        return this.perRetryPolicies;
    }

    /**
     * Get the configured per call policies.
     * @return the per call policies.
     */
    public List<HttpPipelinePolicy> getPerCallPolicies() {
        return this.perCallPolicies;
    }

    IdentityClientOptions setMultiTenantAuthDisabled(boolean multiTenantAuthDisabled) {
        this.multiTenantAuthDisabled = multiTenantAuthDisabled;
        return this;
    }

    IdentityClientOptions setAdditionallyAllowedTenants(Set<String> additionallyAllowedTenants) {
        this.additionallyAllowedTenants = additionallyAllowedTenants;
        return this;
    }

    /**
     * Specifies either the specific regional authority, or use {@link RegionalAuthority#AUTO_DISCOVER_REGION} to attempt to auto-detect the region.
     *
     * @param regionalAuthority the regional authority
     * @return the updated identity client options
     */
    IdentityClientOptions setRegionalAuthority(RegionalAuthority regionalAuthority) {
        this.regionalAuthority = regionalAuthority;
        return this;
    }

    IdentityClientOptions setConfigurationStore(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    IdentityClientOptions setUserAssertion(UserAssertion userAssertion) {
        this.userAssertion = userAssertion;
        return this;
    }

    IdentityClientOptions setPersistenceCache(boolean persistenceCache) {
        this.sharedTokenCacheEnabled = persistenceCache;
        return this;
    }

    IdentityClientOptions setImdsAuthorityHost(String imdsAuthorityHost) {
        this.imdsAuthorityHost = imdsAuthorityHost;
        return this;
    }

    IdentityClientOptions setPerCallPolicies(List<HttpPipelinePolicy> perCallPolicies) {
        this.perCallPolicies = perCallPolicies;
        return this;
    }

    IdentityClientOptions setPerRetryPolicies(List<HttpPipelinePolicy> perRetryPolicies) {
        this.perRetryPolicies = perRetryPolicies;
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

    public IdentityClientOptions setBrowserCustomizationOptions(BrowserCustomizationOptions browserCustomizationOptions) {
        this.browserCustomizationOptions = browserCustomizationOptions;
        return this;
    }

    public BrowserCustomizationOptions getBrowserCustomizationOptions() {
        return this.browserCustomizationOptions;
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
        authorityHost = configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST,
            AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);
        imdsAuthorityHost = configuration.get(AZURE_POD_IDENTITY_AUTHORITY_HOST,
            IdentityConstants.DEFAULT_IMDS_ENDPOINT);
        ValidationUtil.validateAuthHost(authorityHost, LOGGER);
        multiTenantAuthDisabled = configuration
            .get(AZURE_IDENTITY_DISABLE_MULTI_TENANT_AUTH, false);
    }

    /**
     * Gets the timeout to apply to developer credential operations.
     * @return The timeout value for developer credential operations.
     */
    public Duration getCredentialProcessTimeout() {
        return credentialProcessTimeout;
    }

    /**
     * Sets the timeout for developer credential operations.
     * @param credentialProcessTimeout The timeout value for developer credential operations.
     */
    public void setCredentialProcessTimeout(Duration credentialProcessTimeout) {
        this.credentialProcessTimeout = credentialProcessTimeout;
    }

    /**
     * Indicates whether this options instance is part of a {@link ChainedTokenCredential}.
     * @return true if this options instance is part of a {@link ChainedTokenCredential}, false otherwise.
     */
    public boolean isChained() {
        return this.isChained;
    }

    /**
     * Sets whether this options instance is part of a {@link ChainedTokenCredential}.
     * @param isChained
     * @return the updated client options
     */
    public IdentityClientOptions setChained(boolean isChained) {
        this.isChained = isChained;
        return this;
    }

    /**
     * Gets the status whether support logging is enabled or not.
     * @return the flag indicating if support logging is enabled or not.
     */
    public boolean isUnsafeSupportLoggingEnabled() {
        return enableUnsafeSupportLogging;
    }

    /**
     * Enables additional support logging (including PII) for MSAL based credentials.
     * @return the updated client options
     */
    public IdentityClientOptions enableUnsafeSupportLogging() {
        this.enableUnsafeSupportLogging = true;
        return this;
    }

    /**
     * Gets the window handle for use with the interactive broker.
     * @return the window handle for use with the interactive broker.
     */
    public IdentityClientOptions setBrokerWindowHandle(long windowHandle) {
        this.brokerEnabled = true;
        this.brokerWindowHandle = windowHandle;
        return this;
    }

    /**
     * Gets the window handle for use with the interactive broker.
     * @return the window handle for use with the interactive broker.
     */
    public long getBrokerWindowHandle() {
        return this.brokerWindowHandle;
    }

    /**
     * Gets the status whether broker is enabled or not.
     * @return the flag indicating if broker is enabled or not.
     */
    public boolean isBrokerEnabled() {
        return this.brokerEnabled;
    }

    /**
     * Enables MSA passthrough.
     */
    public IdentityClientOptions setEnableLegacyMsaPassthrough(boolean enableMsaPassthrough) {
        this.brokerEnabled = true;
        this.enableMsaPassthrough = enableMsaPassthrough;
        return this;
    }

    /**
     * Sets whether to use the logged-in user's account for broker authentication.
     * @param useDefaultBrokerAccount
     * @return the updated client options
     */
    public IdentityClientOptions setUseDefaultBrokerAccount(boolean useDefaultBrokerAccount) {
        this.useDefaultBrokerAccount = useDefaultBrokerAccount;
        return this;
    }

    /**
     * Gets the status whether MSA passthrough is enabled or not.
     * @return the flag indicating if MSA passthrough is enabled or not.
     */
    public boolean isMsaPassthroughEnabled() {
        return this.enableMsaPassthrough;
    }

    /**
     * Gets the status whether to use the logged-in user's account for broker authentication.
     * @return the flag indicating if the logged-in user's account should be used for broker authentication.
     */
    public boolean useDefaultBrokerAccount() {
        return this.useDefaultBrokerAccount;
    }

    public IdentityClientOptions clone() {
        IdentityClientOptions clone =  new IdentityClientOptions()
            .setAdditionallyAllowedTenants(this.additionallyAllowedTenants)
            .setAllowUnencryptedCache(this.allowUnencryptedCache)
            .setHttpClient(this.httpClient)
            .setAuthenticationRecord(this.authenticationRecord)
            .setExecutorService(this.executorService)
            .setIdentityLogOptionsImpl(this.identityLogOptionsImpl)
            .setTokenCacheOptions(this.tokenCachePersistenceOptions)
            .setRetryTimeout(this.retryTimeout)
            .setRegionalAuthority(this.regionalAuthority)
            .setHttpPipeline(this.httpPipeline)
            .setIncludeX5c(this.includeX5c)
            .setProxyOptions(this.proxyOptions)
            .setMaxRetry(this.maxRetry)
            .setIntelliJKeePassDatabasePath(this.keePassDatabasePath)
            .setAuthorityHost(this.authorityHost)
            .setImdsAuthorityHost(this.imdsAuthorityHost)
            .setMultiTenantAuthDisabled(this.multiTenantAuthDisabled)
            .setUserAssertion(this.userAssertion)
            .setConfigurationStore(this.configuration)
            .setPersistenceCache(this.sharedTokenCacheEnabled)
            .setClientOptions(this.clientOptions)
            .setHttpLogOptions(this.httpLogOptions)
            .setRetryOptions(this.retryOptions)
            .setRetryPolicy(this.retryPolicy)
            .setPerCallPolicies(this.perCallPolicies)
            .setPerRetryPolicies(this.perRetryPolicies)
            .setBrowserCustomizationOptions(this.browserCustomizationOptions)
            .setChained(this.isChained);

        if (isBrokerEnabled()) {
            clone.setBrokerWindowHandle(this.brokerWindowHandle);
            clone.setEnableLegacyMsaPassthrough(this.enableMsaPassthrough);
        }
        if (!isInstanceDiscoveryEnabled()) {
            clone.disableInstanceDiscovery();
        }
        if (isUnsafeSupportLoggingEnabled()) {
            clone.enableUnsafeSupportLogging();
        }
        return clone;
    }
}
