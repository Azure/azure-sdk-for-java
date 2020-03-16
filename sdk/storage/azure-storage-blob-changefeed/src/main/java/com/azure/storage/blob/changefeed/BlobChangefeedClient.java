// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClient;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob changefeed.
 *
 * @see BlobChangefeedClientBuilder
 */
@ServiceClient(builder = BlobChangefeedClientBuilder.class)
public class BlobChangefeedClient {

    private final BlobChangefeedAsyncClient client;

    BlobChangefeedClient(BlobChangefeedAsyncClient client) {
        this.client = client;
    }

    /**
     * This is a temporary method to pass CI for now because this.client was not used anywhere. Will remove in next PR.
     */
    public void tempMethod() {
        this.client.tempMethod();
    }
}
