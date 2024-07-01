// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.events.AddParticipantsFailedEvent;
import com.azure.communication.callingserver.models.events.AddParticipantsSucceededEvent;
import com.azure.communication.callingserver.models.events.CallAutomationEventBase;
import com.azure.communication.callingserver.models.events.CallConnectedEvent;
import com.azure.communication.callingserver.models.events.CallDisconnectedEvent;
import com.azure.communication.callingserver.models.events.CallTransferAcceptedEvent;
import com.azure.communication.callingserver.models.events.CallTransferFailedEvent;
import com.azure.communication.callingserver.models.events.ParticipantsUpdatedEvent;
import com.azure.communication.callingserver.models.events.PlayCompleted;
import com.azure.communication.callingserver.models.events.PlayFailed;
import com.azure.communication.callingserver.models.events.RecordingStateChangedEvent;
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
        try (JsonReader jsonReader = JsonProviders.createReader(data)) {
            CallAutomationEventBase ret;
            if (Objects.equals(eventType, "Microsoft.Communication.CallConnected")) {
                ret = CallConnectedEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallDisconnected")) {
                ret = CallDisconnectedEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantsFailed")) {
                ret = AddParticipantsFailedEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.AddParticipantsSucceeded")) {
                ret = AddParticipantsSucceededEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferAccepted")) {
                ret = CallTransferAcceptedEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallTransferFailed")) {
                ret = CallTransferFailedEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.ParticipantsUpdated")) {
                ret = ParticipantsUpdatedEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.CallRecordingStateChanged")) {
                ret = RecordingStateChangedEvent.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayCompleted")) {
                ret = PlayCompleted.fromJson(jsonReader);
            } else if (Objects.equals(eventType, "Microsoft.Communication.PlayFailed")) {
                ret = PlayFailed.fromJson(jsonReader);
            } else {
                ret = null;
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
