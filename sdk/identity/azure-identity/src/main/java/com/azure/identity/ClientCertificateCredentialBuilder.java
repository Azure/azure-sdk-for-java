// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link ClientCertificateCredential}.
 *
 * @see ClientCertificateCredential
 */
public class ClientCertificateCredentialBuilder extends AadCredentialBuilderBase<ClientCertificateCredentialBuilder> {
    private String clientCertificatePath;
    private InputStream clientCertificate;
    private String clientCertificatePassword;
    private final ClientLogger logger = new ClientLogger(ClientCertificateCredentialBuilder.class);

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
    ClientCertificateCredentialBuilder pemCertificate(InputStream certificate) {
        this.clientCertificate = certificate;
        return this;
    }

    /**
     * Sets the path and password of the PFX certificate for authenticating to AAD.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder pfxCertificate(String certificatePath,
                                                             String clientCertificatePassword) {
        this.clientCertificatePath = certificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    /**
     * Sets the input stream holding the PFX certificate and its password for authenticating to AAD.
     *
     * @param certificate the input stream containing the password protected PFX certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return An updated instance of this builder.
     */
    ClientCertificateCredentialBuilder pfxCertificate(InputStream certificate,
                                                             String clientCertificatePassword) {
        this.clientCertificate = certificate;
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
     * Specifies either the specific regional authority, or use {@link RegionalAuthority#AUTO_DISCOVER_REGION} to
     * attempt to auto-detect the region. If unset, a non-regional authority will be used. This argument should be used
     * only by applications deployed to Azure VMs.
     *
     * @param regionalAuthority the regional authority
     * @return An updated instance of this builder with the regional authority configured.
     */
    public ClientCertificateCredentialBuilder regionalAuthority(RegionalAuthority regionalAuthority) {
        this.identityClientOptions.setRegionalAuthority(regionalAuthority);
        return this;
    }

    /**
     * Creates a new {@link ClientCertificateCredential} with the current configurations.
     *
     * @return a {@link ClientCertificateCredential} with the current configurations.
     */
    public ClientCertificateCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("tenantId", tenantId);
                put("clientCertificate", clientCertificate == null ? clientCertificatePath : clientCertificate);
            }});
        if (clientCertificate != null && clientCertificatePath != null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Both certificate input stream and "
                    + "certificate path are provided in ClientCertificateCredentialBuilder. Only one of them should "
                    + "be provided."));
        }
        return new ClientCertificateCredential(tenantId, clientId, clientCertificatePath, clientCertificate,
            clientCertificatePassword, identityClientOptions);
    }
}
