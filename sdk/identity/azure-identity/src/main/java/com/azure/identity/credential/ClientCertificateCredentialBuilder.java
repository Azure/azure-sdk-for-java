// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent credential builder for instantiating a {@link ClientCertificateCredentialBuilder}.
 *
 * @see ClientCertificateCredentialBuilder
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
     * @return a {@link ClientCertificateCredentialBuilder} with the current configurations.
     */
    public ClientCertificateCredential build() {
        List<String> missing = new ArrayList<>();
        if (clientId == null) {
            missing.add("clientId");
        }
        if (tenantId == null) {
            missing.add("tenantId");
        }
        if (clientCertificate == null) {
            missing.add("clientCertificate");
        }
        if (missing.size() > 0) {
            throw new IllegalArgumentException("Must provide non-null values for "
                + String.join(", ", missing) + " properties in " + this.getClass().getSimpleName());
        }
        return new ClientCertificateCredential(tenantId, clientId, clientCertificate, clientCertificatePassword, identityClientOptions);
    }
}
