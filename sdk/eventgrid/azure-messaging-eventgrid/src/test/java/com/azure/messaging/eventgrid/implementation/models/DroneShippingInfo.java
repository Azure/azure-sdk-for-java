// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;


import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class DroneShippingInfo extends ShippingInfo {
    private String droneId;

    public DroneShippingInfo(String shipmentId) {
        super(shipmentId);
    }

    /**
     * @return the drone id.
     */
    public String getDroneId() {
        return this.droneId;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("shippingType", "Drone");
        jsonWriter.writeStringField("shipmentId", getShipmentId());
        jsonWriter.writeStringField("droneId", droneId);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static DroneShippingInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String shipmentId = null;
            String droneId = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("shipmentId".equals(fieldName)) {
                    shipmentId = reader.getString();
                } else if ("droneId".equals(fieldName)) {
                    droneId = reader.getString();
                }
            }
            DroneShippingInfo droneShippingInfo = new DroneShippingInfo(shipmentId);
            droneShippingInfo.droneId = droneId;
            return droneShippingInfo;
        });
    }
}
