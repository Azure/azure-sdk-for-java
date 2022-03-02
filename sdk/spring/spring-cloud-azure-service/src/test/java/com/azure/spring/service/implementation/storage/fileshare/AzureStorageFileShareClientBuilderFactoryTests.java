// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.fileshare;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.core.implementation.http.DefaultHttpProvider;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.spring.service.implementation.core.http.TestHttpClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
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
class AzureStorageFileShareClientBuilderFactoryTests extends AzureHttpClientBuilderFactoryBaseTests<ShareServiceClientBuilder,
    AzureStorageFileShareTestProperties, ShareServiceClientBuilderFactory> {

    private static final String ENDPOINT = "https://abc.file.core.windows.net/";
    private static final String CONNECTION_STRING = "BlobEndpoint=https://test.blob.core.windows.net/;"
        + "QueueEndpoint=https://test.queue.core.windows.net/;FileEndpoint=https://test.file.core.windows.net/;"
        + "TableEndpoint=https://test.table.core.windows.net/;SharedAccessSignature=sv=2020-08-04"
        + "&ss=bfqt&srt=sco&sp=rwdlacupitfx&se=2023-06-08T15:17:21Z&st=2021-12-27T07:17:21Z&sip=192.168.0.1"
        + "&spr=https,http&sig=test";

    @Test
    void connectionStringConfigured() {
        AzureStorageFileShareTestProperties properties = createMinimalServiceProperties();
        properties.setConnectionString(CONNECTION_STRING);
        final ShareServiceClientBuilder builder = new ShareServiceClientBuilderFactoryExt(properties).build();
        final ShareServiceClient client = builder.buildClient();
        verify(builder, times(1)).connectionString(anyString());
    }

    @Test
    void storageSharedKeyCredentialConfigured() {
        AzureStorageFileShareTestProperties properties = createMinimalServiceProperties();
        properties.setAccountName("test_account_name");
        properties.setAccountKey("test_account_key");
        final ShareServiceClientBuilder builder = new ShareServiceClientBuilderFactoryExt(properties).build();
        final ShareServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(StorageSharedKeyCredential.class));
    }

    @Test
    void azureSasCredentialConfigured() {
        AzureStorageFileShareTestProperties properties = createMinimalServiceProperties();
        properties.setSasToken("test");
        final ShareServiceClientBuilder builder = new ShareServiceClientBuilderFactoryExt(properties).build();
        final ShareServiceClient client = builder.buildClient();
        verify(builder, times(1)).credential(any(AzureSasCredential.class));
    }

    @Test
    void proxyPropertiesConfigured() {
        AzureStorageFileShareTestProperties properties = createMinimalServiceProperties();
        ProxyProperties proxyProperties = properties.getProxy();
        proxyProperties.setHostname("localhost");
        proxyProperties.setPort(8080);
        proxyProperties.setType("http");

        final ShareServiceClientBuilderFactoryProxyExt builderFactory =
            new ShareServiceClientBuilderFactoryProxyExt(properties);
        HttpClientProvider defaultHttpClientProvider = builderFactory.getDefaultHttpClientProvider();
        final ShareServiceClientBuilder builder = builderFactory.build();
        final ShareServiceClient client = builder.buildClient();

        verify(builder, times(1)).httpClient(any(HttpClient.class));
        verify(defaultHttpClientProvider, times(1)).createInstance(any(HttpClientOptions.class));
    }

    @Test
    void retryOptionsConfigured() {
        AzureStorageFileShareTestProperties properties = createMinimalServiceProperties();
        final ShareServiceClientBuilderFactoryExt builderFactory = new ShareServiceClientBuilderFactoryExt(properties);
        final ShareServiceClientBuilder builder = builderFactory.build();
        final ShareServiceClient client = builder.buildClient();
        verify(builder, times(1)).retryOptions(any(RequestRetryOptions.class));
    }

    @Override
    protected AzureStorageFileShareTestProperties createMinimalServiceProperties() {
        AzureStorageFileShareTestProperties properties = new AzureStorageFileShareTestProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    @Override
    protected ShareServiceClientBuilderFactory getClientBuilderFactoryWithMockBuilder(AzureStorageFileShareTestProperties properties) {
        return new ShareServiceClientBuilderFactoryExt(properties);
    }

    @Override
    protected void verifyHttpClientCalled(ShareServiceClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(TestHttpClient.class));
    }

    @Override
    protected void verifyHttpPipelinePolicyAdded(ShareServiceClientBuilder builder, HttpPipelinePolicy policy, VerificationMode mode) {
        verify(builder, mode).addPolicy(policy);
    }

    static class ShareServiceClientBuilderFactoryExt extends ShareServiceClientBuilderFactory {

        ShareServiceClientBuilderFactoryExt(AzureStorageFileShareTestProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public ShareServiceClientBuilder createBuilderInstance() {
            return mock(ShareServiceClientBuilder.class);
        }
    }

    static class ShareServiceClientBuilderFactoryProxyExt extends ShareServiceClientBuilderFactoryExt {

        private final HttpClientProvider httpClientProvider = mock(DefaultHttpProvider.class);

        ShareServiceClientBuilderFactoryProxyExt(AzureStorageFileShareTestProperties blobProperties) {
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

