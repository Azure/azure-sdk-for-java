// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package example.springdata.cosmosdb;

import com.azure.data.cosmos.CosmosKeyCredential;
import com.microsoft.azure.spring.data.cosmosdb.config.AbstractCosmosConfiguration;
import com.microsoft.azure.spring.data.cosmosdb.config.CosmosDBConfig;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnostics;
import com.microsoft.azure.spring.data.cosmosdb.core.ResponseDiagnosticsProcessor;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nullable;


@Configuration
@EnableConfigurationProperties(CosmosDbProperties.class)
@EnableReactiveCosmosRepositories
@PropertySource("classpath:application.properties")
public class UserRepositoryConfiguration extends AbstractCosmosConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryConfiguration.class);

    @Autowired
    private CosmosDbProperties properties;

    private CosmosKeyCredential cosmosKeyCredential;

    @Bean
    public CosmosDBConfig cosmosDbConfig() {
        this.cosmosKeyCredential = new CosmosKeyCredential(properties.getKey());
        CosmosDBConfig cosmosDBConfig = CosmosDBConfig.builder(properties.getUri(), cosmosKeyCredential,
            properties.getDatabase()).build();
        cosmosDBConfig.setPopulateQueryMetrics(properties.isPopulateQueryMetrics());
        cosmosDBConfig.setResponseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation());
        return cosmosDBConfig;
    }

    public void switchToSecondaryKey() {
        this.cosmosKeyCredential.key(properties.getSecondaryKey());
    }

    public void switchToPrimaryKey() {
        this.cosmosKeyCredential.key(properties.getKey());
    }

    public void switchKey(String key) {
        this.cosmosKeyCredential.key(key);
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            LOGGER.info("Response Diagnostics {}", responseDiagnostics);
        }
    }
}
