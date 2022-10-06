// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.config.BinderFactoryAutoConfiguration;
import org.springframework.cloud.stream.config.BinderProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.integration.support.utils.IntegrationUtils;

import static com.azure.spring.cloud.autoconfigure.kafka.AzureKafkaSpringCloudStreamConfiguration.AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS;
import static com.azure.spring.cloud.autoconfigure.kafka.BindingServicePropertiesBeanPostProcessor.SPRING_MAIN_SOURCES_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AzureEventHubsKafkaBinderOAuth2AutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class, BinderFactoryAutoConfiguration.class))
            // Required by the init method of BindingServiceProperties
            .withBean(IntegrationUtils.INTEGRATION_CONVERSION_SERVICE_BEAN_NAME, ConversionServiceFactoryBean.class,
                    ConversionServiceFactoryBean::new);

    @Test
    void shouldNotConfigureWithoutKafkaBinderConfigurationClass() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(KafkaBinderConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(BindingServicePropertiesBeanPostProcessor.class);
                });
    }

    @Test
    void shouldConfigureWhenKafkaDisabled() {
        this.contextRunner
                .withPropertyValues("spring.cloud.azure.eventhubs.kafka.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class));
    }

    @Test
    void shouldConfigureWithKafkaBinderConfigurationClass() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);
                });
    }

    @Test
    void shouldConfigureWhenBinderNameSpecified() {
        this.contextRunner
                .withPropertyValues("spring.cloud.stream.binders.kafka.environment.key=value")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);
                });
    }

    @Test
    void shouldConfigureWhenBinderTypeSpecified() {
        this.contextRunner
                .withPropertyValues("spring.cloud.stream.binders.custom-binder.type=kafka")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    testBinderSources(context.getBean(BindingServiceProperties.class), "custom-binder", AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);
                });
    }

    @Test
    void shouldConfigureWithMultipleBinders() {
        this.contextRunner
                .withPropertyValues(
                        "spring.cloud.stream.binders.kafka-binder-1.type=kafka",
                        "spring.cloud.stream.binders.kafka-binder-2.type=kafka",
                        "spring.cloud.stream.binders.rabbit-binder.environment.key=value"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    BindingServiceProperties bindingServiceProperties = context.getBean(BindingServiceProperties.class);
                    testBinderSources(bindingServiceProperties, "kafka-binder-1", AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);
                    testBinderSources(bindingServiceProperties, "kafka-binder-2", AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS);
                    assertNotEquals(AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS, bindingServiceProperties.getBinders().get("rabbit-binder").getEnvironment().get(SPRING_MAIN_SOURCES_PROPERTY));
                });
    }

    @Test
    void shouldAppendOriginalSources() {

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class))
                .withBean(IntegrationUtils.INTEGRATION_CONVERSION_SERVICE_BEAN_NAME, ConversionServiceFactoryBean.class,
                        ConversionServiceFactoryBean::new)
                .withBean(BindingServiceProperties.class, () -> {
                    BindingServiceProperties bindingServiceProperties = new BindingServiceProperties();
                    BinderProperties kafkaBinderSourceProperty = new BinderProperties();
                    kafkaBinderSourceProperty.getEnvironment().put(SPRING_MAIN_SOURCES_PROPERTY, "test");
                    bindingServiceProperties.getBinders().put("kafka", kafkaBinderSourceProperty);
                    return bindingServiceProperties;
                })
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", AZURE_KAFKA_SPRING_CLOUD_STREAM_CONFIGURATION_CLASS + ",test");
                });
    }

    private void testBinderSources(BindingServiceProperties bindingServiceProperties, String binderName, String binderSources) {
        assertFalse(bindingServiceProperties.getBinders().isEmpty());
        assertNotNull(bindingServiceProperties.getBinders().get(binderName));
        assertEquals(binderSources,
                bindingServiceProperties.getBinders().get(binderName).getEnvironment().get(SPRING_MAIN_SOURCES_PROPERTY));
    }


}
