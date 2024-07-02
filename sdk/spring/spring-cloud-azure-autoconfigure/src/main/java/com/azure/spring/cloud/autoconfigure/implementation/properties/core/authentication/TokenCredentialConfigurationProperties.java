// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication;


import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

/**
 * Azure properties used for getting token credential.
 */
public class TokenCredentialConfigurationProperties implements TokenCredentialOptionsProvider.TokenCredentialOptions {

    /**
     * Client ID to use when performing service principal authentication with Azure.
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

    /**
     * Username to use when performing username/password authentication with Azure.
     */
    private String username;

    /**
     * Password to use when performing username/password authentication with Azure.
     */
    private String password;

    /**
     * Whether to enable managed identity to authenticate with Azure. If true and the client-id is set, will use the
     * client ID as user assigned managed identity client ID.
     */
    private boolean managedIdentityEnabled = false;

    @Override
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    @Override
    public String getClientCertificatePassword() {
        return clientCertificatePassword;
    }

    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isManagedIdentityEnabled() {
        return managedIdentityEnabled;
    }

    public void setManagedIdentityEnabled(boolean managedIdentityEnabled) {
        this.managedIdentityEnabled = managedIdentityEnabled;
    }

}
