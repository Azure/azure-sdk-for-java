// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.webpubsub.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * SocketIO settings for the resource.
 */
@Fluent
public final class WebPubSubSocketIOSettings implements JsonSerializable<WebPubSubSocketIOSettings> {
    /*
     * The service mode of Web PubSub for Socket.IO. Values allowed:
     * "Default": have your own backend Socket.IO server
     * "Serverless": your application doesn't have a backend server
     */
    private String serviceMode;

    /**
     * Creates an instance of WebPubSubSocketIOSettings class.
     */
    public WebPubSubSocketIOSettings() {
    }

    /**
     * Get the serviceMode property: The service mode of Web PubSub for Socket.IO. Values allowed:
     * "Default": have your own backend Socket.IO server
     * "Serverless": your application doesn't have a backend server.
     * 
     * @return the serviceMode value.
     */
    public String serviceMode() {
        return this.serviceMode;
    }

    /**
     * Set the serviceMode property: The service mode of Web PubSub for Socket.IO. Values allowed:
     * "Default": have your own backend Socket.IO server
     * "Serverless": your application doesn't have a backend server.
     * 
     * @param serviceMode the serviceMode value to set.
     * @return the WebPubSubSocketIOSettings object itself.
     */
    public WebPubSubSocketIOSettings withServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("serviceMode", this.serviceMode);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of WebPubSubSocketIOSettings from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of WebPubSubSocketIOSettings if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the WebPubSubSocketIOSettings.
     */
    public static WebPubSubSocketIOSettings fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            WebPubSubSocketIOSettings deserializedWebPubSubSocketIOSettings = new WebPubSubSocketIOSettings();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("serviceMode".equals(fieldName)) {
                    deserializedWebPubSubSocketIOSettings.serviceMode = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedWebPubSubSocketIOSettings;
        });
    }
}
