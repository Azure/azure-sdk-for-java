// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

final class JavaDocCodeSnippetsHelpers {
    static EncryptedBlockBlobClient getEncryptedBlockBlobClient(String blobName, String containerName) {
        return new EncryptedBlobClientBuilder()
            .key(null, null)
            .keyResolver(null)
            .blobName(blobName)
            .containerName(containerName)
            .buildEncryptedBlockBlobClient();
    }

    static BlockBlobClient getBlockBlobClient(String connectionString) {
        return new BlobClientBuilder()
            .connectionString(connectionString)
            .buildClient()
            .getBlockBlobClient();
    }

    static BlockBlobAsyncClient getBlockBlobAsyncClient(String connectionString) {
        return new BlobClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient()
            .getBlockBlobAsyncClient();
    }

    static EncryptedBlockBlobAsyncClient getEncryptedBlockBlobAsyncClient(String blobName, String containerName) {
        return new EncryptedBlobClientBuilder()
            .key(null, null)
            .keyResolver(null)
            .blobName(blobName)
            .containerName(containerName)
            .buildEncryptedBlockBlobAsyncClient();
    }

    static URL generateURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    static AsyncKeyEncryptionKey getKey() {
        return new AsyncKeyEncryptionKey() {
            @Override
            public Mono<String> getKeyId() {
                return null;
            }

            @Override
            public Mono<byte[]> wrapKey(String algorithm, byte[] key) {
                return null;
            }

            @Override
            public Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey) {
                return null;
            }
        };
    }

    static AsyncKeyEncryptionKeyResolver getKeyResolver() {
        return new AsyncKeyEncryptionKeyResolver() {
            @Override
            public Mono<? extends AsyncKeyEncryptionKey> buildAsyncKeyEncryptionKey(String keyId) {
                return null;
            }
        };
    }
}
