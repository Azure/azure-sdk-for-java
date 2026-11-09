// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosBridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.common.PartitionedByIdCollectionRequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.exceptions.LeaseLostException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.JsonNode;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Validates that change feed processor lease writes (partition key range id version) are issued with content response on write disabled
 * and that the lease concurrency token (ETag) is correctly taken from the response headers.
 */
public class LeaseWritesWithoutContentResponseTests {
    private static final String OWNER = "host-1";
    private static final String LEASE_PREFIX = "prefix";

    @Test(groups = "unit")
    public void updateLeaseWithoutContentResponseUsesETagFromResponseHeader() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        List<RecordedWrite> replaces = recordReplaces(clientMock, noContentResponse("etag-2"));

        DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
        ServiceItemLease cachedLease = createLease("etag-1");

        Lease updatedLease = updater.updateLease(
            cachedLease,
            cachedLease.getId(),
            new PartitionKey(cachedLease.getId()),
            new CosmosItemRequestOptions(),
            lease -> {
                lease.setContinuationToken("continuation-2");
                return lease;
            }).block();

        assertThat(updatedLease).isSameAs(cachedLease);
        assertThat(updatedLease.getConcurrencyToken()).isEqualTo("etag-2");
        assertThat(updatedLease.getContinuationToken()).isEqualTo("continuation-2");
        assertThat(updatedLease.getOwner()).isEqualTo(OWNER);

        assertThat(replaces).hasSize(1);
        assertThat(replaces.get(0).ifMatchETag).isEqualTo("etag-1");
        assertThat(replaces.get(0).contentResponseOnWriteEnabled).isFalse();
        verify(clientMock, never()).readItem(anyString(), any(), any(), any());
    }

    @Test(groups = "unit")
    public void sequentialUpdatesUseLatestETagAsIfMatch() {
        // Regression test for the stale If-Match ETag issue - each update must use the ETag returned by the previous
        // write, otherwise every subsequent checkpoint would fail with 412 and require a recovery read.
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        List<RecordedWrite> replaces = recordReplaces(
            clientMock,
            noContentResponse("etag-2"),
            noContentResponse("etag-3"),
            noContentResponse("etag-4"));

        DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
        ServiceItemLease cachedLease = createLease("etag-1");

        for (int i = 2; i <= 4; i++) {
            String continuation = "continuation-" + i;
            Lease updatedLease = updater.updateLease(
                cachedLease,
                cachedLease.getId(),
                new PartitionKey(cachedLease.getId()),
                new CosmosItemRequestOptions(),
                lease -> {
                    lease.setContinuationToken(continuation);
                    return lease;
                }).block();

            assertThat(updatedLease.getConcurrencyToken()).isEqualTo("etag-" + i);
            assertThat(updatedLease.getContinuationToken()).isEqualTo(continuation);
        }

        assertThat(replaces).extracting(write -> write.ifMatchETag).containsExactly("etag-1", "etag-2", "etag-3");
        assertThat(replaces).allMatch(write -> !write.contentResponseOnWriteEnabled);
        verify(clientMock, never()).readItem(anyString(), any(), any(), any());
    }

    @Test(groups = "unit")
    public void updateLeaseDisablesContentResponseEvenWithNullRequestOptions() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        List<RecordedWrite> replaces = recordReplaces(clientMock, noContentResponse("etag-2"));

        DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
        ServiceItemLease cachedLease = createLease("etag-1");

        Lease updatedLease = updater.updateLease(
            cachedLease, cachedLease.getId(), new PartitionKey(cachedLease.getId()), null, lease -> lease).block();

        assertThat(updatedLease.getConcurrencyToken()).isEqualTo("etag-2");
        assertThat(replaces).hasSize(1);
        assertThat(replaces.get(0).ifMatchETag).isEqualTo("etag-1");
        assertThat(replaces.get(0).contentResponseOnWriteEnabled).isFalse();
    }

    @Test(groups = "unit")
    public void updateLeaseWithContentResponseStillUsesDocument() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        CosmosItemResponse<Object> response = contentResponse(
            leaseDocument("lease-id", "etag-from-body", OWNER, "continuation-from-body"),
            "etag-from-header");
        recordReplaces(clientMock, response);

        DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
        ServiceItemLease cachedLease = createLease("etag-1");

        Lease updatedLease = updater.updateLease(
            cachedLease, cachedLease.getId(), new PartitionKey(cachedLease.getId()), new CosmosItemRequestOptions(),
            lease -> lease).block();

        assertThat(updatedLease.getConcurrencyToken()).isEqualTo("etag-from-body");
        // Legacy behavior: setServiceItemLease keeps the cached (submitted) continuation token.
        assertThat(updatedLease.getContinuationToken()).isEqualTo("continuation-1");
    }

    @Test(groups = "unit")
    public void updateLeaseWithoutETagKeepsCachedTokenAndRecoversOnNextUpdate() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        List<RecordedWrite> replaces = recordReplaces(
            clientMock,
            noContentResponse(null),
            preconditionFailed(),
            noContentResponse("etag-4"));
        List<String> reads = recordReads(clientMock, leaseDocument("lease-id", "etag-3", OWNER, "continuation-2"));

        DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
        ServiceItemLease cachedLease = createLease("etag-1");

        Lease firstUpdate = updater.updateLease(
            cachedLease, cachedLease.getId(), new PartitionKey(cachedLease.getId()), new CosmosItemRequestOptions(),
            lease -> lease).block();
        assertThat(firstUpdate.getConcurrencyToken()).isEqualTo("etag-1");

        Lease secondUpdate = updater.updateLease(
            cachedLease, cachedLease.getId(), new PartitionKey(cachedLease.getId()), new CosmosItemRequestOptions(),
            lease -> {
                lease.setContinuationToken("continuation-3");
                return lease;
            }).block();

        assertThat(secondUpdate.getConcurrencyToken()).isEqualTo("etag-4");
        assertThat(secondUpdate.getContinuationToken()).isEqualTo("continuation-3");
        assertThat(replaces).extracting(write -> write.ifMatchETag).containsExactly("etag-1", "etag-1", "etag-3");
        assertThat(reads).hasSize(1);
    }

    @Test(groups = "unit")
    public void updateLeasePreconditionFailedRefreshesETagAndRetries() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        List<RecordedWrite> replaces = recordReplaces(clientMock, preconditionFailed(), noContentResponse("etag-3"));
        List<String> reads = recordReads(clientMock, leaseDocument("lease-id", "etag-2", OWNER, "continuation-1"));

        DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
        ServiceItemLease cachedLease = createLease("etag-1");

        Lease updatedLease = updater.updateLease(
            cachedLease, cachedLease.getId(), new PartitionKey(cachedLease.getId()), new CosmosItemRequestOptions(),
            lease -> {
                lease.setContinuationToken("continuation-2");
                return lease;
            }).block();

        assertThat(updatedLease.getConcurrencyToken()).isEqualTo("etag-3");
        assertThat(updatedLease.getContinuationToken()).isEqualTo("continuation-2");
        assertThat(replaces).extracting(write -> write.ifMatchETag).containsExactly("etag-1", "etag-2");
        assertThat(replaces).allMatch(write -> !write.contentResponseOnWriteEnabled);
        assertThat(reads).containsExactly("lease-id");
    }

    @Test(groups = "unit")
    public void updateLeasePreconditionFailedWithNewOwnerThrowsLeaseLost() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        recordReplaces(clientMock, preconditionFailed());
        recordReads(clientMock, leaseDocument("lease-id", "etag-2", "host-2", "continuation-1"));

        DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
        ServiceItemLease cachedLease = createLease("etag-1");

        StepVerifier.create(updater.updateLease(
                cachedLease, cachedLease.getId(), new PartitionKey(cachedLease.getId()), new CosmosItemRequestOptions(),
                lease -> lease))
            .expectError(LeaseLostException.class)
            .verify();
    }

    @Test(groups = "unit")
    public void updateLeaseConflictOrNotFoundThrowsLeaseLost() {
        for (int statusCode : new int[] { 409, 404 }) {
            ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
            recordReplaces(clientMock, BridgeInternal.createCosmosException(statusCode));

            DocumentServiceLeaseUpdaterImpl updater = new DocumentServiceLeaseUpdaterImpl(clientMock);
            ServiceItemLease cachedLease = createLease("etag-1");

            StepVerifier.create(updater.updateLease(
                    cachedLease, cachedLease.getId(), new PartitionKey(cachedLease.getId()), new CosmosItemRequestOptions(),
                    lease -> lease))
                .expectError(LeaseLostException.class)
                .verify();
        }
    }

    @Test(groups = "unit")
    public void leaseStoreManagerAcquireThenUpdatePropertiesChainsETags() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        List<RecordedWrite> replaces = recordReplaces(
            clientMock,
            noContentResponse("etag-2"),
            noContentResponse("etag-3"));

        LeaseStoreManager leaseStoreManager = createLeaseStoreManager(clientMock);
        ServiceItemLease lease = createLease("etag-1");
        lease.setOwner("host-0");

        Lease acquiredLease = leaseStoreManager.acquire(lease).block();
        assertThat(acquiredLease.getOwner()).isEqualTo(OWNER);
        assertThat(acquiredLease.getConcurrencyToken()).isEqualTo("etag-2");

        acquiredLease.setProperties(Collections.singletonMap("key", "value"));
        Lease updatedLease = leaseStoreManager.updateProperties(acquiredLease).block();
        assertThat(updatedLease.getConcurrencyToken()).isEqualTo("etag-3");
        assertThat(updatedLease.getProperties()).containsEntry("key", "value");

        assertThat(replaces).extracting(write -> write.ifMatchETag).containsExactly("etag-1", "etag-2");
        assertThat(replaces).allMatch(write -> !write.contentResponseOnWriteEnabled);
        verify(clientMock, never()).readItem(anyString(), any(), any(), any());
    }

    @Test(groups = "unit")
    public void createLeaseIfNotExistWithoutContentResponseUsesETagFromResponseHeader() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        List<RecordedWrite> creates = recordCreates(clientMock, noContentResponse("etag-created"));

        LeaseStoreManager leaseStoreManager = createLeaseStoreManager(clientMock);
        Lease lease = leaseStoreManager.createLeaseIfNotExist("0", "continuation-0").block();

        assertThat(lease).isNotNull();
        assertThat(lease.getId()).isEqualTo(LEASE_PREFIX + "..0");
        assertThat(lease.getLeaseToken()).isEqualTo("0");
        assertThat(lease.getContinuationToken()).isEqualTo("continuation-0");
        assertThat(lease.getConcurrencyToken()).isEqualTo("etag-created");
        assertThat(lease.toString()).isNotNull();
        assertThat(creates).hasSize(1);
        assertThat(creates.get(0).contentResponseOnWriteEnabled).isFalse();
    }

    @Test(groups = "unit")
    public void createLeaseIfNotExistConflictReturnsEmpty() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        recordCreates(clientMock, BridgeInternal.createCosmosException(409));

        LeaseStoreManager leaseStoreManager = createLeaseStoreManager(clientMock);
        StepVerifier.create(leaseStoreManager.createLeaseIfNotExist("0", "continuation-0"))
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void leaseStoreInitializationWritesWithoutContentResponse() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        CosmosAsyncContainer leaseContainer = mock(CosmosAsyncContainer.class);
        List<RecordedWrite> creates = recordCreates(
            clientMock,
            noContentResponse("lock-etag"),
            noContentResponse("marker-etag"));
        List<CosmosItemRequestOptions> deletes = new ArrayList<>();
        doAnswer(invocation -> {
            CosmosItemRequestOptions options = invocation.getArgument(2);
            deletes.add(options);
            return Mono.just(noContentResponse(null));
        }).when(clientMock).deleteItem(anyString(), any(), any());

        LeaseStoreImpl leaseStore = new LeaseStoreImpl(
            clientMock, LEASE_PREFIX, leaseContainer, new PartitionedByIdCollectionRequestOptionsFactory());

        StepVerifier.create(leaseStore.acquireInitializationLock(Duration.ofSeconds(30)))
            .expectNext(true)
            .verifyComplete();
        StepVerifier.create(leaseStore.markInitialized())
            .expectNext(true)
            .verifyComplete();
        leaseStore.releaseInitializationLock().block();

        assertThat(creates).hasSize(2);
        assertThat(creates).allMatch(write -> !write.contentResponseOnWriteEnabled);
        assertThat(creates.get(0).documentId).isEqualTo(LEASE_PREFIX + ".lock");
        assertThat(creates.get(1).documentId).isEqualTo(LEASE_PREFIX + ".info");
        assertThat(deletes).hasSize(1);
        assertThat(deletes.get(0).getIfMatchETag()).isEqualTo("lock-etag");
    }

    @Test(groups = "unit")
    public void leaseStoreAcquireInitializationLockConflictReturnsFalse() {
        ChangeFeedContextClient clientMock = mock(ChangeFeedContextClient.class);
        recordCreates(clientMock, BridgeInternal.createCosmosException(409));

        LeaseStoreImpl leaseStore = new LeaseStoreImpl(
            clientMock, LEASE_PREFIX, mock(CosmosAsyncContainer.class), new PartitionedByIdCollectionRequestOptionsFactory());

        StepVerifier.create(leaseStore.acquireInitializationLock(Duration.ofSeconds(30)))
            .expectNext(false)
            .verifyComplete();
    }

    @Test(groups = "unit")
    public void changeFeedProcessorAllowsLeaseClientWithContentResponseOnWriteDisabled() {
        CosmosAsyncContainer feedContainer = mockContainer(true);
        CosmosAsyncContainer leaseContainer = mockContainer(false);

        Consumer<List<JsonNode>> handler = docs -> { };
        ChangeFeedProcessor processor = new ChangeFeedProcessorBuilder()
            .hostName(OWNER)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleChanges(handler)
            .buildChangeFeedProcessor();
        assertThat(processor).isNotNull();
    }

    static CosmosAsyncContainer mockContainer(boolean contentResponseOnWriteEnabled) {
        CosmosAsyncContainer container = mock(CosmosAsyncContainer.class, Mockito.RETURNS_DEEP_STUBS);
        AsyncDocumentClient documentClient = mock(AsyncDocumentClient.class);
        when(documentClient.isContentResponseOnWriteEnabled()).thenReturn(contentResponseOnWriteEnabled);
        when(documentClient.getConsistencyLevel()).thenReturn(ConsistencyLevel.SESSION);
        when(CosmosBridgeInternal.getContextClient(container)).thenReturn(documentClient);
        return container;
    }

    private static LeaseStoreManager createLeaseStoreManager(ChangeFeedContextClient clientMock) {
        return LeaseStoreManagerImpl.builder()
            .leasePrefix(LEASE_PREFIX)
            .leaseCollectionLink(mock(CosmosAsyncContainer.class))
            .leaseContextClient(clientMock)
            .requestOptionsFactory(new PartitionedByIdCollectionRequestOptionsFactory())
            .hostName(OWNER)
            .build();
    }

    private static ServiceItemLease createLease(String etag) {
        ServiceItemLease lease = new ServiceItemLease()
            .withId("lease-id")
            .withLeaseToken("0")
            .withContinuationToken("continuation-1")
            .withOwner(OWNER)
            .withETag(etag);
        return lease;
    }

    private static InternalObjectNode leaseDocument(String id, String etag, String owner, String continuationToken) {
        InternalObjectNode document = new InternalObjectNode();
        document.setId(id);
        document.set("_etag", etag);
        document.set("LeaseToken", "0");
        document.set("Owner", owner);
        document.set("ContinuationToken", continuationToken);
        document.set("_ts", "0");
        return document;
    }

    @SuppressWarnings("unchecked")
    private static CosmosItemResponse<Object> noContentResponse(String etag) {
        CosmosItemResponse<Object> response = mock(CosmosItemResponse.class);
        when(response.getETag()).thenReturn(etag);
        return response;
    }

    @SuppressWarnings("unchecked")
    private static CosmosItemResponse<Object> contentResponse(InternalObjectNode document, String etag) {
        ResourceResponse<Document> resourceResponse = mock(ResourceResponse.class);
        when(resourceResponse.hasPayload()).thenReturn(true);
        when(resourceResponse.getBody()).thenReturn(document.getPropertyBag());
        when(resourceResponse.getETag()).thenReturn(etag);
        return ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor()
            .createCosmosItemResponse(resourceResponse, Object.class, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    private static CosmosException preconditionFailed() {
        return BridgeInternal.createCosmosException(412);
    }

    private static Mono<?> toMono(Object result) {
        if (result instanceof Throwable) {
            return Mono.error((Throwable) result);
        }
        return Mono.just(result);
    }

    private static List<RecordedWrite> recordReplaces(ChangeFeedContextClient clientMock, Object... results) {
        List<RecordedWrite> writes = new ArrayList<>();
        doAnswer(invocation -> {
            CosmosItemRequestOptions options = invocation.getArgument(3);
            writes.add(new RecordedWrite(invocation.getArgument(0), options));
            return toMono(results[Math.min(writes.size(), results.length) - 1]);
        }).when(clientMock).replaceItem(anyString(), any(), any(), any());
        return writes;
    }

    private static List<RecordedWrite> recordCreates(ChangeFeedContextClient clientMock, Object... results) {
        List<RecordedWrite> writes = new ArrayList<>();
        doAnswer(invocation -> {
            Object document = invocation.getArgument(1);
            String id = document instanceof Lease
                ? ((Lease) document).getId()
                : ((InternalObjectNode) document).getId();
            CosmosItemRequestOptions options = invocation.getArgument(2);
            writes.add(new RecordedWrite(id, options));
            return toMono(results[Math.min(writes.size(), results.length) - 1]);
        }).when(clientMock).createItem(any(), any(), any(), anyBoolean());
        return writes;
    }

    @SuppressWarnings("unchecked")
    private static List<String> recordReads(ChangeFeedContextClient clientMock, InternalObjectNode document) {
        List<String> reads = new ArrayList<>();
        doAnswer(invocation -> {
            reads.add(invocation.getArgument(0));
            return Mono.just(contentResponse(document, document.getETag()));
        }).when(clientMock).readItem(anyString(), any(), any(), eq(InternalObjectNode.class));
        return reads;
    }

    private static final class RecordedWrite {
        private final String documentId;
        private final String ifMatchETag;
        private final boolean contentResponseOnWriteEnabled;

        private RecordedWrite(String documentId, CosmosItemRequestOptions options) {
            this.documentId = documentId;
            this.ifMatchETag = options.getIfMatchETag();
            Boolean enabled = options.isContentResponseOnWriteEnabled();
            this.contentResponseOnWriteEnabled = enabled == null || enabled;
        }
    }
}
