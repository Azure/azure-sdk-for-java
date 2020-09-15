// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link ClientCertificateCredential}.
 *
 * @see ClientCertificateCredential
 */
public class ClientCertificateCredentialBuilder extends AadCredentialBuilderBase<ClientCertificateCredentialBuilder> {
    private String clientCertificate;
    private String clientCertificatePassword;

    /**
     * Sets the client certificate for authenticating to AAD.
     *
     * @param certificatePath the PEM file containing the certificate
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder pemCertificate(String certificatePath) {
        ValidationUtil.validateFilePath(getClass().getSimpleName(), certificatePath, "Pem Certificate Path");
        this.clientCertificate = certificatePath;
        return this;
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder pfxCertificate(String certificatePath, String clientCertificatePassword) {
        ValidationUtil.validateFilePath(getClass().getSimpleName(), certificatePath, "Pfx Certificate Path");
        this.clientCertificate = certificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    /**
     * Allows to use an unprotected file specified by <code>cacheFileLocation()</code> instead of
     * Gnome keyring on Linux. This is restricted by default.
     *
     * @return An updated instance of this builder.
     */
    public ClientCertificateCredentialBuilder allowUnencryptedCache() {
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
    public ClientCertificateCredentialBuilder enablePersistentCache() {
        this.identityClientOptions.enablePersistentCache();
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
                put("clientCertificate", clientCertificate);
            }});
        return new ClientCertificateCredential(tenantId, clientId, clientCertificate, clientCertificatePassword,
            identityClientOptions);
    }
}
