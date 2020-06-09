// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.support;

import com.microsoft.azure.spring.data.cosmosdb.core.ReactiveCosmosOperations;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosMappingContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.io.Serializable;

public class ReactiveCosmosRepositoryFactoryBean<T extends Repository<S, K>, S,
    K extends Serializable>
    extends RepositoryFactoryBeanSupport<T, S, K>
    implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private ReactiveCosmosOperations cosmosOperations;
    private boolean mappingContextConfigured = false;

    public ReactiveCosmosRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Autowired
    public void setReactiveCosmosOperations(ReactiveCosmosOperations operations) {
        this.cosmosOperations = operations;
    }

    @Override
    protected final RepositoryFactorySupport createRepositoryFactory() {
        return getFactoryInstance(applicationContext);
    }

    protected RepositoryFactorySupport getFactoryInstance(ApplicationContext applicationContext) {
        return new ReactiveCosmosRepositoryFactory(cosmosOperations, applicationContext);
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
            if (cosmosOperations != null) {
                setMappingContext(cosmosOperations.getConverter().getMappingContext());
            } else {
                setMappingContext(new CosmosMappingContext());
            }
        }
    }

}
