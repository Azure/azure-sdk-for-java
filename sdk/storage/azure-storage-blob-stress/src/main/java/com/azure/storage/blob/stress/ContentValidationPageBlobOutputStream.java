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
 * Page blob output stream with {@link PageBlobOutputStreamOptions#setRequestChecksumAlgorithm} (sync only).
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
            .setRequestChecksumAlgorithm(options.getRequestChecksumAlgorithm());

        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getBlobContentHead(), options.getSize());
             BlobOutputStream outputStream = pageBlobClient.getBlobOutputStream(streamOptions)) {
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (bytesRead < buffer.length) {
                    bufferStream.write(buffer, 0, bytesRead);
                    if (bufferStream.size() >= buffer.length) {
                        byte[] toWrite = bufferStream.toByteArray();
                        int length = toWrite.length - (toWrite.length % buffer.length);
                        outputStream.write(toWrite, 0, length);
                        bufferStream.reset();
                        bufferStream.write(toWrite, length, (toWrite.length % buffer.length));
                    }
                } else {
                    outputStream.write(buffer, 0, bytesRead);
                }
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
