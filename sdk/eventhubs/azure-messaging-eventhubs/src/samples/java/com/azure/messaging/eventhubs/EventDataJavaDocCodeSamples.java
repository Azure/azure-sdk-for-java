// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventData}.
 */
public class EventDataJavaDocCodeSamples {

    /**
     * Creates an EventData using application properties.
     */
    public void createEventData() {
        // BEGIN: com.azure.messaging.eventhubs.eventdata.getProperties
        TelemetryEvent telemetry = new TelemetryEvent("temperature", "37");
        byte[] serializedTelemetryData = telemetry.toString().getBytes(UTF_8);

        EventData eventData = new EventData(serializedTelemetryData);
        eventData.getProperties().put("eventType", TelemetryEvent.class.getName());
        // END: com.azure.messaging.eventhubs.eventdata.getProperties
    }

    private static final class TelemetryEvent {
        private final String name;
        private final String value;

        private TelemetryEvent(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("[name]=%s;[value]=%s", name, value);
        }
    }
}
