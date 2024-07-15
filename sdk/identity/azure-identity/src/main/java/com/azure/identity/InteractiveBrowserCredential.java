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

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Interactive browser authentication is a type of authentication flow offered by
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>
 * that enables users to sign in to applications and services using a web browser.
 * With interactive browser authentication, the user is directed to a sign-in webpage and is prompted to enter their
 * credentials. After the user successfully authenticates, Microsoft Entra ID issues a
 * security token that the application can use to authorize the user's access to its resources. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/interactivebrowsercredential/docs">interactive browser authentication
 * documentation</a>.</p>

 * <p><strong>Required configuration:</strong></p>
 *
 * <p>To use InteractiveBrowserCredential, you need to register an application in Microsoft Entra ID with
 * permissions to log in on behalf of a user. Follow the steps below to configure your registered application.</p>
 *
 * <ol>
 *     <li>Go to Microsoft Entra ID in Azure portal and find your app registration.</li>
 *     <li>Navigate to the Authentication section.</li>
 *     <li>Under Suggested Redirected URIs, check the URI that ends with /common/oauth2/nativeclient.</li>
 *     <li>Under Authentication->Advanced settings, enable "Allow public client flows."</li>
 * </ol>
 *
 * <p>These steps will let the application authenticate, but it still won't have permission to log you into
 * Active Directory, or access resources on your behalf. To address this issue, navigate to API Permissions, and enable
 * Microsoft Graph and the resources you want to access, such as Azure Service Management, Key Vault, and so on.
 * You also need to be the admin of your tenant to grant consent to your application when you log in for the first time.
 * In {@link InteractiveBrowserCredentialBuilder#redirectUrl(String)}, a redirect URL can be specified. It configures
 * the Redirect URL where STS will callback the application with the security code. It is required if a custom
 * client id is specified via {@link InteractiveBrowserCredentialBuilder#clientId(String)} and must match the
 * redirect URL specified during the application registration. You can add the redirect URL to the Redirect URIs
 * subsection under the Authentication section of your registered Microsoft Entra application.</p>
 *
 * <p><strong>Sample: Construct InteractiveBrowserCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.InteractiveBrowserCredential},
 * using the {@link com.azure.identity.InteractiveBrowserCredentialBuilder} to configure it. By default, the credential
 * targets a localhost redirect URL, to override that behaviour a
 * {@link InteractiveBrowserCredentialBuilder#redirectUrl(String)} can be optionally specified. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.interactivebrowsercredential.construct -->
 * <pre>
 * TokenCredential interactiveBrowserCredential = new InteractiveBrowserCredentialBuilder&#40;&#41;
 *     .redirectUrl&#40;&quot;http:&#47;&#47;localhost:8765&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.interactivebrowsercredential.construct -->
 *
 * @see com.azure.identity
 * @see InteractiveBrowserCredentialBuilder
 */
@Immutable
public class InteractiveBrowserCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(InteractiveBrowserCredential.class);

    private final Integer port;
    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private final boolean automaticAuthentication;
    private final String authorityHost;
    private final String redirectUrl;
    private final String loginHint;
    private boolean isCaeEnabledRequestCached;
    private boolean isCaeDisabledRequestCached;
    private boolean isCachePopulated;


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
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .identityClientOptions(identityClientOptions);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();

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
            isCachePopulated = isCachePopulated(request);
            if (isCachePopulated) {
                return identityClient.authenticateWithPublicClientCache(request, cachedToken.get())
                    .onErrorResume(t -> Mono.empty());
            } else {
                return Mono.empty();
            }
        }).switchIfEmpty(Mono.defer(() -> {
            if (!automaticAuthentication) {
                return Mono.error(LOGGER.logExceptionAsError(new AuthenticationRequiredException("Interactive "
                             + "authentication is needed to acquire token. Call Authenticate to initiate the device "
                             + "code authentication.", request)));
            }
            return identityClient.authenticateWithBrowserInteraction(request, port, redirectUrl, loginHint);
        })).map(msalToken -> {
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
            if (!automaticAuthentication) {
                throw LOGGER.logExceptionAsError(new AuthenticationRequiredException("Interactive "
                    + "authentication is needed to acquire token. Call Authenticate to initiate the device "
                    + "code authentication.", request));
            }
            MsalToken accessToken =  identitySyncClient.authenticateWithBrowserInteraction(request, port, redirectUrl, loginHint);
            updateCache(accessToken);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
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
