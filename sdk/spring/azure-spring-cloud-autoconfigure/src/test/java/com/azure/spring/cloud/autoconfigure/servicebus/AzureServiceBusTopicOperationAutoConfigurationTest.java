// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.autoconfigure.resourcemanager.AzureServiceBusResourceManagerAutoConfiguration;
import com.azure.spring.core.converter.AzureAmqpRetryOptionsConverter;
import com.azure.spring.integration.servicebus.converter.ServiceBusMessageConverter;
import com.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusQueueOperationAutoConfigurationTest.NAMESPACE_CONNECTION_STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AzureServiceBusTopicOperationAutoConfigurationTest {

    private static final AzureAmqpRetryOptionsConverter RETRY_OPTIONS_CONVERTER = new AzureAmqpRetryOptionsConverter();

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureServiceBusTopicOperationAutoConfiguration.class));


    @Test
    public void testAzureServiceBusTopicDisabled() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".enabled=false")
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testWithoutAzureServiceBusTopicClient() {
        this.contextRunner.withClassLoader(new FilteredClassLoader(ServiceBusProcessorClient.class))
                          .run(context -> assertThat(context).doesNotHaveBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testConnectionStringProvided() {
        this.contextRunner.withUserConfiguration(
            AzureServiceBusQueueOperationAutoConfigurationTest.StaticConnectionStringProviderConfiguration.class)
                          .run(context -> assertThat(context).hasSingleBean(ServiceBusTopicClientFactory.class)
                                                             .hasSingleBean(ServiceBusTopicOperation.class));
    }

    @Test
    public void testTransportTypeWithAmqpWebSockets() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".client.transport-type=AmqpWebSockets")
                          .withUserConfiguration(AzureServiceBusResourceManagerAutoConfiguration.class)
                          .run(context -> assertThat(context.getBean(AzureServiceBusProperties.class)
                                                            .getClient()
                                                            .getTransportType())
                              .isEqualTo(AmqpTransportType.AMQP_WEB_SOCKETS));
    }

    @Test
    public void testRetryOptions() {
        this.contextRunner.withPropertyValues(AzureServiceBusProperties.PREFIX + ".retry.max-attempts=5",
                                              AzureServiceBusProperties.PREFIX + ".retry.timeout=3000",
                                              AzureServiceBusProperties.PREFIX + ".retry.backoff.delay=1000",
                                              AzureServiceBusProperties.PREFIX + ".retry.backoff.maxDelay=2000")
                          .withUserConfiguration(AzureServiceBusResourceManagerAutoConfiguration.class)
                          .run(context -> {
                              final AzureServiceBusProperties properties = context.getBean(
                                  AzureServiceBusProperties.class);
                              final AmqpRetryOptions retryOptions = RETRY_OPTIONS_CONVERTER.convert(properties.getRetry());

                              assertThat(retryOptions.getMaxRetries()).isEqualTo(5);
                              assertThat(retryOptions.getTryTimeout().getSeconds()).isEqualTo(3L);
                              assertThat(retryOptions.getDelay().getSeconds()).isEqualTo(1);
                              assertThat(retryOptions.getMaxDelay().getSeconds()).isEqualTo(2);
                              assertThat(retryOptions.getMode()).isEqualTo(AmqpRetryMode.FIXED);
                          });
    }

    @Test
    public void testMessageConverterProvided() {
        this.contextRunner.withUserConfiguration(
                AzureServiceBusQueueOperationAutoConfigurationTest.MessageConverterConfiguration.class,
            AzureServiceBusResourceManagerAutoConfiguration.class)
                          .withPropertyValues(
                              AzureServiceBusProperties.PREFIX + "connection-string=" + NAMESPACE_CONNECTION_STRING
                          )
                          .run(context -> {
                              assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                              assertThat(context).hasSingleBean(ServiceBusTopicTemplate.class);

                              ServiceBusMessageConverter messageConverter =
                                  context.getBean(ServiceBusMessageConverter.class);
                              ServiceBusTopicTemplate topicTemplate = context.getBean(ServiceBusTopicTemplate.class);
                              assertSame(messageConverter, topicTemplate.getMessageConverter());
                          });
    }

}
