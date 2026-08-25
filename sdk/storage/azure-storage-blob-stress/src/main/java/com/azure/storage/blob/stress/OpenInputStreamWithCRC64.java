// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Open input stream with CRC64 Algorithm enabled (sync only).
 * Verifies the correctness of the download response content via CRC.
 */
public class OpenInputStreamWithCRC64 extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(OpenInputStreamWithCRC64.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public OpenInputStreamWithCRC64(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        try (InputStream stream = syncClient.openInputStream(
            new BlobInputStreamOptions()
                .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64),
            span)) {
            try (CrcInputStream crcStream = new CrcInputStream(stream)) {
                byte[] buffer = new byte[8192];
                while (crcStream.read(buffer) != -1) {
                    // do nothing
                }
                originalContent.checkMatch(crcStream.getContentInfo(), span).block();
            }
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return monoError(LOGGER, new RuntimeException("openInputStream() does not exist on the async client"));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists()
            .then(super.cleanupAsync());
    }
}
