// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.spring.data.cosmos.repository.config.CosmosRepositoryConfigurationExtension;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Declare {@link EnableCosmosRepositories} annotation and {@link EnableCosmosRepositoriesConfiguration} configuration
 * for default non-async auto-configuration.
 */
public class CosmosRepositoriesAutoConfigureRegistrar extends AbstractRepositoryConfigurationSourceSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableCosmosRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableCosmosRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new CosmosRepositoryConfigurationExtension();
    }

    @EnableCosmosRepositories
    private static class EnableCosmosRepositoriesConfiguration {

    }

}
