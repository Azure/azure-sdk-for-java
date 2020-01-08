// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link InteractiveBrowserCredential}.
 *
 * @see InteractiveBrowserCredential
 */
public class InteractiveBrowserCredentialBuilder extends AadCredentialBuilderBase<InteractiveBrowserCredentialBuilder> {
    private int port;
    private String clientSecret;
    private String clientCertificate;
    private String clientCertificatePassword;

    /**
     * Sets the port for the local HTTP server, for which {@code http://localhost:{port}} must be
     * registered as a valid reply URL on the application.
     *
     * @param port the port on which the credential will listen for the browser authentication result
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public InteractiveBrowserCredentialBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public InteractiveBrowserCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     *
     * @param certificatePath the PEM file containing the certificate
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public InteractiveBrowserCredentialBuilder pemCertificate(String certificatePath) {
        this.clientCertificate = certificatePath;
        return this;
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     *
     * @param certificatePath the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return the InteractiveBrowserCredentialBuilder itself
     */
    public InteractiveBrowserCredentialBuilder pfxCertificate(
            String certificatePath, String clientCertificatePassword) {
        this.clientCertificate = certificatePath;
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    /**
     * Creates a new {@link InteractiveBrowserCredential} with the current configurations.
     *
     * @return a {@link InteractiveBrowserCredential} with the current configurations.
     */
    public InteractiveBrowserCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("clientId", clientId);
                put("port", port);
            }});
        return new InteractiveBrowserCredential(clientId, clientSecret, clientCertificate,
                clientCertificatePassword, tenantId, port, identityClientOptions);
    }
}
