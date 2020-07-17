// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.config;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.common.ExpressionResolver;
import com.azure.spring.data.cosmos.common.TestConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;

public class AbstractCosmosConfigurationIT {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void containsExpressionResolver() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            TestCosmosConfiguration.class);

        assertNotNull(context.getBean(ExpressionResolver.class));
    }

    @Test
    public void containsCosmosDbFactory() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            TestCosmosConfiguration.class);

        Assertions.assertThat(context.getBean(CosmosFactory.class)).isNotNull();
    }

    @Test(expected = NoSuchBeanDefinitionException.class)
    public void defaultObjectMapperBeanNotExists() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            TestCosmosConfiguration.class);

        context.getBean(ObjectMapper.class);
    }

    @Test
    public void objectMapperIsConfigurable() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            ObjectMapperConfiguration.class);

        Assertions.assertThat(context.getBean(ObjectMapper.class)).isNotNull();
        Assertions.assertThat(context.getBean(Constants.OBJECT_MAPPER_BEAN_NAME)).isNotNull();
    }

    @Test
    public void testCosmosClientBuilderConfigurable() throws IllegalAccessException {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            RequestOptionsConfiguration.class);
        final CosmosFactory factory = context.getBean(CosmosFactory.class);

        Assertions.assertThat(factory).isNotNull();

        final CosmosClientBuilder cosmosClientBuilder = factory.getConfig().getCosmosClientBuilder();

        Assertions.assertThat(cosmosClientBuilder).isNotNull();
        final Field consistencyLevelField = FieldUtils.getDeclaredField(CosmosClientBuilder.class,
            "desiredConsistencyLevel", true);
        ConsistencyLevel consistencyLevel =
            (ConsistencyLevel) consistencyLevelField.get(cosmosClientBuilder);
        Assertions.assertThat(consistencyLevel).isEqualTo(ConsistencyLevel.CONSISTENT_PREFIX);
    }

    @Configuration
    @PropertySource(value = { "classpath:application.properties" })
    static class TestCosmosConfiguration extends AbstractCosmosConfiguration {

        @Value("${cosmos.uri:}")
        private String cosmosDbUri;

        @Value("${cosmos.key:}")
        private String cosmosDbKey;

        @Value("${cosmosdb.database:}")
        private String database;

        @Mock
        private CosmosAsyncClient mockClient;

        @Bean
        public CosmosConfig getConfig() {
            final String dbName = StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
            return CosmosConfig.builder()
                               .cosmosClientBuilder(new CosmosClientBuilder()
                                   .endpoint(cosmosDbUri)
                                   .key(cosmosDbKey))
                               .database(dbName)
                               .build();
        }

        @Override
        public CosmosAsyncClient cosmosAsyncClient(CosmosFactory cosmosFactory) {
            return mockClient;
        }
    }

    @Configuration
    static class ObjectMapperConfiguration extends TestCosmosConfiguration {
        @Bean(name = Constants.OBJECT_MAPPER_BEAN_NAME)
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Configuration
    @PropertySource(value = { "classpath:application.properties" })
    static class RequestOptionsConfiguration extends AbstractCosmosConfiguration {

        @Value("${cosmos.uri:}")
        private String cosmosDbUri;

        @Value("${cosmos.key:}")
        private String cosmosDbKey;

        @Value("${cosmosdb.database:}")
        private String database;

        @Bean
        public CosmosConfig getConfig() {
            final String dbName = StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
            final CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder()
                .key(cosmosDbKey)
                .endpoint(cosmosDbUri)
                .consistencyLevel(ConsistencyLevel.CONSISTENT_PREFIX);
            return CosmosConfig.builder()
                               .database(dbName)
                               .cosmosClientBuilder(cosmosClientBuilder)
                               .build();
        }

    }
}
