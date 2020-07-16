// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Map;

/**
 * Data format class for telemetry event.
 */
public class TelemetryEventData {

    private final String name;

    @JsonProperty("iKey")
    private final String instrumentationKey;

    private final Tags tags = new Tags("Spring-on-azure", "Java-maven-plugin");

    private final EventData data = new EventData("EventData");

    private final String time;

    /**
     * Initialize data of a telemetry event
     *
     * @param eventName specify an event
     * @param properties properties of event
     */
    public TelemetryEventData(String eventName, @NonNull Map<String, String> properties) {
        Assert.hasText(eventName, "Event name should contain text.");

        name = "Microsoft.ApplicationInsights.Event";
        instrumentationKey = PropertyLoader.getTelemetryInstrumentationKey();

        data.getBaseData().setName(eventName);
        data.getBaseData().setProperties(properties);
        time = Instant.now().toString();
    }

    /**
     * Get name of event
     *
     * @return name value
     */
    public String getName() {
        return name;
    }

    /**
     * Get instrumentationKey of event
     *
     * @return instrumentationKey value
     */
    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    /**
     * Get tags of event
     *
     * @return Tags value
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * Get data of event
     *
     * @return EventData value
     */
    public EventData getData() {
        return data;
    }

    /**
     * Get time of event
     *
     * @return time value
     */
    public String getTime() {
        return time;
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

        public String getBaseType() {
            return baseType;
        }

        public CustomData getBaseData() {
            return baseData;
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

            private void setName(String name) {
                this.name = name;
            }

            public Map<String, String> getProperties() {
                return properties;
            }

            private void setProperties(Map<String, String> properties) {
                this.properties = properties;
            }
        }
    }
}
