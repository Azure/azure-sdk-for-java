// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Properties for CompatibilityVerifier
 */
@ConfigurationProperties("spring.cloud.azure.compatibility-verifier")
public class AzureCompatibilityVerifierProperties {

    /**
     * Whether to enable the Spring Cloud Azure compatibility verifier.
     */
    private boolean enabled = true;
    /**
     * Comma-delimited list of Spring Boot versions that are compatible with current Spring Cloud Azure's version.
     */
    private List<String> compatibleBootVersions = Arrays.asList("2.5.x", "2.6.x", "2.7.x", "3.0.x");

    /**
     * Whether to enable the Spring Cloud Azure compatibility verifier.
     * @return whether to enable the Spring Cloud Azure compatibility verifier.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set whether to enable the Spring Cloud Azure compatibility verifier.
     * @param enabled whether to enable the Spring Cloud Azure compatibility verifier.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the list of Spring Boot versions that are compatible with current Spring Cloud Azure's version.
     * @return the compatible Spring Boot versions.
     */
    public List<String> getCompatibleBootVersions() {
        return this.compatibleBootVersions;
    }

    /**
     * Set the list of Spring Boot versions that are compatible with current Spring Cloud Azure's version.
     * @param compatibleBootVersions the compatible Spring Boot versions.
     */
    public void setCompatibleBootVersions(List<String> compatibleBootVersions) {
        this.compatibleBootVersions = compatibleBootVersions;
    }
}
