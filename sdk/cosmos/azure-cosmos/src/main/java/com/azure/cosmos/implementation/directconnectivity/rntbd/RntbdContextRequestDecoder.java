// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RntbdContextRequestDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(RntbdContextRequestDecoder.class);

    public RntbdContextRequestDecoder() {
        this.setSingleDecode(true);
    }

    /**
     * Prepare for decoding an @{link RntbdContextRequest} or fire a channel readTree event to pass the input message along
     *
     * @param context the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param message the message to be decoded
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) throws Exception {

        if (message instanceof ByteBuf) {

            final ByteBuf in = (ByteBuf)message;
            
            // BREADCRUMB: Track buffer before reading operation type
            if (logger.isTraceEnabled()) {
                in.touch("RntbdContextRequestDecoder.channelRead: before reading resourceOperationType");
                logger.trace("{} RntbdContextRequestDecoder.channelRead: ByteBuf refCnt={}, readableBytes={}", 
                    context.channel(), in.refCnt(), in.readableBytes());
            }
            
            final int resourceOperationType = in.getInt(in.readerIndex() + Integer.BYTES);

            if (resourceOperationType == 0) {
                assert this.isSingleDecode();
                
                // BREADCRUMB: Going through normal decode path
                if (logger.isTraceEnabled()) {
                    in.touch("RntbdContextRequestDecoder.channelRead: passing to super.channelRead (resourceOperationType == 0)");
                    logger.trace("{} RntbdContextRequestDecoder: resourceOperationType=0, delegating to super.channelRead", 
                        context.channel());
                }
                super.channelRead(context, message);
                return;
            }
            
            // BREADCRUMB: Bypassing decoder - this is a potential leak point if downstream doesn't release
            if (logger.isTraceEnabled()) {
                in.touch("RntbdContextRequestDecoder.channelRead: bypassing decoder (resourceOperationType != 0)");
                logger.trace("{} RntbdContextRequestDecoder: resourceOperationType={}, bypassing decoder and forwarding downstream", 
                    context.channel(), resourceOperationType);
            }
        }
        
        // BREADCRUMB: Message forwarded downstream - downstream MUST release it
        if (logger.isTraceEnabled() && message instanceof ByteBuf) {
            ((ByteBuf) message).touch("RntbdContextRequestDecoder.channelRead: forwarding to next handler");
        }
        context.fireChannelRead(message);
    }

    /**
     * Decode an RntbdContextRequest from an {@link ByteBuf} stream
     * <p>
     * This method will be called till either an input {@link ByteBuf} has nothing to readTree on return from this method or
     * till nothing is readTree from the input {@link ByteBuf}.
     *
     * @param context the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in      the {@link ByteBuf} from which to readTree data
     * @param out     the {@link List} to which decoded messages should be added
     * @throws IllegalStateException thrown if an error occurs
     */
    @Override
    protected void decode(final ChannelHandlerContext context, final ByteBuf in, final List<Object> out) throws IllegalStateException {

        final RntbdContextRequest request;
        in.markReaderIndex();

        try {
            request = RntbdContextRequest.decode(in);
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
        logger.warn("{} RntbdContextRequestDecoder.exceptionCaught: {}", ctx.channel(), cause.getMessage(), cause);
        super.exceptionCaught(ctx, cause);
    }
}
