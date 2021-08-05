// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link CorrelationRuleFilter}.
 */
class CorrelationRuleFilterTest {
    @Test
    void constructorDefault() {
        // Arrange
        final String expectedToString = "CorrelationRuleFilter: ";

        // Act
        final CorrelationRuleFilter actual = new CorrelationRuleFilter();

        // Assert
        assertNull(actual.getCorrelationId());
        assertNull(actual.getContentType());
        assertNull(actual.getLabel());
        assertNull(actual.getMessageId());
        assertNull(actual.getReplyTo());
        assertNull(actual.getReplyToSessionId());
        assertNull(actual.getSessionId());
        assertNull(actual.getTo());

        final String toString = actual.toString();
        assertNotNull(toString);
        assertEquals(expectedToString, toString);
    }

    @Test
    void constructor() {
        // Arrange
        final String expected = "some-id";

        // Act
        final CorrelationRuleFilter actual = new CorrelationRuleFilter(expected);

        // Assert
        assertEquals(expected, actual.getCorrelationId());
        assertNull(actual.getContentType());
        assertNull(actual.getLabel());
        assertNull(actual.getMessageId());
        assertNull(actual.getReplyTo());
        assertNull(actual.getReplyToSessionId());
        assertNull(actual.getSessionId());
        assertNull(actual.getTo());
    }

    @Test
    void setValues() {
        // Arrange
        final String correlationId = "some-id";
        final String contentType = "some-content-type";
        final String label = "some-label";
        final String messageId = "some-message-id";
        final String replyId = "some-reply-id";
        final String replyIdSessionId = "some-reply-session-id";
        final String sessionId = "some-session-id";
        final String to = "some-to";

        final CorrelationRuleFilter actual = new CorrelationRuleFilter("fake");

        // Act
        actual.setCorrelationId(correlationId);
        actual.setContentType(contentType);
        actual.setLabel(label);
        actual.setMessageId(messageId);
        actual.setReplyTo(replyId);
        actual.setReplyToSessionId(replyIdSessionId);
        actual.setSessionId(sessionId);
        actual.setTo(to);

        // Assert
        assertEquals(correlationId, actual.getCorrelationId());
        assertEquals(contentType, actual.getContentType());
        assertEquals(label, actual.getLabel());
        assertEquals(messageId, actual.getMessageId());
        assertEquals(replyId, actual.getReplyTo());
        assertEquals(replyIdSessionId, actual.getReplyToSessionId());
        assertEquals(sessionId, actual.getSessionId());
        assertEquals(to, actual.getTo());

        final String toString = actual.toString();
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains(correlationId));
        assertTrue(toString.contains(contentType));
        assertTrue(toString.contains(label));
        assertTrue(toString.contains(messageId));
        assertTrue(toString.contains(replyId));
        assertTrue(toString.contains(replyIdSessionId));
        assertTrue(toString.contains(sessionId));
        assertTrue(toString.contains(to));
    }

    @Test
    void setProperties() {
        // Arrange
        final String key1 = "some-key1";
        final String value1 = "some-value1";
        final String key2 = "some-key2";
        final String value2 = "some-value2";
        final String correlationId = "some-id";
        final String contentType = "some-content-type";
        final String label = "some-label";

        final CorrelationRuleFilter actual = new CorrelationRuleFilter("fake");

        // Act
        actual.setCorrelationId(correlationId);
        actual.setContentType(contentType);
        actual.setLabel(label);

        actual.getProperties().put(key1, value1);
        actual.getProperties().put(key2, value2);

        // Assert
        assertEquals(correlationId, actual.getCorrelationId());
        assertEquals(contentType, actual.getContentType());
        assertEquals(label, actual.getLabel());

        final String toString = actual.toString();
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains(key1));
        assertTrue(toString.contains(value1));
        assertTrue(toString.contains(key2));
        assertTrue(toString.contains(value2));
    }
}
