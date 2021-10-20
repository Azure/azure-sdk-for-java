// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

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
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
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
class AzureStorageFileShareClientBuilderFactoryTest extends AzureServiceClientBuilderFactoryTestBase<ShareServiceClientBuilder,
        AzureStorageFileShareProperties, ShareServiceClientBuilderFactory> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureStorageFileShareClientBuilderFactoryTest.class);
    private static final String ENDPOINT = "https://abc.file.core.windows.net/";

    @Test
    void testStorageSharedKeyCredentialConfigured() {
        AzureStorageFileShareProperties properties = createMinimalServiceProperties();
        properties.setAccountName("test_account_name");
        properties.setAccountKey("test_account_key");
        final ShareServiceClientBuilder builder = new ShareServiceClientBuilderFactoryExt(properties).build();
        final ShareServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(StorageSharedKeyCredential.class));
    }

    @Test
    void testAzureSasCredentialConfigured() {
        AzureStorageFileShareProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test");
        final ShareServiceClientBuilder builder = new ShareServiceClientBuilderFactoryExt(properties).build();
        final ShareServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(AzureSasCredential.class));
    }

    @Test
    void testHttpClientConfigured() {
        AzureStorageFileShareProperties properties = createMinimalServiceProperties();

        final ShareServiceClientBuilderFactory builderFactory = new ShareServiceClientBuilderFactoryExt(properties);

        builderFactory.setHttpClientProvider(new TestHttpClientProvider());

        final ShareServiceClientBuilder builder = builderFactory.build();
        final ShareServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(TestHttpClient.class));
    }

    @Test
    void testDefaultHttpPipelinePoliciesConfigured() {
        AzureStorageFileShareProperties properties = createMinimalServiceProperties();

        final ShareServiceClientBuilderFactory builderFactory = new ShareServiceClientBuilderFactoryExt(properties);

        builderFactory.addHttpPipelinePolicy(new TestPerCallHttpPipelinePolicy());
        builderFactory.addHttpPipelinePolicy(new TestPerRetryHttpPipelinePolicy());


        final ShareServiceClientBuilder builder = builderFactory.build();
        final ShareServiceClient client = builder.buildClient();

        verify(builder, times(1)).addPolicy(any(TestPerCallHttpPipelinePolicy.class));
        verify(builder, times(1)).addPolicy(any(TestPerRetryHttpPipelinePolicy.class));
    }

    @Test
    void testProxyPropertiesConfigured() {
        AzureStorageFileShareProperties properties = createMinimalServiceProperties();
        ProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);

        final ShareServiceClientBuilderFactoryProxyExt builderFactory = new ShareServiceClientBuilderFactoryProxyExt(properties);
        HttpClientProvider defaultHttpClientProvider = builderFactory.getDefaultHttpClientProvider();
        final ShareServiceClientBuilder builder = builderFactory.build();
        final ShareServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(HttpClient.class));
        verify(defaultHttpClientProvider, times(1)).createInstance(any(HttpClientOptions.class));
    }

    @Override
    protected AzureStorageFileShareProperties createMinimalServiceProperties() {
        AzureStorageFileShareProperties properties = new AzureStorageFileShareProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    static class ShareServiceClientBuilderFactoryExt extends ShareServiceClientBuilderFactory {

        ShareServiceClientBuilderFactoryExt(AzureStorageFileShareProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public ShareServiceClientBuilder createBuilderInstance() {
            return mock(ShareServiceClientBuilder.class);
        }
    }

    static class ShareServiceClientBuilderFactoryProxyExt extends ShareServiceClientBuilderFactoryExt {

        private HttpClientProvider httpClientProvider = mock(DefaultHttpProvider.class);

        ShareServiceClientBuilderFactoryProxyExt(AzureStorageFileShareProperties blobProperties) {
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

