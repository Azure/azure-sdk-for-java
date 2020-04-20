// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.implementation.RequestResponseUtils.UNDEFINED_STATUS_CODE;
import static com.azure.core.amqp.implementation.RequestResponseUtils.UNDEFINED_STATUS_DESCRIPTION;

class RequestResponseUtilsTest {
    /**
     * Verifies that the correct status value is extracted.
     *
     * @param statusCodeHeader Status code header key.
     */
    @ParameterizedTest
    @ValueSource(strings = {"statusCode", "status-code"})
    void getResponseStatusCode(String statusCodeHeader) {
        // Arrange
        final int expected = 451;
        final Map<String, Object> properties = new HashMap<>();
        properties.put(statusCodeHeader, expected);

        final Message message = Proton.message();
        message.setBody(new AmqpValue("test"));
        message.setApplicationProperties(new ApplicationProperties(properties));

        // Act
        final int actual = RequestResponseUtils.getStatusCode(message);

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    /**
     * Verifies the default status code and description are returned.
     */
    @Test
    void defaultResponseRequestResponses() {
        // Arrange
        final Map<String, Object> properties = new HashMap<>();
        properties.put("non-existent", 33214);

        final Message message = Proton.message();
        message.setBody(new AmqpValue("test"));
        message.setApplicationProperties(new ApplicationProperties(properties));

        // Act
        final int actual = RequestResponseUtils.getStatusCode(message);
        final String description = RequestResponseUtils.getStatusDescription(message);

        // Assert
        Assertions.assertEquals(UNDEFINED_STATUS_CODE, actual);
        Assertions.assertEquals(UNDEFINED_STATUS_DESCRIPTION, description);
    }
}
