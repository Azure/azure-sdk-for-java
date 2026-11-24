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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractCosmosConfigurationIT {



    @Test
    public void containsExpressionResolver() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            TestCosmosConfiguration.class);

        assertNotNull(context.getBean(ExpressionResolver.class));
    }

    @Test
    public void containsCosmosFactory() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            TestCosmosConfiguration.class);

        assertThat(context.getBean(CosmosFactory.class)).isNotNull();
    }

    @Test
    public void defaultObjectMapperBeanNotExists() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> {
            final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
                TestCosmosConfiguration.class);

            context.getBean(ObjectMapper.class);
        });
    }

    @Test
    public void objectMapperIsConfigurable() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            ObjectMapperConfiguration.class);

        assertThat(context.getBean(ObjectMapper.class)).isNotNull();
        assertThat(context.getBean(Constants.OBJECT_MAPPER_BEAN_NAME)).isNotNull();
    }

    @Test
    public void testCosmosClientBuilderConfigurable() throws IllegalAccessException, NoSuchFieldException {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
            RequestOptionsConfiguration.class);
        final CosmosFactory factory = context.getBean(CosmosFactory.class);

        assertThat(factory).isNotNull();

        final CosmosAsyncClient cosmosAsyncClient =  factory.getCosmosAsyncClient();

        assertThat(cosmosAsyncClient).isNotNull();
        Field desiredConsistencyLevel = cosmosAsyncClient.getClass().getDeclaredField("desiredConsistencyLevel");
        desiredConsistencyLevel.setAccessible(true);
        ConsistencyLevel consistencyLevel =
            (ConsistencyLevel) desiredConsistencyLevel.get(cosmosAsyncClient);
        assertThat(consistencyLevel).isEqualTo(ConsistencyLevel.CONSISTENT_PREFIX);
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

        @Bean
        public CosmosClientBuilder getCosmosClientBuilder() {
            return new CosmosClientBuilder()
                .endpoint(cosmosDbUri)
                .key(cosmosDbKey);
        }

        @Override
        protected String getDatabaseName() {
            return StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
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
        public CosmosClientBuilder getCosmosClientBuilder() {
            return new CosmosClientBuilder()
                .key(cosmosDbKey)
                .endpoint(cosmosDbUri)
                .consistencyLevel(ConsistencyLevel.CONSISTENT_PREFIX);
        }

        @Override
        protected String getDatabaseName() {
            return StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
        }
    }
}
