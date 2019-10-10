// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import java.net.MalformedURLException;
import java.net.URL;

final class JavaDocCodeSnippetsHelpers {
    static BlobContainerAsyncClient getContainerAsyncClient() {
        return new BlobContainerClientBuilder().buildAsyncClient();
    }

    static BlobContainerClient getContainerClient() {
        return new BlobContainerClientBuilder().buildClient();
    }

    static ContainerClient getContainerClient() {
        return new ContainerClientBuilder().buildClient();
    }

    static BlobAsyncClient getBlobAsyncClient(String blobName) {
        return getContainerAsyncClient().getBlobAsyncClient(blobName);
    }

    static BlobClient getBlobClient(String blobName) {
        return new BlobClient(getBlobAsyncClient(blobName));
    }

    static BlobServiceAsyncClient getBlobServiceAsyncClient() {
        return new BlobServiceClientBuilder().buildAsyncClient();
    }

    static BlobServiceClient getBlobServiceClient() {
        return new BlobServiceClientBuilder().buildClient();
    }

    static URL generateURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
