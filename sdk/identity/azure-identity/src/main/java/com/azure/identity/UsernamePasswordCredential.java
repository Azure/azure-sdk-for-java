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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An AAD credential that acquires a token with a username and a password. Users with 2FA/MFA (Multi-factored auth)
 * turned on will not be able to use this credential. Please use {@link DeviceCodeCredential} or {@link
 * InteractiveBrowserCredential} instead, or create a service principal if you want to authenticate silently.
 */
@Immutable
public class UsernamePasswordCredential implements TokenCredential {
    private final String username;
    private final String password;
    private final IdentityClient identityClient;
    private final String authorityHost;
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private final ClientLogger logger = new ClientLogger(UsernamePasswordCredential.class);

    /**
     * Creates a UserCredential with the given identity client options.
     *
     * @param clientId the client ID of the application
     * @param tenantId the tenant ID of the application
     * @param username the username of the user
     * @param password the password of the user
     * @param identityClientOptions the options for configuring the identity client
     */
    UsernamePasswordCredential(String clientId, String tenantId, String username, String password,
                               IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(username, "'username' cannot be null.");
        Objects.requireNonNull(password, "'password' cannot be null.");
        this.username = username;
        this.password = password;
        identityClient =
            new IdentityClientBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .identityClientOptions(identityClientOptions)
                .build();
        cachedToken = new AtomicReference<>();
        this.authorityHost = identityClientOptions.getAuthorityHost();
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
        }).switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithUsernamePassword(request, username, password)))
            .map(this::updateCache)
            .doOnNext(token -> LoggingUtil.logTokenSuccess(logger, request))
            .doOnError(error -> LoggingUtil.logTokenError(logger, request, error));
    }

    /**
     * Authenticates the user using the specified username and password.
     *
     * @param request The details of the authentication request.
     *
     * @return The {@link AuthenticationRecord} of the authenticated account.
     */
    public Mono<AuthenticationRecord> authenticate(TokenRequestContext request) {
        return Mono.defer(() -> identityClient.authenticateWithUsernamePassword(request, username, password))
                       .map(this::updateCache)
                       .map(msalToken -> cachedToken.get().getAuthenticationRecord());
    }

    /**
     * Authenticates the user using the specified username and password.
     *
     * @return The {@link AuthenticationRecord} of the authenticated account.
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
}
