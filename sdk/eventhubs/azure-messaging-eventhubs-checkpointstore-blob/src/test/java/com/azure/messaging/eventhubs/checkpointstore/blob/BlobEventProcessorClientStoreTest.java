// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.exception.HttpResponseException;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import static org.mockito.ArgumentMatchers.*;
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

    // List Ownership
    // 1. No ownership records exist
    // 2. Ownership records exist
    // 3. Authentication error (API for getBlobs) -> Sees HttpResponseException

    @Test
    public void authError() {
        //Arrange
        BlobContainerAsyncClient client = Mockito.mock(BlobContainerAsyncClient.class);
        when(client.listBlobs(argThat(arg -> {
            return arg.getPrefix().equals("");
        }))).thenReturn(Flux.error(new HttpResponseException()));

        BlobCheckpointStore store = new BlobCheckpointStore(client);
        String fqdn = "";
        String consumerGRoup = "";
        String eventHub = "";

        // Act & Assert
        StepVerifier.create(store.listCheckpoints(fqdn, eventHub, consumerGRoup))
            .consumeErrorWith((error) -> {

            })
            .verify();
    }

    @Test
    public void testListOwnerShip() {
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        BlobItem blobItem = getOwnershipBlobItem("owner1", "etag", "ns/eh/cg/ownership/0"); // valid blob
        BlobItem blobItem2 = getOwnershipBlobItem("owner1", "etag", "ns/eh/cg/0"); // invalid name
        BlobItem blobItem3 = new BlobItem().setName("ns/eh/cg/ownership/5"); // no metadata
        BlobItem blobItem4 = getOwnershipBlobItem(null, "2", "ns/eh/cg/ownership/2"); // valid blob with null ownerid

        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.just(new PagedResponseBase<HttpHeaders,
            BlobItem>(null, 200, null,
            Arrays.asList(blobItem, blobItem2, blobItem3, blobItem4), null,
            null)));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobCheckpointStore.listOwnership("ns", "eh", "cg"))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroup());
                assertEquals("etag", partitionOwnership.getETag());
            })
            .assertNext(partitionOwnership -> {
                assertEquals("", partitionOwnership.getOwnerId());
                assertEquals("2", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroup());
                assertEquals("2", partitionOwnership.getETag());
            }).verifyComplete();
    }

    @Test
    public void testListCheckpoint() {
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        BlobItem blobItem = getCheckpointBlobItem("230", "1", "ns/eh/cg/checkpoint/0"); // valid blob
        BlobItem blobItem2 = new BlobItem().setName("ns/eh/cg/checkpoint/1"); // valid blob with no metadata
        BlobItem blobItem3 = getCheckpointBlobItem("230", "1", "ns/eh/cg/1"); // invalid name
        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.just(new PagedResponseBase<HttpHeaders,
            BlobItem>(null, 200, null,
            Arrays.asList(blobItem, blobItem2, blobItem3), null,
            null)));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobCheckpointStore.listCheckpoints("ns", "eh", "cg"))
            .assertNext(checkpoint -> {
                assertEquals("0", checkpoint.getPartitionId());
                assertEquals("eh", checkpoint.getEventHubName());
                assertEquals("cg", checkpoint.getConsumerGroup());
                assertEquals(1L, checkpoint.getSequenceNumber());
                assertEquals(230L, checkpoint.getOffset());
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

        BlobItem blobItem = getCheckpointBlobItem("230", "1", "ns/eh/cg/checkpoint/0");
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

    @Test
    public void testInvalidCheckpoint() {
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        Assertions.assertThrows(IllegalStateException.class, () -> blobCheckpointStore.updateCheckpoint(null));
        Assertions
            .assertThrows(IllegalStateException.class, () -> blobCheckpointStore.updateCheckpoint(new Checkpoint()));
    }

    @Test
    public void testUpdateCheckpointForNewPartition() {
        Checkpoint checkpoint = new Checkpoint()
            .setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "etag2");
        BlobItem blobItem = getCheckpointBlobItem("230", "1", "ns/eh/cg/checkpoint/0");
        PagedFlux<BlobItem> response = new PagedFlux<BlobItem>(() -> Mono.just(new PagedResponseBase<HttpHeaders,
            BlobItem>(null, 200, null,
            Arrays.asList(blobItem), null,
            null)));

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/checkpoint/0")).thenReturn(blobAsyncClient);
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blobAsyncClient.exists()).thenReturn(Mono.just(false));
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L),
            isNull(), anyMap(), isNull(), isNull(), isNull()))
            .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.updateCheckpoint(checkpoint)).verifyComplete();
    }


    @Test
    public void testClaimOwnership() {
        PartitionOwnership po = createPartitionOwnership("ns", "eh", "cg", "0", "owner1");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "etag2");

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/ownership/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L),
            isNull(), ArgumentMatchers.<Map<String, String>>any(), isNull(), isNull(),
            any(BlobRequestConditions.class)))
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
    public void testClaimOwnershipExistingBlob() {
        PartitionOwnership po = createPartitionOwnership("ns", "eh", "cg", "0", "owner1");
        po.setETag("1");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "2");

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/ownership/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blobAsyncClient
            .setMetadataWithResponse(ArgumentMatchers.<Map<String, String>>any(), any(BlobRequestConditions.class)))
            .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.claimOwnership(Arrays.asList(po)))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroup());
                assertEquals("2", partitionOwnership.getETag());
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
        StepVerifier.create(blobCheckpointStore.claimOwnership(Arrays.asList(po))).verifyError();

        PartitionOwnership po2 = createPartitionOwnership("ns", "eh", "cg", "0", "owner1");
        po2.setETag("1");
        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/ownership/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blobAsyncClient
            .setMetadataWithResponse(ArgumentMatchers.<Map<String, String>>any(), any(BlobRequestConditions.class)))
            .thenReturn(Mono.error(new ResourceModifiedException("Etag did not match", null)));
        StepVerifier.create(blobCheckpointStore.claimOwnership(Arrays.asList(po2))).verifyError();

        blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        when(blobContainerAsyncClient.getBlobAsyncClient(anyString())).thenReturn(null);
        StepVerifier.create(blobCheckpointStore.claimOwnership(Arrays.asList(po))).verifyError();
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

    private BlobItem getOwnershipBlobItem(String owner, String etag, String blobName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("ownerid", owner);
        BlobItemProperties properties = new BlobItemProperties()
            .setLastModified(OffsetDateTime.now())
            .setETag(etag);

        return new BlobItem()
            .setName(blobName)
            .setMetadata(metadata)
            .setProperties(properties);
    }

    private BlobItem getCheckpointBlobItem(String offset, String sequenceNumber, String blobName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("sequencenumber", sequenceNumber);
        metadata.put("offset", offset);
        return new BlobItem()
            .setName(blobName)
            .setMetadata(metadata);
    }
}
