// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

import com.azure.storage.blob.BlobServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A {@link ProtocolResolver} implementation for the {@code azure-blob://} protocol.
 *
 */
public class AzureStorageBlobProtocolResolver implements ProtocolResolver, ResourceLoaderAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageBlobProtocolResolver.class);

    private final BlobServiceClient blobServiceClient;

    public AzureStorageBlobProtocolResolver(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof DefaultResourceLoader) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            LOGGER.warn("Custom Protocol using azure-blob:// prefix will not be enabled.");
        }
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if (AzureStorageUtils.isAzureStorageResource(location, StorageType.BLOB)) {
            return new BlobStorageResource(blobServiceClient, location, true);
        }
        return null;
    }
}
