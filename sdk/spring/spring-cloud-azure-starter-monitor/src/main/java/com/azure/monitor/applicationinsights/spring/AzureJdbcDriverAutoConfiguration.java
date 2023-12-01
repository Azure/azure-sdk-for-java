// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

import io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver;
import io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryInjector;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Workaround waiting for OTel release: https://github.com/open-telemetry/opentelemetry-java-instrumentation/pull/9978
 */
@ConditionalOnClass(OpenTelemetryDriver.class)
@ConditionalOnProperty(
    name = "spring.datasource.driver-class-name",
    havingValue = "io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver")
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@ConditionalOnBean(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
public class AzureJdbcDriverAutoConfiguration {

    @Bean
    OpenTelemetryInjector injectOtelIntoJdbcDriver() {
        return openTelemetry -> OpenTelemetryDriver.install(openTelemetry);
    }

    // To be sure OpenTelemetryDriver knows the OpenTelemetry bean before the initialization of the
    // database connection pool
    // See org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration and
    // io.opentelemetry.instrumentation.spring.autoconfigure.OpenTelemetryAutoConfiguration
    @Bean
    BeanFactoryPostProcessor openTelemetryBeanCreatedBeforeDatasourceBean() {
        return configurableBeanFactory -> {
            BeanDefinition dataSourceBean = configurableBeanFactory.getBeanDefinition("dataSource");
            dataSourceBean.setDependsOn("openTelemetry");
        };
    }
}
