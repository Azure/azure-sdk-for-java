// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.aot.BeanRegistrationExcludeFilter;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.HashMap;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.AZURE_GLOBAL_PROPERTY_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.SPRING_TOKEN_CREDENTIAL_PROVIDER_CONTEXT_BEAN_NAME;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link AzureGlobalProperties}.
 *
 * @since 4.0.0
 */
@Import(AzureGlobalPropertiesAutoConfiguration.Registrar.class)
public class AzureGlobalPropertiesAutoConfiguration {

    static class Registrar implements EnvironmentAware, BeanClassLoaderAware, ImportBeanDefinitionRegistrar {

        private Environment environment;
        private ClassLoader classLoader;

        private static final String AZURE_AUTHENTICATION_TEMPLATE_CLASS_NAME =
            "com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate";

        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            if (!registry.containsBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME)) {
                BeanDefinitionBuilder definitionBuilder = genericBeanDefinition(AzureGlobalProperties.class,
                    () -> Binder.get(this.environment)
                                .bindOrCreate(AzureGlobalProperties.PREFIX,
                                    AzureGlobalProperties.class));
                if (evaluateNonBinderCondition(registry)) {
                    definitionBuilder.addDependsOn(SPRING_TOKEN_CREDENTIAL_PROVIDER_CONTEXT_BEAN_NAME);
                }
                registry.registerBeanDefinition(AZURE_GLOBAL_PROPERTY_BEAN_NAME, definitionBuilder.getBeanDefinition());
            }
        }

        private boolean evaluateNonBinderCondition(BeanDefinitionRegistry registry) {
            if (ClassUtils.isPresent(AZURE_AUTHENTICATION_TEMPLATE_CLASS_NAME, this.classLoader)) {
                if (registry instanceof DefaultListableBeanFactory beanFactory) {
                    if (this.environment instanceof StandardEnvironment standardEnvironment) {
                        MutablePropertySources propertySources = standardEnvironment.getPropertySources();
                        PropertySource<?> firstPropertySource = propertySources.iterator().next();
                        if (firstPropertySource instanceof MapPropertySource mapPropertySource
                            && mapPropertySource.getSource() instanceof HashMap<String, Object> maybeBinderProperties) {
                            if (maybeBinderProperties.isEmpty() && beanFactory.getParentBeanFactory() != null) {
                                return false;
                            }
                        }

                        if (beanFactory.containsBean("outerContext")
                            && beanFactory.getBean("outerContext").getClass() == AnnotationConfigApplicationContext.class) {
                            return false;
                        }

                        return !firstPropertySource.getName().equals("defaultBinderFactoryProperties")
                            && propertySources.stream().anyMatch(src -> src.getName().equals("configurationProperties"));
                    }
                }
            }
            return false;
        }
    }

    static class AzureGlobalPropertiesBeanRegistrationExcludeFilter implements BeanRegistrationExcludeFilter {

        @Override
        public boolean isExcludedFromAotProcessing(RegisteredBean registeredBean) {
            return AZURE_GLOBAL_PROPERTY_BEAN_NAME.equals(registeredBean.getBeanName());
        }

    }
}
