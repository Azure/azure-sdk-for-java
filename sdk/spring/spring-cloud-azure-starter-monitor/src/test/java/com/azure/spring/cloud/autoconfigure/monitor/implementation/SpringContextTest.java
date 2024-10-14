// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.monitor.implementation;

import com.azure.spring.cloud.autoconfigure.monitor.selfdiagnostics.implementation.SelfDiagAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SpringContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    // The tests of the Spring Monitor features are done in the spring-cloud-azure-starter-monitor-test Maven module
    @Test
    void loadContext() {
        this.contextRunner.withConfiguration(AutoConfigurations.of(AzureSpringMonitorAutoConfiguration.class, SelfDiagAutoConfig.class)).run(context -> {
        });
    }

}
