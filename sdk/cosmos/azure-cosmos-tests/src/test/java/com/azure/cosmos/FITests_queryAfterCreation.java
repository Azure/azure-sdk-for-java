// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class FITests_queryAfterCreation
    extends FaultInjectionWithAvailabilityStrategyTestsBase {

    @Test(groups = {"fi-multi-master"}, dataProvider = "testConfigs_queryAfterCreation", retryAnalyzer = FlakyTestRetryAnalyzer.class)
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
