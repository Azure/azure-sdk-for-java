// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The request that generated the event.
 */
@Fluent
public final class Request implements JsonSerializable<Request> {
    /*
     * The ID of the request that initiated the event.
     */
    private String id;

    /*
     * The IP or hostname and possibly port of the client connection that initiated the event. This is the RemoteAddr
     * from the standard http request.
     */
    private String addr;

    /*
     * The externally accessible hostname of the registry instance, as specified by the http host header on incoming
     * requests.
     */
    private String host;

    /*
     * The request method that generated the event.
     */
    private String method;

    /*
     * The user agent header of the request.
     */
    private String useragent;

    /**
     * Creates an instance of Request class.
     */
    public Request() {
    }

    /**
     * Get the id property: The ID of the request that initiated the event.
     * 
     * @return the id value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id property: The ID of the request that initiated the event.
     * 
     * @param id the id value to set.
     * @return the Request object itself.
     */
    public Request withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the addr property: The IP or hostname and possibly port of the client connection that initiated the event.
     * This is the RemoteAddr from the standard http request.
     * 
     * @return the addr value.
     */
    public String addr() {
        return this.addr;
    }

    /**
     * Set the addr property: The IP or hostname and possibly port of the client connection that initiated the event.
     * This is the RemoteAddr from the standard http request.
     * 
     * @param addr the addr value to set.
     * @return the Request object itself.
     */
    public Request withAddr(String addr) {
        this.addr = addr;
        return this;
    }

    /**
     * Get the host property: The externally accessible hostname of the registry instance, as specified by the http host
     * header on incoming requests.
     * 
     * @return the host value.
     */
    public String host() {
        return this.host;
    }

    /**
     * Set the host property: The externally accessible hostname of the registry instance, as specified by the http host
     * header on incoming requests.
     * 
     * @param host the host value to set.
     * @return the Request object itself.
     */
    public Request withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Get the method property: The request method that generated the event.
     * 
     * @return the method value.
     */
    public String method() {
        return this.method;
    }

    /**
     * Set the method property: The request method that generated the event.
     * 
     * @param method the method value to set.
     * @return the Request object itself.
     */
    public Request withMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * Get the useragent property: The user agent header of the request.
     * 
     * @return the useragent value.
     */
    public String useragent() {
        return this.useragent;
    }

    /**
     * Set the useragent property: The user agent header of the request.
     * 
     * @param useragent the useragent value to set.
     * @return the Request object itself.
     */
    public Request withUseragent(String useragent) {
        this.useragent = useragent;
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
        jsonWriter.writeStringField("id", this.id);
        jsonWriter.writeStringField("addr", this.addr);
        jsonWriter.writeStringField("host", this.host);
        jsonWriter.writeStringField("method", this.method);
        jsonWriter.writeStringField("useragent", this.useragent);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of Request from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of Request if the JsonReader was pointing to an instance of it, or null if it was pointing to
     * JSON null.
     * @throws IOException If an error occurs while reading the Request.
     */
    public static Request fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Request deserializedRequest = new Request();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedRequest.id = reader.getString();
                } else if ("addr".equals(fieldName)) {
                    deserializedRequest.addr = reader.getString();
                } else if ("host".equals(fieldName)) {
                    deserializedRequest.host = reader.getString();
                } else if ("method".equals(fieldName)) {
                    deserializedRequest.method = reader.getString();
                } else if ("useragent".equals(fieldName)) {
                    deserializedRequest.useragent = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRequest;
        });
    }
}
