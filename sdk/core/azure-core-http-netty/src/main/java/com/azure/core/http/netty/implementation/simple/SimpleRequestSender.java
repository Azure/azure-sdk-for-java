// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.util.BinaryDataContent;
import com.azure.core.implementation.util.BinaryDataHelper;
import com.azure.core.implementation.util.ByteArrayContent;
import com.azure.core.implementation.util.FileContent;
import com.azure.core.implementation.util.InputStreamContent;
import com.azure.core.implementation.util.StringContent;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.azure.core.http.netty.implementation.simple.SimpleNettyConstants.REQUEST_CONTEXT_KEY;

public class SimpleRequestSender implements FutureListener<Channel> {

    private static final ClientLogger LOGGER = new ClientLogger(SimpleRequestSender.class);

    private final SimpleRequestContext requestContext;

    public SimpleRequestSender(SimpleRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public void operationComplete(Future<Channel> future) throws Exception {
        if (future.isSuccess()) {
            Channel ch = future.getNow();
            try {
                ch.attr(REQUEST_CONTEXT_KEY).set(requestContext);

                HttpRequest request = requestContext.getRequest();

                BinaryDataContent binaryDataContent = null;
                if (request.getContent() != null) {
                    binaryDataContent = BinaryDataHelper.getContent(request.getContent());
                }

                if (binaryDataContent != null) {
                    if (binaryDataContent instanceof ByteArrayContent
                        || binaryDataContent instanceof StringContent) {
                        sendBufferedRequest(request, binaryDataContent, ch);
                    } else if (binaryDataContent instanceof FileContent) {
                        sendFileRequest(request, (FileContent) binaryDataContent, ch);
                    } else if (binaryDataContent instanceof InputStreamContent) {
                        sendStreamRequest(request, (InputStreamContent) binaryDataContent, ch);
                    } else {
                        sendFluxRequest(request, binaryDataContent, ch);
                    }
                } else {
                    sendRequestWithoutBody(request, ch);
                }
            } catch (RuntimeException e) {
                ch.close();
                requestContext.getChannelPool().release(ch);
                requestContext.getResponseFuture().completeExceptionally(e);
            }
        } else {
            requestContext.getResponseFuture().completeExceptionally(future.cause());
        }
    }

    private static void sendRequestWithoutBody(HttpRequest request, Channel channel) {
        HttpMethod method = mapHttpMethod(request.getHttpMethod());

        io.netty.handler.codec.http.HttpRequest nettyRequest = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, method, request.getUrl().toString());
        setHeaders(nettyRequest, request, null, false);

        channel.writeAndFlush(nettyRequest);
    }

    private static void sendBufferedRequest(HttpRequest request, BinaryDataContent binaryDataContent, Channel channel) {
        byte[] requestBytes = binaryDataContent.toBytes();
        HttpMethod method = mapHttpMethod(request.getHttpMethod());

        io.netty.handler.codec.http.HttpRequest nettyRequest = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, method, request.getUrl().toString(),
            Unpooled.wrappedBuffer(requestBytes));
        setHeaders(nettyRequest, request, binaryDataContent.getLength(), true);

        channel.writeAndFlush(nettyRequest);
    }

    private static void sendFileRequest(
        HttpRequest request, FileContent fileContent, Channel channel) throws IOException {
        HttpMethod method = mapHttpMethod(request.getHttpMethod());

        io.netty.handler.codec.http.HttpRequest nettyRequest = new DefaultHttpRequest(
            HttpVersion.HTTP_1_1, method, request.getUrl().toString());
        Long contentLength = fileContent.getLength();
        setHeaders(nettyRequest, request, contentLength, true);
        ChunkedInput<ByteBuf> chunkedInput = new ChunkedNioFile(fileContent.getFile().toFile());

        channel.write(nettyRequest);
        if (contentLength != null) {
            channel.writeAndFlush(chunkedInput);
        } else {
            channel.writeAndFlush(new HttpChunkedInput(chunkedInput));
        }
    }

    private static void sendStreamRequest(
        HttpRequest request, InputStreamContent inputStreamContent, Channel channel) {
        HttpMethod method = mapHttpMethod(request.getHttpMethod());

        io.netty.handler.codec.http.HttpRequest nettyRequest = new DefaultHttpRequest(
            HttpVersion.HTTP_1_1, method, request.getUrl().toString());
        Long contentLength = inputStreamContent.getLength();
        setHeaders(nettyRequest, request, contentLength, true);
        ChunkedInput<ByteBuf> chunkedInput = new ChunkedStream(inputStreamContent.toStream());

        channel.write(nettyRequest);
        if (contentLength != null) {
            channel.writeAndFlush(chunkedInput);
        } else {
            channel.writeAndFlush(new HttpChunkedInput(chunkedInput));
        }
    }

    private void sendFluxRequest(
        HttpRequest request, BinaryDataContent content, Channel channel) {
        HttpMethod method = mapHttpMethod(request.getHttpMethod());
        io.netty.handler.codec.http.HttpRequest nettyRequest = new DefaultHttpRequest(
            HttpVersion.HTTP_1_1, method, request.getUrl().toString());
        Long contentLength = content.getLength();
        setHeaders(nettyRequest, request, contentLength, true);

        channel.write(nettyRequest);

        content.toFluxByteBuffer().doOnEach(signal -> {
            if (signal.isOnNext()) {
                ByteBuffer byteBuffer = signal.get();
                if (byteBuffer != null) {
                    channel.write(Unpooled.wrappedBuffer(byteBuffer));
                }
            } else if (signal.isOnComplete()) {
                if (contentLength == null) {
                    channel.writeAndFlush(new DefaultLastHttpContent());
                } else {
                    channel.flush();
                }
            } else if (signal.isOnError()) {
                channel.close();
                requestContext.getChannelPool().release(channel);
                Throwable throwable = signal.getThrowable();
                if (throwable != null) {
                    requestContext.getResponseFuture().completeExceptionally(throwable);
                } else {
                    requestContext.getResponseFuture().completeExceptionally(
                        new RuntimeException("bad reactor"));
                }
            }
        }).subscribe();
    }

    private static void setHeaders(
        io.netty.handler.codec.http.HttpRequest nettyRequest, HttpRequest request,
        Long contentLength, boolean hasBody) {
        HttpHeaders nettyHeaders = nettyRequest.headers();
        nettyHeaders.set(HttpHeaderNames.HOST, request.getUrl().getHost());
        for (HttpHeader header : request.getHeaders()) {
            nettyRequest.headers().set(header.getName(), header.getValuesList());
        }
        if (hasBody && !nettyHeaders.contains(HttpHeaderNames.CONTENT_LENGTH)) {
            if (contentLength != null) {
                nettyRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);
            } else {
                nettyRequest.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            }
        }
    }

    private static HttpMethod mapHttpMethod(com.azure.core.http.HttpMethod httpMethod) {
        switch (httpMethod) {
            case GET:
                return HttpMethod.GET;
            case POST:
                return HttpMethod.POST;
            case PUT:
                return HttpMethod.PUT;
            case PATCH:
                return HttpMethod.PATCH;
            case DELETE:
                return HttpMethod.DELETE;
            case HEAD:
                return HttpMethod.HEAD;
            case OPTIONS:
                return HttpMethod.OPTIONS;
            case TRACE:
                return HttpMethod.TRACE;
            case CONNECT:
                return HttpMethod.CONNECT;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown http method"));
        }
    }
}
