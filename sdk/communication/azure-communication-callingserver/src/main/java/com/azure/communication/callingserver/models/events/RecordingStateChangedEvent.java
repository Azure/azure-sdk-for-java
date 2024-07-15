// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.communication.callingserver.models.RecordingState;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/** The RecordingStateChangedEvent model. */
@Immutable
public final class RecordingStateChangedEvent extends CallAutomationEventBase {

    /**
     * Recording Id.
     */
    private final String recordingId;

    /**
     * Recording state.
     */
    private final RecordingState recordingState;

    /**
     * Time of when it started recording.
     */
    private final OffsetDateTime startDateTime;

    private RecordingStateChangedEvent(OffsetDateTime startDateTime, String recordingId, RecordingState recordingState) {
        this.startDateTime = startDateTime;
        this.recordingId = recordingId;
        this.recordingState = recordingState;
    }

    /**
     * Get the recordingId property: Recording Id.
     *
     * @return the recordingId value.
     */
    public String getRecordingId() {
        return recordingId;
    }

    /**
     * Get the recordingState property: Recording State.
     *
     * @return the recordingState value.
     */
    public RecordingState getRecordingState() {
        return recordingState;
    }

    /**
     * Get the startDateTime property: Start Date time.
     *
     * @return the startDateTime value.
     */
    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("recordingId", recordingId);
        jsonWriter.writeStringField("state", recordingState == null ? null : recordingState.toString());
        jsonWriter.writeStringField("startDateTime", startDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        jsonWriter.writeStringField("callConnectionId", super.getCallConnectionId());
        jsonWriter.writeStringField("serverCallId", super.getServerCallId());
        jsonWriter.writeStringField("correlationId", super.getCorrelationId());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RecordingStateChangedEvent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecordingStateChangedEvent if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RecordingStateChangedEvent.
     */
    public static RecordingStateChangedEvent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String recordingId = null;
            RecordingState recordingState = null;
            OffsetDateTime startDateTime = null;
            String callConnectionId = null;
            String serverCallId = null;
            String correlationId = null;
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("recordingId".equals(fieldName)) {
                    recordingId = reader.getString();
                } else if ("state".equals(fieldName)) {
                    recordingState = RecordingState.fromString(reader.getString());
                } else if ("startDateTime".equals(fieldName)) {
                    String value = reader.getString();
                    if (!CoreUtils.isNullOrEmpty(value)) {
                        startDateTime = OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    }
                } else if ("callConnectionId".equals(fieldName)) {
                    callConnectionId = reader.getString();
                } else if ("serverCallId".equals(fieldName)) {
                    serverCallId = reader.getString();
                } else if ("correlationId".equals(fieldName)) {
                    correlationId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            final RecordingStateChangedEvent event = new RecordingStateChangedEvent(startDateTime, recordingId, recordingState);
            event.setCorrelationId(correlationId)
                .setServerCallId(serverCallId)
                .setCallConnectionId(callConnectionId);
            return event;
        });
    }
}
