// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder;

import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubAutoConfiguration;
import com.azure.spring.cloud.context.core.impl.EventHubNamespaceManager;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.eventhub.stream.binder.config.EventHubBinderConfiguration;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubOperation;
import com.azure.spring.integration.eventhub.factory.EventHubConnectionStringProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class EventHubBinderConfigurationTest {

    private static final String EVENT_HUB_PROPERTY_PREFIX = "spring.cloud.azure.eventhub.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private String connectionString = "connection-string=Endpoint=sb://eventhub-test-1\"\n"
        + "                + \".servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;\"\n"
        + "                + \"SharedAccessKey=ByyyxxxUw=";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues(AZURE_PROPERTY_PREFIX + "stream.function.definition=supply")
        .withPropertyValues(AZURE_PROPERTY_PREFIX + "stream.bindings.supply-out-0.destination=eventhub1")
        .withConfiguration(AutoConfigurations.of(AzureEnvironmentAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(AzureContextAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(AzureEventHubAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(EventHubBinderConfiguration.class));

    @Test
    public void testStorageNotConfiguredToGetClientFactoryBeanOnConnectionString() {
        contextRunner
            .withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + connectionString)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubClientFactory.class);
                assertThat(context).hasSingleBean(EventHubOperation.class);
                assertThat(context).doesNotHaveBean(StorageAccountManager.class);
            });
    }

    @Test
    public void testStorageNotConfiguredToGetClientFactoryBeanOnMSI() {
        contextRunner
            .withPropertyValues(
                AZURE_PROPERTY_PREFIX + "msi-enabled=true",
                AZURE_PROPERTY_PREFIX + "client-id=fake-client-id",
                AZURE_PROPERTY_PREFIX + "resource-group=fake-res-group",
                AZURE_PROPERTY_PREFIX + "subscription-id=fake-sub"
            )
            .withBean(EventHubConnectionStringProvider.class, connectionString)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubClientFactory.class);
                assertThat(context).hasSingleBean(EventHubNamespaceManager.class);
                assertThat(context).hasSingleBean(EventHubOperation.class);
                assertThat(context).doesNotHaveBean(StorageAccountManager.class);
            });
    }

}
