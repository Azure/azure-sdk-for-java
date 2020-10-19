// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.telemetry;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class TelemetryEventData {

    private static final String NAME = "Microsoft.ApplicationInsights.Event";

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
        return NAME;
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


        private static final String AI_CLOUD_ROLE_INSTANCE = "Spring-on-azure";


        private static final String AI_INTERNAL_SDK_VERSION = "Java-maven-plugin";

        @JsonProperty("ai.cloud.roleInstance")
        public String getAiCloudRoleInstance() {
            return AI_CLOUD_ROLE_INSTANCE;
        }

        @JsonProperty("ai.internal.sdkVersion")
        public String getAiInternalSdkVersion() {
            return AI_INTERNAL_SDK_VERSION;
        }
    }

    private static class EventData {

        private static final String BASE_TYPE = "EventData";

        private final CustomData baseData = new CustomData();

        public CustomData getBaseData() {
            return baseData;
        }

        public String getBaseType() {
            return BASE_TYPE;
        }

        private static class CustomData {

            private static final Integer VER = 2;

            private String name;

            private Map<String, String> properties;

            public Integer getVer() {
                return VER;
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
