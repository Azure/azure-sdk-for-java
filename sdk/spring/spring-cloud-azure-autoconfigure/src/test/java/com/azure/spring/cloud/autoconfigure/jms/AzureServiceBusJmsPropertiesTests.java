// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

import com.azure.spring.cloud.autoconfigure.jms.properties.AzureServiceBusJmsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureServiceBusJmsPropertiesTests {
    static final String CONNECTION_STRING = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;"
        + "SharedAccessKey=sasKey";

    @Test
    void connectionStringNotValid() {
        AzureServiceBusJmsProperties prop = new AzureServiceBusJmsProperties();
        Exception ex = assertThrows(IllegalArgumentException.class,
            prop::afterPropertiesSet);

        String expectedMessage = "'spring.jms.servicebus.connection-string' should be provided";
        String actualMessage = ex.getMessage();
        System.out.println("message:" + actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "xx" })
    void pricingTierNotValid(String pricingTier) {
        AzureServiceBusJmsProperties prop = new AzureServiceBusJmsProperties();
        prop.setConnectionString(CONNECTION_STRING);
        prop.setPricingTier(pricingTier);
        Exception ex = assertThrows(IllegalArgumentException.class,
            prop::afterPropertiesSet);

        String expectedMessage = "'spring.jms.servicebus.pricing-tier' is not valid";
        String actualMessage = ex.getMessage();
        System.out.println("message:" + actualMessage);
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @ParameterizedTest
    @ValueSource(strings = { "basic", "standard", "premium" })
    void setConnectionStringFormatCorrect(String pricingTier) throws Exception {
        AzureServiceBusJmsProperties prop = new AzureServiceBusJmsProperties();
        prop.setConnectionString(CONNECTION_STRING);
        prop.setPricingTier(pricingTier);
        prop.afterPropertiesSet();
        assertThat(prop.getUsername()).isEqualTo("sasKeyName");
        assertThat(prop.getPassword()).isEqualTo("sasKey");
        assertThat(prop.getRemoteUrl()).isEqualTo("amqps://host?amqp.idleTimeout=1800000");
    }
}
