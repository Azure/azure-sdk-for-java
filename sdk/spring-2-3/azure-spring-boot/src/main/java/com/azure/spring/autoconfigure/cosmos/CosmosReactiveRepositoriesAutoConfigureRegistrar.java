// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.cosmos;

import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.ReactiveCosmosRepositoryConfigurationExtension;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Declare {@link EnableReactiveCosmosRepositories} annotation and {@link EnableCosmosReactiveRepositoriesConfiguration}
 * configuration for default async auto-configuration.
 */
public class CosmosReactiveRepositoriesAutoConfigureRegistrar extends AbstractRepositoryConfigurationSourceSupport {
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableReactiveCosmosRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableCosmosReactiveRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new ReactiveCosmosRepositoryConfigurationExtension();
    }

    @EnableReactiveCosmosRepositories
    private static class EnableCosmosReactiveRepositoriesConfiguration {

    }

}
