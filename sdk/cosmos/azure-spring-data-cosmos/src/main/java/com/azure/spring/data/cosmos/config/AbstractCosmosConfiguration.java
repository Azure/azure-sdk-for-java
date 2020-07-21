// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.config;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
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

/**
 * To configure cosmos with client, cosmos factory and template
 */
@Configuration
public abstract class AbstractCosmosConfiguration extends CosmosConfigurationSupport {

    /**
     * Declare CosmosDbFactory bean.
     *
     * @param config of cosmosDbFactory
     * @return CosmosDbFactory bean
     */
    @Bean
    public CosmosFactory cosmosDBFactory(CosmosConfig config) {
        return new CosmosFactory(config);
    }

    /**
     * Declare MappingCosmosConverter bean.
     *
     * @param cosmosMappingContext cosmosMappingContext
     * @return MappingCosmosConverter bean
     * @throws ClassNotFoundException if the class type is invalid
     */
    @Bean
    public MappingCosmosConverter mappingCosmosConverter(CosmosMappingContext cosmosMappingContext)
        throws ClassNotFoundException {
        return new MappingCosmosConverter(cosmosMappingContext, objectMapper);
    }

    /**
     * Declare CosmosClient bean.
     *
     * @param cosmosFactory cosmosDbFactory
     * @return CosmosClient bean
     */
    @Bean
    public CosmosAsyncClient cosmosAsyncClient(CosmosFactory cosmosFactory) {
        return cosmosFactory.getCosmosAsyncClient();
    }

    /**
     * Declare CosmosSyncClient bean.
     *
     * @param cosmosFactory cosmosDBFactory
     * @return CosmosSyncClient bean
     */
    @Bean
    public CosmosClient cosmosClient(CosmosFactory cosmosFactory) {
        return cosmosFactory.getCosmosSyncClient();
    }

    @Qualifier(Constants.OBJECT_MAPPER_BEAN_NAME)
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * Declare CosmosTemplate bean.
     *
     * @param cosmosFactory cosmosDbFactory
     * @param mappingCosmosConverter mappingCosmosConverter
     * @return CosmosTemplate bean
     */
    @Bean
    public CosmosTemplate cosmosTemplate(CosmosFactory cosmosFactory,
                                         MappingCosmosConverter mappingCosmosConverter) {
        return new CosmosTemplate(cosmosFactory, mappingCosmosConverter,
            cosmosFactory.getConfig().getDatabase());
    }

    /**
     * Declare ReactiveCosmosTemplate bean.
     *
     * @param cosmosFactory cosmosDbFactory
     * @param mappingCosmosConverter mappingCosmosConverter
     * @return ReactiveCosmosTemplate bean
     */
    @Bean
    public ReactiveCosmosTemplate reactiveCosmosTemplate(CosmosFactory cosmosFactory,
                                                         MappingCosmosConverter mappingCosmosConverter) {
        return new ReactiveCosmosTemplate(cosmosFactory, mappingCosmosConverter,
            cosmosFactory.getConfig().getDatabase());
    }
}
