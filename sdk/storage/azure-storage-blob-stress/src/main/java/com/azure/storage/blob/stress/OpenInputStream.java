// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.azure.core.util.FluxUtil.monoError;

public class OpenInputStream extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(OpenInputStream.class);
    private static final TelemetryHelper TELEMETRY_HELPER = new TelemetryHelper(OpenInputStream.class);
    private static final OriginalContent ORIGINAL_CONTENT = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;


    public OpenInputStream(StorageStressOptions options) {
        super(options, TELEMETRY_HELPER);
        this.syncClient = getSyncContainerClient().getBlobClient(options.getBlobName());
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(options.getBlobName());
    }

    @Override
    protected boolean runInternal(Context span) throws IOException {
        try (InputStream stream = syncClient.openInputStream()) {
            try (CrcInputStream crcStream = new CrcInputStream(stream)) {
                byte[] buffer = new byte[8192];
                while (crcStream.read(buffer) != -1) {
                    // do nothing
                }
                return ORIGINAL_CONTENT.checkMatch(crcStream.getContentInfo(), span).block();
            }
        }
    }

    @Override
    protected Mono<Boolean> runInternalAsync(Context context) {
        return monoError(LOGGER, new RuntimeException("openInputStream() does not exist on the async client"));
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
