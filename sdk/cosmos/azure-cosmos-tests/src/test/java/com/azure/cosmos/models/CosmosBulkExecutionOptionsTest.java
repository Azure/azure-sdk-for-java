// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.batch.PartitionScopeThresholds;
import org.testng.annotations.Test;

import java.util.Random;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CosmosBulkExecutionOptionsTest {

    private static final Random rnd = new Random();

    @Test(groups = { "unit" })
    public void minAndMaxTargetRetryRateMustNotBeNegative() {
        assertThatThrownBy(
            () -> new CosmosBulkExecutionOptions().setTargetedMicroBatchRetryRate(-0.001, 0))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(
            () -> new CosmosBulkExecutionOptions().setTargetedMicroBatchRetryRate(0.3, 0.29999))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test(groups = { "unit" })
    public void minAndMaxTargetRetryRate() {
        CosmosBulkExecutionOptions options = new CosmosBulkExecutionOptions();

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
    public void thresholdsInstanceCanBePassedAcrossBulkExecutionOptionsInstances() {
        CosmosBulkExecutionOptions initialOptions = new CosmosBulkExecutionOptions();
        CosmosBulkExecutionThresholdsState thresholds = initialOptions.getThresholdsState();
        ConcurrentMap<String, PartitionScopeThresholds> partitionScopeThresholdsMap =
            ImplementationBridgeHelpers.CosmosBulkExecutionThresholdsStateHelper
                .getBulkExecutionThresholdsAccessor()
                .getPartitionScopeThresholds(thresholds);
        CosmosBulkExecutionOptions optionsWithThresholds =
            new CosmosBulkExecutionOptions(null, thresholds, null);

        assertThat(thresholds).isSameAs(optionsWithThresholds.getThresholdsState());
        assertThat(partitionScopeThresholdsMap)
            .isSameAs(
                ImplementationBridgeHelpers.CosmosBulkExecutionThresholdsStateHelper
                    .getBulkExecutionThresholdsAccessor()
                    .getPartitionScopeThresholds(optionsWithThresholds.getThresholdsState()));
    }

    @Test(groups = { "unit" })
    public void throughputControlGroup() {
        CosmosBulkExecutionOptions options = new CosmosBulkExecutionOptions();
        assertThat(options.getThroughputControlGroupName()).isNull();

        CosmosBulkExecutionOptions optionsReturned = options.setThroughputControlGroupName("HelloWorld");
        assertThat(optionsReturned).isSameAs(options);
        assertThat(options.getThroughputControlGroupName()).isNotNull();
        assertThat(options.getThroughputControlGroupName()).isEqualTo("HelloWorld");

        options.setThroughputControlGroupName(null);
        assertThat(options.getThroughputControlGroupName()).isNull();
    }
}
