/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

public class TelemetryEventData {

    private final String name = "Microsoft.ApplicationInsights.Event";

    @JsonProperty("iKey")
    private final String instrumentationKey;

    private final Tags tags = new Tags();

    private final EventData data = new EventData();

    private final String time = Instant.now().toString();

    public TelemetryEventData(String eventName, @NonNull Map<String, String> properties, String instrumentationKey) {
        Assert.hasText(eventName, "Event name should contain text.");
        Assert.hasText(instrumentationKey, "Instrumentation key should contain text.");

        this.instrumentationKey = instrumentationKey;
        this.data.getBaseData().setName(eventName);
        this.data.getBaseData().setProperties(properties);
    }

    public String getName() {
        return name;
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    public Tags getTags() {
        return tags;
    }

    public EventData getData() {
        return data;
    }

    public String getTime() {
        return time;
    }

    private static class Tags {

        @JsonProperty("ai.cloud.roleInstance")
        private final String aiCloudRoleInstance = "Spring-on-azure";

        @JsonProperty("ai.internal.sdkVersion")
        private final String aiInternalSdkVersion = "Java-maven-plugin";

        public String getAiCloudRoleInstance() {
            return aiCloudRoleInstance;
        }

        public String getAiInternalSdkVersion() {
            return aiInternalSdkVersion;
        }
    }

    private static class EventData {

        private final String baseType = "EventData";

        private final CustomData baseData = new CustomData();

        public CustomData getBaseData() {
            return baseData;
        }

        public String getBaseType() {
            return baseType;
        }

        private static class CustomData {

            private final Integer ver = 2;

            private String name;

            private Map<String, String> properties;

            public Integer getVer() {
                return ver;
            }

            public String getName() {
                return name;
            }

            public Map<String, String> getProperties() {
                return properties;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void setProperties(Map<String, String> properties) {
                this.properties = properties;
            }
        }
    }
}
