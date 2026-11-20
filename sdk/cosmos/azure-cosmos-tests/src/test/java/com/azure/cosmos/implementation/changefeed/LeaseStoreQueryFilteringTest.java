// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedQueryFilter;
import com.azure.cosmos.implementation.changefeed.common.LeaseVersion;
import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.changefeed.pkversion.ServiceItemLease;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeaseStoreQueryFilteringTest {
    private static final String QUERY = "SELECT * FROM c WHERE c.age > 5";

    @DataProvider(name = "leaseVersions")
    public Object[][] leaseVersions() {
        return new Object[][] { { false }, { true } };
    }

    @Test(groups = "unit", dataProvider = "leaseVersions")
    public void initializedMarkerValidatesExactQueryIncludingLegacyCompatibility(boolean epk) {
        for (String configured : new String[] { null, QUERY }) {
            for (String persisted : new String[] { null, QUERY, QUERY + " " }) {
                ChangeFeedContextClient client = mock(ChangeFeedContextClient.class);
                InternalObjectNode marker = marker(persisted);
                CosmosItemResponse<InternalObjectNode> markerResponse = response(marker);
                when(client.readItem(anyString(), any(PartitionKey.class), any(), eq(InternalObjectNode.class)))
                    .thenReturn(Mono.just(markerResponse));
                LeaseStoreManager manager = manager(epk, client, configured);
                if (java.util.Objects.equals(configured, persisted)) {
                    StepVerifier.create(manager.isInitialized()).expectNext(true).verifyComplete();
                } else {
                    StepVerifier.create(manager.isInitialized())
                        .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify();
                }
            }
        }
    }

    @Test(groups = "unit", dataProvider = "leaseVersions")
    public void markerPersistsOnlyFingerprintAndConflictRevalidatesWinner(boolean epk) {
        ChangeFeedContextClient client = mock(ChangeFeedContextClient.class);
        AtomicReference<InternalObjectNode> created = new AtomicReference<>();
        when(client.createItem(any(), any(InternalObjectNode.class), any(), eq(false)))
            .thenAnswer(invocation -> {
                InternalObjectNode document = invocation.getArgument(1);
                created.set(document);
                return Mono.just(response(document));
            });
        LeaseStoreManager manager = manager(epk, client, QUERY);
        StepVerifier.create(manager.markInitialized()).expectNext(true).verifyComplete();
        assertThat(created.get().getString(ChangeFeedQueryFilter.PROPERTY_NAME))
            .isEqualTo(ChangeFeedQueryFilter.fingerprint(QUERY));
        assertThat(created.get().toJson()).doesNotContain(QUERY);

        when(client.createItem(any(), any(InternalObjectNode.class), any(), eq(false)))
            .thenReturn(Mono.error(BridgeInternal.createCosmosException(409, "Marker exists")));
        CosmosItemResponse<InternalObjectNode> winner = response(marker(QUERY + " "));
        when(client.readItem(anyString(), any(PartitionKey.class), any(), eq(InternalObjectNode.class)))
            .thenReturn(Mono.just(winner));
        StepVerifier.create(manager.markInitialized())
            .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify();
    }

    @Test(groups = "unit", dataProvider = "leaseVersions")
    public void createdLeaseFingerprintSurvivesSerializationAndReload(boolean epk) {
        for (String configured : new String[] { null, QUERY }) {
            ChangeFeedContextClient client = mock(ChangeFeedContextClient.class);
            AtomicReference<InternalObjectNode> persisted = new AtomicReference<>();
            when(client.createItem(any(), any(Lease.class), any(), eq(false)))
                .thenAnswer(invocation -> {
                    ObjectNode serialized = Utils.getSimpleObjectMapper().valueToTree(invocation.getArgument(1));
                    InternalObjectNode document = new InternalObjectNode(serialized);
                    persisted.set(document);
                    return Mono.just(response(document));
                });
            LeaseStoreManager store = manager(epk, client, configured);
            Mono<Lease> created = epk
                ? store.createLeaseIfNotExist(FeedRangeEpkImpl.forFullRange(), null)
                : store.createLeaseIfNotExist("0", null);
            StepVerifier.create(created).assertNext(lease ->
                ChangeFeedQueryFilter.validateProperties(ChangeFeedQueryFilter.fingerprint(configured),
                    lease.getProperties())).verifyComplete();

            assertThat(persisted.get().getString(ChangeFeedQueryFilter.PROPERTY_NAME))
                .isEqualTo(ChangeFeedQueryFilter.fingerprint(configured));
            assertThat(persisted.get().toJson()).doesNotContain(QUERY);
            Lease reloaded = epk ? ServiceItemLeaseV1.fromDocument(persisted.get())
                : ServiceItemLease.fromDocument(persisted.get());
            ChangeFeedQueryFilter.validateProperties(ChangeFeedQueryFilter.fingerprint(configured),
                reloaded.getProperties());
        }
    }

    @Test(groups = "unit", dataProvider = "leaseVersions")
    public void partiallyInitializedLeasesCannotSilentlyChangeQuery(boolean epk) {
        ChangeFeedContextClient client = mock(ChangeFeedContextClient.class);
        Map<String, String> properties = ChangeFeedQueryFilter.withFingerprint(null,
            ChangeFeedQueryFilter.fingerprint(QUERY));
        Lease storedLease = epk
            ? new ServiceItemLeaseV1().withVersion(LeaseVersion.EPK_RANGE_BASED_LEASE)
                .withId("prefix..0").withLeaseToken("0").withFeedRange(FeedRangeEpkImpl.forFullRange())
                .withProperties(properties)
            : new ServiceItemLease().withId("prefix..0").withLeaseToken("0").withProperties(properties);
        ObjectNode serializedLease = Utils.getSimpleObjectMapper().valueToTree(storedLease);
        InternalObjectNode lease = new InternalObjectNode(serializedLease);
        @SuppressWarnings("unchecked")
        FeedResponse<InternalObjectNode> page = mock(FeedResponse.class);
        when(page.getResults()).thenReturn(Collections.singletonList(lease));
        when(client.queryItems(any(), any(), any(), eq(InternalObjectNode.class))).thenReturn(Flux.just(page));
        StepVerifier.create(manager(epk, client, QUERY).getAllLeases()).expectNextCount(1).verifyComplete();
        StepVerifier.create(manager(epk, client, null).getAllLeases())
            .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify();
        StepVerifier.create(manager(epk, client, QUERY + " ").getAllLeases())
            .expectError(ChangeFeedQueryFilter.QueryFilterMismatchException.class).verify();
    }

    private LeaseStoreManager manager(boolean epk, ChangeFeedContextClient client, String query) {
        LeaseStoreManager.LeaseStoreManagerBuilderDefinition builder = epk
            ? com.azure.cosmos.implementation.changefeed.epkversion.LeaseStoreManagerImpl.builder()
            : com.azure.cosmos.implementation.changefeed.pkversion.LeaseStoreManagerImpl.builder();
        return builder.leaseContextClient(client).leasePrefix("prefix")
            .leaseCollectionLink(mock(CosmosAsyncContainer.class))
            .requestOptionsFactory(mock(RequestOptionsFactory.class)).hostName("host").query(query).build();
    }

    private InternalObjectNode marker(String query) {
        InternalObjectNode marker = new InternalObjectNode();
        marker.setId("prefix.info");
        String fingerprint = ChangeFeedQueryFilter.fingerprint(query);
        if (fingerprint != null) {
            marker.set(ChangeFeedQueryFilter.PROPERTY_NAME, fingerprint);
        }
        return marker;
    }

    private CosmosItemResponse<InternalObjectNode> response(InternalObjectNode document) {
        RxDocumentServiceResponse backend = mock(RxDocumentServiceResponse.class);
        when(backend.hasPayload()).thenReturn(true);
        when(backend.getResponseBody()).thenReturn(document.getPropertyBag());
        return ImplementationBridgeHelpers.CosmosItemResponseHelper.getCosmosItemResponseBuilderAccessor()
            .createCosmosItemResponse(new ResourceResponse<>(backend, Document.class), InternalObjectNode.class, null);
    }
}
