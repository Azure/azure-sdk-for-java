// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.
package com.azure.messaging.eventgrid.systemevents;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The details of the HTTP request.
 * 
 * @deprecated This class is deprecated and may be removed in future releases. System events are now available in the
 * azure-messaging-eventgrid-systemevents package.
 */
@Fluent
@Deprecated
public final class ResourceHttpRequest implements JsonSerializable<ResourceHttpRequest> {

    /*
     * The client request ID.
     */
    @Generated
    private String clientRequestId;

    /*
     * The client IP address.
     */
    @Generated
    private String clientIpAddress;

    /*
     * The request method.
     */
    @Generated
    private String method;

    /*
     * The url used in the request.
     */
    @Generated
    private String url;

    /**
     * Creates an instance of ResourceHttpRequest class.
     */
    @Generated
    public ResourceHttpRequest() {
    }

    /**
     * Get the clientRequestId property: The client request ID.
     *
     * @return the clientRequestId value.
     */
    @Generated
    public String getClientRequestId() {
        return this.clientRequestId;
    }

    /**
     * Set the clientRequestId property: The client request ID.
     *
     * @param clientRequestId the clientRequestId value to set.
     * @return the ResourceHttpRequest object itself.
     */
    @Generated
    public ResourceHttpRequest setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
        return this;
    }

    /**
     * Get the clientIpAddress property: The client IP address.
     *
     * @return the clientIpAddress value.
     */
    @Generated
    public String getClientIpAddress() {
        return this.clientIpAddress;
    }

    /**
     * Set the clientIpAddress property: The client IP address.
     *
     * @param clientIpAddress the clientIpAddress value to set.
     * @return the ResourceHttpRequest object itself.
     */
    @Generated
    public ResourceHttpRequest setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
        return this;
    }

    /**
     * Get the method property: The request method.
     *
     * @return the method value.
     */
    @Generated
    public String getMethod() {
        return this.method;
    }

    /**
     * Set the method property: The request method.
     *
     * @param method the method value to set.
     * @return the ResourceHttpRequest object itself.
     */
    @Generated
    public ResourceHttpRequest setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Get the url property: The url used in the request.
     *
     * @return the url value.
     */
    @Generated
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url property: The url used in the request.
     *
     * @param url the url value to set.
     * @return the ResourceHttpRequest object itself.
     */
    @Generated
    public ResourceHttpRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("clientRequestId", this.clientRequestId);
        jsonWriter.writeStringField("clientIpAddress", this.clientIpAddress);
        jsonWriter.writeStringField("method", this.method);
        jsonWriter.writeStringField("url", this.url);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResourceHttpRequest from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResourceHttpRequest if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResourceHttpRequest.
     */
    @Generated
    public static ResourceHttpRequest fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResourceHttpRequest deserializedResourceHttpRequest = new ResourceHttpRequest();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("clientRequestId".equals(fieldName)) {
                    deserializedResourceHttpRequest.clientRequestId = reader.getString();
                } else if ("clientIpAddress".equals(fieldName)) {
                    deserializedResourceHttpRequest.clientIpAddress = reader.getString();
                } else if ("method".equals(fieldName)) {
                    deserializedResourceHttpRequest.method = reader.getString();
                } else if ("url".equals(fieldName)) {
                    deserializedResourceHttpRequest.url = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResourceHttpRequest;
        });
    }
}
