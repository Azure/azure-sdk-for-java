// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util.binarydata;

import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.implementation.util.IterableOfByteBuffersInputStream;
import io.clientcore.core.implementation.util.StreamUtil;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * A {@link BinaryData} implementation backed by an {@link InputStream}.
 */
public final class InputStreamBinaryData extends BinaryData {
    private static final ClientLogger LOGGER = new ClientLogger(InputStreamBinaryData.class);
    private static final int INITIAL_BUFFER_CHUNK_SIZE = 8 * 1024;
    private static final int MAX_BUFFER_CHUNK_SIZE = 8 * 1024 * 1024;
    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;
    private final Supplier<InputStream> content;
    private final Long length;
    private final boolean isReplayable;
    private final List<ByteBuffer> bufferedContent;

    private volatile byte[] bytes;
    private static final AtomicReferenceFieldUpdater<InputStreamBinaryData, byte[]> BYTES_UPDATER
        = AtomicReferenceFieldUpdater.newUpdater(InputStreamBinaryData.class, byte[].class, "bytes");

    /**
     * Creates an instance of {@link InputStreamBinaryData}.
     *
     * @param inputStream The inputStream that is used as the content for this instance.
     * @param length The length of the inputStream.
     * @throws NullPointerException if {@code content} is null.
     */
    public InputStreamBinaryData(InputStream inputStream, Long length) {
        Objects.requireNonNull(inputStream, "'inputStream' cannot be null.");
        this.length = length;
        this.isReplayable = canMarkReset(inputStream, length);
        if (this.isReplayable) {
            inputStream.mark(length.intValue());
            this.content = () -> resettableContent(inputStream);
        } else {
            this.content = () -> inputStream;
        }
        this.bufferedContent = null;
    }

    private InputStreamBinaryData(Supplier<InputStream> inputStreamSupplier, Long length,
        List<ByteBuffer> bufferedContent) {
        this.content = Objects.requireNonNull(inputStreamSupplier, "'inputStreamSupplier' cannot be null.");
        this.length = length;
        this.isReplayable = true;
        this.bufferedContent = bufferedContent;
    }

    @Override
    public byte[] toBytes() {
        if (length != null && length > MAX_ARRAY_SIZE) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(TOO_LARGE_FOR_BYTE_ARRAY + length));
        }

        return BYTES_UPDATER.updateAndGet(this, bytes -> bytes == null ? getBytes() : bytes);
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
        return content.get();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes()).asReadOnlyBuffer();
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        InputStream inputStream = content.get();
        if (bufferedContent != null) {
            // InputStream has been buffered, access the buffered elements directly to reduce memory copying.
            for (ByteBuffer bb : bufferedContent) {
                ImplUtils.writeByteBufferToStream(bb, outputStream);
            }
        } else {
            // Otherwise use a generic write to.
            // More optimizations can be done here based on the type of InputStream but this is the initial
            // implementation, so it has been kept simple.
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        }
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        InputStream inputStream = content.get();
        if (bufferedContent != null) {
            // InputStream has been buffered, access the buffered elements directly to reduce memory copying.
            for (ByteBuffer bb : bufferedContent) {
                bb = bb.duplicate();
                while (bb.hasRemaining()) {
                    channel.write(bb);
                }
            }
        } else {
            // Otherwise use a generic write to.
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                ByteBuffer bb = ByteBuffer.wrap(buffer, 0, read);
                while (bb.hasRemaining()) {
                    channel.write(bb);
                }
            }
        }
    }

    @Override
    public Long getLength() {
        byte[] data = BYTES_UPDATER.get(this);
        if (data != null) {
            return (long) data.length;
        }
        return length;
    }

    @Override
    public boolean isReplayable() {
        return isReplayable;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        if (isReplayable) {
            return this;
        }

        return readAndBuffer(this.content.get(), length);
    }

    private static boolean canMarkReset(InputStream inputStream, Long length) {
        return length != null && length < MAX_ARRAY_LENGTH && inputStream.markSupported();
    }

    private static InputStream resettableContent(InputStream stream) {
        try {
            stream.reset();
            return stream;
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private static InputStreamBinaryData readAndBuffer(InputStream inputStream, Long length) {
        try {
            List<ByteBuffer> byteBuffers = StreamUtil.readStreamToListOfByteBuffers(inputStream, length,
                INITIAL_BUFFER_CHUNK_SIZE, MAX_BUFFER_CHUNK_SIZE);

            return new InputStreamBinaryData(() -> new IterableOfByteBuffersInputStream(byteBuffers), length,
                byteBuffers);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    private byte[] getBytes() {
        try {
            AccessibleByteArrayOutputStream dataOutputBuffer = new AccessibleByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            InputStream inputStream = this.content.get();
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            return dataOutputBuffer.toByteArrayUnsafe();
        } catch (IOException ex) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public void close() throws IOException {
        content.get().close();
    }
}
