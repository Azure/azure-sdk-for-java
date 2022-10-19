// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.converter;

import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventProcessorClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * Converts a {@link EventProcessorClientProperties.StartPosition} to a {@link EventPosition}.
 */
public final class EventPositionConverter implements Converter<EventProcessorClientProperties.StartPosition, EventPosition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPositionConverter.class);
    private static final String EARLIEST = "earliest";
    private static final String LATEST = "latest";
    public static final EventPositionConverter EVENT_POSITION_CONVERTER = new EventPositionConverter();

    private EventPositionConverter() {

    }

    @NonNull
    @Override
    public EventPosition convert(EventProcessorClientProperties.StartPosition source) {
        if (StringUtils.hasText(source.getOffset())) {
            if (EARLIEST.equalsIgnoreCase(source.getOffset())) {
                return EventPosition.earliest();
            }
            if (LATEST.equalsIgnoreCase(source.getOffset())) {
                return EventPosition.latest();
            }
            try {
                long offset = Long.parseLong(source.getOffset());
                return EventPosition.fromOffset(offset);
            } catch (NumberFormatException exception) {
                LOGGER.warn("The offset {} provided is not parsable, will ignore it", source.getOffset());
            }
        }
        if (source.getSequenceNumber() != null) {
            return EventPosition.fromSequenceNumber(source.getSequenceNumber(), source.isInclusive());
        }
        if (source.getEnqueuedDateTime() != null) {
            return EventPosition.fromEnqueuedTime(source.getEnqueuedDateTime());
        }
        return EventPosition.latest();
    }
}
