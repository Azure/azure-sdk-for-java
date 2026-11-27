// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.models.binarydata.BinaryData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.CHUNKED_WRITER;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.HTTP_RESPONSE;
import static io.clientcore.http.netty4.implementation.Netty4HandlerNames.PROGRESS_AND_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Netty4ExpectContinueHandlerTests {
    @Test
    public void informationalResponsesAreSuppressedAndBodyIsWrittenOnce() {
        EmbeddedChannel channel = channelWithRequest();
        try {
            channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EARLY_HINTS));
            channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
            assertNull(channel.readInbound());
            assertNull(channel.readOutbound());

            channel.writeInbound(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
            LastHttpContent body = channel.readOutbound();
            try {
                assertEquals("body", body.content().toString(StandardCharsets.UTF_8));
            } finally {
                body.release();
            }

            channel.advanceTimeBy(2, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();
            assertNull(channel.readOutbound());
            assertNull(channel.readInbound());

            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            assertTrue(channel.writeInbound(response));
            assertEquals(response, channel.readInbound());
            response.release();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void lateContinueAfterFallbackDoesNotWriteBodyTwice() {
        EmbeddedChannel channel = channelWithRequest();
        try {
            channel.advanceTimeBy(1, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();
            LastHttpContent body = channel.readOutbound();
            try {
                assertEquals("body", body.content().toString(StandardCharsets.UTF_8));
            } finally {
                body.release();
            }

            channel.writeInbound(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            assertNull(channel.readOutbound());
            assertNull(channel.readInbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void finalResponseCancelsFallbackAndMarksRequestIncomplete() {
        EmbeddedChannel channel = channelWithRequest();
        try {
            FullHttpResponse response
                = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED);
            assertTrue(channel.writeInbound(response));
            assertEquals(response, channel.readInbound());
            response.release();

            channel.advanceTimeBy(2, TimeUnit.SECONDS);
            channel.runScheduledPendingTasks();
            assertNull(channel.readOutbound());
            assertTrue(channel.attr(Netty4ExpectContinueHandler.INCOMPLETE_REQUEST).get());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void finalResponseCancelsPartialUploadAndClosesStream() {
        AtomicBoolean closed = new AtomicBoolean();
        ByteArrayInputStream stream = new CloseTrackingInputStream(closed);
        AtomicReference<Throwable> error = new AtomicReference<>();
        EmbeddedChannel channel = new EmbeddedChannel();
        Netty4ProgressAndTimeoutHandler progress = new Netty4ProgressAndTimeoutHandler(null, 100, 100, 0);
        channel.pipeline().addLast("backpressure", new BackpressureHandler());
        channel.pipeline().addLast(PROGRESS_AND_TIMEOUT, progress);
        channel.pipeline().addLast(HTTP_RESPONSE, new ChannelInboundHandlerAdapter());
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri("http://localhost/")
            .setBody(BinaryData.fromStream(stream, 32768L));
        request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");
        try {
            assertTrue(Netty4Utility.sendHttp11Request(request, channel, error).isSuccess());
            assertTrue(channel.readOutbound() instanceof io.netty.handler.codec.http.HttpRequest);
            channel.writeInbound(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            HttpContent firstChunk = channel.readOutbound();
            assertNotNull(firstChunk);
            firstChunk.release();
            assertNotNull(progress.getWriteTimeoutWatcher());

            FullHttpResponse response
                = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED);
            assertTrue(channel.writeInbound(response));
            assertEquals(response, channel.readInbound());
            response.release();
            assertTrue(closed.get(), "The unfinished upload stream must be closed.");
            assertNull(channel.pipeline().get(CHUNKED_WRITER));
            assertNull(progress.getWriteTimeoutWatcher());
            assertNull(progress.getResponseTimeoutWatcher());
            assertNull(error.get());

            channel.unsafe().outboundBuffer().setUserDefinedWritability(1, true);
            channel.runPendingTasks();
            assertNull(channel.readOutbound());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void lateWriteCompletionDoesNotRestartResponseTimeout() {
        DelayedWriteHandler delayedWrite = new DelayedWriteHandler();
        EmbeddedChannel channel = new EmbeddedChannel();
        Netty4ProgressAndTimeoutHandler progress = new Netty4ProgressAndTimeoutHandler(null, 100, 100, 0);
        channel.pipeline().addLast("delay", delayedWrite);
        channel.pipeline().addLast(PROGRESS_AND_TIMEOUT, progress);
        channel.pipeline().addLast(HTTP_RESPONSE, new ChannelInboundHandlerAdapter());
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri("http://localhost/")
            .setBody(BinaryData.fromString("body"));
        request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");
        try {
            assertTrue(Netty4Utility.sendHttp11Request(request, channel, new AtomicReference<>()).isSuccess());
            assertTrue(channel.readOutbound() instanceof io.netty.handler.codec.http.HttpRequest);
            channel.writeInbound(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            assertNotNull(delayedWrite.pendingWrite);

            FullHttpResponse response
                = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED);
            assertTrue(channel.writeInbound(response));
            assertEquals(response, channel.readInbound());
            response.release();
            delayedWrite.pendingWrite.setSuccess();
            assertNull(progress.getWriteTimeoutWatcher());
            assertNull(progress.getResponseTimeoutWatcher());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static final class CloseTrackingInputStream extends ByteArrayInputStream {
        private final AtomicBoolean closed;

        private CloseTrackingInputStream(AtomicBoolean closed) {
            super(new byte[32768]);
            this.closed = closed;
        }

        @Override
        public void close() throws IOException {
            closed.set(true);
            super.close();
        }
    }

    private static final class BackpressureHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ctx.write(msg, promise);
            if (msg instanceof HttpContent && !(msg instanceof LastHttpContent)) {
                ctx.channel().unsafe().outboundBuffer().setUserDefinedWritability(1, false);
            }
        }
    }

    private static final class DelayedWriteHandler extends ChannelOutboundHandlerAdapter {
        private ChannelPromise pendingWrite;

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof LastHttpContent) {
                promise.setUncancellable();
                pendingWrite = promise;
                ReferenceCountUtil.release(msg);
            } else {
                ctx.write(msg, promise);
            }
        }
    }

    private static EmbeddedChannel channelWithRequest() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(HTTP_RESPONSE, new ChannelInboundHandlerAdapter());
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.PUT)
            .setUri("http://localhost/")
            .setBody(BinaryData.fromString("body"));
        request.getHeaders().set(HttpHeaderName.EXPECT, "100-continue");
        assertTrue(Netty4Utility.sendHttp11Request(request, channel, new AtomicReference<>()).isSuccess());
        assertTrue(channel.readOutbound() instanceof io.netty.handler.codec.http.HttpRequest);
        return channel;
    }
}
