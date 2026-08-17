// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.CrossRegionAvailabilityContextForRxDocumentServiceRequest;
import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.PerPartitionAutomaticFailoverInfoHolder;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.CosmosMetricName;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PpcbFailbackLoggingTest {

    private static final PartitionKeyRangeWrapper PARTITION = new PartitionKeyRangeWrapper(
        new PartitionKeyRange("0", "AA", "BB"),
        "collectionRid");
    private static final RegionalRoutingContext REGION = new RegionalRoutingContext(
        URI.create("https://contoso-east-us.documents.azure.com"));
    private static final RegionalRoutingContext SECOND_REGION = new RegionalRoutingContext(
        URI.create("https://contoso-west-us.documents.azure.com"));

    private GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker manager;
    private Logger logger;

    @BeforeMethod(groups = {"unit"})
    public void setup() {
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        doReturn("eastus").when(globalEndpointManager).getRegionName(
            REGION.getGatewayRegionalEndpoint(),
            OperationType.Read);
        doReturn("westus").when(globalEndpointManager).getRegionName(
            SECOND_REGION.getGatewayRegionalEndpoint(),
            OperationType.Read);
        doReturn(false).when(globalEndpointManager).canUseMultipleWriteLocations(Mockito.any());
        doReturn(UnmodifiableList.unmodifiableList(Arrays.asList(REGION, SECOND_REGION)))
            .when(globalEndpointManager)
            .getApplicableReadRegionalRoutingContexts(Mockito.anyList());
        this.logger = Mockito.mock(Logger.class);
        doReturn(true).when(this.logger).isWarnEnabled();
        doReturn(true).when(this.logger).isDebugEnabled();
        this.manager = new GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(
            globalEndpointManager,
            this.logger);
        this.manager.resetCircuitBreakerConfig(PartitionLevelCircuitBreakerConfig.fromJsonString(
            "{\"isPartitionLevelCircuitBreakerEnabled\":true,"
                + "\"consecutiveExceptionCountToleratedForReads\":1,"
                + "\"consecutiveExceptionCountToleratedForWrites\":1}"));
    }

    @Test(groups = {"unit"})
    public void repeatedFailuresAreSampledByPartitionRegionStageAndReason() {
        RuntimeException failure = new RuntimeException("failure");

        for (int occurrence = 0; occurrence < 10; occurrence++) {
            this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", failure);
        }

        verify(this.logger, times(2)).warn(
            contains("collectionResourceId: collectionRid, partitionKeyRangeId: 0, region: eastus, stage: OPEN_CONNECTION_TASK, reason: RuntimeException"),
            same(failure));
        verify(this.logger, times(8)).debug(
            contains("collectionResourceId: collectionRid, partitionKeyRangeId: 0, region: eastus, stage: OPEN_CONNECTION_TASK, reason: RuntimeException"),
            same(failure));
    }

    @Test(groups = {"unit"})
    public void changedFailureReasonUsesTheSameCounter() {
        RuntimeException firstFailure = new RuntimeException("first");
        IllegalStateException changedFailure = new IllegalStateException("changed");

        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", firstFailure);
        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", firstFailure);
        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", changedFailure);

        verify(this.logger).warn(contains("reason: RuntimeException"), same(firstFailure));
        verify(this.logger).debug(contains("reason: RuntimeException"), same(firstFailure));
        verify(this.logger).debug(contains("reason: IllegalStateException"), same(changedFailure));
    }

    @Test(groups = {"unit"})
    public void differentStagesUseTheSameCounter() {
        RuntimeException failure = new RuntimeException("failure");

        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", failure);
        this.manager.logFailbackFailure(PARTITION, REGION, "RECOVERY_PIPELINE", failure);

        verify(this.logger).warn(contains("stage: OPEN_CONNECTION_TASK"), same(failure));
        verify(this.logger).debug(contains("stage: RECOVERY_PIPELINE"), same(failure));
    }

    @Test(groups = {"unit"})
    public void streamFailureWithoutPartitionIdentityIsStillLogged() {
        RuntimeException failure = new RuntimeException("failure");

        this.manager.logFailbackFailure(null, null, "RECOVERY_STREAM", failure);

        verify(this.logger).warn(
            contains("collectionResourceId: , partitionKeyRangeId: , region: , stage: RECOVERY_STREAM, reason: RuntimeException"),
            same(failure));
    }

    @Test(groups = {"unit"})
    public void manyPartitionsUseConstantSamplingState() {
        RuntimeException failure = new RuntimeException("failure");

        for (int rangeId = 0; rangeId < 100; rangeId++) {
            this.manager.logFailbackFailure(
                new PartitionKeyRangeWrapper(
                    new PartitionKeyRange(String.valueOf(rangeId), "AA", "BB"),
                    "collectionRid"),
                REGION,
                "OPEN_CONNECTION_TASK",
                failure);
        }

        verify(this.logger, times(11)).warn(contains("reason: RuntimeException"), same(failure));
        verify(this.logger, times(89)).debug(contains("reason: RuntimeException"), same(failure));
    }

    @Test(groups = {"unit"})
    public void concurrentFailuresPreserveGlobalSamplingCadence() {
        RuntimeException failure = new RuntimeException("failure");

        Flux.range(0, 100)
            .parallel(4)
            .runOn(Schedulers.parallel())
            .doOnNext(ignored -> this.manager.logFailbackFailure(
                PARTITION,
                REGION,
                "OPEN_CONNECTION_TASK",
                failure))
            .sequential()
            .blockLast(Duration.ofSeconds(5));

        verify(this.logger, times(11)).warn(contains("reason: RuntimeException"), same(failure));
        verify(this.logger, times(89)).debug(contains("reason: RuntimeException"), same(failure));
    }

    @Test(groups = {"unit"})
    public void failbackBacklogProgressIsSampledAndCompletionIsLogged() {
        for (int scan = 0; scan < 10; scan++) {
            this.manager.logFailbackBacklog(7);
        }
        this.manager.logFailbackBacklog(0);
        this.manager.logFailbackBacklog(0);

        verify(this.logger, times(2)).info(contains(
            "PPCB failback backlog: unavailablePartitionRegionCount: 7"));
        verify(this.logger, times(8)).debug(contains(
            "PPCB failback backlog: unavailablePartitionRegionCount: 7"));
        verify(this.logger).info(contains(
            "PPCB failback backlog: unavailablePartitionRegionCount: 0"));
    }

    @Test(groups = {"unit"})
    public void failbackPendingRecoveryGaugeIsPerCollectionAndReportsZero() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Map<String, AtomicInteger> pendingRecoveryByCollection = new HashMap<>();
        pendingRecoveryByCollection.put("collectionA", new AtomicInteger(3));
        pendingRecoveryByCollection.put("collectionB", new AtomicInteger(2));

        try {
            assertThat(CosmosMetricName.fromString("cosmos.client.ppcb.failback.pendingRecoveryCount"))
                .isSameAs(CosmosMetricName.PPCB_FAILBACK_PENDING_RECOVERY_COUNT);
            this.manager.registerFailbackPendingRecoveryMeter(
                registry,
                Tag.of("ClientCorrelationId", "client1"));
            this.manager.recordFailbackPendingRecoveryByCollection(pendingRecoveryByCollection);

            assertThat(getPendingRecoveryGauge(registry, "collectionA").value()).isEqualTo(3);
            assertThat(getPendingRecoveryGauge(registry, "collectionB").value()).isEqualTo(2);

            Map<String, AtomicInteger> updatedPendingRecoveryByCollection = new HashMap<>();
            updatedPendingRecoveryByCollection.put("collectionA", new AtomicInteger());
            updatedPendingRecoveryByCollection.put("collectionB", new AtomicInteger(1));
            this.manager.recordFailbackPendingRecoveryByCollection(updatedPendingRecoveryByCollection);

            assertThat(getPendingRecoveryGauge(registry, "collectionA").value()).isZero();
            assertThat(getPendingRecoveryGauge(registry, "collectionB").value()).isEqualTo(1);

            this.manager.close();
            assertThat(registry.find(CosmosMetricName.PPCB_FAILBACK_PENDING_RECOVERY_COUNT.toString())
                .gauges()).isEmpty();
        } finally {
            this.manager.close();
            registry.close();
        }
    }

    @Test(groups = {"unit"})
    public void meterCleanupWinsRaceWithInFlightRowPublication() throws Exception {
        AtomicInteger gaugeRegistrationCount = new AtomicInteger();
        CountDownLatch secondGaugeRegistrationStarted = new CountDownLatch(1);
        CountDownLatch allowSecondGaugeRegistration = new CountDownLatch(1);
        SimpleMeterRegistry registry = new SimpleMeterRegistry() {
            @Override
            protected <T> Gauge newGauge(Meter.Id id, T object, ToDoubleFunction<T> valueFunction) {
                if (gaugeRegistrationCount.incrementAndGet() == 2) {
                    secondGaugeRegistrationStarted.countDown();
                    try {
                        if (!allowSecondGaugeRegistration.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Timed out waiting to continue gauge registration.");
                        }
                    } catch (InterruptedException error) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupted while coordinating gauge registration.", error);
                    }
                }

                return super.newGauge(id, object, valueFunction);
            }
        };
        Map<String, AtomicInteger> initialRecoveryCount = Collections.singletonMap(
            "collectionA",
            new AtomicInteger(1));
        Map<String, AtomicInteger> updatedRecoveryCount = Collections.singletonMap(
            "collectionA",
            new AtomicInteger(2));

        try {
            this.manager.registerFailbackPendingRecoveryMeter(
                registry,
                Tag.of("ClientCorrelationId", "client1"));
            this.manager.recordFailbackPendingRecoveryByCollection(initialRecoveryCount);

            AtomicReference<Throwable> publisherFailure = new AtomicReference<>();
            AtomicReference<Throwable> cleanupFailure = new AtomicReference<>();
            Thread publisher = new Thread(
                () -> {
                    try {
                        this.manager.recordFailbackPendingRecoveryByCollection(updatedRecoveryCount);
                    } catch (Throwable error) {
                        publisherFailure.set(error);
                    }
                },
                "ppcb-meter-publisher");
            CountDownLatch cleanupFinished = new CountDownLatch(1);
            Thread cleanup = new Thread(() -> {
                try {
                    this.manager.removeFailbackPendingRecoveryMeter();
                } catch (Throwable error) {
                    cleanupFailure.set(error);
                } finally {
                    cleanupFinished.countDown();
                }
            }, "ppcb-meter-cleanup");

            publisher.start();
            assertThat(secondGaugeRegistrationStarted.await(1, TimeUnit.SECONDS)).isTrue();
            cleanup.start();
            cleanupFinished.await(1, TimeUnit.SECONDS);
            allowSecondGaugeRegistration.countDown();
            publisher.join(1000);
            cleanup.join(1000);

            assertThat(publisher.isAlive()).isFalse();
            assertThat(cleanup.isAlive()).isFalse();
            assertThat(publisherFailure.get()).isNull();
            assertThat(cleanupFailure.get()).isNull();
            assertThat(registry.find(CosmosMetricName.PPCB_FAILBACK_PENDING_RECOVERY_COUNT.toString())
                .gauges()).isEmpty();
        } finally {
            allowSecondGaugeRegistration.countDown();
            this.manager.close();
            registry.close();
        }
    }

    @Test(groups = {"unit"})
    public void failbackLogsIncludeClientCorrelationId() {
        RuntimeException failure = new RuntimeException("failure");
        this.manager.setClientCorrelationId("client1");

        this.manager.logFailbackBacklog(7);
        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", failure);

        verify(this.logger).info(contains("clientCorrelationId: client1"));
        verify(this.logger).warn(contains("clientCorrelationId: client1"), same(failure));
    }

    @Test(groups = {"unit"})
    public void allRegionsUnavailableClearsExclusionsAndPublishesEmptyState() {
        RxDocumentServiceRequest request = createReadRequest(PARTITION);
        int failureCount = this.manager.getConsecutiveExceptionBasedCircuitBreaker()
            .getAllowedExceptionCountToMaintainStatus(LocationHealthStatus.HealthyWithFailures, true);

        for (int failure = 0; failure < failureCount; failure++) {
            this.manager.handleLocationExceptionForPartitionKeyRange(request, REGION, false);
        }
        for (int failure = 0; failure < failureCount; failure++) {
            this.manager.handleLocationExceptionForPartitionKeyRange(request, SECOND_REGION, false);
        }

        assertThat(getUnavailableRegions(request, PARTITION)).isEmpty();
        assertThat(request.requestContext
            .getPerPartitionCircuitBreakerInfoHolder()
            .getPerPartitionCircuitBreakerInfoHolder()).isEmpty();
    }

    private java.util.List<String> getUnavailableRegions(
        RxDocumentServiceRequest request,
        PartitionKeyRangeWrapper partitionKeyRangeWrapper) {

        return this.manager.getUnavailableRegionsForPartitionKeyRange(
            request,
            partitionKeyRangeWrapper.getCollectionResourceId(),
            partitionKeyRangeWrapper.getPartitionKeyRange());
    }

    private static RxDocumentServiceRequest createReadRequest(
        PartitionKeyRangeWrapper partitionKeyRangeWrapper) {

        RxDocumentServiceRequest request = RxDocumentServiceRequest.create(
            Mockito.mock(DiagnosticsClientContext.class),
            OperationType.Read,
            ResourceType.Document);
        request.setResourceId(partitionKeyRangeWrapper.getCollectionResourceId());
        request.requestContext.resolvedPartitionKeyRange = partitionKeyRangeWrapper.getPartitionKeyRange();
        request.requestContext.resolvedPartitionKeyRangeForCircuitBreaker = partitionKeyRangeWrapper.getPartitionKeyRange();
        request.requestContext.regionalRoutingContextToRoute = REGION;
        request.requestContext.setExcludeRegions(Collections.emptyList());
        request.requestContext.setCrossRegionAvailabilityContext(
            new CrossRegionAvailabilityContextForRxDocumentServiceRequest(
                null,
                null,
                null,
                new AtomicBoolean(false),
                new PerPartitionCircuitBreakerInfoHolder(),
                new PerPartitionAutomaticFailoverInfoHolder()));
        return request;
    }

    private static Gauge getPendingRecoveryGauge(SimpleMeterRegistry registry, String collectionRid) {
        return registry
            .get(CosmosMetricName.PPCB_FAILBACK_PENDING_RECOVERY_COUNT.toString())
            .tag("ClientCorrelationId", "client1")
            .tag("CollectionRid", collectionRid)
            .gauge();
    }
}