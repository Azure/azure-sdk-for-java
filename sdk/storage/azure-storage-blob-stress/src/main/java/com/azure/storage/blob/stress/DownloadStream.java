// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.stress.CrcOutputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class DownloadStream extends BlobScenarioBase<StorageStressOptions> {
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(DownloadStream.class);
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public DownloadStream(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(options.getBlobName());
        this.syncClient = getSyncContainerClient().getBlobClient(options.getBlobName());
        this.asyncClient = getAsyncContainerClient().getBlobAsyncClient(options.getBlobName());
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        try (CrcOutputStream outputStream = new CrcOutputStream()) {
            syncClient.downloadStreamWithResponse(outputStream, null, null, null, false, null, span);
            outputStream.close();
            ORIGINAL_CONTENT.checkMatch(outputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return asyncClient.downloadStreamWithResponse(null, null, null, false)
            .flatMap(response -> ORIGINAL_CONTENT.checkMatch(response.getValue(), span));
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(ORIGINAL_CONTENT.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(super.globalCleanupAsync());
    }
}
