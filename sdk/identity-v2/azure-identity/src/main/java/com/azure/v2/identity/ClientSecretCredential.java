// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.client.ConfidentialClient;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Objects;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

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
 * @see com.azure.v2.identity
 * @see ClientSecretCredentialBuilder
 */
public class ClientSecretCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientSecretCredential.class);

    private final ConfidentialClient confidentialClient;

    /**
     * Creates a ClientSecretCredential with the given identity client options.
     *
     * @param confidentialClientOptions the options for configuring the confidential client
     */
    ClientSecretCredential(ConfidentialClientOptions confidentialClientOptions) {
        Objects.requireNonNull(confidentialClientOptions, "'confidentialClientOptions' cannot be null.");
        Objects.requireNonNull(confidentialClientOptions.getClientSecret(), "'clientSecret' cannot be null.");

        confidentialClient = new ConfidentialClient(confidentialClientOptions);
    }

    @Override
    public AccessToken getToken(TokenRequestContext request) {
        try {
            AccessToken token = confidentialClient.authenticateWithCache(request);
            if (token != null) {
                LoggingUtil.logTokenSuccess(LOGGER, request);
                return token;
            }
        } catch (RuntimeException e) {
        }

        try {
            AccessToken token = confidentialClient.authenticate(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (RuntimeException e) {
            throw logAndThrowTokenError(LOGGER, request, e, CredentialAuthenticationException::new);
        }
    }
}
