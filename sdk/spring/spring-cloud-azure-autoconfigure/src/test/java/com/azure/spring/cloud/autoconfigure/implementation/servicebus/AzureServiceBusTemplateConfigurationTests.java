// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.lang.reflect.Field;

import static com.azure.spring.cloud.autoconfigure.implementation.util.TestServiceBusUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class AzureServiceBusTemplateConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusMessagingAutoConfiguration.class));

    @Test
    void testAzureServiceBusDisabled() {
        this.contextRunner
            .withPropertyValues(AzureServiceBusProperties.PREFIX + ".enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ServiceBusTemplateConfiguration.class));
    }

    @Test
    void withoutServiceBusTemplateShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusTemplate.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.namespace=test-namespace"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ServiceBusTemplateConfiguration.class));
    }

    @Test
    void withoutServiceBusConnectionShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusMessagingAutoConfiguration.ServiceBusTemplateConfiguration.class));
    }

    @Test
    void testMessageConverterProvided() {
        this.contextRunner
            .withBean(ServiceBusMessageConverter.class, () -> mock(ServiceBusMessageConverter.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace")
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                assertThat(context).hasSingleBean(ServiceBusTemplate.class);

                ServiceBusMessageConverter messageConverter = context.getBean(ServiceBusMessageConverter.class);
                ServiceBusTemplate serviceBusTemplate = context.getBean(ServiceBusTemplate.class);
                assertSame(messageConverter, serviceBusTemplate.getMessageConverter());
            });
    }

    @Test
    void testMessageEntityTypeProvided() {
        this.contextRunner
            .withBean(ServiceBusMessageConverter.class, () -> mock(ServiceBusMessageConverter.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.servicebus.entity-type=QUEUE"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertServiceBusTemplate(context, ServiceBusEntityType.QUEUE);
            });
    }

    @Test
    void testMessageProducerEntityTypeProvided() {
        this.contextRunner
            .withBean(ServiceBusMessageConverter.class, () -> mock(ServiceBusMessageConverter.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "test-namespace"),
                "spring.cloud.azure.servicebus.producer.entity-type=topic"
            )
            .withUserConfiguration(AzureServiceBusPropertiesTestConfiguration.class)
            .run(context -> {
                assertServiceBusTemplate(context, ServiceBusEntityType.TOPIC);
            });
    }

    private static void assertServiceBusTemplate(AssertableApplicationContext context,
                                                 ServiceBusEntityType entityType) throws NoSuchFieldException, IllegalAccessException {
        assertThat(context).hasSingleBean(ServiceBusTemplate.class);

        ServiceBusTemplate serviceBusTemplate = context.getBean(ServiceBusTemplate.class);
        Field declaredFieldDefaultEntityType = ServiceBusTemplate.class.getDeclaredField("defaultEntityType");
        declaredFieldDefaultEntityType.setAccessible(true);
        ServiceBusEntityType defaultEntityType = (ServiceBusEntityType) declaredFieldDefaultEntityType.get(serviceBusTemplate);
        assertSame(entityType, defaultEntityType);
    }

}
