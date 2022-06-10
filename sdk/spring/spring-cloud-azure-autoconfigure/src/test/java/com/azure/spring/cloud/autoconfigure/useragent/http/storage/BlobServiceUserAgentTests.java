// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent.http.storage;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.cloud.autoconfigure.useragent.util.UserAgentTestUtil;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class BlobServiceUserAgentTests {

    @Test
    public void userAgentTest() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureStorageBlobAutoConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.account-name=sample"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageBlobAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageBlobProperties.class);
                assertThat(context).hasSingleBean(BlobServiceClientBuilderFactory.class);
                assertThat(context).hasSingleBean(BlobServiceClientBuilder.class);
                assertThat(context).hasSingleBean(BlobServiceAsyncClient.class);
                assertThat(context).hasSingleBean(BlobServiceClient.class);

                BlobServiceClient client = context.getBean(BlobServiceClient.class);
                String userAgent = UserAgentTestUtil.getUserAgent(client.getHttpPipeline());
                Assertions.assertNotNull(userAgent);
                Assertions.assertTrue(userAgent.contains(AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB));
            });
    }
}
