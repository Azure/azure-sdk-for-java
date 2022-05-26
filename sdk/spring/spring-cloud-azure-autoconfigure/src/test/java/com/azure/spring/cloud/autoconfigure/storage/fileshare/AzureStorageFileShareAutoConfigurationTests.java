// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.fileshare;

import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.spring.cloud.autoconfigure.TestBuilderCustomizer;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.fileshare.properties.AzureStorageFileShareProperties;
import com.azure.spring.cloud.service.implementation.storage.fileshare.ShareServiceClientBuilderFactory;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryAsyncClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceVersion;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureStorageFileShareAutoConfigurationTests {

    private static final String STORAGE_CONNECTION_STRING_PATTERN = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageFileShareAutoConfiguration.class));

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void configureWithoutShareServiceClientBuilder(String property) {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ShareServiceClientBuilder.class))
            .withPropertyValues(property)
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageFileShareAutoConfiguration.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void configureWithStorageFileShareDisabled(String property) {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.fileshare.enabled=false",
                property
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

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void shareNameSetShouldConfigureShareClient(String property) {
        ShareServiceClient shareServiceClient = mock(ShareServiceClient.class);
        when(shareServiceClient.getShareClient("share1")).thenReturn(mock(ShareClient.class));
        ShareServiceAsyncClient shareServiceAsyncClient = mock(ShareServiceAsyncClient.class);
        when(shareServiceAsyncClient.getShareAsyncClient("share1")).thenReturn(mock(ShareAsyncClient.class));
        this.contextRunner
            .withPropertyValues(
                property,
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

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void shareNameNotSetShouldNotConfigureShareClient(String property) {
        this.contextRunner
            .withPropertyValues(property)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(ShareServiceClient.class, () -> mock(ShareServiceClient.class))
            .withBean(ShareServiceAsyncClient.class, () -> mock(ShareServiceAsyncClient.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ShareClient.class);
                assertThat(context).doesNotHaveBean(ShareAsyncClient.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void filePathSetShouldConfigureFileClient(String property) {
        ShareServiceClient shareServiceClient = mock(ShareServiceClient.class);
        ShareClient shareClient = mock(ShareClient.class);
        when(shareServiceClient.getShareClient("share1")).thenReturn(shareClient);
        when(shareClient.getFileClient("directory1/file1")).thenReturn(mock(ShareFileClient.class));

        ShareServiceAsyncClient shareServiceAsyncClient = mock(ShareServiceAsyncClient.class);
        ShareAsyncClient shareAsyncClient = mock(ShareAsyncClient.class);
        when(shareServiceAsyncClient.getShareAsyncClient("share1")).thenReturn(shareAsyncClient);
        when(shareAsyncClient.getFileClient("directory1/file1")).thenReturn(mock(ShareFileAsyncClient.class));
        this.contextRunner
            .withPropertyValues(
                property,
                "spring.cloud.azure.storage.fileshare.share-name=share1",
                "spring.cloud.azure.storage.fileshare.file-path=directory1/file1"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(ShareServiceClient.class, () -> shareServiceClient)
            .withBean(ShareServiceAsyncClient.class, () -> shareServiceAsyncClient)
            .run(context -> {
                assertThat(context).hasSingleBean(ShareFileClient.class);
                assertThat(context).hasSingleBean(ShareFileAsyncClient.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void filePathNotSetShouldNotConfigureFileClient(String property) {
        ShareServiceClient shareServiceClient = mock(ShareServiceClient.class);
        when(shareServiceClient.getShareClient("share1")).thenReturn(mock(ShareClient.class));

        ShareServiceAsyncClient shareServiceAsyncClient = mock(ShareServiceAsyncClient.class);
        when(shareServiceAsyncClient.getShareAsyncClient("share1")).thenReturn(mock(ShareAsyncClient.class));

        this.contextRunner
            .withPropertyValues(
                property,
                "spring.cloud.azure.storage.fileshare.share-name=share1"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(ShareServiceClient.class, () -> mock(ShareServiceClient.class))
            .withBean(ShareServiceAsyncClient.class, () -> mock(ShareServiceAsyncClient.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ShareFileClient.class);
                assertThat(context).doesNotHaveBean(ShareFileAsyncClient.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void directoryPathSetShouldConfigureDirectoryClient(String property) {
        ShareServiceClient shareServiceClient = mock(ShareServiceClient.class);
        ShareClient shareClient = mock(ShareClient.class);
        when(shareServiceClient.getShareClient("share1")).thenReturn(shareClient);
        when(shareClient.getDirectoryClient("directory1/directory2")).thenReturn(mock(ShareDirectoryClient.class));

        ShareServiceAsyncClient shareServiceAsyncClient = mock(ShareServiceAsyncClient.class);
        ShareAsyncClient shareAsyncClient = mock(ShareAsyncClient.class);
        when(shareServiceAsyncClient.getShareAsyncClient("share1")).thenReturn(shareAsyncClient);
        when(shareAsyncClient.getDirectoryClient("directory1/directory2")).thenReturn(mock(ShareDirectoryAsyncClient.class));
        this.contextRunner
            .withPropertyValues(
                property,
                "spring.cloud.azure.storage.fileshare.share-name=share1",
                "spring.cloud.azure.storage.fileshare.directory-path=directory1/directory2"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(ShareServiceClient.class, () -> shareServiceClient)
            .withBean(ShareServiceAsyncClient.class, () -> shareServiceAsyncClient)
            .run(context -> {
                assertThat(context).hasSingleBean(ShareDirectoryClient.class);
                assertThat(context).hasSingleBean(ShareDirectoryAsyncClient.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void directoryNameNotSetShouldNotConfigureDirectoryClient(String property) {
        ShareServiceClient shareServiceClient = mock(ShareServiceClient.class);
        when(shareServiceClient.getShareClient("share1")).thenReturn(mock(ShareClient.class));

        ShareServiceAsyncClient shareServiceAsyncClient = mock(ShareServiceAsyncClient.class);
        when(shareServiceAsyncClient.getShareAsyncClient("share1")).thenReturn(mock(ShareAsyncClient.class));

        this.contextRunner
            .withPropertyValues(
                property,
                "spring.cloud.azure.storage.fileshare.share-name=share1"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(ShareServiceClient.class, () -> mock(ShareServiceClient.class))
            .withBean(ShareServiceAsyncClient.class, () -> mock(ShareServiceAsyncClient.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ShareDirectoryClient.class);
                assertThat(context).doesNotHaveBean(ShareDirectoryAsyncClient.class);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void customizerShouldBeCalled(String property) {
        ShareServiceClientBuilderCustomizer customizer = new ShareServiceClientBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(property)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .run(context -> assertThat(customizer.getCustomizedTimes()).isEqualTo(2));
    }

    @ParameterizedTest
    @ValueSource(strings = { "spring.cloud.azure.storage.fileshare.account-name=test-account-name", "spring.cloud.azure.storage.account-name=test-account-name" })
    void otherCustomizerShouldNotBeCalled(String property) {
        ShareServiceClientBuilderCustomizer customizer = new ShareServiceClientBuilderCustomizer();
        OtherBuilderCustomizer otherBuilderCustomizer = new OtherBuilderCustomizer();
        this.contextRunner
            .withPropertyValues(property)
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean("customizer1", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer2", ShareServiceClientBuilderCustomizer.class, () -> customizer)
            .withBean("customizer3", OtherBuilderCustomizer.class, () -> otherBuilderCustomizer)
            .run(context -> {
                assertThat(customizer.getCustomizedTimes()).isEqualTo(2);
                assertThat(otherBuilderCustomizer.getCustomizedTimes()).isEqualTo(0);
            });
    }

    @Test
    void configurationPropertiesShouldBind() {
        String accountName = "test-account-name";
        String connectionString = String.format(STORAGE_CONNECTION_STRING_PATTERN, accountName, "test-key");
        String endpoint = String.format("https://%s.file.core.windows.net", accountName);
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.fileshare.endpoint=" + endpoint,
                "spring.cloud.azure.storage.fileshare.account-key=test-key",
                "spring.cloud.azure.storage.fileshare.sas-token=test-sas-token",
                "spring.cloud.azure.storage.fileshare.connection-string=" + connectionString,
                "spring.cloud.azure.storage.fileshare.account-name=test-account-name",
                "spring.cloud.azure.storage.fileshare.service-version=V2019_02_02",
                "spring.cloud.azure.storage.fileshare.share-name=test-share",
                "spring.cloud.azure.storage.fileshare.file-path=test-file-path",
                "spring.cloud.azure.storage.fileshare.directory-path=test-directory-path"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageFileShareProperties.class);
                AzureStorageFileShareProperties properties = context.getBean(AzureStorageFileShareProperties.class);
                assertEquals(endpoint, properties.getEndpoint());
                assertEquals("test-key", properties.getAccountKey());
                assertEquals("test-sas-token", properties.getSasToken());
                assertEquals(connectionString, properties.getConnectionString());
                assertEquals(accountName, properties.getAccountName());
                assertEquals(ShareServiceVersion.V2019_02_02, properties.getServiceVersion());
                assertEquals("test-share", properties.getShareName());
                assertEquals("test-file-path", properties.getFilePath());
                assertEquals("test-directory-path", properties.getDirectoryPath());
            });
    }

    @Test
    void configurationPropertiesShouldBindWithGlobalConfig() {
        String accountName = "test-account-name";
        String connectionString = String.format(STORAGE_CONNECTION_STRING_PATTERN, accountName, "test-key");
        String endpoint = String.format("https://%s.file.core.windows.net", accountName);
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.storage.endpoint=" + endpoint,
                "spring.cloud.azure.storage.account-key=test-key",
                "spring.cloud.azure.storage.sas-token=test-sas-token",
                "spring.cloud.azure.storage.connection-string=" + connectionString,
                "spring.cloud.azure.storage.account-name=test-account-name"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageFileShareProperties.class);
                AzureStorageFileShareProperties properties = context.getBean(AzureStorageFileShareProperties.class);
                assertEquals(endpoint, properties.getEndpoint());
                assertEquals("test-key", properties.getAccountKey());
                assertEquals("test-sas-token", properties.getSasToken());
                assertEquals(connectionString, properties.getConnectionString());
                assertEquals(accountName, properties.getAccountName());
            });
    }

    private static class ShareServiceClientBuilderCustomizer extends TestBuilderCustomizer<ShareServiceClientBuilder> {

    }

    private static class OtherBuilderCustomizer extends TestBuilderCustomizer<ConfigurationClientBuilder> {

    }

}
