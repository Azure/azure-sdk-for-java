// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.config;

import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.azure.spring.data.cosmos.repository.support.ReactiveCosmosRepositoryFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

/**
 * Configuration extension class based on {@link RepositoryConfigurationExtensionSupport} provide options to set
 * reactive repository support.
 */
public class ReactiveCosmosRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    @Override
    public String getModuleName() {
        return Constants.COSMOS_MODULE_NAME;
    }

    @Override
    public String getModulePrefix() {
        return Constants.COSMOS_MODULE_PREFIX;
    }

    /**
     * Return the name of the repository factory bean class.
     * @return String value of bean name
     */
    public String getRepositoryFactoryBeanClassName() {
        return ReactiveCosmosRepositoryFactoryBean.class.getName();
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.<Class<?>>singleton(ReactiveCosmosRepository.class);
    }

    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.emptyList();
    }


    @Override
    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {
        super.registerBeansForRoot(registry, config);
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource source) {
        final AnnotationAttributes attributes = source.getAttributes();
        builder.addPropertyReference("reactiveCosmosOperations", attributes.getString("reactiveCosmosTemplateRef"));
    }

    //  Overriding this to provide reactive repository support.
    @Override
    protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
        return metadata.isReactiveRepository();
    }
}
