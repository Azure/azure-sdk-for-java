// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.converter;

import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.cloud.service.eventhubs.properties.StartPositionProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.azure.spring.cloud.service.implementation.converter.EventPositionConverter.EVENT_POSITION_CONVERTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventPositionConverterTests {

    @Test
    void shouldGetEarliestOffset() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setOffset("earliest");

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        assertEquals(EventPosition.earliest(), converted);
    }

    @Test
    void shouldGetEarliestOffsetForAnotherCase() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setOffset("EarLiesT");

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        assertEquals(EventPosition.earliest(), converted);
    }

    @Test
    void shouldGetLatestOffset() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setOffset("latest");

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        assertEquals(EventPosition.latest(), converted);
    }

    @Test
    void shouldGetLatestOffsetForAnotherCase() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setOffset("LatesT");

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        assertEquals(EventPosition.latest(), converted);
    }

    @Test
    void shouldGetLatestOffsetForUnknownOffset() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setOffset("random-unknown");

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        assertEquals(EventPosition.latest(), converted);
    }

    @Test
    void shouldGetNumberOffset() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setOffset("123");

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        EventPosition expected = EventPosition.fromOffset(123L);
        assertEquals(expected, converted);
        assertFalse(expected.isInclusive());
    }

    @Test
    void inclusiveShouldNotAffectOffset() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setOffset("123");
        properties.setInclusive(true);

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        EventPosition expected = EventPosition.fromOffset(123L);
        assertEquals(expected, converted);
        assertFalse(expected.isInclusive());
    }

    @Test
    void shouldGetSequenceNumber() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setSequenceNumber(123L);

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        assertEquals(EventPosition.fromSequenceNumber(123L), converted);
        assertFalse(converted.isInclusive());
    }

    @Test
    void shouldGetSequenceNumberInclusive() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setSequenceNumber(123L);
        properties.setInclusive(true);

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        EventPosition expected = EventPosition.fromSequenceNumber(123L, true);
        assertEquals(expected, converted);
        assertTrue(converted.isInclusive());
    }

    @Test
    void shouldGetEnqueuedDateTime() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setEnqueuedDateTime(Instant.MAX);

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        EventPosition expected = EventPosition.fromEnqueuedTime(Instant.MAX);
        assertEquals(expected, converted);
        assertFalse(converted.isInclusive());
    }

    @Test
    void inclusiveShouldNotAffectEnqueuedDateTime() {
        StartPositionProperties properties = new StartPositionProperties();
        properties.setEnqueuedDateTime(Instant.MAX);
        properties.setInclusive(true);

        EventPosition converted = EVENT_POSITION_CONVERTER.convert(properties);
        EventPosition expected = EventPosition.fromEnqueuedTime(Instant.MAX);
        assertEquals(expected, converted);
        assertFalse(converted.isInclusive());
    }

}
