// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.autoconfigure.gremlin;

import com.microsoft.spring.data.gremlin.repository.config.EnableGremlinRepositories;
import com.microsoft.spring.data.gremlin.repository.config.GremlinRepositoryConfigurationExtension;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Declare {@link EnableGremlinRepositories} annotation and {@link EnableGremlinRepositoriesConfiguration} configuration for default non-async auto-configuration.
 */
public class GremlinRepositoriesAutoConfigureRegistrar extends AbstractRepositoryConfigurationSourceSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableGremlinRepositories.class;
    }

    @Override
    protected Class<?> getConfiguration() {
        return EnableGremlinRepositoriesConfiguration.class;
    }

    @Override
    protected RepositoryConfigurationExtension getRepositoryConfigurationExtension() {
        return new GremlinRepositoryConfigurationExtension();
    }

    @EnableGremlinRepositories
    private static class EnableGremlinRepositoriesConfiguration {

    }
}
