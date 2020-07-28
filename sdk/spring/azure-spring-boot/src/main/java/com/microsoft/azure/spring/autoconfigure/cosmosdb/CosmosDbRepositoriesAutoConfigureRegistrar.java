// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.repository.config.CosmosRepositoryConfigurationExtension;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.EnableCosmosRepositories;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Declare {@link EnableCosmosRepositories} annotation and {@link EnableCosmosDbRepositoriesConfiguration} configuration for default non-async auto-configuration.
 */
public class CosmosDbRepositoriesAutoConfigureRegistrar extends AbstractRepositoryConfigurationSourceSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableCosmosRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableCosmosDbRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new CosmosRepositoryConfigurationExtension();
    }

    @EnableCosmosRepositories
    private static class EnableCosmosDbRepositoriesConfiguration {

    }

}
