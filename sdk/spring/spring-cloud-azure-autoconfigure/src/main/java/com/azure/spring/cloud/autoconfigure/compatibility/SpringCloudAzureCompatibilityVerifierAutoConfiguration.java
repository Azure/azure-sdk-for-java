// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * AutoConfiguration for CompatibilityVerifier
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
    value = "spring.cloud.azure.compatibility-verifier.enabled",
    matchIfMissing = true
)
@AutoConfigureOrder(0)
@EnableConfigurationProperties(SpringCloudAzureCompatibilityVerifierProperties.class)
public class SpringCloudAzureCompatibilityVerifierAutoConfiguration {

    @Bean
    SpringCloudAzureCompositeCompatibilityVerifier springCloudAzureCompositeCompatibilityVerifier(List<CompatibilityVerifier> verifiers) {
        SpringCloudAzureCompositeCompatibilityVerifier verifier = new SpringCloudAzureCompositeCompatibilityVerifier(verifiers);
        verifier.verifyDependencies();
        return verifier;
    }

    @Bean
    SpringCloudAzureSpringBootVersionVerifier springCloudAzureSpringBootVersionVerifier(SpringCloudAzureCompatibilityVerifierProperties properties) {
        return new SpringCloudAzureSpringBootVersionVerifier(properties.getCompatibleBootVersions());
    }
}
