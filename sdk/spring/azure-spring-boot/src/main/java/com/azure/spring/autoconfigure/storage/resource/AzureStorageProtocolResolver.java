// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceClientBuilder;
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

/**
 * A {@link ProtocolResolver} implementation for the {@code azure-blob://} or {@code azure-file://} protocol.
 *
 * @author Warren Zhu
 */
public class AzureStorageProtocolResolver implements ProtocolResolver, BeanFactoryPostProcessor, ResourceLoaderAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageProtocolResolver.class);
    private ConfigurableListableBeanFactory beanFactory;
    private BlobServiceClientBuilder blobServiceClientBuilder;
    private ShareServiceClientBuilder shareServiceClientBuilder;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
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
            return new BlobStorageResource(getBlobServiceClientBuilder().buildClient(), location, true);
        } else if (AzureStorageUtils.isAzureStorageResource(location, StorageType.FILE)) {
            return new FileStorageResource(getShareServiceClientBuilder().buildClient(), location, true);
        }

        return null;
    }

    private BlobServiceClientBuilder getBlobServiceClientBuilder() {
        if (blobServiceClientBuilder == null) {
            this.blobServiceClientBuilder = this.beanFactory.getBean(BlobServiceClientBuilder.class);
        }

        return blobServiceClientBuilder;
    }

    private ShareServiceClientBuilder getShareServiceClientBuilder() {
        if (shareServiceClientBuilder == null) {
            this.shareServiceClientBuilder = this.beanFactory.getBean(ShareServiceClientBuilder.class);
        }

        return shareServiceClientBuilder;
    }
}
