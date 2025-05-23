// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.azure.spring.cloud.feature.management.filters.PercentageFilter;
import com.azure.spring.cloud.feature.management.filters.TargetingFilter;
import com.azure.spring.cloud.feature.management.filters.TimeWindowFilter;
import com.azure.spring.cloud.feature.management.targeting.TargetingContextAccessor;
import com.azure.spring.cloud.feature.management.targeting.TargetingEvaluationOptions;

public class FeatureManagementConfigurationTest {

    private static final ApplicationContextRunner CONTEXT_RUNNER = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FeatureManagementConfiguration.class));

    private static final ApplicationContextRunner CONTEXT_RUNNER_OPTIONS = new ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(FeatureManagementTestConfigurations.class, FeatureManagementConfiguration.class));

    @Test
    public void featureManagementTest() {
        CONTEXT_RUNNER.run(context -> {
            assertThat(context).hasSingleBean(FeatureManager.class);
            assertThat(context).doesNotHaveBean(TargetingEvaluationOptions.class);
            assertThat(context).hasSingleBean(TimeWindowFilter.class);
            assertThat(context).hasSingleBean(PercentageFilter.class);
            assertThat(context).doesNotHaveBean(TargetingFilter.class);
        });
    }

    @Test
    public void featureManagementWithEvaluationOptionsTest() {
        CONTEXT_RUNNER_OPTIONS.run(context -> {
            assertThat(context).hasSingleBean(TargetingEvaluationOptions.class);
            assertThat(context).hasSingleBean(FeatureManager.class);
            assertThat(context).hasSingleBean(TimeWindowFilter.class);
            assertThat(context).hasSingleBean(PercentageFilter.class);
            assertThat(context).doesNotHaveBean(TargetingFilter.class);
        });
    }

    @Test
    public void featureManagementWithAccessorTest() {
        CONTEXT_RUNNER.withUserConfiguration(TargetingContextAccessorTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(FeatureManager.class);
                assertThat(context).hasSingleBean(TimeWindowFilter.class);
                assertThat(context).hasSingleBean(PercentageFilter.class);
                assertThat(context).hasSingleBean(TargetingContextAccessor.class);
                assertThat(context).hasSingleBean(TargetingFilter.class);
            });
    }
}
