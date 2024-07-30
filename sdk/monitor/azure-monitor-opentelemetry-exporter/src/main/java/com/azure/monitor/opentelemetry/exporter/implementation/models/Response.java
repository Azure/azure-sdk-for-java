// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

public final class Response implements JsonSerializable<Response> {
    private int itemsReceived;
    private int itemsAccepted;
    private String appId;
    private List<ResponseError> errors;


    public int getItemsReceived() {
        return itemsReceived;
    }

    public Response setItemsReceived(int itemReceived) {
        this.itemsReceived = itemReceived;
        return this;
    }

    public int getItemsAccepted() {
        return itemsAccepted;
    }

    public Response setItemsAccepted(int itemsAccepted) {
        this.itemsAccepted = itemsAccepted;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public Response setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public List<ResponseError> getErrors() {
        return errors;
    }

    public Response setErrors(List<ResponseError> errors) {
        this.errors = errors;
        return this;
    }

    public String toString() {
        return "Response{" +
            "itemsReceived=" + itemsReceived +
            ", itemsAccepted=" + itemsAccepted +
            ", appId='" + appId + '\'' +
            ", errors=" + errors +
            '}';
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeIntField("itemsReceived", itemsReceived)
            .writeIntField("itemsAccepted", itemsAccepted)
            .writeStringField("appId", appId)
            .writeArrayField("errors", errors, JsonWriter::writeJson)
            .writeEndObject();
    }

    public static Response fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Response deserializedValue = new Response();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                // In this case field names are case-sensitive but this could be replaced with 'equalsIgnoreCase' to
                // make them case-insensitive.
                if ("itemsReceived".equals(fieldName)) {
                    deserializedValue.setItemsReceived(reader.getInt());
                } else if ("itemsAccepted".equals(fieldName)) {
                    deserializedValue.setItemsAccepted(reader.getInt());
                } else if ("appId".equals(fieldName)) {
                    deserializedValue.setAppId(reader.getString());
                } else if ("errors".equals(fieldName)) {
                    List<ResponseError> errorList = reader.readArray(ResponseError::fromJson);
                    deserializedValue.setErrors(errorList);
                } else {
                    // Fallthrough case of an unknown property. In this instance the value is skipped, if it's a JSON
                    // array or object the reader will progress until it terminated. This could also throw an exception
                    // if unknown properties should cause that or be read into an additional properties Map for further
                    // usage.
                    reader.skipChildren();
                }
            }

            return deserializedValue;
        });
    }
}
