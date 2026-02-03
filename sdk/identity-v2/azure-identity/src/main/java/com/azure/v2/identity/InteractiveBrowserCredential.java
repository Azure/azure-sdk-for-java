// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.client.MsalAuthenticationAccountCache;
import com.azure.v2.identity.implementation.client.PublicClient;
import com.azure.v2.identity.implementation.models.MsalAuthenticationAccount;
import com.azure.v2.identity.implementation.models.MsalToken;
import com.azure.v2.identity.implementation.models.PublicClientOptions;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.models.AuthenticationRecord;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

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
 * <p>The following code sample demonstrates the creation of a {@link InteractiveBrowserCredential},
 * using the {@link InteractiveBrowserCredentialBuilder} to configure it. By default, the credential
 * targets a localhost redirect URL, to override that behaviour a
 * {@link InteractiveBrowserCredentialBuilder#redirectUrl(String)} can be optionally specified. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * @see com.azure.v2.identity
 * @see InteractiveBrowserCredentialBuilder
 */
public class InteractiveBrowserCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(InteractiveBrowserCredential.class);
    private final PublicClient publicClient;
    private final String authorityHost;
    private final PublicClientOptions publicClientOptions;
    private final MsalAuthenticationAccountCache cache;

    /**
     * Creates a InteractiveBrowserCredential with the given identity client options.
     *
     * @param publicClientOptions the options for configuring the public client
     */
    InteractiveBrowserCredential(PublicClientOptions publicClientOptions) {
        this.publicClient = new PublicClient(publicClientOptions);
        this.publicClientOptions = publicClientOptions;
        this.cache = new MsalAuthenticationAccountCache();
        this.authorityHost = publicClientOptions.getAuthorityHost();
        if (publicClientOptions.getAuthenticationRecord() != null) {
            cache.setCachedAccount(new MsalAuthenticationAccount(publicClientOptions.getAuthenticationRecord()));
        }
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        if (cache.isCachePopulated(request)) {
            try {
                MsalToken token = publicClient.authenticateWithPublicClientCache(request, cache.getCachedAccount());
                if (token != null) {
                    LoggingUtil.logTokenSuccess(LOGGER, request);
                    return token;
                }
            } catch (RuntimeException e) {
            }
        }
        try {
            if (!publicClientOptions.isAutomaticAuthentication()) {
                throw LOGGER.throwableAtError()
                    .log(
                        "Interactive authentication is needed to acquire token. Call Authenticate to initiate the device code authentication.",
                        message -> new AuthenticationRequiredException(message, request));
            }
            MsalToken accessToken = publicClient.authenticateWithBrowserInteraction(request);
            cache.updateCache(accessToken, publicClientOptions, request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (RuntimeException e) {
            throw logAndThrowTokenError(LOGGER, request, e, CoreException::from);
        }
    }

    /**
     * Interactively authenticates a user via the default browser. This method will always generate a challenge to the
     * user.
     *
     * @param request The details of the authentication request.
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was configured via
     * {@link InteractiveBrowserCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
     * when credential was instantiated.
     */
    public AuthenticationRecord authenticate(TokenRequestContext request) {
        MsalToken msalToken = publicClient.authenticateWithBrowserInteraction(request);
        cache.updateCache(msalToken, publicClientOptions, request);
        return cache.getCachedAccount().getAuthenticationRecord();
    }

    /**
     * Interactively authenticates a user via the default browser. This method will always generate a challenge to the
     * user.
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was enabled via
     * {@link InteractiveBrowserCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
     * when credential was instantiated.
     */
    public AuthenticationRecord authenticate() {
        String defaultScope = AzureAuthorityHosts.getDefaultScope(authorityHost);
        if (defaultScope == null) {
            throw LOGGER.throwableAtError()
                .log("Authenticating in this environment requires specifying a TokenRequestContext.",
                    CredentialUnavailableException::new);
        }
        return authenticate(new TokenRequestContext().addScopes(defaultScope));
    }
}
