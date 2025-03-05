// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.client;

import com.azure.identity.v2.BrowserCustomizationOptions;
import com.azure.identity.v2.CredentialAuthenticationException;
import com.azure.identity.v2.TokenCachePersistenceOptions;
import com.azure.identity.v2.implementation.models.MsalToken;
import com.azure.identity.v2.implementation.models.PublicClientOptions;
import com.azure.identity.v2.implementation.util.IdentityUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.core.utils.CoreUtils;
import com.microsoft.aad.msal4j.*;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.utils.SharedExecutorService;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class PublicClient extends ClientBase {

    static final ClientLogger LOGGER = new ClientLogger(PublicClient.class);
    static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");
    static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    static final String HTTP_LOCALHOST = "http://localhost";

    final PublicClientOptions options;

    private final SynchronousAccessor<PublicClientApplication> clientAccessor;
    private final SynchronousAccessor<PublicClientApplication> clientAccessorWithCae;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param options the options configuring the client.
     */
    public PublicClient(PublicClientOptions options) {
        super(options);
        this.options = options == null ? new PublicClientOptions() : options;

        this.clientAccessor = new SynchronousAccessor<>(() -> this.getClient(false));

        this.clientAccessorWithCae
            = new SynchronousAccessor<>(() -> this.getClient(true));
    }


    private SynchronousAccessor<PublicClientApplication>
    getClientInstance(TokenRequestContext request) {
        return request.isCaeEnabled()
            ? clientAccessorWithCae
            : clientAccessor;
    }

    /**
     * Acquire a token from the currently logged in client.
     *
     * @param request the details of the token request
     * @param account the account used to log in to acquire the last token
     * @return An access token, or null if no token exists in the cache.
     */
    @SuppressWarnings("deprecation")
    public MsalToken authenticateWithPublicClientCache(TokenRequestContext request, IAccount account) {
        PublicClientApplication pc = getClientInstance(request).getValue();
        MsalToken token = acquireTokenFromPublicClientSilently(request, pc, account, false);
        if (OffsetDateTime.now().isAfter(token.getExpiresAt().minus(REFRESH_OFFSET))) {
            token = acquireTokenFromPublicClientSilently(request, pc, account, true);
        }
        return token;
    }

    private MsalToken acquireTokenFromPublicClientSilently(TokenRequestContext request, PublicClientApplication pc,
                                                           IAccount account, boolean forceRefresh) {
        SilentParameters.SilentParametersBuilder parametersBuilder
            = SilentParameters.builder(new HashSet<>(request.getScopes()));

        if (forceRefresh) {
            parametersBuilder.forceRefresh(true);
        }

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
            parametersBuilder.forceRefresh(true);
        }

        if (account != null) {
            parametersBuilder = parametersBuilder.account(account);
        }
        parametersBuilder.tenant(IdentityUtil.resolveTenantId(tenantId, request, options.getMsalCommonOptions()));
        try {
            return new MsalToken(pc.acquireTokenSilently(parametersBuilder.build()).get());
        } catch (MalformedURLException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e.getMessage(), e));
        } catch (ExecutionException | InterruptedException e) {
            // Cache misses should not throw an exception, but should log.
            if (e.getMessage().contains("Token not found in the cache")) {
                LOGGER.atLevel(LogLevel.VERBOSE).log("Token not found in the MSAL cache.");
                return null;
            } else {
                throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(e.getMessage(), e));
            }
        }
    }

    PublicClientApplication getClient(boolean enableCae) {
        if (clientId == null) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(options.getMsalCommonOptions().getAuthorityHost())
            .replaceAll("") + "/" + tenantId;
        PublicClientApplication.Builder builder = PublicClientApplication.builder(clientId);
        try {
            builder = builder
                .authority(authorityUrl)
                .instanceDiscovery(options.getMsalCommonOptions().isInstanceDiscoveryEnabled());

            if (!options.getMsalCommonOptions().isInstanceDiscoveryEnabled()) {
                LOGGER.atLevel(LogLevel.VERBOSE).log("Instance discovery and authority validation is disabled. In this"
                    + " state, the library will not fetch metadata to validate the specified authority host. As a"
                    + " result, it is crucial to ensure that the configured authority host is valid and trustworthy.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logThrowableAsWarning(new IllegalStateException(e));
        }

        initializeHttpPipelineAdapter();
        builder.httpClient(httpPipelineAdapter);


        if (options.getMsalCommonOptions().getExecutorService() != null) {
            builder.executorService(options.getMsalCommonOptions().getExecutorService());
        } else {
            builder.executorService(SharedExecutorService.getInstance());
        }

        if (enableCae) {
            Set<String> set = new HashSet<>(1);
            set.add("CP1");
            builder.clientCapabilities(set);
        }

        TokenCachePersistenceOptions tokenCachePersistenceOptions = options.getMsalCommonOptions().getTokenCacheOptions();
        PersistentTokenCacheImpl tokenCache = null;
        if (tokenCachePersistenceOptions != null) {
            try {
                tokenCache = new PersistentTokenCacheImpl(enableCae)
                    .setAllowUnencryptedStorage(tokenCachePersistenceOptions.isUnencryptedStorageAllowed())
                    .setName(tokenCachePersistenceOptions.getName());
                builder.setTokenCacheAccessAspect(tokenCache);
            } catch (Throwable t) {
                throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(
                    "Shared token cache is unavailable in this environment.", t));
            }
        }
        PublicClientApplication publicClientApplication = builder.build();

        if (tokenCache != null) {
            tokenCache.registerCache();
        }
        return publicClientApplication;
    }

    /**
     * Synchronously acquire a token from Active Directory by opening a browser and wait for the user to login. The
     * credential will run a minimal local HttpServer at the given port, so {@code http://localhost:{port}} must be
     * listed as a valid reply URL for the application.
     *
     * @param request the details of the token request
     * @param port the port on which the HTTP server is listening
     * @param redirectUrl the redirect URL to listen on and receive security code
     * @param loginHint the username suggestion to pre-fill the login page's username/email address field
     * @return a Publisher that emits an AccessToken
     */
    public MsalToken authenticateWithBrowserInteraction(TokenRequestContext request, Integer port, String redirectUrl,
                                                        String loginHint) {
        URI redirectUri;
        String redirect;

        if (port != null) {
            redirect = HTTP_LOCALHOST + ":" + port;
        } else if (redirectUrl != null) {
            redirect = redirectUrl;
        } else {
            redirect = HTTP_LOCALHOST;
        }

        try {
            redirectUri = new URI(redirect);
        } catch (URISyntaxException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
        PublicClientApplication pc = getClientInstance(request).getValue();

        // If the broker is enabled, try to get the token for the default account by passing
        // a null account to MSAL. If that fails, show the dialog.
        MsalToken token = null;
        if (token == null) {
            InteractiveRequestParameters.InteractiveRequestParametersBuilder builder
                = buildInteractiveRequestParameters(request, loginHint, redirectUri);

            try {
                return new MsalToken(pc.acquireToken(builder.build()).get());
            } catch (Exception e) {
                throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(
                    "Failed to acquire token with Interactive Browser Authentication.", e));
            }
        }
        return token;
    }

    InteractiveRequestParameters.InteractiveRequestParametersBuilder
    buildInteractiveRequestParameters(TokenRequestContext request, String loginHint, URI redirectUri) {
        InteractiveRequestParameters.InteractiveRequestParametersBuilder builder
            = InteractiveRequestParameters.builder(redirectUri)
            .scopes(new HashSet<>(request.getScopes()))
            .prompt(Prompt.SELECT_ACCOUNT)
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, options.getMsalCommonOptions()));

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            builder.claims(claimsRequest);
        }

        BrowserCustomizationOptions browserCustomizationOptions = options.getBrowserCustomizationOptions();

        if (IdentityUtil.browserCustomizationOptionsPresent(browserCustomizationOptions)) {
            SystemBrowserOptions.SystemBrowserOptionsBuilder browserOptionsBuilder = SystemBrowserOptions.builder();
            if (!CoreUtils.isNullOrEmpty(browserCustomizationOptions.getSuccessMessage())) {
                browserOptionsBuilder.htmlMessageSuccess(browserCustomizationOptions.getSuccessMessage());
            }

            if (!CoreUtils.isNullOrEmpty(browserCustomizationOptions.getErrorMessage())) {
                browserOptionsBuilder.htmlMessageError(browserCustomizationOptions.getErrorMessage());
            }
            builder.systemBrowserOptions(browserOptionsBuilder.build());
        }

        if (loginHint != null) {
            builder.loginHint(loginHint);
        }
        return builder;
    }
}
