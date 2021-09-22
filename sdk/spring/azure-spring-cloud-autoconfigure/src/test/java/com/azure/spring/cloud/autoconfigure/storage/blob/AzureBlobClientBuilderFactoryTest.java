// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.spring.cloud.autoconfigure.AzureServiceClientBuilderFactoryTestBase;
import com.azure.spring.cloud.autoconfigure.core.TestHttpClient;
import com.azure.spring.cloud.autoconfigure.core.TestHttpClientProvider;
import com.azure.spring.cloud.autoconfigure.core.TestPerCallHttpPipelinePolicy;
import com.azure.spring.cloud.autoconfigure.core.TestPerRetryHttpPipelinePolicy;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Xiaolu Dai, 2021/8/25.
 */
class AzureBlobClientBuilderFactoryTest extends AzureServiceClientBuilderFactoryTestBase<BlobServiceClientBuilder,
                                                                                                AzureStorageBlobProperties, BlobServiceClientBuilderFactory> {

    private static final String ENDPOINT = "https://abc.blob.core.windows.net/";

    @Test
    void testMinimalSettings() {
        AzureStorageBlobProperties properties = createMinimalServiceProperties();

        final BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilderFactory(properties).build();
        final BlobServiceClient client = clientBuilder.buildClient();
    }

    @Test
    void testClientSecretTokenCredentialConfigured() {
        AzureStorageBlobProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientSecret("test-secret");
        properties.getProfile().setTenantId("test-tenant");

        final BlobServiceClientBuilder builder = new BlobServiceClientBuilderFactoryExt(properties).build();
        final BlobServiceClient client = builder.buildClient();

        verify(builder, times(1)).credential(any(ClientSecretCredential.class));
    }

    @Test
    void testClientCertificateTokenCredentialConfigured() {
        AzureStorageBlobProperties properties = createMinimalServiceProperties();

        properties.getCredential().setClientId("test-client");
        properties.getCredential().setClientCertificatePath("test-cert-path");
        properties.getCredential().setClientCertificatePassword("test-cert-password");
        properties.getProfile().setTenantId("test-tenant");

        final BlobServiceClientBuilder builder = new BlobServiceClientBuilderFactoryExt(properties).build();
        verify(builder, times(1)).credential(any(ClientCertificateCredential.class));
    }

    @Test
    void testHttpClientConfigured() {
        AzureStorageBlobProperties properties = createMinimalServiceProperties();

        final BlobServiceClientBuilderFactory builderFactory = new BlobServiceClientBuilderFactoryExt(properties);

        builderFactory.setHttpClientProvider(new TestHttpClientProvider());

        final BlobServiceClientBuilder builder = builderFactory.build();
        final BlobServiceClient client = builder.buildClient();

        verify(builder).httpClient(any(TestHttpClient.class));
    }

    @Test
    void testDefaultHttpPipelinePoliciesConfigured() {
        AzureStorageBlobProperties properties = createMinimalServiceProperties();

        final BlobServiceClientBuilderFactory builderFactory = new BlobServiceClientBuilderFactoryExt(properties);

        builderFactory.addHttpPipelinePolicy(new TestPerCallHttpPipelinePolicy());
        builderFactory.addHttpPipelinePolicy(new TestPerRetryHttpPipelinePolicy());


        final BlobServiceClientBuilder builder = builderFactory.build();
        final BlobServiceClient client = builder.buildClient();

        verify(builder, times(1)).addPolicy(any(TestPerCallHttpPipelinePolicy.class));
        verify(builder, times(1)).addPolicy(any(TestPerRetryHttpPipelinePolicy.class));
    }

    @Override
    protected AzureStorageBlobProperties createMinimalServiceProperties() {
        AzureStorageBlobProperties properties = new AzureStorageBlobProperties();
        properties.setEndpoint(ENDPOINT);
        return properties;
    }

    static class BlobServiceClientBuilderFactoryExt extends BlobServiceClientBuilderFactory {

        BlobServiceClientBuilderFactoryExt(AzureStorageBlobProperties blobProperties) {
            super(blobProperties);
        }

        @Override
        public BlobServiceClientBuilder createBuilderInstance() {
            return mock(BlobServiceClientBuilder.class);
        }
    }

}

