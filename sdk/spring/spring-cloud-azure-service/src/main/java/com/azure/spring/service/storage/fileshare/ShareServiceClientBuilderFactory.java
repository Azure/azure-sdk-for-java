// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.fileshare;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.Configuration;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.service.core.PropertyMapper;
import com.azure.spring.service.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.spring.service.storage.common.credential.StorageSharedKeyAuthenticationDescriptor;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.spring.core.ApplicationId.AZURE_SPRING_STORAGE_FILES;
import static com.azure.spring.core.ApplicationId.VERSION;

/**
 * Storage File Share Service client builder factory, it builds the storage blob client according the configuration
 * context and blob properties.
 */
public class ShareServiceClientBuilderFactory extends AbstractAzureStorageClientBuilderFactory<ShareServiceClientBuilder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShareServiceClientBuilderFactory.class);

    private final StorageFileShareProperties fileShareProperties;

    public ShareServiceClientBuilderFactory(StorageFileShareProperties fileShareProperties) {
        this.fileShareProperties = fileShareProperties;
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, HttpClient> consumeHttpClient() {
        return ShareServiceClientBuilder::httpClient;
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, HttpPipelinePolicy> consumeHttpPipelinePolicy() {
        return ShareServiceClientBuilder::addPolicy;
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, HttpPipeline> consumeHttpPipeline() {
        return ShareServiceClientBuilder::pipeline;
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, HttpLogOptions> consumeHttpLogOptions() {
        return ShareServiceClientBuilder::httpLogOptions;
    }

    @Override
    protected ShareServiceClientBuilder createBuilderInstance() {
        return new ShareServiceClientBuilder();
    }

    @Override
    protected AzureProperties getAzureProperties() {
        return this.fileShareProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(ShareServiceClientBuilder builder) {
        return Arrays.asList(
            new StorageSharedKeyAuthenticationDescriptor(provider -> builder.credential(provider.getCredential())),
            new SasAuthenticationDescriptor(provider -> builder.credential(provider.getCredential()))
        );
    }

    @Override
    protected void configureService(ShareServiceClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(this.fileShareProperties.getEndpoint()).to(builder::endpoint);
        map.from(this.fileShareProperties.getServiceVersion()).to(builder::serviceVersion);
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, Configuration> consumeConfiguration() {
        return ShareServiceClientBuilder::configuration;
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, TokenCredential> consumeDefaultTokenCredential() {
        LOGGER.warn("TokenCredential is not supported to configure in ShareServiceClientBuilder.");
        return (a, b) -> { };
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, String> consumeConnectionString() {
        return ShareServiceClientBuilder::connectionString;
    }

    @Override
    protected String getApplicationId() {
        return AZURE_SPRING_STORAGE_FILES + VERSION;
    }

    @Override
    protected BiConsumer<ShareServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return ShareServiceClientBuilder::retryOptions;
    }
}
