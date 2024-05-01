// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.options.DataLakeFileFlushOptions;
import com.azure.storage.file.datalake.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class Append extends DataLakeScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final DataLakeFileClient syncClient;
    private final DataLakeFileAsyncClient asyncClient;
    private final DataLakeFileAsyncClient asyncNoFaultClient;
    public final DataLakeFileClient syncNoFaultClient;

    public Append(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncFileSystemClient().getFileClient(fileName);
        this.asyncClient = getAsyncFileSystemClient().getFileAsyncClient(fileName);
        this.syncNoFaultClient = getSyncFileSystemClientNoFault().getFileClient(fileName);
        this.asyncNoFaultClient = getAsyncFileSystemClientNoFault().getFileAsyncClient(fileName);
    }

    @Override
    protected void runInternal(Context span) {
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getContentHead(), options.getSize())) {
            // Perform faulted append
            syncClient.appendWithResponse(inputStream, 0, options.getSize(), null, null, null, span);
            // Perform non-faulted flush to write data to the service
            syncNoFaultClient.flushWithResponse(options.getSize(), new DataLakeFileFlushOptions(), null, span);
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        Flux<ByteBuffer> byteBufferFlux = new CrcInputStream(originalContent.getContentHead(), options.getSize())
            .convertStreamToByteBuffer();
        // Perform non-faulted append
        return asyncClient.append(byteBufferFlux, 0, options.getSize())
            // Perform non-faulted flush to write data to the service
            .then(asyncNoFaultClient.flush(options.getSize(), true))
            .then(originalContent.checkMatch(byteBufferFlux, span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupFile(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists().then(super.cleanupAsync());
    }
}
