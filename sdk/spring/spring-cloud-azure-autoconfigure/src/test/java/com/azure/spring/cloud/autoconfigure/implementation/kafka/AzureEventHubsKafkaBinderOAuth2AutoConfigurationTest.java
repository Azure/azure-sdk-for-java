// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.cloud.stream.config.BinderFactoryAutoConfiguration;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.integration.support.utils.IntegrationUtils;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.BindingServicePropertiesBeanPostProcessor.KAFKA_OAUTH2_SPRING_MAIN_SOURCES;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.BindingServicePropertiesBeanPostProcessor.SPRING_MAIN_SOURCES_PROPERTY;
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
    private final BindingServicePropertiesBeanPostProcessor bpp = new BindingServicePropertiesBeanPostProcessor();

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

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", KAFKA_OAUTH2_SPRING_MAIN_SOURCES);
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

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", KAFKA_OAUTH2_SPRING_MAIN_SOURCES);
                    assertEquals("value", context.getBean(BindingServiceProperties.class).getBinders().get("kafka").getEnvironment().get("key"));
                });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureWhenOtherSpringEnvironmentSpecified() {
        this.contextRunner
                .withPropertyValues("spring.cloud.stream.binders.kafka.environment.spring.profiles.active=value")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", KAFKA_OAUTH2_SPRING_MAIN_SOURCES);
                    assertEquals("value", ((Map<String, Map<String, Object>>) context.getBean(BindingServiceProperties.class).getBinders().get("kafka").getEnvironment().get("spring"))
                            .get("profiles").get("active"));
                });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldConfigureWhenOtherSpringMainEnvironmentSpecified() {
        this.contextRunner
                .withPropertyValues("spring.cloud.stream.binders.kafka.environment.spring.main.banner-mode=console")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", KAFKA_OAUTH2_SPRING_MAIN_SOURCES);
                    assertEquals("console", ((Map<String, Map<String, Object>>) context.getBean(BindingServiceProperties.class).getBinders().get("kafka").getEnvironment().get("spring"))
                            .get("main").get("banner-mode"));
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

                    testBinderSources(context.getBean(BindingServiceProperties.class), "custom-binder", KAFKA_OAUTH2_SPRING_MAIN_SOURCES);
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
                    testBinderSources(bindingServiceProperties, "kafka-binder-1", KAFKA_OAUTH2_SPRING_MAIN_SOURCES);
                    testBinderSources(bindingServiceProperties, "kafka-binder-2", KAFKA_OAUTH2_SPRING_MAIN_SOURCES);
                    assertNotEquals(KAFKA_OAUTH2_SPRING_MAIN_SOURCES, bindingServiceProperties.getBinders().get("rabbit-binder").getEnvironment().get(SPRING_MAIN_SOURCES_PROPERTY));
                });
    }

    @Test
    void shouldAppendOriginalSources() {
        this.contextRunner
                .withPropertyValues("spring.cloud.stream.binders.kafka.environment.spring.main.sources=value")
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureEventHubsKafkaBinderOAuth2AutoConfiguration.class);
                    assertThat(context).hasSingleBean(BindingServicePropertiesBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(BindingServiceProperties.class);

                    testBinderSources(context.getBean(BindingServiceProperties.class), "kafka", KAFKA_OAUTH2_SPRING_MAIN_SOURCES + ",value");
                });
    }

    private void testBinderSources(BindingServiceProperties bindingServiceProperties, String binderName, String binderSources) {
        assertFalse(bindingServiceProperties.getBinders().isEmpty());
        assertNotNull(bindingServiceProperties.getBinders().get(binderName));
        assertEquals(binderSources, bpp.getOrCreateSpringMainPropertiesMap(bindingServiceProperties.getBinders().get(binderName).getEnvironment()).get("sources"));
    }


}
