// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryCustomizer;
import com.azure.spring.cloud.autoconfigure.jms.AzureServiceBusJmsConnectionFactoryFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.aot.test.generate.TestGenerationContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.aot.ApplicationContextAotGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.test.tools.CompileWithForkedClassLoader;
import org.springframework.core.test.tools.TestCompiler;
import org.springframework.javapoet.ClassName;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.azure.spring.cloud.autoconfigure.implementation.util.TestServiceBusUtils.CONNECTION_STRING_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

@Isolated("AOT compilation uses a forked class loader")
class ServiceBusJmsConnectionFactoryAotTests {

    @Test
    @CompileWithForkedClassLoader
    void registersServiceBusFactoryWithRuntimeCustomizations() {
        compileConnectionFactory(Map.of("spring.jms.cache.enabled", "false"), runtime ->
            assertCustomizedFactory(runtime.getBean("jmsConnectionFactory")));
    }

    @Test
    @CompileWithForkedClassLoader
    void registersCachingFactoryWithRuntimeSettings() {
        compileConnectionFactory(Map.of(), runtime -> {
            CachingConnectionFactory factory = runtime.getBean("jmsConnectionFactory", CachingConnectionFactory.class);
            assertThat(factory.isCacheConsumers()).isFalse();
            assertThat(factory.isCacheProducers()).isFalse();
            assertThat(factory.getSessionCacheSize()).isEqualTo(13);
            assertCustomizedFactory(factory.getTargetConnectionFactory());
        });
    }

    @Test
    @CompileWithForkedClassLoader
    void registersPoolingFactoryWithRuntimeSettingsAndStopsOnClose() {
        AtomicReference<JmsPoolConnectionFactory> factory = new AtomicReference<>();
        compileConnectionFactory(Map.of("spring.jms.servicebus.pool.enabled", "true"), runtime -> {
            factory.set(runtime.getBean("jmsPoolConnectionFactory", JmsPoolConnectionFactory.class));
            assertThat(factory.get().getMaxConnections()).isEqualTo(7);
            assertThat(factory.get().getMaxSessionsPerConnection()).isEqualTo(17);
            assertCustomizedFactory(factory.get().getConnectionFactory());
            assertThat(ReflectionTestUtils.getField(factory.get(), "stopped"))
                .isInstanceOfSatisfying(AtomicBoolean.class, stopped -> assertThat(stopped.get()).isFalse());
        });
        assertThat(ReflectionTestUtils.getField(factory.get(), "stopped"))
            .isInstanceOfSatisfying(AtomicBoolean.class, stopped -> assertThat(stopped.get()).isTrue());
    }

    private void assertCustomizedFactory(Object factory) {
        assertThat(factory).isInstanceOfSatisfying(CustomConnectionFactory.class, connectionFactory ->
            assertThat(connectionFactory.getClientId()).isEqualTo("runtime-client-first-second"));
    }

    @SuppressWarnings("unchecked")
    private void compileConnectionFactory(Map<String, Object> branchProperties,
        Consumer<GenericApplicationContext> assertions) {
        Map<String, Object> buildProperties = new HashMap<>(branchProperties);
        buildProperties.put("spring.jms.servicebus.connection-string",
            String.format(CONNECTION_STRING_FORMAT, "test-namespace"));
        buildProperties.put("spring.jms.servicebus.pricing-tier", "premium");
        buildProperties.put("test.factory.client-id", "build-client");
        buildProperties.put("spring.jms.cache.consumers", "true");
        buildProperties.put("spring.jms.cache.producers", "true");
        buildProperties.put("spring.jms.cache.session-cache-size", "3");
        buildProperties.put("spring.jms.servicebus.pool.max-connections", "2");
        buildProperties.put("spring.jms.servicebus.pool.max-sessions-per-connection", "5");
        Map<String, Object> runtimeProperties = new HashMap<>(buildProperties);
        runtimeProperties.put("test.factory.client-id", "runtime-client");
        runtimeProperties.put("spring.jms.cache.consumers", "false");
        runtimeProperties.put("spring.jms.cache.producers", "false");
        runtimeProperties.put("spring.jms.cache.session-cache-size", "13");
        runtimeProperties.put("spring.jms.servicebus.pool.max-connections", "7");
        runtimeProperties.put("spring.jms.servicebus.pool.max-sessions-per-connection", "17");

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("build", buildProperties));
            context.register(FactoryConfiguration.class);
            TestGenerationContext generation = new TestGenerationContext(
                ClassName.get(getClass().getPackageName(), "JmsFactoryAotTest"));
            ClassName initializerName = new ApplicationContextAotGenerator().processAheadOfTime(context, generation);
            generation.writeGeneratedContent();
            TestCompiler.forSystem().with(generation).compile(compiled -> {
                try (GenericApplicationContext runtime = new GenericApplicationContext()) {
                    runtime.getEnvironment().getPropertySources()
                        .addFirst(new MapPropertySource("runtime", runtimeProperties));
                    ApplicationContextInitializer<GenericApplicationContext> initializer = compiled
                        .getInstance(ApplicationContextInitializer.class, initializerName.toString());
                    initializer.initialize(runtime);
                    runtime.refresh();
                    assertions.accept(runtime);
                }
            });
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({AzureServiceBusJmsProperties.class, JmsProperties.class})
    @Import(ServiceBusJmsConnectionFactoryConfiguration.class)
    static class FactoryConfiguration {

        @Bean
        AzureServiceBusJmsConnectionFactoryFactory instanceFactory(Environment environment) {
            return () -> {
                CustomConnectionFactory factory = new CustomConnectionFactory();
                factory.setClientId(environment.getRequiredProperty("test.factory.client-id"));
                return factory;
            };
        }

        @Bean
        @Order(2)
        AzureServiceBusJmsConnectionFactoryCustomizer secondCustomizer() {
            return factory -> factory.setClientId(factory.getClientId() + "-second");
        }

        @Bean
        @Order(1)
        AzureServiceBusJmsConnectionFactoryCustomizer firstCustomizer() {
            return factory -> factory.setClientId(factory.getClientId() + "-first");
        }
    }

    static class CustomConnectionFactory extends ServiceBusJmsConnectionFactory {
        CustomConnectionFactory() {
            super(String.format(CONNECTION_STRING_FORMAT, "test-namespace"), new ServiceBusJmsConnectionFactorySettings());
        }
    }
}
