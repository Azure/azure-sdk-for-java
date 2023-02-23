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
import java.util.function.Consumer;

/**
 * <p>The device code credential interactively authenticates a user and acquires a token on devices with limited UI.
 * It works by prompting the user to visit a login URL on a browser-enabled machine when the application attempts to
 * authenticate. The user then enters the device code mentioned in the instructions along with their login credentials.
 * Upon successful authentication, the application that requested authentication gets authenticated successfully on the
 * device it's running on.
 * For more information refer to the <a href="https://aka.ms/azsdk/java/identity/devicecodecredential/docs">conceptual knowledge and configuration details</a>.</p>
 *
 * <p><strong>Required configuration:</strong></p>
 * <p>To authenticate a user through device code flow, use the following steps:</p>
 * <ol>
 *     <li>Go to Azure Active Directory in Azure portal and find your app registration.</li>
 *     <li>Navigate to the Authentication section.</li>
 *     <li>Under Suggested Redirected URIs, check the URI that ends with /common/oauth2/nativeclient.</li>
 *     <li>Under Default Client Type, select yes for Treat application as a public client.</li>
 * </ol>
 * <p>These steps will let the application authenticate, but it still won't have permission to log you into
 * Active Directory, or access resources on your behalf. To address this issue, navigate to API Permissions, and enable
 * Microsoft Graph and the resources you want to access, such as Azure Service Management, Key Vault, and so on.
 *
 * You also need to be the admin of your tenant to grant consent to your application when you log in for the first time.
 *
 * If you can't configure the device code flow option on your Active Directory, then it may require your app to
 * be multi- tenant. To make your app multi-tenant, navigate to the Authentication panel, then select Accounts in
 * any organizational directory. Then, select yes for Treat application as Public Client.</p>
 *
 * <p><strong>Sample: Construct DeviceCodeCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.devicecodecredential.construct -->
 * <pre>
 * TokenCredential deviceCodeCredential = new DeviceCodeCredentialBuilder&#40;&#41;
 *     .challengeConsumer&#40;challenge -&gt; &#123;
 *         &#47;&#47; Lets the user know about the challenge.
 *         System.out.println&#40;challenge.getMessage&#40;&#41;&#41;;
 *     &#125;&#41;.build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.devicecodecredential.construct -->
 *
 * <p>The Azure SDK client builders consume TokenCredential for Azure Active Directory (AAD) based authentication.
 * The TokenCredential instantiated above can be passed into most of the Azure SDK client builders for AAD authentication.</p>
 */
@Immutable
public class DeviceCodeCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(DeviceCodeCredential.class);

    private final Consumer<DeviceCodeInfo> challengeConsumer;
    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;
    private final AtomicReference<MsalAuthenticationAccount> cachedToken;
    private final String authorityHost;
    private final boolean automaticAuthentication;


    /**
     * Creates a DeviceCodeCredential with the given identity client options.
     *
     * @param clientId the client ID of the application
     * @param tenantId the tenant ID of the application
     * @param challengeConsumer a method allowing the user to meet the device code challenge
     * @param automaticAuthentication indicates whether automatic authentication should be attempted or not.
     * @param identityClientOptions the options for configuring the identity client
     */
    DeviceCodeCredential(String clientId, String tenantId, Consumer<DeviceCodeInfo> challengeConsumer,
                         boolean automaticAuthentication, IdentityClientOptions identityClientOptions) {
        this.challengeConsumer = challengeConsumer;
        IdentityClientBuilder builder =  new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .identityClientOptions(identityClientOptions);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();
        this.cachedToken = new AtomicReference<>();
        this.authorityHost = identityClientOptions.getAuthorityHost();
        this.automaticAuthentication = automaticAuthentication;
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
        }).switchIfEmpty(
            Mono.defer(() -> {
                if (!automaticAuthentication) {
                    return Mono.error(LOGGER.logExceptionAsError(new AuthenticationRequiredException("Interactive "
                         + "authentication is needed to acquire token. Call Authenticate to initiate the device "
                         + "code authentication.", request)));
                }
                return identityClient.authenticateWithDeviceCode(request, challengeConsumer);
            }))
            .map(this::updateCache)
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(),
                request, error));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        if (cachedToken.get() != null) {
            try {
                return identitySyncClient.authenticateWithPublicClientCache(request, cachedToken.get());
            } catch (Exception e) { }
        }
        try {
            if (!automaticAuthentication) {
                throw LOGGER.logExceptionAsError(new AuthenticationRequiredException("Interactive "
                    + "authentication is needed to acquire token. Call Authenticate to initiate the device "
                    + "code authentication.", request));
            }
            MsalToken accessToken =  identitySyncClient.authenticateWithDeviceCode(request, challengeConsumer);
            updateCache(accessToken);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return accessToken;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }



    /**
     * Authenticates a user via the device code flow.
     *
     * <p> The credential acquires a verification URL and code from the Azure Active Directory. The user must
     * browse to the URL, enter the code, and authenticate with Azure Active Directory. If the user authenticates
     * successfully, the credential receives an access token. </p>
     *
     * @param request The details of the authentication request.
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was configured via
     * {@link DeviceCodeCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
     * when credential was instantiated.
     */
    public Mono<AuthenticationRecord> authenticate(TokenRequestContext request) {
        return Mono.defer(() -> identityClient.authenticateWithDeviceCode(request, challengeConsumer))
                       .map(this::updateCache)
                       .map(msalToken -> cachedToken.get().getAuthenticationRecord());
    }

    /**
     * Authenticates a user via the device code flow.
     *
     * <p> The credential acquires a verification URL and code from the Azure Active Directory. The user must
     * browse to the URL, enter the code, and authenticate with Azure Active Directory. If the user authenticates
     * successfully, the credential receives an access token. </p>
     *
     * @return The {@link AuthenticationRecord} which can be used to silently authenticate the account
     * on future execution if persistent caching was configured via
     * {@link DeviceCodeCredentialBuilder#tokenCachePersistenceOptions(TokenCachePersistenceOptions)}
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
}
