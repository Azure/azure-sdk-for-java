// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.events.AddParticipantFailed;
import com.azure.communication.callautomation.models.events.AddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.AnswerFailed;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CallDisconnected;
import com.azure.communication.callautomation.models.events.CallTransferAccepted;
import com.azure.communication.callautomation.models.events.CallTransferFailed;
import com.azure.communication.callautomation.models.events.CancelAddParticipantFailed;
import com.azure.communication.callautomation.models.events.CancelAddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionStopped;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneFailed;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneReceived;
import com.azure.communication.callautomation.models.events.CreateCallFailed;
import com.azure.communication.callautomation.models.events.DialogCompleted;
import com.azure.communication.callautomation.models.events.DialogConsent;
import com.azure.communication.callautomation.models.events.DialogFailed;
import com.azure.communication.callautomation.models.events.DialogHangup;
import com.azure.communication.callautomation.models.events.DialogLanguageChange;
import com.azure.communication.callautomation.models.events.DialogSensitivityUpdate;
import com.azure.communication.callautomation.models.events.DialogStarted;
import com.azure.communication.callautomation.models.events.DialogTransfer;
import com.azure.communication.callautomation.models.events.HoldFailed;
import com.azure.communication.callautomation.models.events.ParticipantsUpdated;
import com.azure.communication.callautomation.models.events.PlayCanceled;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.PlayFailed;
import com.azure.communication.callautomation.models.events.RecognizeCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.callautomation.models.events.RecordingStateChanged;
import com.azure.communication.callautomation.models.events.RemoveParticipantFailed;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceeded;
import com.azure.communication.callautomation.models.events.TeamsComplianceRecordingStateChanged;
import com.azure.communication.callautomation.models.events.TeamsRecordingStateChanged;
import com.azure.communication.callautomation.models.events.TranscriptionFailed;
import com.azure.communication.callautomation.models.events.TranscriptionResumed;
import com.azure.communication.callautomation.models.events.TranscriptionStarted;
import com.azure.communication.callautomation.models.events.TranscriptionStopped;
import com.azure.communication.callautomation.models.events.SendDtmfTonesCompleted;
import com.azure.communication.callautomation.models.events.SendDtmfTonesFailed;
import com.azure.communication.callautomation.models.events.TranscriptionUpdated;
import com.azure.communication.callautomation.models.events.MediaStreamingStarted;
import com.azure.communication.callautomation.models.events.MediaStreamingStopped;
import com.azure.communication.callautomation.models.events.MediaStreamingFailed;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Event handler for taking care of event related tasks.
 */
public final class CallAutomationEventParser {
    private static final ClientLogger LOGGER = new ClientLogger(CallAutomationEventParser.class);

    /***
     * Returns a list of events from request's body.
     *
     * @param requestBody Body of the event request.
     * @throws RuntimeException Any exceptions occurs at runtime.
     * @return a list of CallAutomationEventBase
     */
    public static List<CallAutomationEventBase> parseEvents(String requestBody) {
        List<CallAutomationEventBase> callAutomationBaseEvents;
        callAutomationBaseEvents = parseCloudEventList(requestBody);

        return callAutomationBaseEvents;
    }

    private static List<CallAutomationEventBase> parseCloudEventList(String requestBody) {
        try {
            List<CloudEvent> cloudEvents;
            List<CallAutomationEventBase> callAutomationBaseEvents = new ArrayList<>();

            try {
                cloudEvents = CloudEvent.fromString(requestBody);
            } catch (RuntimeException e) {
                return callAutomationBaseEvents;
            }

            for (CloudEvent cloudEvent : cloudEvents) {
                CallAutomationEventBase temp = parseSingleCloudEvent(cloudEvent.getData().toString(), cloudEvent.getType());
                if (temp != null) {
                    callAutomationBaseEvents.add(temp);
                }
            }
            return callAutomationBaseEvents;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private static CallAutomationEventBase parseSingleCloudEvent(String data, String eventType) {
        try {
            CallAutomationEventBase ret = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            JsonNode eventData = mapper.readTree(data);

            if (Objects.equals(eventType, "Microsoft.Communication.CallConnected")) {
                ret = mapper.convertValue(eventData, CallConnected.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallDisconnected")) {
                ret = mapper.convertValue(eventData, CallDisconnected.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantFailed")) {
                ret = mapper.convertValue(eventData, AddParticipantFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantSucceeded")) {
                ret = mapper.convertValue(eventData, AddParticipantSucceeded.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferAccepted")) {
                ret = mapper.convertValue(eventData, CallTransferAccepted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferFailed")) {
                ret = mapper.convertValue(eventData, CallTransferFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ParticipantsUpdated")) {
                ret = mapper.convertValue(eventData, ParticipantsUpdated.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecordingStateChanged")) {
                ret = mapper.convertValue(eventData, RecordingStateChanged.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCompleted")) {
                ret = mapper.convertValue(eventData, PlayCompleted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayFailed")) {
                ret = mapper.convertValue(eventData, PlayFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCanceled")) {
                ret = mapper.convertValue(eventData, PlayCanceled.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCompleted")) {
                ret = mapper.convertValue(eventData, RecognizeCompleted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeFailed")) {
                ret = mapper.convertValue(eventData, RecognizeFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCanceled")) {
                ret = mapper.convertValue(eventData, RecognizeCanceled.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantFailed")) {
                ret = mapper.convertValue(eventData, RemoveParticipantFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantSucceeded")) {
                ret = mapper.convertValue(eventData, RemoveParticipantSucceeded.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionToneReceived")) {
                ret = mapper.convertValue(eventData, ContinuousDtmfRecognitionToneReceived.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionToneFailed")) {
                ret = mapper.convertValue(eventData, ContinuousDtmfRecognitionToneFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionStopped")) {
                ret = mapper.convertValue(eventData, ContinuousDtmfRecognitionStopped.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.SendDtmfTonesCompleted")) {
                ret = mapper.convertValue(eventData, SendDtmfTonesCompleted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.SendDtmfTonesFailed")) {
                ret = mapper.convertValue(eventData, SendDtmfTonesFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CancelAddParticipantSucceeded")) {
                ret = mapper.convertValue(eventData, CancelAddParticipantSucceeded.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CancelAddParticipantFailed")) {
                ret = mapper.convertValue(eventData, CancelAddParticipantFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogStarted")) {
                ret = mapper.convertValue(eventData, DialogStarted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogCompleted")) {
                ret = mapper.convertValue(eventData, DialogCompleted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogFailed")) {
                ret = mapper.convertValue(eventData, DialogFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogConsent")) {
                ret = mapper.convertValue(eventData, DialogConsent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogHangup")) {
                ret = mapper.convertValue(eventData, DialogHangup.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogLanguageChange")) {
                ret = mapper.convertValue(eventData, DialogLanguageChange.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogTransfer")) {
                ret = mapper.convertValue(eventData, DialogTransfer.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.DialogSensitivityUpdate")) {
                ret = mapper.convertValue(eventData, DialogSensitivityUpdate.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TeamsComplianceRecordingStateChanged")) {
                ret = mapper.convertValue(eventData, TeamsComplianceRecordingStateChanged.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TeamsRecordingStateChanged")) {
                ret = mapper.convertValue(eventData, TeamsRecordingStateChanged.class);
            }   else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionStarted")) {
                ret = mapper.convertValue(eventData, TranscriptionStarted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionFailed")) {
                ret = mapper.convertValue(eventData, TranscriptionFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionResumed")) {
                ret = mapper.convertValue(eventData, TranscriptionResumed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionStopped")) {
                ret = mapper.convertValue(eventData, TranscriptionStopped.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionUpdated")) {
                ret = mapper.convertValue(eventData, TranscriptionUpdated.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AnswerFailed")) {
                ret = mapper.convertValue(eventData, AnswerFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CreateCallFailed")) {
                ret = mapper.convertValue(eventData, CreateCallFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.HoldFailed")) {
                ret = mapper.convertValue(eventData, HoldFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingStarted")) {
                ret = mapper.convertValue(eventData, MediaStreamingStarted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingStopped")) {
                ret = mapper.convertValue(eventData, MediaStreamingStopped.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingFailed")) {
                ret = mapper.convertValue(eventData, MediaStreamingFailed.class);
            }
            return ret;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
