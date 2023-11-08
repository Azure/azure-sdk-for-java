// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.jdbc.datasource.OpenTelemetryDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfig {

    @Bean
    public DataSource dataSource(OpenTelemetry openTelemetry) {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url("jdbc:h2:mem:db");
        dataSourceBuilder.username("username");
        dataSourceBuilder.password("pwd");
        DataSource dataSource = dataSourceBuilder.build();
        return new OpenTelemetryDataSource(dataSource, openTelemetry);
    }
}
