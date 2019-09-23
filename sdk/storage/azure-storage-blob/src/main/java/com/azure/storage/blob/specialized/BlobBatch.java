package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.BatchOperation;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobURLParts;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.LeaseAccessConditions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class BlobBatch {
    private final ClientLogger logger = new ClientLogger(BlobBatch.class);

    private final URL accountUrl;
    private final HttpPipeline batchPipeline;
    private final List<String> batchRequests;

    public BlobBatch(BlobServiceClient client) {
        this(client.getAccountUrl(), client.getHttpPipeline());
    }

    public BlobBatch(BlobServiceAsyncClient client) {
        this(client.getAccountUrl(), client.getHttpPipeline());
    }

    BlobBatch(URL accountUrl, HttpPipeline pipeline) {
        this.accountUrl = accountUrl;

        HttpPipelinePolicy[] policies = new HttpLoggingPolicy[pipeline.getPolicyCount()];
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            policies[i] = pipeline.getPolicy(i);
        }

        this.batchPipeline = new HttpPipelineBuilder()
            .policies(policies)
            .httpClient(new BatchClient(this::sendCallback))
            .build();

        this.batchRequests = new ArrayList<>();
    }

    void sendCallback(String subRequest, int contentId) {
        this.batchRequests.add(subRequest);
    }

    public BatchOperation<Void> delete(String containerName, String blobName,
        DeleteSnapshotsOptionType deleteOptions, BlobAccessConditions blobAccessConditions) {
        BlobURLParts blobURLParts = BlobURLParts.parse(accountUrl);
        blobURLParts.setContainerName(containerName)
            .setBlobName(blobName);

        try {
            return delete(blobURLParts.toURL(), deleteOptions, blobAccessConditions);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        }
    }
    public BatchOperation<Void> delete(URL blobUrl, DeleteSnapshotsOptionType deleteOptions,
        BlobAccessConditions blobAccessConditions) {

        batchPipeline.send(new HttpRequest(HttpMethod.DELETE, blobUrl));
        return new BlobBatchOperation<>(batchPipeline.send(new HttpRequest(HttpMethod.DELETE, blobUrl)));
    }

    public BatchOperation<Void> setTier(String containerName, String blobName, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {
        BlobURLParts blobURLParts = BlobURLParts.parse(accountUrl);
        blobURLParts.setContainerName(containerName)
            .setBlobName(blobName);

        try {
            return setTier(blobURLParts.toURL(), accessTier, leaseAccessConditions);
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        }

    }

    public BatchOperation<Void> setTier(URL blobUrl, AccessTier accessTier,
        LeaseAccessConditions leaseAccessConditions) {

        batchPipeline.send(new HttpRequest(HttpMethod.PUT, blobUrl));
        return new BlobBatchOperation<>(batchPipeline.send(new HttpRequest(HttpMethod.DELETE, blobUrl)));
    }
}
