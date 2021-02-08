// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.config;

import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationSource;

public class GremlinRepositoryConfigurationExtensionUnitTest {

    private static final String GREMLIN_MODULE_NAME = "Gremlin";
    private static final String GREMLIN_MODULE_PREFIX = "gremlin";
    private static final String GREMLIN_MAPPING_CONTEXT = "gremlinMappingContext";

    private GremlinRepositoryConfigurationExtension extension;

    @Before
    public void setup() {
        this.extension = new GremlinRepositoryConfigurationExtension();
    }

    @Test
    public void testGremlinRepositoryConfigurationExtensionGetters() {
        Assert.assertEquals(this.extension.getModuleName(), GREMLIN_MODULE_NAME);
        Assert.assertEquals(this.extension.getModulePrefix(), GREMLIN_MODULE_PREFIX);
        Assert.assertEquals(this.extension.getIdentifyingTypes().size(), 1);

        Assert.assertSame(this.extension.getIdentifyingTypes().toArray()[0], GremlinRepository.class);
        Assert.assertTrue(this.extension.getIdentifyingAnnotations().isEmpty());
    }

    @Test
    public void testGremlinRepositoryConfigurationExtensionRegisterBeansForRoot() {
        final ResourceLoader loader = new PathMatchingResourcePatternResolver();
        final Environment environment = new StandardEnvironment();
        final BeanDefinitionRegistry registry = new DefaultListableBeanFactory();
        final StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(GremlinConfig.class, true);
        final RepositoryConfigurationSource config = new AnnotationRepositoryConfigurationSource(metadata,
                EnableGremlinRepositories.class, loader, environment, registry);

        Assert.assertFalse(registry.containsBeanDefinition(GREMLIN_MAPPING_CONTEXT));

        this.extension.registerBeansForRoot(registry, config);

        Assert.assertTrue(registry.containsBeanDefinition(GREMLIN_MAPPING_CONTEXT));
    }

    @Test(expected = NotImplementedException.class)
    public void testGetRepositoryFactoryBeanClassNameException() {
        this.extension.getRepositoryFactoryBeanClassName();
    }

    @EnableGremlinRepositories
    private static class GremlinConfig {

    }
}
