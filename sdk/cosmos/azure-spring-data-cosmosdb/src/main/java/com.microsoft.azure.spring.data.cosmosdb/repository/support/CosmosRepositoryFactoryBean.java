// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.support;

import com.microsoft.azure.spring.data.cosmosdb.core.CosmosOperations;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosMappingContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Adapter for Springs {@link FactoryBean} interface to allow easy setup of cosmos repository factories via Spring
 * configuration.
 */
public class CosmosRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends RepositoryFactoryBeanSupport<T, S, ID>
        implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private CosmosOperations operations;
    private boolean mappingContextConfigured = false;

    /**
     * Creates a new {@link RepositoryFactoryBeanSupport} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public CosmosRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /**
     * Set cosmos operation
     *
     * @param operations for cosmos operations
     */
    @Autowired
    public void setCosmosOperations(CosmosOperations operations) {
        this.operations = operations;
    }

    @Override
    protected final RepositoryFactorySupport createRepositoryFactory() {
        return getFactoryInstance(applicationContext);
    }

    protected RepositoryFactorySupport getFactoryInstance(ApplicationContext applicationContext) {
        return new CosmosRepositoryFactory(operations, applicationContext);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void setMappingContext(MappingContext<?, ?> mappingContext) {
        super.setMappingContext(mappingContext);
        this.mappingContextConfigured = true;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        if (!this.mappingContextConfigured) {
            if (operations != null) {
                setMappingContext(operations.getConverter().getMappingContext());
            } else {
                setMappingContext(new CosmosMappingContext());
            }
        }
    }
}
