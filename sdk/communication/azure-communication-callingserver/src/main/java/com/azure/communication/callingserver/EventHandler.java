// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.events.AcsEventType;
import com.azure.communication.callingserver.models.events.AddParticipantsFailedEvent;
import com.azure.communication.callingserver.models.events.AddParticipantsSucceededEvent;
import com.azure.communication.callingserver.models.events.CallAutomationEventBase;
import com.azure.communication.callingserver.models.events.CallConnectedEvent;
import com.azure.communication.callingserver.models.events.CallDisconnectedEvent;
import com.azure.communication.callingserver.models.events.CallTransferAcceptedEvent;
import com.azure.communication.callingserver.models.events.CallTransferFailedEvent;
import com.azure.communication.callingserver.models.events.IncomingCallEvent;
import com.azure.communication.callingserver.models.events.ParticipantsUpdatedEvent;
import com.azure.communication.callingserver.models.events.SubscriptionValidationEvent;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;


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
        callAutomationBaseEvents = parseEventGridEventList(requestBody);
        if (callAutomationBaseEvents.isEmpty()) {
            callAutomationBaseEvents = parseCloudEventList(requestBody);
        }

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

    private static List<CallAutomationEventBase> parseEventGridEventList(String requestBody) {
        try {
            List<EventGridEvent> eventGridEvents;
            List<CallAutomationEventBase> callAutomationBaseEvents = new ArrayList<>();

            try {
                eventGridEvents = EventGridEvent.fromString(requestBody);
            } catch (RuntimeException e) {
                return callAutomationBaseEvents;
            }

            for (EventGridEvent eventGridEvent : eventGridEvents) {
                CallAutomationEventBase temp = parseSingleEventGridEvent(eventGridEvent.getData().toString(), eventGridEvent.getEventType());
                if (temp != null) {
                    callAutomationBaseEvents.add(temp);
                }
            }
            return callAutomationBaseEvents;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
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
                CallAutomationEventBase temp = parseSingleCloudEvent(cloudEvent.getData().toString());
                if (temp != null) {
                    callAutomationBaseEvents.add(temp);
                }
            }
            return callAutomationBaseEvents;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        }
    }

    private static CallAutomationEventBase parseSingleEventGridEvent(String data, String eventType) {
        try {
            CallAutomationEventBase ret = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            JsonNode eventData = mapper.readTree(data);
            AcsEventType type = AcsEventType.fromString(eventType);

            if (type.equals(AcsEventType.SUBSCRIPTION_VALIDATION_EVENT)) {
                ret = mapper.convertValue(eventData, SubscriptionValidationEvent.class);
            } else if (type.equals(AcsEventType.INCOMING_CALL_EVENT)) {
                ret = mapper.convertValue(eventData, IncomingCallEvent.class);
            }

            return ret;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private static CallAutomationEventBase parseSingleCloudEvent(String data) {
        try {
            CallAutomationEventBase ret = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            JsonNode eventData = mapper.readTree(data);
            AcsEventType type = AcsEventType.fromString(mapper
                .convertValue(eventData.get("type"), String.class));

            if (type.equals(AcsEventType.CALL_CONNECTED)) {
                ret = mapper.convertValue(eventData, CallConnectedEvent.class);
            } else if (type.equals(AcsEventType.CALL_DISCONNECTED)) {
                ret = mapper.convertValue(eventData, CallDisconnectedEvent.class);
            } else if (type.equals(AcsEventType.ADD_PARTICIPANTS_FAILED)) {
                ret = mapper.convertValue(eventData, AddParticipantsFailedEvent.class);
            } else if (type.equals(AcsEventType.ADD_PARTICIPANTS_SUCCEEDED)) {
                ret = mapper.convertValue(eventData, AddParticipantsSucceededEvent.class);
            } else if (type.equals(AcsEventType.CALL_TRANSFER_ACCEPTED)) {
                ret = mapper.convertValue(eventData, CallTransferAcceptedEvent.class);
            } else if (type.equals(AcsEventType.CALL_TRANSFER_FAILED)) {
                ret = mapper.convertValue(eventData, CallTransferFailedEvent.class);
            } else if (type.equals(AcsEventType.PARTICIPANTS_UPDATED)) {
                ret = mapper.convertValue(eventData, ParticipantsUpdatedEvent.class);
            }

            return ret;
        } catch (RuntimeException e) {
            throw LOGGER.logExceptionAsError(e);
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
