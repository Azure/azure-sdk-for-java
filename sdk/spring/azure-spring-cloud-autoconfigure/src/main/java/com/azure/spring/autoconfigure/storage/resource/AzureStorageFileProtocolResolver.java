// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

import com.azure.storage.file.share.ShareServiceClient;
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
 * A {@link ProtocolResolver} implementation for the {@code azure-file://} protocol.
 *
 */
public class AzureStorageFileProtocolResolver implements ProtocolResolver, ResourceLoaderAware,
                                                         BeanFactoryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageFileProtocolResolver.class);

    private ConfigurableListableBeanFactory beanFactory;
    private ShareServiceClient shareServiceClient;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof DefaultResourceLoader) {
            ((DefaultResourceLoader) resourceLoader).addProtocolResolver(this);
        } else {
            LOGGER.warn("Custom Protocol using azure-file:// prefix will not be enabled.");
        }
    }

    private ShareServiceClient getShareServiceClient() {
        if (shareServiceClient == null) {
            this.shareServiceClient = this.beanFactory.getBean(ShareServiceClient.class);
        }

        return shareServiceClient;
    }

    @Override
    public Resource resolve(String location, ResourceLoader resourceLoader) {
        if (AzureStorageUtils.isAzureStorageResource(location, StorageType.FILE)) {
            return new StorageFileResource(getShareServiceClient(), location, true);
        }

        return null;
    }
}
