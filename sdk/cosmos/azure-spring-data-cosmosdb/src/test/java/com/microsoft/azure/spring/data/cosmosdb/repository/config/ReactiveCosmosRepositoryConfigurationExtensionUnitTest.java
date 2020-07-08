// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.repository.config;

import com.microsoft.azure.spring.data.cosmosdb.repository.ReactiveCosmosRepository;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfiguration;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

import java.util.Collection;

import static org.assertj.core.api.Assertions.fail;

public class ReactiveCosmosRepositoryConfigurationExtensionUnitTest {

    StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(Config.class, true);
    ResourceLoader loader = new PathMatchingResourcePatternResolver();
    Environment environment = new StandardEnvironment();
    RepositoryConfigurationSource configurationSource = new AnnotationRepositoryConfigurationSource(metadata,
            EnableReactiveCosmosRepositories.class, loader, environment, new DefaultListableBeanFactory());

    private static void assertHashRepo(Class<?> repositoryInterface,
                                       Collection<RepositoryConfiguration<RepositoryConfigurationSource>> configs) {
        for (final RepositoryConfiguration<?> config : configs) {
            if (config.getRepositoryInterface().equals(repositoryInterface.getName())) {
                return;
            }
        }

        fail("expected to find config for repository interface "
                + repositoryInterface.getName() + ", but got: " + configs.toString());
    }

    @Test
    public void isStrictMatchIfRepositoryExtendsStoreSpecificBase() {
        final ReactiveCosmosRepositoryConfigurationExtension extension =
            new ReactiveCosmosRepositoryConfigurationExtension();
        assertHashRepo(TestRepository.class,
            extension.getRepositoryConfigurations(configurationSource, loader, true));
    }

    interface TestRepository extends ReactiveCosmosRepository<Object, String> {
    }

    @EnableReactiveCosmosRepositories(considerNestedRepositories = true)
    static class Config {

    }
}
