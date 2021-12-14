// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.user.agent.http.storage;

import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.properties.AzureStorageFileShareProperties;
import com.azure.spring.service.storage.fileshare.ShareServiceClientBuilderFactory;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;

import static com.azure.spring.core.AzureSpringIdentifier.AZURE_SPRING_STORAGE_FILES;
import static org.assertj.core.api.Assertions.assertThat;

public class ShareServiceUserAgentTest {

    @Disabled // TODO (xiada): fix this after token credential is supported in a share service client
    @Test
    public void userAgentTest() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureStorageFileShareAutoConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.storage.fileshare.account-name=sample"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageFileShareAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageFileShareProperties.class);
                assertThat(context).hasSingleBean(ShareServiceClient.class);
                assertThat(context).hasSingleBean(ShareServiceAsyncClient.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilder.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilderFactory.class);

                String userAgent = getUserAgent(context.getBean(ShareServiceClient.class));
                Assertions.assertNotNull(userAgent);
                Assertions.assertTrue(userAgent.contains(AZURE_SPRING_STORAGE_FILES));
            });
    }

    private String getUserAgent(ShareServiceClient client) {
        UserAgentPolicy policy = getUserAgentPolicy(client);
        Assertions.assertNotNull(policy);
        Field privateStringField;
        try {
            privateStringField = UserAgentPolicy.class.getDeclaredField("userAgent");
            privateStringField.setAccessible(true);
            return (String) privateStringField.get(policy);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    private UserAgentPolicy getUserAgentPolicy(ShareServiceClient client) {
        for (int i = 0; i < client.getHttpPipeline().getPolicyCount(); i++) {
            HttpPipelinePolicy policy = client.getHttpPipeline().getPolicy(i);
            if (policy instanceof UserAgentPolicy) {
                return (UserAgentPolicy) policy;
            }
        }
        return null;
    }
}
