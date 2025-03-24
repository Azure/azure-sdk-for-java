// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.implementation.client.PublicClient;
import com.azure.identity.v2.implementation.models.MsalAuthenticationAccount;
import com.azure.identity.v2.implementation.models.MsalToken;
import com.azure.identity.v2.implementation.models.PublicClientOptions;
import com.azure.identity.v2.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>Device code authentication is a type of authentication flow offered by
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> that
 * allows users to sign in to applications on devices that don't have a web browser or a keyboard.
 * This authentication method is particularly useful for devices such as smart TVs, gaming consoles, and
 * Internet of Things (IoT) devices that may not have the capability to enter a username and password.
 * With device code authentication, the user is presented with a device code on the device that needs to be
 * authenticated. The user then navigates to a web browser on a separate device and enters the code on the
 * Microsoft sign-in page. After the user enters the code, Microsoft Entra ID verifies it and prompts the user to sign in
 * with their credentials, such as a username and password or a multi-factor authentication (MFA) method.
 * Device code authentication can be initiated using various Microsoft Entra-supported protocols, such as OAuth 2.0 and
 * OpenID Connect, and it can be used with a wide range of Microsoft Entra-integrated applications.
 * The DeviceCodeCredential interactively authenticates a user and acquires a token on devices with limited UI.
 * It works by prompting the user to visit a login URL on a browser-enabled machine when the application attempts to
 * authenticate. The user then enters the device code mentioned in the instructions along with their login credentials.
 * Upon successful authentication, the application that requested authentication gets authenticated successfully on the
 * device it's running on. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/devicecodecredential/docs">device code authentication
 * documentation</a>.</p>
 *
 * <p><strong>Required configuration:</strong></p>
 *
 * <p>To authenticate a user through device code flow, use the following steps:</p>
 *
 * <ol>
 *     <li>Go to Microsoft Entra ID in Azure portal and find your app registration.</li>
 *     <li>Navigate to the Authentication section.</li>
 *     <li>Under Suggested Redirected URIs, check the URI that ends with /common/oauth2/nativeclient.</li>
 *     <li>Under Default Client Type, select yes for Treat application as a public client.</li>
 * </ol>
 *
 * <p>These steps will let the application authenticate, but it still won't have permission to log you into
 * Active Directory, or access resources on your behalf. To address this issue, navigate to API Permissions, and enable
 * Microsoft Graph and the resources you want to access, such as Azure Service Management, Key Vault, and so on.
 * You also need to be the admin of your tenant to grant consent to your application when you log in for the first time.
 * If you can't configure the device code flow option on your Active Directory, then it may require your app to
 * be multi- tenant. To make your app multi-tenant, navigate to the Authentication panel, then select Accounts in
 * any organizational directory. Then, select yes for Treat application as Public Client.</p>
 *
 * <p><strong>Sample: Construct DeviceCodeCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link DeviceCodeCredential},
 * using the {@link DeviceCodeCredentialBuilder} to configure it. By default, the credential
 * prints the device code challenge on the command line, to override that behaviours a {@code challengeConsumer}
 * can be optionally specified on the {@link DeviceCodeCredentialBuilder}. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.devicecodecredential.construct -->
 * <pre>
 * TokenCredential deviceCodeCredential = new DeviceCodeCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.devicecodecredential.construct -->
 *
 * @see com.azure.identity
 * @see DeviceCodeCredentialBuilder
 */
public class DeviceCodeCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(DeviceCodeCredential.class);
    private final PublicClient publicClient;
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private boolean isCaeEnabledRequestCached;
    private boolean isCaeDisabledRequestCached;
    private boolean isCachePopulated;

    private final PublicClientOptions publicClientOptions;

    /**
     * Creates a DeviceCodeCredential with the given identity client options.
     *
     * @param publicClientOptions the options for configuring the public client
     */
    DeviceCodeCredential(PublicClientOptions publicClientOptions) {
        this.publicClientOptions = publicClientOptions;
        this.publicClient = new PublicClient(publicClientOptions);
        this.cachedToken = new AtomicReference<>();
        if (publicClientOptions.getAuthenticationRecord() != null) {
            cachedToken.set(new MsalAuthenticationAccount(publicClientOptions.getAuthenticationRecord()));
        }
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        isCachePopulated = isCachePopulated(request);
        if (isCachePopulated) {
            try {
                MsalToken token = publicClient.authenticateWithPublicClientCache(request, cachedToken.get());
                if (token != null) {
                    LoggingUtil.logTokenSuccess(LOGGER, request);
                    return token;
                }
            } catch (Exception e) {
            }
        }
        try {
            if (!publicClientOptions.isAutomaticAuthentication()) {
                throw LOGGER.logThrowableAsError(new AuthenticationRequiredException("Interactive "
                    + "authentication is needed to acquire token. Call Authenticate to initiate the device "
                    + "code authentication.", request));
            }
            MsalToken accessToken
                = publicClient.authenticateWithDeviceCode(request);
            updateCache(accessToken);
            if (request.isCaeEnabled()) {
                isCaeEnabledRequestCached = true;
            } else {
                isCaeDisabledRequestCached = true;
            }
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, request, e);
            throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(e.getMessage(), e));
        }
    }

    /**
     * Authenticates a user via the device code flow.
     *
     * <p> The credential acquires a verification URL and code from the Microsoft Entra ID. The user must
     * browse to the URL, enter the code, and authenticate with Microsoft Entra ID. If the user authenticates
     * successfully, the credential receives an access token. This method will always generate a challenge to the user.</p>
     *
     * @param request The details of the authentication request.
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was configured via
     * {@link DeviceCodeCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
     * when credential was instantiated.
     */
    public AuthenticationRecord authenticate(TokenRequestContext request) {
        this.updateCache(publicClient.authenticateWithDeviceCode(request));
        return cachedToken.get().getAuthenticationRecord();
    }

    /**
     * Authenticates a user via the device code flow.
     *
     * <p> The credential acquires a verification URL and code from the Microsoft Entra ID. The user must
     * browse to the URL, enter the code, and authenticate with Microsoft Entra ID. If the user authenticates
     * successfully, the credential receives an access token. This method will always generate a challenge to the user.</p>
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was configured via
     * {@link DeviceCodeCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
     * when credential was instantiated.
     */
    public AuthenticationRecord authenticate() {
        String defaultScope = AzureAuthorityHosts.getDefaultScope(publicClientOptions.getAuthorityHost());
        if (defaultScope == null) {
            LoggingUtil.logCredentialUnavailableException(LOGGER, new CredentialUnavailableException(
                "Authenticating in this " + "environment requires specifying a TokenRequestContext."));
        }
        return authenticate(new TokenRequestContext().addScopes(defaultScope));
    }

    private AccessToken updateCache(MsalToken msalToken) {
        cachedToken.set(new MsalAuthenticationAccount(new AuthenticationRecord(msalToken.getAuthenticationResult(),
            publicClientOptions.getTenantId(), publicClientOptions.getClientId()),
            msalToken.getAccount().getTenantProfiles()));
        return msalToken;
    }

    private boolean isCachePopulated(TokenRequestContext request) {
        return (cachedToken.get() != null)
            && ((request.isCaeEnabled() && isCaeEnabledRequestCached)
                || (!request.isCaeEnabled() && isCaeDisabledRequestCached));
    }
}
