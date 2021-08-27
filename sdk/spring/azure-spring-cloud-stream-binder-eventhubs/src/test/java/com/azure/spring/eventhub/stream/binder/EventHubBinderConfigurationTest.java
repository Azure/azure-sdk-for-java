package com.azure.spring.eventhub.stream.binder;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureEnvironmentAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.eventhub.AzureEventHubAutoConfiguration;
import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.impl.StorageAccountManager;
import com.azure.spring.eventhub.stream.binder.config.EventHubBinderConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class EventHubBinderConfigurationTest {

    private static final String EVENT_HUB_PROPERTY_PREFIX = "spring.cloud.azure.eventhub.";
    private static final String AZURE_PROPERTY_PREFIX = "spring.cloud.stream.";

    @Mock
    private AzureResourceManager azureResourceManager;

    @Mock
    private AzureProperties azureProperties;

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues(EVENT_HUB_PROPERTY_PREFIX + "connection-string=Endpoint=sb://eventhub-test-1"
            + ".servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;"
            + "SharedAccessKey=ByyyxxxUw=")
        .withPropertyValues(AZURE_PROPERTY_PREFIX + "function.definition=supply")
        .withPropertyValues(AZURE_PROPERTY_PREFIX + "bindings.supply-out-0.destination=eventhub1")
        .withConfiguration(AutoConfigurations.of(AzureEnvironmentAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(AzureContextAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(AzureEventHubAutoConfiguration.class))
        .withConfiguration(AutoConfigurations.of(EventHubBinderConfiguration.class));

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testStorageNotConfiguredToGetClientFactoryBean() {
        contextRunner
            .withBean("storageAccountManager", StorageAccountManager.class, azureResourceManager, azureProperties)
            .run(context -> assertThat(context).hasBean("eventhubClientFactory"));
    }
}
