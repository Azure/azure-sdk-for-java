// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.file.share.ShareServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A {@link ProtocolResolver} implementation for the {@code azure-blob://} or {@code azure-file://} protocol.
 *
 * @author Warren Zhu
 */
public class AzureStorageProtocolResolver implements ProtocolResolver, ResourceLoaderAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageProtocolResolver.class);

    private final BlobServiceClient blobServiceClient;
    private final ShareServiceClient shareServiceClient;

    public AzureStorageProtocolResolver(BlobServiceClient blobServiceClient,
                                        ShareServiceClient shareServiceClient) {
        this.blobServiceClient = blobServiceClient;
        this.shareServiceClient = shareServiceClient;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof DefaultResourceLoader) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            LOGGER.warn("Custom Protocol using azure-blob:// or azure-file:// prefix will not be enabled.");
        }
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if (AzureStorageUtils.isAzureStorageResource(location, StorageType.BLOB)) {
            if (blobServiceClient != null) {
                return new BlobStorageResource(blobServiceClient, location, true);
            } else {
                LOGGER.warn("No blob service client is configured, so the blob resources can't be resolved.");
                return null;
            }
        } else if (AzureStorageUtils.isAzureStorageResource(location, StorageType.FILE)) {
            if (shareServiceClient != null) {
                return new FileStorageResource(shareServiceClient, location, true);
            } else {
                LOGGER.warn("No file share service client is configured, so the file resources can't be resolved.");
                return null;
            }
        }

        return null;
    }
}
