// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.credential;


/**
 * Azure properties used for getting token credential.
 */
public class TokenCredentialProperties {

    /**
     * Client id to use when performing service principal authentication with Azure.
     */
    private String clientId;

    /**
     * Client secret to use when performing service principal authentication with Azure.
     */
    private String clientSecret;

    /**
     * Path of a PEM certificate file to use when performing service principal authentication with Azure.
     */
    private String clientCertificatePath;

    /**
     * Password of the certificate file.
     */
    private String clientCertificatePassword;

    private String username;

    private String password;

    private String managedIdentityClientId;

    public TokenCredentialProperties() {

    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    public String getClientCertificatePassword() {
        return clientCertificatePassword;
    }

    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getManagedIdentityClientId() {
        return managedIdentityClientId;
    }

    public void setManagedIdentityClientId(String managedIdentityClientId) {
        this.managedIdentityClientId = managedIdentityClientId;
    }

}
