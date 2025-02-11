// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StorageFileInputOutputStreamTests extends FileShareTestBase {
    private ShareFileClient fileClient;
    private int length;

    @BeforeEach
    public void setup() {
        String shareName = generateShareName();
        String filePath = generatePathName();
        ShareClient shareClient = shareBuilderHelper(shareName).buildClient();
        shareClient.create();
        fileClient = shareClient.getFileClient(filePath);
    }

    @LiveOnly
    @Test
    public void uploadDownload() throws IOException {
        length = 30 * Constants.MB;
        fileClient.create(length);
        byte[] randomBytes = getRandomByteArray(length);

        StorageFileOutputStream outStream = fileClient.getFileOutputStream();
        outStream.write(randomBytes);
        outStream.close();

        StorageFileInputStream inputStream = fileClient.openInputStream();
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            while ((b = inputStream.read()) != -1) {
                outputStream.write(b);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        byte[] randomBytes2 = outputStream.toByteArray();
        assertArrayEquals(randomBytes2, randomBytes);
    }

    @LiveOnly
    @Test
    public void streamWithOffset() throws IOException {
        length = 7 * Constants.MB;
        fileClient.create(length);
        byte[] randomBytes = getRandomByteArray(9 * Constants.MB);

        StorageFileOutputStream outStream = fileClient.getFileOutputStream();
        outStream.write(randomBytes, 2 * Constants.MB, length);
        outStream.close();

        StorageFileInputStream inputStream = fileClient.openInputStream();
        byte[] b = new byte[length];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (inputStream.read(b) != -1) {
                outputStream.write(b, 0, b.length);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        byte[] randomBytes2 = outputStream.toByteArray();
        assertArrayEquals(randomBytes2, Arrays.copyOfRange(randomBytes, 2 * Constants.MB, 9 * Constants.MB));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPartialResponseHandling() {
        ShareFileAsyncClient mockClient = mock(ShareFileAsyncClient.class);
        StorageFileOutputStream outputStream = new StorageFileOutputStream(mockClient, 0);

        byte[] data = "test data".getBytes();
        int writeLength = data.length;

        when(mockClient.uploadWithResponse(any(Flux.class), eq((long) writeLength), eq(0L)))
            .thenReturn(Mono.error(new IOException()));

        StepVerifier.create(outputStream.dispatchWrite(data, writeLength, 0)).expectError(IOException.class).verify();

        verify(mockClient, times(1)).uploadWithResponse(any(Flux.class), eq((long) writeLength), eq(0L));
    }
}
