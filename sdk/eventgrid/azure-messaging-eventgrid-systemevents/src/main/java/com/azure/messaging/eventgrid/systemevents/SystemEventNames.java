// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.systemevents;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains a number of constants that correspond to the value of {@code eventType} of
 * {@code EventGridEvent}s and {@code code} of {@code CloudEvent}s, when the event originated from an Azure service.
 * This list should be updated with all the service event strings. It also contains a mapping from each service event
 * string to the model class that the event string corresponds to in the {@code data} field, which is used to
 * automatically deserialize system events by their known string.
 */
public final class SystemEventNames {

    private static final Map<String, Class<?>> SYSTEM_EVENT_MAPPINGS = new HashMap<String, Class<?>>();

    static {
    }

    /**
     * Get a mapping of all the system event type strings to their respective class. This is used by default in the
     * {@code EventGridEvent} and {@code CloudEvent} classes.
     *
     * @return a mapping of all the system event strings to system event objects.
     */
    public static Map<String, Class<?>> getSystemEventMappings() {
        return Collections.unmodifiableMap(SYSTEM_EVENT_MAPPINGS);
    }

    private SystemEventNames() {
    }
}
