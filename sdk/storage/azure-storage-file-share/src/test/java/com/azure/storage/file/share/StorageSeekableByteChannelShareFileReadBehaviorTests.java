// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.Context;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.implementation.AzureFileStorageImpl;
import com.azure.storage.file.share.models.ShareFileDownloadAsyncResponse;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileDownloadResponse;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.options.ShareFileDownloadOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StorageSeekableByteChannelShareFileReadBehaviorTests extends FileShareTestBase {
    private ShareFileClient primaryFileClient;
    ShareClient shareClient;
    String shareName;
    String filePath;

    @BeforeEach
    public void setup() {
        shareName = generateShareName();
        filePath = generatePathName();
        shareClient = shareBuilderHelper(shareName).buildClient();
        primaryFileClient = fileBuilderHelper(shareName, filePath).buildFileClient();
    }

    @AfterEach
    public void cleanup() {
        shareClient.deleteIfExists();
    }

    private ShareFileDownloadResponse createMockDownloadResponse(String contentRange) {
        String contentRangeHeader = "Content-Range";
        Map<String, String> headers = new HashMap<>();
        headers.put(contentRangeHeader, contentRange);
        return new ShareFileDownloadResponse(new ShareFileDownloadAsyncResponse(null, 206, new HttpHeaders(headers),
            null, new ShareFileDownloadHeaders().setContentRange(contentRange)));
    }

    private static Stream<Arguments> readCallsToClientCorrectlySupplier() {
        return Stream.of(
            Arguments.of(0, null),
            Arguments.of(50, null),
            Arguments.of(0, new ShareRequestConditions()));
    }

    @ParameterizedTest
    @MethodSource("readCallsToClientCorrectlySupplier")
    public void readCallsToClientCorrectly(int offset, ShareRequestConditions conditions) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.KB);
        AtomicInteger downloadCallCount = new AtomicInteger(0);
        ShareFileClient client = new ShareFileClient(null, new AzureFileStorageImpl(null, null, "fakeurl", false,
            false), "testshare", "testpath", null, null, null, null) {
            @Override
            public ShareFileDownloadResponse downloadWithResponse(OutputStream stream, ShareFileDownloadOptions options,
                Duration timeout, Context context) {
                assertNull(timeout);
                assertNull(context);
                downloadCallCount.incrementAndGet();
                assertEquals(offset, options.getRange().getStart());
                assertEquals(offset + buffer.remaining() - 1, options.getRange().getEnd());
                if (conditions != null) {
                    assertEquals(conditions, options.getRequestConditions());
                }
                return createMockDownloadResponse("bytes " + offset + "-" + (offset + buffer.limit() - 1) + "/4096");
            }
        };

        StorageSeekableByteChannelShareFileReadBehavior behavior =
            new StorageSeekableByteChannelShareFileReadBehavior(client, conditions);

        behavior.read(buffer, offset);

        assertEquals(1, downloadCallCount.get());
    }

    @ParameterizedTest
    @MethodSource("readGracefulPastEndOfFileSupplier")
    public void readGracefulPastEndOfFile(int fileSize, int offset, int readSize, int expectedRead) throws IOException {
        byte[] data = getRandomByteArray(fileSize);
        shareClient.create();
        primaryFileClient.create(fileSize);
        primaryFileClient.upload(new ByteArrayInputStream(data), fileSize, null);

        StorageSeekableByteChannelShareFileReadBehavior behavior =
            new StorageSeekableByteChannelShareFileReadBehavior(primaryFileClient, null);
        ByteBuffer buffer = ByteBuffer.allocate(readSize);

        //when: "ReadBehavior.read() called"
        int read = behavior.read(buffer, offset);

        //and: "correct amount read"
        assertEquals(read, expectedRead);
        assertEquals(buffer.position(), Math.max(expectedRead, 0));
        assertEquals(behavior.getResourceLength(), fileSize);

        //and: "if applicable, correct data read"
        if (offset < fileSize) {
            assertEquals(buffer.position(), expectedRead);
            buffer.limit(buffer.position());
            buffer.rewind();
            byte[] validBufferContent = new byte[buffer.limit()];
            buffer.get(validBufferContent);

            // Assuming validBufferContent is a ByteBuffer and data is a byte array
            assertArrayEquals(validBufferContent, Arrays.copyOfRange(data, offset, data.length));
        }
    }

    private static Stream<Arguments> readGracefulPastEndOfFileSupplier() {
        return Stream.of(
            Arguments.of(Constants.KB, 0, 2 * Constants.KB, Constants.KB), // read larger than file
            Arguments.of(Constants.KB, 500, Constants.KB, Constants.KB - 500), // overlap on end of file
            Arguments.of(Constants.KB, Constants.KB, Constants.KB, -1), // starts at end of file
            Arguments.of(Constants.KB, Constants.KB + 20, Constants.KB, -1) // completely past file
        );
    }

    @Test
    public void readDetectsFileGrowth() throws IOException {
        //given: "data"
        int length = 512;
        byte[] data = getRandomByteArray(2 * length);

        //and: "Storage file at half size"
        shareClient.create();
        primaryFileClient.create(length);
        primaryFileClient.upload(new ByteArrayInputStream(Arrays.copyOfRange(data, 0, length)), length, null);

        //and: "behavior to read file"
        StorageSeekableByteChannelShareFileReadBehavior behavior =
            new StorageSeekableByteChannelShareFileReadBehavior(primaryFileClient, null);
        ByteBuffer buffer = ByteBuffer.allocate(length);

        //when: "entire file initially read"
        int read = behavior.read(buffer, 0);

        //then: "channel state as expected"
        assertEquals(read, length);
        assertEquals(behavior.getResourceLength(), length);

        //and: "buffer correctly filled"
        assertEquals(buffer.position(), buffer.capacity());
        assertArrayEquals(buffer.array(), Arrays.copyOfRange(data, 0, length));

        //when: "read at end of file"
        buffer.clear();
        read = behavior.read(buffer, length);

        //then: "gracefully signal end of file"
        assertEquals(read, -1);
        assertEquals(behavior.getResourceLength(), length);

        //and: "buffer unfilled"
        assertEquals(buffer.position(), 0);

        //when: "file augmented to full size"
        primaryFileClient.setProperties(2 * length, null, null, null);
        primaryFileClient.uploadRangeWithResponse(new ShareFileUploadRangeOptions(
            new ByteArrayInputStream(Arrays.copyOfRange(data, 0, length)), length).setOffset((long) length), null,
            null);

        //and: "behavior reads at previous EOF"
        buffer.clear();
        read = behavior.read(buffer, behavior.getResourceLength());

        //then: "channel state has updated length"
        assertEquals(read, length);
        assertEquals(behavior.getResourceLength(), 2 * length);

        //and: "buffer correctly filled"
        assertEquals(buffer.position(), buffer.capacity());
        assertArrayEquals(buffer.array(), Arrays.copyOfRange(data, 0, length));
    }
}
