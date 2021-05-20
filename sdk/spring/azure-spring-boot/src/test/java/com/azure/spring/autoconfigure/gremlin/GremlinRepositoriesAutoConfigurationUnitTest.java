// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.gremlin;

import com.azure.spring.autoconfigure.cosmos.TestAutoConfigurationPackage;
import com.azure.spring.autoconfigure.gremlin.domain.User;
import com.azure.spring.autoconfigure.gremlin.domain.UserRepository;
import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.query.GremlinTemplate;
import com.microsoft.spring.data.gremlin.repository.config.EnableGremlinRepositories;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

public class GremlinRepositoriesAutoConfigurationUnitTest {

    private AnnotationConfigApplicationContext applicationContext;

    @InjectMocks
    private GremlinTemplate template;

    @Mock
    private GremlinFactory factory;

    @Mock
    private MappingGremlinConverter converter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
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

        Assertions.assertNotNull(this.applicationContext.getBean(UserRepository.class));
    }

    @Test
    public void testInvalidRepositoryConfiguration() {
        initializeApplicationContext(InvalidConfiguration.class);

        Assertions.assertThrows(NoSuchBeanDefinitionException.class,
            () -> this.applicationContext.getBean(UserRepository.class));
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
