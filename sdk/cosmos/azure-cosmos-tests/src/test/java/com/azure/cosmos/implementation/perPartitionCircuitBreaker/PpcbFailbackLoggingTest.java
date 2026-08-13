// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import com.azure.cosmos.models.CosmosMetricName;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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

    private GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker manager;
    private Logger logger;

    @BeforeMethod(groups = {"unit"})
    public void setup() {
        GlobalEndpointManager globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        doReturn("eastus").when(globalEndpointManager).getRegionName(
            REGION.getGatewayRegionalEndpoint(),
            OperationType.Read);
        this.logger = Mockito.mock(Logger.class);
        doReturn(true).when(this.logger).isWarnEnabled();
        doReturn(true).when(this.logger).isDebugEnabled();
        this.manager = new GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(
            globalEndpointManager,
            this.logger);
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
    public void unexpectedStreamFailureIsLoggedAndRetried() {
        RuntimeException failure = new RuntimeException("failure");
        AtomicInteger subscriptions = new AtomicInteger();
        Flux<String> recoveryWork = Flux.defer(() -> subscriptions.incrementAndGet() == 1
            ? Flux.error(failure)
            : Flux.just("recovered"));

        Object result = this.manager.keepFailbackRecoveryAlive(recoveryWork)
            .blockFirst(Duration.ofSeconds(1));

        assertThat(result).isEqualTo("recovered");
        assertThat(subscriptions.get()).isEqualTo(2);
        verify(this.logger).warn(contains("stage: RECOVERY_STREAM"), same(failure));
    }

    @Test(groups = {"unit"})
    public void recoverySurvivesRepeatedFailuresOnOneThreadScheduler() {
        Scheduler scheduler = Schedulers.newBoundedElastic(1, 1, "ppcb-resilience-test");
        AtomicInteger subscriptions = new AtomicInteger();
        Set<String> threadNames = new HashSet<>();

        try {
            Flux<String> recoveryWork = Flux.defer(() -> {
                threadNames.add(Thread.currentThread().getName());
                return subscriptions.incrementAndGet() <= 3
                    ? Flux.error(new RuntimeException("failure"))
                    : Flux.just("recovered");
            }).subscribeOn(scheduler);

            Object result = this.manager.keepFailbackRecoveryAlive(recoveryWork)
                .blockFirst(Duration.ofSeconds(5));

            assertThat(result).isEqualTo("recovered");
            assertThat(subscriptions.get()).isEqualTo(4);
            assertThat(threadNames).hasSize(1);
            assertThat(threadNames.iterator().next()).startsWith("ppcb-resilience-test");
            verify(this.logger, times(1)).warn(contains("stage: RECOVERY_STREAM"), Mockito.any(RuntimeException.class));
            verify(this.logger, times(2)).debug(contains("stage: RECOVERY_STREAM"), Mockito.any(RuntimeException.class));
        } finally {
            scheduler.dispose();
        }
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
    public void failbackRemainingGaugeIsPerCollectionAndReportsZero() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        Map<String, AtomicInteger> remainingByCollection = new HashMap<>();
        remainingByCollection.put("collectionA", new AtomicInteger(3));
        remainingByCollection.put("collectionB", new AtomicInteger(2));

        try {
            assertThat(CosmosMetricName.fromString("cosmos.client.ppcb.failback.pendingPartitionCount"))
                .isSameAs(CosmosMetricName.PPCB_FAILBACK_PENDING_PARTITION_COUNT);
            this.manager.registerFailbackRemainingMeter(
                registry,
                Tag.of("ClientCorrelationId", "client1"));
            this.manager.recordFailbackRemainingByCollection(remainingByCollection);

            assertThat(getFailbackRemainingGauge(registry, "collectionA").value()).isEqualTo(3);
            assertThat(getFailbackRemainingGauge(registry, "collectionB").value()).isEqualTo(2);

            Map<String, AtomicInteger> updatedRemainingByCollection = new HashMap<>();
            updatedRemainingByCollection.put("collectionA", new AtomicInteger());
            updatedRemainingByCollection.put("collectionB", new AtomicInteger(1));
            this.manager.recordFailbackRemainingByCollection(updatedRemainingByCollection);

            assertThat(getFailbackRemainingGauge(registry, "collectionA").value()).isZero();
            assertThat(getFailbackRemainingGauge(registry, "collectionB").value()).isEqualTo(1);

            this.manager.close();
            assertThat(registry.find(CosmosMetricName.PPCB_FAILBACK_PENDING_PARTITION_COUNT.toString())
                .gauges()).isEmpty();
        } finally {
            this.manager.close();
            registry.close();
        }
    }

    private static Gauge getFailbackRemainingGauge(SimpleMeterRegistry registry, String collectionRid) {
        return registry
            .get(CosmosMetricName.PPCB_FAILBACK_PENDING_PARTITION_COUNT.toString())
            .tag("ClientCorrelationId", "client1")
            .tag("CollectionRid", collectionRid)
            .gauge();
    }
}