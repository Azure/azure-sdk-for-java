// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;

import java.time.OffsetDateTime;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob changefeed.
 *
 * @see BlobChangefeedClientBuilder
 */
@ServiceClient(builder = BlobChangefeedClientBuilder.class, isAsync = true)
public class BlobChangefeedAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobChangefeedAsyncClient.class);

    static final String CHANGEFEED_CONTAINER_NAME = "$blobchangefeed";

    private final BlobContainerAsyncClient client;

    /**
     * Package-private constructor for use by {@link BlobChangefeedClientBuilder}.
     *
     * @param pipeline The pipeline used to send and receive service requests.
     * @param url The endpoint where to send service requests.
     * @param version The version of the service to receive requests.
     */
    BlobChangefeedAsyncClient(HttpPipeline pipeline, String url, BlobServiceVersion version) {
        this.client = new BlobContainerClientBuilder()
            .endpoint(url)
            .containerName(CHANGEFEED_CONTAINER_NAME)
            .pipeline(pipeline)
            .serviceVersion(version)
            .buildAsyncClient();
    }

    /**
     *
     * @return
     */
    public BlobChangefeedPagedFlux getEvents() {
        return getEvents(null, null);
    }

    public BlobChangefeedPagedFlux getEvents(OffsetDateTime startTime, OffsetDateTime endTime) {
        return new BlobChangefeedPagedFlux(client, startTime, endTime);
    }

    public BlobChangefeedPagedFlux getEvents(String cursor) {
        return new BlobChangefeedPagedFlux(client, cursor);
    }

}
