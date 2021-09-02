// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Automatic configuration class of {@link AzureProperties} for unified configuration of Azure Spring libraries.
 */
@Import(AzurePropertyAutoConfiguration.Registrar.class)
public class AzurePropertyAutoConfiguration {

    public static final String AZURE_PROPERTY_BEAN_NAME = "azureProperties";

    public static final String PREFIX = "spring.cloud.azure";

    static class Registrar implements EnvironmentAware, ImportBeanDefinitionRegistrar {
        private Environment environment;

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition(AZURE_PROPERTY_BEAN_NAME)) {
                registry.registerBeanDefinition(AZURE_PROPERTY_BEAN_NAME,
                    BeanDefinitionBuilder.genericBeanDefinition(AzureProperties.class,
                        () -> Binder.get(this.environment)
                                    .bindOrCreate(PREFIX, AzureProperties.class)).getBeanDefinition());
            }
        }

    }
}
