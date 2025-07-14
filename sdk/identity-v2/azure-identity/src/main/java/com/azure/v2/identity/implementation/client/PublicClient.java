// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import com.azure.v2.identity.models.BrowserCustomizationOptions;
import com.azure.v2.identity.models.DeviceCodeInfo;
import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.models.MsalToken;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.SystemBrowserOptions;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.RefreshTokenParameters;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.SharedExecutorService;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Public client offers authentication APIs to authenticate via Msal's Public client auth flows.
 */
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

        this.clientAccessorWithCae = new SynchronousAccessor<>(() -> this.getClient(true));
    }

    /**
     * Gets the public client app in a thread safe manner.
     *
     * @param request the token request context
     * @return the accessor holding public client app.
     */
    private SynchronousAccessor<PublicClientApplication> getClientInstance(TokenRequestContext request) {
        return request.isCaeEnabled() ? clientAccessorWithCae : clientAccessor;
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

    /**
     * Acquires token from the msal public client in memory cache if present.
     *
     * @param request the token request context
     * @param pc the public client app
     * @param account the cached account
     * @param forceRefresh the boolean indicating force refresh
     * @return the msal token
     */
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
        parametersBuilder.tenant(IdentityUtil.resolveTenantId(tenantId, request, options));
        try {
            return new MsalToken(pc.acquireTokenSilently(parametersBuilder.build()).get());
        } catch (MalformedURLException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        } catch (ExecutionException | InterruptedException e) {
            // Cache misses should not throw an exception, but should log.
            if (e.getMessage().contains("Token not found in the cache")) {
                LOGGER.atVerbose().log("Token not found in the MSAL cache.");
                return null;
            } else {
                throw LOGGER.throwableAtError().log(e, CredentialAuthenticationException::new);
            }
        }
    }

    /**
     * Gets the public client to be used for auth flows.
     *
     * @param enableCae boolean indicating whether cae should be enabled or not.
     * @return the Public Client Application
     */
    PublicClientApplication getClient(boolean enableCae) {
        if (clientId == null) {
            throw LOGGER.throwableAtError()
                .log("A non-null value for client ID must be provided for user authentication.",
                    IllegalArgumentException::new);
        }
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(options.getAuthorityHost()).replaceAll("") + "/" + tenantId;
        PublicClientApplication.Builder builder = PublicClientApplication.builder(clientId);
        try {
            builder = builder.authority(authorityUrl)
                .instanceDiscovery(options.isInstanceDiscoveryEnabled())
                .logPii(options.isUnsafeSupportLoggingEnabled());

            if (!options.isInstanceDiscoveryEnabled()) {
                LOGGER.atVerbose()
                    .log("Instance discovery and authority validation is disabled. In this"
                        + " state, the library will not fetch metadata to validate the specified authority host. As a"
                        + " result, it is crucial to ensure that the configured authority host is valid and trustworthy.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.throwableAtWarning().log(e, IllegalStateException::new);
        }

        initializeHttpPipelineAdapter();
        builder.httpClient(httpPipelineAdapter);

        if (options.getExecutorService() != null) {
            builder.executorService(options.getExecutorService());
        } else {
            builder.executorService(SharedExecutorService.getInstance());
        }

        if (enableCae) {
            Set<String> set = new HashSet<>(1);
            set.add("CP1");
            builder.clientCapabilities(set);
        }

        TokenCachePersistenceOptions tokenCachePersistenceOptions = options.getTokenCacheOptions();
        PersistentTokenCacheImpl tokenCache = null;
        if (tokenCachePersistenceOptions != null) {
            try {
                tokenCache = new PersistentTokenCacheImpl(enableCae)
                    .setAllowUnencryptedStorage(tokenCachePersistenceOptions.isUnencryptedStorageAllowed())
                    .setName(tokenCachePersistenceOptions.getName());
                builder.setTokenCacheAccessAspect(tokenCache);
            } catch (RuntimeException t) {
                throw LOGGER.throwableAtError()
                    .log("Shared token cache is unavailable in this environment.", t,
                        CredentialAuthenticationException::new);
            }
        }
        PublicClientApplication publicClientApplication = builder.build();

        if (tokenCache != null) {
            tokenCache.registerCache();
        }
        return publicClientApplication;
    }

    /**
     * Acquire a token from MS Entra ID by opening a browser and wait for the user to login. The
     * credential will run a minimal local HttpServer at the given port, so {@code http://localhost:{port}} must be
     * listed as a valid reply URL for the application.
     *
     * @param request the details of the token request
     * @return the msal token
     */
    public MsalToken authenticateWithBrowserInteraction(TokenRequestContext request) {
        URI redirectUri;

        if (options.getRedirectUri() != null) {
            redirectUri = options.getRedirectUri();
        } else {
            try {
                redirectUri = new URI(HTTP_LOCALHOST);
            } catch (URISyntaxException ex) {
                throw LOGGER.throwableAtError().log(ex, IllegalStateException::new);
            }
        }
        PublicClientApplication pc = getClientInstance(request).getValue();

        // If the broker is enabled, try to get the token for the default account by passing
        // a null account to MSAL. If that fails, show the dialog.
        MsalToken token = null;
        if (token == null) {
            InteractiveRequestParameters.InteractiveRequestParametersBuilder builder
                = buildInteractiveRequestParameters(request, options.getLoginHint(), redirectUri);

            try {
                return new MsalToken(pc.acquireToken(builder.build()).get());
            } catch (InterruptedException | ExecutionException e) {
                throw LOGGER.throwableAtError()
                    .log("Failed to acquire token with Interactive Browser Authentication.", e,
                        CredentialAuthenticationException::new);
            }
        }
        return token;
    }

    /**
     * Internal method to build interactive browser auth flow parameters.
     *
     * @param request the token request context
     * @param loginHint the login hint
     * @param redirectUri the redirect URI
     * @return the interactive auth parameters
     */
    InteractiveRequestParameters.InteractiveRequestParametersBuilder
        buildInteractiveRequestParameters(TokenRequestContext request, String loginHint, URI redirectUri) {
        InteractiveRequestParameters.InteractiveRequestParametersBuilder builder
            = InteractiveRequestParameters.builder(redirectUri)
                .scopes(new HashSet<>(request.getScopes()))
                .prompt(Prompt.SELECT_ACCOUNT)
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

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

    /**
     * Acquirs a token from MS Entra ID with a device code challenge. MS Entra ID will provide
     * a device code for login and the user must meet the challenge by authenticating in a browser on the current or a
     * different device.
     *
     * @param request the details of the token request
     * @return the msal token
     */
    public MsalToken authenticateWithDeviceCode(TokenRequestContext request) {
        PublicClientApplication pc = getClientInstance(request).getValue();
        DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder
            = buildDeviceCodeFlowParameters(request, options.getChallengeConsumer());

        try {
            return new MsalToken(pc.acquireToken(parametersBuilder.build()).get());
        } catch (InterruptedException | ExecutionException | RuntimeException e) {
            throw LOGGER.throwableAtError()
                .log("Failed to acquire token with device code.", e, CredentialAuthenticationException::new);
        }
    }

    /**
     * Internal method to build device code auth flow parameters.
     *
     * @param request the token request context
     * @param deviceCodeConsumer the device code consumer logic
     * @return the device code flow parameters.
     */
    DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder buildDeviceCodeFlowParameters(TokenRequestContext request,
        Consumer<DeviceCodeInfo> deviceCodeConsumer) {
        DeviceCodeFlowParameters.DeviceCodeFlowParametersBuilder parametersBuilder = DeviceCodeFlowParameters
            .builder(new HashSet<>(request.getScopes()),
                dc -> deviceCodeConsumer.accept(new DeviceCodeInfo(dc.userCode(), dc.deviceCode(), dc.verificationUri(),
                    OffsetDateTime.now().plusSeconds(dc.expiresIn()), dc.message())))
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
        }
        return parametersBuilder;
    }

    /**
     * Acquire a token from Active Directory with an authorization code from an oauth flow.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     * @throws CredentialAuthenticationException if the authentication fails.
     */
    public MsalToken authenticateWithAuthorizationCode(TokenRequestContext request) {
        AuthorizationCodeParameters.AuthorizationCodeParametersBuilder parametersBuilder
            = AuthorizationCodeParameters.builder(options.getAuthCode(), options.getRedirectUri())
                .scopes(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, options));

        if (request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
        }

        SynchronousAccessor<PublicClientApplication> publicClient = getClientInstance(request);
        try {
            return new MsalToken(publicClient.getValue().acquireToken(parametersBuilder.build()).get());
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.throwableAtError()
                .log("Failed to acquire token with authorization code", e, CredentialAuthenticationException::new);
        }
    }

    /**
     * Authenticates with the azure toolkit auth flow.
     *
     * @param request the token request context
     * @return the msal token
     * @throws CredentialAuthenticationException if the authentication fails.
     */
    public MsalToken authenticateWithAzureToolkit(TokenRequestContext request) {
        AzureToolkitCacheAccessor cacheAccessor = new AzureToolkitCacheAccessor();
        // Look for cached credential in msal cache first.
        String cachedRefreshToken = cacheAccessor.getIntelliJCredentialsFromIdentityMsalCache();
        if (!CoreUtils.isNullOrEmpty(cachedRefreshToken)) {
            RefreshTokenParameters.RefreshTokenParametersBuilder refreshTokenParametersBuilder
                = RefreshTokenParameters.builder(new HashSet<>(request.getScopes()), cachedRefreshToken);

            if (request.getClaims() != null) {
                ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
                refreshTokenParametersBuilder.claims(claimsRequest);
            }

            try {
                return new MsalToken(
                    getClientInstance(request).getValue().acquireToken(refreshTokenParametersBuilder.build()).get());
            } catch (InterruptedException | ExecutionException e) {
                throw LOGGER.throwableAtError()
                    .log("Failed to get token using IntelliJ auth", e, CredentialAuthenticationException::new);
            }
        }

        throw LOGGER.throwableAtError()
            .log(
                "Azure Toolkit authentication not available. Please login with the Azure Toolkit for IntelliJ/Eclipse.",
                CredentialUnavailableException::new);
    }

    /**
     * Gets the client options.
     * @return the client options
     */
    public PublicClientOptions getClientOptions() {
        return this.options;
    }
}
