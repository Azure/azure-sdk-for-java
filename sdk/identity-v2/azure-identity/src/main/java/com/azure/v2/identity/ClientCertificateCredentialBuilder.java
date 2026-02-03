// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Fluent credential builder for instantiating a {@link ClientCertificateCredential}.
 *
 * <p>The ClientCertificateCredential acquires a token via service principal authentication. It is a type of
 * authentication in Azure that enables a non-interactive login to
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a>, allowing an
 * application or service to authenticate itself with Azure resources.
 * A Service Principal is essentially an identity created for an application in Microsoft Entra ID that can be used to
 * authenticate with Azure resources. It's like a "user identity" for the application or service, and it provides
 * a way for the application to authenticate itself with Azure resources without needing to use a user's credentials.
 * <a href="https://learn.microsoft.com/entra/fundamentals/">Microsoft Entra ID</a> allows users to
 * register service principals which can be used as an identity for authentication.
 * A client certificate associated with the registered service principal is used as the password when authenticating
 * the service principal.
 * The {@link ClientCertificateCredentialBuilder} acquires an access token with a client certificate for a service
 * principal/registered Microsoft Entra application. The tenantId, clientId and clientCertificate of the service principal are
 * required for this credential to acquire an access token. It can be used both in Azure hosted and local development
 * environments for authentication. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/clientcertificatecredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
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
 * @see ClientCertificateCredential
 */
public class ClientCertificateCredentialBuilder
    extends EntraIdCredentialBuilderBase<ClientCertificateCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateCredentialBuilder.class);
    private static final String CLASS_NAME = ClientCertificateCredentialBuilder.class.getSimpleName();
    private final ConfidentialClientOptions confidentialClientOptions;

    /**
     * Constructs an instance of ClientCertificateCredentialBuilder.
     */
    public ClientCertificateCredentialBuilder() {
        super();
        confidentialClientOptions = new ConfidentialClientOptions();
    }

    /**
     * Sets the path of the PFX certificate for authenticating to Microsoft Entra ID.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder clientCertificate(String certificatePath) {
        this.confidentialClientOptions.setCertificatePath(certificatePath);
        return this;
    }

    /**
     * Sets the input stream holding the PFX/PEM certificate for authenticating to Microsoft Entra ID.
     *
     * @param certificate the input stream containing the PFX/PEM certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder clientCertificate(InputStream certificate) {
        this.confidentialClientOptions
            .setCertificateBytes(IdentityUtil.convertInputStreamToByteArray(certificate, LOGGER));
        return this;
    }

    /**
     * Sets the input stream holding the PFX certificate for authenticating to Microsoft Entra ID.
     *
     * @param certificate the input stream containing the PFX/PEM certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder clientCertificate(byte[] certificate) {
        this.confidentialClientOptions.setCertificateBytes(Arrays.copyOf(certificate, certificate.length));
        return this;
    }

    /**
     * Sets the password of the client certificate for authenticating to Microsoft Entra ID.
     *
     * @param clientCertificatePassword the password protecting the certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder clientCertificatePassword(String clientCertificatePassword) {
        this.confidentialClientOptions.setCertificatePassword(clientCertificatePassword);
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
    public ClientCertificateCredentialBuilder
        tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        this.confidentialClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Specifies if the x5c claim (public key of the certificate) should be sent as part of the authentication request
     * and enable subject name / issuer based authentication. The default value is false.
     *
     * @param sendCertificateChain the flag to indicate if certificate chain should be sent as part of authentication
     * request.
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder sendCertificateChain(boolean sendCertificateChain) {
        this.confidentialClientOptions.setIncludeX5c(sendCertificateChain);
        return this;
    }

    /**
     * Creates a new {@link ClientCertificateCredential} with the current configurations.
     *
     * @return a {@link ClientCertificateCredential} with the current configurations.
     */
    public ClientCertificateCredential build() {
        ValidationUtil.validate(CLASS_NAME, LOGGER, "clientId", confidentialClientOptions.getClientId(), "tenantId",
            confidentialClientOptions.getTenantId(), "clientCertificate",
            (confidentialClientOptions.getCertificateBytes() == null
                || confidentialClientOptions.getCertificateBytes().length == 0)
                    ? confidentialClientOptions.getCertificatePath()
                    : confidentialClientOptions.getCertificateBytes());

        if (confidentialClientOptions.getCertificateBytes() != null
            && confidentialClientOptions.getCertificatePath() != null) {
            throw LOGGER.throwableAtWarning()
                .log("Both certificate input stream and "
                    + "certificate path/bytes are provided in ClientCertificateCredentialBuilder. Only one of them should "
                    + "be provided.", IllegalArgumentException::new);
        }
        return new ClientCertificateCredential(confidentialClientOptions);
    }

    @Override
    ClientOptions getClientOptions() {
        return confidentialClientOptions;
    }
}
