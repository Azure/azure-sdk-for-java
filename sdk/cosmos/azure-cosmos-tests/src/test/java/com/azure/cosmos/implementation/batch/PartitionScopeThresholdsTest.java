// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.implementation.CosmosBulkExecutionOptionsImpl;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PartitionScopeThresholdsTest {
    private static final Random rnd = new Random();

    @Test(groups = { "unit" })
    public void neverThrottledShouldResultInMaxBatchSize() {
        String pkRangeId = UUID.randomUUID().toString();
        int maxBatchSize = 100;
        PartitionScopeThresholds thresholds =
            new PartitionScopeThresholds(
                pkRangeId,
                new CosmosBulkExecutionOptionsImpl());

        assertThat(thresholds.getTargetMicroBatchSizeSnapshot())
            .isEqualTo(maxBatchSize);

        for (int i = 0; i < 1_000; i++) {
            thresholds.recordEnqueuedRetry();
        }

        assertThat(thresholds.getPartitionKeyRangeId()).isEqualTo(pkRangeId);
        assertThat(thresholds.getTargetMicroBatchSizeSnapshot()).isEqualTo(1);

        for (int i = 0; i < 100_000; i++) {
            thresholds.recordSuccessfulOperation();
        }

        assertThat(thresholds.getTargetMicroBatchSizeSnapshot()).isEqualTo(maxBatchSize);
    }

    @Test(groups = { "unit" })
    public void alwaysThrottledShouldResultInBatSizeOfOne() {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionScopeThresholds thresholds =
            new PartitionScopeThresholds(pkRangeId, new CosmosBulkExecutionOptionsImpl());

        assertThat(thresholds.getTargetMicroBatchSizeSnapshot())
            .isEqualTo(BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST);

        for (int i = 0; i < 1_000; i++) {
            thresholds.recordEnqueuedRetry();
        }

        assertThat(thresholds.getPartitionKeyRangeId()).isEqualTo(pkRangeId);
        assertThat(thresholds.getTargetMicroBatchSizeSnapshot()).isEqualTo(1);
    }

    @Test(groups = { "unit" })
    public void initialTargetMicroBatchSize() {
        String pkRangeId = UUID.randomUUID().toString();
        PartitionScopeThresholds thresholds =
            new PartitionScopeThresholds(pkRangeId, new CosmosBulkExecutionOptionsImpl());
        assertThat(thresholds.getTargetMicroBatchSizeSnapshot())
            .isEqualTo(BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST);

        // initial targetBatchSize should be capped by maxBatchSize
        int maxBatchSize = 5;
        CosmosBulkExecutionOptionsImpl bulkOperations = new CosmosBulkExecutionOptionsImpl();
        bulkOperations.setMaxMicroBatchSize(maxBatchSize);
        thresholds = new PartitionScopeThresholds(pkRangeId, bulkOperations);
        assertThat(thresholds.getTargetMicroBatchSizeSnapshot()).isEqualTo(maxBatchSize);
    }
}
