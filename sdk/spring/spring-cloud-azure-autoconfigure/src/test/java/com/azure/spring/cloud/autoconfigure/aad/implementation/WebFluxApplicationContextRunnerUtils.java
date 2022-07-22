// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation;

import com.azure.spring.cloud.autoconfigure.aad.AadReactiveAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class WebFluxApplicationContextRunnerUtils {

    public static ApplicationContextRunner reactiveApplicationContextRunner() {
        return new ApplicationContextRunner()
            .withUserConfiguration(
                AzureGlobalPropertiesAutoConfiguration.class,
                AadReactiveAutoConfiguration.class
            ).withInitializer(new ConditionEvaluationReportLoggingListener(LogLevel.INFO));
    }

    public static ReactiveWebApplicationContextRunner reactiveWebApplicationContextRunner() {
        return new ReactiveWebApplicationContextRunner()
            .withConfiguration(
                AutoConfigurations.of(WebFluxAutoConfiguration.class))
            .withUserConfiguration(
                AzureGlobalPropertiesAutoConfiguration.class,
                AadReactiveAutoConfiguration.class
            )
            .withInitializer(new ConditionEvaluationReportLoggingListener(LogLevel.INFO));
    }

}
