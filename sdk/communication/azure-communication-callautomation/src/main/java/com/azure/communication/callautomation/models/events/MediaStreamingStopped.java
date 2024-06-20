// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The MediaStreamingStopped model. */
@Fluent
public final class MediaStreamingStopped extends CallAutomationEventBase {

    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    @JsonProperty(value = "resultInformation", access = JsonProperty.Access.WRITE_ONLY)
    private ResultInformation resultInformation;

    /*
     * Defines the result for audio streaming update with the current status
     * and the details about the status
     */
    @JsonProperty(value = "mediaStreamingUpdate", access = JsonProperty.Access.WRITE_ONLY)
    private MediaStreamingUpdate mediaStreamingUpdateResult;

     /**
     * Creates an instance of MediaStreamingStopped class.
     */
    public MediaStreamingStopped() {
        resultInformation = null;
        mediaStreamingUpdateResult = null;
    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code, sub-code and message.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * Get the getMediaStreamingUpdateResult property: Defines the result for audio streaming update with the current status and
     * the details about the status.
     *
     * @return the mediaStreamingUpdate value.
     */
    public MediaStreamingUpdate getMediaStreamingUpdateResult() {
        return this.mediaStreamingUpdateResult;
    }

    static MediaStreamingStopped fromJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final MediaStreamingStopped event = new MediaStreamingStopped();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("mediaStreamingUpdate".equals(fieldName)) {
                    event.mediaStreamingUpdateResult = MediaStreamingUpdate.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
