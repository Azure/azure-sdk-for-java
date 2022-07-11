// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@ConditionalOnClass(DataSource.class)
@Configuration(proxyBeanMethods = false)
public class JDBCPropertiesBeanPostProcessorConfiguration {
    @Bean
    static JDBCPropertiesBeanPostProcessor jdbcConfigurationPropertiesBeanPostProcessor() {
        return new JDBCPropertiesBeanPostProcessor();
    }
}
