// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import java.net.MalformedURLException;
import java.net.URL;

final class JavaDocCodeSnippetsHelpers {
    static ContainerAsyncClient getContainerAsyncClient() {
        return new ContainerClientBuilder().buildAsyncClient();
    }

    static BlobAsyncClient getBlobAsyncClient(String blobName) {
        return getContainerAsyncClient().getBlobAsyncClient(blobName);
    }

    static BlobClient getBlobClient(String blobName) {
        return new BlobClient(getBlobAsyncClient(blobName));
    }

    static URL generateURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
