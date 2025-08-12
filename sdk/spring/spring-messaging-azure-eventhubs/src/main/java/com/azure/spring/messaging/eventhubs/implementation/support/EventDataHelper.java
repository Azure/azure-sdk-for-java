// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.support;

import com.azure.messaging.eventhubs.EventData;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class to process {@link EventData}.
 */
public final class EventDataHelper {

    private EventDataHelper() {
    }

    /**
     * Convert the {@link EventData} to the strings.
     * @param eventData the event data.
     * @return the strings.
     */
    // conniey: Remove warning suppression when azure-messaging-eventhubs is updated to 5.21.0.
    // https://github.com/Azure/azure-sdk-for-java/issues/46359
    @SuppressWarnings("deprecation")
    public static String toString(EventData eventData) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("body", eventData.getBodyAsString());
        map.put("offset", eventData.getOffset());
        map.put("sequenceNumber", eventData.getSequenceNumber());
        map.put("enqueuedTime", eventData.getEnqueuedTime());

        return map.toString();
    }
}
