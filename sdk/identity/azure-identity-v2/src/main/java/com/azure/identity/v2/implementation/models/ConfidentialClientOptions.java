// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import com.microsoft.aad.msal4j.UserAssertion;
import io.clientcore.core.http.pipeline.HttpPipeline;

import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents Confidential Client Options used in Confidential Client OAuth Flow .
 */
public class ConfidentialClientOptions extends ClientOptions {
    private String clientSecret;
    private Function<HttpPipeline, String> clientAssertionFunction;
    private Supplier<String> clientAssertionSupplier;
    private String certificatePath;
    private byte[] certificateBytes;
    private String certificatePassword;
    private boolean includeX5c;
    private UserAssertion userAssertion;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public ConfidentialClientOptions() {
        super();
    }

    /**
     * Gets the configured client secret.
     * @return the client secret
     */
    public String getClientSecret() {
        return this.clientSecret;
    }

    /**
     * Sets the client secret
     * @param clientSecret The client secret
     * @return the ConfidentialClientOptions itself.
     */
    public ConfidentialClientOptions setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public ConfidentialClientOptions
        setClientAssertionFunction(Function<HttpPipeline, String> clientAssertionFunction) {
        this.clientAssertionFunction = clientAssertionFunction;
        return this;
    }

    public Function<HttpPipeline, String> getClientAssertionFunction() {
        return this.clientAssertionFunction;
    }

    public ConfidentialClientOptions setClientAssertionSupplier(Supplier<String> clientAssertionSupplier) {
        this.clientAssertionSupplier = clientAssertionSupplier;
        return this;
    }

    public Supplier<String> getClientAssertionSupplier() {
        return this.clientAssertionSupplier;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public byte[] getCertificateBytes() {
        return certificateBytes;
    }

    public void setCertificateBytes(byte[] certificateBytes) {
        this.certificateBytes = certificateBytes;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public boolean isIncludeX5c() {
        return includeX5c;
    }

    public void setIncludeX5c(boolean includeX5c) {
        this.includeX5c = includeX5c;
    }

    public ConfidentialClientOptions setUserAssertion(String userAssertion) {
        this.userAssertion = new UserAssertion(userAssertion);
        return this;
    }

    public UserAssertion getUserAssertion() {
        return userAssertion;
    }

    @Override
    public ConfidentialClientOptions clone() {
        ConfidentialClientOptions clone
            = (ConfidentialClientOptions) new ConfidentialClientOptions().setClientSecret(this.clientSecret)
                .setClientAssertionFunction(this.clientAssertionFunction)
                .setClientAssertionSupplier(this.clientAssertionSupplier)
                .setClientId(this.getClientId())
                .setTenantId(this.getTenantId())
                .setHttpPipelineOptions(this.getHttpPipelineOptions().clone())
                .setExecutorService(this.getExecutorService())
                .setAuthorityHost(this.getAuthorityHost())
                .setAdditionallyAllowedTenants(this.getAdditionallyAllowedTenants())
                .setTokenCacheOptions(this.getTokenCacheOptions());
        return clone;
    }
}
