// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation;

import com.azure.identity.v2.CredentialAuthenticationException;
import com.azure.identity.v2.TokenCachePersistenceOptions;
import com.azure.identity.v2.implementation.util.IdentityUtil;
import com.microsoft.aad.msal4j.*;
import io.clientcore.core.credential.AccessToken;
import io.clientcore.core.credential.TokenRequestContext;
import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.exception.HttpResponseException;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.SharedExecutorService;
import io.clientcore.core.util.configuration.Configuration;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class ConfidentialClient {

    static final ClientLogger LOGGER = new ClientLogger(ConfidentialClient.class);
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");
    static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);

    private static final String AZURE_IDENTITY_PROPERTIES = "azure-identity.properties";

    private final Map<String, String> properties = new HashMap<>();

    final IdentityClientOptions options;
    final String tenantId;
    final String clientId;
    final String clientSecret;
    HttpPipelineAdapter httpPipelineAdapter;
    HttpPipeline httpPipeline;

    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;
    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessorWithCae;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param tenantId the tenant ID of the application.
     * @param clientId the client ID of the application.
     * @param clientSecret the client secret of the application.
     * @param options the options configuring the client.
     */
    ConfidentialClient(String tenantId, String clientId, String clientSecret, IdentityClientOptions options) {

        if (tenantId == null) {
            tenantId = IdentityUtil.DEFAULT_TENANT;
        }
        if (options == null) {
            options = new IdentityClientOptions();
        }
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.options = options;

        this.confidentialClientApplicationAccessor = new SynchronousAccessor<>(() -> this.getConfidentialClient(false));

        this.confidentialClientApplicationAccessorWithCae
            = new SynchronousAccessor<>(() -> this.getConfidentialClient(true));
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithConfidentialClient(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClient = getConfidentialClientInstance(request).getValue();
        ClientCredentialParameters.ClientCredentialParametersBuilder builder
            = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
        try {
            return new MsalToken(confidentialClient.acquireToken(builder.build()).get());
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    private SynchronousAccessor<ConfidentialClientApplication>
        getConfidentialClientInstance(TokenRequestContext request) {
        return request.isCaeEnabled()
            ? confidentialClientApplicationAccessorWithCae
            : confidentialClientApplicationAccessor;
    }

    /**
     * Acquire a token from the confidential client.
     *
     * @param request the details of the token request
     * @return An access token, or null if no token exists in the cache.
     */
    @SuppressWarnings("deprecation")
    public AccessToken authenticateWithConfidentialClientCache(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClientApplication = getConfidentialClientInstance(request).getValue();
        SilentParameters.SilentParametersBuilder parametersBuilder
            = SilentParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
            parametersBuilder.forceRefresh(true);
        }

        try {
            IAuthenticationResult authenticationResult
                = confidentialClientApplication.acquireTokenSilently(parametersBuilder.build()).get();
            AccessToken accessToken = new MsalToken(authenticationResult);
            if (OffsetDateTime.now().isBefore(accessToken.getExpiresAt().minus(REFRESH_OFFSET))) {
                return accessToken;
            } else {
                throw new IllegalStateException("Received token is close to expiry.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e.getMessage(), e));
        } catch (ExecutionException | InterruptedException e) {
            // Cache misses should not throw an exception, but should log.
            if (e.getMessage().contains("Token not found in the cache")) {
                LOGGER.atLevel(ClientLogger.LogLevel.VERBOSE).log("Token not found in the MSAL cache.");
                return null;
            } else {
                throw LOGGER.logThrowableAsError(new HttpResponseException(e.getMessage(), null, HttpExceptionType.CLIENT_AUTHENTICATION, null ));
            }
        }
    }

    /**
     * Get the configured identity client options.
     *
     * @return the client options.
     */
    public IdentityClientOptions getIdentityClientOptions() {
        return options;
    }


    ConfidentialClientApplication getConfidentialClient(boolean enableCae) {
        if (clientId == null) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/" + tenantId;
        IClientCredential credential;

        if (clientSecret != null) {
            credential = ClientCredentialFactory.createFromSecret(clientSecret);
        } else {
            throw LOGGER.logThrowableAsError(
                new IllegalArgumentException("Must provide client secret or client certificate path."
                    + " To mitigate this issue, please refer to the troubleshooting guidelines here at "
                    + "https://aka.ms/azsdk/java/identity/serviceprincipalauthentication/troubleshoot"));
        }

        ConfidentialClientApplication.Builder applicationBuilder
            = ConfidentialClientApplication.builder(clientId, credential);
        try {
            applicationBuilder = applicationBuilder
                .authority(authorityUrl)
                .instanceDiscovery(options.isInstanceDiscoveryEnabled());

            if (!options.isInstanceDiscoveryEnabled()) {
                LOGGER.atLevel(ClientLogger.LogLevel.VERBOSE)
                    .log("Instance discovery and authority validation is disabled. In this"
                    + " state, the library will not fetch metadata to validate the specified authority host. As a"
                    + " result, it is crucial to ensure that the configured authority host is valid and trustworthy.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(e));
        }

        if (enableCae) {
            Set<String> set = new HashSet<>(1);
            set.add("CP1");
            applicationBuilder.clientCapabilities(set);
        }

        initializeHttpPipelineAdapter();

        if (httpPipelineAdapter != null) {
            applicationBuilder.httpClient(httpPipelineAdapter);
        }

        if (options.getExecutorService() != null) {
            applicationBuilder.executorService(options.getExecutorService());
        } else {
            applicationBuilder.executorService(SharedExecutorService.getInstance());
        }

        TokenCachePersistenceOptions tokenCachePersistenceOptions = options.getTokenCacheOptions();
        PersistentTokenCacheImpl tokenCache = null;
        if (tokenCachePersistenceOptions != null) {
            try {
                tokenCache = new PersistentTokenCacheImpl(enableCae)
                    .setAllowUnencryptedStorage(tokenCachePersistenceOptions.isUnencryptedStorageAllowed())
                    .setName(tokenCachePersistenceOptions.getName());
                applicationBuilder.setTokenCacheAccessAspect(tokenCache);
            } catch (Throwable t) {
                throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(
                    "Shared token cache is unavailable in this environment.", t));
            }
        }

        ConfidentialClientApplication confidentialClientApplication = applicationBuilder.build();

        if (tokenCache != null) {
            tokenCache.registerCache();
        }
        return confidentialClientApplication;
    }

    HttpPipeline getPipeline() {
        // if we've already initialized, return the pipeline
        if (this.httpPipeline != null) {
            return httpPipeline;
        }

        // if the user has supplied a pipeline, use it
        HttpPipeline httpPipeline = options.getHttpPipeline();
        if (httpPipeline != null) {
            this.httpPipeline = httpPipeline;
            return this.httpPipeline;
        }

        // setupPipeline will use the user's HttpClient and HttpClientOptions if they're set
        // otherwise it will use defaults.
        this.httpPipeline = setupPipeline();
        return this.httpPipeline;
    }

    HttpPipeline setupPipeline() {
        Configuration buildConfiguration
            = (options.getConfiguration() == null) ? Configuration.getGlobalConfiguration() : options.getConfiguration();
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        String clientName = properties.getOrDefault(SDK_NAME, "UnknownName");
        String clientVersion = properties.getOrDefault(SDK_VERSION, "UnknownVersion");
        policies.add(new HttpRetryPolicy());
        HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(options.getHttpClient())
            .build();
        return httpPipeline;
    }

    void initializeHttpPipelineAdapter() {
        httpPipelineAdapter = new HttpPipelineAdapter(getPipeline(), options);
    }
}
