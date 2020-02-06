/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.storage;

import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.ServiceURL;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageAutoConfigurationTest {
    private static final String BLOB_HTTP_URL = "http://%s.blob.core.windows.net";
    private static final String BLOB_HTTPS_URL = "https://%s.blob.core.windows.net";
    private static final String ACCOUNT_KEY = "ZmFrZUFjY291bnRLZXk="; /* Base64 encoded for string fakeAccountKey */
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class));

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void serviceUrlBeanNotCreatedByDefault() {
        contextRunner.run(context -> context.getBean(ServiceURL.class));
    }

    @Test
    public void serviceUrlBeanCreatedCorrectly() {
        contextRunner.withPropertyValues("azure.storage.account-name=fakeStorageAccountName",
                "azure.storage.account-key=" + ACCOUNT_KEY)
                .run(context -> {
                    final ServiceURL serviceURL = context.getBean(ServiceURL.class);
                    final String blobUrl = String.format(BLOB_HTTP_URL, "fakeStorageAccountName");
                    assertThat(serviceURL).isNotNull();
                    assertThat(serviceURL.toURL().toString()).isEqualTo(blobUrl);
                });
        
        contextRunner.withPropertyValues("azure.storage.account-name=fakeStorageAccountName",
                "azure.storage.account-key=" + ACCOUNT_KEY, "azure.storage.enable-https=true")
                .run(context -> {
                    final ServiceURL serviceURL = context.getBean(ServiceURL.class);
                    final String blobUrl = String.format(BLOB_HTTPS_URL, "fakeStorageAccountName");
                    assertThat(serviceURL).isNotNull();
                    assertThat(serviceURL.toURL().toString()).isEqualTo(blobUrl);
                });
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void containerUrlNotCreatedIfNotConfigured() {
        contextRunner.withPropertyValues("azure.storage.account-name=fakeStorageAccountName",
                "azure.storage.account-key=" + ACCOUNT_KEY)
                .run(context -> context.getBean(ContainerURL.class));
    }

    @Test
    public void containerUrlCreatedIfConfigured() {
        contextRunner.withPropertyValues("azure.storage.account-name=fakeStorageAccountName",
                "azure.storage.account-key=" + ACCOUNT_KEY,
                "azure.storage.container-name=fakestoragecontainername")
                .run(context -> {
                    final ContainerURL containerURL = context.getBean(ContainerURL.class);
                    assertThat(containerURL).isNotNull();
                    assertThat(containerURL.toURL().toString()).contains("fakestoragecontainername");
                });
    }
}
