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
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.implementation.MsalAuthenticationAccount;
import com.azure.identity.implementation.MsalToken;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Username password authentication is a common type of authentication flow used by many applications and services,
 * including <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>.
 * With username password authentication, users enter their username and password credentials to sign
 * in to an application or service.
 * The UsernamePasswordCredential authenticates a public client application and acquires a token using the
 * user credentials that don't require 2FA/MFA (Multi-factored) authentication. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/usernamepasswordcredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
 *
 * <p>In the scenario where 2FA/MFA (Multi-factored) authentication is turned on, please use
 * {@link DeviceCodeCredential} or {@link InteractiveBrowserCredential} instead.</p>
 *
 * <p><strong>Sample: Construct UsernamePasswordCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link UsernamePasswordCredential},
 * using the {@link UsernamePasswordCredentialBuilder} to configure it. The {@code clientId},
 * {@code username} and {@code password} parameters are required to create
 * {@link UsernamePasswordCredential}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.usernamepasswordcredential.construct -->
 * <pre>
 * TokenCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder&#40;&#41;
 *     .clientId&#40;&quot;&lt;your app client ID&gt;&quot;&#41;
 *     .username&#40;&quot;&lt;your username&gt;&quot;&#41;
 *     .password&#40;&quot;&lt;your password&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.usernamepasswordcredential.construct -->
 *
 * @see com.azure.identity
 * @see UsernamePasswordCredentialBuilder
 * @see DeviceCodeCredential
 * @see InteractiveBrowserCredential
 */
@Immutable
public class UsernamePasswordCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(UsernamePasswordCredential.class);

    private final String username;
    private final String password;
    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;
    private final String authorityHost;
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private boolean isCaeEnabledRequestCached;
    private boolean isCaeDisabledRequestCached;
    private boolean isCachePopulated;

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
        IdentityClientBuilder builder =
            new IdentityClientBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .identityClientOptions(identityClientOptions);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();

        cachedToken = new AtomicReference<>();
        this.authorityHost = identityClientOptions.getAuthorityHost();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            isCachePopulated = isCachePopulated(request);
            if (isCachePopulated) {
                return identityClient.authenticateWithPublicClientCache(request, cachedToken.get())
                    .onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithUsernamePassword(request, username, password)))
            .map(msalToken -> {
                AccessToken accessToken = updateCache(msalToken);
                if (request.isCaeEnabled()) {
                    isCaeEnabledRequestCached = true;
                } else {
                    isCaeDisabledRequestCached = true;
                }
                return accessToken;
            })
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(),
                request, error));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        if (cachedToken.get() != null) {
            try {
                MsalToken token = identitySyncClient.authenticateWithPublicClientCache(request, cachedToken.get());
                if (token != null) {
                    LoggingUtil.logTokenSuccess(LOGGER, request);
                    return token;
                }
            } catch (Exception e) { }
        }

        try {
            MsalToken accessToken = identitySyncClient.authenticateWithUsernamePassword(request, username, password);
            updateCache(accessToken);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
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
            return Mono.error(LoggingUtil.logCredentialUnavailableException(LOGGER,
                identityClient.getIdentityClientOptions(), new CredentialUnavailableException("Authenticating in this "
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

    private boolean isCachePopulated(TokenRequestContext request) {
        return (cachedToken.get() != null) && ((request.isCaeEnabled() && isCaeEnabledRequestCached)
            || (!request.isCaeEnabled() && isCaeDisabledRequestCached));
    }
}
