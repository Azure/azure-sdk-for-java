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
public class SpringCloudAzureCompatibilityVerifierProperties {
    private boolean enabled;
    private List<String> compatibleBootVersions = Arrays.asList("2.5.x", "2.6.x");

    public SpringCloudAzureCompatibilityVerifierProperties() {
    }

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
