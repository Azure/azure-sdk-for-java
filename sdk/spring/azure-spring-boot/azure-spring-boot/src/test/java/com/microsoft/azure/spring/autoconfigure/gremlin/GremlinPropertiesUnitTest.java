/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.gremlin;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.autoconfigure.gremlin.PropertiesUtil.*;

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
                    Assert.assertEquals(context.getBean(GremlinProperties.class).getEndpoint(), ENDPOINT);
                    Assert.assertEquals(context.getBean(GremlinProperties.class).getPort(), PORT);
                    Assert.assertEquals(context.getBean(GremlinProperties.class).getUsername(), USERNAME);
                    Assert.assertEquals(context.getBean(GremlinProperties.class).getPassword(), PASSWORD);
                    Assert.assertFalse(context.getBean(GremlinProperties.class).isTelemetryAllowed());
                });
    }
}
