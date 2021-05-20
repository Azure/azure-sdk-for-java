// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.config;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class AbstractGremlinConfigurationIT {

    @Autowired
    private TestRepositoryConfiguration testConfig;

    @Test
    public void testGremlinFactory() {
        Assertions.assertNotNull(this.testConfig.gremlinFactory());
    }

    @Test
    public void testMappingGremlinConverter() throws ClassNotFoundException {
        Assertions.assertNotNull(this.testConfig.mappingGremlinConverter());
    }

    @Test
    public void testGremlinTemplate() throws ClassNotFoundException {
        Assertions.assertNotNull(this.testConfig.gremlinTemplate(testConfig.gremlinFactory()));
    }
}

