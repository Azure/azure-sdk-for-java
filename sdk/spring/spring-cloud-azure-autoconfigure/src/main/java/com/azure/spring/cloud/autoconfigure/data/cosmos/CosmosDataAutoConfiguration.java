// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosProperties;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 *  {@link EnableAutoConfiguration Auto-configuration} for Spring Data Cosmos support.
 *
 *  @since 4.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ CosmosTemplate.class })
@ConditionalOnExpression("${spring.cloud.azure.cosmos.enabled:true}")
@ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos", name = { "endpoint", "database" })
@Import(CosmosDataDiagnosticsConfiguration.class)
public class CosmosDataAutoConfiguration  {

    @Bean
    @ConditionalOnBean(ResponseDiagnosticsProcessor.class)
    CosmosConfigBuilderCustomizer responseDiagnosticProcessorCustomizer(
        ResponseDiagnosticsProcessor responseDiagnosticsProcessor) {
        return builder -> builder.responseDiagnosticsProcessor(responseDiagnosticsProcessor);
    }

    @Bean
    CosmosConfigBuilderCustomizer cosmosPropertiesCustomizer(AzureCosmosProperties cosmosProperties) {
        return builder -> {
            builder.enableQueryMetrics(cosmosProperties.isPopulateQueryMetrics());
            if (cosmosProperties.getMaxDegreeOfParallelism() != null) {
                builder.maxDegreeOfParallelism(cosmosProperties.getMaxDegreeOfParallelism());
            }
        };
    }

    @Configuration
    static class ExtensibleCosmosConfiguration extends AbstractCosmosConfiguration {
        private final AzureCosmosProperties cosmosProperties;
        private final ObjectProvider<CosmosConfigBuilderCustomizer> customizers;

        ExtensibleCosmosConfiguration(AzureCosmosProperties cosmosProperties,
                                      ObjectProvider<CosmosConfigBuilderCustomizer> customizers) {
            this.cosmosProperties = cosmosProperties;
            this.customizers = customizers;
        }

        @Override
        protected String getDatabaseName() {
            return cosmosProperties.getDatabase();
        }

        @Override
        public CosmosConfig cosmosConfig() {
            final CosmosConfig.CosmosConfigBuilder builder = CosmosConfig.builder();
            customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
            return builder.build();
        }
    }

}
