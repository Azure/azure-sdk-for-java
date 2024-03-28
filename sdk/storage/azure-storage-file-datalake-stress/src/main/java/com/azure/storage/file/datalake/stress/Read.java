// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress;

import com.azure.core.util.Context;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class Read extends DataLakeScenarioBase<StorageStressOptions> {
    private final OriginalContent originalContent = new OriginalContent();
    private final DataLakeFileClient syncClient;
    private final DataLakeFileAsyncClient asyncClient;
    private final DataLakeFileAsyncClient asyncNoFaultClient;

    public Read(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncFileSystemClient().getFileClient(fileName);
        this.asyncClient = getAsyncFileSystemClient().getFileAsyncClient(fileName);
        this.asyncNoFaultClient = getAsyncFileSystemClientNoFault().getFileAsyncClient(fileName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        try (CrcOutputStream outputStream = new CrcOutputStream()) {
            syncClient.readWithResponse(outputStream, null, null, null, false, null, span);
            outputStream.close();
            originalContent.checkMatch(outputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return asyncClient.readWithResponse(null, null, null, false)
            .flatMap(response -> originalContent.checkMatch(response.getValue(), span));
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
