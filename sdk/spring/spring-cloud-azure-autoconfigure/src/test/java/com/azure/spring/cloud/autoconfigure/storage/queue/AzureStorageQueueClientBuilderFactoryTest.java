// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.queue;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.cloud.autoconfigure.AzureServiceClientBuilderFactoryTestBase;
import com.azure.spring.cloud.autoconfigure.core.TestHttpClient;
import com.azure.spring.cloud.autoconfigure.core.TestHttpClientProvider;
import com.azure.spring.cloud.autoconfigure.core.TestPerCallHttpPipelinePolicy;
import com.azure.spring.cloud.autoconfigure.core.TestPerRetryHttpPipelinePolicy;
import com.azure.spring.core.http.DefaultHttpProvider;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Xiaolu Dai, 2021/8/25.
 */
class AzureStorageQueueClientBuilderFactoryTest extends AzureServiceClientBuilderFactoryTestBase<QueueServiceClientBuilder,
        AzureStorageQueueProperties, QueueServiceClientBuilderFactory> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageQueueClientBuilderFactoryTest.class);
    private static final String ENDPOINT = "https://abc.queue.core.windows.net/";

    @Test
    void testStorageSharedKeyCredentialConfigured() {
        AzureStorageQueueProperties properties = createMinimalServiceProperties();
        properties.setAccountName("test_account_name");
        properties.setAccountKey("test_account_key");
        final QueueServiceClientBuilder builder = new QueueServiceClientBuilderFactoryExt(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(StorageSharedKeyCredential.class));
    }

    @Test
    void testAzureSasCredentialConfigured() {
        AzureStorageQueueProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test");
        final QueueServiceClientBuilder builder = new QueueServiceClientBuilderFactoryExt(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(AzureSasCredential.class));
    }

    @Test
    void testHttpClientConfigured() {
        AzureStorageQueueProperties properties = createMinimalServiceProperties();

        final QueueServiceClientBuilderFactory builderFactory = new QueueServiceClientBuilderFactoryExt(properties);

        builderFactory.setHttpClientProvider(new TestHttpClientProvider());

        final QueueServiceClientBuilder builder = builderFactory.build();
        final QueueServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(TestHttpClient.class));
    }

    @Test
    void testDefaultHttpPipelinePoliciesConfigured() {
        AzureStorageQueueProperties properties = createMinimalServiceProperties();

        final QueueServiceClientBuilderFactory builderFactory = new QueueServiceClientBuilderFactoryExt(properties);

        builderFactory.addHttpPipelinePolicy(new TestPerCallHttpPipelinePolicy());
        builderFactory.addHttpPipelinePolicy(new TestPerRetryHttpPipelinePolicy());


        final QueueServiceClientBuilder builder = builderFactory.build();
        final QueueServiceClient client = builder.buildClient();

        verify(builder, times(1)).addPolicy(any(TestPerCallHttpPipelinePolicy.class));
        verify(builder, times(1)).addPolicy(any(TestPerRetryHttpPipelinePolicy.class));
    }

    @Test
    void testProxyPropertiesConfigured() {
        AzureStorageQueueProperties properties = createMinimalServiceProperties();
        ProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);

        final QueueServiceClientBuilderFactoryProxyExt builderFactory = new QueueServiceClientBuilderFactoryProxyExt(properties);
        HttpClientProvider defaultHttpClientProvider = builderFactory.getDefaultHttpClientProvider();
        final QueueServiceClientBuilder builder = builderFactory.build();
        final QueueServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(HttpClient.class));
        verify(defaultHttpClientProvider, times(1)).createInstance(any(HttpClientOptions.class));
    }

    @Override
    protected AzureStorageQueueProperties createMinimalServiceProperties() {
        AzureStorageQueueProperties properties = new AzureStorageQueueProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    static class QueueServiceClientBuilderFactoryExt extends QueueServiceClientBuilderFactory {

        QueueServiceClientBuilderFactoryExt(AzureStorageQueueProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public QueueServiceClientBuilder createBuilderInstance() {
            return mock(QueueServiceClientBuilder.class);
        }
    }

    static class QueueServiceClientBuilderFactoryProxyExt extends QueueServiceClientBuilderFactoryExt {

        private HttpClientProvider httpClientProvider = mock(DefaultHttpProvider.class);

        QueueServiceClientBuilderFactoryProxyExt(AzureStorageQueueProperties blobProperties) {
            super(blobProperties);

            HttpClient httpClient = mock(HttpClient.class);
            when(this.httpClientProvider.createInstance(any(HttpClientOptions.class))).thenReturn(httpClient);
        }

        @Override
        protected HttpClientProvider getHttpClientProvider() {
            return httpClientProvider;
        }

        public HttpClientProvider getDefaultHttpClientProvider() {
            return getHttpClientProvider();
        }
    }
}

