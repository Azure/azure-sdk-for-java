// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.MsalAuthenticationAccount;
import com.azure.identity.implementation.MsalToken;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An AAD credential that acquires a token for an AAD application by prompting the login in the default browser. When
 * authenticated, the oauth2 flow will notify the credential of the authentication code through the reply URL.
 *
 * <p>
 * The application to authenticate to must have delegated user login permissions and have {@code
 * http://localhost:{port}}
 * listed as a valid reply URL.
 */
@Immutable
public class InteractiveBrowserCredential implements TokenCredential {
    private final Integer port;
    private final IdentityClient identityClient;
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private final boolean automaticAuthentication;
    private final String authorityHost;
    private final String redirectUrl;
    private final String loginHint;
    private final ClientLogger logger = new ClientLogger(InteractiveBrowserCredential.class);


    /**
     * Creates a InteractiveBrowserCredential with the given identity client options and a listening port, for which
     * {@code http://localhost:{port}} must be registered as a valid reply URL on the application.
     *
     * @param clientId the client ID of the application
     * @param tenantId the tenant ID of the application
     * @param port the port on which the credential will listen for the browser authentication result
     * @param redirectUrl the redirect URL to listen on and receive security code.
     * @param automaticAuthentication indicates whether automatic authentication should be attempted or not.
     * @param identityClientOptions the options for configuring the identity client
     */
    InteractiveBrowserCredential(String clientId, String tenantId, Integer port, String redirectUrl,
                                 boolean automaticAuthentication, String loginHint,
                                 IdentityClientOptions identityClientOptions) {
        this.port = port;
        this.redirectUrl = redirectUrl;
        identityClient = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .identityClientOptions(identityClientOptions)
            .build();
        cachedToken = new AtomicReference<>();
        this.authorityHost = identityClientOptions.getAuthorityHost();
        this.automaticAuthentication = automaticAuthentication;
        this.loginHint = loginHint;
        if (identityClientOptions.getAuthenticationRecord() != null) {
            cachedToken.set(new MsalAuthenticationAccount(identityClientOptions.getAuthenticationRecord()));
        }
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            if (cachedToken.get() != null) {
                return identityClient.authenticateWithPublicClientCache(request, cachedToken.get())
                    .onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(Mono.defer(() -> {
            if (!automaticAuthentication) {
                return Mono.error(logger.logExceptionAsError(new AuthenticationRequiredException("Interactive "
                             + "authentication is needed to acquire token. Call Authenticate to initiate the device "
                             + "code authentication.", request)));
            }
            return identityClient.authenticateWithBrowserInteraction(request, port, redirectUrl, loginHint);
        })).map(this::updateCache)
            .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
            .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
    }

    /**
     * Interactively authenticates a user via the default browser.
     *
     * @param request The details of the authentication request.
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was configured via
     * {@link InteractiveBrowserCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
     * when credential was instantiated.
     */
    public Mono<AuthenticationRecord> authenticate(TokenRequestContext request) {
        return Mono.defer(() -> identityClient.authenticateWithBrowserInteraction(
                request, port, redirectUrl, loginHint))
            .map(this::updateCache)
            .map(msalToken -> cachedToken.get().getAuthenticationRecord());
    }

    /**
     * Interactively authenticates a user via the default browser.
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was enabled via
     * {@link InteractiveBrowserCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
     * when credential was instantiated.
     */
    public Mono<AuthenticationRecord> authenticate() {
        String defaultScope = AzureAuthorityHosts.getDefaultScope(authorityHost);
        if (defaultScope == null) {
            return Mono.error(logger.logExceptionAsError(new CredentialUnavailableException("Authenticating in this "
                                                    + "environment requires specifying a TokenRequestContext.")));
        }
        return authenticate(new TokenRequestContext().addScopes(defaultScope));
    }

    private AccessToken updateCache(MsalToken msalToken) {
        cachedToken.set(
                new MsalAuthenticationAccount(
                    new AuthenticationRecord(msalToken.getAuthenticationResult(),
                                identityClient.getTenantId(), identityClient.getClientId()),
                    msalToken.getAccount().getTenantProfiles()));
        return msalToken;
    }

    TokenCredentialType getCredentialType() {
        return TokenCredentialType.INTERACTIVE_BROWSER_CREDENTIAL;
    }
}
