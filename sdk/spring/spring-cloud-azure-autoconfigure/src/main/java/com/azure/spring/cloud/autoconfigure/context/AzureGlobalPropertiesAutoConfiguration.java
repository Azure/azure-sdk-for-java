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
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder(new SdkPropertySource(environment)).root(AzureGlobalProperties.PREFIX);

            if (!registry.containsBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME)) {
                registry.registerBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME,
                                                genericBeanDefinition(AzureGlobalProperties.class,
                                                                      () -> Binder.get(this.environment)
                                                                                  .bindOrCreate(AzureGlobalProperties.PREFIX,
                                                                                                AzureGlobalProperties.class))
                                                    .getBeanDefinition());
                registry.registerBeanDefinition("ConfigurationBuilder", genericBeanDefinition(ConfigurationBuilder.class,  () -> configurationBuilder).getBeanDefinition());
            }
        }

    }

    public static class SdkPropertySource implements com.azure.core.util.ConfigurationSource {

        private final Environment env;
        public SdkPropertySource(Environment env) {
            this.env = env;
        }

        private final static Properties APPLICATION_IDS = new Properties() {{
            put("spring.cloud.azure.appconfiguration.http.client.application-id", AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG);
            put("spring.cloud.azure.storage.blob.http.client.application-id", AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB);
            put("spring.cloud.azure.eventhubs.processor.checkpoint-store.http.client.application-id", AzureSpringIdentifier.AZURE_SPRING_STORAGE_BLOB);
        }};

        @Override
        public Set<String> getChildKeys(String path) {
            MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
            propSrcs.addFirst(new PropertiesPropertySource("application-id-source", APPLICATION_IDS));
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
