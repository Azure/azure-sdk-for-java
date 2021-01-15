// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin.config;

import com.azure.spring.data.gremlin.common.GremlinConfig;
import com.azure.spring.data.gremlin.config.AbstractGremlinConfiguration;
import com.azure.spring.data.gremlin.repository.config.EnableGremlinRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableGremlinRepositories(basePackages = "com.azure.spring.data.sample.gremlin.repository")
@EnableConfigurationProperties(GremlinProperties.class)
@PropertySource("classpath:application.properties")
public class UserRepositoryConfiguration extends AbstractGremlinConfiguration {

    @Autowired
    private GremlinProperties gremlinProperties;

    @Override
    public GremlinConfig getGremlinConfig() {
        return GremlinConfig.defaultBuilder()
            .endpoint(gremlinProperties.getEndpoint())
            .port(gremlinProperties.getPort())
            .username(gremlinProperties.getUsername())
            .password(gremlinProperties.getPassword())
            .sslEnabled(gremlinProperties.isSslEnabled())
            .telemetryAllowed(gremlinProperties.isTelemetryAllowed())
            .serializer(gremlinProperties.getSerializer())
            .maxContentLength(gremlinProperties.getMaxContentLength())
            .build();
    }
}
