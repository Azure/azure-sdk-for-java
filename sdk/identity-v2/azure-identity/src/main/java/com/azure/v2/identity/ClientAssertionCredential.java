// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.client.ConfidentialClient;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.LoggingUtil;
import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

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
 * <pre>
 * TokenCredential clientAssertionCredential = new ClientAssertionCredentialBuilder&#40;&#41;.tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .clientAssertion&#40;&#40;&#41; -&gt; &quot;&lt;Client-Assertion&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see ClientCertificateCredentialBuilder
 */
public class ClientAssertionCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientAssertionCredential.class);
    private final ConfidentialClient confidentialClient;

    /**
     * Creates an instance of ClientAssertionCredential.
     *
     * @param confidentialClientOptions the options to configure the confidential client
     */
    ClientAssertionCredential(ConfidentialClientOptions confidentialClientOptions) {
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
        } catch (RuntimeException ignored) {
        }

        try {
            AccessToken token = confidentialClient.authenticate(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (RuntimeException e) {
            throw logAndThrowTokenError(LOGGER, request, e, CoreException::from);
        }
    }
}
