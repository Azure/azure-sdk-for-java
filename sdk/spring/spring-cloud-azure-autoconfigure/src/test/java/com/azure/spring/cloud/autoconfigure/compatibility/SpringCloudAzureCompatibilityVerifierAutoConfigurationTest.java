// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringCloudAzureCompatibilityVerifierAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SpringCloudAzureCompatibilityVerifierAutoConfiguration.class));

    @Test
    void testCompatibilityVerifierPropertiesDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.compatibility-verifier.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(SpringCloudAzureSpringBootVersionVerifier.class));
    }

    @Test
    void testCompatibilityVerifierPropertiesEnabled() {
        this.contextRunner
            .run(context -> assertThat(context).hasSingleBean(SpringCloudAzureSpringBootVersionVerifier.class));
    }

}
