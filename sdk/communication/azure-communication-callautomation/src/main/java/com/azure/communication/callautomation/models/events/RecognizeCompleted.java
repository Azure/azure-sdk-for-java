// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;
import java.util.Optional;

import com.azure.communication.callautomation.models.CallMediaRecognitionType;
import com.azure.communication.callautomation.models.RecognizeResult;
import com.azure.communication.callautomation.models.ChoiceResult;
import com.azure.communication.callautomation.models.DtmfResult;
import com.azure.communication.callautomation.models.SpeechResult;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.azure.core.annotation.Immutable;

/** The RecognizeCompleted model. */
@Immutable
public final class RecognizeCompleted extends CallAutomationEventBaseWithReasonCode {

    /*
     * Determines the subtype of the recognize operation.
     * In case of cancel operation this field is not set and is returned
     * empty
     */
    @JsonProperty(value = "recognitionType", access = JsonProperty.Access.WRITE_ONLY)
    private CallMediaRecognitionType recognitionType;

    /*
     * Defines the result for CallMediaRecognitionType = Dtmf
     */
    @JsonProperty(value = "dtmfResult", access = JsonProperty.Access.WRITE_ONLY)
    private DtmfResult dtmfResult;

    /*
     * Defines the result for CallMediaRecognitionType = Speech or SpeechOrDtmf
     */
    @JsonProperty(value = "speechResult", access = JsonProperty.Access.WRITE_ONLY)
    private SpeechResult speechResult;

    /*
     * Defines the result for RecognizeChoice
     */
    @JsonProperty(value = "choiceResult", access = JsonProperty.Access.WRITE_ONLY)
    private ChoiceResult collectChoiceResult;

    /**
     * Get the collectToneResult or choiceResult property.
     *
     * @return the recognizeResult value.
     */
    public Optional<RecognizeResult> getRecognizeResult() {
        if (this.recognitionType == CallMediaRecognitionType.DTMF) {
            return Optional.ofNullable(this.dtmfResult);

        } else if (this.recognitionType == CallMediaRecognitionType.CHOICES) {
            return Optional.ofNullable(this.collectChoiceResult);
        } else if (this.recognitionType == CallMediaRecognitionType.SPEECH) {
            return Optional.ofNullable(this.speechResult);
        }

        return Optional.empty();
    }

    /**
     * Reads an instance of RecognizeCompleted from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RecognizeCompleted if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RecognizeCompleted.
     */
    public static RecognizeCompleted fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final RecognizeCompleted event = new RecognizeCompleted();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("recognitionType".equals(fieldName)) {
                    event.recognitionType = CallMediaRecognitionType.fromString(reader.getString());
                } else if ("dtmfResult".equals(fieldName)) {
                    event.dtmfResult = DtmfResult.fromJson(reader);
                } else if ("speechResult".equals(fieldName)) {
                    event.speechResult = SpeechResult.fromJson(reader);
                } else if ("choiceResult".equals(fieldName)) {
                    event.collectChoiceResult = ChoiceResult.fromJson(reader);
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
