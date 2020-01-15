package com.azure.storage.blob.perfstress.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.azure.perfstress.PerfStressOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;

public abstract class RandomBlobTest<TOptions extends PerfStressOptions> extends ContainerTest<TOptions> {
    protected final BlobClient _blobClient;
    protected final BlockBlobClient _blockBlobClient;
    protected final BlobAsyncClient _blobAsyncClient;
    protected final BlockBlobAsyncClient _blockBlobAsyncClient;

    public RandomBlobTest(TOptions options) {
        super(options);

        String blobName = "randomblobtest-" + UUID.randomUUID().toString();

        _blobClient = BlobContainerClient.getBlobClient(blobName);
        _blobAsyncClient = BlobContainerAsyncClient.getBlobAsyncClient(blobName);

        _blockBlobClient = _blobClient.getBlockBlobClient();
        _blockBlobAsyncClient = _blobAsyncClient.getBlockBlobAsyncClient();
    }

    public long copyStream(InputStream input, OutputStream out) throws IOException {
        long transferred = 0;
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer, 0, 8192)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }
}