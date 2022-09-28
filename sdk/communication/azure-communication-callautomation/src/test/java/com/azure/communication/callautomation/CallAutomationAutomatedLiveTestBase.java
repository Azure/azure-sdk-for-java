// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
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
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CallAutomationAutomatedLiveTestBase extends CallAutomationLiveTestBase {
    protected ConcurrentHashMap<String, ServiceBusProcessorClient> processorStore;
    // Key: callerId + receiverId, Value: incomingCallContext
    protected ConcurrentHashMap<String, String> incomingCallContextStore;
    // Key: serverCallId(correlationID), Value: <Key: event Class, Value: instance of the event>
    protected ConcurrentHashMap<String, ConcurrentHashMap<Type, CallAutomationEventBase>> eventStore;
    protected static final String SERVICEBUS_CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("SERVICEBUS_STRING",
            "Endpoint=sb://REDACTED.servicebus.windows.net/;SharedAccessKeyName=TestKey;SharedAccessKey=BZscTVbGv+kiKSwVYBGxYS4mWwmFQhOou8EtZn87JJY=");
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

    protected Mono<String> serviceBusWithNewCall(CommunicationIdentifier caller, CommunicationIdentifier receiver) {
        String callerId = parseIdsFromIdentifier(caller);
        String receiverId = parseIdsFromIdentifier(receiver);
        String uniqueId = callerId + receiverId;

        // subscribe
        HttpClient httpClient = HttpClient.createDefault();
        String dispatcherUrl = DISPATCHER_ENDPOINT + String.format("/api/servicebuscallback/subscribe?q=%s", uniqueId);
        HttpRequest request = new HttpRequest(HttpMethod.POST, dispatcherUrl);
        HttpResponse response = httpClient.send(request).block();
        assert response != null;
        System.out.println("Subscription to dispatcher: " + response.getStatusCode());

        // create a service bus processor
        ServiceBusProcessorClient serviceBusProcessorClient = createServiceBusClientBuilderWithConnectionString()
            .processor()
            .queueName(uniqueId)
            .processMessage(this::messageHandler)
            .processError(serviceBusErrorContext -> errorHandler(serviceBusErrorContext, new CountDownLatch(1)))
            .buildProcessorClient();

        serviceBusProcessorClient.start();
        processorStore.put(uniqueId, serviceBusProcessorClient);
        return Mono.just(uniqueId);
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
            String serverCallId = event.getServerCallId();
            if (!eventStore.containsKey(serverCallId)) {
                eventStore.put(serverCallId, new ConcurrentHashMap<>());
            }
            eventStore.get(serverCallId).put(event.getClass(), event);
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
}
