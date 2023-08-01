// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.webpubsub.models;

/**
 * A wrapper class for results of the {@code getClientAccessToken} APIs on
 * {@link com.azure.messaging.webpubsub.WebPubSubServiceAsyncClient} and
 * {@link com.azure.messaging.webpubsub.WebPubSubServiceClient}.
 */
public final class WebPubSubClientAccessToken {
    private final String authToken;
    private final String url;

    /**
     * Creates a new instance with the given values set as immutable properties.
     *
     * @param token The client access token that may be used to access the service.
     * @param url The url that may be used to access the service.
     */
    public WebPubSubClientAccessToken(String token, String url) {
        this.authToken = token;
        this.url = url;
    }

    /**
     * Returns the client access token that may be used to access the service.
     * @return The client access token that may be used to access the service.
     */
    public String getToken() {
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
