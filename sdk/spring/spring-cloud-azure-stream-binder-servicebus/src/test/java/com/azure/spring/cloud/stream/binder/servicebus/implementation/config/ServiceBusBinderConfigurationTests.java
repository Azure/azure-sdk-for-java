// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation.config;

import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusProperties;
import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.customizer.AzureServiceClientBuilderCustomizer;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.ServiceBusProvisioner;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProcessorFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.servicebus.config.ServiceBusProducerFactoryCustomizer;
import com.azure.spring.cloud.stream.binder.servicebus.implementation.ServiceBusMessageChannelBinder;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusExtendedBindingProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.implementation.provisioning.ServiceBusChannelResourceManagerProvisioner;
import com.azure.spring.messaging.servicebus.core.DefaultServiceBusNamespaceProducerFactory;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.Duration;

import static com.azure.messaging.servicebus.models.SubQueue.DEAD_LETTER_QUEUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ServiceBusBinderConfigurationTests {

    private static final String CONNECTION_STRING_FORMAT = "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ServiceBusBinderConfiguration.class));

    @Test
    void configurationNotMatchedWhenBinderBeanExist() {
        this.contextRunner
            .withBean(Binder.class, () -> mock(Binder.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(ServiceBusBinderConfiguration.class);
                assertThat(context).doesNotHaveBean(ServiceBusMessageChannelBinder.class);
            });
    }

    @Test
    void shouldConfigureDefaultChannelProvisionerWhenNoResourceManagerProvided() {
        this.contextRunner
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusBinderConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(ServiceBusChannelProvisioner.class);
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);

                ServiceBusChannelProvisioner channelProvisioner = context.getBean(ServiceBusChannelProvisioner.class);
                assertThat(channelProvisioner).isNotInstanceOf(ServiceBusChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void shouldConfigureArmChannelProvisionerWhenResourceManagerProvided() {
        this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusBinderConfiguration.class);
                assertThat(context).hasSingleBean(ServiceBusExtendedBindingProperties.class);
                assertThat(context).hasSingleBean(ServiceBusChannelProvisioner.class);
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);

                ServiceBusChannelProvisioner channelProvisioner = context.getBean(ServiceBusChannelProvisioner.class);
                assertThat(channelProvisioner).isInstanceOf(ServiceBusChannelResourceManagerProvisioner.class);
            });
    }

    @Test
    void testExtendedBindingPropertiesShouldBind() {
        String producerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-producer-namespace");
        String consumerConnectionString = String.format(CONNECTION_STRING_FORMAT, "fake-consumer-namespace");

        this.contextRunner
            .withPropertyValues(
                "spring.cloud.stream.servicebus.bindings.input.consumer.domain-name=fake-consumer-domain",
                "spring.cloud.stream.servicebus.bindings.input.consumer.namespace=fake-consumer-namespace",
                "spring.cloud.stream.servicebus.bindings.input.consumer.connection-string=" + consumerConnectionString,
                "spring.cloud.stream.servicebus.bindings.input.consumer.entity-name=fake-consumer-sb-entity",
                "spring.cloud.stream.servicebus.bindings.input.consumer.entity-type=TOPIC",
                "spring.cloud.stream.servicebus.bindings.input.consumer.session-enabled=true",
                "spring.cloud.stream.servicebus.bindings.input.consumer.auto-complete=true",
                "spring.cloud.stream.servicebus.bindings.input.consumer.prefetch-count=1",
                "spring.cloud.stream.servicebus.bindings.input.consumer.sub-queue=DEAD_LETTER_QUEUE",
                "spring.cloud.stream.servicebus.bindings.input.consumer.receive-mode=RECEIVE_AND_DELETE",
                "spring.cloud.stream.servicebus.bindings.input.consumer.subscription-name=fake-consumer-subscription",
                "spring.cloud.stream.servicebus.bindings.input.consumer.max-auto-lock-renew-duration=2s",
                "spring.cloud.stream.servicebus.bindings.input.consumer.max-concurrent-calls=5",
                "spring.cloud.stream.servicebus.bindings.input.consumer.max-concurrent-sessions=6",
                "spring.cloud.stream.servicebus.bindings.input.consumer.requeue-rejected=true",

                "spring.cloud.stream.servicebus.bindings.input.producer.domain-name=fake-producer-domain",
                "spring.cloud.stream.servicebus.bindings.input.producer.namespace=fake-producer-namespace",
                "spring.cloud.stream.servicebus.bindings.input.producer.connection-string=" + producerConnectionString,
                "spring.cloud.stream.servicebus.bindings.input.producer.entity-name=fake-producer-sb-entity",
                "spring.cloud.stream.servicebus.bindings.input.producer.entity-type=QUEUE",
                "spring.cloud.stream.servicebus.bindings.input.producer.sync=true",
                "spring.cloud.stream.servicebus.bindings.input.producer.send-timeout=5m"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);
                ServiceBusMessageChannelBinder binder = context.getBean(ServiceBusMessageChannelBinder.class);
                ServiceBusConsumerProperties consumerProperties =
                    binder.getExtendedConsumerProperties("input");
                assertEquals("fake-consumer-domain", consumerProperties.getDomainName());
                assertEquals("fake-consumer-namespace", consumerProperties.getNamespace());
                assertEquals(consumerConnectionString, consumerProperties.getConnectionString());
                assertEquals("fake-consumer-sb-entity", consumerProperties.getEntityName());
                assertEquals(ServiceBusEntityType.TOPIC, consumerProperties.getEntityType());
                assertTrue(consumerProperties.getSessionEnabled());
                assertTrue(consumerProperties.getAutoComplete());
                assertEquals(1, consumerProperties.getPrefetchCount());
                assertEquals(DEAD_LETTER_QUEUE, consumerProperties.getSubQueue());
                assertEquals(ServiceBusReceiveMode.RECEIVE_AND_DELETE, consumerProperties.getReceiveMode());
                assertEquals("fake-consumer-subscription", consumerProperties.getSubscriptionName());
                assertEquals(Duration.ofSeconds(2), consumerProperties.getMaxAutoLockRenewDuration());
                assertEquals(5, consumerProperties.getMaxConcurrentCalls());
                assertTrue(consumerProperties.isRequeueRejected());

                ServiceBusProducerProperties producerProperties =
                    binder.getExtendedProducerProperties("input");
                assertEquals("fake-producer-domain", producerProperties.getDomainName());
                assertEquals("fake-producer-namespace", producerProperties.getNamespace());
                assertEquals(producerConnectionString, producerProperties.getConnectionString());
                assertEquals("fake-producer-sb-entity", producerProperties.getEntityName());
                assertEquals(ServiceBusEntityType.QUEUE, producerProperties.getEntityType());
                assertTrue(producerProperties.isSync());
                assertEquals(Duration.ofMinutes(5), producerProperties.getSendTimeout());
            });
    }

    @Test
    void clientMessageConverterShouldBeConfigured() {
        this.contextRunner
            .withBean(ServiceBusMessageConverter.class)
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusMessageConverter.class);
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);
                ServiceBusMessageConverter converter = context.getBean(ServiceBusMessageConverter.class);
                ServiceBusMessageChannelBinder binder = context.getBean(ServiceBusMessageChannelBinder.class);
                assertThat(ReflectionTestUtils.getField(binder, "messageConverter")).isSameAs(converter);
            });
    }

    @Test
    void producerFactoryCustomizerShouldBeConfigured() {
        this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .run(context -> assertThat(context).hasSingleBean(ServiceBusProducerFactoryCustomizer.class));
    }

    @Test
    void processorFactoryCustomizerShouldBeConfigured() {
        this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .run(context -> assertThat(context).hasSingleBean(ServiceBusProcessorFactoryCustomizer.class));
    }

    @Test
    void producerBuilderCustomizerShouldBeConfiguredToProducerFactoryCustomizer() {
        getCustomizedApplicationContextRunner()
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusProducerFactoryCustomizer.class);
                ServiceBusProducerFactoryCustomizer clientFactoryCustomizer = context.getBean(ServiceBusProducerFactoryCustomizer.class);

                ServiceBusBinderConfiguration.DefaultProducerFactoryCustomizer defaultFactoryCustomizer = (ServiceBusBinderConfiguration.DefaultProducerFactoryCustomizer) clientFactoryCustomizer;

                assertEquals(1, (int) defaultFactoryCustomizer.getClientBuilderCustomizers().stream().count());
                assertEquals(1, (int) defaultFactoryCustomizer.getSenderClientBuilderCustomizers().stream().count());
            });
    }

    @Test
    void processorBuilderCustomizerShouldBeConfiguredToProcessorFactoryCustomizer() {
        getCustomizedApplicationContextRunner()
            .run(context -> {
                assertThat(context).hasSingleBean(ServiceBusProcessorFactoryCustomizer.class);
                ServiceBusProcessorFactoryCustomizer clientFactoryCustomizer = context.getBean(ServiceBusProcessorFactoryCustomizer.class);

                ServiceBusBinderConfiguration.DefaultProcessorFactoryCustomizer defaultFactoryCustomizer = (ServiceBusBinderConfiguration.DefaultProcessorFactoryCustomizer) clientFactoryCustomizer;

                assertEquals(1, (int) defaultFactoryCustomizer.getClientBuilderCustomizers().stream().count());
                assertEquals(2, (int) defaultFactoryCustomizer.getProcessorClientBuilderCustomizers().stream().count());
                assertEquals(3, (int) defaultFactoryCustomizer.getSessionProcessorClientBuilderCustomizers().stream().count());
            });
    }

    private ApplicationContextRunner getCustomizedApplicationContextRunner() {
        return this.contextRunner
            .withBean(ServiceBusProvisioner.class, () -> mock(ServiceBusProvisioner.class))
            .withPropertyValues("spring.cloud.azure.servicebus.namespace=fake-namespace")
            .withBean("client-customizer1", ServiceBusClientBuilderCustomizer.class, ServiceBusClientBuilderCustomizer::new)
            .withBean("producer-customizer1", ServiceBusSenderClientBuilderCustomizer.class, ServiceBusSenderClientBuilderCustomizer::new)
            .withBean("processor-customizer1", ServiceBusProcessorClientBuilderCustomizer.class, ServiceBusProcessorClientBuilderCustomizer::new)
            .withBean("processor-customizer2", ServiceBusProcessorClientBuilderCustomizer.class, ServiceBusProcessorClientBuilderCustomizer::new)
            .withBean("session-processor-customizer1", ServiceBusSessionProcessorClientBuilderCustomizer.class, ServiceBusSessionProcessorClientBuilderCustomizer::new)
            .withBean("session-processor-customizer2", ServiceBusSessionProcessorClientBuilderCustomizer.class, ServiceBusSessionProcessorClientBuilderCustomizer::new)
            .withBean("session-processor-customizer3", ServiceBusSessionProcessorClientBuilderCustomizer.class, ServiceBusSessionProcessorClientBuilderCustomizer::new)
            .withBean("other-customizer1", OtherBuilderCustomizer.class, OtherBuilderCustomizer::new);
    }

    private static class ServiceBusClientBuilderCustomizer implements AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder> {

        @Override
        public void customize(ServiceBusClientBuilder builder) {

        }
    }

    private static class ServiceBusSenderClientBuilderCustomizer implements AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSenderClientBuilder> {

        @Override
        public void customize(ServiceBusClientBuilder.ServiceBusSenderClientBuilder builder) {

        }
    }

    private static class ServiceBusProcessorClientBuilderCustomizer implements AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusProcessorClientBuilder> {

        @Override
        public void customize(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder builder) {

        }
    }

    private static class ServiceBusSessionProcessorClientBuilderCustomizer implements AzureServiceClientBuilderCustomizer<ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder> {

        @Override
        public void customize(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder builder) {

        }
    }


    private static class OtherBuilderCustomizer implements AzureServiceClientBuilderCustomizer<Object> {

        @Override
        public void customize(Object builder) {

        }
    }

    @Test
    void testCustomTokenCredentialConfiguration() {
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "test-namespace");

        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CustomTokenCredentialConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + connectionString,
                "spring.cloud.azure.servicebus.credential.token-credential-bean-name=customTokenCredential"
            )
            .run(context -> {
                // Verify that the custom token credential bean exists
                assertThat(context).hasBean("customTokenCredential");
                TokenCredential customCredential = context.getBean("customTokenCredential", TokenCredential.class);
                assertThat(customCredential).isNotNull();

                // Verify that the properties contain the correct credential bean name
                AzureServiceBusProperties serviceBusProperties = context.getBean(AzureServiceBusProperties.class);
                assertThat(serviceBusProperties).isNotNull();
                assertThat(serviceBusProperties.getCredential()).isNotNull();
                assertThat(serviceBusProperties.getCredential().getTokenCredentialBeanName())
                    .as("The token-credential-bean-name property should be set to customTokenCredential")
                    .isEqualTo("customTokenCredential");

                // Verify the ServiceBusProducerFactoryCustomizer is configured and can apply credential settings
                assertThat(context).hasSingleBean(ServiceBusProducerFactoryCustomizer.class);
                ServiceBusProducerFactoryCustomizer producerFactoryCustomizer =
                    context.getBean(ServiceBusProducerFactoryCustomizer.class);
                assertThat(producerFactoryCustomizer).isNotNull();

                // Verify it's the default customizer with token credential resolver
                assertThat(producerFactoryCustomizer)
                    .isInstanceOf(ServiceBusBinderConfiguration.DefaultProducerFactoryCustomizer.class);
            });
    }

    @Test
    void testCustomTokenCredentialConfigurationWithBinder() {
        String connectionString = String.format(CONNECTION_STRING_FORMAT, "test-namespace");

        this.contextRunner
            .withConfiguration(AutoConfigurations.of(CustomTokenCredentialConfiguration.class))
            .withPropertyValues(
                "spring.cloud.azure.servicebus.connection-string=" + connectionString,
                "spring.cloud.azure.servicebus.credential.token-credential-bean-name=customTokenCredential"
            )
            .run(context -> {
                // Verify that the custom token credential bean exists
                assertThat(context).hasBean("customTokenCredential");
                TokenCredential customCredential = context.getBean("customTokenCredential", TokenCredential.class);
                assertThat(customCredential).isNotNull();

                // Verify that the binder is created
                assertThat(context).hasSingleBean(ServiceBusMessageChannelBinder.class);
                ServiceBusMessageChannelBinder binder = context.getBean(ServiceBusMessageChannelBinder.class);
                assertThat(binder).isNotNull();

                // Test Producer Factory
                // Get the ServiceBusTemplate through reflection (it's created lazily in getServiceBusTemplate)
                Object serviceBusTemplate = ReflectionTestUtils.invokeMethod(binder, "getServiceBusTemplate");
                assertThat(serviceBusTemplate).isNotNull();

                // Get the producer factory from the template
                Field producerFactoryField = serviceBusTemplate.getClass().getDeclaredField("producerFactory");
                producerFactoryField.setAccessible(true);
                Object producerFactory = producerFactoryField.get(serviceBusTemplate);
                assertThat(producerFactory).isInstanceOf(DefaultServiceBusNamespaceProducerFactory.class);

                // Verify tokenCredentialResolver is configured in the producer factory created by binder
                Field producerTokenCredentialResolverField =
                    producerFactory.getClass().getDeclaredField("tokenCredentialResolver");
                producerTokenCredentialResolverField.setAccessible(true);
                Object producerTokenCredentialResolver = producerTokenCredentialResolverField.get(producerFactory);
                assertThat(producerTokenCredentialResolver)
                    .as("TokenCredentialResolver should be configured in the binder's producer factory")
                    .isNotNull();

                // Verify it resolves to the custom credential
                AzureServiceBusProperties serviceBusProperties = context.getBean(AzureServiceBusProperties.class);
                @SuppressWarnings("unchecked")
                AzureCredentialResolver<TokenCredential> producerResolver =
                    (AzureCredentialResolver<TokenCredential>) producerTokenCredentialResolver;
                TokenCredential producerResolvedCredential = producerResolver.resolve(serviceBusProperties);
                assertThat(producerResolvedCredential)
                    .as("The resolved credential in binder's producer factory should be the customTokenCredential bean")
                    .isSameAs(customCredential);

                // Test Processor Factory
                // Get the ProcessorFactory through reflection (it's created lazily in getProcessorFactory)
                Object processorFactory = ReflectionTestUtils.invokeMethod(binder, "getProcessorFactory");
                assertThat(processorFactory).isNotNull();

                // Verify tokenCredentialResolver is configured in the processor factory created by binder
                Field processorTokenCredentialResolverField =
                    processorFactory.getClass().getDeclaredField("tokenCredentialResolver");
                processorTokenCredentialResolverField.setAccessible(true);
                Object processorTokenCredentialResolver = processorTokenCredentialResolverField.get(processorFactory);
                assertThat(processorTokenCredentialResolver)
                    .as("TokenCredentialResolver should be configured in the binder's processor factory")
                    .isNotNull();

                // Verify it resolves to the custom credential
                @SuppressWarnings("unchecked")
                AzureCredentialResolver<TokenCredential> processorResolver =
                    (AzureCredentialResolver<TokenCredential>) processorTokenCredentialResolver;
                TokenCredential processorResolvedCredential = processorResolver.resolve(serviceBusProperties);
                assertThat(processorResolvedCredential)
                    .as("The resolved credential in binder's processor factory should be the customTokenCredential bean")
                    .isSameAs(customCredential);
            });
    }

    @Configuration
    public static class CustomTokenCredentialConfiguration {
        @Bean
        public TokenCredential customTokenCredential() {
            return mock(TokenCredential.class);
        }
    }

}
