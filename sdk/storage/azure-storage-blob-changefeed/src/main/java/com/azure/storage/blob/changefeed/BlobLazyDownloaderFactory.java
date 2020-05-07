package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.BlobAsyncClient;

class BlobLazyDownloaderFactory {

    BlobLazyDownloader getBlobLazyDownloader(BlobAsyncClient client, long blockSize, long offset) {
        return new BlobLazyDownloader(client, blockSize, offset);
    }

    BlobLazyDownloader getBlobLazyDownloader(BlobAsyncClient client, long blockSize) {
        return new BlobLazyDownloader(client, blockSize);
    }
}
