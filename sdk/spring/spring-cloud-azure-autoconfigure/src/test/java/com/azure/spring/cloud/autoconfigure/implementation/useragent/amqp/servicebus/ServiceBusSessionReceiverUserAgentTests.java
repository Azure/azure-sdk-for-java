// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.useragent.amqp.servicebus;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusSessionReceiverClientBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.core.implementation.util.ReflectionUtils.getField;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceBusSessionReceiverUserAgentTests {

    @Test
    void shareServiceBusClientBuilderUserAgentTest() {
        userAgentTest(
            "spring.cloud.azure.servicebus.connection-string=Endpoint=sb://sample.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key",
            "spring.cloud.azure.servicebus.consumer.entity-name=sample",
            "spring.cloud.azure.servicebus.consumer.entity-type=QUEUE",
            "spring.cloud.azure.servicebus.consumer.session-enabled=true"
        );
    }

    @Test
    void notShareServiceBusClientBuilderUserAgentTest() {
        userAgentTest(
            "spring.cloud.azure.servicebus.connection-string=Endpoint=sb://sample.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key",
            "spring.cloud.azure.servicebus.consumer.entity-name=sample",
            "spring.cloud.azure.servicebus.consumer.entity-type=QUEUE",
            "spring.cloud.azure.servicebus.consumer.session-enabled=true",
            "spring.cloud.azure.servicebus.consumer.namespace=sample"
        );
    }

    void userAgentTest(String... propertyValues) {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureServiceBusAutoConfiguration.class))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withPropertyValues(propertyValues)
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusSessionReceiverClientBuilderFactory.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);

                ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder = context.getBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class);
                ServiceBusClientBuilder builder = (ServiceBusClientBuilder) getField(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class, "this$0", receiverClientBuilder);
                ClientOptions options = (ClientOptions) getField(ServiceBusClientBuilder.class, "clientOptions", builder);
                Assertions.assertNotNull(options);
                Assertions.assertEquals(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS, options.getApplicationId());

            });
    }

}
