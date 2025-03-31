// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty.implementation;

import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

import static io.clientcore.core.http.models.HttpMethod.HEAD;

/**
 * A {@link ChannelInboundHandler} implementation that appropriately handles the response reading from the server based
 * on the information provided from the headers.
 * <p>
 * When used with {@code NettyHttpClient} this handler must be added to the pipeline so that the {@link HttpClientCodec}
 * is able to decode the data of the response.
 */
public final class CoreResponseHandler extends ChannelInboundHandlerAdapter {
    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Some states to think about here:
        // 1. If the msg is a FullHttpResponse the full response payload fit into a single "frame" (not an HTTP frame).
        //    In this case everything is considered "buffered".
        // 2. If the msg is an HttpResponse, inspect the Content-Type header and request method to determine handling.
        //    If the Content-Type is a streamable type (e.g. application/octet-stream) then we should use the
        //    LazyChannelReadBinaryData to read the data as needed.
        //    If the Content-Type is not a streamable type (e.g. application/json) then we should buffer the data.
        //    If the request method is HEAD then we should drain the body and ignore it.

        // How to go from this spot to an SDK Response object?

        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    private Response<BinaryData> processResponse(HttpRequest request, FullHttpResponse response) {
        HttpHeaders responseHeaders = response.headers();

        // Thoughts here:
        // 1. This should be using an HttpResponse instead of FullHttpResponse that is based on the initial line
        //    read and parsing of headers.
        // 2. From there we should modify the Channel / pipeline to:
        //    a. Defer reading the body until reading is needed (stream)
        //    b. Eagerly read the body (buffer)
        //    c. Drain the body (ignore)
        BinaryData body = BinaryData.empty();
        switch (getBodyHandling(request, responseHeaders)) {
            case IGNORE:
                if (response.content().isReadable()) {
                    response.content().release();
                }
                break;

            //            case STREAM:
            //                body = BinaryData.fromStream(response);
            //                break;

            case BUFFER:
                body = BinaryData.fromBytes(response.content().nioBuffer().array());
                break;

            default:
                body = BinaryData.fromBytes(response.content().nioBuffer().array());
                break;
        }

        return new NettyResponse(body, response.status().code(), request, response);
    }

    private BodyHandling getBodyHandling(HttpRequest request, HttpHeaders responseHeaders) {
        String contentType = responseHeaders.get(HttpHeaderNames.CONTENT_TYPE);

        if (request.getHttpMethod() == HEAD) {
            return BodyHandling.IGNORE;
        } else if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            return BodyHandling.STREAM;
        } else {
            return BodyHandling.BUFFER;
        }
    }

    private enum BodyHandling {
        IGNORE, STREAM, BUFFER
    }
}
