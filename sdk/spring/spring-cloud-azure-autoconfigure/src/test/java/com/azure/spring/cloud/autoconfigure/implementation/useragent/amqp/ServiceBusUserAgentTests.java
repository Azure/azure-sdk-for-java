// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.useragent.amqp;

import com.azure.core.util.ClientOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusAutoConfiguration;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.service.implementation.servicebus.factory.ServiceBusClientBuilderFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.core.implementation.util.ReflectionUtils.getField;
import static org.assertj.core.api.Assertions.assertThat;

class ServiceBusUserAgentTests {

    @Test
    void userAgentTest() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureServiceBusAutoConfiguration.class))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=Endpoint=sb://sample.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureServiceBusAutoConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilderFactory.class);
                assertThat(context).hasSingleBean(ServiceBusClientBuilder.class);
                assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);

                ServiceBusClientBuilder builder = context.getBean(ServiceBusClientBuilder.class);
                ClientOptions options = (ClientOptions) getField(ServiceBusClientBuilder.class, "clientOptions", builder);
                Assertions.assertNotNull(options);
                Assertions.assertEquals(AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS, options.getApplicationId());

            });
    }

}
