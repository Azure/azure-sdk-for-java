// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.json.JsonWriter;
import io.netty.channel.Channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static io.clientcore.http.netty4.implementation.NettyUtility.awaitLatch;

final class NettyChannelBinaryData extends BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(NettyChannelBinaryData.class);
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    private static final String TOO_LARGE_FOR_BYTE_ARRAY
        = "The content length is too large for a byte array. Content length is: ";

    private final Channel channel;
    private final Long length;

    // Non-final to allow nulling out after use.
    private ByteArrayOutputStream eagerContent;

    private volatile byte[] bytes;

    NettyChannelBinaryData(ByteArrayOutputStream eagerContent, Channel channel, Long length) {
        this.eagerContent = eagerContent;
        this.channel = channel;
        this.length = length;
    }

    @Override
    public byte[] toBytes() {
        if (length != null && length > MAX_ARRAY_SIZE) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + length));
        }

        if (bytes == null) {
            CountDownLatch latch = new CountDownLatch(1);
            channel.pipeline().addLast(new EagerConsumeNetworkResponseHandler(latch, buf -> {
                try {
                    buf.readBytes(eagerContent, buf.readableBytes());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }));

            awaitLatch(latch);
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
    public <T> T toObject(Type type, ObjectSerializer serializer) throws IOException {
        return serializer.deserializeFromBytes(toBytes(), type);
    }

    @Override
    public InputStream toStream() {
        // TODO (alzimmer): This needs to change to a deferred InputStream that pulls content from the Channel when
        //  needed. This is just a starting point.
        return new ByteArrayInputStream(toBytes());
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

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
