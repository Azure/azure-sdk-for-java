// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.RequestOptions;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import com.azure.spring.data.cosmos.common.DynamicContainer;
import com.azure.spring.data.cosmos.common.ResponseDiagnosticsTestUtils;
import com.azure.spring.data.cosmos.common.TestConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.StringUtils;

@Configuration
@PropertySource(value = {"classpath:application.properties"})
@EnableCosmosRepositories
@EnableReactiveCosmosRepositories
public class TestRepositoryConfig extends AbstractCosmosConfiguration {
    @Value("${cosmosdb.uri:}")
    private String cosmosDbUri;

    @Value("${cosmosdb.key:}")
    private String cosmosDbKey;

    @Value("${cosmosdb.connection-string:}")
    private String connectionString;

    @Value("${cosmosdb.database:}")
    private String database;

    @Value("${cosmosdb.populateQueryMetrics}")
    private boolean populateQueryMetrics;

    private RequestOptions getRequestOptions() {
        final RequestOptions options = new RequestOptions();

        options.setConsistencyLevel(ConsistencyLevel.SESSION);
//        options.setDisableRUPerMinuteUsage(true);
        options.setScriptLoggingEnabled(true);

        return options;
    }

    @Bean
    public ResponseDiagnosticsTestUtils responseDiagnosticsTestUtils() {
        return new ResponseDiagnosticsTestUtils();
    }

    @Bean
    public CosmosDBConfig getConfig() {
        final String dbName = StringUtils.hasText(this.database) ? this.database : TestConstants.DB_NAME;
        final RequestOptions options = getRequestOptions();
        final CosmosDBConfig.CosmosDBConfigBuilder builder;

        if (StringUtils.hasText(this.cosmosDbUri)
                && StringUtils.hasText(this.cosmosDbKey)) {
            builder = CosmosDBConfig.builder(cosmosDbUri, cosmosDbKey, dbName);
        } else {
            builder = CosmosDBConfig.builder(connectionString, dbName);
        }
        return builder.requestOptions(options)
                .populateQueryMetrics(populateQueryMetrics)
                .responseDiagnosticsProcessor(responseDiagnosticsTestUtils().getResponseDiagnosticsProcessor())
                .build();
    }

    @Bean
    public DynamicContainer dynamicContainer() {
        return new DynamicContainer(TestConstants.DYNAMIC_BEAN_COLLECTION_NAME);
    }
}
