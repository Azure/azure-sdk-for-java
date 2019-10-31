// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
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
import org.junit.Before;
import org.junit.Test;
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

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BlobPartitionManager}.
 */
public class BlobPartitionManagerTest {

    @Mock
    private BlobContainerAsyncClient blobContainerAsyncClient;

    @Mock
    private BlockBlobAsyncClient blockBlobAsyncClient;

    @Mock
    private BlobAsyncClient blobAsyncClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testListOwnerShip() {
        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(blobContainerAsyncClient);
        BlobItem blobItem = getBlobItem("owner1", "1", "230", "etag", "eh/cg/0");
        PagedFlux<BlobItem> response = new PagedFlux<BlobItem>(() -> Mono.just(new PagedResponseBase<HttpHeaders,
            BlobItem>(null, 200, null,
            Arrays.asList(blobItem), null,
            null)));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobPartitionManager.listOwnership("eh", "cg"))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals(1, (long) partitionOwnership.getSequenceNumber());
                assertEquals(230, (long) partitionOwnership.getOffset());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroupName());
                assertEquals("etag", partitionOwnership.getETag());
            }).verifyComplete();
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = new Checkpoint()
            .setEventHubName("eh")
            .setConsumerGroupName("cg")
            .setOwnerId("owner1")
            .setPartitionId("0")
            .setETag("etag")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Map<String, String> headers = new HashMap<>();
        headers.put("eTag", "etag2");
        when(blobContainerAsyncClient.getBlobAsyncClient("eh/cg/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.setMetadataWithResponse(ArgumentMatchers.<Map<String, String>>any(), any(BlobRequestConditions.class)))
            .thenReturn(Mono.just(new SimpleResponse<>(null, 200, new HttpHeaders(headers), null)));

        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(blobContainerAsyncClient);
        StepVerifier.create(blobPartitionManager.updateCheckpoint(checkpoint))
            .assertNext(etag -> assertEquals("etag2", etag)).verifyComplete();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testClaimOwnership() {
        PartitionOwnership po = createPartitionOwnership("eh", "cg", "0", "owner1");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "etag2");

        when(blobContainerAsyncClient.getBlobAsyncClient("eh/cg/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L),
            isNull(), any(Map.class), isNull(), isNull(), any(BlobRequestConditions.class)))
            .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));

        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(blobContainerAsyncClient);
        StepVerifier.create(blobPartitionManager.claimOwnership(po))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroupName());
                assertEquals("etag2", partitionOwnership.getETag());
                assertNull(partitionOwnership.getSequenceNumber());
                assertNull(partitionOwnership.getOffset());
            }).verifyComplete();
    }


    @Test
    public void testListOwnershipError() {
        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(blobContainerAsyncClient);
        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.error(new SocketTimeoutException()));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobPartitionManager.listOwnership("eh", "cg"))
            .expectError(SocketTimeoutException.class).verify();
    }

    @Test
    public void testUpdateCheckpointError() {
        Checkpoint checkpoint = new Checkpoint()
            .setEventHubName("eh")
            .setConsumerGroupName("cg")
            .setOwnerId("owner1")
            .setPartitionId("0")
            .setETag("etag")
            .setSequenceNumber(2L)
            .setOffset(100L);

        Map<String, String> headers = new HashMap<>();
        headers.put("eTag", "etag2");
        when(blobContainerAsyncClient.getBlobAsyncClient("eh/cg/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.setMetadataWithResponse(ArgumentMatchers.<Map<String, String>>any(), any(BlobRequestConditions.class)))
            .thenReturn(Mono.error(new SocketTimeoutException()));

        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(blobContainerAsyncClient);
        StepVerifier.create(blobPartitionManager.updateCheckpoint(checkpoint))
                .expectError(SocketTimeoutException.class).verify();
    }

    @Test
    public void testFailedOwnershipClaim() {
        PartitionOwnership po = createPartitionOwnership("eh", "cg", "0", "owner1");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "etag2");

        when(blobContainerAsyncClient.getBlobAsyncClient("eh/cg/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L),
            isNull(), ArgumentMatchers.<Map<String, String>>any(), isNull(), isNull(), any(BlobRequestConditions.class)))
            .thenReturn(Mono.error(new ResourceModifiedException("Etag did not match", null)));
        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(blobContainerAsyncClient);
        StepVerifier.create(blobPartitionManager.claimOwnership(po)).verifyComplete();
    }

    private PartitionOwnership createPartitionOwnership(String eventHubName, String consumerGroupName,
        String partitionId, String ownerId) {
        return new PartitionOwnership()
            .setEventHubName(eventHubName)
            .setConsumerGroupName(consumerGroupName)
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
