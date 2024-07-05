// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.AZURE_GLOBAL_PROPERTY_BEAN_NAME;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link AzureGlobalProperties}.
 *
 * @since 4.0.0
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
            if (!registry.containsBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME)) {
                registry.registerBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME,
                                                genericBeanDefinition(AzureGlobalProperties.class,
                                                                      () -> Binder.get(this.environment)
                                                                                  .bindOrCreate(AzureGlobalProperties.PREFIX,
                                                                                                AzureGlobalProperties.class))
                                                    .getBeanDefinition());
            }
        }

    }

    static class AzureGlobalPropertiesBeanRegistrationExcludeFilter implements BeanRegistrationExcludeFilter {

        @Override
        public boolean isExcludedFromAotProcessing(RegisteredBean registeredBean) {
            return AZURE_GLOBAL_PROPERTY_BEAN_NAME.equals(registeredBean.getBeanName());
        }

    }
}
