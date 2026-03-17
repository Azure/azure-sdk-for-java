// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class FITests_queryAfterCreation
    extends FaultInjectionWithAvailabilityStrategyTestsBase {

    @Test(groups = {"fi-multi-master", "fi-thinclient-multi-master"}, dataProvider = "testConfigs_queryAfterCreation", retryAnalyzer = SuperFlakyTestRetryAnalyzer.class)
    public void queryAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Function<ItemOperationInvocationParameters, String> queryGenerator,
        BiFunction<String, ItemOperationInvocationParameters, CosmosResponseWrapper> queryExecution,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> responseValidator,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk,
        boolean shouldInjectPreferredRegionsInClient) {

        // Thin client forces GATEWAY mode — skip DIRECT-only baseline test configs whose
        // timeouts are too tight for the gateway proxy path. Availability strategy (hedging)
        // tests must still run as they validate core thin client functionality.
        if (Configs.isThinClientEnabled() && connectionMode == ConnectionMode.DIRECT && availabilityStrategy == null) {
            throw new SkipException(
                "Skipping DIRECT baseline test config '" + testCaseId + "' under thin client (GATEWAY mode forced)");
        }

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            null,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(FaultInjectionOperationType.QUERY_ITEM),
            (params) -> queryExecution.apply(queryGenerator.apply(params), params),
            faultInjectionCallback,
            validateStatusCode,
            expectedDiagnosticsContextCount,
            firstDiagnosticsContextValidations,
            otherDiagnosticsContextValidations,
            responseValidator,
            numberOfOtherDocumentsWithSameId,
            numberOfOtherDocumentsWithSamePk,
            false,
            connectionMode,
            shouldInjectPreferredRegionsInClient);
    }
}
