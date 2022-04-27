// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Abstract protocolResolver for Storage
 */
public abstract class AbstractAzureStorageProtocolResolver implements ProtocolResolver, ResourcePatternResolver,
    ResourceLoaderAware, BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureStorageProtocolResolver.class);

    /**
     * Stores the Ant path matcher.
     */
    protected final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * The storageType of ProtocolResolver
     * @return the storage type.
     */
    protected abstract StorageType getStorageType();

    /**
     * Get Resource from resource location.
     * @param location The specified resource location.
     * @param autoCreate Whether to auto-create the resource if the resource is not exist.
     * @return the storage {@link Resource}.
     */
    protected abstract Resource getStorageResource(String location, Boolean autoCreate);

    /**
     * The bean factory used by the application context.
     */
    protected ConfigurableListableBeanFactory beanFactory;

    /**
     * List all storage containers.
     * <p>
     * The underlying storage system may support 'prefix' filter, for example, Azure Storage Blob supports this
     * <p>
     * <a href="https://docs.microsoft.com/rest/api/storageservices/list-blobs">https://docs.microsoft.com/rest/api/storageservices/list-blobs</a>
     * <p>
     * In this case, we can avoid load all containers to do client side filtering.
     *
     * @param containerPrefix container name prefix, without any wildcard characters.
     * @return All storage containers match the given prefix, or all containers if the underlying storage system doesn't
     * support prefix match.
     */
    protected abstract Stream<StorageContainerItem> listStorageContainers(String containerPrefix);

    /**
     * Get StorageContainerClient with specified container name.
     *
     * @param name Container name
     * @return the storage container client.
     */
    protected abstract StorageContainerClient getStorageContainerClient(String name);

    /**
     * Set the ResourceLoader that this object runs in.
     *
     * @param resourceLoader the ResourceLoader object to be used by this object
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof DefaultResourceLoader) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            LOGGER.warn("Custom Protocol using azure-{}:// prefix will not be enabled.", getStorageType().getType());
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if (AzureStorageUtils.isAzureStorageResource(location, getStorageType())) {
            return getResource(location);
        }
        return null;
    }

    /**
     * @see ResourcePatternResolver#getResources(java.lang.String)
     */
    @Override
    public Resource[] getResources(String pattern) throws IOException {
        Resource[] resources = null;

        if (AzureStorageUtils.isAzureStorageResource(pattern, getStorageType())) {
            if (matcher.isPattern(AzureStorageUtils.stripProtocol(pattern, getStorageType()))) {
                String containerPattern = AzureStorageUtils.getContainerName(pattern, getStorageType());
                String filePattern = AzureStorageUtils.getFilename(pattern, getStorageType());
                resources = resolveResources(containerPattern, filePattern);
            } else {
                return new Resource[] { getResource(pattern) };
            }
        }
        if (null == resources) {
            throw new IOException("Resources not found at " + pattern);
        }
        return resources;
    }

    /**
     * @see ResourcePatternResolver#getResource(java.lang.String)
     */
    @Override
    public Resource getResource(String location) {
        Resource resource = null;

        if (AzureStorageUtils.isAzureStorageResource(location, getStorageType())) {
            resource = getStorageResource(location, true);
        }

        if (null == resource) {
            throw new IllegalArgumentException("Resource not found at " + location);
        }
        return resource;
    }

    /**
     * @see ResourcePatternResolver#getClassLoader()
     */
    @Override
    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    /**
     * Storage container item.
     */
    protected static class StorageContainerItem {

        private final String name;

        /**
         * Creates a new instance of {@link StorageContainerItem}.
         *
         * @param name Name of the container item.
         */
        public StorageContainerItem(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the container item.
         *
         * @return The name of the container item.
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Storage item.
     */
    protected static class StorageItem {

        private final String container;
        private final String name;
        private final StorageType storageType;

        /**
         * Creates a new instance of {@link StorageItem}.
         *
         * @param container The container name.
         * @param fileName The file name.
         * @param storageType The storage type.
         */
        public StorageItem(String container, String fileName, StorageType storageType) {
            this.container = container;
            this.name = fileName;
            this.storageType = storageType;
        }

        /**
         * Gets the container name.
         *
         * @return The container name.
         */
        public String getContainer() {
            return container;
        }

        /**
         * Gets the item name.
         *
         * @return The item name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the storage type.
         *
         * @return The storage type.
         */
        public StorageType getStorageType() {
            return storageType;
        }

        /**
         * Gets the resource location equivalent for this item.
         *
         * @return The resource location equivalent for this item.
         */
        public String toResourceLocation() {
            return AzureStorageUtils.getStorageProtocolPrefix(getStorageType()) + container + "/" + name;
        }
    }

    /**
     * Storage container client.
     */
    protected interface StorageContainerClient {

        /**
         * Gets the container name.
         *
         * @return The container name.
         */
        String getName();

        /**
         * Normally, a cloud storage system doesn't support wildcard pattern matching, but support prefix match
         *
         * @param  itemPrefix the prefix of item’s path
         * @return All items with the given prefix, or all items if the underlying system doesn't
         * support prefix matching.
         */
        Stream<StorageItem> listItems(String itemPrefix);

    }

    /**
     * List all resources with specified container pattern and item pattern.
     *
     * @param containerPattern An ant style string which represent containers.
     * @param itemPattern An ant style string which represent storage items.
     * @return All resources matching the provided patterns.
     */
    protected Resource[] resolveResources(String containerPattern, String itemPattern) {
        return getMatchedContainers(containerPattern)
            .flatMap(c -> getMatchedItems(c, itemPattern))
            .map(s -> getStorageResource(s.toResourceLocation(), false))
            .toArray(Resource[]::new);
    }

    /**
     * List all containers with the provided pattern.
     *
     * @param pattern An ant style string which represent containers.
     * @return All container clients matching the provided pattern.
     */
    protected Stream<StorageContainerClient> getMatchedContainers(String pattern) {
        //if the given pattern doesn't have any wildcard characters, we can avoid the complex client side filtering.
        if (matcher.isPattern(pattern)) {
            //trying to extract prefix from the pattern, so we can leverage server side filtering first.
            return listStorageContainers(getValidPrefix(pattern))
                //client side filtering to find the containers
                .filter(i -> matcher.match(pattern, i.getName()))
                .map(i -> getStorageContainerClient(i.getName()));
        } else {
            return Stream.of(getStorageContainerClient(pattern));
        }
    }

    /**
     * List all storage items with specified container and item pattern.
     *
     * @param containerClient The specified container where to get storage items.
     * @param itemPattern An ant style string represents StorageItems.
     * @return All matching items.
     */
    protected Stream<StorageItem> getMatchedItems(StorageContainerClient containerClient, String itemPattern) {
        if (matcher.isPattern(itemPattern)) {
            //trying to extract prefix from the pattern, so we can leverage server side filtering first.
            return containerClient.listItems(getValidPrefix(itemPattern))
                                  //client side filtering to find the containers
                                  .filter(item -> matcher.match(itemPattern, item.getName()));
        } else {
            return Stream.of(new StorageItem(containerClient.getName(), itemPattern, getStorageType()));
        }
    }

    private String getValidPrefix(String keyPattern) {
        int starIndex = keyPattern.indexOf("*");
        int markIndex = keyPattern.indexOf("?");
        int index = Math.min(starIndex == -1 ? keyPattern.length() : starIndex,
            markIndex == -1 ? keyPattern.length() : markIndex);
        return keyPattern.substring(0, index);
    }
}
