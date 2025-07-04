//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//package io.clientcore.http.netty4.implementation;
//
//import io.clientcore.core.http.models.HttpMethod;
//import io.clientcore.core.http.models.HttpRequest;
//import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.Channel;
//import io.netty.handler.codec.http.DefaultFullHttpResponse;
//import io.netty.handler.codec.http.DefaultHttpHeaders;
//import io.netty.handler.codec.http.DefaultHttpResponse;
//import io.netty.handler.codec.http.HttpResponseStatus;
//import io.netty.handler.codec.http.HttpVersion;
//import io.netty.handler.codec.http.LastHttpContent;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Timeout;
//
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicReference;
//
//import static io.clientcore.http.netty4.TestUtils.createChannelWithReadHandling;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
///**
// * Tests {@link Netty4ResponseHandler}.
// */
//@Timeout(value = 3, unit = TimeUnit.MINUTES)
//public class Netty4ResponseHandlerTests {
//    @Test
//    public void firstReadIsFullHttpResponse() throws Exception {
//        HttpRequest request = new HttpRequest();
//        AtomicReference<ResponseStateInfo> responseReference = new AtomicReference<>();
//        CountDownLatch latch = new CountDownLatch(1);
//
//        Netty4ResponseHandler responseHandler
//            = new Netty4ResponseHandler(request, responseReference, new AtomicReference<>(), latch);
//
//        Channel ch = createChannelWithReadHandling((ignored, channel) -> {
//            Netty4ResponseHandler handler = channel.pipeline().get(Netty4ResponseHandler.class);
//            MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);
//
//            try {
//                handler.channelRead(ctx, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
//                    Unpooled.EMPTY_BUFFER, new DefaultHttpHeaders(), new DefaultHttpHeaders()));
//                handler.channelReadComplete(ctx);
//            } catch (Exception ex) {
//                ctx.fireExceptionCaught(ex);
//            }
//        });
//
//        ch.pipeline().addLast(responseHandler);
//        ch.read();
//
//        assertEquals(0, latch.getCount());
//
//        ResponseStateInfo info = responseReference.get();
//        assertNotNull(info);
//
//        assertTrue(info.isChannelConsumptionComplete());
//        assertEquals(0, info.getEagerContent().size());
//    }
//
//    @Test
//    public void incompleteIgnoredResponseBody() {
//        byte[] ignoredBodyData = new byte[32];
//        ThreadLocalRandom.current().nextBytes(ignoredBodyData);
//
//        HttpRequest request = new HttpRequest().setMethod(HttpMethod.HEAD);
//        AtomicReference<ResponseStateInfo> responseReference = new AtomicReference<>();
//        CountDownLatch latch = new CountDownLatch(1);
//
//        Netty4ResponseHandler responseHandler
//            = new Netty4ResponseHandler(request, responseReference, new AtomicReference<>(), latch);
//
//        Channel ch = createChannelWithReadHandling((readCount, channel) -> {
//            if (readCount == 0) {
//                Netty4ResponseHandler handler = channel.pipeline().get(Netty4ResponseHandler.class);
//                MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);
//                try {
//                    handler.channelRead(ctx,
//                        new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, new DefaultHttpHeaders()));
//                    handler.channelReadComplete(ctx);
//                } catch (Exception ex) {
//                    ctx.fireExceptionCaught(ex);
//                }
//            } else {
//                Netty4EagerConsumeChannelHandler handler
//                    = channel.pipeline().get(Netty4EagerConsumeChannelHandler.class);
//                MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(ignoredBodyData));
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(ignoredBodyData));
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(ignoredBodyData));
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(ignoredBodyData));
//                handler.channelRead(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
//                handler.channelReadComplete(ctx);
//            }
//        });
//
//        ch.pipeline().addLast(responseHandler);
//        ch.read();
//
//        assertEquals(0, latch.getCount());
//
//        ResponseStateInfo info = responseReference.get();
//        assertNotNull(info);
//    }
//
//    @Test
//    public void bufferedResponseBodyLargerThanInitialRead() {
//        byte[] bodyPieces = new byte[32];
//        ThreadLocalRandom.current().nextBytes(bodyPieces);
//
//        byte[] expectedBody = new byte[bodyPieces.length * 4];
//        System.arraycopy(bodyPieces, 0, expectedBody, 0, bodyPieces.length);
//        System.arraycopy(bodyPieces, 0, expectedBody, bodyPieces.length, bodyPieces.length);
//        System.arraycopy(bodyPieces, 0, expectedBody, bodyPieces.length * 2, bodyPieces.length);
//        System.arraycopy(bodyPieces, 0, expectedBody, bodyPieces.length * 3, bodyPieces.length);
//
//        HttpRequest request = new HttpRequest();
//        AtomicReference<ResponseStateInfo> responseReference = new AtomicReference<>();
//        CountDownLatch latch = new CountDownLatch(1);
//
//        Netty4ResponseHandler responseHandler
//            = new Netty4ResponseHandler(request, responseReference, new AtomicReference<>(), latch);
//
//        Channel ch = createChannelWithReadHandling((readCount, channel) -> {
//            if (readCount == 0) {
//                Netty4ResponseHandler handler = channel.pipeline().get(Netty4ResponseHandler.class);
//                MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);
//                try {
//                    handler.channelRead(ctx,
//                        new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, new DefaultHttpHeaders()));
//                    handler.channelReadComplete(ctx);
//                } catch (Exception ex) {
//                    ctx.fireExceptionCaught(ex);
//                }
//            } else {
//                Netty4EagerConsumeChannelHandler handler
//                    = channel.pipeline().get(Netty4EagerConsumeChannelHandler.class);
//                MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(bodyPieces));
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(bodyPieces));
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(bodyPieces));
//                handler.channelRead(ctx, Unpooled.wrappedBuffer(bodyPieces));
//                handler.channelRead(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
//                handler.channelReadComplete(ctx);
//            }
//        });
//
//        ch.pipeline().addLast(responseHandler);
//        ch.read();
//
//        assertEquals(0, latch.getCount());
//
//        ResponseStateInfo info = responseReference.get();
//        assertNotNull(info);
//    }
//}
