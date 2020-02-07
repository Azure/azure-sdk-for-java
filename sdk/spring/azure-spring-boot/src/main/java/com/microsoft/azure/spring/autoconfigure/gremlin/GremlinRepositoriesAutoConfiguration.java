/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.gremlin;

import com.microsoft.spring.data.gremlin.repository.GremlinRepository;
import com.microsoft.spring.data.gremlin.repository.config.GremlinRepositoryConfigurationExtension;
import com.microsoft.spring.data.gremlin.repository.support.GremlinRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass({GremlinRepository.class})
@ConditionalOnMissingBean({GremlinRepositoryFactoryBean.class, GremlinRepositoryConfigurationExtension.class})
@ConditionalOnProperty(prefix = "spring.data.gremlin.repositories", name = "enabled", havingValue = "true",
        matchIfMissing = true)
@Import(GremlinRepositoriesAutoConfigureRegistrar.class)
public class GremlinRepositoriesAutoConfiguration {
}
