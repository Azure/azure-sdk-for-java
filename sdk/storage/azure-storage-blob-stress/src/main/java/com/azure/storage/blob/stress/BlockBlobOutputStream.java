// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static com.azure.core.util.FluxUtil.monoError;

public class BlockBlobOutputStream extends BlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(BlockBlobOutputStream.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;

    public BlockBlobOutputStream(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        BlockBlobClient blockBlobClient = syncClient.getBlockBlobClient();

        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize());
             BlobOutputStream outputStream = blockBlobClient.getBlobOutputStream(null, span)) {
            byte[] buffer = new byte[4096]; // Define a buffer
            int bytesRead;

            // Read from the inputStream and write to the blobOutputStream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Ensure to close the blobOutputStream to flush any remaining data and finalize the blob.
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
        return super.setupAsync().then(originalContent.setupBlob(asyncNoFaultClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.delete()
            .then(super.cleanupAsync());
    }

}
