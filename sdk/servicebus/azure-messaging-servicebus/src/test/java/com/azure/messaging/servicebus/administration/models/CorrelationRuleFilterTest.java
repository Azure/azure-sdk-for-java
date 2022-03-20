// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void testEqualsAndHashCode() {
        CorrelationRuleFilter filter1 = new CorrelationRuleFilter();
        CorrelationRuleFilter filter2 = new CorrelationRuleFilter();
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1 = new CorrelationRuleFilter("id1");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2 = new CorrelationRuleFilter("id2");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2 = new CorrelationRuleFilter("id1");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setContentType("myContent");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setContentType("anotherType");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setContentType("myContent");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setLabel("thisLabel");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setLabel("anotherLabel");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setLabel("thisLabel");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setMessageId("1234");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setMessageId("5678");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setMessageId("1234");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.getProperties().put("key", "value");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.getProperties().put("key", "eulav");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.getProperties().put("key", "value");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setReplyTo("replyToAddress");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setReplyTo("smething else");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setReplyTo("replyToAddress");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setReplyToSessionId("9876");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setReplyToSessionId("5432");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setReplyToSessionId("9876");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setSessionId("ThisSession");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setSessionId("ThatSession");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setSessionId("ThisSession");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1.setTo("Fred");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setTo("Barney");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2.setTo("Fred");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

    }
}
