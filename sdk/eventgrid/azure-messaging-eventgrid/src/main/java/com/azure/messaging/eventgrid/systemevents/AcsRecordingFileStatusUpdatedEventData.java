// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.
package com.azure.messaging.eventgrid.systemevents;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Schema of the Data property of an EventGridEvent for a Microsoft.Communication.RecordingFileStatusUpdated event.
 * 
 * @deprecated This class is deprecated and may be removed in future releases. System events are now available in the
 * azure-messaging-eventgrid-systemevents package.
 */
@Fluent
@Deprecated
public final class AcsRecordingFileStatusUpdatedEventData
    implements JsonSerializable<AcsRecordingFileStatusUpdatedEventData> {

    /*
     * The details of recording storage information
     */
    @Generated
    private AcsRecordingStorageInfoProperties recordingStorageInfo;

    /*
     * The time at which the recording started
     */
    @Generated
    private OffsetDateTime recordingStartTime;

    /*
     * The recording duration in milliseconds
     */
    @Generated
    private Long recordingDurationMs;

    /*
     * The recording content type- AudioVideo, or Audio
     */
    @Generated
    private RecordingContentType recordingContentType;

    /*
     * The recording channel type - Mixed, Unmixed
     */
    @Generated
    private RecordingChannelType recordingChannelType;

    /*
     * The recording format type - Mp4, Mp3, Wav
     */
    @Generated
    private RecordingFormatType recordingFormatType;

    /*
     * The reason for ending recording session
     */
    @Generated
    private String sessionEndReason;

    /**
     * Creates an instance of AcsRecordingFileStatusUpdatedEventData class.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData() {
    }

    /**
     * Get the recordingStorageInfo property: The details of recording storage information.
     *
     * @return the recordingStorageInfo value.
     */
    @Generated
    public AcsRecordingStorageInfoProperties getRecordingStorageInfo() {
        return this.recordingStorageInfo;
    }

    /**
     * Set the recordingStorageInfo property: The details of recording storage information.
     *
     * @param recordingStorageInfo the recordingStorageInfo value to set.
     * @return the AcsRecordingFileStatusUpdatedEventData object itself.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData
        setRecordingStorageInfo(AcsRecordingStorageInfoProperties recordingStorageInfo) {
        this.recordingStorageInfo = recordingStorageInfo;
        return this;
    }

    /**
     * Get the recordingStartTime property: The time at which the recording started.
     *
     * @return the recordingStartTime value.
     */
    @Generated
    public OffsetDateTime getRecordingStartTime() {
        return this.recordingStartTime;
    }

    /**
     * Set the recordingStartTime property: The time at which the recording started.
     *
     * @param recordingStartTime the recordingStartTime value to set.
     * @return the AcsRecordingFileStatusUpdatedEventData object itself.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData setRecordingStartTime(OffsetDateTime recordingStartTime) {
        this.recordingStartTime = recordingStartTime;
        return this;
    }

    /**
     * Get the recordingDuration property: The recording duration.
     *
     * @return the recordingDuration value.
     */
    @Generated
    public Duration getRecordingDuration() {
        if (this.recordingDurationMs != null) {
            return Duration.ofMillis(this.recordingDurationMs);
        }
        return null;
    }

    /**
     * Set the recordingDuration property: The recording duration.
     *
     * @param recordingDuration the recordingDuration value to set.
     * @return the AcsRecordingFileStatusUpdatedEventData object itself.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData setRecordingDuration(Duration recordingDuration) {
        if (recordingDuration != null) {
            this.recordingDurationMs = recordingDuration.toMillis();
        } else {
            this.recordingDurationMs = null;
        }
        return this;
    }

    /**
     * Get the recordingContentType property: The recording content type- AudioVideo, or Audio.
     *
     * @return the recordingContentType value.
     */
    @Generated
    public RecordingContentType getRecordingContentType() {
        return this.recordingContentType;
    }

    /**
     * Set the recordingContentType property: The recording content type- AudioVideo, or Audio.
     *
     * @param recordingContentType the recordingContentType value to set.
     * @return the AcsRecordingFileStatusUpdatedEventData object itself.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData setRecordingContentType(RecordingContentType recordingContentType) {
        this.recordingContentType = recordingContentType;
        return this;
    }

    /**
     * Get the recordingChannelType property: The recording channel type - Mixed, Unmixed.
     *
     * @return the recordingChannelType value.
     */
    @Generated
    public RecordingChannelType getRecordingChannelType() {
        return this.recordingChannelType;
    }

    /**
     * Set the recordingChannelType property: The recording channel type - Mixed, Unmixed.
     *
     * @param recordingChannelType the recordingChannelType value to set.
     * @return the AcsRecordingFileStatusUpdatedEventData object itself.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData setRecordingChannelType(RecordingChannelType recordingChannelType) {
        this.recordingChannelType = recordingChannelType;
        return this;
    }

    /**
     * Get the recordingFormatType property: The recording format type - Mp4, Mp3, Wav.
     *
     * @return the recordingFormatType value.
     */
    @Generated
    public RecordingFormatType getRecordingFormatType() {
        return this.recordingFormatType;
    }

    /**
     * Set the recordingFormatType property: The recording format type - Mp4, Mp3, Wav.
     *
     * @param recordingFormatType the recordingFormatType value to set.
     * @return the AcsRecordingFileStatusUpdatedEventData object itself.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData setRecordingFormatType(RecordingFormatType recordingFormatType) {
        this.recordingFormatType = recordingFormatType;
        return this;
    }

    /**
     * Get the sessionEndReason property: The reason for ending recording session.
     *
     * @return the sessionEndReason value.
     */
    @Generated
    public String getSessionEndReason() {
        return this.sessionEndReason;
    }

    /**
     * Set the sessionEndReason property: The reason for ending recording session.
     *
     * @param sessionEndReason the sessionEndReason value to set.
     * @return the AcsRecordingFileStatusUpdatedEventData object itself.
     */
    @Generated
    public AcsRecordingFileStatusUpdatedEventData setSessionEndReason(String sessionEndReason) {
        this.sessionEndReason = sessionEndReason;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("recordingStorageInfo", this.recordingStorageInfo);
        jsonWriter.writeStringField("recordingStartTime",
            this.recordingStartTime == null
                ? null
                : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.recordingStartTime));
        jsonWriter.writeNumberField("recordingDurationMs", this.recordingDurationMs);
        jsonWriter.writeStringField("recordingContentType",
            this.recordingContentType == null ? null : this.recordingContentType.toString());
        jsonWriter.writeStringField("recordingChannelType",
            this.recordingChannelType == null ? null : this.recordingChannelType.toString());
        jsonWriter.writeStringField("recordingFormatType",
            this.recordingFormatType == null ? null : this.recordingFormatType.toString());
        jsonWriter.writeStringField("sessionEndReason", this.sessionEndReason);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AcsRecordingFileStatusUpdatedEventData from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AcsRecordingFileStatusUpdatedEventData if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AcsRecordingFileStatusUpdatedEventData.
     */
    @Generated
    public static AcsRecordingFileStatusUpdatedEventData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AcsRecordingFileStatusUpdatedEventData deserializedAcsRecordingFileStatusUpdatedEventData
                = new AcsRecordingFileStatusUpdatedEventData();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("recordingStorageInfo".equals(fieldName)) {
                    deserializedAcsRecordingFileStatusUpdatedEventData.recordingStorageInfo
                        = AcsRecordingStorageInfoProperties.fromJson(reader);
                } else if ("recordingStartTime".equals(fieldName)) {
                    deserializedAcsRecordingFileStatusUpdatedEventData.recordingStartTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("recordingDurationMs".equals(fieldName)) {
                    deserializedAcsRecordingFileStatusUpdatedEventData.recordingDurationMs
                        = reader.getNullable(JsonReader::getLong);
                } else if ("recordingContentType".equals(fieldName)) {
                    deserializedAcsRecordingFileStatusUpdatedEventData.recordingContentType
                        = RecordingContentType.fromString(reader.getString());
                } else if ("recordingChannelType".equals(fieldName)) {
                    deserializedAcsRecordingFileStatusUpdatedEventData.recordingChannelType
                        = RecordingChannelType.fromString(reader.getString());
                } else if ("recordingFormatType".equals(fieldName)) {
                    deserializedAcsRecordingFileStatusUpdatedEventData.recordingFormatType
                        = RecordingFormatType.fromString(reader.getString());
                } else if ("sessionEndReason".equals(fieldName)) {
                    deserializedAcsRecordingFileStatusUpdatedEventData.sessionEndReason = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedAcsRecordingFileStatusUpdatedEventData;
        });
    }
}
