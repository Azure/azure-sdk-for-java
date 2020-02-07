/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.gremlin;

import com.microsoft.spring.data.gremlin.common.GremlinFactory;
import com.microsoft.spring.data.gremlin.conversion.MappingGremlinConverter;
import com.microsoft.spring.data.gremlin.mapping.GremlinMappingContext;
import com.microsoft.spring.data.gremlin.query.GremlinTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static com.microsoft.azure.spring.autoconfigure.gremlin.PropertiesUtil.*;

public class GremlinAutoConfigurationUnitTest {

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GremlinAutoConfiguration.class));

    @Test
    public void testAllBeanCreated() {
        this.contextRunner
                .withPropertyValues(GREMLIN_ENDPOINT_CONFIG)
                .withPropertyValues(GREMLIN_PORT_CONFIG)
                .withPropertyValues(GREMLIN_USERNAME_CONFIG)
                .withPropertyValues(GREMLIN_PASSWORD_CONFIG)
                .withPropertyValues(GREMLIN_TELEMETRY_CONFIG_ALLOWED)
                .run(context -> {
                    Assert.assertNotNull(context.getBean(GremlinFactory.class));
                    Assert.assertNotNull(context.getBean(GremlinFactory.class).getGremlinClient());
                    Assert.assertNotNull(context.getBean(GremlinTemplate.class));
                    Assert.assertNotNull(context.getBean(GremlinMappingContext.class));
                    Assert.assertNotNull(context.getBean(MappingGremlinConverter.class));
                });
    }
}
