// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.events.AcsEventType;
import com.azure.communication.callingserver.models.events.AddParticipantsFailedEvent;
import com.azure.communication.callingserver.models.events.CallConnectedEvent;
import com.azure.communication.callingserver.models.events.CallDisconnectedEvent;
import com.azure.communication.callingserver.models.events.CallingServerBaseEvent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Event handler for taking care of event related tasks.
 */
public final class EventHandler {

    private static EventHandler eventHandler;

    private final ClientLogger logger;

    private EventHandler() {
        this.logger = new ClientLogger(EventHandler.class);
    }

    static EventHandler getEventHandler() {
        if (eventHandler == null) {
            eventHandler = new EventHandler();
        }

        return eventHandler;
    }

    /***
     * Returns an event from raw data.
     *
     * @param data Event raw data.
     * @return the event.
     */
    public CallingServerBaseEvent parseEvent(String data) {
        try {
            CallingServerBaseEvent ret = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            JsonNode root = mapper.readTree(data);
            JsonNode eventData = root.get("data");

            AcsEventType type = AcsEventType.fromString(mapper
                .convertValue(eventData.get("type"), String.class));

            if (type.equals(AcsEventType.CALL_CONNECTED)) {
                ret = mapper.convertValue(eventData, CallConnectedEvent.class);
            } else if (type.equals(AcsEventType.CALL_DISCONNECTED)) {
                ret = mapper.convertValue(eventData, CallDisconnectedEvent.class);
            } else if (type.equals(AcsEventType.ADD_PARTICIPANT_FAILED)) {
                ret = mapper.convertValue(eventData, AddParticipantsFailedEvent.class);
            } else if (type.equals(AcsEventType.ADD_PARTICIPANT_SUCCEEDED)) {
                ret = mapper.convertValue(eventData, CallDisconnectedEvent.class);
            } else if (type.equals(AcsEventType.CALL_TRANSFER_ACCEPTED)) {
                ret = mapper.convertValue(eventData, AddParticipantsFailedEvent.class);
            } else if (type.equals(AcsEventType.CALL_TRANSFER_FAILED)) {
                ret = mapper.convertValue(eventData, CallDisconnectedEvent.class);
            } else if (type.equals(AcsEventType.PARTICIPANTS_UPDATED)) {
                ret = mapper.convertValue(eventData, AddParticipantsFailedEvent.class);
            }

            return ret;
        } catch (Exception e) {
            throw  logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
