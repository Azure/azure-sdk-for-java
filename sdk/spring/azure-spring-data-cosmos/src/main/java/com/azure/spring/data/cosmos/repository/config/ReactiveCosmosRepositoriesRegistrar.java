// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

/**
 * Inherit {@link RepositoryBeanDefinitionRegistrarSupport} class to enable repository and get extension
 */
public class ReactiveCosmosRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableReactiveCosmosRepositories.class;
    }

    @Override
    protected RepositoryConfigurationExtension getExtension() {
        return new ReactiveCosmosRepositoryConfigurationExtension();
    }


}
