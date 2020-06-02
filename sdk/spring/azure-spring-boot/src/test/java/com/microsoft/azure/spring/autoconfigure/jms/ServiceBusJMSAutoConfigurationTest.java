// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.jms;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceBusJMSAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ServiceBusJMSAutoConfiguration.class));

    @Test
    public void testAzureServiceBusDisabled() {
        this.contextRunner.withPropertyValues("spring.jms.servicebus.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));
    }

    @Test
    public void testWithoutServiceBusJMSNamespace() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(JmsConnectionFactory.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureServiceBusJMSProperties.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testAzureServiceBusJMSPropertiesValidation() {
        this.contextRunner.run(context -> context.getBean(AzureServiceBusJMSProperties.class));
    }

    @Test
    public void testAzureServiceBusJMSPropertiesConfigured() {

        final String connectionString = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;SharedAccessKey=sasKey";

        this.contextRunner = this.contextRunner.withPropertyValues(
            "spring.jms.servicebus.connection-string=" + connectionString
        );

        this.contextRunner = this.contextRunner.withPropertyValues(
            "spring.jms.servicebus.topic-client-id=cid"
        );

        this.contextRunner = this.contextRunner.withPropertyValues(
            "spring.jms.servicebus.idle-timeout=123"
        );

        this.contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(AzureServiceBusJMSProperties.class);
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getConnectionString()).isEqualTo(
                    connectionString);
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getTopicClientId()).isEqualTo("cid");
                assertThat(context.getBean(AzureServiceBusJMSProperties.class).getIdleTimeout()).isEqualTo(123);
            }
        );
    }

}
