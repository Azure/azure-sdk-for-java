// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureStorageFileShareAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageFileShareAutoConfiguration.class));

    @Test
    void configureWithoutShareServiceClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ShareServiceClientBuilder.class))
            .withPropertyValues("spring.cloud.azure.storage.fileshare.account-name=sa")
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageFileShareAutoConfiguration.class));
    }

    @Test
    void configureWithStorageFileShareDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.fileshare.enabled=false",
                "spring.cloud.azure.storage.fileshare.account-name=sa"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageFileShareAutoConfiguration.class));
    }

    @Test
    @Disabled // TODO (xiada): fix this after default token credential could be set
    void accountNameSetShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.fileshare.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageFileShareAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageFileShareProperties.class);
                assertThat(context).hasSingleBean(ShareServiceClient.class);
                assertThat(context).hasSingleBean(ShareServiceAsyncClient.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilder.class);
                assertThat(context).hasSingleBean(ShareServiceClientBuilderFactory.class);
            });
    }
    
}
