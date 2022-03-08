// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.config;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;

/**
 * To configure cosmos with client, cosmos factory and template
 */
@Configuration
public abstract class AbstractCosmosConfiguration extends CosmosConfigurationSupport {

    /**
     * Declare CosmosFactory bean.
     *
     * @param cosmosAsyncClient of cosmosFactory
     * @return CosmosFactory bean
     */
    @Bean
    public CosmosFactory cosmosFactory(CosmosAsyncClient cosmosAsyncClient) {
        return new CosmosFactory(cosmosAsyncClient, getDatabaseName());
    }

    /**
     * Declare MappingCosmosConverter bean.
     *
     * @param cosmosMappingContext cosmosMappingContext
     * @return MappingCosmosConverter bean
     */
    @Bean
    public MappingCosmosConverter mappingCosmosConverter(CosmosMappingContext cosmosMappingContext) {
        return new MappingCosmosConverter(cosmosMappingContext, objectMapper);
    }

    /**
     * Declare CosmosAsyncClient bean.
     *
     * @param cosmosClientBuilder cosmosClientBuilder
     * @return CosmosAsyncClient bean
     */
    @Bean
    public CosmosAsyncClient cosmosAsyncClient(CosmosClientBuilder cosmosClientBuilder) {
        return CosmosFactory.createCosmosAsyncClient(cosmosClientBuilder);
    }

    @Qualifier(Constants.OBJECT_MAPPER_BEAN_NAME)
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Qualifier(Constants.AUDITING_HANDLER_BEAN_NAME)
    @Autowired(required = false)
    private IsNewAwareAuditingHandler cosmosAuditingHandler;

    /**
     * Declare CosmosTemplate bean.
     *
     * @param cosmosFactory cosmosFactory
     * @param cosmosConfig cosmosConfig
     * @param mappingCosmosConverter mappingCosmosConverter
     * @return CosmosTemplate bean
     */
    @Bean
    public CosmosTemplate cosmosTemplate(CosmosFactory cosmosFactory,
                                         CosmosConfig cosmosConfig,
                                         MappingCosmosConverter mappingCosmosConverter) {
        return new CosmosTemplate(cosmosFactory, cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
    }

    /**
     * Declare ReactiveCosmosTemplate bean.
     *
     * @param cosmosFactory cosmosFactory
     * @param cosmosConfig cosmosConfig
     * @param mappingCosmosConverter mappingCosmosConverter
     * @return ReactiveCosmosTemplate bean
     */
    @Bean
    public ReactiveCosmosTemplate reactiveCosmosTemplate(CosmosFactory cosmosFactory,
                                                         CosmosConfig cosmosConfig,
                                                         MappingCosmosConverter mappingCosmosConverter) {
        return new ReactiveCosmosTemplate(cosmosFactory, cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
    }

    /**
     * Declare CosmosConfig bean
     *
     * @return CosmosConfig bean
     */
    @Bean
    public CosmosConfig cosmosConfig() {
        return new CosmosConfig.CosmosConfigBuilder().build();
    }
}
