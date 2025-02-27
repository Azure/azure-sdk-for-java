// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

/**
 * Options to configure the IdentityClient.
 */
public class ConfidentialClientOptions extends ClientOptionsBase {
    private String clientSecret;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public ConfidentialClientOptions() { super(); }

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


    public ConfidentialClientOptions clone() {
        ConfidentialClientOptions clone
            = (ConfidentialClientOptions) new ConfidentialClientOptions()
            .setClientSecret(clientSecret)
            .setHttpPipelineOptions(this.getHttpPipelineOptions().clone())
            .setMsalConfigurationOptions(this.getMsalConfigurationOptions().clone());
        return clone;
    }
}
