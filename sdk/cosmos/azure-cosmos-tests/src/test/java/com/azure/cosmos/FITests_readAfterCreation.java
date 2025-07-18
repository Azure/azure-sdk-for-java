// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class FITests_readAfterCreation
    extends FaultInjectionWithAvailabilityStrategyTestsBase {

    @Test(groups = {"fi-multi-master"}, dataProvider = "testConfigs_readAfterCreation", retryAnalyzer = FlakyTestRetryAnalyzer.class)
    public void readAfterCreation(
        String testCaseId,
        Duration endToEndTimeout,
        ThresholdBasedAvailabilityStrategy availabilityStrategy,
        CosmosRegionSwitchHint regionSwitchHint,
        ConnectionMode connectionMode,
        String readItemDocumentIdOverride,
        BiConsumer<CosmosAsyncContainer, FaultInjectionOperationType> faultInjectionCallback,
        BiConsumer<Integer, Integer> validateStatusCode,
        Consumer<CosmosDiagnosticsContext> validateDiagnosticsContext,
        boolean shouldInjectPreferredRegionsInClient) {

        Function<ItemOperationInvocationParameters, CosmosResponseWrapper> readItemCallback = (params) ->
            new CosmosResponseWrapper(params.container
                .readItem(
                    readItemDocumentIdOverride != null
                        ? readItemDocumentIdOverride
                        : params.idAndPkValuePair.getLeft(),
                    new PartitionKey(params.idAndPkValuePair.getRight()),
                    params.options,
                    ObjectNode.class)
                .block());

        execute(
            testCaseId,
            endToEndTimeout,
            availabilityStrategy,
            regionSwitchHint,
            null,
            notSpecifiedWhetherIdempotentWriteRetriesAreEnabled,
            ArrayUtils.toArray(FaultInjectionOperationType.READ_ITEM),
            readItemCallback,
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
