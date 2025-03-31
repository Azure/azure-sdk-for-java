// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty.implementation;

import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.json.JsonWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;

/**
 * Implementation of {@link BinaryData} that's based on a Netty {@link Channel} that is read when content is needed.
 */
public final class LazyChannelReadBinaryData extends BinaryData {
    private final Channel channel;

    // Might need a flag for whether the Channel was already consumed.
    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<LazyChannelReadBinaryData, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(LazyChannelReadBinaryData.class, byte[].class, "bytes");

    LazyChannelReadBinaryData(Channel channel) {
        this.channel = Objects.requireNonNull(channel, "'channel' cannot be null.");
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        consumeChannelEagerly(buf -> {
            try {
                buf.readBytes(outputStream, buf.readableBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        return outputStream.toByteArray();
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public <T> T toObject(Type type, ObjectSerializer serializer) throws IOException {
        return serializer.deserializeFromBytes(toBytes(), type);
    }

    @Override
    public InputStream toStream() {
        return null;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        consumeChannelEagerly(buf -> {
            try {
                buf.readBytes(outputStream, buf.readableBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        writeTo(Channels.newOutputStream(channel));
    }

    private void consumeChannelEagerly(Consumer<ByteBuf> readConsumer) {
        // Set autoRead to true as we're going to eagerly read the remaining data from the network now.
        channel.config().setAutoRead(true);
        channel.read();
        CountDownLatch latch = new CountDownLatch(1);
        ChannelInboundHandler eagerReadHandler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                ByteBuf buf = null;
                if (msg instanceof ByteBufHolder) {
                    buf = ((ByteBufHolder) msg).content();
                } else if (msg instanceof ByteBuf) {
                    buf = (ByteBuf) msg;
                }

                if (buf != null && buf.isReadable()) {
                    readConsumer.accept(buf);
                }
                super.channelRead(ctx, msg);
            }

            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                latch.countDown();
                super.channelReadComplete(ctx);
            }
        };
        channel.pipeline().addLast(eagerReadHandler);
        channel.read();
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        channel.pipeline().remove(eagerReadHandler);
    }

    @Override
    public void writeTo(JsonWriter jsonWriter) throws IOException {
        Objects.requireNonNull(jsonWriter, "'jsonWriter' cannot be null");

        jsonWriter.writeBinary(toBytes());
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes());
    }

    @Override
    public Long getLength() {
        return 0L;
    }

    @Override
    public boolean isReplayable() {
        return false;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        return BinaryData.fromBytes(toBytes());
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
