// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.ParallelTransferOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChunkedDownloadUtilsTests {
    OffsetDateTime ifModifiedSince;
    OffsetDateTime ifUnmodifiedSince;
    BlobRequestConditions requestConditions;
    BlobDownloadAsyncResponse response;

    @BeforeEach
    void setup() {
        ifModifiedSince = OffsetDateTime.now().minusDays(1);
        ifUnmodifiedSince = OffsetDateTime.now();
        requestConditions
            = new BlobRequestConditions().setIfModifiedSince(ifModifiedSince).setIfUnmodifiedSince(ifUnmodifiedSince);
        BlobDownloadHeaders headers = new BlobDownloadHeaders().setETag("0x8DABC").setContentRange("bytes 0-3/8");
        response = new BlobDownloadAsyncResponse(null, 206, new HttpHeaders(), Flux.<ByteBuffer>empty(), headers);

    }

    @Test
    void downloadFirstChunkPreservesRetrievedETagUntilHttpBoundary() {
        requestConditions.setIfNoneMatch("*").setLeaseId("leaseId");

        Tuple3<Long, BlobRequestConditions, BlobDownloadAsyncResponse> result
            = ChunkedDownloadUtils
                .downloadFirstChunk(new BlobRange(0), new ParallelTransferOptions().setBlockSizeLong(4L),
                    requestConditions, (range, conditions) -> Mono.just(response), true)
                .block();

        assertNotNull(result);
        assertEquals("0x8DABC", result.getT2().getIfMatch());
        assertEquals(ifModifiedSince, result.getT2().getIfModifiedSince());
        assertEquals(ifUnmodifiedSince, result.getT2().getIfUnmodifiedSince());
        assertEquals("*", result.getT2().getIfNoneMatch());
        assertEquals("leaseId", result.getT2().getLeaseId());
    }

    @Test
    void downloadFirstChunkUsesIfUnmodifiedSinceFromRequestConditions() {
        Tuple3<Long, BlobRequestConditions, BlobDownloadAsyncResponse> result
            = ChunkedDownloadUtils
                .downloadFirstChunk(new BlobRange(0), new ParallelTransferOptions().setBlockSizeLong(4L),
                    requestConditions, (range, conditions) -> Mono.just(response), true)
                .block();

        assertNotNull(result);
        assertEquals("0x8DABC", result.getT2().getIfMatch());
        assertEquals(ifModifiedSince, result.getT2().getIfModifiedSince());
        assertEquals(ifUnmodifiedSince, result.getT2().getIfUnmodifiedSince());
    }
}
