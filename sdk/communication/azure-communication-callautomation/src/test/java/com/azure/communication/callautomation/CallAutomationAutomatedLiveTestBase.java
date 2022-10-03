// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.models.events.AddParticipantsFailedEvent;
import com.azure.communication.callautomation.models.events.AddParticipantsSucceededEvent;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.callautomation.models.events.CallConnectedEvent;
import com.azure.communication.callautomation.models.events.CallDisconnectedEvent;
import com.azure.communication.callautomation.models.events.CallTransferAcceptedEvent;
import com.azure.communication.callautomation.models.events.CallTransferFailedEvent;
import com.azure.communication.callautomation.models.events.ParticipantsUpdatedEvent;
import com.azure.communication.callautomation.models.events.PlayCompletedEvent;
import com.azure.communication.callautomation.models.events.PlayFailedEvent;
import com.azure.communication.callautomation.models.events.RecognizeCompleted;
import com.azure.communication.callautomation.models.events.RecognizeFailed;
import com.azure.communication.callautomation.models.events.RecordingStateChangedEvent;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.core.http.HttpClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CallAutomationAutomatedLiveTestBase extends CallAutomationLiveTestBase {
    protected ConcurrentHashMap<String, ServiceBusProcessorClient> processorStore;
    // Key: callerId + receiverId, Value: incomingCallContext
    protected ConcurrentHashMap<String, String> incomingCallContextStore;
    // Key: callConnectionId, Value: <Key: event Class, Value: instance of the event>
    protected ConcurrentHashMap<String, ConcurrentHashMap<Type, CallAutomationEventBase>> eventStore;
    protected static final String SERVICEBUS_CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("SERVICEBUS_STRING",
            "Endpoint=sb://REDACTED.servicebus.windows.net/;SharedAccessKeyName=REDACTED;SharedAccessKey=REDACTEDu8EtZn87JJY=");
    protected static final String DISPATCHER_ENDPOINT = Configuration.getGlobalConfiguration()
        .get("DISPATCHER_ENDPOINT",
            "https://incomingcalldispatcher.azurewebsites.net");
    protected static final String DISPATCHER_CALLBACK = DISPATCHER_ENDPOINT + "/api/servicebuscallback/events";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        processorStore = new ConcurrentHashMap<>();
        incomingCallContextStore = new ConcurrentHashMap<>();
        eventStore = new ConcurrentHashMap<>();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        processorStore.forEach((key, value) -> value.close());
    }

    protected static ServiceBusClientBuilder createServiceBusClientBuilderWithConnectionString() {
        return new ServiceBusClientBuilder()
            .connectionString(SERVICEBUS_CONNECTION_STRING)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS);
    }

    protected String serviceBusWithNewCall(CommunicationIdentifier caller, CommunicationIdentifier receiver) {
        String callerId = parseIdsFromIdentifier(caller);
        String receiverId = parseIdsFromIdentifier(receiver);
        String uniqueId = callerId + receiverId;

        // subscribe
        HttpClient httpClient = HttpClient.createDefault();
        String dispatcherUrl = DISPATCHER_ENDPOINT + String.format("/api/servicebuscallback/subscribe?q=%s", uniqueId);
        HttpRequest request = new HttpRequest(HttpMethod.POST, dispatcherUrl);
        HttpResponse response = httpClient.send(request).block();
        assert response != null;
        System.out.println(String.format("Subscription to dispatcher of %s: ", uniqueId) + response.getStatusCode());

        // create a service bus processor
        ServiceBusProcessorClient serviceBusProcessorClient = createServiceBusClientBuilderWithConnectionString()
            .processor()
            .queueName(uniqueId)
            .processMessage(this::messageHandler)
            .processError(serviceBusErrorContext -> errorHandler(serviceBusErrorContext, new CountDownLatch(1)))
            .buildProcessorClient();

        serviceBusProcessorClient.start();
        processorStore.put(uniqueId, serviceBusProcessorClient);
        return uniqueId;
    }

    protected void messageHandler(ServiceBusReceivedMessageContext context) {
        // receive message from dispatcher
        ServiceBusReceivedMessage message = context.getMessage();
        String body = message.getBody().toString();

        // parse the message
        assert !body.isEmpty();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        JsonNode eventData;
        try {
            eventData = mapper.readTree(body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // check if this is an incomingCallEvent(Event grid event) or normal callAutomation cloud events
        if (eventData.get("incomingCallContext") != null) {
            String incomingCallContext = mapper.convertValue(eventData.get("incomingCallContext"), String.class);
            CommunicationIdentifierModel from = mapper.convertValue(eventData.get("from"), CommunicationIdentifierModel.class);
            CommunicationIdentifierModel to = mapper.convertValue(eventData.get("to"), CommunicationIdentifierModel.class);
            String uniqueId = removeAllNonChar(from.getRawId() + to.getRawId());

            incomingCallContextStore.put(uniqueId, incomingCallContext);
        } else {
            CallAutomationEventBase event = EventHandler.parseEvent(body);
            assert event != null : "Event cannot be null";
            String callConnectionId = event.getCallConnectionId();
            if (!eventStore.containsKey(callConnectionId)) {
                eventStore.put(callConnectionId, new ConcurrentHashMap<>());
            }
            eventStore.get(callConnectionId).put(event.getClass(), event);
        }
    }

    protected void errorHandler(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
            context.getFullyQualifiedNamespace(), context.getEntityPath());

        if (!(context.getException() instanceof ServiceBusException)) {
            System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
            || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
            || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            System.out.printf("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
                reason, exception.getMessage());

            countdownLatch.countDown();
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            System.out.printf("Message lock lost for message: %s%n", context.getException());
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            try {
                // Choosing an arbitrary amount of time to wait until trying again.
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                System.err.println("Unable to sleep for period of time");
            }
        } else {
            System.out.printf("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
                reason, context.getException());
        }
    }

    protected static String parseIdsFromIdentifier(CommunicationIdentifier communicationIdentifier) {
        assert communicationIdentifier != null;
        CommunicationIdentifierModel communicationIdentifierModel = CommunicationIdentifierConverter.convert(communicationIdentifier);
        assert communicationIdentifierModel.getRawId() != null;
        return removeAllNonChar(communicationIdentifierModel.getRawId());
    }

    protected static String removeAllNonChar(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "");
    }

    protected String waitForIncomingCallContext(String uniqueId, Duration timeOut) throws InterruptedException {
        if (getTestMode() != TestMode.PLAYBACK) {
            LocalDateTime timeOutTime = LocalDateTime.now().plusSeconds(timeOut.getSeconds());
            while (LocalDateTime.now().isBefore(timeOutTime)) {
                String incomingCallContext = incomingCallContextStore.get(uniqueId);
                if (incomingCallContext != null) {
                    incomingCallContextStore.remove(uniqueId);
                    return incomingCallContext;
                }
                Thread.sleep(1000);
            }
            return null;
        }

        return "REDACTED_a0pvc2ai329xk1j2z";
    }

    @SuppressWarnings("unchecked")
    protected <T extends CallAutomationEventBase> T waitForEvent(Class<T> eventType, String callConnectionId, Duration timeOut) throws InterruptedException {
        if (getTestMode() != TestMode.PLAYBACK) {
            LocalDateTime timeOutTime = LocalDateTime.now().plusSeconds(timeOut.getSeconds());
            while (LocalDateTime.now().isBefore(timeOutTime)) {
                if (eventStore.get(callConnectionId) != null) {
                    T event = (T) eventStore.get(callConnectionId).get(eventType);

                    if (event != null) {
                        return event;
                    }
                }
                Thread.sleep(1000);
            }
            return null;
        }

        return generateEventForPlayback(eventType);
    }

    @SuppressWarnings("unchecked")
    private <T extends CallAutomationEventBase> T generateEventForPlayback(Class<T> eventType) {
        String eventName = "";
        if (eventType.equals(CallConnectedEvent.class)) {
            eventName = "Microsoft.Communication.CallConnected";
        } else if (eventType.equals(CallDisconnectedEvent.class)) {
            eventName = "Microsoft.Communication.CallDisconnected";
        } else if (eventType.equals(AddParticipantsFailedEvent.class)) {
            eventName = "Microsoft.Communication.AddParticipantsFailed";
        } else if (eventType.equals(AddParticipantsSucceededEvent.class)) {
            eventName = "Microsoft.Communication.AddParticipantsSucceeded";
        } else if (eventType.equals(CallTransferAcceptedEvent.class)) {
            eventName = "Microsoft.Communication.CallTransferAccepted";
        } else if (eventType.equals(CallTransferFailedEvent.class)) {
            eventName = "Microsoft.Communication.CallTransferFailed";
        } else if (eventType.equals(ParticipantsUpdatedEvent.class)) {
            eventName = "Microsoft.Communication.ParticipantsUpdated";
        } else if (eventType.equals(RecordingStateChangedEvent.class)) {
            eventName = "Microsoft.Communication.CallRecordingStateChanged";
        } else if (eventType.equals(PlayCompletedEvent.class)) {
            eventName = "Microsoft.Communication.PlayCompleted";
        } else if (eventType.equals(PlayFailedEvent.class)) {
            eventName = "Microsoft.Communication.PlayFailed";
        } else if (eventType.equals(RecognizeCompleted.class)) {
            eventName = "Microsoft.Communication.RecognizeCompleted";
        } else if (eventType.equals(RecognizeFailed.class)) {
            eventName = "Microsoft.Communication.RecognizeFailed";
        } else {
            return null;
        }
        String eventPayload = String.format("[{\"id\":\"e1REDACTED-2e2c-44a0-8f65-77d5REDACTEDde\",\"source\":\"calling/callConnections/411REDACTED-5800-4683-acec-6b4REDACTEDfe1\",\"type\":\"%s\",\"data\":{\"participants\":[{\"rawId\":\"8:acs:1bdREDACTED-9507-4542-bb64-a7bREDACTED8d4_00000014-25a2-eeba-92fd-8b3REDACTEDf3d4\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:1bREDACTED-9507-4542-bb64-a7bREDACTEDd4_00000014-25a2-eeba-92fd-8b3aREDACTEDd4\"}},{\"rawId\":\"8:acs:1bREDACTED-9507-4542-bb64-a7b2REDACTED8d4_00000014-25a2-efc7-defd-8b3aREDACTEDa6\",\"kind\":\"communicationUser\",\"communicationUser\":{\"id\":\"8:acs:1bREDACTED-9507-4542-bb64-a7b22cREDACTED4_00000014-25a2-efc7-defd-8b3a0REDACTEDa6\"}}],\"callConnectionId\":\"411REDACTED-5800-4683-acec-6b4746REDACTED1\",\"serverCallId\":\"aHR0cHMREDACTEDZmxpZ2h0cHJveHkuc2t5cGUuY29tL2FwaS92Mi9jcC9jb252REDACTEDtMDIuY29udi5za3lwZS5jb20vY29udi9lbHhmX0VpaUlFU3ZLUUlDNlpyVGdnP2k9MTAmZT02Mzc5OTk0NDkwNTM2MTY5MzU=\",\"correlationId\":\"34REDACTED-1f79-4030-a90f-141REDACTED29\"},\"time\":\"2022-09-28T20:19:54.3681008\\u002B00:00\",\"specversion\":\"1.0\",\"datacontenttype\":\"application/json\",\"subject\":\"calling/callConnections/411REDACTED-5800-4683-acec-6b4746REDACTED\"}]",
            eventName);
        CallAutomationEventBase dummyEvent = EventHandler.parseEvent(eventPayload);
        return (T) dummyEvent;
    }
}
