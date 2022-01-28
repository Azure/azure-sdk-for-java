// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.fileshare;

import com.azure.core.util.ClientOptions;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.implementation.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

/**
 * Storage File Share Service client builder factory, it builds the storage blob client according the configuration
 * context and blob properties.
 */
public class ShareServiceClientBuilderFactory extends AbstractAzureStorageClientBuilderFactory<ShareServiceClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShareServiceClientBuilderFactory.class);

    private final ShareServiceClientProperties shareServiceClientProperties;

    /**
     * Create a  {@link ShareServiceClientBuilderFactory} instance with the properties.
     * @param shareServiceClientProperties the properties of the share service client.
     */
    public ShareServiceClientBuilderFactory(ShareServiceClientProperties shareServiceClientProperties) {
        this.shareServiceClientProperties = shareServiceClientProperties;
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, ClientOptions> consumeClientOptions() {
        return ShareServiceClientBuilder::clientOptions;
    }

    @Override
    protected ShareServiceClientBuilder createBuilderInstance() {
        return new ShareServiceClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.shareServiceClientProperties;
    }

    @Override
    protected void configureService(ShareServiceClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(this.shareServiceClientProperties.getEndpoint()).to(builder::endpoint);
        map.from(this.shareServiceClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return ShareServiceClientBuilder::retryOptions;
    }
}
