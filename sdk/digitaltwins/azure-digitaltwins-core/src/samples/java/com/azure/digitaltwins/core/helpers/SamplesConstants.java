// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.helpers;

public class SamplesConstants {
    public static final String RoomModelId = "dtmi:samples:Room;1";
    public static final String WifiModelId = "dtmi:samples:Wifi;1";
    public static final String BuildingModelId = "dtmi:samples:Building;1";
    public static final String FloorModelId = "dtmi:samples:Floor;1";
    public static final String HvacModelId = "dtmi:samples:HVAC;1";

    public static final String TemporaryComponentModelPrefix = "dtmi:samples:ComponentModel;";
    public static final String TemporaryModelPrefix = "dtmi:samples:TempModel;";

    public static final String TemporaryTwinPrefix = "sampleTwin";

    public static final String ComponentId = "COMPONENT_ID";
    public static final String ModelId = "MODEL_ID";

    public static final String TemporaryComponentModelPayload =
        "{" +
            "\"@id\": \"" + ComponentId + "\","+
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

    public static final String TemporaryModelWithComponentPayload =
        "{" +
            "\"@id\": \""+ ModelId + "\"," +
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
                    "\"schema\": \"" + ComponentId + "\"" +
                "}," +
                "{" +
                    "\"@type\": \"Telemetry\"," +
                    "\"name\": \"Telemetry1\"," +
                    "\"schema\": \"integer\"" +
                "}" +
            "]" +
        "}";
}
