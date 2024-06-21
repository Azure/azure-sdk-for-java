// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Objects;

/** The base event interface. */
public abstract class CallAutomationEventBase {
    /*
     * Call connection ID.
     */
    @JsonProperty(value = "callConnectionId")
    private String callConnectionId;

    /*
     * Server call ID.
     */
    @JsonProperty(value = "serverCallId")
    private String serverCallId;

    /*
     * Correlation ID for event to call correlation.
     */
    @JsonProperty(value = "correlationId")
    private String correlationId;

    /*
     * Operation context
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    CallAutomationEventBase() {
        this.serverCallId = null;
        this.callConnectionId = null;
        this.correlationId = null;
        this.operationContext = null;
    }

    /**
     * Reads an instance of CallAutomationEventBase from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @param eventType the event type.
     * @return An instance of CallAutomationEventBase if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallAutomationEventBase.
     */
    public static CallAutomationEventBase fromJson(JsonReader jsonReader, String eventType) throws IOException {
        return jsonReader.readObject(reader -> {
            final JsonReader reader1 = reader.bufferObject();
            reader1.nextToken(); // Prepare for reading
            final CallAutomationEventBase event;
            if (Objects.equals(eventType, "Microsoft.Communication.CallConnected")) {
                event = CallConnected.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallDisconnected")) {
                event = CallDisconnected.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantFailed")) {
                event = AddParticipantFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantSucceeded")) {
                event = AddParticipantSucceeded.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferAccepted")) {
                event = CallTransferAccepted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferFailed")) {
                event = CallTransferFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ParticipantsUpdated")) {
                event = ParticipantsUpdated.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecordingStateChanged")) {
                event = RecordingStateChanged.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCompleted")) {
                event = PlayCompleted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayFailed")) {
                event = PlayFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCanceled")) {
                event = PlayCanceled.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCompleted")) {
                event = RecognizeCompleted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeFailed")) {
                event = RecognizeFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCanceled")) {
                event = RecognizeCanceled.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantFailed")) {
                event = RemoveParticipantFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantSucceeded")) {
                event = RemoveParticipantSucceeded.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionToneReceived")) {
                event = ContinuousDtmfRecognitionToneReceived.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionToneFailed")) {
                event = ContinuousDtmfRecognitionToneFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionStopped")) {
                event = ContinuousDtmfRecognitionStopped.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.SendDtmfTonesCompleted")) {
                event = SendDtmfTonesCompleted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.SendDtmfTonesFailed")) {
                event = SendDtmfTonesFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CancelAddParticipantSucceeded")) {
                event = CancelAddParticipantSucceeded.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CancelAddParticipantFailed")) {
                event = CancelAddParticipantFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogStarted")) {
                event = DialogStarted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogCompleted")) {
                event = DialogCompleted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogFailed")) {
                event = DialogFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogConsent")) {
                event = DialogConsent.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogHangup")) {
                event = DialogHangup.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogLanguageChange")) {
                event = DialogLanguageChange.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogTransfer")) {
                event = DialogTransfer.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogSensitivityUpdate")) {
                event = DialogSensitivityUpdate.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TeamsComplianceRecordingStateChanged")) {
                event = TeamsComplianceRecordingStateChanged.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TeamsRecordingStateChanged")) {
                event = TeamsRecordingStateChanged.fromJsonImpl(reader1);
            }   else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionStarted")) {
                event = TranscriptionStarted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionFailed")) {
                event = TranscriptionFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionResumed")) {
                event = TranscriptionResumed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionStopped")) {
                event = TranscriptionStopped.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionUpdated")) {
                event = TranscriptionUpdated.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AnswerFailed")) {
                event = AnswerFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CreateCallFailed")) {
                event = CreateCallFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.HoldFailed")) {
                event = HoldFailed.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingStarted")) {
                event = MediaStreamingStarted.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingStopped")) {
                event = MediaStreamingStopped.fromJsonImpl(reader1);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingFailed")) {
                event = MediaStreamingFailed.fromJsonImpl(reader1);
            } else {
                return null;
            }
            final JsonReader reader2 = reader1.reset();
            reader2.nextToken();
            while (reader2.nextToken() != JsonToken.END_DOCUMENT) {
                String fieldName = reader2.getFieldName();
                reader2.nextToken();
                if ("callConnectionId".equals(fieldName)) {
                    event.callConnectionId = reader2.getString();
                } else if ("serverCallId".equals(fieldName)) {
                    event.serverCallId = reader2.getString();
                } else if ("correlationId".equals(fieldName)) {
                    event.correlationId = reader2.getString();
                } else if ("operationContext".equals(fieldName)) {
                    event.operationContext = reader2.getString();
                } else {
                    reader2.skipChildren();
                }
            }
            return event;
        });
    }

    /**
     * Get the callConnectionId property: Call connection ID.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
    }

    /**
     * Get the serverCallId property: Server call ID.
     *
     * @return the serverCallId value.
     */
    public String getServerCallId() {
        return this.serverCallId;
    }

    /**
     * Get the correlationId property: Correlation ID for event to call correlation.
     *
     * @return the correlationId value.
     */
    public String getCorrelationId() {
        return this.correlationId;
    }

    /**
     * Get the operationContext property: Operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }
}
