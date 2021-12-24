// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.spring.data.cosmos.core.ReactiveCosmosTemplate;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.azure.spring.data.cosmos.repository.config.ReactiveCosmosRepositoryConfigurationExtension;
import com.azure.spring.data.cosmos.repository.support.ReactiveCosmosRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import {@link CosmosReactiveRepositoriesAutoConfigureRegistrar} class as a Bean in Spring.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ ReactiveCosmosRepository.class })
@ConditionalOnMissingBean({ ReactiveCosmosRepositoryFactoryBean.class,
    ReactiveCosmosRepositoryConfigurationExtension.class })
@AutoConfigureAfter(CosmosDataAutoConfiguration.class)
@ConditionalOnBean(ReactiveCosmosTemplate.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.cosmos.repositories",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@Import(CosmosReactiveRepositoriesAutoConfigureRegistrar.class)
public class CosmosReactiveRepositoriesAutoConfiguration {
}
