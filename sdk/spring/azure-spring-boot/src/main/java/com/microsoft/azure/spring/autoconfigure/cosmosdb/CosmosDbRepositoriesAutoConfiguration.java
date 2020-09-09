// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.CosmosRepositoryConfigurationExtension;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import {@link CosmosDbRepositoriesAutoConfigureRegistrar} class as a Bean in Spring.
 */
@Configuration
@ConditionalOnClass({ CosmosRepository.class })
@ConditionalOnMissingBean({ CosmosRepositoryFactoryBean.class,
        CosmosRepositoryConfigurationExtension.class })
@ConditionalOnProperty(prefix = "azure.cosmosdb.repositories",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import(CosmosDbRepositoriesAutoConfigureRegistrar.class)
public class CosmosDbRepositoriesAutoConfiguration {
}
