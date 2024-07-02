// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties("spring.cloud.azure.compatibility-verifier")
public class AzureCompatibilityVerifierProperties {

    /**
     * Whether to enable the Spring Cloud Azure compatibility verifier.
     */
    private boolean enabled = true;
    /**
     * Comma-delimited list of Spring Boot versions that are compatible with current Spring Cloud Azure's version.
     */
    private List<String> compatibleBootVersions = Arrays.asList("3.0.x", "3.1.x", "3.2.x", "3.3.x");

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getCompatibleBootVersions() {
        return this.compatibleBootVersions;
    }

    public void setCompatibleBootVersions(List<String> compatibleBootVersions) {
        this.compatibleBootVersions = compatibleBootVersions;
    }
}
