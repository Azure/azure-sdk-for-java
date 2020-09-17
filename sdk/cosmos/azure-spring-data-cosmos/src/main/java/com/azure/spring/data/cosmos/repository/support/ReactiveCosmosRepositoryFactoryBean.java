// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.ReactiveCosmosOperations;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

/**
 * Adapter for Springs {@link FactoryBean} interface to allow easy setup of reactive cosmos repository factories
 * via Spring configuration.
 */
public class ReactiveCosmosRepositoryFactoryBean<T extends Repository<S, K>, S,
    K extends Serializable>
    extends RepositoryFactoryBeanSupport<T, S, K> {

    private ReactiveCosmosOperations cosmosOperations;
    private boolean mappingContextConfigured = false;

    /**
     * Creates a new {@link RepositoryFactoryBeanSupport} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    public ReactiveCosmosRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    /**
     * Set reactive CosmosDB operations
     *
     * @param operations contains cosmos operations
     */
    public void setReactiveCosmosOperations(ReactiveCosmosOperations operations) {
        this.cosmosOperations = operations;
    }

    @Override
    protected final RepositoryFactorySupport createRepositoryFactory() {
        return getFactoryInstance();
    }

    protected RepositoryFactorySupport getFactoryInstance() {
        return new ReactiveCosmosRepositoryFactory(cosmosOperations);
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
            if (cosmosOperations != null) {
                setMappingContext(cosmosOperations.getConverter().getMappingContext());
            } else {
                setMappingContext(new CosmosMappingContext());
            }
        }
    }

}
