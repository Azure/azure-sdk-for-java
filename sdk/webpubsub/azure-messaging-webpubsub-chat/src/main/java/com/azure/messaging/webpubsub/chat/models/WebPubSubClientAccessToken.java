// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat.models;

/** A client access token and URL for connecting to Azure Web PubSub. */
public final class WebPubSubClientAccessToken {
    private final String token;
    private final String url;

    /**
     * Creates a Web PubSub client access token result.
     *
     * @param token The client access token.
     * @param url The client connection URL.
     */
    public WebPubSubClientAccessToken(String token, String url) {
        this.token = token;
        this.url = url;
    }

    /**
     * Gets the client access token.
     *
     * @return The client access token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the client connection URL.
     *
     * @return The client connection URL.
     */
    public String getUrl() {
        return url;
    }
}
