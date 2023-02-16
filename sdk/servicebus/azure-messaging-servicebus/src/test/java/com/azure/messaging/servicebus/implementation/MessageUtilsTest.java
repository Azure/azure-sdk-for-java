// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.DescribedType;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link MessageUtils}.
 */
class MessageUtilsTest {
    // Get rules message only with a default rule.
    private static final byte[] DEFAULT_RULE_MESSAGE = new byte[] {
        0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64, 64, 64, 0, 83, 116, -63, 83, 8, -95,
        10, 115, 116, 97, 116, 117, 115, 67, 111, 100, 101, 113, 0, 0, 0, -56, -95, 14, 101, 114, 114, 111, 114, 67,
        111, 110, 100, 105, 116, 105, 111, 110, 64, -95, 17, 115, 116, 97, 116, 117, 115, 68, 101, 115, 99, 114,
        105, 112, 116, 105, 111, 110, 64, -95, 25, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115, 111, 102, 116, 58,
        116, 114, 97, 99, 107, 105, 110, 103, 45, 105, 100, 64, 0, 83, 119, -63, 86, 2, -95, 5, 114, 117, 108, 101,
        115, -64, 76, 1, -63, 73, 2, -95, 16, 114, 117, 108, 101, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105,
        111, 110, 0, -128, 0, 0, 1, 55, 0, 0, 0, 4, -64, 42, 4, 0, -128, 0, 0, 0, 19, 112, 0, 0, 7, 69, 0, -128, 0,
        0, 1, 55, 0, 0, 0, 5, 69, -95, 8, 36, 68, 101, 102, 97, 117, 108, 116, -125, 0, 0, 1, -126, -112, -66, -80,
        119
    };

    // Get rules message with no rules.
    private static final byte[] NO_RULE_MESSAGE = new byte[] {
        0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64, 64, 64, 0, 83, 116, -63, -128, 8,
        -95, 10, 115, 116, 97, 116, 117, 115, 67, 111, 100, 101, 113, 0, 0, 0, -52, -95, 14, 101, 114, 114, 111,
        114, 67, 111, 110, 100, 105, 116, 105, 111, 110, -93, 29, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115,
        111, 102, 116, 58, 114, 117, 108, 101, 115, 45, 110, 111, 116, 45, 102, 111, 117, 110, 100, -95, 17, 115,
        116, 97, 116, 117, 115, 68, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, -95, 14, 78, 111, 32, 114, 117,
        108, 101, 115, 32, 102, 111, 117, 110, 100, -95, 25, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115, 111,
        102, 116, 58, 116, 114, 97, 99, 107, 105, 110, 103, 45, 105, 100, 64
    };

    // Get rules message with a default rule and two customized rules, one is correlation rule, the another one is sql
    // rule.
    private static final byte[] THREE_RULE_MESSAGE = new byte[] {
        0, 83, 115, -64, 15, 13, 64, 64, 64, 64, 64, 83, 1, 64, 64, 64, 64, 64, 64, 64, 0, 83, 116, -63, 83, 8, -95, 10,
        115, 116, 97, 116, 117, 115, 67, 111, 100, 101, 113, 0, 0, 0, -56, -95, 14, 101, 114, 114, 111, 114, 67, 111,
        110, 100, 105, 116, 105, 111, 110, 64, -95, 17, 115, 116, 97, 116, 117, 115, 68, 101, 115, 99, 114, 105, 112,
        116, 105, 111, 110, 64, -95, 25, 99, 111, 109, 46, 109, 105, 99, 114, 111, 115, 111, 102, 116, 58, 116, 114, 97,
        99, 107, 105, 110, 103, 45, 105, 100, 64, 0, 83, 119, -47, 0, 0, 1, 48, 0, 0, 0, 2, -95, 5, 114, 117, 108, 101,
        115, -48, 0, 0, 1, 32, 0, 0, 0, 3, -63, 73, 2, -95, 16, 114, 117, 108, 101, 45, 100, 101, 115, 99, 114, 105,
        112, 116, 105, 111, 110, 0, -128, 0, 0, 1, 55, 0, 0, 0, 4, -64, 42, 4, 0, -128, 0, 0, 0, 19, 112, 0, 0, 7, 69,
        0, -128, 0, 0, 1, 55, 0, 0, 0, 5, 69, -95, 8, 36, 68, 101, 102, 97, 117, 108, 116, -125, 0, 0, 1, -126, -112,
        -66, -80, 119, -63, 117, 2, -95, 16, 114, 117, 108, 101, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111,
        110, 0, -128, 0, 0, 1, 55, 0, 0, 0, 4, -64, 86, 4, 0, -128, 0, 0, 0, 19, 112, 0, 0, 9, -64, 29, 9, 64, 64, 64,
        -95, 3, 102, 111, 111, 64, 64, 64, 64, -63, 14, 2, -95, 3, 98, 97, 114, -95, 6, 114, 97, 110, 100, 111, 109, 0,
        -128, 0, 0, 1, 55, 0, 0, 0, 5, 69, -95, 22, 110, 101, 119, 45, 99, 111, 114, 114, 101, 108, 97, 116, 105, 111,
        110, 45, 102, 105, 108, 116, 101, 114, -125, 0, 0, 1, -126, -111, 22, 53, -99, -63, 88, 2, -95, 16, 114, 117,
        108, 101, 45, 100, 101, 115, 99, 114, 105, 112, 116, 105, 111, 110, 0, -128, 0, 0, 1, 55, 0, 0, 0, 4, -64, 57,
        4, 0, -128, 0, 0, 0, 19, 112, 0, 0, 6, -64, 8, 2, -95, 3, 49, 61, 49, 84, 20, 0, -128, 0, 0, 1, 55, 0, 0, 0, 5,
        69, -95, 14, 110, 101, 119, 45, 115, 113, 108, 45, 102, 105, 108, 116, 101, 114, -125, 0, 0, 1, -126, -111, 23,
        29, 49
    };

    @Test
    void convertDotNetTicksToInstant() {
        // Arrange
        final String dateTime = "2016-11-30T20:57:01.4638052Z";
        final OffsetDateTime expected = Instant.parse(dateTime).atOffset(ZoneOffset.UTC);
        final long dotNetTicks = 636161362214638052L;

        // Act
        final OffsetDateTime actual = MessageUtils.convertDotNetTicksToOffsetDateTime(dotNetTicks);

        // Assert
        assertEquals(expected, actual, "DateTime conversion from DotNet to Java failed");
    }

    @Test
    void convertDotNetBytesToUUID() {
        // Arrange
        final String guidString = "b5dc4a70-ac5d-43b3-b132-ec8fcdac3a9d";
        // Java bytes are signed where as dotNet bytes are unsigned. No problem type casting larger than 127 unsigned
        // bytes to java signed bytes as we are interested only in the individual bits for UUID conversion.
        final byte[] dotNetGuidBytes = {112, 74, (byte) 220, (byte) 181, 93, (byte) 172, (byte) 179, 67, (byte) 177,
            50, (byte) 236, (byte) 143, (byte) 205, (byte) 172, 58, (byte) 157};

        // Act
        final UUID convertedGuid = MessageUtils.convertDotNetBytesToUUID(dotNetGuidBytes);

        // Assert
        assertEquals(guidString, convertedGuid.toString(), "UUID conversion from DotNet to Java failed");
    }

    @Test
    void convertUUIDToDotNetBytes() {
        // Arrange
        String guidString = "b5dc4a70-ac5d-43b3-b132-ec8fcdac3a9d";
        UUID javaGuid = UUID.fromString(guidString);
        byte[] dotNetGuidBytes = {112, 74, (byte) 220, (byte) 181, 93, (byte) 172, (byte) 179, 67, (byte) 177, 50, (byte) 236, (byte) 143, (byte) 205, (byte) 172, 58, (byte) 157};

        // Act
        byte[] convertedBytes = MessageUtils.convertUUIDToDotNetBytes(javaGuid);

        // Assert
        assertArrayEquals(dotNetGuidBytes, convertedBytes, "UUID conversion from Java to DotNet failed");
    }

    @ParameterizedTest
    @MethodSource("checkMessageRules")
    @SuppressWarnings("unchecked")
    void verifyMessageCount(byte[] messageByte, long messageCount) {
        // Arrange
        final Message message = Proton.message();
        message.decode(messageByte, 0, messageByte.length);
        AmqpValue body = (AmqpValue) message.getBody();
        Collection<RuleProperties> ruleProperties = new ArrayList<>();

        // Act & Assert

        // No rules message's body will be null.
        if (body != null) {
            List<Map<String, DescribedType>> rules = ((Map<String, List<Map<String, DescribedType>>>) body.getValue())
                .get(ManagementConstants.RULES);
            if (rules != null) {
                // Decode all rules and add to collection.
                for (Map<String, DescribedType> rule : rules) {
                    DescribedType ruleDescription = rule.get(ManagementConstants.RULE_DESCRIPTION);
                    ruleProperties.add(MessageUtils.decodeRuleDescribedType(ruleDescription));
                }
            }
        }

        assertEquals(ruleProperties.size(), messageCount);
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifyRules() {
        // Arrange
        final Message message = Proton.message();
        message.decode(THREE_RULE_MESSAGE, 0, THREE_RULE_MESSAGE.length);
        AmqpValue body = (AmqpValue) message.getBody();
        Collection<RuleProperties> ruleProperties = new ArrayList<>();
        List<Map<String, DescribedType>> rules = ((Map<String, List<Map<String, DescribedType>>>) body.getValue())
            .get(ManagementConstants.RULES);

        // act & Assert
        for (Map<String, DescribedType> rule : rules) {
            DescribedType ruleDescription = rule.get(ManagementConstants.RULE_DESCRIPTION);
            ruleProperties.add(MessageUtils.decodeRuleDescribedType(ruleDescription));
        }
        AtomicInteger ruleCount = new AtomicInteger();
        for (RuleProperties ruleProperty : ruleProperties) {
            String ruleName = ruleProperty.getName();
            if ("$Default".equals(ruleName)) {
                assertEquals(ruleProperty.getFilter(), new TrueRuleFilter());
                ruleCount.getAndIncrement();
            } else if ("new-correlation-filter".equals(ruleName)) {
                if (ruleProperty.getFilter() instanceof CorrelationRuleFilter) {
                    CorrelationRuleFilter filter = (CorrelationRuleFilter) ruleProperty.getFilter();
                    assertEquals(filter.getReplyTo(), "foo");
                    assertEquals(filter.getProperties().get("bar"), "random");
                    ruleCount.getAndIncrement();
                }
            } else if ("new-sql-filter".equals(ruleName)) {
                assertEquals(ruleProperty.getFilter(), new TrueRuleFilter());
                ruleCount.getAndIncrement();
            }
        }
        assertEquals(ruleCount.get(), 3);
    }

    @ParameterizedTest
    @MethodSource("calculateTotalTimeoutTestData")
    void calculateTotalTimeout(AmqpRetryOptions amqpRetryOptions, long expectedResult) {
        assertEquals(expectedResult, MessageUtils.getTotalTimeout(amqpRetryOptions).toMillis());
    }

    static Stream<Arguments> calculateTotalTimeoutTestData() {
        // default value of AmqpRetryTimeOut: Max retries: 3, delay: 800ms, max delay: 1m, try timeout: 1m
        Arguments defaultValue = Arguments.of(new AmqpRetryOptions(),
            60 * 1000 + (800 + 60 * 1000) + (1600 + 60 * 1000) + (3200 + 60 * 1000));
        Arguments reachMaxDelay = Arguments.of(new AmqpRetryOptions().setDelay(Duration.ofSeconds(30)),
            60 * 1000 + (30 * 1000 + 60 * 1000) + (60 * 1000 + 60 * 1000) * 2);
        Arguments fixedDelay = Arguments.of(new AmqpRetryOptions().setMode(AmqpRetryMode.FIXED),
            60 * 1000 + (800 + 60 * 1000) * 3);
        Arguments zeroRetry = Arguments.of(new AmqpRetryOptions().setMaxRetries(0),
            60 * 1000);
        return Stream.of(defaultValue, reachMaxDelay, fixedDelay, zeroRetry);
    }

    static Stream<Arguments> checkMessageRules() {
        return Stream.of(
            Arguments.of(DEFAULT_RULE_MESSAGE, 1),
            Arguments.of(NO_RULE_MESSAGE, 0),
            Arguments.of(THREE_RULE_MESSAGE, 3)
        );
    }
}
