// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventgrid.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class ContosoItemReceivedEventData implements JsonSerializable<ContosoItemReceivedEventData> {
    private String itemSku;

    private String itemUri;

    public String getItemSku() {
        return this.itemSku;
    }

    public String getItemUri() {
        return this.itemUri;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("itemSku", itemSku);
        jsonWriter.writeStringField("itemUri", itemUri);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static ContosoItemReceivedEventData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ContosoItemReceivedEventData contosoItemReceivedEventData = new ContosoItemReceivedEventData();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("itemSku".equals(fieldName)) {
                    contosoItemReceivedEventData.itemSku = reader.getString();
                } else if ("itemUri".equals(fieldName)) {
                    contosoItemReceivedEventData.itemUri = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return contosoItemReceivedEventData;
        });
    }
}

