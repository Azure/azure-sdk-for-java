// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class FITests_readManyAfterCreation
    extends FaultInjectionWithAvailabilityStrategyTestsBase {

    @Test(groups = {"fi-multi-master", "fi-thinclient-multi-master"}, dataProvider = "testConfigs_readManyAfterCreation", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readManyAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        Function<ItemOperationInvocationParameters, List<Pair<String, String>>> readManyTuples,
        BiFunction<ItemOperationInvocationParameters, List<Pair<String, String>>, CosmosResponseWrapper> readManyOperation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        int expectedDiagnosticsContextCount,
        Consumer<CosmosDiagnosticsContext>[] firstDiagnosticsContextValidations,
        Consumer<CosmosDiagnosticsContext>[] otherDiagnosticsContextValidations,
        Consumer<CosmosResponseWrapper> responseValidator,
        int numberOfOtherDocumentsWithSameId,
        int numberOfOtherDocumentsWithSamePk,
        boolean shouldInjectPreferredRegionsInClient) {

        // readMany hardcodes DIRECT — skip baseline (no hedging) under thin client
        // which forces GATEWAY. Availability strategy tests must still run.
        if (Configs.isThinClientEnabled() && availabilityStrategy == null) {
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
            ArrayUtils.toArray(
                FaultInjectionOperationType.QUERY_ITEM,
                FaultInjectionOperationType.READ_ITEM
            ),
            (params) -> readManyOperation.apply(params, readManyTuples.apply(params)),
            faultInjectionCallback,
            validateStatusCode,
            expectedDiagnosticsContextCount,
            firstDiagnosticsContextValidations,
            otherDiagnosticsContextValidations,
            responseValidator,
            numberOfOtherDocumentsWithSameId,
            numberOfOtherDocumentsWithSamePk,
            false,
            ConnectionMode.DIRECT,
            shouldInjectPreferredRegionsInClient);
    }
}
