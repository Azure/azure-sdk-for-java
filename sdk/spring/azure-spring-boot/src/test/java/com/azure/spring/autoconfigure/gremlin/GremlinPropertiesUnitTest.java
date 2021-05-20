// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.gremlin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.azure.spring.autoconfigure.gremlin.PropertiesUtil.*;

public class GremlinPropertiesUnitTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GremlinAutoConfiguration.class));

    @Test
    public void testAllProperties() {
        this.contextRunner
                .withPropertyValues(GREMLIN_ENDPOINT_CONFIG)
                .withPropertyValues(GREMLIN_PORT_CONFIG)
                .withPropertyValues(GREMLIN_USERNAME_CONFIG)
                .withPropertyValues(GREMLIN_PASSWORD_CONFIG)
                .withPropertyValues(GREMLIN_TELEMETRY_CONFIG_NOT_ALLOWED)
                .run(context -> {
                    Assertions.assertEquals(context.getBean(GremlinProperties.class).getEndpoint(), ENDPOINT);
                    Assertions.assertEquals(context.getBean(GremlinProperties.class).getPort(), PORT);
                    Assertions.assertEquals(context.getBean(GremlinProperties.class).getUsername(), USERNAME);
                    Assertions.assertEquals(context.getBean(GremlinProperties.class).getPassword(), PASSWORD);
                    Assertions.assertFalse(context.getBean(GremlinProperties.class).isTelemetryAllowed());
                });
    }
}
