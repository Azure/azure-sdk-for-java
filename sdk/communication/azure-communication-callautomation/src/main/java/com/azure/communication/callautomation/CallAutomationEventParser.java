// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.events.AddParticipantFailed;
import com.azure.communication.callautomation.models.events.AddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnected;
import com.azure.communication.callautomation.models.events.CallDisconnected;
import com.azure.communication.callautomation.models.events.CallTransferAccepted;
import com.azure.communication.callautomation.models.events.CallTransferFailed;
import com.azure.communication.callautomation.models.events.CancelAddParticipantFailed;
import com.azure.communication.callautomation.models.events.CancelAddParticipantSucceeded;
import com.azure.communication.callautomation.models.events.ConnectFailed;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionStopped;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneFailed;
import com.azure.communication.callautomation.models.events.ContinuousDtmfRecognitionToneReceived;
import com.azure.communication.callautomation.models.events.HoldFailed;
import com.azure.communication.callautomation.models.events.ParticipantsUpdated;
import com.azure.communication.callautomation.models.events.PlayCanceled;
import com.azure.communication.callautomation.models.events.PlayCompleted;
import com.azure.communication.callautomation.models.events.PlayFailed;
import com.azure.communication.callautomation.models.events.PlayStarted;
import com.azure.communication.callautomation.models.events.RecognizeCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.callautomation.models.events.RecordingStateChanged;
import com.azure.communication.callautomation.models.events.RemoveParticipantFailed;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceeded;
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
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
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
        try (JsonReader jsonReader = JsonProviders.createReader(data)) {
            CallAutomationEventBase ret;
            if (Objects.equals(eventType, "Microsoft.Communication.CallConnected")) {
                ret = CallConnected.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallDisconnected")) {
                ret = CallDisconnected.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantFailed")) {
                ret = AddParticipantFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantSucceeded")) {
                ret = AddParticipantSucceeded.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferAccepted")) {
                ret = CallTransferAccepted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferFailed")) {
                ret = CallTransferFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ParticipantsUpdated")) {
                ret = ParticipantsUpdated.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecordingStateChanged")) {
                ret = RecordingStateChanged.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayStarted")) {
                ret = PlayStarted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCompleted")) {
                ret = PlayCompleted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayFailed")) {
                ret = PlayFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCanceled")) {
                ret = PlayCanceled.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCompleted")) {
                ret = RecognizeCompleted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeFailed")) {
                ret = RecognizeFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCanceled")) {
                ret = RecognizeCanceled.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantFailed")) {
                ret = RemoveParticipantFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantSucceeded")) {
                ret = RemoveParticipantSucceeded.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionToneReceived")) {
                ret = ContinuousDtmfRecognitionToneReceived.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionToneFailed")) {
                ret = ContinuousDtmfRecognitionToneFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ContinuousDtmfRecognitionStopped")) {
                ret = ContinuousDtmfRecognitionStopped.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.SendDtmfTonesCompleted")) {
                ret = SendDtmfTonesCompleted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.SendDtmfTonesFailed")) {
                ret = SendDtmfTonesFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CancelAddParticipantSucceeded")) {
                ret = CancelAddParticipantSucceeded.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CancelAddParticipantFailed")) {
                ret = CancelAddParticipantFailed.fromJson(jsonReader);
            }  else if (Objects.equals(eventType, "Microsoft.Communication.ConnectFailed")) {
                ret = ConnectFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionStarted")) {
                ret = TranscriptionStarted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionFailed")) {
                ret = TranscriptionFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionResumed")) {
                ret = TranscriptionResumed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionStopped")) {
                ret = TranscriptionStopped.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.TranscriptionUpdated")) {
                ret = TranscriptionUpdated.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.HoldFailed")) {
                ret = HoldFailed.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingStarted")) {
                ret = MediaStreamingStarted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingStopped")) {
                ret = MediaStreamingStopped.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.MediaStreamingFailed")) {
                ret = MediaStreamingFailed.fromJson(jsonReader);
            } else {
                ret = null;
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
