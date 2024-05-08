// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.file.datalake.DataLakeFileAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.stress.utils.OriginalContent;
import com.azure.storage.stress.CrcInputStream;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;


import java.io.IOException;
import java.io.OutputStream;

import static com.azure.core.util.FluxUtil.monoError;

public class DataLakeOutputStream extends DataLakeScenarioBase<StorageStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(DataLakeOutputStream.class);
    private final OriginalContent originalContent = new OriginalContent();
    private final DataLakeFileClient syncClient;
    private final DataLakeFileAsyncClient asyncNoFaultClient;
    public final DataLakeFileClient syncNoFaultClient;

    public DataLakeOutputStream(StorageStressOptions options) {
        super(options);
        String fileName = generateFileName();
        this.syncClient = getSyncFileSystemClient().getFileClient(fileName);
        this.asyncNoFaultClient = getAsyncFileSystemClientNoFault().getFileAsyncClient(fileName);
        this.syncNoFaultClient = getSyncFileSystemClientNoFault().getFileClient(fileName);
    }

    @Override
    protected void runInternal(Context span) throws IOException {

        try (CrcInputStream inputStream = new CrcInputStream(originalContent.getContentHead(), options.getSize());
             OutputStream outputStream = syncClient.getOutputStream(null, span)) {
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
        return monoError(LOGGER, new RuntimeException("getOutputStream() does not exist on the async client"));
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
