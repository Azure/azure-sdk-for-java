// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.blob;

import com.azure.core.util.ClientOptions;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.implementation.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.policy.RequestRetryOptions;

import java.util.function.BiConsumer;

/**
 * Storage Blob Service client builder factory, it builds the storage blob client according the configuration context
 * and blob properties.
 */
public class BlobServiceClientBuilderFactory extends AbstractAzureStorageClientBuilderFactory<BlobServiceClientBuilder> {

    private final BlobServiceClientProperties blobServiceClientProperties;

    /**
     * Create a {@link BlobServiceClientBuilderFactory} with the {@link BlobServiceClientProperties}.
     * @param blobServiceClientProperties the properties for the blob service client.
     */
    public BlobServiceClientBuilderFactory(BlobServiceClientProperties blobServiceClientProperties) {
        this.blobServiceClientProperties = blobServiceClientProperties;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, ClientOptions> consumeClientOptions() {
        return BlobServiceClientBuilder::clientOptions;
    }

    @Override
    public BlobServiceClientBuilder createBuilderInstance() {
        return new BlobServiceClientBuilder();
    }

    @Override
    public void configureService(BlobServiceClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(blobServiceClientProperties.getCustomerProvidedKey()).to(CustomerProvidedKey::new);
        map.from(blobServiceClientProperties.getEncryptionScope()).to(builder::encryptionScope);
        map.from(blobServiceClientProperties.getEndpoint()).to(builder::endpoint);
        map.from(blobServiceClientProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return blobServiceClientProperties;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return BlobServiceClientBuilder::retryOptions;
    }
}
