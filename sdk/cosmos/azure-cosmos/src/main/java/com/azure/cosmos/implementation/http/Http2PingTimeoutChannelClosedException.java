// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.http;

import java.nio.channels.ClosedChannelException;

/**
 * Marker exception raised when {@link Http2PingHandler} closes an HTTP/2 parent
 * channel after consecutive PING ACK timeouts (or PING send failures) crossed
 * the configured threshold.
 * <p>
 * This is a <b>local transport</b> failure -- typically caused by NAT / load
 * balancer idle reaping of an otherwise-healthy connection. The remote
 * service (e.g. Cosmos DB Standard Gateway or ThinClient proxy) is NOT
 * known to be unhealthy. Code paths that classify network failures (e.g.
 * {@code com.azure.cosmos.implementation.ClientRetryPolicy}) should use this
 * marker to suppress region mark-down / cross-region failover and instead
 * retry against the same regional endpoint with a fresh connection.
 * <p>
 * Extends {@link ClosedChannelException} so existing classifiers (notably
 * {@code WebExceptionUtility.isNetworkFailure}) continue to recognize it as a
 * network failure; discrimination from a generic closed channel is via {@code
 * instanceof} (see {@code WebExceptionUtility.isHttp2PingTimeoutClose}).
 * <p>
 * Note: {@link ClosedChannelException} only exposes a no-arg constructor, so
 * the message is carried in a private field and surfaced via {@link
 * #getMessage()}.
 */
public final class Http2PingTimeoutChannelClosedException extends ClosedChannelException {

    private static final long serialVersionUID = 1L;

    private final String message;

    public Http2PingTimeoutChannelClosedException(String message, Throwable cause) {
        super();
        this.message = message;
        if (cause != null) {
            initCause(cause);
        }
    }

    @Override
    public String getMessage() {
        return message;
    }
}
