// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.mediaservices.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.mediaservices.models.LiveEventHealthStatus;
import com.azure.resourcemanager.mediaservices.models.LiveEventIngestion;
import com.azure.resourcemanager.mediaservices.models.LiveEventState;
import com.azure.resourcemanager.mediaservices.models.LiveEventTrackStatus;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The live event status.
 */
@Fluent
public final class LiveEventStatusInner implements JsonSerializable<LiveEventStatusInner> {
    /*
     * Current state of the live event. See https://go.microsoft.com/fwlink/?linkid=2139012 for more information.
     */
    private LiveEventState state;

    /*
     * Health status of last 20 seconds.
     */
    private LiveEventHealthStatus healthStatus;

    /*
     * List of strings justifying the health status.
     */
    private List<String> healthDescriptions;

    /*
     * Last updated UTC time of this status.
     */
    private OffsetDateTime lastUpdatedTime;

    /*
     * Live event ingestion entry.
     */
    private LiveEventIngestion ingestion;

    /*
     * Track entry list.
     */
    private List<LiveEventTrackStatus> trackStatus;

    /**
     * Creates an instance of LiveEventStatusInner class.
     */
    public LiveEventStatusInner() {
    }

    /**
     * Get the state property: Current state of the live event. See https://go.microsoft.com/fwlink/?linkid=2139012 for
     * more information.
     * 
     * @return the state value.
     */
    public LiveEventState state() {
        return this.state;
    }

    /**
     * Set the state property: Current state of the live event. See https://go.microsoft.com/fwlink/?linkid=2139012 for
     * more information.
     * 
     * @param state the state value to set.
     * @return the LiveEventStatusInner object itself.
     */
    public LiveEventStatusInner withState(LiveEventState state) {
        this.state = state;
        return this;
    }

    /**
     * Get the healthStatus property: Health status of last 20 seconds.
     * 
     * @return the healthStatus value.
     */
    public LiveEventHealthStatus healthStatus() {
        return this.healthStatus;
    }

    /**
     * Set the healthStatus property: Health status of last 20 seconds.
     * 
     * @param healthStatus the healthStatus value to set.
     * @return the LiveEventStatusInner object itself.
     */
    public LiveEventStatusInner withHealthStatus(LiveEventHealthStatus healthStatus) {
        this.healthStatus = healthStatus;
        return this;
    }

    /**
     * Get the healthDescriptions property: List of strings justifying the health status.
     * 
     * @return the healthDescriptions value.
     */
    public List<String> healthDescriptions() {
        return this.healthDescriptions;
    }

    /**
     * Set the healthDescriptions property: List of strings justifying the health status.
     * 
     * @param healthDescriptions the healthDescriptions value to set.
     * @return the LiveEventStatusInner object itself.
     */
    public LiveEventStatusInner withHealthDescriptions(List<String> healthDescriptions) {
        this.healthDescriptions = healthDescriptions;
        return this;
    }

    /**
     * Get the lastUpdatedTime property: Last updated UTC time of this status.
     * 
     * @return the lastUpdatedTime value.
     */
    public OffsetDateTime lastUpdatedTime() {
        return this.lastUpdatedTime;
    }

    /**
     * Set the lastUpdatedTime property: Last updated UTC time of this status.
     * 
     * @param lastUpdatedTime the lastUpdatedTime value to set.
     * @return the LiveEventStatusInner object itself.
     */
    public LiveEventStatusInner withLastUpdatedTime(OffsetDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
        return this;
    }

    /**
     * Get the ingestion property: Live event ingestion entry.
     * 
     * @return the ingestion value.
     */
    public LiveEventIngestion ingestion() {
        return this.ingestion;
    }

    /**
     * Set the ingestion property: Live event ingestion entry.
     * 
     * @param ingestion the ingestion value to set.
     * @return the LiveEventStatusInner object itself.
     */
    public LiveEventStatusInner withIngestion(LiveEventIngestion ingestion) {
        this.ingestion = ingestion;
        return this;
    }

    /**
     * Get the trackStatus property: Track entry list.
     * 
     * @return the trackStatus value.
     */
    public List<LiveEventTrackStatus> trackStatus() {
        return this.trackStatus;
    }

    /**
     * Set the trackStatus property: Track entry list.
     * 
     * @param trackStatus the trackStatus value to set.
     * @return the LiveEventStatusInner object itself.
     */
    public LiveEventStatusInner withTrackStatus(List<LiveEventTrackStatus> trackStatus) {
        this.trackStatus = trackStatus;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (ingestion() != null) {
            ingestion().validate();
        }
        if (trackStatus() != null) {
            trackStatus().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("state", this.state == null ? null : this.state.toString());
        jsonWriter.writeStringField("healthStatus", this.healthStatus == null ? null : this.healthStatus.toString());
        jsonWriter.writeArrayField("healthDescriptions", this.healthDescriptions,
            (writer, element) -> writer.writeString(element));
        jsonWriter.writeStringField("lastUpdatedTime",
            this.lastUpdatedTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.lastUpdatedTime));
        jsonWriter.writeJsonField("ingestion", this.ingestion);
        jsonWriter.writeArrayField("trackStatus", this.trackStatus, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of LiveEventStatusInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of LiveEventStatusInner if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the LiveEventStatusInner.
     */
    public static LiveEventStatusInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            LiveEventStatusInner deserializedLiveEventStatusInner = new LiveEventStatusInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("state".equals(fieldName)) {
                    deserializedLiveEventStatusInner.state = LiveEventState.fromString(reader.getString());
                } else if ("healthStatus".equals(fieldName)) {
                    deserializedLiveEventStatusInner.healthStatus
                        = LiveEventHealthStatus.fromString(reader.getString());
                } else if ("healthDescriptions".equals(fieldName)) {
                    List<String> healthDescriptions = reader.readArray(reader1 -> reader1.getString());
                    deserializedLiveEventStatusInner.healthDescriptions = healthDescriptions;
                } else if ("lastUpdatedTime".equals(fieldName)) {
                    deserializedLiveEventStatusInner.lastUpdatedTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("ingestion".equals(fieldName)) {
                    deserializedLiveEventStatusInner.ingestion = LiveEventIngestion.fromJson(reader);
                } else if ("trackStatus".equals(fieldName)) {
                    List<LiveEventTrackStatus> trackStatus
                        = reader.readArray(reader1 -> LiveEventTrackStatus.fromJson(reader1));
                    deserializedLiveEventStatusInner.trackStatus = trackStatus;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedLiveEventStatusInner;
        });
    }
}
