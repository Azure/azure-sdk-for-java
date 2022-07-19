// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;


import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;

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
}
