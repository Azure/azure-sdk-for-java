/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.util;

import com.azure.messaging.eventhubs.EventData;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventDataHelper {

    public static String toString(EventData eventData) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("body", eventData.getBodyAsString());
        map.put("offset", eventData.getOffset());
        map.put("sequenceNumber", eventData.getSequenceNumber());
        map.put("enqueuedTime", eventData.getEnqueuedTime());

        return map.toString();
    }
}
