// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

/**
 * Fluent credential builder for instantiating a {@link ClientSecretCredential}.
 *
 * <p>The {@link ClientSecretCredential} acquires a token via service principal authentication. It is a type of
 * authentication in Azure that enables a non-interactive login to
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>, allowing an
 * application or service to authenticate itself with Azure resources.
 * A Service Principal is essentially an identity created for an application in Microsoft Entra ID that can be used to
 * authenticate with Azure resources. It's like a "user identity" for the application or service, and it provides
 * a way for the application to authenticate itself with Azure resources without needing to use a user's credentials.
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> allows users to
 * register service principals which can be used as an identity for authentication.
 * A client secret associated with the registered service principal is used as the password when authenticating the
 * service principal.
 * The {@link ClientSecretCredential} acquires an access token with a client secret for a service principal/registered
 * Microsoft Entra application. The tenantId, clientId and clientSecret of the service principal are required for this credential
 * to acquire an access token. It can be used both in Azure hosted and local development environments for
 * authentication. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/clientsecretcredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
 *
 * <p><strong>Sample: Construct a simple ClientSecretCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ClientSecretCredential},
 * using the {@link com.azure.identity.ClientSecretCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link com.azure.identity.ClientSecretCredential} .Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
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
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ClientSecretCredential},
 * using the {@link com.azure.identity.ClientSecretCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link com.azure.identity.ClientSecretCredential}. The {@code proxyOptions} can be optionally configured to target
 * a proxy. Once this credential is created, it may be passed into the builder of many of the Azure SDK for Java
 * client builders as the 'credential' parameter.</p>
 *
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
 * @see ClientSecretCredential
 */
public class ClientSecretCredentialBuilder extends AadCredentialBuilderBase<ClientSecretCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ClientSecretCredentialBuilder.class);
    private static final String CLASS_NAME = ClientSecretCredentialBuilder.class.getSimpleName();

    private String clientSecret;

    /**
     * Constructs an instance of ClientSecretCredentialBuilder.
     */
    public ClientSecretCredentialBuilder() {
        super();
    }

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the Microsoft Entra application.
     * @return An updated instance of this builder.
     */
    public ClientSecretCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Enables the shared token cache which is disabled by default. If enabled, the credential will store tokens
     * in a cache persisted to the machine, protected to the current user, which can be shared by other credentials
     * and processes.
     *
     * @return An updated instance of this builder.
     */
    ClientSecretCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    ClientSecretCredentialBuilder allowUnencryptedCache() {
        this.identityClientOptions.setAllowUnencryptedCache(true);
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
    public ClientSecretCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                                tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Creates a new {@link ClientCertificateCredential} with the current configurations.
     *
     * @return a {@link ClientSecretCredentialBuilder} with the current configurations.
     */
    public ClientSecretCredential build() {
        ValidationUtil.validate(CLASS_NAME, LOGGER, "clientId", clientId, "tenantId", tenantId,
            "clientSecret", clientSecret);

        return new ClientSecretCredential(tenantId, clientId, clientSecret, identityClientOptions);
    }
}
