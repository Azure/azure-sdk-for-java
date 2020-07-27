/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManager;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;

public abstract class AzureManager<T, K> implements ResourceManager<T, K> {
    private static final Logger log = LoggerFactory.getLogger(AzureManager.class);

    protected final AzureProperties azureProperties;
    protected final Azure azure;

    public AzureManager(@NonNull Azure azure, @NonNull AzureProperties azureProperties) {
        this.azure = azure;
        this.azureProperties = azureProperties;
    }

    @Override
    public boolean exists(K key) {
        return get(key) != null;
    }

    @Override
    public T get(K key) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String name = getResourceName(key);

        try {
            log.info("Fetching {} with name '{}' ...", getResourceType(), name);
            return internalGet(key);
        } catch (CloudException e) {
            String errorMessage = String.join(", ", e.getMessage(), e.body().code(), e.body().message());
            String message = String.format("Fetching %s with name '%s' failed due to: %s", getResourceType(), name,
                    errorMessage);
            log.error(message);
            throw new RuntimeException(message);
        } finally {
            stopWatch.stop();
            log.info("Fetching {} with name '{}' finished in {} seconds", getResourceType(), name,
                    stopWatch.getTime(TimeUnit.SECONDS));
        }
    }

    @Override
    public T create(K key) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String name = getResourceName(key);

        try {
            log.info("Creating {} with name '{}' ...", getResourceType(), name);
            return internalCreate(key);
        } catch (CloudException e) {
            String errorMessage = String.join(", ", e.getMessage(), e.body().code(), e.body().message());
            String message = String.format("Creating %s with name '%s' failed due to: %s", getResourceType(), name,
                    errorMessage);
            log.error(message);
            throw new RuntimeException(message);
        } finally {
            stopWatch.stop();
            log.info("Creating {} with name '{}' finished in {} seconds", getResourceType(), name,
                    stopWatch.getTime(TimeUnit.SECONDS));
        }
    }

    @Override
    public T getOrCreate(K key) {
        T result = get(key);

        if (result != null) {
            return result;
        }

        if (!azureProperties.isAutoCreateResources()) {
            String message = String.format("%s with name '%s' not existed.", getResourceType(), getResourceName(key));
            log.warn(message);
            String enable = "If you want to enable automatic resource creation. Please set spring.cloud.azure" +
                    ".auto-create-resources=true";
            throw new IllegalArgumentException(message + enable);
        }

        return create(key);
    }

    abstract String getResourceName(K key);

    abstract String getResourceType();

    abstract T internalGet(K key);

    abstract T internalCreate(K key);
}
