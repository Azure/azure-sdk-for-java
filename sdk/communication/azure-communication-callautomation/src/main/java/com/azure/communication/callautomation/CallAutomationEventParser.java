// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.events.AddParticipantFailedEventData;
import com.azure.communication.callautomation.models.events.AddParticipantSucceededEventData;
import com.azure.communication.callautomation.models.events.CallAutomationEventData;
import com.azure.communication.callautomation.models.events.CallConnectedEventData;
import com.azure.communication.callautomation.models.events.CallDisconnectedEventData;
import com.azure.communication.callautomation.models.events.CallTransferAcceptedEventData;
import com.azure.communication.callautomation.models.events.CallTransferFailedEventData;
import com.azure.communication.callautomation.models.events.ParticipantsUpdatedEventData;
import com.azure.communication.callautomation.models.events.PlayCanceledEventData;
import com.azure.communication.callautomation.models.events.PlayFailedEventData;
import com.azure.communication.callautomation.models.events.RecognizeCanceledEventData;
import com.azure.communication.callautomation.models.events.RecognizeCompletedEventData;
import com.azure.communication.callautomation.models.events.RecognizeFailedEventData;
import com.azure.communication.callautomation.models.events.PlayCompletedEventData;
import com.azure.communication.callautomation.models.events.RecordingStateChangedEventData;
import com.azure.communication.callautomation.models.events.RemoveParticipantFailedEventData;
import com.azure.communication.callautomation.models.events.RemoveParticipantSucceededEventData;
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
     * @return a list of CallAutomationEventData
     */
    public static List<CallAutomationEventData> parseEvents(String requestBody) {
        List<CallAutomationEventData> callAutomationBaseEvents;
        callAutomationBaseEvents = parseCloudEventList(requestBody);

        return callAutomationBaseEvents;
    }

    /***
     * Returns the first(or the only) event of request's body.
     *
     * @param requestBody Body of the event request.
     * @throws RuntimeException Any exceptions occurs at runtime.
     * @return the first(or the only) event if request is not empty, otherwise null is returned.
     */
    public static CallAutomationEventData parseEvent(String requestBody) {
        List<CallAutomationEventData> callAutomationBaseEvents = parseEvents(requestBody);
        return callAutomationBaseEvents.isEmpty() ? null : callAutomationBaseEvents.get(0);
    }

    private static List<CallAutomationEventData> parseCloudEventList(String requestBody) {
        try {
            List<CloudEvent> cloudEvents;
            List<CallAutomationEventData> callAutomationBaseEvents = new ArrayList<>();

            try {
                cloudEvents = CloudEvent.fromString(requestBody);
            } catch (RuntimeException e) {
                return callAutomationBaseEvents;
            }

            for (CloudEvent cloudEvent : cloudEvents) {
                CallAutomationEventData temp = parseSingleCloudEvent(cloudEvent.getData().toString(), cloudEvent.getType());
                if (temp != null) {
                    callAutomationBaseEvents.add(temp);
                }
            }
            return callAutomationBaseEvents;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private static CallAutomationEventData parseSingleCloudEvent(String data, String eventType) {
        try {
            CallAutomationEventData ret = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            JsonNode eventData = mapper.readTree(data);

            if (Objects.equals(eventType, "Microsoft.Communication.CallConnected")) {
                ret = mapper.convertValue(eventData, CallConnectedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallDisconnected")) {
                ret = mapper.convertValue(eventData, CallDisconnectedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantFailed")) {
                ret = mapper.convertValue(eventData, AddParticipantFailedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantSucceeded")) {
                ret = mapper.convertValue(eventData, AddParticipantSucceededEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferAccepted")) {
                ret = mapper.convertValue(eventData, CallTransferAcceptedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferFailed")) {
                ret = mapper.convertValue(eventData, CallTransferFailedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ParticipantsUpdated")) {
                ret = mapper.convertValue(eventData, ParticipantsUpdatedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecordingStateChanged")) {
                ret = mapper.convertValue(eventData, RecordingStateChangedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCompleted")) {
                ret = mapper.convertValue(eventData, PlayCompletedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayFailed")) {
                ret = mapper.convertValue(eventData, PlayFailedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCanceled")) {
                ret = mapper.convertValue(eventData, PlayCanceledEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCompleted")) {
                ret = mapper.convertValue(eventData, RecognizeCompletedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeFailed")) {
                ret = mapper.convertValue(eventData, RecognizeFailedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCanceled")) {
                ret = mapper.convertValue(eventData, RecognizeCanceledEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantFailed")) {
                ret = mapper.convertValue(eventData, RemoveParticipantFailedEventData.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RemoveParticipantSucceeded")) {
                ret = mapper.convertValue(eventData, RemoveParticipantSucceededEventData.class);
            }
            return ret;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
