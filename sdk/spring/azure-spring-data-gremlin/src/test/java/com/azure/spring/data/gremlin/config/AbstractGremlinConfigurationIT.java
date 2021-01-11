// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.config;

import com.azure.spring.data.gremlin.common.TestRepositoryConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfiguration.class)
public class AbstractGremlinConfigurationIT {

    @Autowired
    private TestRepositoryConfiguration testConfig;

    @Test
    public void testGremlinFactory() {
        Assert.assertNotNull(this.testConfig.gremlinFactory());
    }

    @Test
    public void testMappingGremlinConverter() throws ClassNotFoundException {
        Assert.assertNotNull(this.testConfig.mappingGremlinConverter());
    }

    @Test
    public void testGremlinTemplate() throws ClassNotFoundException {
        Assert.assertNotNull(this.testConfig.gremlinTemplate(testConfig.gremlinFactory()));
    }
}

