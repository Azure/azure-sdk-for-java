// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common;

import com.azure.spring.data.gremlin.exception.GremlinIllegalConfigurationException;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.azure.spring.data.gremlin.common.TestConstants.EMPTY_STRING;
import static com.azure.spring.data.gremlin.common.TestConstants.ILLEGAL_ENDPOINT_PORT;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ContextConfiguration(classes = {GremlinFactoryUnitTest.TestConfiguration.class})
@ExtendWith(SpringExtension.class)
public class GremlinFactoryUnitTest {

    @Autowired
    private GremlinFactory factory;

    @Test
    public void testGremlinFactoryException() {
        final GremlinConfig config = GremlinConfig.builder(TestConstants.FAKE_ENDPOINT, TestConstants.FAKE_USERNAME,
                TestConstants.FAKE_PASSWORD).build();

        assertThrows(GremlinIllegalConfigurationException.class,
            () -> new GremlinFactory(config).getGremlinClient());
    }

    @Test
    public void testGremlinFactoryNormal() {
        final Client client = factory.getGremlinClient();

        Assertions.assertEquals(client.getCluster().getPort(), TestConstants.DEFAULT_ENDPOINT_PORT);
        Assertions.assertFalse(client.getSettings().getSession().isPresent());
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public GremlinFactory getGremlinFactory() {
            return new GremlinFactory(getGremlinConfig());
        }

        @Bean
        public GremlinConfig getGremlinConfig() {
            return GremlinConfig.builder(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING)
                    .port(ILLEGAL_ENDPOINT_PORT)
                    .build();
        }
    }
}
