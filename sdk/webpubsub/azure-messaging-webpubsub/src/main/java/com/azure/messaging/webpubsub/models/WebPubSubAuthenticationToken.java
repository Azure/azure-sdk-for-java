// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.webpubsub.models;

/**
 * A wrapper class for results of the {@code getAuthenticationToken} APIs on
 * {@link com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient} and
 * {@link com.azure.messaging.webpubsub.WebPubSubServiceClient}.
 */
public final class WebPubSubAuthenticationToken {
    private final String authToken;
    private final String url;

    /**
     * Creates a new instance with the given values set as immutable properties.
     *
     * @param authToken The authentication token that may be used to access the service.
     * @param url The url that may be used to access the service.
     */
    public WebPubSubAuthenticationToken(String authToken, String url) {
        this.authToken = authToken;
        this.url = url;
    }

    /**
     * Returns the authentication token that may be used to access the service.
     * @return The authentication token that may be used to access the service.
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Returns the url that may be used to access the service.
     * @return The url that may be used to access the service.
     */
    public String getUrl() {
        return url;
    }
}
