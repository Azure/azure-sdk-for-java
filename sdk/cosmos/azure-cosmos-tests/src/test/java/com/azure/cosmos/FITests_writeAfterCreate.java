// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class FITests_writeAfterCreate
    extends FaultInjectionWithAvailabilityStrategyTestsBase {

    @Test(groups = {"fi-multi-master"}, dataProvider = "testConfigs_writeAfterCreation", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void writeAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        Duration customMinRetryTimeInLocalRegion,
        Boolean nonIdempotentWriteRetriesEnabled,
        FaultInjectionOperationType faultInjectionOperationType,
        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> actionAfterInitialCreation,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext,
        boolean shouldInjectPreferredRegionsInClient) {

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            customMinRetryTimeInLocalRegion,
            nonIdempotentWriteRetriesEnabled,
            ArrayUtils.toArray(faultInjectionOperationType),
            actionAfterInitialCreation,
            faultInjectionCallback,
            validateStatusCode,
            1,
            ArrayUtils.toArray(validateDiagnosticsContext),
            null,
            null,
            0,
            0,
            false,
            connectionMode,
            shouldInjectPreferredRegionsInClient);
    }
}
