// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsAutoConfiguration;
import com.azure.spring.cloud.core.provider.connectionstring.StaticConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import com.azure.spring.cloud.resourcemanager.implementation.connectionstring.ArmConnectionStringProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collections;
import java.util.Map;

import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.security.auth.SecurityProtocol.SASL_SSL;
import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("deprecation")
class AzureEventHubsKafkaAutoConfigurationTests {

    static final String CONNECTION_STRING_FORMAT =
        "Endpoint=sb://%s.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaAutoConfiguration.class, KafkaAutoConfiguration.class));
    @Test
    void shouldNotConfigureWhenAzureEventHubsKafkaDisabled() {
        this.contextRunner
                .withPropertyValues("spring.cloud.azure.eventhubs.kafka.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaAutoConfiguration.class));
    }

    @Test
    void shouldNotConfigureWithoutKafkaTemplate() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(KafkaTemplate.class))
                .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaAutoConfiguration.class));
    }

    @Test
    void shouldConfigureWhenStaticConnectionStringProvided() {
        this.contextRunner
                .withUserConfiguration(StaticConnectionStringProviderConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaAutoConfiguration.class);
                    assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                    assertThat(context).hasSingleBean(KafkaPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties bean = context.getBean(KafkaProperties.class);
                    Map<String, String> properties = bean.getProperties();
                    assertThat(bean.getBootstrapServers()).isEqualTo(Collections.singletonList("static-namespace.servicebus.windows.net:9093"));
                    assertThat(properties.get(SECURITY_PROTOCOL_CONFIG)).isEqualTo(SASL_SSL.name());
                    assertThat(properties.get(SASL_MECHANISM)).isEqualTo("PLAIN");
                });
    }

    @Test
    void shouldConfigureWhenConnectionStringProvided() {
        this.contextRunner
                .withPropertyValues(
                        "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "static-namespace")
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaAutoConfiguration.class);
                    assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                    assertThat(context).hasSingleBean(KafkaPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties bean = context.getBean(KafkaProperties.class);
                    Map<String, String> properties = bean.getProperties();
                    assertThat(bean.getBootstrapServers()).isEqualTo(Collections.singletonList("static-namespace.servicebus.windows.net:9093"));
                    assertThat(properties.get(SECURITY_PROTOCOL_CONFIG)).isEqualTo(SASL_SSL.name());
                    assertThat(properties.get(SASL_MECHANISM)).isEqualTo("PLAIN");
                });
    }

    @Test
    void shouldWorkWithEventHubsAutoConfiguration() {
        this.contextRunner
                .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
                .withConfiguration(AutoConfigurations.of(AzureEventHubsAutoConfiguration.class))
                .withPropertyValues(
                        "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "static-namespace")
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaAutoConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubsAutoConfiguration.class);
                    assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                    assertThat(context).hasSingleBean(KafkaPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    assertThat(context).hasBean("eventHubsStaticConnectionStringProvider");
                });
    }

    @Test
    void shouldWorkWithAzureEventHubsAndArm() {
        this.contextRunner
                .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
                .withConfiguration(AutoConfigurations.of(AzureEventHubsAutoConfiguration.class, ArmConnectionStringProviderConfiguration.class))
                .withPropertyValues(
                        "spring.cloud.azure.eventhubs.connection-string=" + String.format(CONNECTION_STRING_FORMAT, "static-namespace")
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaAutoConfiguration.class);
                    assertThat(context).hasSingleBean(AzureEventHubsAutoConfiguration.class);
                    assertThat(context).hasSingleBean(StaticConnectionStringProvider.class);
                    assertThat(context).hasSingleBean(KafkaPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    assertThat(context).hasBean("eventHubsStaticConnectionStringProvider");
                });
    }

    @Test
    void shouldNotConfigureWhenServiceBusStaticConnectionStringProvided() {
        this.contextRunner
                .withUserConfiguration(ServiceBusStaticConnectionStringProviderConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(KafkaPropertiesBeanPostProcessor.class);
                });
    }

    @Test
    void shouldConfigureWhenArmConnectionStringProvided() {
        this.contextRunner
                .withUserConfiguration(ArmConnectionStringProviderConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaAutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaPropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(KafkaProperties.class);

                    KafkaProperties bean = context.getBean(KafkaProperties.class);
                    Map<String, String> properties = bean.getProperties();
                    assertThat(bean.getBootstrapServers()).isEqualTo(Collections.singletonList("arm-namespace.servicebus.windows.net:9093"));
                    assertThat(properties.get(SECURITY_PROTOCOL_CONFIG)).isEqualTo(SASL_SSL.name());
                    assertThat(properties.get(SASL_MECHANISM)).isEqualTo("PLAIN");
                });
    }

    @Test
    void shouldConfigureWhenAnotherKafkaPropertiesProvided() {
        KafkaProperties defaultProperties = new KafkaProperties();
        defaultProperties.setBootstrapServers(Collections.singletonList("default-namespace.servicebus.windows.net:9093"));

        this.contextRunner
                .withBean(KafkaProperties.class, () -> defaultProperties, beanDefinition -> beanDefinition.setPrimary(true))
                .withUserConfiguration(StaticConnectionStringProviderConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaAutoConfiguration.class);
                    assertThat(context).hasSingleBean(KafkaPropertiesBeanPostProcessor.class);
                    Map<String, KafkaProperties> kafkaPropertiesMap = context.getBeansOfType(KafkaProperties.class);
                    assertThat(kafkaPropertiesMap).hasSize(2);

                    KafkaProperties bean = context.getBean(KafkaProperties.class);
                    Map<String, String> properties = bean.getProperties();
                    assertThat(bean.getBootstrapServers()).isEqualTo(Collections.singletonList("static-namespace.servicebus.windows.net:9093"));
                    assertThat(properties.get(SECURITY_PROTOCOL_CONFIG)).isEqualTo(SASL_SSL.name());
                    assertThat(properties.get(SASL_MECHANISM)).isEqualTo("PLAIN");
                });
    }

    @Configuration
    static class StaticConnectionStringProviderConfiguration {

        @Bean
        StaticConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider() {
            return new StaticConnectionStringProvider<>(AzureServiceType.EVENT_HUBS,
                    String.format(CONNECTION_STRING_FORMAT, "static-namespace"));
        }
    }

    @Configuration
    static class ServiceBusStaticConnectionStringProviderConfiguration {

        @Bean
        StaticConnectionStringProvider<AzureServiceType.ServiceBus> connectionStringProvider() {
            return new StaticConnectionStringProvider<>(AzureServiceType.SERVICE_BUS,
                    String.format(CONNECTION_STRING_FORMAT, "servicebus-namespace"));
        }
    }

    @Configuration
    static class ArmConnectionStringProviderConfiguration {

        @Bean
        ArmConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider() {
            return new ArmConnectionStringProvider<AzureServiceType.EventHubs>(null, null) {

                @Override
                public String getConnectionString() {
                    return String.format(CONNECTION_STRING_FORMAT, "arm-namespace");
                }

                @Override
                public AzureServiceType.EventHubs getServiceType() {
                    return AzureServiceType.EVENT_HUBS;
                }
            };
        }
    }


}
