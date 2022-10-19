// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.core.management.AzureEnvironment.AZURE;
import static com.azure.core.management.AzureEnvironment.AZURE_CHINA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class AzureResourceManagerAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureResourceManagerAutoConfiguration.class));

    @Test
    void testAzureResourceManagerDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.resourcemanager.enabled=false")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureResourceManager.class);
                assertThat(context).doesNotHaveBean(AzureProfile.class);
            });
    }

    @Test
    void configureWithoutTenantId() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.resourcemanager.enabled=true")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureResourceManager.class);
                assertThat(context).doesNotHaveBean(AzureProfile.class);
            });
    }

    @Test
    void configureWithTenantIdAndSubId() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=test-tenant",
                "spring.cloud.azure.profile.subscription-id=test-subscription-id"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .run(context -> {
                assertThat(context).hasSingleBean(AzureResourceManagerAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureProfile.class);
            });
    }

    @Test
    void configureWithTenantId() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.profile.tenant-id=test-tenant")
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureResourceManager.class);
                assertThat(context).doesNotHaveBean(AzureProfile.class);
            });
    }

    @Test
    void testWithoutAzureResourceManagerClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(AzureResourceManager.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureProfile.class));
    }

    @Test
    void testWithoutAzureResourceMetadataClass() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(AzureResourceMetadata.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureProfile.class));
    }

    @Test
    void testAzureProfileWithAzureDefault() {
        this.contextRunner
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=test-tenant-id",
                "spring.cloud.azure.profile.subscription-id=test-subscription-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureProfile.class);

                AzureProfile azureProfile = context.getBean(AzureProfile.class);
                assertEquals("test-subscription-id", azureProfile.getSubscriptionId());
                assertEquals("test-tenant-id", azureProfile.getTenantId());
                assertEquals(AZURE.getActiveDirectoryEndpoint(), azureProfile.getEnvironment().getActiveDirectoryEndpoint());
            });
    }

    @Test
    void testAzureProfileWithAzureChina() {
        this.contextRunner
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class)
            .withBean(AzureResourceManager.class, () -> mock(AzureResourceManager.class))
            .withPropertyValues(
                "spring.cloud.azure.profile.tenant-id=test-tenant-id",
                "spring.cloud.azure.profile.subscription-id=test-subscription-id",
                "spring.cloud.azure.profile.cloud-type=azure_china"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureProfile.class);

                AzureProfile azureProfile = context.getBean(AzureProfile.class);
                assertEquals("test-subscription-id", azureProfile.getSubscriptionId());
                assertEquals("test-tenant-id", azureProfile.getTenantId());
                assertEquals(AZURE_CHINA.getActiveDirectoryEndpoint(), azureProfile.getEnvironment().getActiveDirectoryEndpoint());
            });
    }
}
