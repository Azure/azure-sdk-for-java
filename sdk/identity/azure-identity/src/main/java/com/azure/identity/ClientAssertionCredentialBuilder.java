// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.function.Supplier;

/**
 * Fluent credential builder for instantiating a {@link ClientAssertionCredential}.
 *
 * <p>The {@link ClientAssertionCredential} acquires a token via client assertion and service principal authentication.
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
 * The {@link ClientAssertionCredential} acquires an access token with a client client assertion for a
 * service principal/registered Microsoft Entra application. The tenantId, clientId and clientAssertion of the service principal
 * are required for this credential to acquire an access token. It can be used both in Azure hosted and local
 * development environments for authentication.</p>
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
 * @see ClientAssertionCredential
 */
public class ClientAssertionCredentialBuilder extends AadCredentialBuilderBase<ClientAssertionCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ClientAssertionCredentialBuilder.class);
    private static final String CLASS_NAME = ClientAssertionCredentialBuilder.class.getSimpleName();

    private Supplier<String> clientAssertionSupplier;

    /**
     * Constructs an instance of ClientAssertionCredentialBuilder.
     */
    public ClientAssertionCredentialBuilder() {
        super();
    }

    /**
     * Sets the supplier containing the logic to supply the client assertion when invoked.
     *
     * @param clientAssertionSupplier the supplier supplying client assertion.
     * @return An updated instance of this builder.
     */
    public ClientAssertionCredentialBuilder clientAssertion(Supplier<String> clientAssertionSupplier) {
        this.clientAssertionSupplier = clientAssertionSupplier;
        return this;
    }

    /**
     * Configures the persistent shared token cache options and enables the persistent token cache which is disabled
     * by default. If configured, the credential will store tokens in a cache persisted to the machine, protected to
     * the current user, which can be shared by other credentials and processes.
     *
     * @param tokenCachePersistenceOptions the token cache configuration options
     * @return An updated instance of this builder with the token cache options configured.
     */
    public ClientAssertionCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Creates a new {@link ClientAssertionCredential} with the current configurations.
     *
     * @return a {@link ClientAssertionCredential} with the current configurations.
     * @throws IllegalArgumentException if either of clientId, tenantId or clientAssertion is not present.
     */
    public ClientAssertionCredential build() {
        ValidationUtil.validate(CLASS_NAME, LOGGER, "clientId", clientId, "tenantId", tenantId,
            "clientAssertion", clientAssertionSupplier);

        return new ClientAssertionCredential(clientId, tenantId, clientAssertionSupplier, identityClientOptions);
    }
}
