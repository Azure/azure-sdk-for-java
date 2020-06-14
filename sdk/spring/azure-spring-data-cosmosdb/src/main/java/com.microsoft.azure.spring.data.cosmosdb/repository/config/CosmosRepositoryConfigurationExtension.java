// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.repository.config;

import com.microsoft.azure.spring.data.cosmosdb.Constants;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosMappingContext;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosRepositoryFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

/**
 * Configuration extension class based on {@link RepositoryConfigurationExtensionSupport} provide options to set
 * repository support.
 */
public class CosmosRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {

    @Override
    public String getModuleName() {
        return Constants.COSMOSDB_MODULE_NAME;
    }

    @Override
    public String getModulePrefix() {
        return Constants.COSMOSDB_MODULE_PREFIX;
    }

    /**
     * Return the name of the repository factory bean class.
     * @return String value of bean name
     */
    public String getRepositoryFactoryBeanClassName() {
        return CosmosRepositoryFactoryBean.class.getName();
    }

    @Override
    protected Collection<Class<?>> getIdentifyingTypes() {
        return Collections.<Class<?>>singleton(CosmosRepository.class);
    }

    @Override
    protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
        return Collections.emptyList();
    }


    @Override
    public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource config) {
        super.registerBeansForRoot(registry, config);

        if (!registry.containsBeanDefinition(Constants.COSMOS_MAPPING_CONTEXT)) {
            final RootBeanDefinition definition = new RootBeanDefinition(CosmosMappingContext.class);
            definition.setRole(AbstractBeanDefinition.ROLE_INFRASTRUCTURE);
            definition.setSource(config.getSource());

            registry.registerBeanDefinition(Constants.COSMOS_MAPPING_CONTEXT, definition);
        }
    }

    @Override
    public void postProcess(BeanDefinitionBuilder builder, RepositoryConfigurationSource source) {
        super.postProcess(builder, source);
    }

    //  Overriding this to provide reactive repository support.
    @Override
    protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
        //  CosmosRepository is the sync repository, and hence returning !isReactiveRepository.
        //  ReactiveCosmosRepository is reactive repository.
        return !metadata.isReactiveRepository();
    }
}
