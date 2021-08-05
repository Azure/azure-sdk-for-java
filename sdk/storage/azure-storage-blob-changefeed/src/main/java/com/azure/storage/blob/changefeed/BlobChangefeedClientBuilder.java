// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.changefeed;

import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import com.azure.storage.internal.avro.implementation.AvroReaderFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of
 * {@link BlobChangefeedClient BlobChangefeedClients} and {@link BlobChangefeedAsyncClient BlobChangefeedAsyncClients}
 * when {@link #buildClient() buildClient} and {@link #buildAsyncClient() buildAsyncClient} are called respectively.
 */
@ServiceClientBuilder(serviceClients = {BlobChangefeedClient.class, BlobChangefeedAsyncClient.class})
public final class BlobChangefeedClientBuilder {

    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-blob-changefeed.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");

    static final String CHANGEFEED_CONTAINER_NAME = "$blobchangefeed";

    private final String accountUrl;
    private final HttpPipeline pipeline;
    private final BlobServiceVersion version;

    /**
     * Constructs the {@link BlobChangefeedClientBuilder} from the URL and pipeline of the {@link BlobServiceClient}.
     *
     * @param client {@link BlobServiceClient} whose properties are used to configure the builder.
     */
    public BlobChangefeedClientBuilder(BlobServiceClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
        this.version = client.getServiceVersion();
    }

    /**
     * Constructs the {@link BlobChangefeedClientBuilder} from from the URL and pipeline of the
     * {@link BlobServiceAsyncClient}.
     *
     * @param client {@link BlobServiceClient} whose properties are used to configure the builder.
     */
    public BlobChangefeedClientBuilder(BlobServiceAsyncClient client) {
        this.accountUrl = client.getAccountUrl();
        this.pipeline = client.getHttpPipeline();
        this.version = client.getServiceVersion();
    }

    /**
     * Creates a {@link BlobChangefeedClient}.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildClient}
     *F
     * @return a {@link BlobChangefeedClient} created from the configurations in this builder.
     */
    public BlobChangefeedClient buildClient() {
        return new BlobChangefeedClient(buildAsyncClient());
    }

    /**
     * Creates a {@link BlobChangefeedAsyncClient}.
     *
     * <p><strong>Code sample</strong></p>
     *
     * {@codesnippet com.azure.storage.blob.changefeed.BlobChangefeedClientBuilder#buildAsyncClient}
     *
     * @return a {@link BlobChangefeedAsyncClient} created from the configurations in this builder.
     */
    public BlobChangefeedAsyncClient buildAsyncClient() {
        BlobContainerAsyncClient client = new BlobContainerClientBuilder()
            .endpoint(accountUrl)
            .containerName(CHANGEFEED_CONTAINER_NAME)
            .pipeline(addBlobUserAgentModificationPolicy(pipeline))
            .serviceVersion(version)
            .buildAsyncClient();
        AvroReaderFactory avroReaderFactory = new AvroReaderFactory();
        BlobChunkedDownloaderFactory blobChunkedDownloaderFactory = new BlobChunkedDownloaderFactory(client);
        ChunkFactory chunkFactory = new ChunkFactory(avroReaderFactory, blobChunkedDownloaderFactory);
        ShardFactory shardFactory = new ShardFactory(chunkFactory, client);
        SegmentFactory segmentFactory = new SegmentFactory(shardFactory, client);
        ChangefeedFactory changefeedFactory = new ChangefeedFactory(segmentFactory, client);
        return new BlobChangefeedAsyncClient(changefeedFactory);
    }

    private HttpPipeline addBlobUserAgentModificationPolicy(HttpPipeline pipeline) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();

        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy currPolicy = pipeline.getPolicy(i);
            policies.add(currPolicy);
            if (currPolicy instanceof UserAgentPolicy) {
                policies.add(new BlobUserAgentModificationPolicy(CLIENT_NAME, CLIENT_VERSION));
            }
        }

        return new HttpPipelineBuilder()
            .httpClient(pipeline.getHttpClient())
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }
}
