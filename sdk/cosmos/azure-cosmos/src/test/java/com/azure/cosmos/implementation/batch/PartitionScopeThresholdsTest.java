// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.batch;

import com.azure.cosmos.BulkProcessingOptions;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PartitionScopeThresholdsTest {
    private static final Random rnd = new Random();

    @Test(groups = { "unit" })
    public void neverThrottledShouldResultInMaxBatSize() {
        String pkRangeId = UUID.randomUUID().toString();
        int maxBatchSize = rnd.nextInt(1_000);
        maxBatchSize = 1_000;
        PartitionScopeThresholds<Object> thresholds =
            new PartitionScopeThresholds<>(pkRangeId, new BulkProcessingOptions<>().setMaxMicroBatchSize(maxBatchSize));

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
        PartitionScopeThresholds<Object> thresholds =
            new PartitionScopeThresholds<>(pkRangeId, new BulkProcessingOptions<>());

        assertThat(thresholds.getTargetMicroBatchSizeSnapshot())
            .isEqualTo(BatchRequestResponseConstants.MAX_OPERATIONS_IN_DIRECT_MODE_BATCH_REQUEST);

        for (int i = 0; i < 1_000; i++) {
            thresholds.recordEnqueuedRetry();
        }

        assertThat(thresholds.getPartitionKeyRangeId()).isEqualTo(pkRangeId);
        assertThat(thresholds.getTargetMicroBatchSizeSnapshot()).isEqualTo(1);
    }
}
