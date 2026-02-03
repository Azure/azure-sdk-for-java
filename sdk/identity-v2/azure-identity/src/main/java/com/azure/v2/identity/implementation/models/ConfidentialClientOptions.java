// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.models;

import com.microsoft.aad.msal4j.UserAssertion;
import io.clientcore.core.http.pipeline.HttpPipeline;

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
     * Creates a copy of confidential client options from provided client options instance.
     *
     * @param clientOptions the confidential client options to copy.
     */
    public ConfidentialClientOptions(ConfidentialClientOptions clientOptions) {
        super(clientOptions);
        this.clientSecret = clientOptions.getClientSecret();
        this.clientAssertionFunction = clientOptions.getClientAssertionFunction();
        this.clientAssertionSupplier = clientOptions.getClientAssertionSupplier();
        this.certificatePath = clientOptions.getCertificatePath();
        this.certificateBytes = clientOptions.getCertificateBytes();
        this.certificatePassword = clientOptions.getCertificatePassword();
        this.includeX5c = clientOptions.isIncludeX5c();
        this.userAssertion = clientOptions.getUserAssertion();
    }

    /**
     * Creates a copy of confidential client options from provided client options instance.
     *
     * @param clientOptions the client options to copy.
     */
    public ConfidentialClientOptions(ClientOptions clientOptions) {
        super(clientOptions);
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

    /**
     * Sets the Client Assertion Function for Pipelines Credential flow.
     *
     * @param clientAssertionFunction the client assertion function
     * @return the updated options.
     */
    public ConfidentialClientOptions
        setClientAssertionFunction(Function<HttpPipeline, String> clientAssertionFunction) {
        this.clientAssertionFunction = clientAssertionFunction;
        return this;
    }

    /**
     * Gets the Client Assertion Function for Pipelines Credential flow.
     *
     * @return the client assertion function.
     */
    public Function<HttpPipeline, String> getClientAssertionFunction() {
        return this.clientAssertionFunction;
    }

    /**
     * Sets the client assertion supplier for client assertion auth flow.
     *
     * @param clientAssertionSupplier the client assertion supplier
     * @return the updated options
     */
    public ConfidentialClientOptions setClientAssertionSupplier(Supplier<String> clientAssertionSupplier) {
        this.clientAssertionSupplier = clientAssertionSupplier;
        return this;
    }

    /**
     * Gets the client assertion supplier.
     *
     * @return the client assertion supplier
     */
    public Supplier<String> getClientAssertionSupplier() {
        return this.clientAssertionSupplier;
    }

    /**
     * Gets the client certificate path.
     *
     * @return the client certificate path.
     */
    public String getCertificatePath() {
        return certificatePath;
    }

    /**
     * Sets the client certificate path.
     *
     * @param certificatePath the client certificate path.
     * @return the client certificate path.
     */
    public ConfidentialClientOptions setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
        return this;
    }

    /**
     * Gets the certificate bytes.
     *
     * @return the certificate bytes.
     */
    public byte[] getCertificateBytes() {
        return certificateBytes;
    }

    /**
     * Sets the certificate bytes.
     *
     * @param certificateBytes the certificate bytes
     * @return the updated options
     */
    public ConfidentialClientOptions setCertificateBytes(byte[] certificateBytes) {
        this.certificateBytes = certificateBytes;
        return this;
    }

    /**
     * Gets the certificate password.
     *
     * @return the certificate password.
     */
    public String getCertificatePassword() {
        return certificatePassword;
    }

    /**
     * Sets the certificate password.
     *
     * @param certificatePassword the certificate password
     * @return the updated options
     */
    public ConfidentialClientOptions setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
        return this;
    }

    /**
     * Checks whether x5c should be included in auth request.
     *
     * @return A boolean indicating whether x5c should be included.
     */
    public boolean isIncludeX5c() {
        return includeX5c;
    }

    /**
     * Sets whether x5c should be included.
     *
     * @param includeX5c A boolean indicating whether x5c should be included
     * @return the updated options
     */
    public ConfidentialClientOptions setIncludeX5c(boolean includeX5c) {
        this.includeX5c = includeX5c;
        return this;
    }

    /**
     * Sets the user assertion for OBO auth flow.
     *
     * @param userAssertion the user assertion
     * @return the updated options
     */
    public ConfidentialClientOptions setUserAssertion(String userAssertion) {
        this.userAssertion = new UserAssertion(userAssertion);
        return this;
    }

    /**
     * Gets the user assertion.
     *
     * @return the user assertion
     */
    public UserAssertion getUserAssertion() {
        return userAssertion;
    }
}
