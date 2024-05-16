// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;

import static com.azure.core.util.FluxUtil.monoError;

public class FileOutputStream extends ShareScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(FileOutputStream.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final ShareFileClient syncClient;
    private final ShareFileAsyncClient asyncNoFaultClient;

    public FileOutputStream(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncShareClient().getFileClient(fileName);
        this.asyncNoFaultClient = getAsyncShareClientNoFault().getFileClient(fileName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {

        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getContentHead(), options.getSize());
             OutputStream outputStream = syncClient.getFileOutputStream()) {
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
        return monoError(LOGGER, new RuntimeException("getFileOutputStream() does not exist on the async client"));
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
