// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;

import static io.netty.handler.codec.http2.Http2Error.PROTOCOL_ERROR;
import static io.netty.handler.codec.http2.Http2Exception.connectionError;
import static io.netty.handler.codec.http2.HttpConversionUtil.addHttp2ToHttpHeaders;
import static io.netty.handler.codec.http2.HttpConversionUtil.parseStatus;

/**
 * Implementation of {@link Http2EventAdapter} that converts HTTP/2 frames into HTTP/1.1 objects.
 * <p>
 * This is similar to {@link InboundHttp2ToHttpAdapter} but it doesn't buffer the entire response into a
 * {@link FullHttpResponse}. Rather it streams frames as they arrive, allowing for more efficient memory usage.
 */
final class Netty4StreamingHttp2Adapter extends Http2EventAdapter {
    private static final ClientLogger LOGGER = new ClientLogger(Netty4StreamingHttp2Adapter.class);

    private final Http2Connection connection;

    Netty4StreamingHttp2Adapter(Http2Connection connection) {
        this.connection = connection;
    }

    // TODO (alzimmer): This implementation is close but needs a way to control when WINDOWS_UPDATE frames are sent to
    //  prevent race conditions between switching from the initial response data handling in Netty4ResponseHandler and
    //  either eager or deferred content reading in the custom handlers.
    //  For now, while huge responses don't need to be supported yet, use InboundHttp2ToHttpAdapter to buffer the
    //  entire response into a FullHttpResponse.
    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
        throws Http2Exception {
        Http2Stream stream = connection.stream(streamId);
        if (stream == null) {
            throw LOGGER.throwableAtError()
                .addKeyValue("streamId", streamId)
                .log("Data Frame received for unknown stream", message -> connectionError(PROTOCOL_ERROR, message));
        }

        // data may be using pooled buffers (can't find a way to determine if it is pooled or not), and downstream may
        // not eagerly consume the data. Create a copy to ensure that the data is not reclaimed / corrupted before use.
        int dataReadableBytes = data.readableBytes();
        data = Unpooled.copiedBuffer(data);
        if (endOfStream) {
            ctx.fireChannelRead(new DefaultLastHttpContent(data));
        } else {
            ctx.fireChannelRead(new DefaultHttpContent(data));
        }

        // All bytes have been processed.
        return dataReadableBytes + padding;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
        boolean endOfStream) throws Http2Exception {
        onHeadersRead(ctx, streamId, headers, -1, (short) -1, false, padding, endOfStream);
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
        short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
        Http2Stream stream = connection.stream(streamId);
        if (stream == null) {
            throw LOGGER.throwableAtError()
                .addKeyValue("streamId", streamId)
                .log("Header Frame received for unknown stream", message -> connectionError(PROTOCOL_ERROR, message));
        }

        HttpHeaders httpHeaders = new WrappedHttp11Headers(new io.clientcore.core.http.models.HttpHeaders());
        addHttp2ToHttpHeaders(streamId, headers, httpHeaders, HttpVersion.HTTP_1_1, false, false);

        HttpResponse response;
        if (endOfStream) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, parseStatus(headers.status()),
                Unpooled.EMPTY_BUFFER, new WrappedHttp11Headers(new io.clientcore.core.http.models.HttpHeaders()),
                new WrappedHttp11Headers(new io.clientcore.core.http.models.HttpHeaders()));
            addHttp2ToHttpHeaders(streamId, headers, (FullHttpMessage) response, false);
        } else {
            HttpHeaders wrappedHeaders = new WrappedHttp11Headers(new io.clientcore.core.http.models.HttpHeaders());
            response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, parseStatus(headers.status()), wrappedHeaders);
            addHttp2ToHttpHeaders(streamId, headers, wrappedHeaders, HttpVersion.HTTP_1_1, false, false);
        }

        // Add special headers for stream dependency and weight.
        if (streamDependency > Http2CodecUtil.CONNECTION_STREAM_ID) {
            response.headers()
                .setInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), streamDependency);
        }
        if (weight > 0) {
            response.headers().setShort(HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), weight);
        }

        ctx.fireChannelRead(response);
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
        ctx.fireExceptionCaught(LOGGER.throwableAtError()
            .log("HTTP/2 to HTTP layer caught stream reset",
                message -> connectionError(Http2Error.valueOf(errorCode), message)));
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers,
        int padding) throws Http2Exception {
        ctx.fireExceptionCaught(new UnsupportedOperationException("Push promises are not supported."));
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
        // Propagate settings for downstream handlers to process.
        ctx.fireChannelRead(settings);
    }
}
