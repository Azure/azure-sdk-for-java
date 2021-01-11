// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.support;

import com.azure.spring.data.cosmos.core.CosmosOperations;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import org.springframework.beans.factory.FactoryBean;
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
        extends RepositoryFactoryBeanSupport<T, S, ID> {

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
    public void setCosmosOperations(CosmosOperations operations) {
        this.operations = operations;
    }

    @Override
    protected final RepositoryFactorySupport createRepositoryFactory() {
        return getFactoryInstance();
    }

    protected RepositoryFactorySupport getFactoryInstance() {
        return new CosmosRepositoryFactory(operations);
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
