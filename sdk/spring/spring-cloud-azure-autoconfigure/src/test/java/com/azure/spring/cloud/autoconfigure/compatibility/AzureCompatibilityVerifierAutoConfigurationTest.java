// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureSpringBootVersionVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureCompatibilityVerifierAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureCompatibilityVerifierAutoConfiguration.class));

    @Test
    void testCompatibilityVerifierPropertiesDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.compatibility-verifier.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureSpringBootVersionVerifier.class));
    }

    @Test
    void testCompatibilityVerifierPropertiesEnabled() {
        this.contextRunner
            .run(context -> assertThat(context).hasSingleBean(AzureSpringBootVersionVerifier.class));
    }

    @Test
    void testCompatibleSpringBootVersionCanSet() {
        String version = SpringBootVersion.getVersion();
        this.contextRunner
            .withPropertyValues(
                String.format("spring.cloud.azure.compatibility-verifier.compatible-boot-versions=%s", version)
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureCompatibilityVerifierProperties.class);
                AzureCompatibilityVerifierProperties verifierProperties = context.getBean(AzureCompatibilityVerifierProperties.class);
                assertThat(verifierProperties.getCompatibleBootVersions()).isEqualTo(Arrays.asList(version));
            });
    }
}
