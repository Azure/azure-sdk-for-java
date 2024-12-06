// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.ResponseBase;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobAsyncClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.JRE;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.CHECKPOINT_PATH;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.OFFSET;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.OWNERSHIP_PATH;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.OWNER_ID;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.SEQUENCE_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BlobCheckpointStore}.
 */
@DisabledOnJre(JRE.JAVA_8)
public class BlobCheckpointStoreTests {

    @Mock
    private BlobContainerAsyncClient blobContainerAsyncClient;

    @Mock
    private BlockBlobAsyncClient blockBlobAsyncClient;

    @Mock
    private BlobAsyncClient blobAsyncClient;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void beforeEach() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (autoCloseable != null) {
            autoCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Tests that listing ownership works.
     */
    @Test
    public void testListOwnership() {
        final String fullyQualifiedNamespace = "namespace.microsoft.com";
        final String eventHubName = "MyEventHubName";
        final String consumerGroup = "$Default";
        final String prefix = getLegacyPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup);
        final String ownershipPrefix = prefix + OWNERSHIP_PATH;

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        BlobItem blobItem = getOwnershipBlobItem("owner1", "etag", ownershipPrefix + "0"); // valid blob
        BlobItem blobItem2 = getOwnershipBlobItem("owner1", "etag", prefix + "/0"); // invalid name
        BlobItem blobItem3 = new BlobItem().setName(ownershipPrefix + "5"); // no metadata
        BlobItem blobItem4 = getOwnershipBlobItem(null, "2", ownershipPrefix + "2"); // valid blob with null ownerid

        PagedFlux<BlobItem> response
            = new PagedFlux<>(() -> Mono.just(new PagedResponseBase<HttpHeaders, BlobItem>(null, 200, null,
                Arrays.asList(blobItem, blobItem2, blobItem3, blobItem4), null, null)));

        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenAnswer(invocation -> {
            final ListBlobsOptions argument = invocation.getArgument(0);
            final String arg = argument.getPrefix();

            if (ownershipPrefix.equals(arg)) {
                return response;
            } else {
                return Flux.error(new IllegalArgumentException("Did not expect this prefix: " + arg));
            }
        });

        StepVerifier.create(blobCheckpointStore.listOwnership(fullyQualifiedNamespace, eventHubName, consumerGroup))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals(eventHubName, partitionOwnership.getEventHubName());
                assertEquals(consumerGroup, partitionOwnership.getConsumerGroup());
                assertEquals("etag", partitionOwnership.getETag());
                assertEquals(fullyQualifiedNamespace, partitionOwnership.getFullyQualifiedNamespace());
            })
            .assertNext(partitionOwnership -> {
                assertEquals("", partitionOwnership.getOwnerId());
                assertEquals("2", partitionOwnership.getPartitionId());
                assertEquals(eventHubName, partitionOwnership.getEventHubName());
                assertEquals(consumerGroup, partitionOwnership.getConsumerGroup());
                assertEquals("2", partitionOwnership.getETag());
                assertEquals(fullyQualifiedNamespace, partitionOwnership.getFullyQualifiedNamespace());
            })
            .verifyComplete();
    }

    /**
     * Tests that errors are propagated with {@link CheckpointStore#listOwnership(String, String, String)}.
     */
    @Test
    public void testListOwnershipError() {
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.error(new SocketTimeoutException()));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobCheckpointStore.listOwnership("ns", "eh", "cg"))
            .expectError(SocketTimeoutException.class)
            .verify();
    }

    /**
     * Verifies that it lists checkpoints.
     */
    @Test
    public void testListCheckpoint() {
        final String fullyQualifiedNamespace = "namespace.microsoft.com";
        final String eventHubName = "MyEventHubName";
        final String consumerGroup = "$Default";
        final String prefix = getLegacyPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup);
        final String checkpointPrefix = prefix + CHECKPOINT_PATH;

        final BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        final BlobItem blobItem = getCheckpointBlobItem("230", "1", checkpointPrefix + "0"); // valid blob
        final BlobItem blobItem2 = new BlobItem().setName(checkpointPrefix + "1"); // valid blob but not a valid checkpoint.
        final BlobItem blobItem3 = getCheckpointBlobItem("233", "3", prefix + "1"); // invalid name
        final PagedFlux<BlobItem> response
            = new PagedFlux<>(() -> Mono.just(new PagedResponseBase<HttpHeaders, BlobItem>(null, 200, null,
                Arrays.asList(blobItem, blobItem2, blobItem3), null, null)));

        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenAnswer(invocation -> {
            final ListBlobsOptions listBlobsOptions = invocation.getArgument(0);
            final String arg = listBlobsOptions.getPrefix();

            if (checkpointPrefix.equals(arg)) {
                return response;
            } else {
                return Flux.error(new IllegalArgumentException("Did not expect this prefix: " + arg));
            }
        });

        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        // Act & Assert
        StepVerifier.create(blobCheckpointStore.listCheckpoints(fullyQualifiedNamespace, eventHubName, consumerGroup))
            .assertNext(checkpoint -> {
                assertEquals("0", checkpoint.getPartitionId());
                assertEquals(eventHubName, checkpoint.getEventHubName());
                assertEquals(consumerGroup, checkpoint.getConsumerGroup());
                assertEquals(1L, checkpoint.getSequenceNumber());
                assertEquals(230L, checkpoint.getOffset());
            })
            .verifyComplete();
    }

    /**
     * Tests that errors are propagated with {@link CheckpointStore#listCheckpoints(String, String, String)}.
     */
    @Test
    public void testListCheckpointError() {
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.error(new SocketTimeoutException()));
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        StepVerifier.create(blobCheckpointStore.listCheckpoints("ns", "eh", "cg"))
            .expectError(SocketTimeoutException.class)
            .verify();
    }

    /**
     * Tests that can update checkpoint.
     */
    @Test
    public void testUpdateCheckpoint() {
        // Arrange
        final String fullyQualifiedNamespace = "namespace.microsoft.com";
        final String eventHubName = "MyEventHubName2";
        final String consumerGroup = "$DefaultOne";
        final String prefix = getLegacyPrefix(fullyQualifiedNamespace, eventHubName, consumerGroup);
        final String partitionId = "1";
        final String blobName = prefix + CHECKPOINT_PATH + partitionId;

        final Checkpoint checkpoint = new Checkpoint().setFullyQualifiedNamespace(fullyQualifiedNamespace)
            .setEventHubName(eventHubName)
            .setConsumerGroup(consumerGroup)
            .setPartitionId(partitionId)
            .setSequenceNumber(2L)
            .setOffset(100L);

        final BlobItem blobItem = getCheckpointBlobItem("230", "1", blobName);
        final PagedFlux<BlobItem> response
            = new PagedFlux<>(() -> Mono.just(new PagedResponseBase<HttpHeaders, BlobItem>(null, 200, null,
                Collections.singletonList(blobItem), null, null)));

        when(blobContainerAsyncClient.getBlobAsyncClient(blobName)).thenReturn(blobAsyncClient);
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blobAsyncClient.exists()).thenReturn(Mono.just(true));

        when(blobAsyncClient.setMetadata(ArgumentMatchers.<Map<String, String>>any())).thenReturn(Mono.empty());

        final BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);

        // Act & Assert
        StepVerifier.create(blobCheckpointStore.updateCheckpoint(checkpoint)).verifyComplete();
    }

    /**
     * Tests that errors are thrown if the checkpoint is invalid
     */
    @Test
    public void testUpdateCheckpointInvalid() {
        // Arrange
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> blobCheckpointStore.updateCheckpoint(null));
        assertThrows(IllegalStateException.class, () -> blobCheckpointStore.updateCheckpoint(new Checkpoint()));
    }

    /**
     * Tests that will update checkpoint if one does not exist.
     */
    @Test
    public void testUpdateCheckpointForNewPartition() {
        final Checkpoint checkpoint = new Checkpoint().setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);
        final String legacyPrefix = getLegacyPrefix(checkpoint.getFullyQualifiedNamespace(),
            checkpoint.getEventHubName(), checkpoint.getConsumerGroup());
        final String blobName = legacyPrefix + CHECKPOINT_PATH + checkpoint.getPartitionId();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaderName.ETAG, "etag2");

        BlobItem blobItem = getCheckpointBlobItem("230", "1", blobName);

        PagedFlux<BlobItem> response
            = new PagedFlux<BlobItem>(() -> Mono.just(new PagedResponseBase<HttpHeaders, BlobItem>(null, 200, null,
                Collections.singletonList(blobItem), null, null)));

        when(blobContainerAsyncClient.getBlobAsyncClient(blobName)).thenReturn(blobAsyncClient);
        when(blobContainerAsyncClient.listBlobs(any(ListBlobsOptions.class))).thenReturn(response);

        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blobAsyncClient.exists()).thenReturn(Mono.just(false));

        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L), isNull(),
            anyMap(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.updateCheckpoint(checkpoint)).verifyComplete();
    }

    /**
     * Tests claiming ownership on a partition that never had an entry.
     */
    @Test
    public void testClaimOwnership() {
        PartitionOwnership po = createPartitionOwnership("ns", "eh", "cg", "1", "owner1");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaderName.ETAG, "etag2");

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/ownership/1")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L), isNull(),
            ArgumentMatchers.<Map<String, String>>any(), isNull(), isNull(), any(BlobRequestConditions.class)))
                .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.claimOwnership(Collections.singletonList(po)))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("1", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroup());
                assertEquals("etag2", partitionOwnership.getETag());
            })
            .verifyComplete();
    }

    /**
     * Tests claiming ownership on a previously owned partition.
     */
    @Test
    public void testClaimOwnershipExistingBlob() {
        PartitionOwnership po = createPartitionOwnership("ns", "eh", "cg", "0", "owner1");
        po.setETag("1");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaderName.ETAG, "2");

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/ownership/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.setMetadataWithResponse(ArgumentMatchers.<Map<String, String>>any(),
            any(BlobRequestConditions.class)))
                .thenReturn(Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null)));
        when(blobAsyncClient.exists()).thenReturn(Mono.just(true));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.claimOwnership(Collections.singletonList(po)))
            .assertNext(partitionOwnership -> {
                assertEquals("owner1", partitionOwnership.getOwnerId());
                assertEquals("0", partitionOwnership.getPartitionId());
                assertEquals("eh", partitionOwnership.getEventHubName());
                assertEquals("cg", partitionOwnership.getConsumerGroup());
                assertEquals("2", partitionOwnership.getETag());
            })
            .verifyComplete();
    }

    /**
     * Tests that a failed ownership claim returns normally instead of throwing exception downstream.
     */
    @Test
    public void testClaimOwnershipFailed() {
        final String namespace = "foo.servicebus.windows.net";
        final String eventHubName = "test-event-hub";
        final String consumerGroup = "test-cg";
        final String partitionId = "0";
        final String ownerId = "owner-id-1";
        final PartitionOwnership po
            = createPartitionOwnership(namespace, eventHubName, consumerGroup, partitionId, ownerId);
        final String ownershipPath
            = getLegacyPrefix(namespace, eventHubName, consumerGroup) + OWNERSHIP_PATH + partitionId;

        when(blobContainerAsyncClient.getBlobAsyncClient(ownershipPath)).thenReturn(blobAsyncClient);
        when(blobAsyncClient.getBlockBlobAsyncClient()).thenReturn(blockBlobAsyncClient);
        when(blockBlobAsyncClient.uploadWithResponse(ArgumentMatchers.<Flux<ByteBuffer>>any(), eq(0L), isNull(),
            ArgumentMatchers.<Map<String, String>>any(), isNull(), isNull(), any(BlobRequestConditions.class)))
                .thenReturn(Mono.error(new ResourceModifiedException("Etag did not match", null)));

        final BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);

        // Act & Assert
        StepVerifier.create(blobCheckpointStore.claimOwnership(Collections.singletonList(po))).verifyComplete();

        // 2. Test that when we are "updating" metadata, and it errors, it can return normally.
        final PartitionOwnership po2
            = createPartitionOwnership(namespace, eventHubName, consumerGroup, partitionId, ownerId).setETag("1");

        when(blobAsyncClient.setMetadataWithResponse(ArgumentMatchers.<Map<String, String>>any(),
            any(BlobRequestConditions.class)))
                .thenReturn(Mono.error(new ResourceModifiedException("Etag did not match", null)));

        // Act & Assert
        StepVerifier.create(blobCheckpointStore.claimOwnership(Collections.singletonList(po2))).verifyComplete();

        // 3. Test when BlobAsyncClient is null, it can still return normally.
        final BlobContainerAsyncClient anotherContainerClient = mock(BlobContainerAsyncClient.class);
        final BlobCheckpointStore anotherCheckpointStore = new BlobCheckpointStore(anotherContainerClient);

        when(anotherContainerClient.getBlobAsyncClient(anyString())).thenReturn(null);

        // Act & Assert
        StepVerifier.create(anotherCheckpointStore.claimOwnership(Collections.singletonList(po))).verifyComplete();
    }

    /**
     * Tests that an error is returned when {@link CheckpointStore#updateCheckpoint(Checkpoint)} errors.
     */
    @Test
    public void testUpdateCheckpointError() {
        Checkpoint checkpoint = new Checkpoint().setFullyQualifiedNamespace("ns")
            .setEventHubName("eh")
            .setConsumerGroup("cg")
            .setPartitionId("0")
            .setSequenceNumber(2L)
            .setOffset(100L);

        when(blobContainerAsyncClient.getBlobAsyncClient("ns/eh/cg/checkpoint/0")).thenReturn(blobAsyncClient);
        when(blobAsyncClient.exists()).thenReturn(Mono.just(true));
        when(blobAsyncClient.setMetadata(ArgumentMatchers.<Map<String, String>>any()))
            .thenReturn(Mono.error(new SocketTimeoutException()));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(blobContainerAsyncClient);
        StepVerifier.create(blobCheckpointStore.updateCheckpoint(checkpoint))
            .expectError(SocketTimeoutException.class)
            .verify();
    }

    private static PartitionOwnership createPartitionOwnership(String fullyQualifiedNamespace, String eventHubName,
        String consumerGroupName, String partitionId, String ownerId) {
        return new PartitionOwnership().setFullyQualifiedNamespace(fullyQualifiedNamespace)
            .setEventHubName(eventHubName)
            .setConsumerGroup(consumerGroupName)
            .setPartitionId(partitionId)
            .setOwnerId(ownerId);
    }

    private static BlobItem getOwnershipBlobItem(String owner, String etag, String blobName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(OWNER_ID, owner);
        BlobItemProperties properties = new BlobItemProperties().setLastModified(OffsetDateTime.now()).setETag(etag);

        return new BlobItem().setName(blobName).setMetadata(metadata).setProperties(properties);
    }

    private static BlobItem getCheckpointBlobItem(String offset, String sequenceNumber, String blobName) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put(SEQUENCE_NUMBER, sequenceNumber);
        metadata.put(OFFSET, offset);
        return new BlobItem().setName(blobName).setMetadata(metadata);
    }

    private static String getLegacyPrefix(String fullyQualifiedNamespace, String eventHubName, String consumerGroup) {
        return String.join("/", fullyQualifiedNamespace, eventHubName, consumerGroup);
    }
}
