// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.client.MsalAuthenticationAccountCache;
import com.azure.v2.identity.implementation.client.ConfidentialClient;
import com.azure.v2.identity.implementation.client.PublicClient;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.models.MsalToken;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

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
 * <p>The following code sample demonstrates the creation of a {@link AuthorizationCodeCredential},
 * using the {@link AuthorizationCodeCredentialBuilder} to configure it.
 * The {@code authorizationCode}, {@code redirectUrl} and {@code clientId} are required to be configured to create
 * {@link AuthorizationCodeCredential}. Once this credential is created, it may be passed into the builder of many of
 * the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential authorizationCodeCredential = new AuthorizationCodeCredentialBuilder&#40;&#41;.authorizationCode&#40;
 *         &quot;&#123;authorization-code-received-at-redirectURL&#125;&quot;&#41;
 *     .redirectUrl&#40;&quot;&#123;redirectUrl-where-authorization-code-is-received&#125;&quot;&#41;
 *     .clientId&#40;&quot;&#123;clientId-of-application-being-authenticated&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see AuthorizationCodeCredentialBuilder
 */
public class AuthorizationCodeCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(AuthorizationCodeCredential.class);

    private final ConfidentialClient confidentialClient;
    private final PublicClient publicClient;
    private final PublicClientOptions publicClientOptions;
    private final MsalAuthenticationAccountCache cache;

    /**
     * Creates an AuthorizationCodeCredential with the given identity client options.
     *
     * @param clientSecret the client secret of the application
     * @param publicClientOptions the options for configuring the identity client
     */
    AuthorizationCodeCredential(String clientSecret, PublicClientOptions publicClientOptions) {
        this.publicClient = new PublicClient(publicClientOptions);
        this.publicClientOptions = publicClientOptions;
        if (!CoreUtils.isNullOrEmpty(clientSecret)) {
            confidentialClient = new ConfidentialClient(
                (ConfidentialClientOptions) new ConfidentialClientOptions().setClientSecret(clientSecret)
                    .setClientId(publicClientOptions.getClientId())
                    .setTenantId(publicClientOptions.getTenantId()));
        } else {
            confidentialClient = null;
        }
        this.cache = new MsalAuthenticationAccountCache();
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        if (cache.isCachePopulated(request)) {
            if (confidentialClient != null) {
                return confidentialClient.authenticateWithCache(request, cache.getCachedAccount());
            } else {
                return publicClient.authenticateWithPublicClientCache(request, cache.getCachedAccount());
            }
        }

        MsalToken accessToken;

        try {
            if (confidentialClient != null) {
                accessToken = confidentialClient.authenticateWithAuthorizationCode(request,
                    publicClientOptions.getAuthCode(), publicClientOptions.getRedirectUri());
            } else {
                accessToken = publicClient.authenticateWithAuthorizationCode(request);
            }
            cache.updateCache(accessToken, publicClientOptions, request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (RuntimeException e) {
            throw logAndThrowTokenError(LOGGER, request, e, CredentialAuthenticationException::new);
        }
    }
}
