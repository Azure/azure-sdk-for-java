// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.fileshare;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.credential.descriptor.SasAuthenticationDescriptor;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.storage.common.AbstractAzureStorageClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.storage.common.credential.StorageSharedKeyAuthenticationDescriptor;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
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
        return this.shareServiceClientProperties;
    }

    @Override
    protected List<AuthenticationDescriptor<?>> getAuthenticationDescriptors(ShareServiceClientBuilder builder) {
        return Arrays.asList(
            new StorageSharedKeyAuthenticationDescriptor(builder::credential),
            new SasAuthenticationDescriptor(builder::credential)
        );
    }

    @Override
    protected void configureService(ShareServiceClientBuilder builder) {
        PropertyMapper map = new PropertyMapper();
        map.from(this.shareServiceClientProperties.getEndpoint()).to(builder::endpoint);
        map.from(this.shareServiceClientProperties.getServiceVersion()).to(builder::serviceVersion);
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
    protected BiConsumer<ShareServiceClientBuilder, RequestRetryOptions> consumeRequestRetryOptions() {
        return ShareServiceClientBuilder::retryOptions;
    }
}
