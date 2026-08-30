// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.common.ParallelTransferOptions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChunkedDownloadUtilsTests {
    @Test
    void downloadFirstChunkQuotesRetrievedETag() {
        BlobDownloadHeaders headers = new BlobDownloadHeaders().setETag("0x8DABC").setContentRange("bytes 0-3/8");
        BlobDownloadAsyncResponse response
            = new BlobDownloadAsyncResponse(null, 206, new HttpHeaders(), Flux.<ByteBuffer>empty(), headers);

        Tuple3<Long, BlobRequestConditions, BlobDownloadAsyncResponse> result = ChunkedDownloadUtils
            .downloadFirstChunk(new BlobRange(0), new ParallelTransferOptions().setBlockSizeLong(4L),
                new BlobRequestConditions(), (range, conditions) -> Mono.just(response), true)
            .block();

        assertNotNull(result);
        assertEquals("\"0x8DABC\"", result.getT2().getIfMatch());
    }
}
