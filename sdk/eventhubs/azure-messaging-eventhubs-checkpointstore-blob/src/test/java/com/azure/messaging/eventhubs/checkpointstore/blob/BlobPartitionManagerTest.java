// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlockBlobAsyncClient;
import com.azure.storage.blob.ContainerAsyncClient;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.Metadata;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link BlobPartitionManager}.
 */
public class BlobPartitionManagerTest {

    @Mock
    private ContainerAsyncClient containerAsyncClient;

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
        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(containerAsyncClient);
        BlobItem blobItem = getBlobItem("owner1", "1", "230", "etag", "eh/cg/0");
        PagedFlux<BlobItem> response = new PagedFlux<BlobItem>(() -> Mono.just(new PagedResponseBase<HttpHeaders,
            BlobItem>(null, 200, null,
            Arrays.asList(blobItem), null,
            null)));
        when(containerAsyncClient.listBlobsFlat(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobPartitionManager.listOwnership("eh", "cg"))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.ownerId());
                assertEquals("0", partitionOwnership.partitionId());
                assertEquals(1, (long) partitionOwnership.sequenceNumber());
                assertEquals("230", partitionOwnership.offset());
                assertEquals("eh", partitionOwnership.eventHubName());
                assertEquals("cg", partitionOwnership.consumerGroupName());
                assertEquals("etag", partitionOwnership.eTag());
            }).verifyComplete();
    }

    @Test
    public void testUpdateCheckpoint() {
        Checkpoint checkpoint = new Checkpoint()
            .eventHubName("eh")
            .consumerGroupName("cg")
            .ownerId("owner1")
            .partitionId("0")
            .eTag("etag")
            .sequenceNumber(2L)
            .offset("100");

        Map<String, String> headers = new HashMap<>();
        headers.put("eTag", "etag2");
        when(containerAsyncClient.getBlobAsyncClient("eh/cg/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.setMetadataWithResponse(any(Metadata.class), any(BlobAccessConditions.class)))
            .thenReturn(Mono.just(new VoidResponse(null, 200, new HttpHeaders(headers))));

        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(containerAsyncClient);
        StepVerifier.create(blobPartitionManager.updateCheckpoint(checkpoint))
            .assertNext(etag -> assertEquals("etag2", etag)).verifyComplete();
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testClaimOwnership() {
        PartitionOwnership po = createPartitionOwnership("eh", "cg", "0", "owner1");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.put("eTag", "etag2");

        when(containerAsyncClient.getBlobAsyncClient("eh/cg/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.asBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L),
            isNull(), any(Metadata.class), any(BlobAccessConditions.class)))
            .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));

        BlobPartitionManager blobPartitionManager = new BlobPartitionManager(containerAsyncClient);
        StepVerifier.create(blobPartitionManager.claimOwnership(po))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.ownerId());
                assertEquals("0", partitionOwnership.partitionId());
                assertEquals("eh", partitionOwnership.eventHubName());
                assertEquals("cg", partitionOwnership.consumerGroupName());
                assertEquals("etag2", partitionOwnership.eTag());
                assertNull(partitionOwnership.sequenceNumber());
                assertNull(partitionOwnership.offset());
            }).verifyComplete();
    }

    private PartitionOwnership createPartitionOwnership(String eventHubName, String consumerGroupName,
        String partitionId, String ownerId) {
        return new PartitionOwnership()
            .eventHubName(eventHubName)
            .consumerGroupName(consumerGroupName)
            .partitionId(partitionId)
            .ownerId(ownerId);
    }

    private BlobItem getBlobItem(String owner, String sequenceNumber, String offset, String etag, String blobName) {
        Metadata metadata = getMetadata(owner, sequenceNumber, offset);

        BlobProperties properties = new BlobProperties()
            .lastModified(OffsetDateTime.now())
            .etag(etag);

        return new BlobItem()
            .name(blobName)
            .metadata(metadata)
            .properties(properties);
    }

    private Metadata getMetadata(String owner, String sequenceNumber, String offset) {
        Metadata metadata = new Metadata();
        metadata.put("ownerId", owner);
        metadata.put("sequenceNumber", sequenceNumber);
        metadata.put("offset", offset);
        return metadata;
    }
}
