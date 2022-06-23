// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.resource;

import java.time.Instant;

import com.azure.data.appconfiguration.ConfigurationClient;

/**
 * Wrapper for Configuration Client to manage backoff.
 */
public class ConfigurationClientWrapper {

    private final String endpoint;

    private final ConfigurationClient client;

    private Instant backoffEndTime;

    private int failedAttempts;

    /**
     * Wrapper for Configuration Client to manage backoff.
     * @param endpoint client endpoint
     * @param client Configuration Client to App Configuration store
     */
    public ConfigurationClientWrapper(String endpoint, ConfigurationClient client) {
        this.endpoint = endpoint;
        this.client = client;
        this.backoffEndTime = Instant.now();
        this.failedAttempts = 0;
    }

    /**
     * @return backOffEndTime
     */
    public Instant getBackoffEndTime() {
        return backoffEndTime;
    }

    /**
     * Updates the backoff time and increases the number of failed attempts.
     * @param backoffEndTime next time this client can be used.
     */
    public void updateBackoffEndTime(Instant backoffEndTime) {
        this.backoffEndTime = backoffEndTime;
        this.failedAttempts += 1;
    }

    /**
     * @return number of failed attempts
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /**
     * Sets the number of failed attempts to 0.
     */
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }

    /**
     * @return endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @return ConfiguraitonClinet
     */
    public ConfigurationClient getClient() {
        return client;
    }

}
