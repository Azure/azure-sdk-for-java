// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.implementation.ConfidentialClient;
import com.azure.identity.v2.implementation.ConfidentialClientBuilder;
import com.azure.identity.v2.implementation.IdentityClientOptions;
import com.azure.identity.v2.implementation.util.LoggingUtil;
import io.clientcore.core.credentials.AccessToken;
import io.clientcore.core.credentials.TokenCredential;
import io.clientcore.core.credentials.TokenRequestContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Objects;

/**
 * <p>The ClientSecretCredential acquires a token via service principal authentication. It is a type of authentication
 * in Azure that enables a non-interactive login to
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>, allowing an
 * application or service to authenticate itself with Azure resources.
 * A Service Principal is essentially an identity created for an application in Microsoft Entra ID that can be used to
 * authenticate with Azure resources. It's like a "user identity" for the application or service, and it provides
 * a way for the application to authenticate itself with Azure resources without needing to use a user's credentials.
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> allows users to
 * register service principals which can be used as an identity for authentication.
 * A client secret associated with the registered service principal is used as the password when authenticating the
 * service principal.
 * The ClientSecretCredential acquires an access token with a client secret for a service principal/registered
 * Microsoft Entra application. The tenantId, clientId and clientSecret of the service principal are required for this credential
 * to acquire an access token. It can be used both in Azure hosted and local development environments for
 * authentication. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/clientsecretcredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
 *
 * <p>As a pre-requisite, a service principal is required to use this authentication mechanism. If you don't have
 * a service principal, refer to
 * <a href="https://aka.ms/azsdk/java/identity/serviceprincipal/create/docs">create a service principal with Azure CLI.
 * </a></p>
 *
 * <p><strong>Sample: Construct a simple ClientSecretCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.v2.ClientSecretCredential},
 * using the {@link ClientSecretCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link com.azure.identity.v2.ClientSecretCredential} .Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.clientsecretcredential.construct -->
 * <pre>
 * TokenCredential clientSecretCredential = new ClientSecretCredentialBuilder&#40;&#41;.tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.construct -->
 *
 * <p><strong>Sample: Construct a ClientSecretCredential behind a proxy</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.v2.ClientSecretCredential},
 * using the {@link ClientSecretCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link com.azure.identity.v2.ClientSecretCredential}. The {@code proxyOptions} can be optionally configured to target
 * a proxy. Once this credential is created, it may be passed into the builder of many of the Azure SDK for Java
 * client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.clientsecretcredential.constructwithproxy -->
 * <pre>
 * TokenCredential secretCredential = new ClientSecretCredentialBuilder&#40;&#41;.tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientSecret&#40;clientSecret&#41;
 *     .proxyOptions&#40;new ProxyOptions&#40;Type.HTTP, new InetSocketAddress&#40;&quot;10.21.32.43&quot;, 5465&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientsecretcredential.constructwithproxy -->
 *
 * @see com.azure.identity.v2
 * @see ClientSecretCredentialBuilder
 */
public class ClientSecretCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientSecretCredential.class);

    private final ConfidentialClient confidentialClient;

    /**
     * Creates a ClientSecretCredential with the given identity client options.
     *
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param clientSecret the secret value of the Microsoft Entra application.
     * @param identityClientOptions the options for configuring the identity client
     */
    ClientSecretCredential(String tenantId, String clientId, String clientSecret,
        IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(clientSecret, "'clientSecret' cannot be null.");
        Objects.requireNonNull(identityClientOptions, "'identityClientOptions' cannot be null.");
        ConfidentialClientBuilder builder = new ConfidentialClientBuilder().tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .identityClientOptions(identityClientOptions);

        confidentialClient = builder.build();
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            AccessToken token = confidentialClient.authenticateWithConfidentialClientCache(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (Exception e) {
        }

        try {
            AccessToken token = confidentialClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, request, e);
            throw e;
        }
    }
}
