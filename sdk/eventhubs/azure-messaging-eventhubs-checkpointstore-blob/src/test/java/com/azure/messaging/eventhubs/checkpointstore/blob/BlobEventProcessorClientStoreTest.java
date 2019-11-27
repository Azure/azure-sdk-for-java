// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BlobCheckpointStore}.
 */
public class BlobEventProcessorClientStoreTest {

    @Mock
    private BlobContainerAsyncClient blobContainerAsyncClient;

    @Mock
    private BlockBlobAsyncClient blockBlobAsyncClient;

    @Mock
    private BlobAsyncClient blobAsyncClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testListOwnerShip() {
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        BlobItem blobItem = getBlobItem("owner1", "1", "230", "etag", "ns/eh/cg/ownership/0");
        BlobItem blobItem2 = getBlobItem("owner1", "1", "230", "etag", "ns/eh/cg/0");
        PagedFlux<BlobItem> response = new PagedFlux<BlobItem>(() -> Mono.just(new PagedResponseBase<HttpHeaders,
            BlobItem>(null, 200, null,
            Arrays.asList(blobItem, blobItem2), null,
            null)));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobCheckpointStore.listOwnership("ns", "eh", "cg"))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroup());
                assertEquals("etag", partitionOwnership.getETag());
            }).verifyComplete();
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Map<String, String> headers = new HashMap<>();
        headers.put("eTag", "etag2");
        BlobItem blobItem = getBlobItem("owner1", "1", "230", "etag", "ns/eh/cg/checkpoint/0");
        PagedFlux<BlobItem> response = new PagedFlux<BlobItem>(() -> Mono.just(new PagedResponseBase<HttpHeaders,
            BlobItem>(null, 200, null,
            Arrays.asList(blobItem), null,
            null)));

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/checkpoint/0")).thenReturn(blobAsyncClient);
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blobAsyncClient.exists()).thenReturn(Mono.just(true));
        when(blobAsyncClient.setMetadata(ArgumentMatchers.<Map<String, String>>any()))
            .thenReturn(Mono.empty());

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);

        StepVerifier.create(blobCheckpointStore.updateCheckpoint(checkpoint)).verifyComplete();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testClaimOwnership() {
        PartitionOwnership po = createPartitionOwnership("ns", "eh", "cg", "0", "owner1");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "etag2");

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/ownership/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L),
            isNull(), any(Map.class), isNull(), isNull(), any(BlobRequestConditions.class)))
            .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.claimOwnership(Arrays.asList(po)))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroup());
                assertEquals("etag2", partitionOwnership.getETag());
            }).verifyComplete();
    }


    @Test
    public void testListOwnershipError() {
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.error(new SocketTimeoutException()));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobCheckpointStore.listOwnership("ns", "eh", "cg"))
            .expectError(SocketTimeoutException.class).verify();
    }

    @Test
    public void testUpdateCheckpointError() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Map<String, String> headers = new HashMap<>();
        headers.put("eTag", "etag2");
        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/checkpoint/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.exists()).thenReturn(Mono.just(true));
        when(blobAsyncClient.setMetadata(ArgumentMatchers.<Map<String, String>>any()))
            .thenReturn(Mono.error(new SocketTimeoutException()));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.updateCheckpoint(checkpoint))
            .expectError(SocketTimeoutException.class).verify();
    }

    @Test
    public void testFailedOwnershipClaim() {
        PartitionOwnership po = createPartitionOwnership("ns", "eh", "cg", "0", "owner1");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "etag2");

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/ownership/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L),
            isNull(), ArgumentMatchers.<Map<String, String>>any(), isNull(), isNull(),
            any(BlobRequestConditions.class)))
            .thenReturn(Mono.error(new ResourceModifiedException("Etag did not match", null)));
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.claimOwnership(Arrays.asList(po))).verifyComplete();
    }

    private PartitionOwnership createPartitionOwnership(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroupName,
        String partitionId, String ownerId) {
        return new PartitionOwnership()
            .setFullyQualifiedNamespace(fullyQualifiedNamespace)
            .setEventHubName(eventHubName)
            .setConsumerGroup(consumerGroupName)
            .setPartitionId(partitionId)
            .setOwnerId(ownerId);
    }

    private BlobItem getBlobItem(String owner, String sequenceNumber, String offset, String etag, String blobName) {
        Map<String, String> metadata = getMetadata(owner, sequenceNumber, offset);

        BlobItemProperties properties = new BlobItemProperties()
            .setLastModified(OffsetDateTime.now())
            .setETag(etag);

        return new BlobItem()
            .setName(blobName)
            .setMetadata(metadata)
            .setProperties(properties);
    }

    private Map<String, String> getMetadata(String owner, String sequenceNumber, String offset) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("OwnerId", owner);
        metadata.put("SequenceNumber", sequenceNumber);
        metadata.put("Offset", offset);
        return metadata;
    }
}
