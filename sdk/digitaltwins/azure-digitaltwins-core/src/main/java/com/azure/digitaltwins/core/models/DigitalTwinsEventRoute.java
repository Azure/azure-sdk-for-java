// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

// This class exists so that the public APIs don't directly consume a generated type and so that we can avoid exposing a
// validate() method that the generated type comes with when client side validation is enabled.

/**
 * The EventRoute model. Event routes are used for defining where published telemetry gets sent to. As an example, an
 * event route can point towards an Azure EventHub as a consumer of published telemetry.
 */
@Fluent
public final class DigitalTwinsEventRoute implements JsonSerializable<DigitalTwinsEventRoute> {
    /*
     * The id of the event route.
     */
    private String id;

    /*
     * The name of the endpoint this event route is bound to.
     */
    private final String endpointName;

    /*
     * An expression which describes the events which are routed to the
     * endpoint.
     */
    private String filter;

    /**
     * Creates an instance of EventRoute class.
     *
     * @param endpointName the name of the endpoint that this event route connects to.
     */
    public DigitalTwinsEventRoute(String endpointName) {
        this.endpointName = endpointName;
    }

    /**
     * Get this event route's id property: The id of the event route.
     *
     * @return the id value.
     */
    public String getEventRouteId() {
        return this.id;
    }

    /**
     * Get the endpointName property: The name of the endpoint this event route is bound to.
     *
     * @return the endpointName value.
     */
    public String getEndpointName() {
        return this.endpointName;
    }

    /**
     * Get the filter property: An expression which describes the events which are routed to the endpoint.
     *
     * @return the filter value.
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * Set the filter property: An expression which describes the events which are routed to the endpoint.
     *
     * @param filter the filter value to set.
     * @return the EventRoute object itself.
     */
    public DigitalTwinsEventRoute setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets this event route's ID.
     *
     * @param id The event route's ID to set.
     * @return the EventRoute object itself.
     */
    public DigitalTwinsEventRoute setEventRouteId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("endpointName", endpointName)
            .writeStringField("filter", filter)
            .writeEndObject();
    }

    /**
     * Reads an instance of DigitalTwinsEventRoute from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DigitalTwinsEventRoute if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IllegalStateException If the required property 'endpointName' is missing.
     * @throws IOException If an error occurs while reading the DigitalTwinsEventRoute.
     */
    public static DigitalTwinsEventRoute fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String id = null;
            boolean endpointNameFound = false;
            String endpointName = null;
            String filter = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    id = reader.getString();
                } else if ("endpointName".equals(fieldName)) {
                    endpointName = reader.getString();
                    endpointNameFound = true;
                } else if ("filter".equals(fieldName)) {
                    filter = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            if (!endpointNameFound) {
                throw new IllegalStateException("Missing required property 'endpointName'.");
            }

            return new DigitalTwinsEventRoute(endpointName)
                .setEventRouteId(id)
                .setFilter(filter);
        });
    }
}
