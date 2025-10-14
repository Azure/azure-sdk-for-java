// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.http.netty4.implementation.Netty4AlpnHandler;
import io.clientcore.http.netty4.mocking.MockChannel;
import io.netty.channel.Channel;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoop;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class TestUtils {
    /**
     * Asserts that two arrays are equal.
     * <p>
     * This method is similar to JUnit's {@link Assertions#assertArrayEquals(byte[], byte[])} except that it takes
     * advantage of hardware intrinsics offered by the JDK to optimize comparing the byte arrays.
     * <p>
     * If the arrays aren't equal this will call {@link Assertions#assertArrayEquals(byte[], byte[])} to take advantage
     * of the better error message, but this is the exceptional case and worth the double comparison performance hit.
     *
     * @param expected The expected byte array.
     * @param actual The actual byte array.
     */
    public static void assertArraysEqual(byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual)) {
            Assertions.assertArrayEquals(expected, actual);
        }
    }

    /**
     * Creates a {@link Channel} that is able to mock {@link Channel#read()} operations.
     *
     * @param readHandler A {@link BiConsumer} that takes the current read count and the channel and mocks reading
     * operations.
     * @return A {@link Channel}.
     */
    public static Channel createChannelWithReadHandling(BiConsumer<Integer, Channel> readHandler) {
        return createChannelWithReadHandling(readHandler, null);
    }

    /**
     * Creates a {@link Channel} that is able to mock {@link Channel#read()} operations.
     *
     * @param readHandler A {@link BiConsumer} that takes the current read count and the channel and mocks reading
     * operations.
     * @param protocolVersion The HTTP protocol version to set on the channel's attributes. Can be null.
     * @return A {@link Channel}.
     */
    public static Channel createChannelWithReadHandling(BiConsumer<Integer, Channel> readHandler,
        HttpProtocolVersion protocolVersion) {
        EventLoop eventLoop = new DefaultEventLoop() {
            @Override
            public boolean inEventLoop(Thread thread) {
                return true;
            }
        };

        AtomicInteger readCount = new AtomicInteger();
        Channel channel = new MockChannel() {
            @Override
            public Channel read() {
                int count = readCount.getAndIncrement();
                readHandler.accept(count, this);
                return this;
            }

            @Override
            public boolean isActive() {
                return true;
            }
        };

        if (protocolVersion != null) {
            channel.attr(Netty4AlpnHandler.HTTP_PROTOCOL_VERSION_KEY).set(protocolVersion);
        }

        try {
            eventLoop.register(channel).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return channel;
    }
}
