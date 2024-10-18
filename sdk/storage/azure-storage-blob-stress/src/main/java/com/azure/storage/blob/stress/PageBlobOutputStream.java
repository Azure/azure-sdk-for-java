// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.azure.core.util.FluxUtil.monoError;

public class PageBlobOutputStream extends PageBlobScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(BlockBlobOutputStream.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    // this blob is used to perform normal upload in the setup phase
    private final PageBlobAsyncClient tempSetupPageBlobClient;

    public PageBlobOutputStream(StorageStressOptions options) {
        super(options);
        String blobName = generateBlobName();
        String tempBlobName = generateBlobName();

        this.syncClient = getSyncContainerClient().getBlobClient(blobName);
        this.asyncNoFaultClient = getAsyncContainerClientNoFault().getBlobAsyncClient(blobName);
        BlobAsyncClient tempSetupBlobClient = getAsyncContainerClientNoFault().getBlobAsyncClient(tempBlobName);
        this.tempSetupPageBlobClient = tempSetupBlobClient.getPageBlobAsyncClient();
    }

    @Override
    protected void runInternal(Context span) throws IOException {
        PageBlobClient pageBlobClient = syncClient.getPageBlobClient();

        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize());
             BlobOutputStream outputStream = pageBlobClient.getBlobOutputStream(new PageRange().setStart(0)
            .setEnd(options.getSize() - 1))) {
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[512]; // Use 512-byte blocks for Page Blob
            int bytesRead;

            // Read from the inputStream and write to the blobOutputStream in 512-byte chunks
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead < buffer.length) {
                    // If bytesRead is less than 512, store in a temporary buffer
                    bufferStream.write(buffer, 0, bytesRead);
                    // Check if the bufferStream has reached or exceeded 512 bytes
                    if (bufferStream.size() >= buffer.length) {
                        byte[] toWrite = bufferStream.toByteArray();
                        int length = toWrite.length - (toWrite.length % buffer.length);
                        outputStream.write(toWrite, 0, length);
                        bufferStream.reset();
                        // Keep the remainder in the bufferStream
                        bufferStream.write(toWrite, length, (toWrite.length % buffer.length));
                    }
                } else {
                    // If bytesRead is exactly 512, write directly
                    outputStream.write(buffer, 0, bytesRead);
                }
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
        return super.setupAsync()
            .then(asyncNoFaultClient.getPageBlobAsyncClient().create(options.getSize()))
            .then(tempSetupPageBlobClient.create(options.getSize()))
            .then(originalContent.setupPageBlob(tempSetupPageBlobClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.getPageBlobAsyncClient().delete()
            .then(tempSetupPageBlobClient.delete())
            .then(super.cleanupAsync());
    }
}
