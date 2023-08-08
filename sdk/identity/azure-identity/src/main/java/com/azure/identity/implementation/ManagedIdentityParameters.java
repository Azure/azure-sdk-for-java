// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

/**
 * Wrapper Class for Managed Identity Parameters.
 */
public class ManagedIdentityParameters {
    private String identityEndpoint;
    private String identityHeader;
    private String msiEndpoint;
    private String msiSecret;
    private String identityServerThumbprint;

    /**
     * Creates an Instance of ManagedIdentityParameters.
     */
    public ManagedIdentityParameters() { }

    /**
     * Get the Identity Endpoint.
     * @return the Identity Endpoint.
     */
    public String getIdentityEndpoint() {
        return identityEndpoint;
    }

    /**
     * Set the Identity Endpoint.
     * @param identityEndpoint the Identity Endpoint.
     * @return the {@link ManagedIdentityParameters}
     */
    public ManagedIdentityParameters setIdentityEndpoint(String identityEndpoint) {
        this.identityEndpoint = identityEndpoint;
        return this;
    }

    /**
     * Get the Identity Header.
     * @return the Identity Header.
     */
    public String getIdentityHeader() {
        return identityHeader;
    }

    /**
     * Set the Identity Header.
     * @param identityHeader the Identity Header.
     * @return the Identity Header
     */
    public ManagedIdentityParameters setIdentityHeader(String identityHeader) {
        this.identityHeader = identityHeader;
        return this;
    }

    /**
     * Get the MSI Endpoint.
     * @return the MSI Endpoint.
     */
    public String getMsiEndpoint() {
        return msiEndpoint;
    }

    /**
     * Set the MSI Endpoint
     * @param msiEndpoint the MSI Endpoint
     * @return the MSI endpoint.
     */
    public ManagedIdentityParameters setMsiEndpoint(String msiEndpoint) {
        this.msiEndpoint = msiEndpoint;
        return this;
    }

    /**
     * Get the MSI Secret.
     * @return the MSI Secret.
     */
    public String getMsiSecret() {
        return msiSecret;
    }

    /**
     * Set the MSI Secret
     * @param msiSecret the MSI Secret
     * @return the MSI Secret
     */
    public ManagedIdentityParameters setMsiSecret(String msiSecret) {
        this.msiSecret = msiSecret;
        return this;
    }

    /**
     * Get the Identity Server Thumbprint
     * @return the Identity Server Thumbprint
     */
    public String getIdentityServerThumbprint() {
        return identityServerThumbprint;
    }

    /**
     * Set the Identity Server Thumbprint
     * @param identityServerThumbprint the Identity Server Thumbprint
     * @return the Identity Server Thumbprint
     */
    public ManagedIdentityParameters setIdentityServerThumbprint(String identityServerThumbprint) {
        this.identityServerThumbprint = identityServerThumbprint;
        return this;
    }
}
