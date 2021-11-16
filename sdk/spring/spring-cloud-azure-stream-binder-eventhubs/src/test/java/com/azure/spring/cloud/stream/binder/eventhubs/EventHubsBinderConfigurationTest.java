// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureEventHubsResourceManagerAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureResourceManagerAutoConfiguration;
import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsBinderConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class EventHubsBinderConfigurationTest {

    private static final String EVENT_HUB_PROPERTY_PREFIX = "spring.cloud.azure.eventhub.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    private String connectionString = "connection-string=Endpoint=sb://eventhub-test-1"
        + ".servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;"
        + "SharedAccessKey=ByyyxxxUw=";

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues(AZURE_PROPERTY_PREFIX + "stream.function.definition=supply")
        .withPropertyValues(AZURE_PROPERTY_PREFIX + "stream.bindings.supply-out-0.destination=eventhub1")
        .withConfiguration(AutoConfigurations.of(AzureResourceManagerAutoConfiguration.class,
                                                 AzureEventHubsResourceManagerAutoConfiguration.class,
                                                 AzureEventHubsAutoConfiguration.class,
                                                 AzureEventHubsMessagingAutoConfiguration.class,
                                                 EventHubsBinderConfiguration.class));
/*
// TODO (xiada): tests
    @Test
    public void testStorageNotConfiguredToGetClientFactoryBeanOnConnectionString() {
        contextRunner
            .withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + connectionString)
            .run(context -> {
                assertThat(context).hasSingleBean(EventHubClientFactory.class);
                assertThat(context).hasSingleBean(EventHubOperation.class);
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
                assertThat(context).doesNotHaveBean(com.azure.spring.cloud.resourcemanager.core.impl.StorageAccountCrud.class);
            });
    }
*/

}
