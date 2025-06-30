// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.json.JsonWriter;
import io.netty.channel.Channel;

import java.io.ByteArrayInputStream;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static io.clientcore.http.netty4.implementation.Netty4Utility.awaitLatch;

/**
 * Implementation of {@link BinaryData} that is backed by a Netty {@link Channel}.
 */
public final class Netty4ChannelBinaryData extends BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(Netty4ChannelBinaryData.class);
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final String TOO_LARGE_FOR_BYTE_ARRAY
        = "The content length is too large for a byte array. Content length is: ";

    private final Channel channel;
    private final Long length;
    private final boolean isHttp2;
    private final AtomicBoolean streamDrained = new AtomicBoolean(false);

    // Non-final to allow nulling out after use.
    private ByteArrayOutputStream eagerContent;

    private volatile byte[] bytes;

    /**
     * Creates an instance of {@link Netty4ChannelBinaryData}.
     *
     * @param eagerContent Response body content that was eagerly read by Netty while processing the HTTP headers.
     * @param channel The Netty {@link Channel}.
     * @param length Size of the response body (if known).
     * @param isHttp2 Flag indicating whether the handler is used for HTTP/2 or not.
     */
    public Netty4ChannelBinaryData(ByteArrayOutputStream eagerContent, Channel channel, Long length, boolean isHttp2) {
        this.eagerContent = eagerContent;
        this.channel = channel;
        this.length = length;
        this.isHttp2 = isHttp2;
    }

    @Override
    public byte[] toBytes() {
        if (length != null && length > MAX_ARRAY_SIZE) {
            throw LOGGER.throwableAtError().log(TOO_LARGE_FOR_BYTE_ARRAY + length, IllegalStateException::new);
        }

        if (bytes == null) {
            drainStream();
            bytes = eagerContent.toByteArray();
            eagerContent = null;
        }

        return bytes;
    }

    @Override
    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public <T> T toObject(Type type, ObjectSerializer serializer) {
        try {
            return serializer.deserializeFromBytes(toBytes(), type);
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public InputStream toStream() {
        if (bytes == null) {
            return new Netty4ChannelInputStream(eagerContent, channel, isHttp2, this::drainStream);
        } else {
            return new ByteArrayInputStream(bytes);
        }
    }

    @Override
    public void writeTo(JsonWriter jsonWriter) {
        Objects.requireNonNull(jsonWriter, "'jsonWriter' cannot be null");

        try {
            jsonWriter.writeBinary(toBytes());
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public void writeTo(OutputStream outputStream) {
        try {
            if (bytes == null) {
                // Channel hasn't been read yet, don't buffer it, just write it to the OutputStream as it's being read.
                if (eagerContent.size() > 0) {
                    outputStream.write(eagerContent.toByteArray());
                    eagerContent.reset();
                }

                CountDownLatch latch = new CountDownLatch(1);
                Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch,
                    buf -> buf.readBytes(outputStream, buf.readableBytes()), isHttp2);
                channel.pipeline().addLast(Netty4HandlerNames.EAGER_CONSUME, handler);
                channel.config().setAutoRead(true);

                channel.eventLoop().execute(channel::read);

                awaitLatch(latch);
                streamDrained.set(true);

                Throwable exception = handler.channelException();
                if (exception != null) {
                    if (exception instanceof Error) {
                        throw (Error) exception;
                    } else {
                        throw CoreException.from(exception);
                    }
                }
            } else {
                // Already converted the Channel to a byte[], use it.
                outputStream.write(bytes);
            }
        } catch (IOException ex) {
            throw LOGGER.throwableAtError().log(ex, CoreException::from);
        } finally {
            close();
        }
    }

    @Override
    public void writeTo(WritableByteChannel byteChannel) {
        writeTo(Channels.newOutputStream(byteChannel));
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes());
    }

    @Override
    public Long getLength() {
        return length;
    }

    @Override
    public boolean isReplayable() {
        return false;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        return BinaryData.fromBytes(toBytes());
    }

    /**
     * Ensures the underlying network stream is fully consumed but does not close the channel,
     * allowing it to be reused by the connection pool.
     */
    @Override
    public void close() {
        drainStream();
    }

    private void drainStream() {
        if (streamDrained.compareAndSet(false, true)) {
            if (channel.pipeline().get(Netty4EagerConsumeChannelHandler.class) != null) {
                return;
            }

            CountDownLatch latch = new CountDownLatch(1);
            Netty4EagerConsumeChannelHandler handler = new Netty4EagerConsumeChannelHandler(latch,
                buf -> buf.readBytes(eagerContent, buf.readableBytes()), isHttp2);
            channel.pipeline().addLast(Netty4HandlerNames.EAGER_CONSUME, handler);
            channel.config().setAutoRead(true);

            channel.eventLoop().execute(channel::read);
            awaitLatch(latch);

            Throwable exception = handler.channelException();
            if (exception != null) {
                if (exception instanceof Error) {
                    throw (Error) exception;
                } else {
                    throw CoreException.from(exception);
                }
            }
        }
    }
}
