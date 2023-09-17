// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.io.InputStream;

/**
 * Fluent credential builder for instantiating a {@link ClientCertificateCredential}.
 *
 * <p>The ClientCertificateCredential acquires a token via service principal authentication. It is a type of
 * authentication in Azure that enables a non-interactive login to
 * <a href="https://learn.microsoft.com/azure/active-directory/fundamentals/">Azure Active Directory (Azure AD)
 * </a>, allowing an application or service to authenticate itself with Azure resources.
 * A Service Principal is essentially an identity created for an application in Azure AD that can be used to
 * authenticate with Azure resources. It's like a "user identity" for the application or service, and it provides
 * a way for the application to authenticate itself with Azure resources without needing to use a user's credentials.
 * <a href="https://learn.microsoft.com/azure/active-directory/fundamentals/">Azure Active Directory
 * (Azure AD)</a> allows users to register service principals which can be used as an identity for authentication.
 * A client certificate associated with the registered service principal is used as the password when authenticating
 * the service principal.
 * The {@link ClientCertificateCredentialBuilder} acquires an access token with a client certificate for a service
 * principal/registered AAD application. The tenantId, clientId and clientCertificate of the service principal are
 * required for this credential to acquire an access token. It can be used both in Azure hosted and local development
 * environments for authentication. For more information refer to the
 * <a href="https://aka.ms/azsdk/java/identity/clientcertificatecredential/docs">conceptual knowledge and configuration
 * details</a>.</p>
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
 * <p><strong>Sample: Construct a ClientCertificateCredential behind a proxy</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.ClientCertificateCredential},
 * using the {@link com.azure.identity.ClientCertificateCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code certificate} parameters are required to create
 * {@link com.azure.identity.ClientSecretCredential}. The {@code proxyOptions} can be optionally configured to target
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
 * @see ClientCertificateCredential
 */
public class ClientCertificateCredentialBuilder extends AadCredentialBuilderBase<ClientCertificateCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(ClientCertificateCredentialBuilder.class);
    private static final String CLASS_NAME = ClientCertificateCredentialBuilder.class.getSimpleName();

    private String clientCertificatePath;
    private InputStream clientCertificate;
    private String clientCertificatePassword;

    /**
     * Sets the path of the PEM certificate for authenticating to AAD.
     *
     * @param certificatePath the PEM file containing the certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder pemCertificate(String certificatePath) {
        this.clientCertificatePath = certificatePath;
        return this;
    }

    /**
     * Sets the input stream holding the PEM certificate for authenticating to AAD.
     *
     * @param certificate the input stream containing the PEM certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder pemCertificate(InputStream certificate) {
        this.clientCertificate = certificate;
        return this;
    }

    /**
     * Sets the path and password of the PFX certificate for authenticating to AAD.
     *
     * @deprecated This API is deprecated and will be removed. Specify the PFX certificate via
     * {@link ClientCertificateCredentialBuilder#pfxCertificate(String)} API and client certificate password via
     * the {@link ClientCertificateCredentialBuilder#clientCertificatePassword(String)} API as applicable.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return An updated instance of this builder.
     */
    @Deprecated
    public ClientCertificateCredentialBuilder pfxCertificate(String certificatePath,
                                                             String clientCertificatePassword) {
        this.clientCertificatePath = certificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    /**
     * Sets the path of the PFX certificate for authenticating to AAD.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder pfxCertificate(String certificatePath) {
        this.clientCertificatePath = certificatePath;
        return this;
    }

    /**
     * Sets the input stream holding the PFX certificate and its password for authenticating to AAD.
     *
     * @param certificate the input stream containing the password protected PFX certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder pfxCertificate(InputStream certificate) {
        this.clientCertificate = certificate;
        return this;
    }

    /**
     * Sets the password of the client certificate for authenticating to AAD.
     *
     * @param clientCertificatePassword the password protecting the certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder clientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    ClientCertificateCredentialBuilder allowUnencryptedCache() {
        this.identityClientOptions.setAllowUnencryptedCache(true);
        return this;
    }

    /**
     * Enables the shared token cache which is disabled by default. If enabled, the credential will store tokens
     * in a cache persisted to the machine, protected to the current user, which can be shared by other credentials
     * and processes.
     *
     * @return An updated instance of this builder.
     */
    ClientCertificateCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
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
    public ClientCertificateCredentialBuilder tokenCachePersistenceOptions(TokenCachePersistenceOptions
                                                                          tokenCachePersistenceOptions) {
        this.identityClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
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
        this.identityClientOptions.setIncludeX5c(sendCertificateChain);
        return this;
    }

    /**
     * Creates a new {@link ClientCertificateCredential} with the current configurations.
     *
     * @return a {@link ClientCertificateCredential} with the current configurations.
     */
    public ClientCertificateCredential build() {
        ValidationUtil.validate(CLASS_NAME, LOGGER, "clientId", clientId, "tenantId", tenantId,
            "clientCertificate", clientCertificate == null ? clientCertificatePath : clientCertificate);

        if (clientCertificate != null && clientCertificatePath != null) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("Both certificate input stream and "
                    + "certificate path are provided in ClientCertificateCredentialBuilder. Only one of them should "
                    + "be provided."));
        }
        return new ClientCertificateCredential(tenantId, clientId, clientCertificatePath, clientCertificate,
            clientCertificatePassword, identityClientOptions);
    }
}
