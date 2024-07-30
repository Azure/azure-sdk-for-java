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

import java.util.function.Supplier;

/**
 * <p>The ClientAssertionCredential acquires a token via client assertion and service principal authentication.
 * This authentication method provides a secure and scalable way for client applications to access Azure resources
 * without the need for users to provide their credentials. It is often used in scenarios where a client application
 * needs to access Azure resources on behalf of a user, such as in a multi-tier application architecture.
 * In this authentication method, the client application creates a JSON Web Token (JWT) that includes information about
 * the service principal (such as its client ID and tenant ID) and signs it using a client secret. The client then
 * sends this token to
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> as proof of its
 * identity. Microsoft Entra ID verifies the token signature and checks that the service principal has
 * the necessary permissions to access the requested Azure resource. If the token is valid and the service principal is
 * authorized, Microsoft Entra ID issues an access token that the client application can use to access the requested resource.
 * The ClientAssertionCredential acquires an access token with a client client assertion for a
 * service principal/registered Microsoft Entra application. The tenantId, clientId, and clientAssertion of the service principal
 * are required for this credential to acquire an access token. It can be used both in Azure-hosted and local
 * development environments for authentication.</p>
 *
 * <p>As a pre-requisite, a service principal is required to use this authentication mechanism. If you don't have a
 * service principal, refer to
 * <a href="https://aka.ms/azsdk/java/identity/serviceprincipal/create/docs">create a service principal with Azure CLI.
 * </a></p>
 *
 * <p><strong>Sample: Construct a simple ClientAssertionCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ClientAssertionCredential},
 * using the {@link ClientAssertionCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code certificate} parameters are required to create
 * {@link ClientAssertionCredential}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.clientassertioncredential.construct -->
 * <pre>
 * TokenCredential clientAssertionCredential = new ClientAssertionCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientAssertion&#40;&#40;&#41; -&gt; &quot;&lt;Client-Assertion&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientassertioncredential.construct -->
 *
 * <p><strong>Sample: Construct a ClientAssertionCredential behind a proxy</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ClientAssertionCredential},
 * using the {@link ClientAssertionCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientAssertion} parameters are required to create
 * {@link ClientAssertionCredential}. THe {@code proxyOptions} can be optionally configured to
 * target a proxy. Once this credential is created, it may be passed into the builder of many of the Azure SDK for Java
 * client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.clientassertioncredential.constructwithproxy -->
 * <pre>
 * TokenCredential assertionCredential = new ClientAssertionCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientAssertion&#40;&#40;&#41; -&gt; &quot;&lt;Client-Assertion&gt;&quot;&#41;
 *     .proxyOptions&#40;new ProxyOptions&#40;Type.HTTP, new InetSocketAddress&#40;&quot;10.21.32.43&quot;, 5465&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientassertioncredential.constructwithproxy -->
 *
 * @see com.azure.identity
 * @see ClientCertificateCredentialBuilder
 */
@Immutable
public class ClientAssertionCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientAssertionCredential.class);
    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;
    /**
     * Creates an instance of ClientAssertionCredential.
     *
     * @param clientId the client ID of user assigned or system assigned identity.
     * @param tenantId the tenant ID of the application
     * @param clientAssertion the supplier of the client assertion
     * @param identityClientOptions the options to configure the identity client
     */
    ClientAssertionCredential(String clientId, String tenantId, Supplier<String> clientAssertion,
                              IdentityClientOptions identityClientOptions) {
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientAssertionSupplier(clientAssertion)
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
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (Exception ignored) { }

        try {
            AccessToken token = identitySyncClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            // wrap the exception in a RuntimeException to avoid checked exception problems.
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
