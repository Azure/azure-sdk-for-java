// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.circuitBreaker.GlobalPartitionEndpointManagerForCircuitBreaker;
import com.azure.cosmos.implementation.circuitBreaker.LocationHealthStatus;
import com.azure.cosmos.implementation.circuitBreaker.LocationSpecificContext;
import com.azure.cosmos.implementation.circuitBreaker.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class GlobalPartitionEndpointManagerForCircuitBreakerTests {

    private final static Pair<URI, String> LocationEastUsEndpointToLocationPair = Pair.of(createUrl("https://contoso-east-us.documents.azure.com"), "eastus");
    private final static Pair<URI, String> LocationEastUs2EndpointToLocationPair = Pair.of(createUrl("https://contoso-east-us-2.documents.azure.com"), "eastus2");
    private final static Pair<URI, String> LocationCentralUsEndpointToLocationPair = Pair.of(createUrl("https://contoso-central-us.documents.azure.com"), "centralus");

    private static final boolean READ_OPERATION_TRUE = true;

    private GlobalEndpointManager globalEndpointManagerMock;

    @BeforeClass(groups = {"unit"})
    public void beforeClass() {
        this.globalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

        Mockito
            .when(this.globalEndpointManagerMock.getRegionName(LocationEastUsEndpointToLocationPair.getKey(), OperationType.Read))
            .thenReturn(LocationEastUsEndpointToLocationPair.getRight());
        Mockito
            .when(this.globalEndpointManagerMock.getRegionName(LocationEastUsEndpointToLocationPair.getKey(), OperationType.Create))
            .thenReturn(LocationEastUsEndpointToLocationPair.getRight());
        Mockito
            .when(this.globalEndpointManagerMock.getRegionName(LocationCentralUsEndpointToLocationPair.getKey(), OperationType.Read))
            .thenReturn(LocationCentralUsEndpointToLocationPair.getRight());
        Mockito
            .when(this.globalEndpointManagerMock.getRegionName(LocationCentralUsEndpointToLocationPair.getKey(), OperationType.Create))
            .thenReturn(LocationCentralUsEndpointToLocationPair.getRight());
        Mockito
            .when(this.globalEndpointManagerMock.getRegionName(LocationEastUs2EndpointToLocationPair.getKey(), OperationType.Read))
            .thenReturn(LocationEastUs2EndpointToLocationPair.getRight());
        Mockito
            .when(this.globalEndpointManagerMock.getRegionName(LocationEastUs2EndpointToLocationPair.getKey(), OperationType.Create))
            .thenReturn(LocationEastUs2EndpointToLocationPair.getRight());
    }

    @DataProvider(name = "partitionLevelCircuitBreakerConfigs")
    public Object[][] partitionLevelCircuitBreakerConfigs() {
        return new Object[][] {
            new Object[] {
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"COUNT_BASED\","
                    + "\"circuitBreakerFailureTolerance\": \"LOW\"}",
                READ_OPERATION_TRUE
            },
            new Object[] {
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"COUNT_BASED\","
                    + "\"circuitBreakerFailureTolerance\": \"MEDIUM\"}",
                READ_OPERATION_TRUE
            },
            new Object[] {
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"COUNT_BASED\","
                    + "\"circuitBreakerFailureTolerance\": \"HIGH\"}",
                READ_OPERATION_TRUE
            },
            new Object[] {
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"COUNT_BASED\","
                    + "\"circuitBreakerFailureTolerance\": \"LOW\"}",
                !READ_OPERATION_TRUE
            },
            new Object[] {
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"COUNT_BASED\","
                    + "\"circuitBreakerFailureTolerance\": \"MEDIUM\"}",
                !READ_OPERATION_TRUE
            },
            new Object[] {
                "{\"isPartitionLevelCircuitBreakerEnabled\": true, "
                    + "\"circuitBreakerType\": \"COUNT_BASED\","
                    + "\"circuitBreakerFailureTolerance\": \"HIGH\"}",
                !READ_OPERATION_TRUE
            }
        };
    }

    @Test(groups = {"unit"}, dataProvider = "partitionLevelCircuitBreakerConfigs")
    public void recordHealthyStatus(String partitionLevelCircuitBreakerConfigAsJsonString, boolean readOperationTrue) {

        System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", partitionLevelCircuitBreakerConfigAsJsonString);

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = new GlobalPartitionEndpointManagerForCircuitBreaker(this.globalEndpointManagerMock);

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            readOperationTrue ? OperationType.Read : OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        globalPartitionEndpointManagerForCircuitBreaker
            .handleLocationSuccessForPartitionKeyRange(request);

        Map<URI, LocationSpecificContext> locationToLocationSpecificContextMappings
            = globalPartitionEndpointManagerForCircuitBreaker.getLocationToLocationSpecificContextMappings(new PartitionKeyRangeWrapper(
                new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive), collectionResourceId));

        LocationSpecificContext locationSpecificContext
            = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isTrue();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isFalse();

        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    @Test(groups = {"unit"}, dataProvider = "partitionLevelCircuitBreakerConfigs")
    public void recordHealthyToHealthyWithFailuresStatusTransition(String partitionLevelCircuitBreakerConfigAsJsonString, boolean readOperationTrue) {

        System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", partitionLevelCircuitBreakerConfigAsJsonString);

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = new GlobalPartitionEndpointManagerForCircuitBreaker(this.globalEndpointManagerMock);

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        List<URI> applicableReadWriteEndpoints = ImmutableList.of(
            LocationEastUs2EndpointToLocationPair,
            LocationEastUsEndpointToLocationPair,
            LocationCentralUsEndpointToLocationPair)
            .stream()
            .map(uriToLocationMappings -> uriToLocationMappings.getLeft())
            .collect(Collectors.toList());

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            readOperationTrue ? OperationType.Read : OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        Mockito.when(this.globalEndpointManagerMock.getReadEndpoints()).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));
        Mockito.when(this.globalEndpointManagerMock.getWriteEndpoints()).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));

        globalPartitionEndpointManagerForCircuitBreaker
            .handleLocationExceptionForPartitionKeyRange(request, LocationEastUs2EndpointToLocationPair.getKey());

        Map<URI, LocationSpecificContext> locationToLocationSpecificContextMappings
            = globalPartitionEndpointManagerForCircuitBreaker.getLocationToLocationSpecificContextMappings(new PartitionKeyRangeWrapper(
            new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive), collectionResourceId));

        LocationSpecificContext locationSpecificContext
            = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isTrue();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isFalse();

        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    @Test(groups = {"unit"}, dataProvider = "partitionLevelCircuitBreakerConfigs")
    public void recordHealthyWithFailuresToUnavailableStatusTransition(String partitionLevelCircuitBreakerConfigAsJsonString, boolean readOperationTrue) {

        System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", partitionLevelCircuitBreakerConfigAsJsonString);

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = new GlobalPartitionEndpointManagerForCircuitBreaker(this.globalEndpointManagerMock);

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        List<URI> applicableReadWriteEndpoints = ImmutableList.of(
                LocationEastUs2EndpointToLocationPair,
                LocationEastUsEndpointToLocationPair,
                LocationCentralUsEndpointToLocationPair)
            .stream()
            .map(uriToLocationMappings -> uriToLocationMappings.getLeft())
            .collect(Collectors.toList());

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            readOperationTrue ? OperationType.Read : OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        Mockito.when(this.globalEndpointManagerMock.getApplicableWriteEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));
        Mockito.when(this.globalEndpointManagerMock.getApplicableReadEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));

        int exceptionCountToHandle
            = globalPartitionEndpointManagerForCircuitBreaker.getConsecutiveExceptionBasedCircuitBreaker().getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, readOperationTrue);

        for (int i = 1; i <= exceptionCountToHandle + 1; i++) {
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationEastUs2EndpointToLocationPair.getKey());
        }

        Map<URI, LocationSpecificContext> locationToLocationSpecificContextMappings
            = globalPartitionEndpointManagerForCircuitBreaker.getLocationToLocationSpecificContextMappings(new PartitionKeyRangeWrapper(
            new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive), collectionResourceId));

        LocationSpecificContext locationSpecificContext
            = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isFalse();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isTrue();

        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    @Test(groups = {"unit"}, dataProvider = "partitionLevelCircuitBreakerConfigs")
    public void recordUnavailableToHealthyTentativeStatusTransition(String partitionLevelCircuitBreakerConfigAsJsonString, boolean readOperationTrue) {

        System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", partitionLevelCircuitBreakerConfigAsJsonString);

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = new GlobalPartitionEndpointManagerForCircuitBreaker(this.globalEndpointManagerMock);

        globalPartitionEndpointManagerForCircuitBreaker.init();

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        List<URI> applicableReadWriteEndpoints = ImmutableList.of(
                LocationEastUs2EndpointToLocationPair,
                LocationEastUsEndpointToLocationPair,
                LocationCentralUsEndpointToLocationPair)
            .stream()
            .map(uriToLocationMappings -> uriToLocationMappings.getLeft())
            .collect(Collectors.toList());

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            readOperationTrue ? OperationType.Read : OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        Mockito.when(this.globalEndpointManagerMock.getApplicableWriteEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));
        Mockito.when(this.globalEndpointManagerMock.getApplicableReadEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));

        int exceptionCountToHandle
            = globalPartitionEndpointManagerForCircuitBreaker.getConsecutiveExceptionBasedCircuitBreaker().getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, readOperationTrue);

        for (int i = 1; i <= exceptionCountToHandle + 1; i++) {
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationEastUs2EndpointToLocationPair.getKey());
        }

        Map<URI, LocationSpecificContext> locationToLocationSpecificContextMappings
            = globalPartitionEndpointManagerForCircuitBreaker.getLocationToLocationSpecificContextMappings(new PartitionKeyRangeWrapper(
            new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive), collectionResourceId));

        LocationSpecificContext locationSpecificContext
            = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isFalse();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isTrue();

        try {
            Thread.sleep(65_000);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        locationSpecificContext = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isTrue();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isFalse();

        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    @Test(groups = {"unit"}, dataProvider = "partitionLevelCircuitBreakerConfigs")
    public void recordHealthyTentativeToHealthyStatusTransition(String partitionLevelCircuitBreakerConfigAsJsonString, boolean readOperationTrue) {

        System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", partitionLevelCircuitBreakerConfigAsJsonString);

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = new GlobalPartitionEndpointManagerForCircuitBreaker(this.globalEndpointManagerMock);

        globalPartitionEndpointManagerForCircuitBreaker.init();

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        List<URI> applicableReadWriteEndpoints = ImmutableList.of(
                LocationEastUs2EndpointToLocationPair,
                LocationEastUsEndpointToLocationPair,
                LocationCentralUsEndpointToLocationPair)
            .stream()
            .map(uriToLocationMappings -> uriToLocationMappings.getLeft())
            .collect(Collectors.toList());

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            readOperationTrue ? OperationType.Read : OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        Mockito.when(this.globalEndpointManagerMock.getApplicableWriteEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));
        Mockito.when(this.globalEndpointManagerMock.getApplicableReadEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));

        int exceptionCountToHandle
            = globalPartitionEndpointManagerForCircuitBreaker.getConsecutiveExceptionBasedCircuitBreaker().getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, readOperationTrue);

        for (int i = 1; i <= exceptionCountToHandle + 1; i++) {
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationEastUs2EndpointToLocationPair.getKey());
        }

        Map<URI, LocationSpecificContext> locationToLocationSpecificContextMappings
            = globalPartitionEndpointManagerForCircuitBreaker.getLocationToLocationSpecificContextMappings(new PartitionKeyRangeWrapper(
            new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive), collectionResourceId));

        LocationSpecificContext locationSpecificContext
            = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isFalse();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isTrue();

        try {
            Thread.sleep(65_000);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        locationSpecificContext = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        int successCountToUpgradeStatus = globalPartitionEndpointManagerForCircuitBreaker.getConsecutiveExceptionBasedCircuitBreaker().getMinimumSuccessCountForStatusUpgrade(LocationHealthStatus.HealthyTentative, readOperationTrue);

        for (int i = 1; i <= successCountToUpgradeStatus + 1; i++) {
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationSuccessForPartitionKeyRange(request);
        }

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isTrue();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isFalse();

        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    @Test(groups = {"unit"}, dataProvider = "partitionLevelCircuitBreakerConfigs")
    public void recordHealthyTentativeToUnavailableTransition(String partitionLevelCircuitBreakerConfigAsJsonString, boolean readOperationTrue) {

        System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", partitionLevelCircuitBreakerConfigAsJsonString);

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = new GlobalPartitionEndpointManagerForCircuitBreaker(this.globalEndpointManagerMock);

        globalPartitionEndpointManagerForCircuitBreaker.init();

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        List<URI> applicableReadWriteEndpoints = ImmutableList.of(
                LocationEastUs2EndpointToLocationPair,
                LocationEastUsEndpointToLocationPair,
                LocationCentralUsEndpointToLocationPair)
            .stream()
            .map(uriToLocationMappings -> uriToLocationMappings.getLeft())
            .collect(Collectors.toList());

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            readOperationTrue ? OperationType.Read : OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        Mockito.when(this.globalEndpointManagerMock.getApplicableWriteEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));
        Mockito.when(this.globalEndpointManagerMock.getApplicableReadEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));

        int exceptionCountToHandle
            = globalPartitionEndpointManagerForCircuitBreaker.getConsecutiveExceptionBasedCircuitBreaker().getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, readOperationTrue);

        for (int i = 1; i <= exceptionCountToHandle + 1; i++) {
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationEastUs2EndpointToLocationPair.getKey());
        }

        Map<URI, LocationSpecificContext> locationToLocationSpecificContextMappings
            = globalPartitionEndpointManagerForCircuitBreaker.getLocationToLocationSpecificContextMappings(new PartitionKeyRangeWrapper(
            new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive), collectionResourceId));

        LocationSpecificContext locationSpecificContext
            = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isFalse();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isTrue();

        try {
            Thread.sleep(65_000);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        exceptionCountToHandle = globalPartitionEndpointManagerForCircuitBreaker.getConsecutiveExceptionBasedCircuitBreaker().getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyTentative, readOperationTrue);

        for (int i = 1; i <= exceptionCountToHandle + 1; i++) {
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationEastUs2EndpointToLocationPair.getKey());
        }

        locationSpecificContext = locationToLocationSpecificContextMappings.get(LocationEastUs2EndpointToLocationPair.getKey());

        assertThat(locationSpecificContext.isRegionAvailableToProcessRequests()).isFalse();
        assertThat(locationSpecificContext.isExceptionThresholdBreached()).isTrue();

        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    @Test(groups = {"unit"}, dataProvider = "partitionLevelCircuitBreakerConfigs")
    public void allRegionsUnhealthyHandling(String partitionLevelCircuitBreakerConfigAsJsonString, boolean readOperationTrue) {
        System.setProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG", partitionLevelCircuitBreakerConfigAsJsonString);

        GlobalPartitionEndpointManagerForCircuitBreaker globalPartitionEndpointManagerForCircuitBreaker
            = new GlobalPartitionEndpointManagerForCircuitBreaker(this.globalEndpointManagerMock);

        globalPartitionEndpointManagerForCircuitBreaker.init();

        String pkRangeId = "0";
        String minInclusive = "AA";
        String maxExclusive = "BB";
        String collectionResourceId = "dbs/db1/colls/coll1";

        List<URI> applicableReadWriteEndpoints = ImmutableList.of(
                LocationEastUs2EndpointToLocationPair,
                LocationEastUsEndpointToLocationPair,
                LocationCentralUsEndpointToLocationPair)
            .stream()
            .map(uriToLocationMappings -> uriToLocationMappings.getLeft())
            .collect(Collectors.toList());

        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            readOperationTrue ? OperationType.Read : OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            pkRangeId,
            minInclusive,
            maxExclusive,
            LocationEastUs2EndpointToLocationPair.getKey());

        Mockito.when(this.globalEndpointManagerMock.getApplicableWriteEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));
        Mockito.when(this.globalEndpointManagerMock.getApplicableReadEndpoints(Mockito.anyList())).thenReturn((UnmodifiableList<URI>) UnmodifiableList.unmodifiableList(applicableReadWriteEndpoints));

        int exceptionCountToHandle
            = globalPartitionEndpointManagerForCircuitBreaker
            .getConsecutiveExceptionBasedCircuitBreaker()
            .getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, readOperationTrue);

        for (int i = 1; i <= exceptionCountToHandle + 1; i++) {
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationEastUs2EndpointToLocationPair.getKey());
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationEastUsEndpointToLocationPair.getKey());
            globalPartitionEndpointManagerForCircuitBreaker
                .handleLocationExceptionForPartitionKeyRange(request, LocationCentralUsEndpointToLocationPair.getKey());
        }

        Map<URI, LocationSpecificContext> locationToLocationSpecificContextMappings
            = globalPartitionEndpointManagerForCircuitBreaker.getLocationToLocationSpecificContextMappings(new PartitionKeyRangeWrapper(
            new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive), collectionResourceId));

        assertThat(locationToLocationSpecificContextMappings).isNull();

        System.clearProperty("COSMOS.PARTITION_LEVEL_CIRCUIT_BREAKER_CONFIG");
    }

    private RxDocumentServiceRequest constructRxDocumentServiceRequestInstance(
        OperationType operationType,
        ResourceType resourceType,
        String collectionResourceId,
        String partitionKeyRangeId,
        String minInclusive,
        String maxExclusive,
        URI locationEndpointToRoute) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            operationType,
            resourceType);

        request.setResourceId(collectionResourceId);

        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(partitionKeyRangeId, minInclusive, maxExclusive);
        request.requestContext.locationEndpointToRoute = locationEndpointToRoute;
        request.requestContext.setExcludeRegions(Collections.emptyList());

        return request;
    }

    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
