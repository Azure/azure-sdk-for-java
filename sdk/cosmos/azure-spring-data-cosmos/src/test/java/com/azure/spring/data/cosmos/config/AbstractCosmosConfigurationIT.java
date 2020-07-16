// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.config;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosDbFactory;
import com.azure.spring.data.cosmos.common.ExpressionResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.spring.data.cosmos.common.TestConstants;
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

import static org.junit.Assert.assertNotNull;

public class AbstractCosmosConfigurationIT {
    private static final String OBJECTMAPPER_BEAN_NAME = Constants.OBJECTMAPPER_BEAN_NAME;

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

        Assertions.assertThat(context.getBean(CosmosDbFactory.class)).isNotNull();
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
        Assertions.assertThat(context.getBean(OBJECTMAPPER_BEAN_NAME)).isNotNull();
    }

    @Test
    public void testRequestOptionsConfigurable() {
        final AbstractApplicationContext context = new AnnotationConfigApplicationContext(
                RequestOptionsConfiguration.class);
        final CosmosDbFactory factory = context.getBean(CosmosDbFactory.class);

        Assertions.assertThat(factory).isNotNull();

        final RequestOptions options = factory.getConfig().getRequestOptions();

        Assertions.assertThat(options).isNotNull();
        Assertions.assertThat(options.getConsistencyLevel()).isEqualTo(ConsistencyLevel.CONSISTENT_PREFIX);
        Assertions.assertThat(options.isScriptLoggingEnabled()).isTrue();
    }

    @Configuration
    @PropertySource(value = {"classpath:application.properties"})
    static class TestCosmosConfiguration extends AbstractCosmosConfiguration {

        @Value("${cosmosdb.uri:}")
        private String cosmosDbUri;

        @Value("${cosmosdb.key:}")
        private String cosmosDbKey;

        @Value("${cosmosdb.database:}")
        private String database;

        @Mock
        private CosmosClient mockClient;

        @Bean
        public CosmosDBConfig getConfig() {
            final String dbName = StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
            return CosmosDBConfig.builder(cosmosDbUri, cosmosDbKey, dbName).build();
        }

        @Override
        public CosmosClient cosmosClient(CosmosDBConfig config) {
            return mockClient;
        }
    }

    @Configuration
    static class ObjectMapperConfiguration extends TestCosmosConfiguration {
        @Bean(name = OBJECTMAPPER_BEAN_NAME)
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Configuration
    @PropertySource(value = {"classpath:application.properties"})
    static class RequestOptionsConfiguration extends AbstractCosmosConfiguration {

        @Value("${cosmosdb.uri:}")
        private String cosmosDbUri;

        @Value("${cosmosdb.key:}")
        private String cosmosDbKey;

        @Value("${cosmosdb.database:}")
        private String database;

        private RequestOptions getRequestOptions() {
            final RequestOptions options = new RequestOptions();

            options.setConsistencyLevel(ConsistencyLevel.CONSISTENT_PREFIX);
            options.setScriptLoggingEnabled(true);

            return options;
        }

        @Bean
        public CosmosDBConfig getConfig() {
            final String dbName = StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
            final RequestOptions options = getRequestOptions();
            return CosmosDBConfig.builder(cosmosDbUri, cosmosDbKey, dbName)
                    .requestOptions(options)
                    .build();
        }

    }
}
