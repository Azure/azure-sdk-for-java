// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.blob;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.storage.blob.properties.AzureStorageBlobProperties;
import com.azure.spring.service.storage.blob.BlobServiceClientBuilderFactory;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureStorageBlobAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageBlobAutoConfiguration.class));

    @Test
    void configureWithoutBlobServiceClientBuilder() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(BlobServiceClientBuilder.class))
            .withPropertyValues("spring.cloud.azure.storage.blob.account-name=sa")
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageBlobAutoConfiguration.class));
    }

    @Test
    void configureWithStorageBlobDisabled() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.enabled=false",
                "spring.cloud.azure.storage.blob.account-name=sa"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageBlobAutoConfiguration.class));
    }

    @Test
    void accountNameSetShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.blob.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageBlobAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureStorageBlobProperties.class);
                assertThat(context).hasSingleBean(BlobServiceClient.class);
                assertThat(context).hasSingleBean(BlobServiceAsyncClient.class);
                assertThat(context).hasSingleBean(BlobServiceClientBuilder.class);
                assertThat(context).hasSingleBean(BlobServiceClientBuilderFactory.class);
            });
    }

    @Test
    void containerNameSetShouldConfigureContainerClient() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.account-name=sa",
                "spring.cloud.azure.storage.blob.container-name=container1"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(BlobContainerClient.class);
                assertThat(context).hasSingleBean(BlobContainerAsyncClient.class);
            });
    }

    @Test
    void containerNameNotSetShouldNotConfigureContainerClient() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.blob.account-name=sa"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).doesNotHaveBean(BlobContainerClient.class);
                assertThat(context).doesNotHaveBean(BlobContainerAsyncClient.class);
            });
    }

    @Test
    void customizerShouldBeCalled() {
        BlobServiceClientBuilderCustomizer customizer = new BlobServiceClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.blob.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", BlobServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", BlobServiceClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @Test
    void otherCustomizerShouldNotBeCalled() {
        BlobServiceClientBuilderCustomizer customizer = new BlobServiceClientBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.storage.blob.account-name=sa")
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", BlobServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", BlobServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    private static class BlobServiceClientBuilderCustomizer extends TestBuilderCustomizer<BlobServiceClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}
