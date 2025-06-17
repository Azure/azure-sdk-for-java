// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.storage.blob.perf;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

public class DownloadBlobTest extends ServiceTest<BlobPerfStressOptions> {
    private final int bufferSize;

    private final String containerName;
    private final String blobName;

    public DownloadBlobTest(BlobPerfStressOptions options) {
        super(options);

        this.bufferSize = StoragePerfUtils.getDynamicDownloadBufferSize(options.getSize());

        Configuration configuration = Configuration.getGlobalConfiguration().clone();
        this.containerName = configuration.get("STORAGE_CONTAINER_NAME");
        if (CoreUtils.isNullOrEmpty(this.containerName)) {
            throw new IllegalStateException(
                "STORAGE_CONTAINER_NAME must be set to the container containing the blob " + "to download.");
        }
        this.blobName = configuration.get("STORAGE_BLOB_NAME");
        if (CoreUtils.isNullOrEmpty(this.blobName)) {
            throw new IllegalStateException("STORAGE_BLOB_NAME must be set to the blob to download.");
        }
    }

    @Override
    public void run() {
        try (InputStream inputStream = callDownload()) {
            // Consume InputStream with minimal work
            drainInputStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InputStream callDownload() {
        return blobClient.download(containerName, blobName, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null);
    }

    /**
     * Drains the InputStream as quickly as possible without processing its content.
     */
    private void drainInputStream(InputStream inputStream) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        while (inputStream.read(buffer) != -1) {
            // Do nothing, just read and discard the data
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.error(new RuntimeException("Async is a thing of past."));
    }
}
