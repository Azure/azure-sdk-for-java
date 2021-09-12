// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.context.core.api.ResourceCrud;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
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
public abstract class AbstractResourceCrud<T, K> implements ResourceCrud<T, K> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourceCrud.class);

    protected final AzureResourceManager resourceManager;
    protected final AzureResourceMetadata resourceMetadata;

    public AbstractResourceCrud(@NonNull AzureResourceManager resourceManager,
                                @NonNull AzureResourceMetadata resourceMetadata) {
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

        return create(key);
    }

    abstract String getResourceName(K key);

    abstract String getResourceType();

    abstract T internalGet(K key);

    abstract T internalCreate(K key);
}
