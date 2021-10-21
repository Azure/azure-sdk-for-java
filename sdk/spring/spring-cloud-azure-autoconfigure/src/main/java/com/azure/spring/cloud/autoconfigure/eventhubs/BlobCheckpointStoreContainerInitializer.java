// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs;

import com.azure.storage.blob.BlobContainerAsyncClient;

/**
 * Interface to be implemented in order to configure the BlobCheckpointStore's container programmatically.
 *
 */
public interface BlobCheckpointStoreContainerInitializer {

    void init(BlobContainerAsyncClient containerAsyncClient);

}
