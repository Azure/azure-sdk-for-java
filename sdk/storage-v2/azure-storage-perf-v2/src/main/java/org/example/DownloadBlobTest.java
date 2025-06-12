// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package org.example;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

public class DownloadBlobTest extends ServiceTest<BlobPerfStressOptions> {
    private final int bufferSize;
    private final byte[] buffer;

    private String containerName;
    private String blobName;

    public DownloadBlobTest(BlobPerfStressOptions options) {
        super(options);

        this.bufferSize = StoragePerfUtils.getDynamicDownloadBufferSize(options.getSize());
        this.buffer = new byte[bufferSize];

        this.containerName = System.getenv("STORAGE_CONTAINER_NAME");
        this.blobName = System.getenv("STORAGE_BLOB_NAME");
    }

    @Override
    public void run() {
        {
            InputStream inputStream = null;
            try {
                inputStream = blobClient.download(containerName, blobName, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null);

                // Consume InputStream with minimal work
                drainInputStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
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
