// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

/**
 * Options to configure the IdentityClient.
 */
public class PublicClientOptions extends ClientOptionsBase {
    private String clientSecret;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public PublicClientOptions() { super(); }

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
    public PublicClientOptions setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }


    public PublicClientOptions clone() {
        PublicClientOptions clone
            = (PublicClientOptions) new PublicClientOptions()
            .setClientSecret(clientSecret)
            .setHttpPipelineOptions(this.getHttpPipelineOptions().clone())
            .setMsalCommonOptions(this.getMsalCommonOptions().clone());
        return clone;
    }
}
