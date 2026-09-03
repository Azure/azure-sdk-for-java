// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.AvailabilityStrategyContext;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.CrossRegionAvailabilityContextForRxDocumentServiceRequest;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.PointOperationContextForCircuitBreaker;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.StoreResponseDiagnostics;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.PartitionLevelAutomaticFailoverInfo;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.PerPartitionAutomaticFailoverInfoHolder;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.PerPartitionCircuitBreakerInfoHolder;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class GlobalPartitionEndpointManagerForPPAFUnitTests extends TestSuiteBase {

    private static final String IS_PARTITION_LEVEL_CONFIG_ENABLED_SYS_PROPERTY_KEY = "COSMOS.IS_PER_PARTITION_AUTOMATIC_FAILOVER_ENABLED";

    private GlobalEndpointManager singleWriteAccountGlobalEndpointManagerMock;
    private GlobalEndpointManager multiWriteAccountGlobalEndpointManagerMock;

    private static final Logger logger = LoggerFactory.getLogger(GlobalPartitionEndpointManagerForPPAFUnitTests.class);

    private static final String EAST_US_CNST = "eastus";
    private static final String EAST_US_2_CNST = "eastus2";
    private static final String CENTRAL_US_CNST = "centralus";

    private static final URI EAST_US_URI_CNST = createUrl("https://contoso-east-us.documents.azure.com");
    private static final URI EAST_US_2_URI_CNST = createUrl("https://contoso-east-us-2.documents.azure.com");
    private static final URI CENTRAL_US_URI_CNST = createUrl("https://contoso-central-us.documents.azure.com");

    private final static Pair<URI, String> LocationEastUsEndpointToLocationPair = Pair.of(EAST_US_URI_CNST, EAST_US_CNST);
    private final static Pair<URI, String> LocationEastUs2EndpointToLocationPair = Pair.of(EAST_US_2_URI_CNST, EAST_US_2_CNST);
    private final static Pair<URI, String> LocationCentralUsEndpointToLocationPair = Pair.of(CENTRAL_US_URI_CNST, CENTRAL_US_CNST);

    @BeforeClass(groups = {"unit"})
    public void beforeClass() {
        this.singleWriteAccountGlobalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);
        this.multiWriteAccountGlobalEndpointManagerMock = Mockito.mock(GlobalEndpointManager.class);

        ConnectionPolicy connectionPolicy = new ConnectionPolicy(DirectConnectionConfig.getDefaultConfig());

        Mockito.when(this.singleWriteAccountGlobalEndpointManagerMock.getConnectionPolicy()).thenReturn(connectionPolicy);
        Mockito.when(this.multiWriteAccountGlobalEndpointManagerMock.getConnectionPolicy()).thenReturn(connectionPolicy);

        List<URI> availableReadEndpoints = Arrays.asList(EAST_US_URI_CNST, EAST_US_2_URI_CNST, CENTRAL_US_URI_CNST);
        List<RegionalRoutingContext> availableReadRegionalRoutingContexts = availableReadEndpoints
            .stream()
            .map(RegionalRoutingContext::new)
            .collect(Collectors.toList());

        Mockito.when(this.singleWriteAccountGlobalEndpointManagerMock.getAvailableReadRoutingContexts()).thenReturn(availableReadRegionalRoutingContexts);
        Mockito.when(this.singleWriteAccountGlobalEndpointManagerMock.getApplicableReadRegionalRoutingContexts(Mockito.anyList())).thenReturn(new UnmodifiableList<>(availableReadRegionalRoutingContexts));
        Mockito.when(this.singleWriteAccountGlobalEndpointManagerMock.canUseMultipleWriteLocations()).thenReturn(false);
        Mockito.when(this.singleWriteAccountGlobalEndpointManagerMock.canUseMultipleWriteLocations(Mockito.any())).thenReturn(false);

        Mockito.when(this.multiWriteAccountGlobalEndpointManagerMock.getAvailableReadRoutingContexts()).thenReturn(availableReadRegionalRoutingContexts);
        Mockito.when(this.multiWriteAccountGlobalEndpointManagerMock.getApplicableReadRegionalRoutingContexts(Mockito.anyList())).thenReturn(new UnmodifiableList<>(availableReadRegionalRoutingContexts));
        Mockito.when(this.multiWriteAccountGlobalEndpointManagerMock.canUseMultipleWriteLocations()).thenReturn(true);
        Mockito.when(this.multiWriteAccountGlobalEndpointManagerMock.canUseMultipleWriteLocations(Mockito.any())).thenReturn(true);
    }

    @DataProvider(name = "tryMarkEndpointAsUnavailableForPartitionKeyRangeTestArgs")
    public Object[][] tryMarkEndpointAsUnavailableForPartitionKeyRangeTestArgs() {
        return new Object[][]{
            {
                OperationType.Create,
                EAST_US_URI_CNST,
                EAST_US_2_URI_CNST,
                this.singleWriteAccountGlobalEndpointManagerMock,
                true
            },
            {
                OperationType.Read,
                EAST_US_URI_CNST,
                null,
                this.singleWriteAccountGlobalEndpointManagerMock,
                false
            },
            {
                OperationType.Create,
                EAST_US_URI_CNST,
                null,
                this.multiWriteAccountGlobalEndpointManagerMock,
                false
            },
            {
                OperationType.Read,
                EAST_US_URI_CNST,
                null,
                this.multiWriteAccountGlobalEndpointManagerMock,
                false
            }
        };
    }

    @Test(groups = {"unit"}, dataProvider = "tryMarkEndpointAsUnavailableForPartitionKeyRangeTestArgs", timeOut = TIMEOUT)
    public void tryMarkEndpointAsUnavailableForPartitionKeyRange(
        OperationType operationType,
        URI regionalEndpointWithFailure,
        URI regionEndpointToUsePostFailover,
        GlobalEndpointManager globalEndpointManager,
        boolean expectedCanOpOrchestrateFailover) throws NoSuchFieldException, IllegalAccessException {

        try {
            GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
                = new GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(globalEndpointManager, true);

            String pkRangeId = "0";
            String minInclusive = "AA";
            String maxExclusive = "BB";
            String collectionResourceId = "dbs/db1/colls/coll1";

            Field failedRegionalRoutingContextsField = PartitionLevelAutomaticFailoverInfo.class.getDeclaredField("failedRegionalRoutingContexts");

            assertThat(failedRegionalRoutingContextsField).isNotNull();

            Field currentRegionalContextField = PartitionLevelAutomaticFailoverInfo.class.getDeclaredField("current");

            assertThat(currentRegionalContextField).isNotNull();

            failedRegionalRoutingContextsField.setAccessible(true);
            currentRegionalContextField.setAccessible(true);

            Field partitionKeyRangeToFailoverInfoField
                = GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class.getDeclaredField("partitionKeyRangeToFailoverInfo");

            partitionKeyRangeToFailoverInfoField.setAccessible(true);

            ConcurrentHashMap<PartitionKeyRangeWrapper, ?> partitionKeyRangeToFailoverInfo
                = (ConcurrentHashMap<PartitionKeyRangeWrapper, ?>) partitionKeyRangeToFailoverInfoField.get(globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

            RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
                operationType,
                ResourceType.Document,
                collectionResourceId,
                pkRangeId,
                collectionResourceId,
                minInclusive,
                maxExclusive,
                regionalEndpointWithFailure);

            boolean canOpOrchestrateFailover
                = globalPartitionEndpointManagerForPerPartitionAutomaticFailover.tryMarkEndpointAsUnavailableForPartitionKeyRange(request, false);

            assertThat(canOpOrchestrateFailover).isEqualTo(expectedCanOpOrchestrateFailover);

            Object partitionLevelFailoverInfo
                = partitionKeyRangeToFailoverInfo.get(new PartitionKeyRangeWrapper(request.requestContext.resolvedPartitionKeyRange, collectionResourceId));

            if (canOpOrchestrateFailover) {
                Set<RegionalRoutingContext> failedRegionalRoutingContexts = (Set<RegionalRoutingContext>) failedRegionalRoutingContextsField.get(partitionLevelFailoverInfo);

                RegionalRoutingContext regionalRoutingContextWithFailure = new RegionalRoutingContext(regionalEndpointWithFailure);

                assertThat(failedRegionalRoutingContexts.contains(regionalRoutingContextWithFailure)).isTrue();
                RegionalRoutingContext current = (RegionalRoutingContext) currentRegionalContextField.get(partitionLevelFailoverInfo);

                RegionalRoutingContext regionalRoutingContextToUsePostFailover = new RegionalRoutingContext(regionEndpointToUsePostFailover);
                assertThat(current).isEqualTo(regionalRoutingContextToUsePostFailover);
            }

        } finally {
            System.clearProperty(IS_PARTITION_LEVEL_CONFIG_ENABLED_SYS_PROPERTY_KEY);
        }
    }

    @Test(groups = {"unit"})
    public void diagnosticsAreEmptyWithoutDesignatedOverride() throws Exception {
        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            OperationType.Create,
            ResourceType.Document,
            "dbs/db1/colls/coll1",
            "0",
            "dbs/db1/colls/coll1",
            "AA",
            "BB",
            EAST_US_URI_CNST);
        request.requestContext.regionalRoutingContextToRoute = null;

        ClientSideRequestStatistics directStatistics
            = new ClientSideRequestStatistics(mockDiagnosticsClientContext());
        directStatistics.recordResponse(request, null, this.singleWriteAccountGlobalEndpointManagerMock);

        ClientSideRequestStatistics gatewayStatistics
            = new ClientSideRequestStatistics(mockDiagnosticsClientContext());
        gatewayStatistics.recordGatewayResponse(
            request,
            Mockito.mock(StoreResponseDiagnostics.class),
            this.singleWriteAccountGlobalEndpointManagerMock);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode directJson = objectMapper.readTree(objectMapper.writeValueAsString(directStatistics));
        JsonNode gatewayJson = objectMapper.readTree(objectMapper.writeValueAsString(gatewayStatistics));

        Assertions.assertThat(directJson.at("/responseStatisticsList/0/ppaf").isObject()).isTrue();
        Assertions.assertThat(directJson.at("/responseStatisticsList/0/ppaf").isEmpty()).isTrue();
        Assertions.assertThat(gatewayJson.at("/gatewayStatisticsList/0/ppaf").isObject()).isTrue();
        Assertions.assertThat(gatewayJson.at("/gatewayStatisticsList/0/ppaf").isEmpty()).isTrue();
    }

    @Test(groups = {"unit"})
    public void designatedOverrideDiagnosticsContainRegionAndStableSince() throws Exception {
        String collectionResourceId = "dbs/db1/colls/coll1";
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover manager
            = new GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(
                this.singleWriteAccountGlobalEndpointManagerMock,
                true);
        RxDocumentServiceRequest failoverRequest = constructRxDocumentServiceRequestInstance(
            OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            "0",
            collectionResourceId,
            "AA",
            "BB",
            EAST_US_URI_CNST);

        Mockito.when(this.singleWriteAccountGlobalEndpointManagerMock.getRegionName(
            EAST_US_2_URI_CNST,
            OperationType.Read)).thenReturn(EAST_US_2_CNST);

        Instant beforeDesignation = Instant.now();
        Assertions.assertThat(manager.tryMarkEndpointAsUnavailableForPartitionKeyRange(failoverRequest, false))
            .isTrue();
        Instant afterDesignation = Instant.now();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode firstSnapshot = objectMapper.readTree(objectMapper.writeValueAsString(
            failoverRequest.requestContext.getPerPartitionFailoverContextHolder()));
        Instant designatedSince = Instant.parse(firstSnapshot.get("since").asText());

        Assertions.assertThat(firstSnapshot.get("currWriteRegion").asText()).isEqualTo(EAST_US_2_CNST);
        Assertions.assertThat(designatedSince).isBetween(beforeDesignation, afterDesignation);

        RxDocumentServiceRequest reuseRequest = constructRxDocumentServiceRequestInstance(
            OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            "0",
            collectionResourceId,
            "AA",
            "BB",
            EAST_US_URI_CNST);
        Assertions.assertThat(manager.tryAddPartitionLevelLocationOverride(reuseRequest)).isTrue();

        JsonNode reusedSnapshot = objectMapper.readTree(objectMapper.writeValueAsString(
            reuseRequest.requestContext.getPerPartitionFailoverContextHolder()));
        Assertions.assertThat(reusedSnapshot.get("currWriteRegion").asText()).isEqualTo(EAST_US_2_CNST);
        Assertions.assertThat(reusedSnapshot.get("since").asText()).isEqualTo(firstSnapshot.get("since").asText());
    }

    @Test(groups = {"unit"})
    public void responseStatisticsRetainDesignatedOverrideAtRecordTime() throws Exception {
        String collectionResourceId = "dbs/db1/colls/coll1";
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover manager
            = new GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(
                this.singleWriteAccountGlobalEndpointManagerMock,
                true);
        RxDocumentServiceRequest request = constructRxDocumentServiceRequestInstance(
            OperationType.Create,
            ResourceType.Document,
            collectionResourceId,
            "0",
            collectionResourceId,
            "AA",
            "BB",
            EAST_US_URI_CNST);

        Mockito.when(this.singleWriteAccountGlobalEndpointManagerMock.getRegionName(
            EAST_US_2_URI_CNST,
            OperationType.Read)).thenReturn(EAST_US_2_CNST);
        Assertions.assertThat(manager.tryMarkEndpointAsUnavailableForPartitionKeyRange(request, false)).isTrue();
        request.requestContext.regionalRoutingContextToRoute = null;

        ClientSideRequestStatistics directStatistics
            = new ClientSideRequestStatistics(mockDiagnosticsClientContext());
        directStatistics.recordResponse(request, null, this.singleWriteAccountGlobalEndpointManagerMock);
        request.requestContext.setPerPartitionAutomaticFailoverInfoHolder(null);

        Assertions.assertThat(manager.tryAddPartitionLevelLocationOverride(request)).isTrue();
        request.requestContext.regionalRoutingContextToRoute = null;
        ClientSideRequestStatistics gatewayStatistics
            = new ClientSideRequestStatistics(mockDiagnosticsClientContext());
        gatewayStatistics.recordGatewayResponse(
            request,
            Mockito.mock(StoreResponseDiagnostics.class),
            this.singleWriteAccountGlobalEndpointManagerMock);
        request.requestContext.setPerPartitionAutomaticFailoverInfoHolder(null);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode directJson = objectMapper.readTree(objectMapper.writeValueAsString(directStatistics));
        JsonNode gatewayJson = objectMapper.readTree(objectMapper.writeValueAsString(gatewayStatistics));

        assertPopulatedPpaf(directJson.at("/responseStatisticsList/0/ppaf"));
        assertPopulatedPpaf(gatewayJson.at("/gatewayStatisticsList/0/ppaf"));
        Assertions.assertThat(directJson.at("/responseStatisticsList/0")
            .has("perPartitionAutomaticFailoverInfoHolder")).isFalse();
        Assertions.assertThat(gatewayJson.at("/gatewayStatisticsList/0")
            .has("perPartitionAutomaticFailoverInfoHolder")).isFalse();
    }

    @Test(groups = {"unit"})
    public void allRegionUnavailableHandlingWithMultiThreading() {

        try {
            int threadPoolSizeForExecutors = 4;

            ScheduledThreadPoolExecutor executorForEastUs = new ScheduledThreadPoolExecutor(threadPoolSizeForExecutors);
            executorForEastUs.setRemoveOnCancelPolicy(true);
            executorForEastUs.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

            ScheduledThreadPoolExecutor executorForCentralUs = new ScheduledThreadPoolExecutor(threadPoolSizeForExecutors);
            executorForCentralUs.setRemoveOnCancelPolicy(true);
            executorForCentralUs.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

            ScheduledThreadPoolExecutor executorForEastUs2 = new ScheduledThreadPoolExecutor(threadPoolSizeForExecutors);
            executorForEastUs2.setRemoveOnCancelPolicy(true);
            executorForEastUs2.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

            List<ScheduledFuture<?>> scheduledFutures = new ArrayList<>();

            String pkRangeId = "0";
            String minInclusive = "AA";
            String maxExclusive = "BB";
            String collectionResourceId = "dbs/db1/colls/coll1";
            PartitionKeyRange partitionKeyRange = new PartitionKeyRange(pkRangeId, minInclusive, maxExclusive);

            List<URI> applicableReadWriteEndpoints = ImmutableList.of(
                    LocationEastUs2EndpointToLocationPair,
                    LocationEastUsEndpointToLocationPair,
                    LocationCentralUsEndpointToLocationPair)
                .stream()
                .map(Pair::getLeft)
                .collect(Collectors.toList());

            RxDocumentServiceRequest requestCentralUs = constructRxDocumentServiceRequestInstance(
                OperationType.Create,
                ResourceType.Document,
                collectionResourceId,
                pkRangeId,
                collectionResourceId,
                minInclusive,
                maxExclusive,
                LocationCentralUsEndpointToLocationPair.getKey());

            RxDocumentServiceRequest requestEastUs = constructRxDocumentServiceRequestInstance(
                OperationType.Create,
                ResourceType.Document,
                collectionResourceId,
                pkRangeId,
                collectionResourceId,
                minInclusive,
                maxExclusive,
                LocationEastUsEndpointToLocationPair.getKey());

            RxDocumentServiceRequest requestEastUs2 = constructRxDocumentServiceRequestInstance(
                OperationType.Create,
                ResourceType.Document,
                collectionResourceId,
                pkRangeId,
                collectionResourceId,
                minInclusive,
                maxExclusive,
                LocationEastUs2EndpointToLocationPair.getKey());

            GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover
                = new GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover(this.singleWriteAccountGlobalEndpointManagerMock, true);

            for (int i = 1; i <= 100; i++) {

                ScheduledFuture<?> scheduledFutureForEastUs = executorForEastUs.schedule(
                    () -> {
                        try {
                            validateAllRegionsAreNotUnavailableAfterExceptionInLocation(
                                globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
                                requestEastUs,
                                LocationEastUsEndpointToLocationPair.getKey(),
                                collectionResourceId,
                                partitionKeyRange,
                                applicableReadWriteEndpoints);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    1,
                    TimeUnit.MILLISECONDS);

                ScheduledFuture<?> scheduledFutureForCentralUs = executorForCentralUs.schedule(
                    () -> {
                        try {
                            validateAllRegionsAreNotUnavailableAfterExceptionInLocation(
                                globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
                                requestCentralUs,
                                LocationCentralUsEndpointToLocationPair.getKey(),
                                collectionResourceId,
                                partitionKeyRange,
                                applicableReadWriteEndpoints);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    1,
                    TimeUnit.MILLISECONDS);

                ScheduledFuture<?> scheduledFutureForEastUs2 = executorForEastUs2.schedule(
                    () -> {
                        try {
                            validateAllRegionsAreNotUnavailableAfterExceptionInLocation(
                                globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
                                requestEastUs2,
                                LocationEastUs2EndpointToLocationPair.getKey(),
                                collectionResourceId,
                                partitionKeyRange,
                                applicableReadWriteEndpoints);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    1,
                    TimeUnit.MILLISECONDS);

                scheduledFutures.add(scheduledFutureForEastUs);
                scheduledFutures.add(scheduledFutureForCentralUs);
                scheduledFutures.add(scheduledFutureForEastUs2);
            }

            while (true) {

                boolean areTasksStillRunning = false;

                for (ScheduledFuture<?> scheduledFuture : scheduledFutures) {
                    if (!scheduledFuture.isDone()) {
                        areTasksStillRunning = true;
                        break;
                    }
                }

                if (!areTasksStillRunning) {
                    break;
                }
            }

            executorForEastUs.shutdown();
            executorForCentralUs.shutdown();
            executorForEastUs2.shutdown();
        } finally {
            System.clearProperty(IS_PARTITION_LEVEL_CONFIG_ENABLED_SYS_PROPERTY_KEY);
        }
    }

    private static void validateAllRegionsAreNotUnavailableAfterExceptionInLocation(
        GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover globalPartitionEndpointManagerForPerPartitionAutomaticFailover,
        RxDocumentServiceRequest request,
        URI locationWithFailure,
        String collectionResourceId,
        PartitionKeyRange partitionKeyRange,
        List<URI> applicableReadWriteLocations) throws NoSuchFieldException, IllegalAccessException {

        logger.warn("Handling exception for {}", locationWithFailure.getPath());
        globalPartitionEndpointManagerForPerPartitionAutomaticFailover.tryMarkEndpointAsUnavailableForPartitionKeyRange(request, false);

        Field failedLocationsField = PartitionLevelAutomaticFailoverInfo.class.getDeclaredField("failedRegionalRoutingContexts");

        assertThat(failedLocationsField).isNotNull();

        Field currentField = PartitionLevelAutomaticFailoverInfo.class.getDeclaredField("current");

        assertThat(currentField).isNotNull();

        failedLocationsField.setAccessible(true);
        currentField.setAccessible(true);

        Field partitionKeyRangeToFailoverInfoField
            = GlobalPartitionEndpointManagerForPerPartitionAutomaticFailover.class.getDeclaredField("partitionKeyRangeToFailoverInfo");

        partitionKeyRangeToFailoverInfoField.setAccessible(true);

        ConcurrentHashMap<PartitionKeyRangeWrapper, ?> partitionKeyRangeToFailoverInfo
            = (ConcurrentHashMap<PartitionKeyRangeWrapper, ?>) partitionKeyRangeToFailoverInfoField.get(globalPartitionEndpointManagerForPerPartitionAutomaticFailover);

        Object partitionLevelFailoverInfo = partitionKeyRangeToFailoverInfo.get(new PartitionKeyRangeWrapper(partitionKeyRange, collectionResourceId));

        if (partitionLevelFailoverInfo != null) {
            Set<RegionalRoutingContext> failedRegionalRoutingContexts = (Set<RegionalRoutingContext>) failedLocationsField.get(partitionLevelFailoverInfo);

            logger.info("Assert that all regions are not Unavailable!");
            Assertions.assertThat(failedRegionalRoutingContexts.size()).isLessThan(applicableReadWriteLocations.size());
        }
    }

    private RxDocumentServiceRequest constructRxDocumentServiceRequestInstance(
        OperationType operationType,
        ResourceType resourceType,
        String collectionResourceId,
        String partitionKeyRangeId,
        String collectionLink,
        String minInclusive,
        String maxExclusive,
        URI locationEndpointToRoute) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            mockDiagnosticsClientContext(),
            operationType,
            resourceType);

        request.setResourceId(collectionResourceId);

        request.requestContext.resolvedPartitionKeyRange = new PartitionKeyRange(partitionKeyRangeId, minInclusive, maxExclusive);
        request.requestContext.resolvedPartitionKeyRangeForPerPartitionAutomaticFailover = request.requestContext.resolvedPartitionKeyRange;
        request.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(locationEndpointToRoute);
        request.requestContext.resolvedCollectionRid = collectionResourceId;
        request.requestContext.setExcludeRegions(Collections.emptyList());
        request.requestContext.setCrossRegionAvailabilityContext(
            new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                null,
                new PointOperationContextForCircuitBreaker(
                    new AtomicBoolean(false),
                    false,
                    collectionLink,
                    new SerializationDiagnosticsContext()),
                new AvailabilityStrategyContext(true, true),
                new AtomicBoolean(false),
                new PerPartitionCircuitBreakerInfoHolder(),
                new PerPartitionAutomaticFailoverInfoHolder())
);

        return request;
    }

    private static void assertPopulatedPpaf(JsonNode ppaf) {
        Assertions.assertThat(ppaf.isObject()).isTrue();
        Assertions.assertThat(ppaf.get("currWriteRegion").asText()).isEqualTo(EAST_US_2_CNST);
        Assertions.assertThat(Instant.parse(ppaf.get("since").asText())).isNotNull();
    }

    private static URI createUrl(String url) {
        try {
            return new URI(url);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
