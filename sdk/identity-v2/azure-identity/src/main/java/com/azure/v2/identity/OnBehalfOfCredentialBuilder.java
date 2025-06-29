// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Fluent credential builder for instantiating a {@link OnBehalfOfCredential}.
 *
 * <p>On Behalf of authentication in Azure is a way for a user or application to authenticate to a service or resource
 * using credentials from another identity provider. This type of authentication is typically used when a user or
 * application wants to access a resource in Azure, but their credentials are managed by a different identity provider,
 * such as an on-premises Active Directory or a third-party identity provider.
 * To use "On Behalf of" authentication in Azure, the user must first authenticate to the identity provider using their
 * credentials. The identity provider then issues a security token that contains information about the user and their
 * permissions. This security token is then passed to Azure, which uses it to authenticate the user or application and
 * grant them access to the requested resource.
 * The OnBehalfOfCredential acquires a token with a client secret/certificate and user assertion for a Microsoft Entra application
 * on behalf of a user principal.</p>
 *
 * <p>The following code sample demonstrates the creation of a {@link OnBehalfOfCredential},
 * using the {@link OnBehalfOfCredentialBuilder} to configure it. The {@code tenantId},
 * {@code clientId} and {@code clientSecret} parameters are required to create
 * {@link OnBehalfOfCredential}. The {@code userAssertion} can be optionally specified on the
 * {@link OnBehalfOfCredentialBuilder}. Once this credential is created, it may be passed into the
 * builder of many of the Azure SDK for Java client builders as the 'credential' parameter.</p>
 *
 * <pre>
 * TokenCredential onBehalfOfCredential = new OnBehalfOfCredentialBuilder&#40;&#41;.clientId&#40;&quot;&lt;app-client-ID&gt;&quot;&#41;
 *     .clientSecret&#40;&quot;&lt;app-Client-Secret&gt;&quot;&#41;
 *     .tenantId&#40;&quot;&lt;app-tenant-ID&gt;&quot;&#41;
 *     .userAssertion&#40;&quot;&lt;user-assertion&gt;&quot;&#41;
 *     .build&#40;&#41;;
 * </pre>
 *
 * @see OnBehalfOfCredential
 */
public class OnBehalfOfCredentialBuilder extends EntraIdCredentialBuilderBase<OnBehalfOfCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(OnBehalfOfCredentialBuilder.class);
    private static final String CLASS_NAME = OnBehalfOfCredentialBuilder.class.getSimpleName();
    private final ConfidentialClientOptions confidentialClientOptions;

    /**
     * Constructs an instance of OnBehalfOfCredentialBuilder.
     */
    public OnBehalfOfCredentialBuilder() {
        super();
        confidentialClientOptions = new ConfidentialClientOptions();
    }

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the Microsoft Entra application.
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientSecret(String clientSecret) {
        this.confidentialClientOptions.setClientSecret(clientSecret);
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
    public OnBehalfOfCredentialBuilder
        tokenCachePersistenceOptions(TokenCachePersistenceOptions tokenCachePersistenceOptions) {
        this.confidentialClientOptions.setTokenCacheOptions(tokenCachePersistenceOptions);
        return this;
    }

    /**
     * Sets the path and password of the PFX certificate for authenticating to Microsoft Entra ID.
     *
     * @param certificatePath the PFX/PEM file containing the certificate
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientCertificatePath(String certificatePath) {
        this.confidentialClientOptions.setCertificatePath(certificatePath);
        return this;
    }

    /**
     * Sets the input stream holding the PFX certificate for authenticating to Microsoft Entra ID.
     *
     * @param certificate the input stream containing the PFX/PEM certificate
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientCertificate(InputStream certificate) {
        this.confidentialClientOptions
            .setCertificateBytes(IdentityUtil.convertInputStreamToByteArray(certificate, LOGGER));
        return this;
    }

    /**
     * Sets the input stream holding the PFX certificate for authenticating to Microsoft Entra ID.
     *
     * @param certificate the byte array containing the PFX/PEM certificate
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientCertificate(byte[] certificate) {
        this.confidentialClientOptions.setCertificateBytes(Arrays.copyOf(certificate, certificate.length));
        return this;
    }

    /**
     * Sets the password of the client certificate for authenticating to Microsoft Entra ID.
     *
     * @param clientCertificatePassword the password protecting the certificate
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientCertificatePassword(String clientCertificatePassword) {
        this.confidentialClientOptions.setCertificatePassword(clientCertificatePassword);
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
    public OnBehalfOfCredentialBuilder sendCertificateChain(boolean sendCertificateChain) {
        this.confidentialClientOptions.setIncludeX5c(sendCertificateChain);
        return this;
    }

    /**
     * Configure the User Assertion Scope to be used for OnBehalfOf Authentication request.
     *
     * @param userAssertion the user assertion access token to be used for On behalf Of authentication flow
     * @return An updated instance of this builder with the user assertion scope configured.
     */
    public OnBehalfOfCredentialBuilder userAssertion(String userAssertion) {
        this.confidentialClientOptions.setUserAssertion(userAssertion);
        return this;
    }

    /**
     * Sets the supplier containing the logic to supply the client assertion when invoked.
     *
     * @param clientAssertionSupplier the supplier supplying client assertion.
     * @return An updated instance of this builder.
     */
    public OnBehalfOfCredentialBuilder clientAssertion(Supplier<String> clientAssertionSupplier) {
        this.confidentialClientOptions.setClientAssertionSupplier(clientAssertionSupplier);
        return this;
    }

    /**
     * Creates a new {@link OnBehalfOfCredential} with the current configurations.
     *
     * @return a {@link OnBehalfOfCredential} with the current configurations.
     * @throws IllegalArgumentException if eiter both the client secret and certificate are configured or none of them
     * are configured.
     */
    public OnBehalfOfCredential build() {
        ValidationUtil.validate(CLASS_NAME, LOGGER, "clientId", confidentialClientOptions.getClientId(), "tenantId",
            confidentialClientOptions.getTenantId());

        String clientSecret = confidentialClientOptions.getClientSecret();
        String clientCertificatePath = confidentialClientOptions.getCertificatePath();
        Supplier<String> clientAssertionSupplier = confidentialClientOptions.getClientAssertionSupplier();

        if ((clientSecret == null && clientCertificatePath == null && clientAssertionSupplier == null)
            || (clientSecret != null && clientCertificatePath != null)
            || (clientSecret != null && clientAssertionSupplier != null)
            || (clientCertificatePath != null && clientAssertionSupplier != null)) {
            throw LOGGER.throwableAtWarning()
                .log("Exactly one of client secret, "
                    + "client certificate path, or client assertion supplier must be provided "
                    + "in OnBehalfOfCredentialBuilder.", IllegalArgumentException::new);
        }

        if (confidentialClientOptions.getCertificateBytes() != null
            && confidentialClientOptions.getCertificatePath() != null) {
            throw LOGGER.throwableAtWarning()
                .log("Both certificate input stream and "
                    + "certificate path/bytes are provided in ClientCertificateCredentialBuilder. Only one of them should "
                    + "be provided.", IllegalArgumentException::new);
        }

        return new OnBehalfOfCredential(confidentialClientOptions);
    }

    @Override
    ClientOptions getClientOptions() {
        return confidentialClientOptions;
    }
}
