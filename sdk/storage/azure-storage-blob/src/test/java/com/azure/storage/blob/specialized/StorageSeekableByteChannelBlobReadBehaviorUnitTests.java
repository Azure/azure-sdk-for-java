// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;

public class StorageSeekableByteChannelBlobReadBehaviorUnitTests {

    private BlobDownloadResponse createMockDownloadResponse(String contentRange) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Range", contentRange);
        return new BlobDownloadResponse(new BlobDownloadAsyncResponse(null, 206, new HttpHeaders(headers), null,
            new BlobDownloadHeaders().setContentRange(contentRange)));
    }

    @ParameterizedTest
    @MethodSource("truncatedErrorResponseAtEofSupplier")
    void readReturnEofWhenErrorResponseTruncatedAtKnownEof(long blobSize) throws IOException {
        BlobClientBase client = Mockito.mock(BlobClientBase.class);
        RuntimeException reactorWrapped = new RuntimeException(new IOException("connection reset by peer"));
        Mockito.when(client.downloadStreamWithResponse(any(), any(), any(), any(), anyBoolean(), any(), any()))
            .thenThrow(reactorWrapped);

        StorageSeekableByteChannelBlobReadBehavior behavior
            = new StorageSeekableByteChannelBlobReadBehavior(client, ByteBuffer.allocate(0), -1, blobSize, null);

        assertEquals(-1, behavior.read(ByteBuffer.allocate(Constants.KB), blobSize));
    }

    private static Stream<Arguments> truncatedErrorResponseAtEofSupplier() {
        return Stream.of(Arguments.of(Constants.KB), Arguments.of(50L * Constants.MB));
    }

    @Test
    void readRethrowsRuntimeExceptionWhenNotAtEof() {
        BlobClientBase client = Mockito.mock(BlobClientBase.class);
        RuntimeException reactorWrapped = new RuntimeException(new IOException("connection reset by peer"));
        Mockito.when(client.downloadStreamWithResponse(any(), any(), any(), any(), anyBoolean(), any(), any()))
            .thenThrow(reactorWrapped);

        StorageSeekableByteChannelBlobReadBehavior behavior
            = new StorageSeekableByteChannelBlobReadBehavior(client, ByteBuffer.allocate(0), -1, Constants.KB, null);

        assertThrows(RuntimeException.class, () -> behavior.read(ByteBuffer.allocate(Constants.KB), 0));
    }

    @Test
    void readPassesNonNullDownloadRetryOptionsToClient() throws IOException {
        BlobClientBase client = Mockito.mock(BlobClientBase.class);
        ArgumentCaptor<DownloadRetryOptions> retryCaptor = ArgumentCaptor.forClass(DownloadRetryOptions.class);
        Mockito.when(client.downloadStreamWithResponse(any(), any(), any(), any(), anyBoolean(), any(), any()))
            .thenReturn(createMockDownloadResponse("bytes 0-1023/1024"));

        StorageSeekableByteChannelBlobReadBehavior behavior
            = new StorageSeekableByteChannelBlobReadBehavior(client, ByteBuffer.allocate(0), -1, Constants.KB, null);
        behavior.read(ByteBuffer.allocate(Constants.KB), 0);

        verify(client).downloadStreamWithResponse(any(), any(), retryCaptor.capture(), any(), anyBoolean(), any(),
            any());
        assertNotNull(retryCaptor.getValue());
    }
}
