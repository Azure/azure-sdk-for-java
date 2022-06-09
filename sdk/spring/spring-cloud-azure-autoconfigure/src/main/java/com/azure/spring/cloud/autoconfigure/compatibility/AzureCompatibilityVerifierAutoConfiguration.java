// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import com.azure.spring.cloud.autoconfigure.implementation.compatibility.AzureSpringBootVersionVerifier;
import com.azure.spring.cloud.autoconfigure.implementation.compatibility.ClassNameResolverPredicate;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Cloud Azure compatibility verifier.
 *
 * @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "spring.cloud.azure.compatibility-verifier.enabled", matchIfMissing = true)
@AutoConfigureOrder
@EnableConfigurationProperties(AzureCompatibilityVerifierProperties.class)
public class AzureCompatibilityVerifierAutoConfiguration {

    @Bean
    AzureSpringBootVersionVerifier springCloudAzureSpringBootVersionVerifier(AzureCompatibilityVerifierProperties properties) {
        AzureSpringBootVersionVerifier verifier = new AzureSpringBootVersionVerifier(properties.getCompatibleBootVersions(), new ClassNameResolverPredicate());
        verifier.verify();
        return verifier;
    }
}
