package com.azure.spring.cloud.feature.management;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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
        });
    }

    @Test
    public void featureManagementWithEvaluationOptionsTest() {
        CONTEXT_RUNNER_OPTIONS.run(context -> {
            assertThat(context).hasSingleBean(TargetingEvaluationOptions.class);
            assertThat(context).hasSingleBean(FeatureManager.class);
        });
    }

}
