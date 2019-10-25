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
     * @return the ClientCertificateCredentialBuilder itself
     */
    public ClientCertificateCredentialBuilder pemCertificate(String certificatePath) {
        this.clientCertificate = certificatePath;
        return this;
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return the ClientCertificateCredentialBuilder itself
     */
    public ClientCertificateCredentialBuilder pfxCertificate(String certificatePath, String clientCertificatePassword) {
        this.clientCertificate = certificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
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
