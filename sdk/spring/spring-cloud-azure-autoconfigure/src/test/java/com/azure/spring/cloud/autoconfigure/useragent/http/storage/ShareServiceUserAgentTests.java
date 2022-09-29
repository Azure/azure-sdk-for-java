// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.useragent.http.storage;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare.properties.AzureStorageFileShareProperties;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.AzureStorageFileShareAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.useragent.util.UserAgentTestUtil;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.storage.fileshare.ShareServiceClientBuilderFactory;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ShareServiceUserAgentTests {

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

                ShareServiceClient client = context.getBean(ShareServiceClient.class);
                String userAgent = UserAgentTestUtil.getUserAgent(client.getHttpPipeline());
                Assertions.assertNotNull(userAgent);
                Assertions.assertTrue(userAgent.contains(AzureSpringIdentifier.AZURE_SPRING_STORAGE_FILES));
            });
    }
}
