// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.channel.ChannelOperations;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;

/**
 * Helper class containing utility methods.
 */
public final class Utility {

    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

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

    public static Mono<Void> writeFile(Flux<ByteBuf> content, AsynchronousFileChannel outFile, long position) {
        if (content == null && outFile == null) {
            return FluxUtil.monoError(LOGGER, new NullPointerException("'content' and 'outFile' cannot be null."));
        } else if (content == null) {
            return FluxUtil.monoError(LOGGER, new NullPointerException("'content' cannot be null."));
        } else if (outFile == null) {
            return FluxUtil.monoError(LOGGER, new NullPointerException("'outFile' cannot be null."));
        } else if (position < 0) {
            return FluxUtil.monoError(LOGGER, new IllegalArgumentException("'position' cannot be less than 0."));
        }

        return Mono.create(emitter -> content.subscribe(new NettyFileWriteSubscriber(outFile, position, emitter)));
    }

    private Utility() {
    }
}
