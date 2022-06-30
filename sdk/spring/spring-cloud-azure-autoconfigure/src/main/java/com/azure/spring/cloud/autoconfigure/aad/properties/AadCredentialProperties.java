// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.properties;

/**
 * Properties used for authorize.
 */
public class AadCredentialProperties {

    /**
     * Client ID to use when performing service principal authentication with Azure.
     */
    private String clientId;

    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    private String clientSecret;

    /**
     * Path of a PFX or P12 certificate file to use when performing service principal authentication with Azure.
     */
    private String clientCertificatePath;

    /**
     * Password of the certificate file.
     */
    private String clientCertificatePassword;

    /**
     *
     * @return The client ID.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     *
     * @param clientId The client ID.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     *
     * @return The client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     *
     * @param clientSecret The client secret.
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * @return The client certificate path.
     */
    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    /**
     * @param clientCertificatePath The client certificate path.
     */
    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    /**
     * @return The client certificate password.
     */
    public String getClientCertificatePassword() {
        return clientCertificatePassword;
    }

    /**
     * @param clientCertificatePassword The client certificate password.
     */
    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }
}
