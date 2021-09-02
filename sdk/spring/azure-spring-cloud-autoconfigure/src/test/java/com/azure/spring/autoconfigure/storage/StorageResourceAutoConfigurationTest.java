// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageProtocolResolver;
import com.azure.spring.autoconfigure.unity.AzurePropertyAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageResourceAutoConfigurationTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
        AutoConfigurations.of(StorageResourceAutoConfiguration.class, AzurePropertyAutoConfiguration.class));

    @Test
    public void testAzureStorageDisabled() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(new ClassPathResource("storage.enable.config")))
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageProtocolResolver.class));
    }
}
