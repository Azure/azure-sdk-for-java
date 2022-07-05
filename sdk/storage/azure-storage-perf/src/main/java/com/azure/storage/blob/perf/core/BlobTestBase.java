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
import reactor.core.publisher.Mono;

import static com.azure.perf.test.core.TestDataCreationHelper.createRandomByteBufferFlux;

public abstract class BlobTestBase<TOptions extends BlobPerfStressOptions> extends ContainerTest<TOptions> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected static  final String BLOB_NAME_PREFIX = "randomblobtest-";
    protected final BlobClient blobClient;
    protected final BlockBlobClient blockBlobClient;
    protected final BlobAsyncClient blobAsyncClient;
    protected final BlockBlobAsyncClient blockBlobAsyncClient;


    public BlobTestBase(TOptions options, String blobName) {
        super(options);

        if (options.getClientEncryption() != null) {
            EncryptionVersion version;
            if (options.getClientEncryption().equals("1.0")) {
                version = EncryptionVersion.V1;
            } else if (options.getClientEncryption().equals("2.0")) {
                version = EncryptionVersion.V2;
            } else {
                throw new IllegalArgumentException("Encryption version not recognized");
            }

            Random rand = new Random(System.currentTimeMillis());
            byte[] data = new byte[256];
            rand.nextBytes(data);
            FakeKey key = new FakeKey("keyId", data);

            EncryptedBlobClientBuilder builder = new EncryptedBlobClientBuilder(version)
                .blobClient(blobContainerClient.getBlobClient(blobName))
                .key(key, KeyWrapAlgorithm.A256KW.toString());

            blobClient = builder.buildEncryptedBlobClient();
            blobAsyncClient = builder.buildEncryptedBlobAsyncClient();
        } else {
            blobClient = blobContainerClient.getBlobClient(blobName);
            blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);
        }

        blockBlobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();
        blockBlobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName).getBlockBlobAsyncClient();
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then();
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then();
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
