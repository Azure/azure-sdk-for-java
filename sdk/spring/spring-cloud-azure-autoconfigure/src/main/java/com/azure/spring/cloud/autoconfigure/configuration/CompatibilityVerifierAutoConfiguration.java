// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.configuration;

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
    value = {"spring.cloud.azure.compatibility-verifier.enabled"},
    matchIfMissing = true
)
@AutoConfigureOrder(0)
@EnableConfigurationProperties({CompatibilityVerifierProperties.class})
public class CompatibilityVerifierAutoConfiguration {
    public CompatibilityVerifierAutoConfiguration() {
    }

    @Bean
    CompositeCompatibilityVerifier compositeCompatibilityVerifier(List<CompatibilityVerifier> verifiers) {
        CompositeCompatibilityVerifier verifier = new CompositeCompatibilityVerifier(verifiers);
        verifier.verifyDependencies();
        return verifier;
    }

    @Bean
    SpringBootVersionVerifier springBootVersionVerifier(CompatibilityVerifierProperties properties) {
        return new SpringBootVersionVerifier(properties.getCompatibleBootVersions());
    }
}
