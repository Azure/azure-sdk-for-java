// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.PageBlobOutputStreamOptions;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.PageBlobAsyncClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * Page blob output stream with {@link PageBlobOutputStreamOptions#setContentValidationAlgorithm} (sync only).
 */
public class ContentValidationPageBlobOutputStream extends PageBlobScenarioBase<ContentValidationStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(ContentValidationPageBlobOutputStream.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncNoFaultClient;
    /** Page blob used only to seed {@link OriginalContent} (same pattern as {@link PageBlobOutputStream}). */
    private final PageBlobAsyncClient tempSetupPageBlobClient;

    public ContentValidationPageBlobOutputStream(ContentValidationStressOptions options) {
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
        PageBlobOutputStreamOptions streamOptions = new PageBlobOutputStreamOptions(
            new PageRange().setStart(0).setEnd(options.getSize() - 1))
            .setContentValidationAlgorithm(options.getContentValidationAlgorithm());

        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize());
             BlobOutputStream outputStream = pageBlobClient.getBlobOutputStream(streamOptions)) {
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Always accumulate into bufferStream to avoid dropping or reordering bytes
                bufferStream.write(buffer, 0, bytesRead);
                // Flush all full 512-byte pages from the accumulator
                if (bufferStream.size() >= buffer.length) {
                    byte[] toWrite = bufferStream.toByteArray();
                    int length = toWrite.length - (toWrite.length % buffer.length);
                    if (length > 0) {
                        outputStream.write(toWrite, 0, length);
                        bufferStream.reset();
                        // Keep any remaining partial page bytes in the accumulator
                        bufferStream.write(toWrite, length, toWrite.length - length);
                    }
                }
            }
            // For page blobs, total content size must be a multiple of 512 bytes.
            // Any remaining bytes here indicate misalignment and would result in silent truncation.
            if (bufferStream.size() != 0) {
                throw new IOException("Remaining bytes in buffer that do not align to 512-byte page size.");
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
            .then(asyncNoFaultClient.getPageBlobAsyncClient().create(options.getSize()))
            .then(tempSetupPageBlobClient.create(options.getSize()))
            .then(originalContent.setupPageBlob(tempSetupPageBlobClient, options.getSize()));
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return asyncNoFaultClient.getPageBlobAsyncClient().deleteIfExists()
            .onErrorResume(e -> Mono.empty())
            .then(tempSetupPageBlobClient.deleteIfExists())
            .then(super.cleanupAsync());
    }
}
