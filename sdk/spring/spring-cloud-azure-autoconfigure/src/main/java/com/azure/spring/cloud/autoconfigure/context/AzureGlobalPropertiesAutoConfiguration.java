// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.context;

import com.azure.core.util.ConfigurationBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.AzureSpringIdentifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.AZURE_GLOBAL_PROPERTY_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.CONFIGURATION_BUILDER_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.CONFIGURATION_BUILDER_FOR_INTEGRATION_BEAN_NAME;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * Automatic configuration class of {@link AzureGlobalProperties} for global configuration of Azure Spring
 * libraries.
 */
@Import(AzureGlobalPropertiesAutoConfiguration.Registrar.class)
public class AzureGlobalPropertiesAutoConfiguration {

    static class Registrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {
        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(new SdkPropertySource(environment, false)).root(AzureGlobalProperties.PREFIX);
            ConfigurationBuilder configurationBuilderForIntegrations = new ConfigurationBuilder(new SdkPropertySource(environment, true)).root(AzureGlobalProperties.PREFIX);


            if (!registry.containsBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME)) {
                registry.registerBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME,
                                                genericBeanDefinition(AzureGlobalProperties.class,
                                                                      () -> Binder.get(this.environment)
                                                                                  .bindOrCreate(AzureGlobalProperties.PREFIX,
                                                                                                AzureGlobalProperties.class))
                                                    .getBeanDefinition());
                registry.registerBeanDefinition(CONFIGURATION_BUILDER_BEAN_NAME, genericBeanDefinition(ConfigurationBuilder.class,  () -> configurationBuilder).getBeanDefinition());
                registry.registerBeanDefinition(CONFIGURATION_BUILDER_FOR_INTEGRATION_BEAN_NAME, genericBeanDefinition(ConfigurationBuilder.class,  () -> configurationBuilderForIntegrations).getBeanDefinition());
            }
        }

    }

    public static class SdkPropertySource implements com.azure.core.util.ConfigurationSource {

        private static final Properties APPLICATION_IDS = new Properties() {{
            put("spring.cloud.azure.appconfiguration.http.client.application-id", AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG);
            put("spring.cloud.azure.storage.blob.http.client.application-id", AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB);
            put("spring.cloud.azure.eventhubs.processor.checkpoint-store.http.client.application-id", AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB);
        }};

        private static Properties APPLICAITON_IDS_NOT_INTEGRATIONS;
        private static Properties APPLICATION_IDS_INTEGRATIONS;
        static {
            APPLICAITON_IDS_NOT_INTEGRATIONS = new Properties(APPLICATION_IDS);
            APPLICAITON_IDS_NOT_INTEGRATIONS.put("spring.cloud.azure.servicebus.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            APPLICAITON_IDS_NOT_INTEGRATIONS.put("spring.cloud.azure.servicebus.consumer.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            APPLICAITON_IDS_NOT_INTEGRATIONS.put("spring.cloud.azure.servicebus.producer.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);
            APPLICAITON_IDS_NOT_INTEGRATIONS.put("spring.cloud.azure.servicebus.processor.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_SERVICE_BUS);

            APPLICATION_IDS_INTEGRATIONS = new Properties(APPLICATION_IDS);
            APPLICATION_IDS_INTEGRATIONS.put("spring.cloud.azure.servicebus.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
            APPLICATION_IDS_INTEGRATIONS.put("spring.cloud.azure.servicebus.consumer.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
            APPLICATION_IDS_INTEGRATIONS.put("spring.cloud.azure.servicebus.producer.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
            APPLICATION_IDS_INTEGRATIONS.put("spring.cloud.azure.servicebus.processor.amqp.client.application-id", AzureSpringIdentifier.AZURE_SPRING_INTEGRATION_SERVICE_BUS);
        }

        private final Environment env;
        private final PropertiesPropertySource applicaitonIdSource;

        public SdkPropertySource(Environment env, boolean integrations) {
            this.env = env;

            applicaitonIdSource = new PropertiesPropertySource("application-id-source", integrations ? APPLICATION_IDS_INTEGRATIONS : APPLICAITON_IDS_NOT_INTEGRATIONS);
        }

        @Override
        public Set<String> getChildKeys(String path) {
            MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
            propSrcs.addFirst(applicaitonIdSource);
            return StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof EnumerablePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::<String>stream)
                .filter(propName -> propName.startsWith(path) && propName.length() > path.length() && propName.charAt(path.length()) == '.')
                .collect(Collectors.toSet());
        }

        @Override
        public String getValue(String propertyName) {
            return env.getProperty(propertyName);
        }
    }
}
