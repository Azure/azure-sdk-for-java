// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties.authentication;


import com.azure.spring.cloud.core.provider.authentication.TokenCredentialOptionsProvider;

/**
 * Azure properties used for getting token credential.
 */
public final class TokenCredentialProperties implements TokenCredentialOptionsProvider.TokenCredentialOptions {

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
     * client id as user assigned managed identity client id.
     */
    private boolean managedIdentityEnabled;

    @Override
    public String getClientId() {
        return clientId;
    }

    /**
     * Set the client id.
     * @param clientId The client id.
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Set the client secret.
     * @param clientSecret The client secret.
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    /**
     * Set the client certificate path.
     * @param clientCertificatePath The client certificate path.
     */
    public void setClientCertificatePath(String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    @Override
    public String getClientCertificatePassword() {
        return clientCertificatePassword;
    }

    /**
     * Set the client certificate password.
     * @param clientCertificatePassword The client certificate password.
     */
    public void setClientCertificatePassword(String clientCertificatePassword) {
        this.clientCertificatePassword = clientCertificatePassword;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Set the username.
     * @param username The username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Set the password.
     * @param password The password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isManagedIdentityEnabled() {
        return managedIdentityEnabled;
    }

    /**
     * Whether to enable managed identity.
     * @param managedIdentityEnabled whether managed identity is enabled.
     */
    public void setManagedIdentityEnabled(boolean managedIdentityEnabled) {
        this.managedIdentityEnabled = managedIdentityEnabled;
    }
}
