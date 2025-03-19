// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

import com.azure.identity.v2.AuthenticationRecord;
import com.azure.identity.v2.BrowserCustomizationOptions;

/**
 * Options to configure the IdentityClient.
 */
public class PublicClientOptions extends ClientOptions {
    private BrowserCustomizationOptions browserCustomizationOptions;
    private AuthenticationRecord authenticationRecord;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public PublicClientOptions() {
        super();
        this.browserCustomizationOptions = new BrowserCustomizationOptions();
    }

    /**
     * Gets the configured client secret.
     *
     * @return the browser customization options
     */
    public BrowserCustomizationOptions getBrowserCustomizationOptions() {
        return this.browserCustomizationOptions;
    }

    /**
     * Sets the browser customization options for {@link com.azure.identity.v2.InteractiveBrowserCredential} authentication.
     *
     * @param browserCustomizationOptions The browser customization options
     * @return the ConfidentialClientOptions itself.
     */
    public PublicClientOptions setBrowserCustomizationOptions(BrowserCustomizationOptions browserCustomizationOptions) {
        this.browserCustomizationOptions = browserCustomizationOptions;
        return this;
    }

    /**
     * Sets the {@link AuthenticationRecord} captured from a previous authentication.
     *
     * @param authenticationRecord The Authentication record to be configured.
     *
     * @return An updated instance of this builder with the configured authentication record.
     */
    public PublicClientOptions setAuthenticationRecord(AuthenticationRecord authenticationRecord) {
        this.authenticationRecord = authenticationRecord;
        return this;
    }

    /**
     * Get the configured {@link AuthenticationRecord}.
     *
     * @return {@link AuthenticationRecord}.
     */
    public AuthenticationRecord getAuthenticationRecord() {
        return authenticationRecord;
    }

    public PublicClientOptions clone() {
        PublicClientOptions clone = (PublicClientOptions) new PublicClientOptions()
            .setBrowserCustomizationOptions(browserCustomizationOptions)
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
