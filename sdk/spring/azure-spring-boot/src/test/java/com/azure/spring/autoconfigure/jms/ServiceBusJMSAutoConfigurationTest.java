// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceBusJMSAutoConfigurationTest {
    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ServiceBusJMSAutoConfiguration.class, JmsAutoConfiguration.class));

    private final String connectionString = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;SharedAccessKey=sasKey";

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
    public void testCachingConnectionFactoryIsAutowired() {

        configureContextPropertyValues();

        this.contextRunner.run(
            context -> {
                assertThat(context).hasSingleBean(ConnectionFactory.class);
                assertThat(context).hasSingleBean(JmsTemplate.class);
                ConnectionFactory connectionFactory = context.getBean(ConnectionFactory.class);
                assertThat(context.getBean(JmsTemplate.class).getConnectionFactory()).isEqualTo(connectionFactory);
            }
        );
    }

    @Test
    public void testAzureServiceBusJMSPropertiesConfigured() {

        configureContextPropertyValues();

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

    private void configureContextPropertyValues() {

        this.contextRunner = this.contextRunner.withPropertyValues(
            "spring.jms.servicebus.connection-string=" + connectionString,
            "spring.jms.servicebus.topic-client-id=cid",
            "spring.jms.servicebus.idle-timeout=123"
        );

    }
}
