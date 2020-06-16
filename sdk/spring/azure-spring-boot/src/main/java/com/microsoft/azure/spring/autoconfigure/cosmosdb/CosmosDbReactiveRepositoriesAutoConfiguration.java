// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.repository.ReactiveCosmosRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.config.ReactiveCosmosRepositoryConfigurationExtension;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.ReactiveCosmosRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import {@link CosmosDbReactiveRepositoriesAutoConfigureRegistrar} class as a Bean in Spring.
 */
@Configuration
@ConditionalOnClass({ ReactiveCosmosRepository.class })
@ConditionalOnMissingBean({ ReactiveCosmosRepositoryFactoryBean.class,
        ReactiveCosmosRepositoryConfigurationExtension.class })
@ConditionalOnProperty(prefix = "azure.cosmosdb.repositories",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import(CosmosDbReactiveRepositoriesAutoConfigureRegistrar.class)
public class CosmosDbReactiveRepositoriesAutoConfiguration {
}
