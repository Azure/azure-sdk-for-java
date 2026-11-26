// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectionIT {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.azure.spring.cloud.config.SelectionTestData#scenarios")
    void appliesLabelAndSelectPriority(SelectionTestData.Scenario scenario) {
        SpringApplication application = new SpringApplication(AppConfiguration.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        try (ConfigurableApplicationContext context = application.run(scenario.arguments("${STORE_NAME}"))) {
            assertEquals(scenario.expectedEntries(), context.getBean(SelectionProperties.class).entries());
            assertEquals(scenario.expectedEntries().get("shared"),
                context.getEnvironment().getProperty("selection.entries.shared"));
        }
    }
}
