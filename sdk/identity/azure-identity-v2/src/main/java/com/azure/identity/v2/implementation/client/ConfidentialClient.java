// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.client;

import com.azure.identity.v2.CredentialAuthenticationException;
import com.azure.identity.v2.TokenCachePersistenceOptions;
import com.azure.identity.v2.implementation.models.ConfidentialClientOptions;
import com.azure.identity.v2.implementation.models.MsalToken;
import com.azure.identity.v2.implementation.util.IdentityUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.utils.SharedExecutorService;

import java.net.MalformedURLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class ConfidentialClient extends ClientBase {

    static final ClientLogger LOGGER = new ClientLogger(ConfidentialClient.class);
    static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");
    static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    final ConfidentialClientOptions confidentialClientOptions;

    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;
    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessorWithCae;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param options the options configuring the client.
     */
    public ConfidentialClient(ConfidentialClientOptions options) {
        super(options);
        this.confidentialClientOptions = options == null ? new ConfidentialClientOptions() : options;

        this.confidentialClientApplicationAccessor = new SynchronousAccessor<>(() -> this.getClient(false));

        this.confidentialClientApplicationAccessorWithCae
            = new SynchronousAccessor<>(() -> this.getClient(true));
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticate(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClient = getConfidentialClientInstance(request).getValue();
        ClientCredentialParameters.ClientCredentialParametersBuilder builder
            = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, getMsalOptions()));
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
    public AccessToken authenticateWithCache(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClientApplication = getConfidentialClientInstance(request).getValue();
        SilentParameters.SilentParametersBuilder parametersBuilder
            = SilentParameters.builder(new HashSet<>(request.getScopes()))
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, getMsalOptions()));

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
                LOGGER.atLevel(LogLevel.VERBOSE).log("Token not found in the MSAL cache.");
                return null;
            } else {
                throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(e.getMessage()));
            }
        }
    }

    ConfidentialClientApplication getClient(boolean enableCae) {
        if (clientId == null) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(getMsalOptions().getAuthorityHost()).replaceAll("") + "/" + tenantId;
        IClientCredential credential;

        if (confidentialClientOptions.getClientSecret() != null) {
            credential = ClientCredentialFactory.createFromSecret(confidentialClientOptions.getClientSecret());
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
                .instanceDiscovery(getMsalOptions().isInstanceDiscoveryEnabled());

            if (!getMsalOptions().isInstanceDiscoveryEnabled()) {
                LOGGER.atLevel(LogLevel.VERBOSE)
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

        if (getMsalOptions().getExecutorService() != null) {
            applicationBuilder.executorService(getMsalOptions().getExecutorService());
        } else {
            applicationBuilder.executorService(SharedExecutorService.getInstance());
        }

        TokenCachePersistenceOptions tokenCachePersistenceOptions = getMsalOptions().getTokenCacheOptions();
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
}
