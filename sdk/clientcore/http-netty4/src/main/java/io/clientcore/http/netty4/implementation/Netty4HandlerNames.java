// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.flush.FlushConsolidationHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Utility class containing constants for Netty 4 handler names.
 * <p>
 * These names are used to find and properly position handlers in the Netty pipeline.
 */
public final class Netty4HandlerNames {

    /**
     * Name for the {@link ProxyHandler}.
     */
    public static final String PROXY = "clientcore.proxy";

    /**
     * Name for the proxy {@link HttpClientCodec}.
     */
    public static final String PROXY_CODEC = "clientcore.proxycodec";

    /**
     * Name for the {@link SslHandler}.
     */
    public static final String SSL = "clientcore.ssl";

    /**
     * Name for the {@link Netty4SslInitializationHandler}.
     */
    public static final String SSL_INITIALIZER = "clientcore.sslinitializer";

    /**
     * Name for the {@link Netty4H2OrHttp11Handler}.
     */
    public static final String HTTP_VERSION_PICKER = "clientcore.httpversionpicker";

    /**
     * Name for the HTTP/1.1 {@link HttpClientCodec}
     */
    public static final String HTTP_1_1_CODEC = "clientcore.http11codec";

    /**
     * Name for the HTTP/2 {@link FlushConsolidationHandler}.
     */
    public static final String HTTP_2_FLUSH = "clientcore.http2flush";

    /**
     * Name for the HTTP/2 {@link Http2FrameCodec}.
     */
    public static final String HTTP_2_CODEC = "clientcore.http2codec";

    /**
     * Name for the HTTP/2 {@link Http2MultiplexHandler}.
     */
    public static final String HTTP_2_MULTIPLEX = "clientcore.http2multiplex";

    /**
     * Name for the {@link Netty4ProgressAndTimeoutHandler}.
     */
    public static final String PROGRESS_AND_TIMEOUT = "clientcore.progressandtimeout";

    /**
     * Name for the {@link ChunkedWriteHandler}.
     */
    public static final String CHUNKED_WRITER = "clientcore.chunkedwriter";

    /**
     * Name for the {@link Netty4ResponseHandler}.
     */
    public static final String RESPONSE = "clientcore.response";

    /**
     * Name for the {@link Netty4EagerConsumeChannelHandler}.
     */
    public static final String EAGER_CONSUME = "clientcore.eagerconsume";

    /**
     * Name for the {@link Netty4InitiateOneReadHandler}.
     */
    public static final String READ_ONE = "clientcore.readone";

    private Netty4HandlerNames() {
    }
}
