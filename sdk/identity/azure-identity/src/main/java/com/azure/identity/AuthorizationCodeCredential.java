// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.annotation.Immutable;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.MsalToken;
import com.azure.identity.implementation.MsalAuthenticationAccount;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Authorization Code authentication in Azure is a type of authentication mechanism that allows users to
 * authenticate with <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * and obtain an authorization code that can be used to request an access token to access
 * Azure resources. It is a widely used authentication mechanism and is supported by a wide range of Azure services
 * and applications. It provides a secure and scalable way to authenticate users and grant them access to Azure
 * resources.
 * The AuthorizationCodeCredential authenticates a user or an application and acquires a token with the configured
 * authorization code and the redirectURL where authorization code was received.</p>
 *
 * <p><strong>Sample: Construct AuthorizationCodeCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.AuthorizationCodeCredential},
 * using the {@link com.azure.identity.AuthorizationCodeCredentialBuilder} to configure it.
 * The {@code authorizationCode}, {@code redirectUrl} and {@code clientId} are required to be configured to create
 * {@link AuthorizationCodeCredential}. Once this credential is created, it may be passed into the builder of many of
 * the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.authorizationcodecredential.construct -->
 * <pre>
 * TokenCredential authorizationCodeCredential = new AuthorizationCodeCredentialBuilder&#40;&#41;
 *     .authorizationCode&#40;&quot;&#123;authorization-code-received-at-redirectURL&#125;&quot;&#41;
 *     .redirectUrl&#40;&quot;&#123;redirectUrl-where-authorization-code-is-received&#125;&quot;&#41;
 *     .clientId&#40;&quot;&#123;clientId-of-application-being-authenticated&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.authorizationcodecredential.construct -->
 *
 * @see com.azure.identity
 * @see AuthorizationCodeCredentialBuilder
 */
@Immutable
public class AuthorizationCodeCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AuthorizationCodeCredential.class);

    private final String authCode;
    private final URI redirectUri;
    private final IdentityClient identityClient;
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private boolean isCaeEnabledRequestCached;
    private boolean isCaeDisabledRequestCached;
    private boolean isCachePopulated;
    private final boolean useConfidentialClient;

    /**
     * Creates an AuthorizationCodeCredential with the given identity client options.
     *
     * @param clientId the client ID of the application
     * @param clientSecret the client secret of the application
     * @param tenantId the tenant ID of the application
     * @param authCode the Oauth 2.0 authorization code grant
     * @param redirectUri the redirect URI used to authenticate to Microsoft Entra ID
     * @param identityClientOptions the options for configuring the identity client
     */
    AuthorizationCodeCredential(String clientId, String clientSecret, String tenantId, String authCode,
                                URI redirectUri, IdentityClientOptions identityClientOptions) {
        identityClient = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .identityClientOptions(identityClientOptions)
            .build();
        this.cachedToken = new AtomicReference<>();
        this.authCode = authCode;
        this.redirectUri = redirectUri;
        this.useConfidentialClient = !CoreUtils.isNullOrEmpty(clientSecret);
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.defer(() -> {
            isCachePopulated = isCachePopulated(request);
            if (isCachePopulated) {
                if (useConfidentialClient) {
                    return identityClient.authenticateWithConfidentialClientCache(request, cachedToken.get())
                        .map(accessToken -> (MsalToken) accessToken);
                } else {
                    return identityClient.authenticateWithPublicClientCache(request, cachedToken.get())
                        .onErrorResume(t -> Mono.empty());
                }
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(
            Mono.defer(() -> identityClient.authenticateWithAuthorizationCode(request, authCode, redirectUri)))
               .map(msalToken -> {
                   cachedToken.set(new MsalAuthenticationAccount(
                                new AuthenticationRecord(msalToken.getAuthenticationResult(),
                                        identityClient.getTenantId(), identityClient.getClientId())));
                   if (request.isCaeEnabled()) {
                       isCaeEnabledRequestCached = true;
                   } else {
                       isCaeDisabledRequestCached = true;
                   }
                   return (AccessToken) msalToken;
               })
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(),
                request, error));
    }

    private boolean isCachePopulated(TokenRequestContext request) {
        return (cachedToken.get() != null) && ((request.isCaeEnabled() && isCaeEnabledRequestCached)
            || (!request.isCaeEnabled() && isCaeDisabledRequestCached));
    }
}
