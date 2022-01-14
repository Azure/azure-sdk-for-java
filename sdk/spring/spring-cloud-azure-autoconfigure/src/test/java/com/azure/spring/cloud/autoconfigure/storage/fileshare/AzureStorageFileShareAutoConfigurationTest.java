// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.fileshare.properties.AzureStorageFileShareProperties;
import com.azure.spring.service.implementation.storage.fileshare.ShareServiceClientBuilderFactory;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    @Disabled // TODO (xiada): fix this after token credential is supported in a share service client
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

    @Test
    void shareNameSetShouldConfigureShareClient() {
        ShareServiceClient shareServiceClient = mock(ShareServiceClient.class);
        when(shareServiceClient.getShareClient("share1")).thenReturn(mock(ShareClient.class));
        ShareServiceAsyncClient shareServiceAsyncClient = mock(ShareServiceAsyncClient.class);
        when(shareServiceAsyncClient.getShareAsyncClient("share1")).thenReturn(mock(ShareAsyncClient.class));
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.fileshare.account-name=sa",
                "spring.cloud.azure.storage.fileshare.share-name=share1"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(ShareServiceClient.class, () -> shareServiceClient)
            .withBean(ShareServiceAsyncClient.class, () -> shareServiceAsyncClient)
            .run(context -> {
                assertThat(context).hasSingleBean(ShareClient.class);
                assertThat(context).hasSingleBean(ShareAsyncClient.class);
            });
    }

    @Test
    void shareNameNotSetShouldNotConfigureShareClient() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.fileshare.account-name=sa"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(ShareServiceClient.class, () -> mock(ShareServiceClient.class))
            .withBean(ShareServiceAsyncClient.class, () -> mock(ShareServiceAsyncClient.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ShareClient.class);
                assertThat(context).doesNotHaveBean(ShareAsyncClient.class);
            });
    }

    @Test
    void customizerShouldBeCalled() {
        ShareServiceClientBuilderCustomizer customizer = new ShareServiceClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.fileshare.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        ShareServiceClientBuilderCustomizer customizer = new ShareServiceClientBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.fileshare.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class ShareServiceClientBuilderCustomizer extends TestBuilderCustomizer<ShareServiceClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}
