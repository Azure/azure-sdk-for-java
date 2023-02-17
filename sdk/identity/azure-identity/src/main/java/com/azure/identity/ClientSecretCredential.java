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
import com.azure.identity.implementation.util.LoggingUtil;
import reactor.core.publisher.Mono;

import java.util.Objects;


/**
 * <p>The ClientSecretCredential acquires an access token with a client secret for a service principal/registered AAD application.
 * The tenantId, clientId and clientSecret of the service principal are required for this credential to acquire an access token.
 * It can be used both in Azure hosted and local development environments for authentication.
 * For more information refer to the <a href="https://aka.ms/azsdk/java/identity/clientsecretcredential/docs"> conceptual knowledge and configuration details </a>.</p>
 *
 * <p>As a pre-requisite, a service principal is required to use this authentication mechanism. If you don't have a service principal,
 * refer to <a href="https://aka.ms/azsdk/java/identity/serviceprincipal/create/docs">create a service principal with Azure CLI.</a></p>
 *
 * <p><strong>Sample: Construct a simple ClientSecretCredential</strong></p>
 * <!-- src_embed com.azure.identity.credential.clientsecretcredential.construct -->
 * <pre>
 * TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.construct -->
 *
 * <p><strong>Sample: Construct a ClientSecretCredential behind a proxy</strong></p>
 * <!-- src_embed com.azure.identity.credential.clientsecretcredential.constructwithproxy -->
 * <pre>
 * TokenCredential secretCredential = new ClientSecretCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .proxyOptions&#40;new ProxyOptions&#40;Type.HTTP, new InetSocketAddress&#40;&quot;10.21.32.43&quot;, 5465&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.constructwithproxy -->
 *
 * <p>The Azure SDK client builders consume TokenCredential for Azure Active Directory (AAD) based authentication. The TokenCredential instantiated
 * above can be passed into most of the Azure SDK client builders to for AAD authentication.</p>
 */
@Immutable
public class ClientSecretCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientSecretCredential.class);

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates a ClientSecretCredential with the given identity client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param clientSecret the secret value of the AAD application.
     * @param identityClientOptions the options for configuring the identity client
     */
    ClientSecretCredential(String tenantId, String clientId, String clientSecret,
                           IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(clientSecret, "'clientSecret' cannot be null.");
        Objects.requireNonNull(identityClientOptions, "'identityClientOptions' cannot be null.");
        IdentityClientBuilder builder =  new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .identityClientOptions(identityClientOptions);

        identityClient = builder.build();
        identitySyncClient = builder.buildSyncClient();
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return identityClient.authenticateWithConfidentialClientCache(request)
            .onErrorResume(t -> Mono.empty())
            .switchIfEmpty(Mono.defer(() -> identityClient.authenticateWithConfidentialClient(request)))
            .doOnNext(token -> LoggingUtil.logTokenSuccess(LOGGER, request))
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request,
                error));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClientCache(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) { }

        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }
}
