// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.implementation;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpResponseCode;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static com.azure.core.amqp.implementation.RequestResponseUtils.UNDEFINED_ERROR_CONDITION;
import static com.azure.core.amqp.implementation.RequestResponseUtils.UNDEFINED_STATUS_CODE;
import static com.azure.core.amqp.implementation.RequestResponseUtils.UNDEFINED_STATUS_DESCRIPTION;

class RequestResponseUtilsTest {
    /**
     * Verifies that the correct status value is extracted.
     *
     * @param statusCodeHeader Status code header key.
     */
    @ParameterizedTest
    @ValueSource(strings = { "statusCode", "status-code" })
    void getResponseStatusCode(String statusCodeHeader) {
        // Arrange
        final AmqpResponseCode expected = AmqpResponseCode.GONE;
        final Map<String, Object> properties = new HashMap<>();
        properties.put(statusCodeHeader, expected.getValue());

        final Message message = Proton.message();
        message.setBody(new AmqpValue("test"));
        message.setApplicationProperties(new ApplicationProperties(properties));

        // Act
        final AmqpResponseCode actual = RequestResponseUtils.getStatusCode(message);

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    /**
     * Verifies that the correct description is extracted.
     *
     * @param header Description header key.
     */
    @ParameterizedTest
    @ValueSource(strings = { "statusDescription", "status-description" })
    void getResponseDescription(String header) {
        // Arrange
        final String expected = "Contents of description";
        final Map<String, Object> properties = new HashMap<>();
        properties.put(header, expected);

        final Message message = Proton.message();
        message.setBody(new AmqpValue("test"));
        message.setApplicationProperties(new ApplicationProperties(properties));

        // Act
        final String actual = RequestResponseUtils.getStatusDescription(message);

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    /**
     * Verifies that the correct description is extracted.
     *
     * @param header Description header key.
     */
    @ParameterizedTest
    @ValueSource(strings = { "error-condition", "errorCondition" })
    void getErrorCondition(String header) {
        // Arrange
        final AmqpErrorCondition expected = AmqpErrorCondition.LINK_STOLEN;
        final Map<String, Object> properties = new HashMap<>();
        properties.put(header, expected.getErrorCondition());

        final Message message = Proton.message();
        message.setBody(new AmqpValue("test"));
        message.setApplicationProperties(new ApplicationProperties(properties));

        // Act
        final String actual = RequestResponseUtils.getErrorCondition(message);

        // Assert
        Assertions.assertEquals(expected.getErrorCondition(), actual);
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
        final AmqpResponseCode actual = RequestResponseUtils.getStatusCode(message);
        final String description = RequestResponseUtils.getStatusDescription(message);
        final String errorCondition = RequestResponseUtils.getErrorCondition(message);

        // Assert
        Assertions.assertEquals(UNDEFINED_STATUS_CODE, actual);
        Assertions.assertEquals(UNDEFINED_STATUS_DESCRIPTION, description);
        Assertions.assertEquals(UNDEFINED_ERROR_CONDITION, errorCondition);
    }

    @Test
    void getErrorConditionSymbol() {
        // Arrange
        final AmqpErrorCondition expected = AmqpErrorCondition.SERVER_BUSY_ERROR;
        final Map<String, Object> properties = new HashMap<>();
        properties.put("error-condition", Symbol.valueOf(expected.getErrorCondition()));

        final Message message = Proton.message();
        message.setBody(new AmqpValue("test"));
        message.setApplicationProperties(new ApplicationProperties(properties));

        // Act
        final String actual = RequestResponseUtils.getErrorCondition(message);

        // Assert
        Assertions.assertEquals(expected.getErrorCondition(), actual);
    }
}
