// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms.properties;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureServiceBusJmsPropertiesTests {
    private static final ClientLogger LOGGER = new ClientLogger(AzureServiceBusJmsPropertiesTests.class);

    static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(AzureServiceBusJmsPropertiesTestConfig.class);

    @Test
    void connectionStringNotValid() {
        AzureServiceBusJmsProperties prop = new AzureServiceBusJmsProperties();
        Exception ex = assertThrows(IllegalArgumentException.class,
            prop::afterPropertiesSet);

        String expectedMessage = "'spring.jms.servicebus.connection-string' should be provided.";
        String actualMessage = ex.getMessage();
        LOGGER.log(LogLevel.VERBOSE, () -> "message:" + actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"xx"})
    void pricingTierNotValid(String pricingTier) {
        AzureServiceBusJmsProperties prop = new AzureServiceBusJmsProperties();
        prop.setConnectionString(CONNECTION_STRING);
        prop.setPricingTier(pricingTier);
        Exception ex = assertThrows(IllegalArgumentException.class,
            prop::afterPropertiesSet);

        String expectedMessage = "'spring.jms.servicebus.pricing-tier' is not valid";
        String actualMessage = ex.getMessage();
        LOGGER.log(LogLevel.VERBOSE, () -> "message:" + actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void testPasswordlessEnabled() {
        contextRunner
            .withPropertyValues("spring.jms.servicebus.passwordless-enabled=true")
            .run(context -> {
                Exception ex = assertThrows(IllegalStateException.class, () -> context.getBean(AzureServiceBusJmsProperties.class));
                String actualMessage = ex.getCause().getMessage();
                String expectedMessage = "Passwordless connections enabled, 'spring.jms.servicebus.namespace' should be provided.";
                assertTrue(actualMessage.contains(expectedMessage));
            });
    }

    @EnableConfigurationProperties
    static class AzureServiceBusJmsPropertiesTestConfig {

        @Bean
        @ConfigurationProperties(AzureServiceBusJmsProperties.PREFIX)
        AzureServiceBusJmsProperties jmsProperties() {
            return new AzureServiceBusJmsProperties();
        }
    }

}
