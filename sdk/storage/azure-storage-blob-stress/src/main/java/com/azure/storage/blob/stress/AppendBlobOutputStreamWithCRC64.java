// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.core.util.Context;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.blob.options.AppendBlobOutputStreamOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Append blob output stream with CRC64 enabled (sync only).
 */
public class AppendBlobOutputStreamWithCRC64 extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(AppendBlobOutputStreamWithCRC64.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    /** Separate blob used to upload reference content for {@link OriginalContent} checksum (block blob). */
    private final BlobAsyncClient tempSetupBlobClient;

    public AppendBlobOutputStreamWithCRC64(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        String tempBlobName = generateBlobName();

        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.tempSetupBlobClient = getAsyncContainerClientNoFault().getBlobAsyncClient(tempBlobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        AppendBlobClient appendBlobClient = syncClient.getAppendBlobClient();
        // Reset the append blob at the start of each iteration. The boolean overload
        // getBlobOutputStream(true) does this implicitly via create(true); the options overload
        // does not, so we replicate that behavior here. Without this reset, fault-injection
        // sequences that commit a block server-side but drop the response leave the cached
        // appendPosition stale, causing subsequent retries to fail with 412 AppendPositionConditionNotMet,
        // which combined with non-retriable Crc64Mismatch on truncated-body faults collapses the pass rate.
        appendBlobClient.create(true);

        AppendBlobOutputStreamOptions streamOptions = new AppendBlobOutputStreamOptions()
            .setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);

        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize());
             BlobOutputStream outputStream = appendBlobClient.getBlobOutputStream(streamOptions)) {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            originalContent.checkMatch(inputStream.getContentInfo(), span).block();
        }
    }

    @Override
    protected Mono<Void> runInternalAsync(Context span) {
        return monoError(LOGGER, new RuntimeException("getBlobOutputStream() does not exist on the async client"));
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(asyncNoFaultClient.getAppendBlobAsyncClient().create())
            .then(originalContent.setupBlob(tempSetupBlobClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.deleteIfExists()
            .then(tempSetupBlobClient.deleteIfExists())
            .then(super.cleanupAsync());
    }
}
