package com.azure.spring.cloud.autoconfigure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties("spring.cloud.azure.compatibility-verifier")
public class CompatibilityVerifierProperties {
    private boolean enabled;
    private List<String> compatibleBootVersions = Arrays.asList("2.5.x", "2.6.x");

    public CompatibilityVerifierProperties() {
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
