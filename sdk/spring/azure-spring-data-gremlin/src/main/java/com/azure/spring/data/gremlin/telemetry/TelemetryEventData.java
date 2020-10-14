// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

public class TelemetryEventData {

    private final String name;

    @JsonProperty("iKey")
    private final String instrumentationKey;

    private final Tags tags = new Tags("Spring-on-azure", "Java-maven-plugin");

    private final EventData data = new EventData("EventData");

    private final String time;

    public TelemetryEventData(String eventName, @NonNull Map<String, String> properties) {
        Assert.notNull(properties, "properties should not be null");
        Assert.hasText(eventName, "Event name should contain text.");

        name = "Microsoft.ApplicationInsights.Event";
        instrumentationKey = PropertyLoader.getTelemetryInstrumentationKey();

        data.getBaseData().setName(eventName);
        data.getBaseData().setProperties(properties);
        time = Instant.now().toString();
    }

    private static class Tags {

        @JsonProperty("ai.cloud.roleInstance")
        private final String aiCloudRoleInstance;

        @JsonProperty("ai.internal.sdkVersion")
        private final String aiInternalSdkVersion;

        Tags(String instance, String sdkVersion) {
            aiCloudRoleInstance = instance;
            aiInternalSdkVersion = sdkVersion;
        }

        public String getAiCloudRoleInstance() {
            return aiCloudRoleInstance;
        }

        public String getAiInternalSdkVersion() {
            return aiInternalSdkVersion;
        }
    }

    private static class EventData {

        private final String baseType;

        private final CustomData baseData = new CustomData();

        EventData(String baseType) {
            this.baseType = baseType;
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

            private void setName(String name) {
                this.name = name;
            }

            private void setProperties(Map<String, String> properties) {
                this.properties = properties;
            }
        }

        public String getBaseType() {
            return baseType;
        }

        public CustomData getBaseData() {
            return baseData;
        }
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
}
