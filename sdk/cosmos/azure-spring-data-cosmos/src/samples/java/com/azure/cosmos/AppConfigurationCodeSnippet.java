// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */

import com.azure.core.credential.AzureKeyCredential;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosDBConfig;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCosmosRepositories
public class AppConfigurationCodeSnippet extends AbstractCosmosConfiguration {
    // configuration code
    @Value("${azure.cosmosdb.uri}")
    private String uri;

    @Value("${azure.cosmosdb.key}")
    private String key;

    @Value("${azure.cosmosdb.secondaryKey}")
    private String secondaryKey;

    @Value("${azure.cosmosdb.database}")
    private String dbName;

    @Value("${azure.cosmosdb.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    public CosmosDBConfig getConfig() {
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential(key);
        return CosmosDBConfig.builder()
                             .database(dbName)
                             .cosmosClientBuilder(new CosmosClientBuilder().credential(azureKeyCredential))
                             .enableQueryMetrics(queryMetricsEnabled)
                             .build();
    }
}
