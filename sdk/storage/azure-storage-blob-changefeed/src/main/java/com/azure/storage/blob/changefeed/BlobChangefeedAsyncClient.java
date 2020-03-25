// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.OffsetDateTime;

import static com.azure.core.util.FluxUtil.pagedFluxError;

/**
 * This class provides a client that contains all operations that apply to Azure Storage Blob changefeed.
 *
 * @see BlobChangefeedClientBuilder
 */
@ServiceClient(builder = BlobChangefeedClientBuilder.class, isAsync = true)
public class BlobChangefeedAsyncClient {
    private final ClientLogger logger = new ClientLogger(BlobChangefeedAsyncClient.class);

    private static final String CHANGEFEED_CONTAINER_NAME = "$blobchangefeed";
    private static final String SEGMENT_PREFIX = "idx/segments/";

    private final BlobContainerAsyncClient client;

    BlobChangefeedAsyncClient(String accountUrl, HttpPipeline pipeline, BlobServiceVersion version) {
        this.client = new BlobContainerClientBuilder()
            .endpoint(accountUrl)
            .containerName(CHANGEFEED_CONTAINER_NAME)
            .pipeline(pipeline)
            .serviceVersion(version)
            .buildAsyncClient();
    }

    public Flux<BlobChangefeedEvent> getEvents() {
        return getEvents(new ChangefeedBlobsOptions());
    }

    public Flux<BlobChangefeedEvent> getEvents(ChangefeedBlobsOptions options) {
        try {
            return getEventsWithOptionalTimeout( options, null, null,null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    public Flux<BlobChangefeedEvent> getEvents(OffsetDateTime startTime, OffsetDateTime endTime) {
        try {
            return getEventsWithOptionalTimeout(null, startTime, endTime, null);
        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    Flux<BlobChangefeedEvent> getEventsWithOptionalTimeout(ChangefeedBlobsOptions options, OffsetDateTime startTime,
        OffsetDateTime endTime, Duration timeout) {
        options = options == null ? new ChangefeedBlobsOptions() : options;
        startTime = startTime == null ? OffsetDateTime.MIN : startTime;
        endTime = endTime == null ? OffsetDateTime.MAX : endTime;

        return new Changefeed(this.client, startTime, endTime).getEvents();
    }
}
