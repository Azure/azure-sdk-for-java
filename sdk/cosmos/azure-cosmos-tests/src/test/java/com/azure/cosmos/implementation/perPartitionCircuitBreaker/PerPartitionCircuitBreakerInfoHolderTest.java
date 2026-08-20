// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.CrossRegionAvailabilityContextForRxDocumentServiceRequest;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.PerPartitionAutomaticFailoverInfoHolder;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;

public class PerPartitionCircuitBreakerInfoHolderTest {

    @Test(groups = {"unit"})
    public void storesImmutableStateSnapshot() {
        LocationSpecificHealthContext healthContext = createHealthContext(LocationHealthStatus.Unavailable);
        Map<String, LocationSpecificHealthContext> currentState = new LinkedHashMap<>();
        currentState.put("eastus", healthContext);

        PerPartitionCircuitBreakerInfoHolder holder = new PerPartitionCircuitBreakerInfoHolder();
        holder.setPerPartitionCircuitBreakerInfoHolder(currentState);
        PerPartitionCircuitBreakerInfoHolder snapshot = holder.snapshot();
        currentState.clear();

        assertThat(holder.getPerPartitionCircuitBreakerInfoHolder())
            .containsOnlyKeys("eastus")
            .containsValue(healthContext);
        assertThat(snapshot.getPerPartitionCircuitBreakerInfoHolder())
            .isSameAs(holder.getPerPartitionCircuitBreakerInfoHolder());
        assertThatThrownBy(() -> holder.getPerPartitionCircuitBreakerInfoHolder().clear())
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test(groups = {"unit"})
    public void uninitializedSnapshotIsSharedAndReadOnly() {
        PerPartitionCircuitBreakerInfoHolder holder = new PerPartitionCircuitBreakerInfoHolder();

        assertThat(holder.snapshot()).isSameAs(PerPartitionCircuitBreakerInfoHolder.EMPTY);
        assertThatThrownBy(() -> PerPartitionCircuitBreakerInfoHolder.EMPTY
            .setPerPartitionCircuitBreakerInfoHolder(Collections.emptyMap()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test(groups = {"unit"})
    public void initializedEmptyStateIsSerialized() throws Exception {
        PerPartitionCircuitBreakerInfoHolder holder = new PerPartitionCircuitBreakerInfoHolder();
        holder.setPerPartitionCircuitBreakerInfoHolder(Collections.emptyMap());

        ObjectMapper objectMapper = new ObjectMapper();

        assertThat(objectMapper.writeValueAsString(holder))
            .isEqualTo("{\"stateByRegion\":{}}");
        assertThat(objectMapper.writeValueAsString(PerPartitionCircuitBreakerInfoHolder.EMPTY))
            .isEqualTo("null");
    }

    @Test(groups = {"unit"})
    public void stateIsSerializedUsingCompactFieldNames() throws Exception {
        LocationSpecificHealthContext healthContext = new LocationSpecificHealthContext.Builder()
            .withLocationHealthStatus(LocationHealthStatus.Unavailable)
            .withExceptionCountForReadForCircuitBreaking(1)
            .withExceptionCountForWriteForCircuitBreaking(2)
            .withSuccessCountForReadForRecovery(3)
            .withSuccessCountForWriteForRecovery(4)
            .withUnavailableSince(Instant.EPOCH)
            .build();
        PerPartitionCircuitBreakerInfoHolder holder = new PerPartitionCircuitBreakerInfoHolder();
        holder.setPerPartitionCircuitBreakerInfoHolder(Collections.singletonMap("eastus", healthContext));

        assertThat(new ObjectMapper().writeValueAsString(holder))
            .isEqualTo("{\"stateByRegion\":{\"eastus\":{\"st\":\"Unavailable\",\"rErr\":1,\"wErr\":2,"
                + "\"rOk\":3,\"wOk\":4,\"unavailableSince\":\"1970-01-01T00:00:00Z\"}}}");
    }

    @Test(groups = {"unit"})
    public void responseStatisticsRetainStateAtRecordTime() {
        DiagnosticsClientContext diagnosticsClientContext = Mockito.mock(DiagnosticsClientContext.class);
        PerPartitionCircuitBreakerInfoHolder holder = new PerPartitionCircuitBreakerInfoHolder();
        holder.setPerPartitionCircuitBreakerInfoHolder(Collections.singletonMap(
            "eastus",
            createHealthContext(LocationHealthStatus.Unavailable)));
        RxDocumentServiceRequest request = createRequest(diagnosticsClientContext, holder);

        ClientSideRequestStatistics statistics = new ClientSideRequestStatistics(diagnosticsClientContext);
        statistics.recordResponse(request, null, null);
        holder.setPerPartitionCircuitBreakerInfoHolder(Collections.singletonMap(
            "westus",
            createHealthContext(LocationHealthStatus.Healthy)));

        PerPartitionCircuitBreakerInfoHolder recordedHolder = statistics.getResponseStatisticsList()
            .iterator()
            .next()
            .getPerPartitionCircuitBreakerInfoHolder();
        assertThat(recordedHolder.getPerPartitionCircuitBreakerInfoHolder()).containsOnlyKeys("eastus");
    }

    @Test(groups = {"unit"})
    public void gatewayStatisticsRetainStateAtRecordTime() throws Exception {
        DiagnosticsClientContext diagnosticsClientContext = Mockito.mock(DiagnosticsClientContext.class);
        PerPartitionCircuitBreakerInfoHolder holder = new PerPartitionCircuitBreakerInfoHolder();
        holder.setPerPartitionCircuitBreakerInfoHolder(Collections.singletonMap(
            "eastus",
            createHealthContext(LocationHealthStatus.Unavailable)));
        RxDocumentServiceRequest request = createRequest(diagnosticsClientContext, holder);

        ClientSideRequestStatistics statistics = new ClientSideRequestStatistics(diagnosticsClientContext);
        statistics.recordGatewayResponse(request, Mockito.mock(StoreResponseDiagnostics.class), null);
        holder.setPerPartitionCircuitBreakerInfoHolder(Collections.singletonMap(
            "westus",
            createHealthContext(LocationHealthStatus.Healthy)));

        PerPartitionCircuitBreakerInfoHolder recordedHolder = statistics.getGatewayStatisticsList()
            .get(0)
            .getPerPartitionCircuitBreakerInfoHolder();
        assertThat(recordedHolder.getPerPartitionCircuitBreakerInfoHolder()).containsOnlyKeys("eastus");
        assertThat(new ObjectMapper().writeValueAsString(statistics))
            .contains("\"ppcb\":{\"stateByRegion\":{\"eastus\":");
    }

    @Test(groups = {"unit"})
    public void routingLookupInitializesEmptyStateWhenNoCircuitExists() throws Exception {
        DiagnosticsClientContext diagnosticsClientContext = Mockito.mock(DiagnosticsClientContext.class);
        PerPartitionCircuitBreakerInfoHolder holder = new PerPartitionCircuitBreakerInfoHolder();
        RxDocumentServiceRequest request = createRequest(diagnosticsClientContext, holder);
        request.setResourceId("collectionRid");
        PartitionKeyRange partitionKeyRange = new PartitionKeyRange("0", "AA", "BB");
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRange;
        request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = partitionKeyRange;

        RegionalRoutingContext eastUs = new RegionalRoutingContext(URI.create("https://eastus.documents.azure.com"));
        RegionalRoutingContext westUs = new RegionalRoutingContext(URI.create("https://westus.documents.azure.com"));
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        doReturn(false).when(globalEndpointManager).canUseMultipleWriteLocations(request);
        doReturn(UnmodifiableList.unmodifiableList(Arrays.asList(eastUs, westUs)))
            .when(globalEndpointManager)
            .getApplicableReadRegionalRoutingContexts(Collections.emptyList());

        GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker manager
            = new GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(globalEndpointManager);
        manager.resetCircuitBreakerConfig(PartitionLevelCircuitBreakerConfig.fromJsonString(
            "{\"isPartitionLevelCircuitBreakerEnabled\":true,"
                + "\"consecutiveExceptionCountToleratedForReads\":10,"
                + "\"consecutiveExceptionCountToleratedForWrites\":5}"));

        assertThat(manager.getUnavailableRegionsForPartitionKeyRange(request, "collectionRid", partitionKeyRange))
            .isEmpty();
        assertThat(holder.getPerPartitionCircuitBreakerInfoHolder()).isEmpty();

        ClientSideRequestStatistics statistics = new ClientSideRequestStatistics(diagnosticsClientContext);
        statistics.recordResponse(request, null, null);
        assertThat(new ObjectMapper().writeValueAsString(statistics))
            .contains("\"ppcb\":{\"stateByRegion\":{}}");
    }

    private static RxDocumentServiceRequest createRequest(
        DiagnosticsClientContext diagnosticsClientContext,
        PerPartitionCircuitBreakerInfoHolder holder) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            diagnosticsClientContext,
            OperationType.Read,
            ResourceType.Document);
        request.requestContext.setCrossRegionAvailabilityContext(
            new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                null,
                null,
                null,
                new AtomicBoolean(false),
                holder,
                new PerPartitionAutomaticFailoverInfoHolder()));
        return request;
    }

    private static LocationSpecificHealthContext createHealthContext(LocationHealthStatus healthStatus) {
        return new LocationSpecificHealthContext.Builder()
            .withLocationHealthStatus(healthStatus)
            .withUnavailableSince(Instant.EPOCH)
            .build();
    }
}