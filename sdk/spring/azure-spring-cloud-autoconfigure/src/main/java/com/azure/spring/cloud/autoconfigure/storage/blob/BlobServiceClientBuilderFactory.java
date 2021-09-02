// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.spring.cloud.autoconfigure.storage.common.credential.StorageSharedKeyAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.factory.AbstractAzureHttpClientBuilderFactory;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_STORAGE_BLOB;
import static com.azure.spring.core.ApplicationId.VERSION;

/**
 * Storage Blob Service client builder factory, it builds the storage blob client according the configuration context
 * and blob properties.
 */
public class BlobServiceClientBuilderFactory extends AbstractAzureHttpClientBuilderFactory<BlobServiceClientBuilder> {

    private final AzureStorageBlobProperties blobProperties;

    public BlobServiceClientBuilderFactory(AzureStorageBlobProperties blobProperties) {
        this.blobProperties = blobProperties;
    }


    @Override
    public BlobServiceClientBuilder createBuilderInstance() {
        return new BlobServiceClientBuilder();
    }

    @Override
    public void configureService(BlobServiceClientBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(blobProperties.getCustomerProvidedKey()).to(builder::customerProvidedKey);
        map.from(blobProperties.getEncryptionScope()).to(builder::encryptionScope);
        map.from(blobProperties.getEndpoint()).to(builder::endpoint);
        map.from(blobProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, HttpClient> consumeHttpClient() {
        return BlobServiceClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return BlobServiceClientBuilder::addPolicy;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, Configuration> consumeConfiguration() {
        return BlobServiceClientBuilder::configuration;
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return blobProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(BlobServiceClientBuilder builder) {
        return Arrays.asList(
            new StorageSharedKeyAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())),
            new SasAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())),
            new TokenAuthenticationDescriptor(provider -> builder.credential(provider.getCredential()))
        );
    }

    @Override
    protected String getApplicationId() {
        return AZURE_SPRING_STORAGE_BLOB + VERSION;
    }
}
