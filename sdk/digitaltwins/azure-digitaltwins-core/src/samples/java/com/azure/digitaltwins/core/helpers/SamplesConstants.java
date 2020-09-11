// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.helpers;

public class SamplesConstants {
    public static final String ROOM_MODEL_ID = "dtmi:samples:Room;1";
    public static final String WIFI_MODEL_ID = "dtmi:samples:Wifi;1";
    public static final String BUILDING_MODEL_ID = "dtmi:samples:Building;1";
    public static final String FLOOR_MODEL_ID = "dtmi:samples:Floor;1";
    public static final String HVAC_MODEL_ID = "dtmi:samples:HVAC;1";

    public static final String TEMPORARY_COMPONENT_MODEL_PREFIX = "dtmi:samples:ComponentModel;";
    public static final String TEMPORARY_MODEL_PREFIX = "dtmi:samples:TempModel;";

    public static final String TEMPORARY_TWIN_PREFIX = "sampleTwin";

    public static final String COMPONENT_ID = "COMPONENT_ID";
    public static final String MODEL_ID = "MODEL_ID";

    public static final String TEMPORARY_COMPONENT_MODEL_PAYLOAD =
        "{" +
            "\"@id\": \"" + COMPONENT_ID + "\","+
            "\"@type\": \"Interface\"," +
            "\"@context\": \"dtmi:dtdl:context;2\"," +
            "\"displayName\": \"Component1\"," +
            "\"contents\": [" +
                "{" +
                    "\"@type\": \"Property\"," +
                    "\"name\": \"ComponentProp1\"," +
                    "\"schema\": \"string\"" +
                "}," +
                "{" +
                    "\"@type\": \"Property\"," +
                    "\"name\": \"ComponentProp2\"," +
                    "\"schema\": \"integer\"" +
                "}," +
                "{" +
                    "\"@type\": \"Telemetry\"," +
                    "\"name\": \"ComponentTelemetry1\"," +
                    "\"schema\": \"integer\"" +
                "}" +
            "]" +
        "}";

    public static final String TEMPORARY_MODEL_WITH_COMPONENT_PAYLOAD =
        "{" +
            "\"@id\": \""+ MODEL_ID + "\"," +
            "\"@type\": \"Interface\"," +
            "\"@context\": \"dtmi:dtdl:context;2\"," +
            "\"displayName\": \"TempModel\"," +
            "\"contents\": [" +
                "{" +
                    "\"@type\": \"Property\"," +
                    "\"name\": \"Prop1\"," +
                    "\"schema\": \"string\"" +
                "}," +
                "{" +
                    "\"@type\": \"Property\"," +
                    "\"name\": \"Prop2\"," +
                    "\"schema\": \"integer\"" +
                "}," +
                "{" +
                    "\"@type\": \"Component\"," +
                    "\"name\": \"Component1\"," +
                    "\"schema\": \"" + COMPONENT_ID + "\"" +
                "}," +
                "{" +
                    "\"@type\": \"Telemetry\"," +
                    "\"name\": \"Telemetry1\"," +
                    "\"schema\": \"integer\"" +
                "}" +
            "]" +
        "}";
}
