// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class StorageSeekableByteChannelBlobReadBehaviorTests extends BlobTestBase {
    private BlockBlobClient blockBlobClient;
    private PageBlobClient pageBlobClient;
    private AppendBlobClient appendBlobClient;

    @BeforeEach
    public void setup() {
        blockBlobClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
        pageBlobClient = cc.getBlobClient(generateBlobName()).getPageBlobClient();
        appendBlobClient = cc.getBlobClient(generateBlobName()).getAppendBlobClient();
    }

    @AfterEach
    public void cleanup() {
        cc.deleteIfExists();
    }

    private BlobDownloadResponse createMockDownloadResponse(String contentRange) {
        String contentRangeHeader = "Content-Range";
        Map<String, String> headers = new HashMap<>();
        headers.put(contentRangeHeader, contentRange);
        return new BlobDownloadResponse(new BlobDownloadAsyncResponse(null, 206, new HttpHeaders(headers), null,
            new BlobDownloadHeaders().setContentRange(contentRange)));
    }

    @ParameterizedTest
    @MethodSource("readCallsToClientCorrectlySupplier")
    public void readCallsToClientCorrectly(long offset, int bufferSize, BlobRequestConditions conditions)
        throws IOException {
        BlobClientBase client = Mockito.mock(BlobClientBase.class);
        ArgumentCaptor<BlobRange> blobRangeCaptor = ArgumentCaptor.forClass(BlobRange.class);
        Mockito.when(client.downloadStreamWithResponse(any(), any(), any(), any(), anyBoolean(), any(), any()))
            .thenReturn(
                createMockDownloadResponse("bytes " + offset + "-" + (offset + bufferSize - 1) + "/" + Constants.MB));

        StorageSeekableByteChannelBlobReadBehavior behavior = new StorageSeekableByteChannelBlobReadBehavior(client,
            ByteBuffer.allocate(0), -1, Constants.MB, conditions);

        // when: "ReadBehavior.read() called"
        behavior.read(ByteBuffer.allocate(bufferSize), offset);

        // then: "Expected ShareFileClient download parameters given"
        verify(client, times(1)).downloadStreamWithResponse(any(), blobRangeCaptor.capture(), any(), eq(conditions),
            eq(false), any(), any());

        BlobRange range = blobRangeCaptor.getValue();
        assertEquals(offset, range.getOffset());
        assertEquals(bufferSize, range.getCount());
    }

    private static Stream<Arguments> readCallsToClientCorrectlySupplier() {
        return Stream.of(Arguments.of(0, Constants.KB, null), Arguments.of(50, Constants.KB, null),
            Arguments.of(0, 2000, null), Arguments.of(0, Constants.KB, new BlobRequestConditions()));
    }

    @ParameterizedTest
    @MethodSource("readUsesCacheCorrectlySupplier")
    void readUsesCacheCorrectly(long offset, int bufferSize, int cacheSize) throws Exception {
        // given: "Behavior with a starting cached response"
        BlobClientBase client = Mockito.mock(BlobClientBase.class);
        ByteBuffer initialCache = getRandomData(cacheSize);
        StorageSeekableByteChannelBlobReadBehavior behavior
            = new StorageSeekableByteChannelBlobReadBehavior(client, initialCache, offset, Constants.MB, null);

        // Stubbing downloadStreamWithResponse before any read call
        Mockito
            .when(client.downloadStreamWithResponse(Mockito.any(),
                Mockito.argThat(range -> range.getOffset() == offset && range.getCount().intValue() == bufferSize),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean(), Mockito.any(), Mockito.any()))
            .thenAnswer(invocation -> {
                OutputStream os = invocation.getArgument(0);
                BlobRange range = invocation.getArgument(1);
                os.write(getRandomData(range.getCount().intValue()).array());
                return createMockDownloadResponse(
                    "bytes " + offset + "-" + (offset + bufferSize - 1) + "/" + Constants.MB);
            });
        // when: "ReadBehavior.read() called at offset of cache"
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        int read1 = behavior.read(buffer, offset);

        // then: "Cache used"
        Mockito.verify(client, Mockito.times(0))
            .downloadStreamWithResponse(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.anyBoolean(), Mockito.any(), Mockito.any());
        assertEquals(Math.min(bufferSize, cacheSize), read1);
        byte[] actual = new byte[read1];
        buffer.flip();
        buffer.get(actual);
        byte[] expected = new byte[read1];
        initialCache.get(expected);
        TestUtils.assertArraysEqual(expected, actual);

        // when: "Read again at same offset"
        buffer.clear();
        int read2 = behavior.read(buffer, offset);

        // then: "Client read because cache was cleared after use"
        assertEquals(bufferSize, read2);
        assertEquals(read2, buffer.position());
    }

    static Stream<Arguments> readUsesCacheCorrectlySupplier() {
        return Stream.of(Arguments.of(0L, Constants.KB, Constants.KB), Arguments.of(50L, Constants.KB, Constants.KB),
            Arguments.of(0L, 2 * Constants.KB, Constants.KB), Arguments.of(0L, Constants.KB, 2 * Constants.KB));
    }

    @ParameterizedTest
    @MethodSource("readGracefulPastEndOfBlobSupplier")
    public void readGracefulPastEndOfBlob(String type, long fileSize, int offset, int readSize, int expectedRead)
        throws IOException {
        byte[] data = getRandomByteArray((int) fileSize);
        BlobClientBase client;

        // given: "Selected client type initialized"
        switch (type) {
            case "block":
                blockBlobClient.upload(BinaryData.fromBytes(data));
                client = blockBlobClient;
                break;

            case "page":
                pageBlobClient.create(fileSize);
                pageBlobClient.uploadPages(new PageRange().setStart(0).setEnd(fileSize - 1),
                    new ByteArrayInputStream(data));
                client = pageBlobClient;
                break;

            case "append":
                appendBlobClient.create();
                appendBlobClient.appendBlock(new ByteArrayInputStream(data), fileSize);
                client = appendBlobClient;
                break;

            default:
                throw new RuntimeException("Bad test input");
        }

        // and: "behavior to target it"
        StorageSeekableByteChannelBlobReadBehavior behavior
            = new StorageSeekableByteChannelBlobReadBehavior(client, ByteBuffer.allocate(0), -1, fileSize, null);

        // when: "ReadBehavior.read() called"
        ByteBuffer buffer = ByteBuffer.allocate(readSize);
        int read = behavior.read(buffer, offset);

        // and: "correct amount read"
        assertEquals(expectedRead, read);
        assertEquals(Math.max(expectedRead, 0), buffer.position());
        assertEquals(fileSize, behavior.getResourceLength());

        // and: "if applicable, correct data read"
        if (offset < fileSize) {
            assertEquals(expectedRead, buffer.position());
            buffer.limit(buffer.position());
            buffer.rewind();
            byte[] validBufferContent = new byte[buffer.limit()];
            buffer.get(validBufferContent);

            TestUtils.assertArraysEqual(data, offset, validBufferContent, 0, validBufferContent.length);
        }
    }

    private static Stream<Arguments> readGracefulPastEndOfBlobSupplier() {
        return Stream.of(Arguments.of("block", Constants.KB, 0, 2 * Constants.KB, Constants.KB),
            Arguments.of("block", Constants.KB, 600, Constants.KB, Constants.KB - 600),
            Arguments.of("block", Constants.KB, Constants.KB, Constants.KB, -1),
            Arguments.of("block", Constants.KB, Constants.KB + 20, Constants.KB, -1),
            Arguments.of("page", Constants.KB, 0, 2 * Constants.KB, Constants.KB),
            Arguments.of("page", Constants.KB, 600, Constants.KB, Constants.KB - 600),
            Arguments.of("page", Constants.KB, Constants.KB, Constants.KB, -1),
            Arguments.of("page", Constants.KB, Constants.KB + 20, Constants.KB, -1),
            Arguments.of("append", Constants.KB, 0, 2 * Constants.KB, Constants.KB),
            Arguments.of("append", Constants.KB, 600, Constants.KB, Constants.KB - 600),
            Arguments.of("append", Constants.KB, Constants.KB, Constants.KB, -1),
            Arguments.of("append", Constants.KB, Constants.KB + 20, Constants.KB, -1));
    }

    @Test
    void readDetectsBlobGrowth() throws IOException {
        // Given: data
        int halfLength = 512;
        byte[] data = getRandomByteArray(2 * halfLength);

        // Blob at half size
        String blockId1 = new String(Base64.getEncoder().encode("blockId1".getBytes()));
        blockBlobClient.stageBlock(blockId1, BinaryData.fromBytes(Arrays.copyOfRange(data, 0, halfLength)));
        blockBlobClient.commitBlockList(Collections.singletonList(blockId1));

        // behavior to read blob
        StorageSeekableByteChannelBlobReadBehavior behavior = new StorageSeekableByteChannelBlobReadBehavior(
            blockBlobClient, ByteBuffer.allocate(0), -1, halfLength, null);
        ByteBuffer buffer = ByteBuffer.allocate(halfLength);

        // entire blob initially read
        int read = behavior.read(buffer, 0);

        // behavior state as expected
        assertEquals(halfLength, read);
        assertEquals(halfLength, behavior.getResourceLength());

        // buffer correctly filled
        assertEquals(buffer.capacity(), buffer.position());
        TestUtils.assertArraysEqual(data, 0, buffer.array(), 0, halfLength);

        // read at end of blob
        buffer.clear();
        read = behavior.read(buffer, halfLength);

        // gracefully signal end of blob
        assertEquals(-1, read);
        assertEquals(halfLength, behavior.getResourceLength());

        // buffer unfilled
        assertEquals(0, buffer.position());

        // blob augmented to full size
        String blockId2 = new String(Base64.getEncoder().encode("blockId2".getBytes()));
        blockBlobClient.stageBlock(blockId2, BinaryData.fromBytes(Arrays.copyOfRange(data, halfLength, data.length)));
        blockBlobClient.commitBlockList(Arrays.asList(blockId1, blockId2), true);

        // behavior reads at previous EOF
        buffer.clear();
        read = behavior.read(buffer, behavior.getResourceLength());

        // channel state has updated length
        assertEquals(halfLength, read);
        assertEquals(2 * halfLength, behavior.getResourceLength());

        // buffer correctly filled
        assertEquals(buffer.capacity(), buffer.position());
        TestUtils.assertArraysEqual(data, halfLength, buffer.array(), 0, data.length - halfLength);
    }
}
