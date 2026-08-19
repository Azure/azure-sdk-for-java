// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.PartitionKeyRangeWrapper;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;

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
        this.manager = new GlobalPartitionEndpointManagerForPerPartitionCircuitBreaker(
            globalEndpointManager,
            this.logger);
    }

    @Test(groups = {"unit"})
    public void repeatedFailuresAreWarnedAndContainRecoveryIdentity() {
        RuntimeException failure = new RuntimeException("connection failed");

        for (int failureIndex = 0; failureIndex < 10; failureIndex++) {
            this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", failure);
        }

        String expectedFields = "PPCB failback failed: collectionResourceId=collectionRid, "
            + "partitionKeyRangeId=0, region=eastus, stage=OPEN_CONNECTION_TASK, "
            + "exceptionType=java.lang.RuntimeException, exceptionMessage=connection failed";
        verify(this.logger, times(10)).warn(contains(expectedFields), same(failure));
    }

    @Test(groups = {"unit"})
    public void changedFailureReasonIsWarned() {
        RuntimeException firstFailure = new RuntimeException("first");
        IllegalStateException changedFailure = new IllegalStateException("changed");

        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", firstFailure);
        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", changedFailure);

        verify(this.logger).warn(contains("exceptionMessage=first"), same(firstFailure));
        verify(this.logger).warn(
            contains("exceptionType=java.lang.IllegalStateException, exceptionMessage=changed"),
            same(changedFailure));
    }

    @Test(groups = {"unit"})
    public void differentStagesAreWarned() {
        RuntimeException failure = new RuntimeException("failure");

        this.manager.logFailbackFailure(PARTITION, REGION, "OPEN_CONNECTION_TASK", failure);
        this.manager.logFailbackFailure(PARTITION, REGION, "RECOVERY_PIPELINE", failure);

        verify(this.logger).warn(contains("stage=OPEN_CONNECTION_TASK"), same(failure));
        verify(this.logger).warn(contains("stage=RECOVERY_PIPELINE"), same(failure));
    }

    @Test(groups = {"unit"})
    public void streamFailureWithoutPartitionIdentityIsStillLogged() {
        RuntimeException failure = new RuntimeException("stream failed");

        this.manager.logFailbackFailure(null, null, "RECOVERY_STREAM", failure);

        verify(this.logger).warn(
            contains("collectionResourceId=, partitionKeyRangeId=, region=, stage=RECOVERY_STREAM"),
            same(failure));
    }

    @Test(groups = {"unit"})
    public void failuresForManyPartitionsAreWarned() {
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

        verify(this.logger, times(100)).warn(contains("exceptionMessage=failure"), same(failure));
    }

    @Test(groups = {"unit"})
    public void concurrentFailuresAreWarned() {
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

        verify(this.logger, times(100)).warn(contains("exceptionMessage=failure"), same(failure));
    }
}