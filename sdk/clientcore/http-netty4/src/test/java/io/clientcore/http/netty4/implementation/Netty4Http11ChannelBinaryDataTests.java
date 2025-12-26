// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.ByteArrayBinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.clientcore.http.netty4.TestUtils.assertArraysEqual;
import static io.clientcore.http.netty4.TestUtils.createChannelWithReadHandling;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link Netty4ChannelBinaryData}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class Netty4Http11ChannelBinaryDataTests {
    private static final byte[] HELLO_BYTES = "Hello".getBytes(StandardCharsets.UTF_8);
    private static final byte[] WORLD_BYTES = " World!".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HELLO_WORLD_BYTES = "Hello World!".getBytes(StandardCharsets.UTF_8);

    @Test
    public void toBytesWillThrowIsLengthIsTooLarge() {
        assertThrows(IllegalStateException.class,
            () -> new Netty4ChannelBinaryData(null, null, Long.MAX_VALUE, false).toBytes());
    }

    @Test
    public void toBytesCaches() throws IOException {
        byte[] expected = "Hello world!".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channelWithNoData(), (long) expected.length, false);

        assertArraysEqual(expected, binaryData.toBytes());
        assertArraysEqual(expected, binaryData.toBytes());
    }

    @Test
    public void toStringTest() throws IOException {
        String expected = "Hello world!";
        byte[] bytes = expected.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(bytes);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channelWithNoData(), (long) bytes.length, false);

        assertEquals(expected, binaryData.toString());
    }

    @Test
    public void toByteBuffer() throws IOException {
        byte[] bytes = "Hello world!".getBytes(StandardCharsets.UTF_8);
        ByteBuffer expected = ByteBuffer.wrap(bytes);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(bytes);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channelWithNoData(), (long) bytes.length, false);

        assertEquals(expected, binaryData.toByteBuffer());
    }

    @Test
    public void toStreamUsesBytesIfToBytesWasAlreadyCalled() throws IOException {
        byte[] expected = "Hello world!".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channelWithNoData(), (long) expected.length, false);

        assertArraysEqual(expected, binaryData.toBytes());

        InputStream stream = binaryData.toStream();
        assertNotEquals(Netty4ChannelInputStream.class, stream.getClass());

        byte[] actual = new byte[expected.length];
        int off = 0;
        int read;
        while ((read = stream.read(actual, off, actual.length - off)) != -1) {
            off += read;
        }

        assertArraysEqual(expected, actual);
    }

    @Test
    public void writeToOutputStreamUsesBytesIfToBytesWasAlreadyCalled() throws IOException {
        byte[] expected = "Hello world!".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channelWithNoData(), (long) expected.length, false);

        assertArraysEqual(expected, binaryData.toBytes());

        ByteArrayOutputStream writeTo = new ByteArrayOutputStream();
        binaryData.writeTo(writeTo);

        assertArraysEqual(expected, writeTo.toByteArray());
    }

    @Test
    public void channelBinaryDataLengthIsKnown() {
        assertEquals(1, new Netty4ChannelBinaryData(null, null, 1L, false).getLength());
    }

    @Test
    public void channelBinaryDataLengthIsUnknown() {
        assertNull(new Netty4ChannelBinaryData(null, null, null, false).getLength());
    }

    @Test
    public void channelBinaryDataIsNeverReplayable() {
        assertFalse(new Netty4ChannelBinaryData(null, null, null, false).isReplayable());
    }

    @Test
    public void channelBinaryDataToReplayableReturnsAByteArrayBinaryData() throws IOException {
        byte[] expected = HELLO_WORLD_BYTES;

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channelWithNoData(), (long) expected.length, false);

        BinaryData replayable = binaryData.toReplayableBinaryData();
        assertInstanceOf(ByteArrayBinaryData.class, replayable);

        assertArraysEqual(expected, binaryData.toBytes());
        assertArraysEqual(expected, replayable.toBytes());
    }

    @Test
    public void toStreamReturnsNettyStreamWhenNotDrained() throws IOException {
        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(HELLO_BYTES);
        Channel channel = createChannelWithReadHandling((ignored, ch) -> {
            ByteBuf content = Unpooled.wrappedBuffer(WORLD_BYTES);
            ch.pipeline().fireChannelRead(content);
            ch.pipeline().fireChannelRead(LastHttpContent.EMPTY_LAST_CONTENT);
            ch.pipeline().fireChannelReadComplete();
        });
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channel, (long) HELLO_WORLD_BYTES.length, false);

        InputStream stream = binaryData.toStream();

        assertInstanceOf(Netty4ChannelInputStream.class, stream);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        assertArraysEqual(HELLO_WORLD_BYTES, result.toByteArray());
    }

    @Test
    public void toBytesDrainsFromLiveChannel() throws IOException {
        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(HELLO_BYTES);

        EmbeddedChannel channel = new EmbeddedChannel();

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channel, (long) HELLO_WORLD_BYTES.length, false, null);

        Thread serverThread = new Thread(() -> {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            channel.writeInbound(new DefaultHttpContent(Unpooled.wrappedBuffer(WORLD_BYTES)));
            channel.writeInbound(LastHttpContent.EMPTY_LAST_CONTENT);
        });

        serverThread.start();

        byte[] result = binaryData.toBytes();

        assertArrayEquals(HELLO_WORLD_BYTES, result);
        assertTrue(channel.config().isAutoRead());
    }

    @Test
    public void toBytesThrowsIfChannelErrors() {
        IOException testException = new IOException("test error");
        Channel channel = createChannelWithReadHandling((ignored, ch) -> {
            ch.pipeline().addLast(new ExceptionSuppressingHandler());
            ch.pipeline().fireExceptionCaught(testException);
        });
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channel, 10L, false);

        CoreException exception = assertThrows(CoreException.class, binaryData::toBytes);
        assertEquals(testException, exception.getCause());
    }

    @Test
    public void closeAfterDrainingDisconnectsChannel() {
        TestMockChannel realChannel = new TestMockChannel();
        new DefaultEventLoop().register(realChannel);
        Runnable cleanupTask = () -> {
            realChannel.disconnect();
            realChannel.close();
        };

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), realChannel, 0L, false, cleanupTask);

        binaryData.toBytes();
        binaryData.close();

        assertTrue(realChannel.disconnectCalled);
        assertTrue(realChannel.closeCalled);
    }

    @Test
    public void toObjectThrowsCoreExceptionOnSerializationError() {
        TestMockChannel channel = new TestMockChannel();
        new DefaultEventLoop().register(channel);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channel, 0L, false);

        CoreException ex = assertThrows(CoreException.class,
            () -> binaryData.toObject(String.class, new IOExceptionThrowingSerializer()));
        assertInstanceOf(IOException.class, ex.getCause());
    }

    @Test
    public void cleanupDoesNothingIfHandlerIsMissing() {
        TestMockChannel realChannel = new TestMockChannel();
        new DefaultEventLoop().register(realChannel);
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), realChannel, 0L, false);

        binaryData.toBytes();

        assertFalse(realChannel.closeCalled);
    }

    @Test
    public void toBytesOnInactiveChannelReturnsEagerContent() throws IOException {
        byte[] eagerBytes = "eager".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(eagerBytes);

        TestMockChannel channel = new TestMockChannel();
        new DefaultEventLoop().register(channel);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channel, (long) eagerBytes.length, false);

        channel.close().awaitUninterruptibly();
        byte[] result = binaryData.toBytes();

        assertArraysEqual(eagerBytes, result);
    }

    @Test
    public void toBytesUsesEagerContentWhenSufficient() throws IOException {
        byte[] fullBody = "Full body".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(fullBody);

        TestMockChannel realChannel = new TestMockChannel();
        new DefaultEventLoop().register(realChannel);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, realChannel, (long) fullBody.length, false);

        byte[] result = binaryData.toBytes();

        assertArraysEqual(fullBody, result);
        assertFalse(realChannel.configCalled);
        assertFalse(realChannel.readCalled);
    }

    @Test
    public void closeBeforeDrainingEventuallyCleansUp() throws InterruptedException {
        EmbeddedChannel channel = new EmbeddedChannel();
        assertTrue(channel.isActive());

        CountDownLatch cleanupLatch = new CountDownLatch(1);
        Runnable cleanupTask = cleanupLatch::countDown;

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channel, 1L, false, cleanupTask);

        binaryData.close();

        channel.close().awaitUninterruptibly();

        assertTrue(cleanupLatch.await(10, TimeUnit.SECONDS),
            "Cleanup task was not called after the channel became inactive.");
    }

    @Test
    public void testBinaryDataWithoutOnClose() {
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), new EmbeddedChannel(), 10L, false);
        assertEquals(10L, binaryData.getLength());
    }

    @Test
    public void writeToAlreadyDrainedStreamThrowsException() {
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channelWithNoData(), 0L, false);

        binaryData.writeTo(new ByteArrayOutputStream());

        assertThrows(IllegalStateException.class, () -> binaryData.writeTo(new ByteArrayOutputStream()));
    }

    @Test
    public void writeToThrowsWhenChannelErrors() {
        IOException testException = new IOException("test writeTo error");
        Channel channel = createChannelWithReadHandling((ignored, ch) -> {
            ch.pipeline().addLast(new ExceptionSuppressingHandler());
            ch.pipeline().fireExceptionCaught(testException);
        });
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channel, 10L, false);

        CoreException exception
            = assertThrows(CoreException.class, () -> binaryData.writeTo(new ByteArrayOutputStream()));
        assertEquals(testException, exception.getCause());
    }

    @Test
    public void writeToThrowsWhenChannelThrowsError() {
        // This test covers the 'instanceof Error' branch in writeTo(OutputStream).
        AssertionError testError = new AssertionError("test writeTo error");
        Channel channel = createChannelWithReadHandling((ignored, ch) -> {
            ch.pipeline().addLast(new ExceptionSuppressingHandler());
            ch.pipeline().fireExceptionCaught(testError);
        });
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channel, 10L, false);

        AssertionError error
            = assertThrows(AssertionError.class, () -> binaryData.writeTo(new ByteArrayOutputStream()));
        assertEquals(testError, error);
    }

    @Test
    public void closeIsIdempotent() {
        AtomicInteger closed = new AtomicInteger(0);
        Runnable cleanupTask = closed::getAndIncrement;
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channelWithNoData(), 0L, false, cleanupTask);

        binaryData.toBytes();

        binaryData.close();
        binaryData.close();

        assertEquals(1, closed.get(), "Close should have been called only once");
    }

    @Test
    public void toBytesThrowsOnInactiveChannelWithIncompleteBody() throws IOException {
        // This test covers the case where the channel is closed but the eager content is insufficient.
        byte[] eagerBytes = "eager".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(eagerBytes);

        TestMockChannel channel = new TestMockChannel();
        new DefaultEventLoop().register(channel);

        // The Expected length is 10, but we only have 5 bytes.
        Netty4ChannelBinaryData binaryData = new Netty4ChannelBinaryData(eagerContent, channel, 10L, false);

        channel.close().awaitUninterruptibly();

        CoreException exception = assertThrows(CoreException.class, binaryData::toBytes);

        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    public void toBytesThrowsWhenChannelThrowsError() {
        // This test covers the 'instanceof Error' branch in drainStream() used by toBytes().
        AssertionError testError = new AssertionError("test toBytes error");
        Channel channel = createChannelWithReadHandling((ignored, ch) -> {
            ch.pipeline().addLast(new ExceptionSuppressingHandler());
            ch.pipeline().fireExceptionCaught(testError);
        });
        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(new ByteArrayOutputStream(), channel, 10L, false);

        AssertionError error = assertThrows(AssertionError.class, binaryData::toBytes);
        assertEquals(testError, error);
    }

    private static class TestMockChannel extends AbstractChannel {
        private volatile boolean configCalled = false;
        private volatile boolean readCalled = false;
        private volatile boolean disconnectCalled = false;
        private volatile boolean closeCalled = false;

        protected TestMockChannel() {
            super(null);
        }

        @Override
        protected AbstractUnsafe newUnsafe() {
            return new AbstractUnsafe() {
                @Override
                public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                    promise.setSuccess();
                }
            };
        }

        @Override
        protected boolean isCompatible(EventLoop loop) {
            return true;
        }

        @Override
        protected SocketAddress localAddress0() {
            return null;
        }

        @Override
        protected SocketAddress remoteAddress0() {
            return null;
        }

        @Override
        protected void doBind(SocketAddress localAddress) {
        }

        @Override
        protected void doDisconnect() {
            disconnectCalled = true;
        }

        @Override
        public ChannelFuture disconnect(ChannelPromise promise) {
            disconnectCalled = true;
            return super.disconnect(promise);
        }

        @Override
        public ChannelFuture disconnect() {
            disconnectCalled = true;
            return super.disconnect();
        }

        @Override
        protected void doClose() {
            closeCalled = true;
        }

        @Override
        public ChannelFuture close() {
            closeCalled = true;
            return super.close();
        }

        @Override
        public ChannelFuture close(ChannelPromise promise) {
            closeCalled = true;
            return super.close(promise);
        }

        @Override
        protected void doBeginRead() {
        }

        @Override
        protected void doWrite(ChannelOutboundBuffer in) {
        }

        @Override
        public ChannelConfig config() {
            configCalled = true;
            return new DefaultChannelConfig(this);
        }

        @Override
        public boolean isOpen() {
            return !closeCalled;
        }

        @Override
        public boolean isActive() {
            return !closeCalled;
        }

        @Override
        public ChannelMetadata metadata() {
            return new ChannelMetadata(false);
        }

        @Override
        public Channel read() {
            readCalled = true;
            return super.read();
        }
    }

    private static final class ExceptionSuppressingHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        }
    }

    private static Channel channelWithNoData() {
        return createChannelWithReadHandling((ignored, channel) -> {
            Netty4EagerConsumeChannelHandler handler = channel.pipeline().get(Netty4EagerConsumeChannelHandler.class);
            MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);

            handler.channelRead(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
            handler.channelReadComplete(ctx);
        });
    }

    private static final class IOExceptionThrowingSerializer implements ObjectSerializer {
        @Override
        public <T> T deserializeFromBytes(byte[] data, Type type) throws IOException {
            throw new IOException("deserialization failed");
        }

        @Override
        public <T> T deserializeFromStream(InputStream stream, Type type) throws IOException {
            throw new IOException("deserialization failed");
        }

        @Override
        public byte[] serializeToBytes(Object value) throws IOException {
            throw new IOException("serialization failed");
        }

        @Override
        public void serializeToStream(OutputStream stream, Object value) throws IOException {
            throw new IOException("serialization failed");
        }

        @Override
        public boolean supportsFormat(SerializationFormat format) {
            return true;
        }
    }
}
