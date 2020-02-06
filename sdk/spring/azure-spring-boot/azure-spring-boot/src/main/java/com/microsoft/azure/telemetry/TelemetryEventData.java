/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.utils.PropertyLoader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

@Getter
public class TelemetryEventData {

    private final String name;

    @JsonProperty("iKey")
    private final String instrumentationKey;

    private final Tags tags = new Tags("Spring-on-azure", "Java-maven-plugin");

    private final EventData data = new EventData("EventData");

    private final String time;

    public TelemetryEventData(String eventName, @NonNull Map<String, String> properties) {
        Assert.hasText(eventName, "Event name should contain text.");

        name = "Microsoft.ApplicationInsights.Event";
        instrumentationKey = PropertyLoader.getTelemetryInstrumentationKey();

        data.getBaseData().setName(eventName);
        data.getBaseData().setProperties(properties);
        time = Instant.now().toString();
    }

    @Getter
    private static class Tags {

        @JsonProperty("ai.cloud.roleInstance")
        private final String aiCloudRoleInstance;

        @JsonProperty("ai.internal.sdkVersion")
        private final String aiInternalSdkVersion;

        public Tags(String instance, String sdkVersion) {
            aiCloudRoleInstance = instance;
            aiInternalSdkVersion = sdkVersion;
        }
    }

    @Getter
    private static class EventData {

        private final String baseType;

        private final CustomData baseData = new CustomData();

        public EventData(String baseType) {
            this.baseType = baseType;
        }

        @Getter
        @Setter(AccessLevel.PRIVATE)
        private static class CustomData {

            private final Integer ver = 2;

            private String name;

            private Map<String, String> properties;
        }
    }
}
