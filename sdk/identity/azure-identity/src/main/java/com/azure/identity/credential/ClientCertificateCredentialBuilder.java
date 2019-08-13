// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link ClientCertificateCredential}.
 *
 * @see ClientCertificateCredential
 */
public class ClientCertificateCredentialBuilder extends AadCredentialBuilderBase<ClientCertificateCredentialBuilder> {
    private String tenantId;
    private String clientCertificate;
    private String clientCertificatePassword;

    /**
     * Sets the tenant ID of the application.
     * @param tenantId the tenant ID of the application.
     * @return the ClientCertificateCredentialBuilder itself
     */
    public ClientCertificateCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

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
     * @return a {@link ClientCertificateCredential} with the current configurations.
     */
    public ClientCertificateCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("tenantId", tenantId);
                put("clientCertificate", clientCertificate);
            }});
        return new ClientCertificateCredential(tenantId, clientId, clientCertificate, clientCertificatePassword, identityClientOptions);
    }
}
