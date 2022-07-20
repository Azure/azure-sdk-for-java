// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;


import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class IOUtilsTest {

    private static final Random RANDOM = new Random();

    @Test
    public void canTransferFromReadableByteChannelToWriteableByteChannel() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        RANDOM.nextBytes(data);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        WritableByteChannel destination = Channels.newChannel(byteArrayOutputStream);

        IOUtils.transfer(source, destination);

        assertArrayEquals(data, byteArrayOutputStream.toByteArray());
    }

    @Test
    public void canTransferFromReadableByteChannelToWriteableByteChannelWithPartialWrites() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        RANDOM.nextBytes(data);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        WritableByteChannel destination = new PartialWriteChannel(Channels.newChannel(byteArrayOutputStream));

        IOUtils.transfer(source, destination);

        assertArrayEquals(data, byteArrayOutputStream.toByteArray());
    }

    @Test
    public void canTransferFromReadableByteChannelToAsynchronousByteChannel() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        RANDOM.nextBytes(data);

        Path tempFile = Files.createTempFile("ioutilstest", null);
        tempFile.toFile().deleteOnExit();

        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        try (AsynchronousByteChannel destination = IOUtils.toAsynchronousByteChannel(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            IOUtils.transferAsync(source, destination).block();
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    @Test
    public void canTransferFromReadableByteChannelToAsynchronousByteChannelWithPartialWrites() throws IOException {
        byte[] data = new byte[10 * 1024 * 1024 + 117]; // more than default buffer.
        RANDOM.nextBytes(data);

        Path tempFile = Files.createTempFile("ioutilstest", null);
        tempFile.toFile().deleteOnExit();

        ReadableByteChannel source = Channels.newChannel(new ByteArrayInputStream(data));
        try (AsynchronousByteChannel destination = IOUtils.toAsynchronousByteChannel(
            AsynchronousFileChannel.open(tempFile, StandardOpenOption.WRITE), 0)) {
            AsynchronousByteChannel paritialWriteDestination = new PartialWriteAsynchronousChannel(destination);
            IOUtils.transferAsync(source, paritialWriteDestination).block();
        }

        assertArrayEquals(data, Files.readAllBytes(tempFile));
    }

    /**
     * This channel simulates cases where channel won't consume whole buffer.
     */
    private static final class PartialWriteChannel implements WritableByteChannel {
        private final WritableByteChannel delegate;

        private PartialWriteChannel(WritableByteChannel delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (src.remaining() > 1) {
                byte[] partialCopy = new byte[src.remaining() - 1];
                src.get(partialCopy);
                return delegate.write(ByteBuffer.wrap(partialCopy));
            } else {
                return delegate.write(src);
            }
        }

        @Override
        public boolean isOpen() {
            return delegate.isOpen();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static final class PartialWriteAsynchronousChannel implements AsynchronousByteChannel {

        private final AsynchronousByteChannel delegate;

        private PartialWriteAsynchronousChannel(AsynchronousByteChannel delegate) {
            this.delegate = Objects.requireNonNull(delegate);
        }

        @Override
        public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Future<Integer> read(ByteBuffer dst) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
            if (src.remaining() > 1) {
                byte[] partialCopy = new byte[src.remaining() - 1];
                src.get(partialCopy);
                delegate.write(ByteBuffer.wrap(partialCopy), attachment, handler);
            } else {
                delegate.write(src, attachment, handler);
            }
        }

        @Override
        public Future<Integer> write(ByteBuffer src) {
            if (src.remaining() > 1) {
                byte[] partialCopy = new byte[src.remaining() - 1];
                src.get(partialCopy);
                return delegate.write(ByteBuffer.wrap(partialCopy));
            } else {
                return delegate.write(src);
            }
        }

        @Override
        public boolean isOpen() {
            return delegate.isOpen();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
