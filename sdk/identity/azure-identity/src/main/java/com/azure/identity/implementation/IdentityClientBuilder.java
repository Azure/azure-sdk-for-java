// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.SharedTokenCacheCredential;

import java.io.InputStream;

/**
 * Fluent client builder for instantiating an {@link IdentityClient}.
 *
 * @see IdentityClient
 */
public final class IdentityClientBuilder {
    private IdentityClientOptions identityClientOptions;
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String certificatePath;
    private InputStream certificate;
    private String certificatePassword;
    private boolean sharedTokenCacheCred;

    /**
     * Sets the tenant ID for the client.
     * @param tenantId the tenant ID for the client.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * Sets the client ID for the client.
     * @param clientId the client ID for the client.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the client secret for the client.
     * @param clientSecret the secret value of the AAD application.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Sets the client certificate for the client.
     *
     * @param certificatePath the PEM/PFX file containing the certificate
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder certificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
        return this;
    }

    /**
     * Sets the client certificate for the client.
     *
     * @param certificate the PEM/PFX certificate
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder certificate(InputStream certificate) {
        this.certificate = certificate;
        return this;
    }

    /**
     * Sets the client certificate for the client.
     *
     * @param certificatePassword the password protecting the PFX file
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder certificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
        return this;
    }

    /**
     * Sets the options for the client.
     * @param identityClientOptions the options for the client.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder identityClientOptions(IdentityClientOptions identityClientOptions) {
        this.identityClientOptions = identityClientOptions;
        return this;
    }

    /**
     * Indicate whether the credential is {@link SharedTokenCacheCredential} or not.
     *
     * @param isSharedTokenCacheCred the shared token cache credential status.
     * @return the updated IdentityClientBuilder.
     */
    public IdentityClientBuilder sharedTokenCacheCredential(boolean isSharedTokenCacheCred) {
        this.sharedTokenCacheCred = isSharedTokenCacheCred;
        return this;
    }

    /**
     * @return a {@link IdentityClient} with the current configurations.
     */
    public IdentityClient build() {
        return new IdentityClient(tenantId, clientId, clientSecret, certificatePath, certificate,
            certificatePassword, sharedTokenCacheCred, identityClientOptions);
    }
}
