// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import java.net.MalformedURLException;
import java.net.URL;

final class JavaDocCodeSnippetsHelpers {
    static ContainerAsyncClient getContainerAsyncClient() {
        return new ContainerClientBuilder().buildAsyncClient();
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

    static PageBlobClient getPageBlobClient(String blobName, String containerName) {
        return new BlobClientBuilder()
            .blobName(blobName)
            .containerName(containerName)
            .buildPageBlobClient();
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
