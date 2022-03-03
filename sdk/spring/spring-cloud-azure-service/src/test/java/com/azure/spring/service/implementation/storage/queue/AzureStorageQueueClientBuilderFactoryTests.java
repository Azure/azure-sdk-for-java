// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.queue;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.spring.cloud.core.implementation.http.DefaultHttpProvider;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.service.implementation.core.http.TestHttpClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.verification.VerificationMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
class AzureStorageQueueClientBuilderFactoryTests extends AzureHttpClientBuilderFactoryBaseTests<QueueServiceClientBuilder,
    AzureStorageQueueTestProperties, QueueServiceClientBuilderFactory> {

    private static final String ENDPOINT = "https://abc.queue.core.windows.net/";
    private static final String CONNECTION_STRING = "BlobEndpoint=https://test.blob.core.windows.net/;"
        + "QueueEndpoint=https://test.queue.core.windows.net/;FileEndpoint=https://test.file.core.windows.net/;"
        + "TableEndpoint=https://test.table.core.windows.net/;SharedAccessSignature=sv=2020-08-04"
        + "&ss=bfqt&srt=sco&sp=rwdlacupitfx&se=2023-06-08T15:17:21Z&st=2021-12-27T07:17:21Z&sip=192.168.0.1"
        + "&spr=https,http&sig=test";

    @Test
    void clientSecretTokenCredentialConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");

        final QueueServiceClientBuilder builder = new QueueServiceClientBuilderFactoryExt(properties).build();
        final QueueServiceClient client = builder.buildClient();

        verify(builder, times(1)).credential(any(ClientSecretCredential.class));
    }

    @Test
    void clientCertificateTokenCredentialConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");

        final QueueServiceClientBuilder builder = new QueueServiceClientBuilderFactoryExt(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(ClientCertificateCredential.class));
    }

    @Test
    void connectionStringConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        properties.setConnectionString(CONNECTION_STRING);
        final QueueServiceClientBuilder builder = new QueueServiceClientBuilderFactoryExt(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).connectionString(anyString());
    }

    @Test
    void storageSharedKeyCredentialConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        properties.setAccountName("test_account_name");
        properties.setAccountKey("test_account_key");
        final QueueServiceClientBuilder builder = new QueueServiceClientBuilderFactoryExt(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(StorageSharedKeyCredential.class));
    }

    @Test
    void azureSasCredentialConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test");
        final QueueServiceClientBuilder builder = new QueueServiceClientBuilderFactoryExt(properties).build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(AzureSasCredential.class));
    }

    @Test
    void proxyPropertiesConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        ProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");

        final QueueServiceClientBuilderFactoryProxyExt builderFactory = new QueueServiceClientBuilderFactoryProxyExt(properties);
        HttpClientProvider defaultHttpClientProvider = builderFactory.getDefaultHttpClientProvider();
        final QueueServiceClientBuilder builder = builderFactory.build();
        final QueueServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(HttpClient.class));
        verify(defaultHttpClientProvider, times(1)).createInstance(any(HttpClientOptions.class));
    }

    @Test
    void retryOptionsConfigured() {
        AzureStorageQueueTestProperties properties = createMinimalServiceProperties();
        final QueueServiceClientBuilderFactoryExt builderFactory = new QueueServiceClientBuilderFactoryExt(properties);
        final QueueServiceClientBuilder builder = builderFactory.build();
        final QueueServiceClient client = builder.buildClient();
        verify(builder, times(1)).retryOptions(any(RequestRetryOptions.class));
    }

    @Override
    protected AzureStorageQueueTestProperties createMinimalServiceProperties() {
        AzureStorageQueueTestProperties properties = new AzureStorageQueueTestProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    @Override
    protected QueueServiceClientBuilderFactory getClientBuilderFactoryWithMockBuilder(AzureStorageQueueTestProperties properties) {
        return new QueueServiceClientBuilderFactoryExt(properties);
    }

    @Override
    protected void verifyHttpClientCalled(QueueServiceClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyHttpPipelinePolicyAdded(QueueServiceClientBuilder builder, HttpPipelinePolicy policy, VerificationMode mode) {
        verify(builder, mode).addPolicy(policy);
    }

    static class QueueServiceClientBuilderFactoryExt extends QueueServiceClientBuilderFactory {

        QueueServiceClientBuilderFactoryExt(AzureStorageQueueTestProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public QueueServiceClientBuilder createBuilderInstance() {
            return mock(QueueServiceClientBuilder.class);
        }
    }

    static class QueueServiceClientBuilderFactoryProxyExt extends QueueServiceClientBuilderFactoryExt {

        private HttpClientProvider httpClientProvider = mock(DefaultHttpProvider.class);

        QueueServiceClientBuilderFactoryProxyExt(AzureStorageQueueTestProperties blobProperties) {
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

