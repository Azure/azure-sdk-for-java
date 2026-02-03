// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

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
 * <p>The following code sample demonstrates the creation of a {@link ClientSecretCredential},
 * using the {@link ClientSecretCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link ClientSecretCredential} .Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder&#40;&#41;.tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see ClientSecretCredential
 */
public class ClientSecretCredentialBuilder extends EntraIdCredentialBuilderBase<ClientSecretCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ClientSecretCredentialBuilder.class);
    private final ConfidentialClientOptions confidentialClientOptions;

    /**
     * Constructs an instance of ClientSecretCredentialBuilder.
     */
    public ClientSecretCredentialBuilder() {
        super();
        confidentialClientOptions = new ConfidentialClientOptions();
    }

    @Override
    ClientOptions getClientOptions() {
        return confidentialClientOptions;
    }

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the Microsoft Entra application.
     * @return An updated instance of this builder.
     */
    public ClientSecretCredentialBuilder clientSecret(String clientSecret) {
        confidentialClientOptions.setClientSecret(clientSecret);
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
    public ClientSecretCredentialBuilder
        tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        getClientOptions().setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Creates a new {@link ClientSecretCredential} with the current configurations.
     *
     * @return a {@link ClientSecretCredentialBuilder} with the current configurations.
     */
    public ClientSecretCredential build() {
        ValidationUtil.validate(LOGGER, "clientId", getClientOptions().getClientId(), "tenantId",
            getClientOptions().getTenantId(), "clientSecret", confidentialClientOptions.getClientSecret());

        return new ClientSecretCredential(confidentialClientOptions);
    }
}
