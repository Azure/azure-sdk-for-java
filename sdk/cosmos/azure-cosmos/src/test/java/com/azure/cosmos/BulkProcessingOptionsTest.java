// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BulkProcessingOptionsTest {

    private static final Random rnd = new Random();

    @Test(groups = { "unit" })
    public void minAndMaxTargetRetryRateMustNotBeNegative() {
        assertThatThrownBy(
            () -> new BulkProcessingOptions<Object>().setTargetedMicroBatchRetryRate(-0.001, 0))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
            () -> new BulkProcessingOptions<Object>().setTargetedMicroBatchRetryRate(0.3, 0.29999))
            .isInstanceOf(IllegalArgumentException.class);;
    }

    @Test(groups = { "unit" })
    public void minAndMaxTargetRetryRate() {
        BulkProcessingOptions<Object> options = new BulkProcessingOptions<Object>();

        double rnd1 = rnd.nextDouble();
        double rnd2 = rnd.nextDouble();
        double randomMin = Math.min(rnd1, rnd2);
        double randomMax = Math.max(rnd1, rnd2);
        assertThat(randomMin)
            .isEqualTo(
                options.setTargetedMicroBatchRetryRate(randomMin, randomMax).getMinTargetedMicroBatchRetryRate());
        assertThat(randomMax)
                .isEqualTo(
                options.setTargetedMicroBatchRetryRate(randomMin, randomMax).getMaxTargetedMicroBatchRetryRate());
    }

    @Test(groups = { "unit" })
    public void thresholdsInstanceCanBePassedAcrossBulkProcessingOptionsInstances() {
        BulkProcessingOptions<Object> initialOptions = new BulkProcessingOptions<Object>();
        BulkProcessingThresholds<Object> thresholds = initialOptions.getThresholds();
        ConcurrentMap<String, PartitionScopeThresholds<Object>> partitionScopeThresholdsMap =
            ImplementationBridgeHelpers
                .BulkProcessingThresholdsHelper
                .getBulkProcessingThresholdsAccessor()
                .getPartitionScopeThresholds(thresholds);
        BulkProcessingOptions<Object> optionsWithThresholds =
            new BulkProcessingOptions<Object>(null, thresholds);

        assertThat(thresholds).isSameAs(optionsWithThresholds.getThresholds());
        assertThat(partitionScopeThresholdsMap)
            .isSameAs(
                ImplementationBridgeHelpers
                    .BulkProcessingThresholdsHelper
                    .getBulkProcessingThresholdsAccessor()
                    .getPartitionScopeThresholds(optionsWithThresholds.getThresholds()));
    }
}
