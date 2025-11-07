// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.lang.Nullable;
import org.springframework.util.StopWatch;

/**
 * Abstract Azure resource manager.
 *
 * @param <T> The type of resource.
 * @param <K> The type of resource key.
 * @param <P> Azure resource properties.
 */
public abstract class AbstractResourceCrud<T, K, P> implements ResourceCrud<T, K, P> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourceCrud.class);
    static final int RESOURCE_NOT_FOUND = 404;

    protected final AzureResourceManager resourceManager;
    protected final AzureResourceMetadata resourceMetadata;

    /**
     * Creates a new instance of {@link AbstractResourceCrud}.
     *
     * @param resourceManager The Azure resource manager.
     * @param resourceMetadata The Azure resource metadata.
     */
    protected AbstractResourceCrud(AzureResourceManager resourceManager,
                                AzureResourceMetadata resourceMetadata) {
        this.resourceManager = resourceManager;
        this.resourceMetadata = resourceMetadata;
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
            String message = String.format("Fetching %s with name '%s' failed due to: %s", resourceType, name, e.toString());
            throw new RuntimeException(message, e);
        } finally {
            stopWatch.stop();
            LOGGER.info("Fetching {} with name '{}' finished in {} seconds", resourceType, name,
                stopWatch.getTotalTimeMillis() / 1000);
        }
    }

    @Override
    public T create(K key) {
        return doCreate(key, null);
    }

    @Override
    public T create(K key, P properties) {
        return doCreate(key, properties);
    }

    private T doCreate(K key, @Nullable P properties) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final String resourceType = getResourceType();
        final String name = getResourceName(key);

        try {
            LOGGER.info("Creating {} with name '{}' ...", resourceType, name);
            return internalCreate(key, properties);
        } catch (ManagementException e) {
            String message = String.format("Creating %s with name '%s' failed due to: %s", resourceType, name, e.toString());
            throw new RuntimeException(message, e);
        } finally {
            stopWatch.stop();
            LOGGER.info("Creating {} with name '{}' finished in {} seconds", getResourceType(), name,
                stopWatch.getTotalTimeMillis() / 1000);
        }
    }

    @Override
    public T getOrCreate(K key) {
        return doGetOrCreate(key, null);
    }

    @Override
    public T getOrCreate(K key, P properties) {
        return doGetOrCreate(key, properties);
    }

    private T doGetOrCreate(K key, @Nullable P properties) {
        T result = get(key);

        if (result != null) {
            return result;
        }

        return doCreate(key, properties);
    }

    abstract String getResourceName(K key);

    abstract String getResourceType();

    abstract T internalGet(K key);

    abstract T internalCreate(K key);

    T internalCreate(K key, @Nullable P properties) {
        return internalCreate(key);
    }
}
