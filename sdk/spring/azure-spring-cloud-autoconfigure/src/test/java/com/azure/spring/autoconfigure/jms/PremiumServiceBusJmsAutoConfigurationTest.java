// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumServiceBusJmsAutoConfigurationTest extends AbstractServiceBusJmsAutoConfigurationTest {

    PremiumServiceBusJmsAutoConfigurationTest() {
        this.contextRunner = super.contextRunner
            .withPropertyValues("spring.jms.servicebus.pricing-tier=premium")
            .withConfiguration(AutoConfigurations.of(PremiumServiceBusJmsAutoConfiguration.class));
    }

    @Test
    void testAzureServiceBusPremiumAutoConfiguration() {
        this.contextRunner.withPropertyValues("spring.jms.servicebus.pricing-tier=basic")
                     .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class));

        this.contextRunner.withPropertyValues("spring.jms.servicebus.enabled=false")
                     .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class));

        this.contextRunner.withPropertyValues("spring.jms.servicebus.connection-string=" + CONNECTION_STRING)
                     .run(context -> assertThat(context).hasSingleBean(AzureServiceBusJmsProperties.class));
    }

    @Test
    void testWithoutServiceBusJMSNamespace() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusJmsConnectionFactory.class))
                          .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJmsProperties.class));
    }

}
