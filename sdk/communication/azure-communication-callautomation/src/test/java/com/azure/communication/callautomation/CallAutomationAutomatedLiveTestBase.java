// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallAutomationAutomatedLiveTestBase extends CallAutomationLiveTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(CallAutomationAutomatedLiveTestBase.class);

    protected ConcurrentHashMap<String, ServiceBusProcessorClient> processorStore;
    // Key: callerId + receiverId, Value: incomingCallContext
    protected ConcurrentHashMap<String, String> incomingCallContextStore;
    // Key: callConnectionId, Value: <Key: event Class, Value: instance of the event>
    protected ConcurrentHashMap<String, ConcurrentHashMap<Type, CallAutomationEventBase>> eventStore;
    protected List<String> eventsToPersist;
    protected static final String SERVICEBUS_CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("SERVICEBUS_STRING",
            "Endpoint=sb://REDACTED.servicebus.windows.net/;SharedAccessKeyName=REDACTED;SharedAccessKey=REDACTEDu8EtZn87JJY=");
    protected static final String DISPATCHER_ENDPOINT = Configuration.getGlobalConfiguration()
        .get("DISPATCHER_ENDPOINT",
            "https://incomingcalldispatcher.azurewebsites.net");
    protected static final String DISPATCHER_CALLBACK = DISPATCHER_ENDPOINT + "/api/servicebuscallback/events";
    protected static final String TRANSPORT_URL = Configuration.getGlobalConfiguration()
        .get("TRANSPORT_URL",
            "https://REDACTED");
    protected static final String COGNITIVE_SERVICE_ENDPOINT = Configuration.getGlobalConfiguration()
        .get("COGNITIVE_SERVICE_ENDPOINT",
            "https://REDACTED");
    protected static final String BOT_APP_ID = Configuration.getGlobalConfiguration()
        .get("BOT_APP_ID", "REDACTED-bedb-REDACTED-b8c6-REDACTED");

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("value")
        .add("rawId")
        .add("id")
        .add("callbackUri")
        .add("botAppId")
        .add("ivrContext")
        .add("incomingCallContext")
        .add("serverCallId")
        .add("transportUrl");

    protected static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT),
        Pattern.CASE_INSENSITIVE);

    protected static final String URL_REGEX = "(?<=http:\\/\\/|https:\\/\\/)([^\\/?]+)";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        processorStore = new ConcurrentHashMap<>();
        incomingCallContextStore = new ConcurrentHashMap<>();
        eventStore = new ConcurrentHashMap<>();
        eventsToPersist = new ArrayList<>();

        // Load persisted events back to memory when in playback mode
        if (getTestMode() == TestMode.PLAYBACK) {
            try {
                String fileName = "./src/test/resources/" + testContextManager.getTestName() + ".json";
                FileInputStream fileInputStream = new FileInputStream(fileName);
                byte[] jsonData = new byte[fileInputStream.available()];
                fileInputStream.read(jsonData);
                fileInputStream.close();
                String jsonString = new String(jsonData, StandardCharsets.UTF_8);
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> persistedEvents = objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
                persistedEvents.forEach(this::messageBodyHandler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        if (processorStore != null) {
            processorStore.forEach((key, value) -> value.close());
        }

        // In recording mode, manually store events from event dispatcher into local disk as the callAutomationClient doesn't do so
        if (getTestMode() == TestMode.RECORD) {
            try {
                String fileName = "./src/test/resources/" + testContextManager.getTestName() + ".json";

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonString = objectMapper.writeValueAsString(eventsToPersist);
                new FileOutputStream(fileName).close();
                FileOutputStream fileOutputStream = new FileOutputStream(fileName, false);
                fileOutputStream.write(jsonString.getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
        if (getTestMode() != TestMode.PLAYBACK) {
            // subscribe
            HttpClient httpClient = HttpClient.createDefault();
            String dispatcherUrl = DISPATCHER_ENDPOINT + String.format("/api/servicebuscallback/subscribe?q=%s", uniqueId);
            HttpRequest request = new HttpRequest(HttpMethod.POST, dispatcherUrl);
            HttpResponse response = httpClient.send(request).block();
            assert response != null;

            // create a service bus processor
            ServiceBusProcessorClient serviceBusProcessorClient = createServiceBusClientBuilderWithConnectionString()
                .processor()
                .queueName(uniqueId)
                .processMessage(this::messageHandler)
                .processError(serviceBusErrorContext -> errorHandler(serviceBusErrorContext, new CountDownLatch(1)))
                .buildProcessorClient();

            serviceBusProcessorClient.start();
            processorStore.put(uniqueId, serviceBusProcessorClient);
        }
        return uniqueId;
    }

    protected void messageHandler(ServiceBusReceivedMessageContext context) {
        // receive message from dispatcher
        ServiceBusReceivedMessage message = context.getMessage();
        String body = message.getBody().toString();

        // When in recording mode, save incoming events into memory for future use
        if (getTestMode() == TestMode.RECORD) {
            String redactedBody = redact(body, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(body));
            eventsToPersist.add(redactedBody);
        }

        messageBodyHandler(body);
    }

    private void messageBodyHandler(String body) {
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
            CallAutomationEventBase event = CallAutomationEventParser.parseEvents(body).get(0);
            assert event != null : "Event cannot be null";
            String callConnectionId = event.getCallConnectionId();
            if (!eventStore.containsKey(callConnectionId)) {
                eventStore.put(callConnectionId, new ConcurrentHashMap<>());
            }
            eventStore.get(callConnectionId).put(event.getClass(), event);
        }
    }

    protected void errorHandler(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
        LOGGER.log(LogLevel.VERBOSE, () -> String.format(
            "Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
            context.getFullyQualifiedNamespace(), context.getEntityPath()));

        if (!(context.getException() instanceof ServiceBusException)) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Non-ServiceBusException occurred: " + context.getException());
            return;
        }

        ServiceBusException exception = (ServiceBusException) context.getException();
        ServiceBusFailureReason reason = exception.getReason();

        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
            || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
            || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            LOGGER.log(LogLevel.VERBOSE, () -> String.format(
                "An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
                reason, exception.getMessage()));

            countdownLatch.countDown();
        } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
            LOGGER.log(LogLevel.VERBOSE,
                () -> String.format("Message lock lost for message: %s%n", context.getException()));
        } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
            // Choosing an arbitrary amount of time to wait until trying again.
            sleepIfRunningAgainstService(1000);
        } else {
            LOGGER.log(LogLevel.VERBOSE, () -> String.format("Error source %s, reason %s, message: %s%n",
                context.getErrorSource(), reason, context.getException()));
        }
    }

    protected String parseIdsFromIdentifier(CommunicationIdentifier communicationIdentifier) {
        assert communicationIdentifier != null;
        CommunicationIdentifierModel communicationIdentifierModel = CommunicationIdentifierConverter.convert(communicationIdentifier);
        assert communicationIdentifierModel.getRawId() != null;
        return getTestMode() == TestMode.PLAYBACK ? "REDACTED" : removeAllNonChar(communicationIdentifierModel.getRawId());
    }

    /* Change the plus + sign to it's unicode without the special characters i.e. u002B.
     * It's required because the dispatcher app receives the incoming call context for PSTN calls
     * with the + as unicode in it and builds the topic id with it to send the event.*/
    protected static String removeAllNonChar(String input) {
        return input.replace("+", "u002B").replaceAll("[^a-zA-Z0-9_-]", "");
    }

    protected String waitForIncomingCallContext(String uniqueId, Duration timeOut) throws InterruptedException {
        LocalDateTime timeOutTime = LocalDateTime.now().plusSeconds(timeOut.getSeconds());
        while (LocalDateTime.now().isBefore(timeOutTime)) {
            String incomingCallContext = incomingCallContextStore.get(uniqueId);
            if (incomingCallContext != null) {
                return incomingCallContext;
            }
            sleepIfRunningAgainstService(1000);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <T extends CallAutomationEventBase> T waitForEvent(Class<T> eventType, String callConnectionId, Duration timeOut) throws InterruptedException {
        LocalDateTime timeOutTime = LocalDateTime.now().plusSeconds(timeOut.getSeconds());
        while (LocalDateTime.now().isBefore(timeOutTime)) {
            if (eventStore.get(callConnectionId) != null) {
                T event = (T) eventStore.get(callConnectionId).get(eventType);
                if (event != null) {
                    return event;
                }
            }
            sleepIfRunningAgainstService(1000);
        }
        return null;
    }

    protected String redact(String content, Matcher matcher) {
        while (matcher.find()) {
            String captureGroup = matcher.group(1);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(1), "REDACTED");
            }
        }
        return content;
    }
}
