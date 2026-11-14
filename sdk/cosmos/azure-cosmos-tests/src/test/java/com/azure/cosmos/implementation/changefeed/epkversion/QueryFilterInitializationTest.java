// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.Bootstrapper;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseStore;
import com.azure.cosmos.implementation.changefeed.LeaseStoreManager;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedQueryFilter;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class QueryFilterInitializationTest {
    private static final String QUERY_A = "SELECT * FROM c WHERE c.a = 1";
    private static final String QUERY_B = "SELECT * FROM c WHERE c.a = 2";

    @DataProvider(name = "filterChanges")
    public Object[][] filterChanges() {
        return new Object[][] {
            { null, QUERY_B, false }, { QUERY_A, null, false }, { QUERY_A, QUERY_B, false },
            { null, QUERY_B, true }, { QUERY_A, null, true }, { QUERY_A, QUERY_B, true }
        };
    }

    @Test(groups = "unit", dataProvider = "filterChanges")
    public void realSynchronizerRejectsPartialEpkLeasesWithoutPoisoningMarker(
        String original, String changed, boolean migration) {
        Lease lease = new ServiceItemLeaseV1().withId("prefix..0").withLeaseToken("0")
            .withVersion(LeaseVersion.EPK_RANGE_BASED_LEASE).withFeedRange(FeedRangeEpkImpl.forFullRange())
            .withProperties(ChangeFeedQueryFilter.withFingerprint(null, ChangeFeedQueryFilter.fingerprint(original)));
        AtomicReference<String> marker = new AtomicReference<>();
        Harness rejected = new Harness(changed, lease, null, marker, migration);
        StepVerifier.create(rejected.bootstrapper.initialize())
            .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify(Duration.ofSeconds(5));
        assertThat(marker).hasValue(null);
        verify(rejected.store).releaseInitializationLock();
        verify(rejected.primary, never()).createLeaseIfNotExist(any(FeedRangeEpkImpl.class), any());
        verify(rejected.auxiliary, never()).markInitialized();
        if (migration) {
            verify(rejected.auxiliary).releaseInitializationLock();
        }

        Harness recovered = new Harness(original, lease, null, marker, migration);
        StepVerifier.create(recovered.bootstrapper.initialize()).verifyComplete();
        assertThat(marker).hasValue(ChangeFeedQueryFilter.fingerprint(original) == null
            ? "unfiltered" : ChangeFeedQueryFilter.fingerprint(original));
        verify(recovered.primary, never()).createLeaseIfNotExist(any(FeedRangeEpkImpl.class), any());
    }

    @Test(groups = "unit", dataProvider = "filterChanges")
    public void missingAuxiliaryMarkerStillValidatesEveryLegacyLeaseBeforeWrites(
        String original, String changed, boolean migration) {
        Lease legacy = new ServiceItemLease().withId("prefix..0").withLeaseToken("0")
            .withProperties(ChangeFeedQueryFilter.withFingerprint(null, ChangeFeedQueryFilter.fingerprint(original)));
        AtomicReference<String> marker = new AtomicReference<>();
        Harness harness = new Harness(changed, null, legacy, marker, migration);
        StepVerifier.create(harness.bootstrapper.initialize())
            .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify(Duration.ofSeconds(5));
        assertThat(marker).hasValue(null);
        verify(harness.store, never()).acquireInitializationLock(any());
        verify(harness.auxiliary, never()).acquireInitializationLock(any());
        verify(harness.auxiliary, never()).markInitialized();
        verify(harness.primary, never()).createLeaseIfNotExist(any(FeedRangeEpkImpl.class), any());
    }

    @Test(groups = "unit")
    public void markerAbsentInterleavingIsRevalidatedUnderBothLocks() {
        for (Object[] change : filterChanges()) {
            String original = (String) change[0];
            String changed = (String) change[1];
            boolean migration = (boolean) change[2];
            Lease legacy = new ServiceItemLease().withId("prefix..0").withLeaseToken("0")
                .withProperties(ChangeFeedQueryFilter.withFingerprint(null, ChangeFeedQueryFilter.fingerprint(original)));
            AtomicReference<Lease> persistedLegacy = new AtomicReference<>();
            AtomicReference<String> marker = new AtomicReference<>();
            Harness rejected = new Harness(changed, null, persistedLegacy, marker, migration,
                () -> persistedLegacy.set(legacy));
            StepVerifier.create(rejected.bootstrapper.initialize())
                .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify(Duration.ofSeconds(5));
            assertThat(marker).hasValue(null);
            verify(rejected.auxiliary, times(2)).getAllLeases();
            verify(rejected.primary, never()).createLeaseIfNotExist(any(FeedRangeEpkImpl.class), any());
            verify(rejected.auxiliary, never()).markInitialized();
            verify(rejected.store).releaseInitializationLock();
            verify(rejected.auxiliary).releaseInitializationLock();

            Harness recovered = new Harness(original, null, persistedLegacy, marker, true, () -> { });
            StepVerifier.create(recovered.bootstrapper.initialize()).verifyComplete();
            assertThat(marker).hasValue(ChangeFeedQueryFilter.fingerprint(original) == null
                ? "unfiltered" : ChangeFeedQueryFilter.fingerprint(original));
            verify(recovered.primary).createLeaseIfNotExist(any(FeedRangeEpkImpl.class), any());
        }
    }

    @Test(groups = "unit")
    public void synchronizerRetainsUnrelatedErrorBehavior() {
        ChangeFeedContextClient client = mock(ChangeFeedContextClient.class);
        when(client.getOverlappingRanges(any(), eq(true)))
            .thenReturn(Mono.error(new IllegalStateException("unrelated routing failure")));
        PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(client, "container",
            mock(LeaseStoreManager.class), mock(LeaseStoreManager.class), 1, 1,
            new ChangeFeedProcessorOptions(), ChangeFeedMode.FULL_FIDELITY);
        StepVerifier.create(synchronizer.createMissingLeases()).verifyComplete();
    }

    private static final class Harness {
        private final LeaseStore store = mock(LeaseStore.class);
        private final LeaseStoreManager primary;
        private final LeaseStoreManager auxiliary;
        private final Bootstrapper bootstrapper;

        private Harness(String query, Lease epkLease, Lease pkLease, AtomicReference<String> marker, boolean migration) {
            this(query, epkLease, new AtomicReference<>(pkLease), marker, migration, () -> { });
        }

        private Harness(String query, Lease epkLease, AtomicReference<Lease> pkLease,
            AtomicReference<String> marker, boolean migration, Runnable beforeLegacyLockGranted) {
            primary = manager(true, query, epkLease);
            auxiliary = manager(false, query, pkLease::get);
            doReturn(Mono.just(false)).when(auxiliary).isInitialized();
            doReturn(Mono.defer(() -> {
                beforeLegacyLockGranted.run();
                return Mono.just(true);
            })).when(auxiliary).acquireInitializationLock(any());
            doReturn(Mono.just(true)).when(auxiliary).releaseInitializationLock();
            doReturn(Mono.just(true)).when(auxiliary).markInitialized();
            when(store.isInitialized()).thenAnswer(invocation -> Mono.fromSupplier(() -> {
                if (marker.get() != null) {
                    ChangeFeedQueryFilter.validate(ChangeFeedQueryFilter.fingerprint(query),
                        "unfiltered".equals(marker.get()) ? null : marker.get());
                }
                return marker.get() != null;
            }));
            when(store.acquireInitializationLock(any())).thenReturn(Mono.just(true));
            when(store.releaseInitializationLock()).thenReturn(Mono.just(true));
            when(store.markInitialized()).thenReturn(Mono.fromSupplier(() -> {
                String hash = ChangeFeedQueryFilter.fingerprint(query);
                marker.set(hash == null ? "unfiltered" : hash);
                return true;
            }));
            ChangeFeedContextClient feed = mock(ChangeFeedContextClient.class);
            when(feed.getOverlappingRanges(any(), eq(true))).thenReturn(Mono.just(Collections.singletonList(
                new PartitionKeyRange().setId("0").setMinInclusive("").setMaxExclusive("FF"))));
            ChangeFeedMode mode = migration ? ChangeFeedMode.INCREMENTAL : ChangeFeedMode.FULL_FIDELITY;
            ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions().setQuery(query);
            PartitionSynchronizerImpl synchronizer = new PartitionSynchronizerImpl(feed, "container",
                primary, primary, 1, 1, options, mode);
            bootstrapper = migration
                ? new PkRangeIdVersionLeaseStoreBootstrapperImpl(synchronizer, store,
                    Duration.ofSeconds(5), Duration.ofMillis(1), auxiliary, primary, options, mode)
                : new BootstrapperImpl(synchronizer, store, Duration.ofSeconds(5), Duration.ofMillis(1),
                    primary, auxiliary, options, mode);
        }
    }

    private static LeaseStoreManager manager(boolean epk, String query, Lease lease) {
        return manager(epk, query, () -> lease);
    }

    private static LeaseStoreManager manager(boolean epk, String query, Supplier<Lease> leaseSupplier) {
        ChangeFeedContextClient client = mock(ChangeFeedContextClient.class);
        when(client.queryItems(any(), any(), any(), eq(InternalObjectNode.class))).thenReturn(Flux.defer(() -> {
            @SuppressWarnings("unchecked")
            FeedResponse<InternalObjectNode> page = mock(FeedResponse.class);
            Lease lease = leaseSupplier.get();
            if (lease == null) {
                when(page.getResults()).thenReturn(Collections.emptyList());
            } else {
                ObjectNode serialized = Utils.getSimpleObjectMapper().valueToTree(lease);
                InternalObjectNode compatibleFirstLease = new InternalObjectNode(serialized.deepCopy());
                compatibleFirstLease.setId("prefix..compatible");
                compatibleFirstLease.set(ChangeFeedQueryFilter.PROPERTY_NAME, ChangeFeedQueryFilter.fingerprint(query));
                when(page.getResults()).thenReturn(Arrays.asList(compatibleFirstLease, new InternalObjectNode(serialized)));
            }
            return Flux.just(page);
        }));
        when(client.createItem(any(), any(Lease.class), any(), eq(false))).thenAnswer(invocation -> {
            ObjectNode serialized = Utils.getSimpleObjectMapper().valueToTree(invocation.getArgument(1));
            RxDocumentServiceResponse response = mock(RxDocumentServiceResponse.class);
            when(response.hasPayload()).thenReturn(true);
            when(response.getResponseBody()).thenReturn(serialized);
            return Mono.just(ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor()
                .createCosmosItemResponse(new ResourceResponse<>(response, Document.class), InternalObjectNode.class, null));
        });
        LeaseStoreManager.LeaseStoreManagerBuilderDefinition builder = epk
            ? LeaseStoreManagerImpl.builder()
            : com.azure.cosmos.implementation.changefeed.pkversion.LeaseStoreManagerImpl.builder();
        return spy(builder.leaseContextClient(client).leasePrefix("prefix").hostName("host").query(query)
            .leaseCollectionLink(mock(CosmosAsyncContainer.class))
            .requestOptionsFactory(mock(RequestOptionsFactory.class)).build());
    }
}
