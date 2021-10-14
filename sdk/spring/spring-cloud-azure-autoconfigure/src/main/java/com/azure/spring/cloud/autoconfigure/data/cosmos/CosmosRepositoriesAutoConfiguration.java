// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.config.CosmosRepositoryConfigurationExtension;
import com.azure.spring.data.cosmos.repository.support.CosmosRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import {@link CosmosRepositoriesAutoConfigureRegistrar} class as a Bean in Spring.
 */
@Configuration
@ConditionalOnClass({ CosmosRepository.class })
@ConditionalOnMissingBean({ CosmosRepositoryFactoryBean.class,
    CosmosRepositoryConfigurationExtension.class })
@ConditionalOnProperty(prefix = "azure.cosmos.repositories",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import(CosmosRepositoriesAutoConfigureRegistrar.class)
public class CosmosRepositoriesAutoConfiguration {
}
