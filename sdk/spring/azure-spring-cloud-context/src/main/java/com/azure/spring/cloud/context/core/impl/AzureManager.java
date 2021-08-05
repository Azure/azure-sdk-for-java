// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.spring.cloud.context.core.api.ResourceManager;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.StopWatch;

/**
 * Abstract Azure resource manager.
 *
 * @param <T> The type of resource.
 * @param <K> The type of resource key.
 */
public abstract class AzureManager<T, K> implements ResourceManager<T, K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureManager.class);

    private final AzureProperties azureProperties;
    protected final String resourceGroup;
    protected final String region;

    public AzureManager(@NonNull AzureProperties azureProperties) {
        this.azureProperties = azureProperties;
        this.resourceGroup = azureProperties.getResourceGroup();
        this.region = azureProperties.getRegion();
    }

    @Override
    public boolean exists(K key) {
        boolean exists = get(key) != null;
        if (!exists) {
            LOGGER.debug("{} '{}' does not exist.", getResourceType(), getResourceName(key));
        }
        return exists;
    }

    @Override
    public T get(K key) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final String resourceType = getResourceType();
        final String name = getResourceName(key);

        try {
            LOGGER.info("Fetching {} with name '{}' ...", resourceType, name);
            return internalGet(key);
        } catch (ManagementException e) {
            LOGGER.error("Fetching {} with name '{}' failed due to: {}", resourceType, name, e.toString());
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            stopWatch.stop();
            LOGGER.info("Fetching {} with name '{}' finished in {} seconds", resourceType, name,
                stopWatch.getTotalTimeMillis() / 1000);
        }
    }

    @Override
    public T create(K key) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final String resourceType = getResourceType();
        final String name = getResourceName(key);

        try {
            LOGGER.info("Creating {} with name '{}' ...", resourceType, name);
            return internalCreate(key);
        } catch (ManagementException e) {
            LOGGER.error("Creating {} with name '{}' failed due to: {}", resourceType, name, e.toString());
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            stopWatch.stop();
            LOGGER.info("Creating {} with name '{}' finished in {} seconds", getResourceType(), name,
                stopWatch.getTotalTimeMillis() / 1000);
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
            LOGGER.warn(message);
            String enable = "If you want to enable automatic resource creation. Please set spring.cloud.azure"
                + ".auto-create-resources=true";
            throw new IllegalArgumentException(message + enable);
        }

        return create(key);
    }

    abstract String getResourceName(K key);

    abstract String getResourceType();

    abstract T internalGet(K key);

    abstract T internalCreate(K key);
}
