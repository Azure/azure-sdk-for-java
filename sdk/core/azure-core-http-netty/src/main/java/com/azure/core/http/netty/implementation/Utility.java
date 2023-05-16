// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import io.netty.buffer.ByteBuf;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelOperations;

import java.nio.ByteBuffer;

/**
 * Helper class containing utility methods.
 */
public final class Utility {
    /**
     * Deep copies the passed {@link ByteBuf} into a {@link ByteBuffer}.
     * <p>
     * Using this method ensures that data returned by the network is resilient against Reactor Netty releasing the
     * passed {@link ByteBuf} once the {@code doOnNext} operator fires.
     *
     * @param byteBuf The Netty {@link ByteBuf} to deep copy.
     * @return A newly allocated {@link ByteBuffer} containing the copied bytes.
     */
    public static ByteBuffer deepCopyBuffer(ByteBuf byteBuf) {
        ByteBuffer buffer = ByteBuffer.allocate(byteBuf.readableBytes());
        byteBuf.readBytes(buffer);
        buffer.rewind();
        return buffer;
    }

    /**
     * Closes a connection if it hasn't been disposed.
     *
     * @param reactorNettyConnection The connection to close.
     */
    public static void closeConnection(Connection reactorNettyConnection) {
        // ChannelOperations is generally the default implementation of Connection used.
        //
        // Using the specific subclass allows for a finer grain handling.
        if (reactorNettyConnection instanceof ChannelOperations) {
            ChannelOperations<?, ?> channelOperations = (ChannelOperations<?, ?>) reactorNettyConnection;

            // Given that this is an HttpResponse the only time this will be called is when the outbound has completed.
            //
            // From there the only thing that needs to be checked is whether the inbound has been disposed (completed),
            // and if not dispose it (aka drain it).
            if (!channelOperations.isInboundDisposed()) {
                channelOperations.channel().eventLoop().execute(channelOperations::discard);
            }
        } else if (!reactorNettyConnection.isDisposed()) {
            reactorNettyConnection.channel().eventLoop().execute(reactorNettyConnection::dispose);
        }
    }

    /**
     * Gets the buffer size to use when using {@link ByteBufAsyncWriteSubscriber} and {@link ByteBufWriteSubscriber}.
     *
     * @param bodySize The size of the body, may be null if the body size isn't specified.
     * @return The buffer size to use.
     */
    static int getByteBufSubscriberBufferSize(Long bodySize) {
        if (bodySize == null) {
            return 32768; // 32KB
        }

        // The value returned here requires fine-tuning as these size could result in the memory being allocated inside
        // or outside TLAB, where outside TLAB allocations are much slower. But this also needs to walk a fine middle
        // ground on being large enough to reduce the number of file writing calls being made.
        //
        // For very large files it may be better to have outside TLAB allocations to reduce the number of overall writes
        // required to fully write the file.
        if (bodySize > 1024 * 1024 * 1024) { // 1GB+
            return 131072; // 128KB
        } else if (bodySize > 100 * 1024 * 1024) { // 100MB+
            return 65536; // 64KB
        } else {
            return 32768; // 32KB
        }
    }

    private Utility() {
    }
}
