// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.blob;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.TokenAuthenticationDescriptor;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.util.PropertyMapper;
import com.azure.spring.service.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.spring.service.storage.common.credential.StorageSharedKeyAuthenticationDescriptor;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.policy.RequestRetryOptions;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Storage Blob Service client builder factory, it builds the storage blob client according the configuration context
 * and blob properties.
 */
public class BlobServiceClientBuilderFactory extends AbstractAzureStorageClientBuilderFactory<BlobServiceClientBuilder> {

    private final StorageBlobProperties blobProperties;

    public BlobServiceClientBuilderFactory(StorageBlobProperties blobProperties) {
        this.blobProperties = blobProperties;
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
        map.from(blobProperties.getCustomerProvidedKey()).to(CustomerProvidedKey::new);
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
    protected BiConsumer<BlobServiceClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return BlobServiceClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return BlobServiceClientBuilder::httpLogOptions;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, Configuration> consumeConfiguration() {
        return BlobServiceClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        return BlobServiceClientBuilder::credential;
    }

    @Override
    protected BiConsumer<BlobServiceClientBuilder, String> consumeConnectionString() {
        return BlobServiceClientBuilder::connectionString;
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
    protected BiConsumer<BlobServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return BlobServiceClientBuilder::retryOptions;
    }
}
