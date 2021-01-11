// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin.web.service.config;

import com.azure.spring.data.gremlin.common.GremlinConfig;
import com.azure.spring.data.gremlin.config.AbstractGremlinConfiguration;
import com.azure.spring.data.gremlin.repository.config.EnableGremlinRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableGremlinRepositories(basePackages = "com.microsoft.azure.spring.data.gremlin.web.service.repository")
@EnableConfigurationProperties(GremlinProperties.class)
@PropertySource("classpath:application.properties")
public class UserRepositoryConfiguration extends AbstractGremlinConfiguration {

    @Autowired
    private GremlinProperties gremlinProps;

    @Override
    public GremlinConfig getGremlinConfig() {
        return GremlinConfig.defaultBuilder()
            .endpoint(gremlinProps.getEndpoint())
            .port(gremlinProps.getPort())
            .username(gremlinProps.getUsername())
            .password(gremlinProps.getPassword())
            .sslEnabled(gremlinProps.isSslEnabled())
            .telemetryAllowed(gremlinProps.isTelemetryAllowed())
            .serializer(gremlinProps.getSerializer())
            .maxContentLength(gremlinProps.getMaxContentLength())
            .build();
    }
}
