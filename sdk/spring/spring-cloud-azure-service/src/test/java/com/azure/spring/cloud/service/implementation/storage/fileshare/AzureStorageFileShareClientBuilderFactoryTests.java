// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.fileshare;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.HttpClientOptions;
import com.azure.spring.cloud.service.implementation.AzureHttpClientBuilderFactoryBaseTests;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceVersion;
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
class AzureStorageFileShareClientBuilderFactoryTests extends
    AzureHttpClientBuilderFactoryBaseTests<
        ShareServiceClientBuilder,
        AzureStorageFileShareTestProperties,
        AzureStorageFileShareClientBuilderFactoryTests.ShareServiceClientBuilderFactoryExt> {

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

    @Override
    protected AzureStorageFileShareTestProperties createMinimalServiceProperties() {
        AzureStorageFileShareTestProperties properties = new AzureStorageFileShareTestProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    @Override
    protected ShareServiceClientBuilderFactoryExt createClientBuilderFactoryWithMockBuilder(AzureStorageFileShareTestProperties properties) {
        return new ShareServiceClientBuilderFactoryExt(properties);
    }

    @Override
    protected void verifyHttpClientCalled(ShareServiceClientBuilder builder, VerificationMode mode) {
        verify(builder, mode).httpClient(any(HttpClient.class));
    }

    @Override
    protected HttpClientOptions getHttpClientOptions(ShareServiceClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpClientOptions();
    }

    @Override
    protected List<HttpPipelinePolicy> getHttpPipelinePolicies(ShareServiceClientBuilderFactoryExt builderFactory) {
        return builderFactory.getHttpPipelinePolicies();
    }

    @Override
    protected void buildClient(ShareServiceClientBuilder builder) {
        builder.buildClient();
    }

    @Override
    protected void verifyServicePropertiesConfigured() {
        AzureStorageFileShareTestProperties properties = new AzureStorageFileShareTestProperties();
        properties.setEndpoint(ENDPOINT);
        properties.setServiceVersion(ShareServiceVersion.V2019_02_02);

        final ShareServiceClientBuilder builder = new ShareServiceClientBuilderFactoryExt(properties).build();
        final ShareServiceClient client = builder.buildClient();

        verify(builder, times(1)).endpoint(ENDPOINT);
        verify(builder, times(1)).serviceVersion(ShareServiceVersion.V2019_02_02);
    }

    @Override
    protected void verifyRetryOptionsCalled(ShareServiceClientBuilder builder,
                                            AzureStorageFileShareTestProperties properties, VerificationMode mode) {
        verify(builder, mode).retryOptions(any(RequestRetryOptions.class));
    }

    @Override
    protected void verifyCredentialCalled(ShareServiceClientBuilder builder,
                                          Class<? extends TokenCredential> tokenCredentialClass,
                                          VerificationMode mode) {
        // the file share doesn't support token credential
    }

    static class ShareServiceClientBuilderFactoryExt extends ShareServiceClientBuilderFactory {

        ShareServiceClientBuilderFactoryExt(AzureStorageFileShareTestProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public ShareServiceClientBuilder createBuilderInstance() {
            return mock(ShareServiceClientBuilder.class);
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

