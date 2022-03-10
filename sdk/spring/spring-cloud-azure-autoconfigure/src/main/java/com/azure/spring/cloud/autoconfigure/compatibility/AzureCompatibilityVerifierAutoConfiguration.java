// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureSpringBootVersionVerifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AutoConfiguration for Spring Cloud Azure Compatibility Verifier
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.cloud.azure.compatibility-verifier.enabled", matchIfMissing = true)
@AutoConfigureOrder
@EnableConfigurationProperties(AzureCompatibilityVerifierProperties.class)
public class AzureCompatibilityVerifierAutoConfiguration {

    @Bean
    AzureSpringBootVersionVerifier springCloudAzureSpringBootVersionVerifier(AzureCompatibilityVerifierProperties properties) {
        AzureSpringBootVersionVerifier verifier = new AzureSpringBootVersionVerifier(properties.getCompatibleBootVersions());
        verifier.verify();
        return verifier;
    }
}
