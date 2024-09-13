// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSeekableByteChannelReadResult;
import com.azure.storage.blob.models.ConsistentReadControl;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageSeekableByteChannel;
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BlobSeekableByteChannelTests extends BlobTestBase {

    BlobClient bc;
    BlockBlobClient blockClient;

    @BeforeEach
    public void setup() {
        bc = cc.getBlobClient(generateBlobName());
        blockClient = cc.getBlobClient(generateBlobName()).getBlockBlobClient();
    }

    @ParameterizedTest
    @MethodSource("channelReadDataSupplier")
    public void e2EChannelRead(int streamBufferSize, int copyBufferSize, int dataLength) throws IOException {
        // given: "uploaded blob"
        byte[] data = getRandomByteArray(dataLength);
        bc.upload(BinaryData.fromBytes(data));

        // when: "Channel initialized"
        BlobSeekableByteChannelReadResult result = bc.openSeekableByteChannelRead(
            new BlobSeekableByteChannelReadOptions().setReadSizeInBytes(streamBufferSize), null);
        SeekableByteChannel channel = result.getChannel();

        // then: "Channel initialized to position zero"
        assertEquals(0, channel.position());
        assertNotNull(result.getProperties());
        assertEquals(data.length, result.getProperties().getBlobSize());

        // when: "read from channel"
        ByteArrayOutputStream downloadedData = new ByteArrayOutputStream();
        int copied = copy(channel, downloadedData, copyBufferSize);

        // then: "channel position updated accordingly"
        assertEquals(dataLength, copied);
        assertEquals(dataLength, channel.position());

        // and: "expected data downloaded"
        TestUtils.assertArraysEqual(data, downloadedData.toByteArray());
    }

    static Stream<Arguments> channelReadDataSupplier() {
        return Stream.of(
            Arguments.of(50, 40, Constants.KB),
            Arguments.of(Constants.KB + 50, 40, Constants.KB)
        );
    }

    /**
     * Copies the InputStream contents to the destination byte channel.
     * @param src Bytes source.
     * @param dst Bytes destination.
     * @param copySize Size of array to copy contents with.
     * @return Total number of bytes read from src.
     */
    private static int copy(SeekableByteChannel src, OutputStream dst, int copySize) throws IOException {
        int read;
        int totalRead = 0;
        byte[] temp = new byte[copySize];
        ByteBuffer bb = ByteBuffer.wrap(temp);
        while ((read = src.read(bb)) != -1) {
            totalRead += read;
            dst.write(temp, 0, read);
            bb.clear();
        }
        return totalRead;
    }

    /**
     * Copies the InputStream contents to the destination byte channel.
     * @param src Bytes source.
     * @param dst Bytes destination.
     * @param copySize Size of array to copy contents with.
     * @return Total number of bytes read from src.
     */
    private static int copy(InputStream src, SeekableByteChannel dst, int copySize) throws IOException {
        int read;
        int totalRead = 0;
        byte[] temp = new byte[copySize];
        while ((read = src.read(temp)) != -1) {
            totalRead += read;
            int written = 0;
            while (written < read) {
                written += dst.write(ByteBuffer.wrap(temp, written, read - written));
            }
        }
        return totalRead;
    }

    @LiveOnly
    @ParameterizedTest
    @MethodSource("channelReadDataSupplier")
    public void e2EChannelWriteBlock(int streamBufferSize, int copyBufferSize, int dataLength) throws IOException {
        SeekableByteChannel channel = blockClient.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(OVERWRITE).setBlockSizeInBytes((long) streamBufferSize));

        // then: "Channel initialized to position zero"
        assertEquals(0, channel.position());

        // when: "write to channel"
        byte[] data = getRandomByteArray(dataLength);
        int copied = copy(new ByteArrayInputStream(data), channel, copyBufferSize);

        // then: "channel position updated accordingly"
        assertEquals(dataLength, copied);
        assertEquals(dataLength, channel.position());

        // when: "channel flushed"
        channel.close();

        // then: "appropriate data uploaded"
        TestUtils.assertArraysEqual(data, blockClient.downloadContent().toBytes());
    }

    @ParameterizedTest
    @CsvSource({
        "1024, " + Integer.MAX_VALUE,
        "1024, " + (Integer.MAX_VALUE + 1000L),
        "1024, " + (Long.MAX_VALUE / 2)
    })
    public void supportsGreaterThanMaxIntBlobSize(int toRead, long offset) throws Exception {
        // Given: "data"
        long blobSize = Long.MAX_VALUE;
        ByteBuffer data = getRandomData(toRead);

        // And: "read behavior to blob where length > maxint"
        BlobClientBase client = mock(BlobClientBase.class);
        StorageSeekableByteChannelBlobReadBehavior behavior =
            new StorageSeekableByteChannelBlobReadBehavior(client, ByteBuffer.allocate(0), -1, blobSize, null);

        // And: "StorageSeekableByteChannel"
        StorageSeekableByteChannel channel = new StorageSeekableByteChannel(toRead, behavior, 0);

        // When: "seek"
        channel.position(offset);

        // Then: "position set"
        assertEquals(offset, channel.position());

        when(client.downloadStreamWithResponse(any(),
            argThat(r -> r != null && r.getOffset() == offset && r.getCount() == toRead),
            any(), any(), anyBoolean(), any(), any()))
            .thenAnswer((InvocationOnMock invocation) -> {
                OutputStream os = invocation.getArgument(0);
                os.write(data.array());
                String contentRange = "bytes " + offset + "-" + (offset + toRead - 1) + "/" + blobSize;
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaderName.CONTENT_RANGE, contentRange);
                return new BlobDownloadResponse(new BlobDownloadAsyncResponse(null, 206, headers, null,
                    new BlobDownloadHeaders().setContentRange(contentRange)));
            });

        // When: "read"
        ByteBuffer readBuffer = ByteBuffer.allocate(toRead);
        int read = channel.read(readBuffer);

        // Then: "appropriate data read"
        assertEquals(toRead, read);
        TestUtils.assertArraysEqual(data.array(), readBuffer.array());

        doThrow(new RuntimeException("Incorrect parameters")).when(client)
            .downloadStreamWithResponse(any(), any(), any(), any(), anyBoolean(), any(), any());

        // And: "channel position updated"
        assertEquals(offset + toRead, channel.position());
    }

    @ParameterizedTest
    @MethodSource("channelReadModeDataSupplier")
    public void clientCreatesAppropriateChannelReadMode(BlobRequestConditions conditions, Integer blockSize,
        ConsistentReadControl control, Long position) throws IOException {
        BlobContainerClient versionedCC = versionedBlobServiceClient.getBlobContainerClient(generateContainerName());
        versionedCC.create();
        bc = versionedCC.getBlobClient(generateBlobName());

        // when: "make channel in read mode"
        bc.upload(BinaryData.fromBytes(getRandomByteArray(1024)));
        StorageSeekableByteChannel channel = (StorageSeekableByteChannel) bc.openSeekableByteChannelRead(
            new BlobSeekableByteChannelReadOptions()
                .setRequestConditions(conditions)
                .setReadSizeInBytes(blockSize)
                .setConsistentReadControl(control)
                .setInitialPosition(position),
            null
        ).getChannel();

        // then: "channel WriteBehavior is null"
        assertNull(channel.getWriteBehavior());

        // and: "channel ReadBehavior has appropriate values"
        StorageSeekableByteChannelBlobReadBehavior readBehavior =
            (StorageSeekableByteChannelBlobReadBehavior) channel.getReadBehavior();
        assertNotNull(readBehavior.getClient());

        if (conditions != null) {
            assertEquals(conditions, readBehavior.getRequestConditions());
        }

        if (control == null || control == ConsistentReadControl.ETAG) {
            assertNotNull(readBehavior.getRequestConditions());
            assertNotNull(readBehavior.getRequestConditions().getIfMatch());
        } else if (control == ConsistentReadControl.VERSION_ID) {
            assertNotEquals(readBehavior.getClient(), bc);
            assertNotNull(readBehavior.getClient().getVersionId());
            assertEquals(conditions, readBehavior.getRequestConditions());
        } else {
            assertEquals(conditions, readBehavior.getRequestConditions());
        }

        // and: "channel has appropriate values"
        assertEquals(blockSize == null ? 4 * Constants.MB : blockSize, channel.getChunkSize());
        assertEquals(position == null ? 0 : position, channel.position());

        versionedCC.delete();
    }

    static Stream<Arguments> channelReadModeDataSupplier() {
        return Stream.of(
            Arguments.of(null, null, null, null),
            Arguments.of(new BlobRequestConditions(), null, ConsistentReadControl.NONE, null),
            Arguments.of(new BlobRequestConditions(), null, ConsistentReadControl.ETAG, null),
            Arguments.of(new BlobRequestConditions(), null, ConsistentReadControl.VERSION_ID, null),
            Arguments.of(null, 500, null, null),
            Arguments.of(null, null, ConsistentReadControl.NONE, null),
            Arguments.of(null, null, ConsistentReadControl.ETAG, null),
            Arguments.of(null, null, ConsistentReadControl.VERSION_ID, null),
            Arguments.of(null, null, null, 800L)
        );
    }

    @ParameterizedTest
    @MethodSource("channelWriteModeDataSupplier")
    public void clientCreatesAppropriateChannelWriteModeBlock(
        BlockBlobSeekableByteChannelWriteOptions.WriteMode writeMode, Integer blockSize,
        BlobHttpHeaders headers, Map<String, String> metadata, Map<String, String> tags, AccessTier tier,
        BlobRequestConditions conditions) throws IOException {
        // when: "make channel in write mode"
        StorageSeekableByteChannel channel = (StorageSeekableByteChannel) blockClient.openSeekableByteChannelWrite(
            new BlockBlobSeekableByteChannelWriteOptions(writeMode)
                .setBlockSizeInBytes(blockSize != null ? Long.valueOf(blockSize) : null)
                .setHeaders(headers)
                .setMetadata(metadata)
                .setTags(tags)
                .setTier(tier)
                .setRequestConditions(conditions)
        );

        // then: "channel ReadBehavior is null"
        assertNull(channel.getReadBehavior());

        // and: "channel WriteBehavior has appropriate values"
        StorageSeekableByteChannelBlockBlobWriteBehavior writeBehavior =
            (StorageSeekableByteChannelBlockBlobWriteBehavior) channel.getWriteBehavior();
        assertEquals(writeMode.toString().toLowerCase(), writeBehavior.getWriteMode().toString().toLowerCase());
        assertEquals(headers, writeBehavior.getHeaders());
        assertEquals(metadata, writeBehavior.getMetadata());
        assertEquals(tags, writeBehavior.getTags());
        assertEquals(tier, writeBehavior.getTier());
        assertEquals(conditions, writeBehavior.getRequestConditions());

        // and: "channel has appropriate values"
        assertEquals((blockSize == null ? 4 * Constants.MB : blockSize), channel.getChunkSize());
        assertEquals(0, channel.position());
    }

    static Stream<Arguments> channelWriteModeDataSupplier() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        return Stream.of(
            Arguments.of(OVERWRITE, null, null, null, null, null, null),
            Arguments.of(OVERWRITE, 500, null, null, null, null, null),
            Arguments.of(OVERWRITE, null, new BlobHttpHeaders(), null, null, null, null),
            Arguments.of(OVERWRITE, null, null, metadata, null, null, null),
            Arguments.of(OVERWRITE, null, null, null, metadata, null, null),
            Arguments.of(OVERWRITE, null, null, null, null, AccessTier.COOL, null),
            Arguments.of(OVERWRITE, null, null, null, null, null, new BlobRequestConditions())
        );
    }

}
