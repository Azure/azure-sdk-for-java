// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.models.Checkpoint;
import com.azure.messaging.eventhubs.models.PartitionOwnership;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobItemProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlockBlobItem;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.SocketTimeoutException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.CHECKPOINT_PATH;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.OFFSET;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.OWNERSHIP_PATH;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.OWNER_ID;
import static com.azure.messaging.eventhubs.checkpointstore.blob.BlobCheckpointStore.SEQUENCE_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link BlobCheckpointStore}.
 */
public class BlobCheckpointStoreTests {

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

        BlobItem blobItem = getOwnershipBlobItem("owner1", "etag", ownershipPrefix + "0"); // valid blob
        BlobItem blobItem2 = getOwnershipBlobItem("owner1", "etag", prefix + "/0"); // invalid name
        BlobItem blobItem3 = new BlobItem().setName(ownershipPrefix + "5"); // no metadata
        BlobItem blobItem4 = getOwnershipBlobItem(null, "2", ownershipPrefix + "2"); // valid blob with null ownerid

        PagedFlux<BlobItem> response
            = new PagedFlux<>(() -> Mono.just(new PagedResponseBase<HttpHeaders, BlobItem>(null, 200, null,
                Arrays.asList(blobItem, blobItem2, blobItem3, blobItem4), null, null)));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            PagedFlux<BlobItem> listBlobsInternal(String prefix) {
                if (ownershipPrefix.equals(prefix)) {
                    return response;
                } else {
                    return FluxUtil.pagedFluxError(new ClientLogger(BlobCheckpointStoreTests.class),
                        new IllegalArgumentException("Did not expect this prefix: " + prefix));
                }
            }
        };

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
        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.error(new SocketTimeoutException()));
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            PagedFlux<BlobItem> listBlobsInternal(String prefix) {
                return response;
            }
        };

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

        final BlobItem blobItem = getCheckpointBlobItem("230", "1", checkpointPrefix + "0"); // valid blob
        final BlobItem blobItem2 = new BlobItem().setName(checkpointPrefix + "1"); // valid blob but not a valid checkpoint.
        final BlobItem blobItem3 = getCheckpointBlobItem("233", "3", prefix + "1"); // invalid name
        final PagedFlux<BlobItem> response
            = new PagedFlux<>(() -> Mono.just(new PagedResponseBase<HttpHeaders, BlobItem>(null, 200, null,
                Arrays.asList(blobItem, blobItem2, blobItem3), null, null)));

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            PagedFlux<BlobItem> listBlobsInternal(String prefix) {
                if (checkpointPrefix.equals(prefix)) {
                    return response;
                } else {
                    return FluxUtil.pagedFluxError(new ClientLogger(BlobCheckpointStoreTests.class),
                        new IllegalArgumentException("Did not expect this prefix: " + prefix));
                }
            }
        };

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
        PagedFlux<BlobItem> response = new PagedFlux<>(() -> Mono.error(new SocketTimeoutException()));
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            PagedFlux<BlobItem> listBlobsInternal(String prefix) {
                return response;
            }
        };

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

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            PagedFlux<BlobItem> listBlobsInternal(String prefix) {
                return response;
            }

            @Override
            BlobWrapper getBlobAsyncClient(String name) {
                if (Objects.equals(blobName, name)) {
                    return new BlobWrapper(null) {
                        @Override
                        Mono<Boolean> exists() {
                            return Mono.just(true);
                        }

                        @Override
                        Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
                            BlobRequestConditions requestConditions) {
                            return Mono.empty();
                        }
                    };
                }

                return null;
            }
        };

        // Act & Assert
        StepVerifier.create(blobCheckpointStore.updateCheckpoint(checkpoint)).verifyComplete();
    }

    /**
     * Tests that errors are thrown if the checkpoint is invalid
     */
    @Test
    public void testUpdateCheckpointInvalid() {
        // Arrange
        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null);

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

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            PagedFlux<BlobItem> listBlobsInternal(String prefix) {
                return response;
            }

            @Override
            BlobWrapper getBlobAsyncClient(String name) {
                if (Objects.equals(blobName, name)) {
                    return new BlobWrapper(null) {
                        @Override
                        Mono<Boolean> exists() {
                            return Mono.just(false);
                        }

                        @Override
                        Mono<Response<BlockBlobItem>> uploadWithResponse(Map<String, String> metadata,
                            BlobRequestConditions blobRequestConditions) {
                            return Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null));
                        }
                    };
                }

                return null;
            }
        };

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

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            BlobWrapper getBlobAsyncClient(String name) {
                if (Objects.equals("ns/eh/cg/ownership/1", name)) {
                    return new BlobWrapper(null) {
                        @Override
                        Mono<Response<BlockBlobItem>> uploadWithResponse(Map<String, String> metadata,
                            BlobRequestConditions blobRequestConditions) {
                            return Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null));
                        }
                    };
                }

                return null;
            }
        };

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

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            BlobWrapper getBlobAsyncClient(String name) {
                if (Objects.equals("ns/eh/cg/ownership/0", name)) {
                    return new BlobWrapper(null) {
                        @Override
                        Mono<Boolean> exists() {
                            return Mono.just(true);
                        }

                        @Override
                        Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
                            BlobRequestConditions requestConditions) {
                            return Mono.just(new ResponseBase<>(null, 200, httpHeaders, null, null));
                        }
                    };
                }

                return null;
            }
        };

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

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            BlobWrapper getBlobAsyncClient(String name) {
                if (Objects.equals(ownershipPath, name)) {
                    return new BlobWrapper(null) {
                        @Override
                        Mono<Boolean> exists() {
                            return Mono.just(true);
                        }

                        @Override
                        Mono<Response<BlockBlobItem>> uploadWithResponse(Map<String, String> metadata,
                            BlobRequestConditions blobRequestConditions) {
                            return Mono.error(new ResourceModifiedException("Etag did not match", null));
                        }

                        @Override
                        Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
                            BlobRequestConditions requestConditions) {
                            return Mono.error(new ResourceModifiedException("Etag did not match", null));
                        }
                    };
                }

                return null;
            }
        };

        // Act & Assert
        StepVerifier.create(blobCheckpointStore.claimOwnership(Collections.singletonList(po))).verifyComplete();

        // 2. Test that when we are "updating" metadata, and it errors, it can return normally.
        final PartitionOwnership po2
            = createPartitionOwnership(namespace, eventHubName, consumerGroup, partitionId, ownerId).setETag("1");

        // Act & Assert
        StepVerifier.create(blobCheckpointStore.claimOwnership(Collections.singletonList(po2))).verifyComplete();

        // 3. Test when BlobAsyncClient is null, it can still return normally.
        final BlobCheckpointStore anotherCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            BlobWrapper getBlobAsyncClient(String blobName) {
                return null;
            }
        };

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

        BlobCheckpointStore blobCheckpointStore = new BlobCheckpointStore(null) {
            @Override
            BlobWrapper getBlobAsyncClient(String name) {
                if (Objects.equals("ns/eh/cg/checkpoint/0", name)) {
                    return new BlobWrapper(null) {
                        @Override
                        Mono<Boolean> exists() {
                            return Mono.just(true);
                        }

                        @Override
                        Mono<Response<Void>> setMetadataWithResponse(Map<String, String> metadata,
                            BlobRequestConditions requestConditions) {
                            return Mono.error(new SocketTimeoutException());
                        }
                    };
                }

                return null;
            }
        };

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
