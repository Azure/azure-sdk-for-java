// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.UUID;

import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.perf.BlobPerfStressOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.cryptography.EncryptedBlobClientBuilder;
import com.azure.storage.blob.specialized.cryptography.EncryptionVersion;

public abstract class BlobTestBase<TOptions extends BlobPerfStressOptions> extends ContainerTest<TOptions> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final BlobClient blobClient;
    protected final BlockBlobClient blockBlobClient;
    protected final BlobAsyncClient blobAsyncClient;
    protected final BlockBlobAsyncClient blockBlobAsyncClient;

    public BlobTestBase(TOptions options) {
        super(options);

        String blobName = "randomblobtest-" + UUID.randomUUID().toString();

        if (options.getEncryptionVersion() != null) {
            Random rand = new Random(System.currentTimeMillis());
            byte[] data = new byte[256];
            rand.nextBytes(data);
            FakeKey key = new FakeKey("keyId", data);

            EncryptedBlobClientBuilder builder = new EncryptedBlobClientBuilder(options.getEncryptionVersion())
                .blobClient(blobContainerClient.getBlobClient(blobName))
                .key(key, KeyWrapAlgorithm.A256KW.toString());

            blobClient = builder.buildEncryptedBlobClient();
            blobAsyncClient = builder.buildEncryptedBlobAsyncClient();
        } else {
            blobClient = blobContainerClient.getBlobClient(blobName);
            blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);
        }

        blockBlobClient = blobClient.getBlockBlobClient();
        blockBlobAsyncClient = blobAsyncClient.getBlockBlobAsyncClient();
    }

    public long copyStream(InputStream input, OutputStream out) throws IOException {
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = input.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }
}
