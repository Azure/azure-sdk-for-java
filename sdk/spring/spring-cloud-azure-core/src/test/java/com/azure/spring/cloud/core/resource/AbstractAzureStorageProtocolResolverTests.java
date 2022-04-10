// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.azure.spring.cloud.core.resource.AzureStorageUtils.getStorageProtocolPrefix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractAzureStorageProtocolResolverTests {
    protected static final String CONTAINER_NAME = "container";
    protected static final String NON_EXISTING = "non-existing";
    protected static final String EXISTING_ITEM_NAME = "blob";
    protected static final long CONTENT_LENGTH = 4096L;
    private DefaultResourceLoader resourceLoader;

    private ProtocolResolver protocolResolver;

    @BeforeEach
    public void setup() {
        //we have to initialize sdk client first
        initializeSDKClient();
        resourceLoader = new DefaultResourceLoader();
        protocolResolver = createInstance();
        if (protocolResolver instanceof ResourceLoaderAware) {
            ((ResourceLoaderAware) protocolResolver).setResourceLoader(resourceLoader);
        }
    }

    protected abstract ProtocolResolver createInstance();

    protected abstract void initializeSDKClient();

    protected abstract StorageType getStorageType();

    protected Resource getResource(String location) {
        return resourceLoader.getResource(getStorageProtocolPrefix(getStorageType()) + location);
    }

    protected WritableResource getWritableResource(String location) {
        Resource resource = getResource(location);
        assertTrue(resource instanceof WritableResource);
        return (WritableResource) resource;
    }

    protected Resource[] getResources(String... locations) {
        List<Resource> resources = new ArrayList<>();
        for (String location : locations) {
            Resource resource = resourceLoader.getResource(location);
            if (resource instanceof WritableResource) {
                resources.add(resource);
            }
        }
        return resources.toArray(new Resource[0]);
    }

    @Test
    void testEmptyPath() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> getResource(""));
    }

    @Test
    void testSlashPath() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> getResource("/"));
    }

    @Test
    void testGetResourceWithExistingResource() {
        String resourceName = CONTAINER_NAME + "/" + EXISTING_ITEM_NAME;
        Resource resource = getResource(resourceName);
        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void testValidObject() throws Exception {
        Resource resource = getResource("container/blob");
        assertTrue(resource.exists());
        assertEquals(CONTENT_LENGTH, resource.contentLength());
    }

    @Test
    void testWritable() throws Exception {
        WritableResource writableResource = getWritableResource("container/blob");
        assertTrue(writableResource.isWritable());
        assertNotNull(writableResource.getOutputStream());
    }

    /**
     * By default, Azure Storage Resource is autoCreated.
     */
    @Test
    void testWritableResourceAutoCreate() {
        assertNotNull(getWritableResource("container/non-existing"));
    }

    @Test
    void testGetInputStreamOnNullBlob() {
        Resource resource = getResource("container/non-existing");
        assertThrows(FileNotFoundException.class, resource::getInputStream);
    }

    @Test
    void testGetFilenameOnNonExistingBlob() {
        assertEquals(NON_EXISTING, getResource("container/non-existing").getFilename());
    }

    @Test
    void testContainerDoesNotExist() {
        assertFalse(getResource("non-existing/blob").exists());
    }

    @Test
    void testContainerExistsButResourceDoesNot() {
        Resource resource = getResource("container/non-existing");
        assertFalse(resource.exists());
    }

}
