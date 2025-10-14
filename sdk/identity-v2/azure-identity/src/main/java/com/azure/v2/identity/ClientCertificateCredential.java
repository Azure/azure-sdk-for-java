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

import java.io.ByteArrayInputStream;
import java.util.Objects;

import static com.azure.v2.identity.implementation.util.LoggingUtil.logAndThrowTokenError;

/**
 * <p>The ClientCertificateCredential acquires a token via service principal authentication. It is a type of
 * authentication in Azure that enables a non-interactive login to
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>, allowing
 * an application or service to authenticate itself with Azure resources.
 * A Service Principal is essentially an identity created for an application in Microsoft Entra ID that can be used to
 * authenticate with Azure resources. It's like a "user identity" for the application or service, and it provides
 * a way for the application to authenticate itself with Azure resources without needing to use a user's credentials.
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> allows users
 * to register service principals which can be used as an identity for authentication.
 * A client certificate associated with the registered service principal is used as the password when authenticating
 * the service principal.
 * The ClientCertificateCredential acquires an access token with a client certificate for a service principal/registered
 * Microsoft Entra application. The tenantId, clientId and clientCertificate of the service principal are required for this
 * credential to acquire an access token. It can be used both in Azure hosted and local development environments for
 * authentication. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/clientcertificatecredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
 *
 * <p>As a pre-requisite, a service principal is required to use this authentication mechanism. If you don't have a
 * service principal, refer to
 * <a href="https://aka.ms/azsdk/java/identity/serviceprincipal/create/docs">create a service principal with Azure CLI.
 * </a></p>
 *
 * <p><strong>Sample: Construct a simple ClientCertificateCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ClientCertificateCredential},
 * using the {@link ClientCertificateCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code certificate} parameters are required to create
 * {@link ClientCertificateCredential}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential clientCertificateCredential = new ClientCertificateCredentialBuilder&#40;&#41;.tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .pemCertificate&#40;&quot;&lt;PATH-TO-PEM-CERTIFICATE&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * <p><strong>Sample: Construct a ClientCertificateCredential using {@link ByteArrayInputStream}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link ClientCertificateCredential},
 * using the {@link ClientCertificateCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code certificate} parameters are required to create
 * {@link ClientSecretCredential}. The {@code certificate} in this example is configured as
 * a {@link ByteArrayInputStream}. This is helpful if the certificate is available in memory via a cert store.</p>
 *
 * <pre>
 * ByteArrayInputStream certificateStream = new ByteArrayInputStream&#40;certificateBytes&#41;;
 * TokenCredential certificateCredentialWithStream = new ClientCertificateCredentialBuilder&#40;&#41;.tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .pemCertificate&#40;certificateStream&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see com.azure.v2.identity
 * @see ClientCertificateCredentialBuilder
 */
public class ClientCertificateCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateCredential.class);

    private final ConfidentialClient confidentialClient;

    /**
     * Creates a ClientCertificateCredential with the given confidential client options.
     *
     * @param confidentialClientOptions the options to configure the confidential client
     */
    ClientCertificateCredential(ConfidentialClientOptions confidentialClientOptions) {
        Objects.requireNonNull(
            confidentialClientOptions.getCertificatePath() == null
                ? confidentialClientOptions.getCertificateBytes()
                : confidentialClientOptions.getCertificatePath(),
            "'certificate' and 'certificatePath' cannot both be null.");

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
