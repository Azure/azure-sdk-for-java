// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ResourceLeakDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class RntbdRequestDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(RntbdContextRequestDecoder.class);

    private static final boolean leakDetectionDebuggingEnabled = ResourceLeakDetector.getLevel().ordinal() >=
        ResourceLeakDetector.Level.ADVANCED.ordinal();

    /**
     * Prepare for decoding an @{link RntbdRequest} or fire a channel readTree event to pass the input message along.
     *
     * @param context the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param message the message to be decoded
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) throws Exception {

        if (message instanceof ByteBuf) {

            final ByteBuf in = (ByteBuf) message;
            // BREADCRUMB: Track buffer before reading operation type
            if (leakDetectionDebuggingEnabled) {
                in.touch("RntbdRequestDecoder.channelRead: before reading resourceOperationType");
            }

            final int resourceOperationType = in.getInt(in.readerIndex() + Integer.BYTES);

            if (resourceOperationType != 0) {
                // BREADCRUMB: Going through normal decode path
                if (leakDetectionDebuggingEnabled) {
                    in.touch("RntbdRequestDecoder.channelRead: passing to super.channelRead (resourceOperationType != 0)");
                }
                super.channelRead(context, message);
                return;
            }

            // BREADCRUMB: Bypassing decoder - this is a potential leak point if downstream doesn't release
            if (leakDetectionDebuggingEnabled) {
                in.touch("RntbdRequestDecoder.channelRead: bypassing decoder (resourceOperationType == 0)");
            }
        }

        // BREADCRUMB: Message forwarded downstream - downstream MUST release it
        if (leakDetectionDebuggingEnabled && message instanceof ByteBuf) {
            ((ByteBuf) message).touch("RntbdRequestDecoder.channelRead: forwarding to next handler");
        }
        context.fireChannelRead(message);
    }

    /**
     * Decode the input {@link ByteBuf} to an {@link RntbdRequest} instance.
     * <p>
     * This method will be called till either the input {@link ByteBuf} has nothing to readTree after return from this
     * method or till nothing was readTree from the input {@link ByteBuf}.
     *
     * @param context the {@link ChannelHandlerContext} to which this {@link ByteToMessageDecoder} belongs.
     * @param in the {@link ByteBuf} from which to read data.
     * @param out the {@link List} to which decoded messages should be added.
     *
     * @throws IllegalStateException thrown if an error occurs
     */
    @Override
    protected void decode(
        final ChannelHandlerContext context,
        final ByteBuf in,
        final List<Object> out) throws IllegalStateException {

        final RntbdRequest request;
        in.markReaderIndex();

        try {
            request = RntbdRequest.decode(in);
        } catch (final IllegalStateException error) {
            in.resetReaderIndex();
            throw error;
        }

        in.discardReadBytes();
        out.add(request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // BREADCRUMB: Track exceptions that might lead to leaked buffers
        logger.warn("{} RntbdRequestDecoder.exceptionCaught: {}", ctx.channel(), cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
