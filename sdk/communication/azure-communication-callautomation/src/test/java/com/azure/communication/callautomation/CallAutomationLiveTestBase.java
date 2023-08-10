// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.models.events.CallAutomationEventBase;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallAutomationLiveTestBase extends TestProxyTestBase {
    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
            "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT",
        "https://REDACTED.communication.azure.com/");
    protected static final String ENDPOINT_401 = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT_401",
        "https://REDACTED.communication.azure.com/");
    protected static final String PMA_ENDPOINT = Configuration.getGlobalConfiguration().get("PMA_Endpoint", "https://REDACTED.communication.azure.com/");
    protected static final Boolean COMMUNICATION_CUSTOM_ENDPOINT_ENABLED = Configuration.getGlobalConfiguration().get("COMMUNICATION_CUSTOM_ENDPOINT_ENABLED", false);
    protected static final String METADATA_URL = Configuration.getGlobalConfiguration()
        .get("METADATA_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/acsmetadata");
    protected static final String VIDEO_URL = Configuration.getGlobalConfiguration()
        .get("VIDEO_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/video");
    protected static final String CONTENT_URL_404 = Configuration.getGlobalConfiguration()
        .get("CONTENT_URL_404", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141d/content/acsmetadata");
    protected static final String RECORDING_DELETE_URL = Configuration.getGlobalConfiguration()
        .get("RECORDING_DELETE_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d10-598a3ea36bfbc27e68c026b17982af22");
    protected static final String RECORDING_DELETE_URL_404 = Configuration.getGlobalConfiguration()
        .get("RECORDING_DELETE_URL_404", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c");
    protected static final String RANDOM_RESOURCE_IDENTIFIER = "82e890fc-188a-4b67-bb7d-defRANDOM1e";
    protected static final String ACS_USER_1 = Configuration.getGlobalConfiguration()
        .get("TARGET_USER_ID", String.format("8:acs:%s_00000014-00d6-e250-28df-44482200202a", RANDOM_RESOURCE_IDENTIFIER));
    protected static final String ACS_USER_2 = Configuration.getGlobalConfiguration()
        .get("ANOTHER_TARGET_USER_ID", String.format("8:acs:%s_00000014-00d7-31b3-28df-444822002030", RANDOM_RESOURCE_IDENTIFIER));
    protected static final String ACS_USER_CALL_RECORDING = Configuration.getGlobalConfiguration()
        .get("CALL_RECORDING_USER_ID");

    protected static final String ACS_RESOURCE_PHONE = Configuration.getGlobalConfiguration()
        .get("AZURE_PHONE_NUMBER", "+18331234567");
    protected static final String PHONE_USER_1 = Configuration.getGlobalConfiguration()
        .get("TARGET_PHONE_NUMBER", "+16471234567");
    protected static final String MEDIA_SOURCE = Configuration.getGlobalConfiguration()
        .get("ACS_MEDIA_SOURCE", "https://acstestapp1.azurewebsites.net/audio/bot-hold-music-2.wav");
    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("value")
        .add("rawId")
        .add("id")
        .add("callbackUri");
    protected static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT),
        Pattern.CASE_INSENSITIVE);
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

    @Override
    @SuppressWarnings("unchecked")
    protected void beforeTest() {
        super.beforeTest();
        processorStore = new ConcurrentHashMap<>();
        incomingCallContextStore = new ConcurrentHashMap<>();
        eventStore = new ConcurrentHashMap<>();
        eventsToPersist = new ArrayList<>();

        // Load persisted events back to memory when in playback mode
        if (getTestMode() == TestMode.PLAYBACK) {
            try {
                InputStream fileStream = new URL(interceptorManager.getRecordingFileLocation()).openStream();
                ObjectInputStream objectInputStream =
                    new ObjectInputStream(fileStream);
                ArrayList<String> persistedEvents = (ArrayList<String>) objectInputStream.readObject();
                persistedEvents.forEach(this::messageBodyHandler);
                objectInputStream.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    // @Override
    // protected void afterTest() {
    //     super.afterTest();
    //     processorStore.forEach((key, value) -> value.close());
    //
    //     // In recording mode, manually store events from event dispatcher into local disk as the callAutomationClient doesn't do so
    //     if (getTestMode() == TestMode.RECORD) {
    //         try {
    //             String fileName = "./src/test/resources/session-records/" + testContextManager.getTestName();
    //             new FileOutputStream(fileName).close();
    //             FileOutputStream fileOutputStream = new FileOutputStream(fileName, false);
    //             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
    //             objectOutputStream.writeObject(eventsToPersist);
    //             objectOutputStream.flush();
    //             objectOutputStream.close();
    //         } catch (IOException e) {
    //             throw new RuntimeException(e);
    //         }
    //     }
    // }

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
        System.out.println(body);

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
            Thread.sleep(1000);
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
            Thread.sleep(1000);
        }
        return null;
    }
    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientUsingConnectionString(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(getHttpClientOrUsePlayback(httpClient));
        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(
                Arrays.asList(new CustomMatcher()
                    .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-hmac-string-to-sign-base64"))));
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return builder;
    }

    protected CallAutomationClientBuilder getCallAutomationClientUsingConnectionString(HttpClient httpClient) {

        CallAutomationClientBuilder builder;
        if (COMMUNICATION_CUSTOM_ENDPOINT_ENABLED) {
            builder = new CallAutomationClientBuilder()
                .connectionString(CONNECTION_STRING)
                .endpoint(PMA_ENDPOINT)
                .httpClient(getHttpClientOrUsePlayback(httpClient));
        } else {
            builder = new CallAutomationClientBuilder()
                .connectionString(CONNECTION_STRING)
                .httpClient(getHttpClientOrUsePlayback(httpClient));
        }
        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(
                Arrays.asList(new CustomMatcher()
                    .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-hmac-string-to-sign-base64", "Repeatability-First" +
                        "-Sent", "Repeatability-Request-ID", "x-ms-content-sha256"))));
        }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return builder;
    }

    protected Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process()
            .flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                /* Should sanitize printed response url */
                System.out.println("Chain-ID header for " + testName + " request "
                    + bufferedResponse.getRequest().getUrl()
                    + ": " + bufferedResponse.getHeaderValue("X-Microsoft-Skype-Chain-ID"));
                return Mono.just(bufferedResponse);
            });
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
