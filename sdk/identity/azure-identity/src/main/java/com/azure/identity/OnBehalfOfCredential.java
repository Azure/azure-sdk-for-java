// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * <p>On Behalf of authentication in Azure is a way for a user or application to authenticate to a service or resource
 * using credentials from another identity provider. This type of authentication is typically used when a user or
 * application wants to access a resource in Azure, but their credentials are managed by a different identity provider,
 * such as an on-premises Active Directory or a third-party identity provider.
 * To use "On Behalf of" authentication in Azure, the user must first authenticate to the identity provider using their
 * credentials. The identity provider then issues a security token that contains information about the user and their
 * permissions. This security token is then passed to Azure, which uses it to authenticate the user or application and
 * grant them access to the requested resource.
 * The OnBehalfOfCredential acquires a token with a client secret/certificate and user assertion for a Microsoft Entra application
 * on behalf of a user principal.</p>
 *
 * <p>The following code sample demonstrates the creation of a {@link OnBehalfOfCredential},
 * using the {@link OnBehalfOfCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link com.azure.identity.OnBehalfOfCredential}. The {@code userAssertion} can be optionally specified on the
 * {@link OnBehalfOfCredentialBuilder}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.obocredential.construct -->
 * <pre>
 * TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder&#40;&#41;
 *     .clientId&#40;&quot;&lt;app-client-ID&gt;&quot;&#41;
 *     .clientSecret&#40;&quot;&lt;app-Client-Secret&gt;&quot;&#41;
 *     .tenantId&#40;&quot;&lt;app-tenant-ID&gt;&quot;&#41;
 *     .userAssertion&#40;&quot;&lt;user-assertion&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.obocredential.construct -->
 *
 * @see com.azure.identity
 * @see OnBehalfOfCredentialBuilder
 */
public class OnBehalfOfCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(OnBehalfOfCredential.class);

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates OnBehalfOfCredential with the specified Microsoft Entra application details and client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param clientSecret the secret value of the Microsoft Entra application.
     * @param certificatePath the PEM file or PFX file containing the certificate
     * @param certificatePassword the password protecting the PFX file
     * @param identityClientOptions the options for configuring the identity client
     */
    OnBehalfOfCredential(String clientId, String tenantId, String clientSecret, String certificatePath,
                                String certificatePassword, Supplier<String> clientAssertionSupplier,
                                IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .certificatePath(certificatePath)
            .clientAssertionSupplier(clientAssertionSupplier)
            .certificatePassword(certificatePassword)
            .identityClientOptions(identityClientOptions);

        identitySyncClient = builder.buildSyncClient();
        identityClient = builder.build();

    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.deferContextual(ctx -> identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithOBO(request)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(),
                request, error)));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClientCache(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (Exception e) { }

        try {
            AccessToken token = identitySyncClient.authenticateWithOBO(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }

}
