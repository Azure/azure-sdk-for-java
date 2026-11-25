// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.implementation.config;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.implementation.EventHubsMessageChannelBinder;
import org.junit.jupiter.api.Test;
import org.springframework.aot.test.generate.TestGenerationContext;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.stream.binder.BinderChildContextInitializer;
import org.springframework.cloud.stream.binder.BinderType;
import org.springframework.cloud.stream.binder.BinderTypeRegistry;
import org.springframework.cloud.stream.binder.DefaultBinderFactory;
import org.springframework.cloud.stream.binder.DefaultBinderTypeRegistry;
import org.springframework.cloud.stream.config.BindingServiceConfiguration;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.aot.ApplicationContextAotGenerator;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.test.tools.CompileWithForkedClassLoader;
import org.springframework.core.test.tools.TestCompiler;
import org.springframework.javapoet.ClassName;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.AZURE_GLOBAL_PROPERTY_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;

class EventHubsBinderAotTests {

    @Test
    @CompileWithForkedClassLoader
    @SuppressWarnings("unchecked")
    void generatedBinderChildContextRegistersGlobalProperties() {
        Map<String, Object> properties = Map.of(
            "spring.cloud.stream.binders.eventhubs.type", "eventhubs",
            "spring.cloud.stream.binders.eventhubs.environment.spring.cloud.azure.eventhubs.namespace", "aot-test",
            "spring.cloud.stream.binders.eventhubs.environment.spring.cloud.azure.credential.client-id", "child-client");
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
            context.register(BinderConfiguration.class);
            TestGenerationContext generationContext = new TestGenerationContext(
                ClassName.get(getClass().getPackageName(), "EventHubsAotTest"));
            ClassName initializerName = new ApplicationContextAotGenerator()
                .processAheadOfTime(context, generationContext);
            generationContext.writeGeneratedContent();
            TestCompiler.forSystem().with(generationContext).compile(compiled -> {
                try (GenericApplicationContext runtime = new GenericApplicationContext()) {
                    runtime.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
                    ApplicationContextInitializer<GenericApplicationContext> initializer = compiled
                        .getInstance(ApplicationContextInitializer.class, initializerName.toString());
                    initializer.initialize(runtime);
                    runtime.refresh();
                    DefaultBinderFactory factory = runtime.getBean(DefaultBinderFactory.class);
                    assertThat(ReflectionTestUtils.getField(factory, "binderChildContextInitializers"))
                        .asInstanceOf(MAP).containsKey("eventhubs");
                    AtomicReference<ConfigurableApplicationContext> childContext = new AtomicReference<>();
                    factory.setListeners(List.of((name, child) -> childContext.set(child)));
                    assertThat(factory.getBinder("eventhubs", MessageChannel.class))
                        .isInstanceOf(EventHubsMessageChannelBinder.class);
                    assertThat(childContext.get()).isNotNull();
                    assertThat(childContext.get().containsLocalBean(AZURE_GLOBAL_PROPERTY_BEAN_NAME)).isTrue();
                    assertThat(childContext.get().getBean(AzureGlobalProperties.class).getCredential().getClientId())
                        .isEqualTo("child-client");
                }
            });
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class BinderConfiguration {

        @Bean
        BinderTypeRegistry binderTypeRegistry() {
            return new DefaultBinderTypeRegistry(Map.of("eventhubs",
                new BinderType("eventhubs", new Class<?>[] {EventHubsBinderConfiguration.class})));
        }

        @Bean
        BinderChildContextInitializer binderChildContextInitializer() {
            return new BinderChildContextInitializer();
        }

        @Bean
        DefaultBinderFactory binderFactory(BinderTypeRegistry registry, BinderChildContextInitializer initializer,
            Environment environment) {
            BindingServiceProperties properties = Binder.get(environment)
                .bindOrCreate("spring.cloud.stream", BindingServiceProperties.class);
            DefaultBinderFactory factory = new DefaultBinderFactory(
                BindingServiceConfiguration.getBinderConfigurations(registry, properties), registry, null);
            initializer.setBinderFactory(factory);
            return factory;
        }
    }
}
