// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.user.agent.http;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.AzureStorageBlobAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.service.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;

import static com.azure.spring.core.AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB;
import static org.assertj.core.api.Assertions.assertThat;

public class BlobServiceUserAgentTest {

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

                BlobServiceClient blobServiceClient = context.getBean(BlobServiceClient.class);
                UserAgentPolicy policy = getUserAgentPolicy(blobServiceClient);
                Assertions.assertNotNull(policy);
                Field privateStringField = UserAgentPolicy.class.getDeclaredField("userAgent");
                privateStringField.setAccessible(true);
                String userAgent = (String) privateStringField.get(policy);
                Assertions.assertTrue(userAgent.contains(AZURE_SPRING_STORAGE_BLOB));
            });
    }

    private UserAgentPolicy getUserAgentPolicy(BlobServiceClient client) {
        for (int i = 0; i < client.getHttpPipeline().getPolicyCount(); i++) {
            HttpPipelinePolicy policy = client.getHttpPipeline().getPolicy(i);
            if (policy instanceof UserAgentPolicy) {
                return (UserAgentPolicy)policy;
            }
        }
        return null;
    }
}
