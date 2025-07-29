// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.ByteArrayBinaryData;
import io.clientcore.http.netty4.mocking.MockChannelHandlerContext;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.LastHttpContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static io.clientcore.http.netty4.TestUtils.assertArraysEqual;
import static io.clientcore.http.netty4.TestUtils.createChannelWithReadHandling;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link Netty4ChannelBinaryData}.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
public class Netty4Http11ChannelBinaryDataTests {
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
        byte[] expected = "Hello world!".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream eagerContent = new ByteArrayOutputStream();
        eagerContent.write(expected);

        Netty4ChannelBinaryData binaryData
            = new Netty4ChannelBinaryData(eagerContent, channelWithNoData(), (long) expected.length, false);

        BinaryData replayable = binaryData.toReplayableBinaryData();
        assertInstanceOf(ByteArrayBinaryData.class, replayable);

        assertArraysEqual(expected, binaryData.toBytes());
        assertArraysEqual(expected, replayable.toBytes());
    }

    private static Channel channelWithNoData() {
        return createChannelWithReadHandling((ignored, channel) -> {
            Netty4EagerConsumeChannelHandler handler = channel.pipeline().get(Netty4EagerConsumeChannelHandler.class);
            MockChannelHandlerContext ctx = new MockChannelHandlerContext(channel);

            handler.channelRead(ctx, LastHttpContent.EMPTY_LAST_CONTENT);
            handler.channelReadComplete(ctx);
        });
    }
}
