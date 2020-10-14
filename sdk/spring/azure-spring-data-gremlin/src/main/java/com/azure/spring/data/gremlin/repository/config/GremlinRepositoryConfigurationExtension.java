// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.config;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.mapping.GremlinMappingContext;
import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

public class GremlinRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    @Override
    public String getModuleName() {
        return Constants.GREMLIN_MODULE_NAME;
    }

    @Override
    public String getModulePrefix() {
        return Constants.GREMLIN_MODULE_PREFIX;
    }

    @Override
    public String getRepositoryFactoryBeanClassName() {
        throw new NotImplementedException("Gremlin RepositoryFactoryBean is not implemented");
    }

    @Override
    public Collection<Class<?>> getIdentifyingTypes() {
        return Collections.singleton(GremlinRepository.class);
    }

    @Override
    public Collection<Class <? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {
        super.registerBeansForRoot(registry, config);

        if (!registry.containsBeanDefinition(Constants.GREMLIN_MAPPING_CONTEXT)) {
            final RootBeanDefinition definition = new RootBeanDefinition(GremlinMappingContext.class);

            definition.setRole(AbstractBeanDefinition.ROLE_INFRASTRUCTURE);
            definition.setSource(config.getSource());

            registry.registerBeanDefinition(Constants.GREMLIN_MAPPING_CONTEXT, definition);
        }
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
        super.postProcess(builder, source);
    }
}

