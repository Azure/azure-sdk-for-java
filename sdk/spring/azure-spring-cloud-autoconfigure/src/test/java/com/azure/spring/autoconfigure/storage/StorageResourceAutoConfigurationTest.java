// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageBlobProtocolResolver;
import com.azure.spring.cloud.autoconfigure.context.AzurePropertyAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class StorageResourceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(StorageResourceAutoConfiguration.class,
                                                 AzurePropertyAutoConfiguration.class));

    @Test
    public void testAzureStorageDisabled() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureStorageBlobProtocolResolver.class));
    }

}
