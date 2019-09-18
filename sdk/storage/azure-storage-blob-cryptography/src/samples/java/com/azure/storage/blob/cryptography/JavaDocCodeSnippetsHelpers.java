// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.cryptography;

import java.net.MalformedURLException;
import java.net.URL;

final class JavaDocCodeSnippetsHelpers {
    static EncryptedBlockBlobClient getEncryptedBlockBlobClient(String blobName, String containerName) {
        return new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(null, null)
            .blobName(blobName)
            .containerName(containerName)
            .buildEncryptedBlockBlobClient();
    }

    static EncryptedBlockBlobAsyncClient getEncryptedBlockBlobAsyncClient(String blobName, String containerName) {
        return new EncryptedBlobClientBuilder()
            .keyAndKeyResolver(null, null)
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
}
