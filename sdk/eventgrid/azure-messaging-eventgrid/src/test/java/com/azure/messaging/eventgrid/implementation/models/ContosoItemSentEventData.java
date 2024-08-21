// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;


import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class ContosoItemSentEventData implements JsonSerializable<ContosoItemSentEventData> {
    private ShippingInfo shippingInfo;

    /**
     * @return the shipping info.
     */
    public ShippingInfo getShippingInfo() {
        return this.shippingInfo;
    }


    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("shippingInfo", shippingInfo);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public ContosoItemSentEventData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ContosoItemSentEventData contosoItemSentEventData = new ContosoItemSentEventData();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("shippingInfo".equals(fieldName)) {
                    contosoItemSentEventData.shippingInfo = ShippingInfo.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return contosoItemSentEventData;
        });
    }
}
