/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.gremlin;

import com.microsoft.spring.data.gremlin.repository.config.EnableGremlinRepositories;
import com.microsoft.spring.data.gremlin.repository.config.GremlinRepositoryConfigurationExtension;
import org.springframework.boot.autoconfigure.data.AbstractRepositoryConfigurationSourceSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

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
