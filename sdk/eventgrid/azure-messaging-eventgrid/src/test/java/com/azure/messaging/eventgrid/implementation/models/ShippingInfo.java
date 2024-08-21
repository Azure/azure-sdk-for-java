// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class ShippingInfo implements JsonSerializable<ShippingInfo> {
    private String shipmentId;

    public ShippingInfo(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    /**
     * @return the shipment id.
     */
    public String getShipmentId() {
        return this.shipmentId;
    }




    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("shipmentId", shipmentId);
        return jsonWriter;
    }

    public static ShippingInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("shippingType".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }

                if ("Drone".equals(discriminatorValue)) {
                    return DroneShippingInfo.fromJson(readerToUse.reset());
                } else if ("Rocket".equals(discriminatorValue)) {
                    return RocketShippingInfo.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    private static ShippingInfo fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String shipmentId = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("shipmentId".equals(fieldName)) {
                    shipmentId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return new ShippingInfo(shipmentId);
        });
    }
}
