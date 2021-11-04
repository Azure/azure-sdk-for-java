package com.azure.spring.core.resource;

import java.io.IOException;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;

public abstract class AbstractAzureStorageProtocolResolver implements ProtocolResolver, ResourcePatternResolver,
    ResourceLoaderAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAzureStorageProtocolResolver.class);

    /**
     * Stores the Ant path matcher.
     */
    protected final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof DefaultResourceLoader) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            LOGGER.warn("Custom Protocol using azure-{}:// prefix will not be enabled.", getStorageType().getType());
        }
    }

    protected abstract StorageType getStorageType();


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

    protected abstract Resource getStorageResource(String location, Boolean autoCreate);

    protected static class StorageItem {

        private final String container;
        private final String name;
        private final StorageType storageType;

        public StorageItem(String container, String item, StorageType storageType) {
            this.container = container;
            this.name = item;
            this.storageType = storageType;
        }

        public String getContainer() {
            return container;
        }

        public String getName() {
            return name;
        }

        public StorageType getStorageType() {
            return storageType;
        }

        public String toResourceLocation() {
            return AzureStorageUtils.getStorageProtocolPrefix(getStorageType()) + container + "/" + name;
        }
    }

    protected static class StorageContainerItem {

        private final String name;

        public StorageContainerItem(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    protected interface StorageContainerClient {

        String getName();

        /**
         * List all storage items with the given prefix, or all items if the underlying system doesn't support prefix
         * matching.
         * <p>
         * Normally, a cloud storage system doesn't support wildcard pattern matching, but support prefix match
         */
        Stream<StorageItem> listItems(String itemPrefix);

    }

    private String getValidPrefix(String keyPattern) {
        int starIndex = keyPattern.indexOf("*");
        int markIndex = keyPattern.indexOf("?");
        int index = Math.min(starIndex == -1 ? keyPattern.length() : starIndex,
            markIndex == -1 ? keyPattern.length() : markIndex);
        //        String beforeIndex = keyPattern.substring(0, index);
        return keyPattern.substring(0, index);
        //        return beforeIndex.contains("/") ? beforeIndex.substring(0, beforeIndex.lastIndexOf('/') + 1) :
        //        beforeIndex;
    }

    /**
     * List all storage containers.
     * <p>
     * The underlying storage system may support 'prefix' filter, for example, Azure Storage Blob supports this
     * <p>
     * https://docs.microsoft.com/en-us/rest/api/storageservices/list-blobs
     * <p>
     * In this case, we can avoid load all containers to do client side filtering.
     *
     * @param containerPrefix container name prefix, without any wildcard characters.
     * @return All storage containers match the given prefix, or all containers if the underlying storage system doesn't
     * support prefix match.
     */
    protected abstract Stream<StorageContainerItem> listStorageContainers(String containerPrefix);

    protected abstract StorageContainerClient getStorageContainerClient(String name);

    protected Resource[] resolveResources(String containerPattern, String itemPattern) {
        return getMatchedContainers(containerPattern)
            .flatMap(c -> getMatchedItems(c, itemPattern))
            .map(s -> getStorageResource(s.toResourceLocation(), false))
            .toArray(Resource[]::new);
    }

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
}
