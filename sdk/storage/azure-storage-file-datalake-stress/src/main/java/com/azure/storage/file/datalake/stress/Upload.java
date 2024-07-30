// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.Utility;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.options.FileParallelUploadOptions;
import com.azure.storage.file.datalake.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class Upload extends DataLakeScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final DataLakeFileClient syncClient;
    private final DataLakeFileAsyncClient asyncClient;
    private final DataLakeFileAsyncClient asyncNoFaultClient;

    public Upload(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncFileSystemClient().getFileClient(fileName);
        this.asyncClient = getAsyncFileSystemClient().getFileAsyncClient(fileName);
        this.asyncNoFaultClient = getAsyncFileSystemClientNoFault().getFileAsyncClient(fileName);
    }

    @Override
    protected void runInternal(Context span) {
        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getContentHead(), options.getSize())) {
            syncClient.uploadWithResponse(new FileParallelUploadOptions(inputStream)
                .setParallelTransferOptions(new ParallelTransferOptions()
                    .setMaxSingleUploadSizeLong(4 * 1024 * 1024L).setMaxConcurrency(1)),
                null, span);
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        Flux<ByteBuffer> byteBufferFlux = new CrcInputStream(originalContent.getContentHead(), options.getSize())
            .convertStreamToByteBuffer();
        return asyncClient.uploadWithResponse(new FileParallelUploadOptions(byteBufferFlux)
                .setParallelTransferOptions(new ParallelTransferOptions().setMaxSingleUploadSizeLong(4 * 1024 * 1024L)))
            .then(originalContent.checkMatch(byteBufferFlux, span));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(originalContent.setupFile(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(super.cleanupAsync());
    }
}
