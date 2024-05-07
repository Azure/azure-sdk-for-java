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

import java.io.ByteArrayInputStream;
import java.util.Objects;

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
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ClientCertificateCredential},
 * using the {@link com.azure.identity.ClientCertificateCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code certificate} parameters are required to create
 * {@link com.azure.identity.ClientCertificateCredential}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.clientcertificatecredential.construct -->
 * <pre>
 * TokenCredential clientCertificateCredential = new ClientCertificateCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .pemCertificate&#40;&quot;&lt;PATH-TO-PEM-CERTIFICATE&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientcertificatecredential.construct -->
 *
 * <p><strong>Sample: Construct a ClientCertificateCredential using {@link ByteArrayInputStream}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ClientCertificateCredential},
 * using the {@link com.azure.identity.ClientCertificateCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code certificate} parameters are required to create
 * {@link com.azure.identity.ClientSecretCredential}. The {@code certificate} in this example is configured as
 * a {@link ByteArrayInputStream}. This is helpful if the certificate is available in memory via a cert store.</p>
 *
 * <!-- src_embed com.azure.identity.credential.clientcertificatecredential.constructWithStream -->
 * <pre>
 * ByteArrayInputStream certificateStream = new ByteArrayInputStream&#40;certificateBytes&#41;;
 * TokenCredential certificateCredentialWithStream = new ClientCertificateCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .pemCertificate&#40;certificateStream&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientcertificatecredential.constructWithStream -->
 *
 * <p><strong>Sample: Construct a ClientCertificateCredential behind a proxy</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ClientCertificateCredential},
 * using the {@link com.azure.identity.ClientCertificateCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code certificate} parameters are required to create
 * {@link com.azure.identity.ClientSecretCredential}. THe {@code proxyOptions} can be optionally configured to target
 * a proxy. Once this credential is created, it may be passed into the builder of many of the Azure SDK for Java
 * client builders as the 'credential' parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.clientcertificatecredential.constructwithproxy -->
 * <pre>
 * TokenCredential certificateCredential = new ClientCertificateCredentialBuilder&#40;&#41;
 *     .tenantId&#40;tenantId&#41;
 *     .clientId&#40;clientId&#41;
 *     .pfxCertificate&#40;&quot;&lt;PATH-TO-PFX-CERTIFICATE&gt;&quot;, &quot;P&#64;s$w0rd&quot;&#41;
 *     .proxyOptions&#40;new ProxyOptions&#40;Type.HTTP, new InetSocketAddress&#40;&quot;10.21.32.43&quot;, 5465&#41;&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.clientcertificatecredential.constructwithproxy -->
 *
 * @see com.azure.identity
 * @see ClientCertificateCredentialBuilder
 */
@Immutable
public class ClientCertificateCredential implements TokenCredential {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateCredential.class);

    private final IdentityClient identityClient;
    private final IdentitySyncClient identitySyncClient;

    /**
     * Creates a ClientCertificateCredential with default identity client options.
     * @param tenantId the tenant ID of the application
     * @param clientId the client ID of the application
     * @param certificatePath the PEM file or PFX file containing the certificate
     * @param certificate the PEM or PFX certificate
     * @param certificatePassword the password protecting the PFX file
     * @param identityClientOptions the options to configure the identity client
     */
    ClientCertificateCredential(String tenantId, String clientId, String certificatePath, byte[] certificate,
                                String certificatePassword, IdentityClientOptions identityClientOptions) {
        Objects.requireNonNull(certificatePath == null ? certificate : certificatePath,
                "'certificate' and 'certificatePath' cannot both be null.");
        IdentityClientBuilder builder = new IdentityClientBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .certificatePath(certificatePath)
            .certificate(certificate)
            .certificatePassword(certificatePassword)
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
            .doOnError(error -> LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(),
                request, error));
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
            AccessToken token = identitySyncClient.authenticateWithConfidentialClient(request);
            LoggingUtil.logTokenSuccess(LOGGER, request);
            return token;
        } catch (Exception e) {
            LoggingUtil.logTokenError(LOGGER, identityClient.getIdentityClientOptions(), request, e);
            throw e;
        }
    }
}
