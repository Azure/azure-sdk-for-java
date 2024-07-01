// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.identity.SharedTokenCacheCredential;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Fluent client builder for instantiating an {@link IdentityClient}.
 *
 * @see IdentityClient
 */
public final class IdentityClientBuilder {
    private IdentityClientOptions identityClientOptions = new IdentityClientOptions();
    private String tenantId;
    private String clientId;
    private String resourceId;
    private String clientSecret;
    private String clientAssertionPath;
    private String certificatePath;
    private byte[] certificate;
    private String certificatePassword;
    private boolean sharedTokenCacheCred;
    private Duration clientAssertionTimeout;
    private Supplier<String> clientAssertionSupplier;
    private Function<HttpPipeline, String> clientAssertionSupplierWithHttpPipeline;

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

    public IdentityClientBuilder resourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Sets the client secret for the client.
     * @param clientSecret the secret value of the Microsoft Entra application.
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
     * Sets the supplier for client assertion.
     *
     * @param clientAssertionSupplier the supplier of client assertion.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder clientAssertionSupplier(Supplier<String> clientAssertionSupplier) {
        this.clientAssertionSupplier = clientAssertionSupplier;
        return this;
    }

    public IdentityClientBuilder clientAssertionSupplierWithHttpPipeline(Function<HttpPipeline, String> clientAssertionSupplier) {
        this.clientAssertionSupplierWithHttpPipeline = clientAssertionSupplier;
        return this;
    }

    /**
     * Sets the client certificate for the client.
     *
     * @param clientAssertionPath the path to the file containing client assertion.
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder clientAssertionPath(String clientAssertionPath) {
        this.clientAssertionPath = clientAssertionPath;
        return this;
    }

    /**
     * Sets the client certificate for the client.
     *
     * @param certificate the PEM/PFX certificate
     * @return the IdentityClientBuilder itself
     */
    public IdentityClientBuilder certificate(byte[] certificate) {
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
     * Configure the time out to use re-use confidential client for. Post time out, a new instance of client is created.
     *
     * @param clientAssertionTimeout the time out to use for the client assertion configured via
     * {@link IdentityClientBuilder#clientAssertionPath(String)}.
     * @return the updated IdentityClientBuilder.
     */
    public IdentityClientBuilder clientAssertionTimeout(Duration clientAssertionTimeout) {
        this.clientAssertionTimeout = clientAssertionTimeout;
        return this;
    }

    /**
     * @return a {@link IdentityClient} with the current configurations.
     */
    public IdentityClient build() {
        return new IdentityClient(tenantId, clientId, clientSecret, certificatePath, clientAssertionPath, resourceId,
            clientAssertionSupplier, clientAssertionSupplierWithHttpPipeline, certificate, certificatePassword, sharedTokenCacheCred, clientAssertionTimeout,
            identityClientOptions);
    }

    public IdentitySyncClient buildSyncClient() {
        return new IdentitySyncClient(tenantId, clientId, clientSecret, certificatePath, clientAssertionPath, resourceId,
            clientAssertionSupplier, clientAssertionSupplierWithHttpPipeline, certificate, certificatePassword, sharedTokenCacheCred, clientAssertionTimeout,
            identityClientOptions);
    }
}
