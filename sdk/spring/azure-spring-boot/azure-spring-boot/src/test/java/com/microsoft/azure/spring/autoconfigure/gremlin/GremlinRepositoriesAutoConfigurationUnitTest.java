/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.gremlin;

import com.microsoft.azure.spring.autoconfigure.cosmosdb.TestAutoConfigurationPackage;
import com.microsoft.azure.spring.autoconfigure.gremlin.domain.User;
import com.microsoft.azure.spring.autoconfigure.gremlin.domain.UserRepository;
import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.query.GremlinTemplate;
import com.microsoft.spring.data.gremlin.repository.config.EnableGremlinRepositories;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

@RunWith(MockitoJUnitRunner.class)
public class GremlinRepositoriesAutoConfigurationUnitTest {

    private AnnotationConfigApplicationContext applicationContext;

    @InjectMocks
    private GremlinTemplate template;

    @Mock
    private GremlinFactory factory;

    @Mock
    private MappingGremlinConverter converter;

    @After
    public void cleanup() {
        if (this.applicationContext != null) {
            this.applicationContext.close();
        }
    }

    private void initializeApplicationContext(Class<?>... classes) {
        this.applicationContext = new AnnotationConfigApplicationContext();
        this.applicationContext.register(classes);
        this.applicationContext.register(GremlinRepositoriesAutoConfiguration.class);
        this.applicationContext.getBeanFactory().registerSingleton(GremlinTemplate.class.getName(), this.template);
        this.applicationContext.refresh();
    }

    @Test
    public void testDefaultRepositoryConfiguration() {
        initializeApplicationContext(TestConfiguration.class);

        Assert.assertNotNull(this.applicationContext.getBean(UserRepository.class));
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void testInvalidRepositoryConfiguration() {
        initializeApplicationContext(InvalidConfiguration.class);

        this.applicationContext.getBean(UserRepository.class);
    }

    @Configuration
    @TestAutoConfigurationPackage(User.class)
    protected static class TestConfiguration {

    }

    @Configuration
    @EnableGremlinRepositories("fake.repository")
    @TestAutoConfigurationPackage(GremlinRepositoriesAutoConfigurationUnitTest.class)
    protected static class InvalidConfiguration {

    }
}
