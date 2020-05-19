// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobAsyncClient;

/**
 * Factory class for {@link BlobLazyDownloader}.
 */
class BlobLazyDownloaderFactory {

    /**
     * Gets a new instance of a BlobLazyDownloader.
     *
     * @param client The blob client.
     * @param blockSize The block size to download.
     * @param offset The offset to start downloading from.
     * @return {@link BlobLazyDownloader}
     */
    BlobLazyDownloader getBlobLazyDownloader(BlobAsyncClient client, long blockSize, long offset) {
        return new BlobLazyDownloader(client, blockSize, offset);
    }

    /**
     * Gets a new instance of a BlobLazyDownloader.
     *
     * @param client The blob client.
     * @param totalSize The total size to download.
     * @return {@link BlobLazyDownloader}
     */
    BlobLazyDownloader getBlobLazyDownloader(BlobAsyncClient client, long totalSize) {
        return new BlobLazyDownloader(client, totalSize);
    }
}
