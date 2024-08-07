// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.trafficmanager.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.trafficmanager.models.HeatMapEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficFlow;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Class representing a Traffic Manager HeatMap properties.
 */
@Fluent
public final class HeatMapProperties implements JsonSerializable<HeatMapProperties> {
    /*
     * The beginning of the time window for this HeatMap, inclusive.
     */
    private OffsetDateTime startTime;

    /*
     * The ending of the time window for this HeatMap, exclusive.
     */
    private OffsetDateTime endTime;

    /*
     * The endpoints used in this HeatMap calculation.
     */
    private List<HeatMapEndpoint> endpoints;

    /*
     * The traffic flows produced in this HeatMap calculation.
     */
    private List<TrafficFlow> trafficFlows;

    /**
     * Creates an instance of HeatMapProperties class.
     */
    public HeatMapProperties() {
    }

    /**
     * Get the startTime property: The beginning of the time window for this HeatMap, inclusive.
     * 
     * @return the startTime value.
     */
    public OffsetDateTime startTime() {
        return this.startTime;
    }

    /**
     * Set the startTime property: The beginning of the time window for this HeatMap, inclusive.
     * 
     * @param startTime the startTime value to set.
     * @return the HeatMapProperties object itself.
     */
    public HeatMapProperties withStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Get the endTime property: The ending of the time window for this HeatMap, exclusive.
     * 
     * @return the endTime value.
     */
    public OffsetDateTime endTime() {
        return this.endTime;
    }

    /**
     * Set the endTime property: The ending of the time window for this HeatMap, exclusive.
     * 
     * @param endTime the endTime value to set.
     * @return the HeatMapProperties object itself.
     */
    public HeatMapProperties withEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Get the endpoints property: The endpoints used in this HeatMap calculation.
     * 
     * @return the endpoints value.
     */
    public List<HeatMapEndpoint> endpoints() {
        return this.endpoints;
    }

    /**
     * Set the endpoints property: The endpoints used in this HeatMap calculation.
     * 
     * @param endpoints the endpoints value to set.
     * @return the HeatMapProperties object itself.
     */
    public HeatMapProperties withEndpoints(List<HeatMapEndpoint> endpoints) {
        this.endpoints = endpoints;
        return this;
    }

    /**
     * Get the trafficFlows property: The traffic flows produced in this HeatMap calculation.
     * 
     * @return the trafficFlows value.
     */
    public List<TrafficFlow> trafficFlows() {
        return this.trafficFlows;
    }

    /**
     * Set the trafficFlows property: The traffic flows produced in this HeatMap calculation.
     * 
     * @param trafficFlows the trafficFlows value to set.
     * @return the HeatMapProperties object itself.
     */
    public HeatMapProperties withTrafficFlows(List<TrafficFlow> trafficFlows) {
        this.trafficFlows = trafficFlows;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (endpoints() != null) {
            endpoints().forEach(e -> e.validate());
        }
        if (trafficFlows() != null) {
            trafficFlows().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("startTime",
            this.startTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.startTime));
        jsonWriter.writeStringField("endTime",
            this.endTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.endTime));
        jsonWriter.writeArrayField("endpoints", this.endpoints, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("trafficFlows", this.trafficFlows, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of HeatMapProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of HeatMapProperties if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the HeatMapProperties.
     */
    public static HeatMapProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            HeatMapProperties deserializedHeatMapProperties = new HeatMapProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("startTime".equals(fieldName)) {
                    deserializedHeatMapProperties.startTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("endTime".equals(fieldName)) {
                    deserializedHeatMapProperties.endTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("endpoints".equals(fieldName)) {
                    List<HeatMapEndpoint> endpoints = reader.readArray(reader1 -> HeatMapEndpoint.fromJson(reader1));
                    deserializedHeatMapProperties.endpoints = endpoints;
                } else if ("trafficFlows".equals(fieldName)) {
                    List<TrafficFlow> trafficFlows = reader.readArray(reader1 -> TrafficFlow.fromJson(reader1));
                    deserializedHeatMapProperties.trafficFlows = trafficFlows;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedHeatMapProperties;
        });
    }
}
