// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.netty.channel.Channel;

import java.io.ByteArrayOutputStream;

/**
 * Holder of information gathered by {@link Netty4ResponseHandler}. Used in {@code NettyHttpClient} to create the final
 * {@link Response}.
 */
public final class ResponseStateInfo {
    private final Channel responseChannel;
    private final boolean channelConsumptionComplete;
    private final int statusCode;
    private final HttpHeaders headers;
    private final ByteArrayOutputStream eagerContent;
    private final ResponseBodyHandling responseBodyHandling;
    private final boolean isHttp2;

    ResponseStateInfo(Channel responseChannel, boolean channelConsumptionComplete, int statusCode, HttpHeaders headers,
        ByteArrayOutputStream eagerContent, ResponseBodyHandling responseBodyHandling, boolean isHttp2) {
        this.responseChannel = responseChannel;
        this.channelConsumptionComplete = channelConsumptionComplete;
        this.statusCode = statusCode;
        this.headers = headers;
        this.eagerContent = eagerContent;
        this.responseBodyHandling = responseBodyHandling;
        this.isHttp2 = isHttp2;
    }

    /**
     * Gets the Netty {@link Channel} that holds the connection to the response.
     *
     * @return The Netty {@link Channel} that holds the connection to the response.
     */
    public Channel getResponseChannel() {
        return responseChannel;
    }

    /**
     * Flag indicating whether the channel consumption is complete.
     *
     * @return Whether the channel consumption is complete.
     */
    public boolean isChannelConsumptionComplete() {
        return channelConsumptionComplete;
    }

    /**
     * Gets the HTTP status code of the response.
     *
     * @return The HTTP status code of the response.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the HTTP headers of the response.
     *
     * @return The HTTP headers of the response.
     */
    public HttpHeaders getHeaders() {
        return headers;
    }

    /**
     * Gets the content that was eagerly read from the Netty pipeline when processing the initial status line and
     * headers.
     *
     * @return The content that was eagerly read from the Netty pipeline.
     */
    public ByteArrayOutputStream getEagerContent() {
        return eagerContent;
    }

    /**
     * Gets the response body handling strategy.
     *
     * @return The response body handling strategy.
     */
    public ResponseBodyHandling getResponseBodyHandling() {
        return responseBodyHandling;
    }

    /**
     * Flag indicating whether the connection is using HTTP/2 or not.
     *
     * @return Whether the connection is using HTTP/2.
     */
    public boolean isHttp2() {
        return isHttp2;
    }
}
