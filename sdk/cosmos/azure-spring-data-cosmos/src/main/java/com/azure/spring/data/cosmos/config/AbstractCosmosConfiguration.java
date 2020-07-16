// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.config;

import com.azure.data.cosmos.CosmosClient;
import com.azure.data.cosmos.sync.CosmosSyncClient;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.spring.data.cosmos.CosmosDbFactory;
import com.azure.spring.data.cosmos.Constants;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * To configure cosmos with client, cosmoscdb factory and template
 */
@Configuration
public abstract class AbstractCosmosConfiguration extends CosmosConfigurationSupport {

    /**
     * Declare CosmosClient bean.
     * @param config of cosmosDbFactory
     * @return CosmosClient bean
     */
    @Bean
    public CosmosClient cosmosClient(CosmosDBConfig config) {
        return this.cosmosDbFactory(config).getCosmosClient();
    }

    /**
     * Declare CosmosSyncClient bean.
     * @param config of cosmosDbFactory
     * @return CosmosSyncClient bean
     */
    @Bean
    public CosmosSyncClient cosmosSyncClient(CosmosDBConfig config) {
        return this.cosmosDbFactory(config).getCosmosSyncClient();
    }

    @Qualifier(Constants.OBJECTMAPPER_BEAN_NAME)
    @Autowired(required = false)
    private ObjectMapper objectMapper;

    /**
     * Declare CosmosDbFactory bean.
     * @param config of cosmosDbFactory
     * @return CosmosDbFactory bean
     */
    @Bean
    public CosmosDbFactory cosmosDbFactory(CosmosDBConfig config) {
        return new CosmosDbFactory(config);
    }

    /**
     * Declare CosmosTemplate bean.
     * @param config of cosmosDbFactory
     * @return CosmosTemplate bean
     * @throws ClassNotFoundException if the class type is invalid
     */
    @Bean
    public CosmosTemplate cosmosTemplate(CosmosDBConfig config) throws ClassNotFoundException {
        return new CosmosTemplate(this.cosmosDbFactory(config), this.mappingCosmosConverter(),
                config.getDatabase());
    }

    /**
     * Declare ReactiveCosmosTemplate bean.
     * @param config of cosmosDbFactory
     * @return ReactiveCosmosTemplate bean
     * @throws ClassNotFoundException if the class type is invalid
     */
    @Bean
    public ReactiveCosmosTemplate reactiveCosmosTemplate(CosmosDBConfig config) throws ClassNotFoundException {
        return new ReactiveCosmosTemplate(this.cosmosDbFactory(config), this.mappingCosmosConverter(),
            config.getDatabase());
    }

    /**
     * Declare MappingCosmosConverter bean.
     * @return MappingCosmosConverter bean
     * @throws ClassNotFoundException if the class type is invalid
     */
    @Bean
    public MappingCosmosConverter mappingCosmosConverter() throws ClassNotFoundException {
        return new MappingCosmosConverter(this.cosmosMappingContext(), objectMapper);
    }
}
