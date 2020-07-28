// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.repository.config.EnableReactiveCosmosRepositories;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.ReactiveCosmosRepositoryConfigurationExtension;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Declare {@link EnableReactiveCosmosRepositories} annotation and {@link EnableCosmosDbReactiveRepositoriesConfiguration} configuration for default async auto-configuration.
 */
public class CosmosDbReactiveRepositoriesAutoConfigureRegistrar extends AbstractRepositoryConfigurationSourceSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableReactiveCosmosRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableCosmosDbReactiveRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new ReactiveCosmosRepositoryConfigurationExtension();
    }

    @EnableReactiveCosmosRepositories
    private static class EnableCosmosDbReactiveRepositoriesConfiguration {

    }

}
