// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class RocketShippingInfo extends ShippingInfo {
    private String rocketNumber;

    public RocketShippingInfo(String shipmentId) {
        super(shipmentId);
    }
    /**
     * @return the rocket number.
     */
    public String getRocketNumber() {
        return this.rocketNumber;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("shippingType", "Rocket");
        jsonWriter.writeStringField("shipmentId", getShipmentId());
        jsonWriter.writeStringField("rocketNumber", rocketNumber);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static RocketShippingInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String shipmentId = null;
            String rocketNumber = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("shipmentId".equals(fieldName)) {
                    shipmentId = reader.getString();
                } else if ("rocketNumber".equals(fieldName)) {
                    rocketNumber = reader.getString();
                }
            }
            RocketShippingInfo rocketShippingInfo = new RocketShippingInfo(shipmentId);
            rocketShippingInfo.rocketNumber = rocketNumber;
            return rocketShippingInfo;
        });
    }
}
