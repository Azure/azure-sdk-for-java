// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.events.AddParticipantsFailedEvent;
import com.azure.communication.callautomation.models.events.AddParticipantsSucceededEvent;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnectedEvent;
import com.azure.communication.callautomation.models.events.CallDisconnectedEvent;
import com.azure.communication.callautomation.models.events.CallTransferAcceptedEvent;
import com.azure.communication.callautomation.models.events.CallTransferFailedEvent;
import com.azure.communication.callautomation.models.events.ParticipantsUpdatedEvent;
import com.azure.communication.callautomation.models.events.PlayCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCanceled;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.callautomation.models.events.PlayCompletedEvent;
import com.azure.communication.callautomation.models.events.PlayFailedEvent;
import com.azure.communication.callautomation.models.events.RecordingStateChangedEvent;
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
public final class EventHandler {
    private static final ClientLogger LOGGER = new ClientLogger(EventHandler.class);

    /***
     * Returns a list of events from request's body.
     *
     * @param requestBody Body of the event request.
     * @throws RuntimeException Any exceptions occurs at runtime.
     * @return a list of CallAutomationEventBase
     */
    public static List<CallAutomationEventBase> parseEventList(String requestBody) {
        List<CallAutomationEventBase> callAutomationBaseEvents;
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
    public static CallAutomationEventBase parseEvent(String requestBody) {
        List<CallAutomationEventBase> callAutomationBaseEvents = parseEventList(requestBody);
        return callAutomationBaseEvents.isEmpty() ? null : callAutomationBaseEvents.get(0);
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
                ret = mapper.convertValue(eventData, CallConnectedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallDisconnected")) {
                ret = mapper.convertValue(eventData, CallDisconnectedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantsFailed")) {
                ret = mapper.convertValue(eventData, AddParticipantsFailedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantsSucceeded")) {
                ret = mapper.convertValue(eventData, AddParticipantsSucceededEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferAccepted")) {
                ret = mapper.convertValue(eventData, CallTransferAcceptedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferFailed")) {
                ret = mapper.convertValue(eventData, CallTransferFailedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ParticipantsUpdated")) {
                ret = mapper.convertValue(eventData, ParticipantsUpdatedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallRecordingStateChanged")) {
                ret = mapper.convertValue(eventData, RecordingStateChangedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCompleted")) {
                ret = mapper.convertValue(eventData, PlayCompletedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayFailed")) {
                ret = mapper.convertValue(eventData, PlayFailedEvent.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCanceled")) {
                ret = mapper.convertValue(eventData, PlayCanceled.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCompleted")) {
                ret = mapper.convertValue(eventData, RecognizeCompleted.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeFailed")) {
                ret = mapper.convertValue(eventData, RecognizeFailed.class);
            } else if (Objects.equals(eventType, "Microsoft.Communication.RecognizeCanceled")) {
                ret = mapper.convertValue(eventData, RecognizeCanceled.class);
            }

            return ret;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
