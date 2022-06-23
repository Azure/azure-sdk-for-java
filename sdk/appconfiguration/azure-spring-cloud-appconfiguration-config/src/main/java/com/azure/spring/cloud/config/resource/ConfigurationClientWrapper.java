package com.azure.spring.cloud.config.resource;

import java.time.Instant;

import com.azure.data.appconfiguration.ConfigurationClient;

public class ConfigurationClientWrapper {

    private final String endpoint;

    private final ConfigurationClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    public ConfigurationClientWrapper(String endpoint, ConfigurationClient client) {
        this.endpoint = endpoint;
        this.client = client;
        this.backoffEndTime = Instant.now();
        this.failedAttempts = 0;
    }

    public Instant getBackoffEndTime() {
        return backoffEndTime;
    }

    public void updateBackoffEndTime(Instant backoffEndTime) {
        this.backoffEndTime = backoffEndTime;
        this.failedAttempts += 1;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public ConfigurationClient getClient() {
        return client;
    }

}
