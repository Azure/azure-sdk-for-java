// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.spring.service.AzureServiceClientBuilderFactoryTestBase;
import com.azure.spring.service.core.http.TestHttpClient;
import com.azure.spring.service.core.http.TestHttpClientProvider;
import com.azure.spring.service.core.http.TestPerCallHttpPipelinePolicy;
import com.azure.spring.service.core.http.TestPerRetryHttpPipelinePolicy;
import com.azure.spring.core.http.DefaultHttpProvider;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
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
class AzureBlobClientBuilderFactoryTest extends AzureServiceClientBuilderFactoryTestBase<BlobServiceClientBuilder,
    TestAzureStorageBlobProperties, BlobServiceClientBuilderFactory> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureBlobClientBuilderFactoryTest.class);
    private static final String ENDPOINT = "https://abc.blob.core.windows.net/";

    @Test
    void testMinimalSettings() {
        TestAzureStorageBlobProperties properties = createMinimalServiceProperties();

        final BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilderFactory(properties).build();
        final BlobServiceClient client = clientBuilder.buildClient();
    }

    @Test
    void testClientSecretTokenCredentialConfigured() {
        TestAzureStorageBlobProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");

        final BlobServiceClientBuilder builder = new BlobServiceClientBuilderFactoryExt(properties).build();
        final BlobServiceClient client = builder.buildClient();

        verify(builder, times(1)).credential(any(ClientSecretCredential.class));
    }

    @Test
    void testClientCertificateTokenCredentialConfigured() {
        TestAzureStorageBlobProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");

        final BlobServiceClientBuilder builder = new BlobServiceClientBuilderFactoryExt(properties).build();
        verify(builder, times(1)).credential(any(ClientCertificateCredential.class));
    }

    @Test
    void testHttpClientConfigured() {
        TestAzureStorageBlobProperties properties = createMinimalServiceProperties();

        final BlobServiceClientBuilderFactory builderFactory = new BlobServiceClientBuilderFactoryExt(properties);

        builderFactory.setHttpClientProvider(new TestHttpClientProvider());

        final BlobServiceClientBuilder builder = builderFactory.build();
        final BlobServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(TestHttpClient.class));
    }

    @Test
    void testDefaultHttpPipelinePoliciesConfigured() {
        TestAzureStorageBlobProperties properties = createMinimalServiceProperties();

        final BlobServiceClientBuilderFactory builderFactory = new BlobServiceClientBuilderFactoryExt(properties);

        builderFactory.addHttpPipelinePolicy(new TestPerCallHttpPipelinePolicy());
        builderFactory.addHttpPipelinePolicy(new TestPerRetryHttpPipelinePolicy());


        final BlobServiceClientBuilder builder = builderFactory.build();
        final BlobServiceClient client = builder.buildClient();

        verify(builder, times(1)).addPolicy(any(TestPerCallHttpPipelinePolicy.class));
        verify(builder, times(1)).addPolicy(any(TestPerRetryHttpPipelinePolicy.class));
    }

    @Test
    void testProxyPropertiesConfigured() {
        TestAzureStorageBlobProperties properties = createMinimalServiceProperties();
        ProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);

        final BlobServiceClientBuilderFactoryProxyExt builderFactory = new BlobServiceClientBuilderFactoryProxyExt(properties);
        HttpClientProvider defaultHttpClientProvider = builderFactory.getDefaultHttpClientProvider();
        final BlobServiceClientBuilder builder = builderFactory.build();
        final BlobServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(HttpClient.class));
        verify(defaultHttpClientProvider, times(1)).createInstance(any(HttpClientOptions.class));
    }

    @Override
    protected TestAzureStorageBlobProperties createMinimalServiceProperties() {
        TestAzureStorageBlobProperties properties = new TestAzureStorageBlobProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    static class BlobServiceClientBuilderFactoryExt extends BlobServiceClientBuilderFactory {

        BlobServiceClientBuilderFactoryExt(TestAzureStorageBlobProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public BlobServiceClientBuilder createBuilderInstance() {
            return mock(BlobServiceClientBuilder.class);
        }
    }

    static class BlobServiceClientBuilderFactoryProxyExt extends BlobServiceClientBuilderFactoryExt {

        private HttpClientProvider httpClientProvider = mock(DefaultHttpProvider.class);

        BlobServiceClientBuilderFactoryProxyExt(TestAzureStorageBlobProperties blobProperties) {
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

