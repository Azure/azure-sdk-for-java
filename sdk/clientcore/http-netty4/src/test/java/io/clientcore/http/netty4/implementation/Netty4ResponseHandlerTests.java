// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.TestUtils.createChannelWithReadHandling;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Netty4ResponseHandler}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class Netty4ResponseHandlerTests {
    @Test
    public void firstReadIsFullHttpResponse() throws Exception {
        HttpRequest request = new HttpRequest();
        AtomicReference<ResponseStateInfo> responseReference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Netty4ResponseHandler responseHandler
            = new Netty4ResponseHandler(request, responseReference, new AtomicReference<>(), latch);

        Channel ch = createChannelWithReadHandling((ignored, channel) -> {
            Netty4ResponseHandler handler = channel.pipeline().get(Netty4ResponseHandler.class);
            MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);

            try {
                handler.channelRead(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                    Unpooled.EMPTY_BUFFER, new DefaultHttpHeaders(), new DefaultHttpHeaders()));
                handler.channelReadComplete(ctx);
            } catch (Exception ex) {
                ctx.fireExceptionCaught(ex);
            }
        });

        ch.pipeline().addLast(responseHandler);
        ch.read();

        assertEquals(0, latch.getCount());

        ResponseStateInfo info = responseReference.get();
        assertNotNull(info);

        assertTrue(info.isChannelConsumptionComplete());
        assertEquals(0, info.getEagerContent().size());
    }

    @Test
    public void incompleteIgnoredResponseBody() throws InterruptedException {
        CountDownLatch headersLatch = new CountDownLatch(1);
        Netty4ResponseHandler responseHandler = new Netty4ResponseHandler(new HttpRequest().setMethod(HttpMethod.HEAD),
            new AtomicReference<>(), new AtomicReference<>(), headersLatch);

        CountDownLatch bodyLatch = new CountDownLatch(1);

        Channel ch = createChannelWithReadHandling((readCount, channel) -> {
            try {
                if (readCount == 0) {
                    responseHandler.channelRead(new MockChannelHandlerContext(channel),
                        new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                    responseHandler.channelReadComplete(new MockChannelHandlerContext(channel));
                } else {
                    Netty4EagerConsumeChannelHandler eagerConsumer
                        = channel.pipeline().get(Netty4EagerConsumeChannelHandler.class);
                    eagerConsumer.channelRead(new MockChannelHandlerContext(channel),
                        LastHttpContent.EMPTY_LAST_CONTENT);
                    eagerConsumer.channelReadComplete(new MockChannelHandlerContext(channel));
                }
            } catch (Exception e) {
                channel.pipeline().fireExceptionCaught(e);
            }
        });

        ch.pipeline().addLast(responseHandler);

        ch.read();
        assertTrue(headersLatch.await(10, TimeUnit.SECONDS));

        ch.pipeline().addLast(new Netty4EagerConsumeChannelHandler(bodyLatch, ignored -> {
        }, false));

        ch.read();
        assertTrue(bodyLatch.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void bufferedResponseBodyLargerThanInitialRead() throws InterruptedException {
        AtomicReference<ResponseStateInfo> responseReference = new AtomicReference<>();
        CountDownLatch headersLatch = new CountDownLatch(1);

        Netty4ResponseHandler responseHandler
            = new Netty4ResponseHandler(new HttpRequest(), responseReference, new AtomicReference<>(), headersLatch);

        CountDownLatch bodyLatch = new CountDownLatch(1);

        Channel ch = createChannelWithReadHandling((readCount, channel) -> {
            try {
                if (readCount == 0) {
                    responseHandler.channelRead(new MockChannelHandlerContext(channel),
                        new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                    responseHandler.channelReadComplete(new MockChannelHandlerContext(channel));
                } else {
                    Netty4EagerConsumeChannelHandler eagerConsumer
                        = channel.pipeline().get(Netty4EagerConsumeChannelHandler.class);
                    eagerConsumer.channelRead(new MockChannelHandlerContext(channel),
                        LastHttpContent.EMPTY_LAST_CONTENT);
                    eagerConsumer.channelReadComplete(new MockChannelHandlerContext(channel));
                }
            } catch (Exception e) {
                channel.pipeline().fireExceptionCaught(e);
            }
        });

        ch.pipeline().addLast(responseHandler);

        ch.read();
        assertTrue(headersLatch.await(10, TimeUnit.SECONDS));
        ResponseStateInfo info = responseReference.get();
        assertNotNull(info);

        ch.pipeline().addLast(new Netty4EagerConsumeChannelHandler(bodyLatch, buf -> {
            try {
                buf.readBytes(info.getEagerContent(), buf.readableBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }, false));

        ch.read();
        assertTrue(bodyLatch.await(10, TimeUnit.SECONDS));
    }
}
