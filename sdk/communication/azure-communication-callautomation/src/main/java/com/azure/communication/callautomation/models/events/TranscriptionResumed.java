// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/**
 * The TranscriptionResumed model.
 */
@Fluent
public final class TranscriptionResumed extends CallAutomationEventBase {

    /*
     * Contains the resulting SIP code/sub-code and message from NGC services.
     */
    @JsonProperty(value = "resultInformation", access = JsonProperty.Access.WRITE_ONLY)
    private ResultInformation resultInformation;

    /*
     * Defines the result for TranscriptionUpdate with the current status and the details about the status
     */
    @JsonProperty(value = "transcriptionUpdate", access = JsonProperty.Access.WRITE_ONLY)
    private TranscriptionUpdate transcriptionUpdateResult;

    /**
     * Creates an instance of TranscriptionResumed class.
     */
    public TranscriptionResumed() {
        resultInformation = null;
        transcriptionUpdateResult = null;
    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code/sub-code and message from NGC services.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * Get the transcriptionUpdateResult property: Defines the result for TranscriptionUpdate with the current status
     * and the details about the status.
     *
     * @return the transcriptionUpdateResult value.
     */
    public TranscriptionUpdate getTranscriptionUpdateResult() {
        return this.transcriptionUpdateResult;
    }

    /**
     * Reads an instance of TranscriptionResumed from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TranscriptionResumed if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the TranscriptionResumed.
     */
    public static TranscriptionResumed fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TranscriptionResumed event = new TranscriptionResumed();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("transcriptionUpdate".equals(fieldName)) {
                    event.transcriptionUpdateResult = TranscriptionUpdate.fromJson(reader);
                } else {
                    if (!event.handleField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
