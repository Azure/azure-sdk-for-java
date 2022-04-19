// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.queue;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.cloud.service.implementation.core.http.TestHttpClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 */
class AzureStorageQueueClientBuilderFactoryTests extends
    AzureHttpClientBuilderFactoryBaseTests<
        QueueServiceClientBuilder,
        AzureStorageQueueTestProperties,
        AzureStorageQueueClientBuilderFactoryTests.QueueServiceClientBuilderFactoryExt> {

    private static final String ENDPOINT = "https://abc.queue.core.windows.net/";
    private static final String CONNECTION_STRING = "BlobEndpoint=https://test.blob.core.windows.net/;"
        + "QueueEndpoint=https://test.queue.core.windows.net/;FileEndpoint=https://test.file.core.windows.net/;"
        + "TableEndpoint=https://test.table.core.windows.net/;SharedAccessSignature=sv=2020-08-04"
        + "&ss=bfqt&srt=sco&sp=rwdlacupitfx&se=2023-06-08T15:17:21Z&st=2021-12-27T07:17:21Z&sip=192.168.0.1"
        + "&spr=https,http&sig=test";

    @Test
    void connectionStringConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        properties.setConnectionString(CONNECTION_STRING);
        final QueueServiceClientBuilder builder = createClientBuilderFactoryWithMockBuilder(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).connectionString(anyString());
    }

    @Test
    void storageSharedKeyCredentialConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        properties.setAccountName("test_account_name");
        properties.setAccountKey("test_account_key");
        final QueueServiceClientBuilder builder = createClientBuilderFactoryWithMockBuilder(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(StorageSharedKeyCredential.class));
    }

    @Test
    void azureSasCredentialConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test");
        final QueueServiceClientBuilder builder = createClientBuilderFactoryWithMockBuilder(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(AzureSasCredential.class));
    }


    @Override
    protected AzureStorageQueueTestProperties createMinimalServiceProperties() {
        AzureStorageQueueTestProperties properties = new AzureStorageQueueTestProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    @Override
    protected QueueServiceClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(
        AzureStorageQueueTestProperties properties) {
        return new QueueServiceClientBuilderFactoryExt(properties);
    }

    @Override
    protected void buildClient(QueueServiceClientBuilder builder) {
        builder.buildClient();
    }

    @Override
    protected void verifyRetryOptionsCalled(QueueServiceClientBuilder builder,
                                            AzureStorageQueueTestProperties properties,
                                            VerificationMode mode) {
        verify(builder, mode).retryOptions(any(RequestRetryOptions.class));
    }

    @Override
    protected void verifyCredentialCalled(QueueServiceClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        verify(builder, mode).credential(any(tokenCredentialClass));
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(QueueServiceClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(QueueServiceClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }

    @Override
    protected void verifyHttpClientCalled(QueueServiceClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    static class QueueServiceClientBuilderFactoryExt extends QueueServiceClientBuilderFactory {

        QueueServiceClientBuilderFactoryExt(AzureStorageQueueTestProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public QueueServiceClientBuilder createBuilderInstance() {
            return mock(QueueServiceClientBuilder.class);
        }

        @Override
        public HttpClientOptions getHttpClientOptions() {
            return super.getHttpClientOptions();
        }

        @Override
        public List<HttpPipelinePolicy> getHttpPipelinePolicies() {
            return super.getHttpPipelinePolicies();
        }
    }

}

