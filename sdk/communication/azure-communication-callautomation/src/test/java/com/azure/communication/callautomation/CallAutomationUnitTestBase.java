// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callautomation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;

import com.azure.communication.callautomation.implementation.models.AddParticipantResponseInternal;
import com.azure.communication.callautomation.implementation.models.CallConnectionPropertiesInternal;
import com.azure.communication.callautomation.implementation.models.CallConnectionStateModelInternal;
import com.azure.communication.callautomation.implementation.models.CallParticipantInternal;
import com.azure.communication.callautomation.implementation.models.GetParticipantsResponseInternal;
import com.azure.communication.callautomation.implementation.models.DialogStateResponse;
import com.azure.communication.callautomation.models.MediaStreamingAudioChannel;
import com.azure.communication.callautomation.models.MediaStreamingOptions;
import com.azure.communication.callautomation.models.MediaStreamingContent;
import com.azure.communication.callautomation.models.MediaStreamingTransport;
import com.azure.communication.callautomation.models.TranscriptionOptions;
import com.azure.communication.callautomation.models.TranscriptionTransportType;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import com.azure.json.JsonProviders;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CallAutomationUnitTestBase {
    static final String MOCK_ENDPOINT = "https://REDACTED.communication.azure.com/";
    static final String MOCK_CONNECTION_STRING = String.format("endpoint=%s;accesskey=eyJhbG", MOCK_ENDPOINT);
    static final String CALL_CONNECTION_ID = "callConnectionId";
    static final String CALL_SERVER_CALL_ID = "serverCallId";
    static final String CALL_CALLER_ID = "callerId";
    static final CommunicationUserIdentifier USER_1 = new CommunicationUserIdentifier("userId1");
    static final String CALL_CALLER_DISPLAY_NAME = "callerDisplayName";
    static final String CALL_TARGET_ID = "targetId";
    static final String CALL_TRANSFEREE_ID = "transfereeId";
    static final String CALL_CONNECTION_STATE = "connected";
    static final String CALL_SUBJECT = "subject";
    static final String CALL_CALLBACK_URL = "https://REDACTED.com/events";
    static final String CALL_INCOMING_CALL_CONTEXT = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.REDACTED";
    static final String CALL_OPERATION_CONTEXT = "operationContext";
    static final String MEDIA_SUBSCRIPTION_ID = "mediaSubscriptionId";
    static final String DATA_SUBSCRIPTION_ID = "dataSubscriptionId";
    static final String DIALOG_ID = "dialogId";
    static final String BOT_APP_ID = "botAppId";

    static final MediaStreamingOptions MEDIA_STREAMING_CONFIGURATION = new MediaStreamingOptions(
        "https://websocket.url.com",
        MediaStreamingTransport.WEBSOCKET,
        MediaStreamingContent.AUDIO,
        MediaStreamingAudioChannel.MIXED,
        true
    );

    static final TranscriptionOptions TRANSCRIPTION_CONFIGURATION = new TranscriptionOptions(
        "https://websocket.url.com",
        TranscriptionTransportType.WEBSOCKET,
        "en-US",
        true
    );

    public static String generateDownloadResult(String content) {
        return content;
    }

    public static String generateCallProperties(String callConnectionId, String serverCallId, String callerId,
                                                String callerDisplayName, String targetId, String connectionState,
                                                String subject, String callbackUri, String mediaSubscriptionId, String dataSubscriptionId) {
        CallConnectionPropertiesInternal result = new CallConnectionPropertiesInternal()
            .setCallConnectionId(callConnectionId)
            .setServerCallId(serverCallId)
            .setCallbackUri(callbackUri)
            .setCallConnectionState(CallConnectionStateModelInternal.fromString(connectionState))
            .setMediaSubscriptionId(mediaSubscriptionId)
            .setDataSubscriptionId(dataSubscriptionId)
            .setSourceDisplayName(callerDisplayName)
            .setTargets(new ArrayList<>(Collections.singletonList(ModelGenerator.generateUserIdentifierModel(targetId)))
            );

        return serializeObject(result);
    }

    public static String generateGetParticipantResponse(String callerId, boolean isMuted, boolean isHold) {
        CallParticipantInternal callParticipant = ModelGenerator.generateAcsCallParticipantInternal(callerId, isMuted, isHold);
        return serializeObject(callParticipant);
    }

    public static String generateListParticipantsResponse() {
        GetParticipantsResponseInternal getParticipantsResponseInternal = new GetParticipantsResponseInternal()
            .setValue(new ArrayList<>(Arrays.asList(
                ModelGenerator.generateAcsCallParticipantInternal(CALL_CALLER_ID, false, false),
                ModelGenerator.generateAcsCallParticipantInternal(CALL_TARGET_ID, true, true))))
            .setNextLink("");

        return serializeObject(getParticipantsResponseInternal);
    }

    public static String generateAddParticipantsResponse() {
        AddParticipantResponseInternal addParticipantsResponseInternal = new AddParticipantResponseInternal()
            .setOperationContext(CALL_OPERATION_CONTEXT)
            .setParticipant(ModelGenerator.generateAcsCallParticipantInternal(CALL_TARGET_ID, false, false));

        return serializeObject(addParticipantsResponseInternal);
    }

    public static String generateDialogStateResponse() {
        DialogStateResponse dialogStateResponse = new DialogStateResponse()
            .setDialogId(DIALOG_ID);

        return serializeObject(dialogStateResponse);
    }

    public static CallAutomationAsyncClient getCallAutomationAsyncClient(ArrayList<SimpleEntry<String, Integer>> responses) {
        HttpClient mockHttpClient = new MockHttpClient(responses);

        return new CallAutomationClientBuilder()
            .httpClient(mockHttpClient)
            .connectionString(MOCK_CONNECTION_STRING)
            .buildAsyncClient();
    }

    public static CallAutomationClient getCallAutomationClient(ArrayList<SimpleEntry<String, Integer>> responses) {
        HttpClient mockHttpClient = new MockHttpClient(responses);

        return new CallAutomationClientBuilder()
            .httpClient(mockHttpClient)
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient();
    }

    public static CallAutomationClient getCallAutomationClientWithSourceIdentity(CommunicationUserIdentifier sourceIdentifier,
            ArrayList<SimpleEntry<String, Integer>> responses) {
        HttpClient mockHttpClient = new MockHttpClient(responses);

        return new CallAutomationClientBuilder()
            .httpClient(mockHttpClient)
            .connectionString(MOCK_CONNECTION_STRING)
            .sourceIdentity(sourceIdentifier)
            .buildClient();
    }

    public static CallConnection getCallConnection(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallAutomationClient callAutomationClient = getCallAutomationClient(responses);
        return callAutomationClient.getCallConnection("callConnectionId");
    }

    public static CallConnectionAsync getCallConnectionAsync(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallAutomationAsyncClient callingServerClient = getCallAutomationAsyncClient(responses);
        return callingServerClient.getCallConnectionAsync("callConnectionId");
    }

    public static HttpResponse generateMockResponse(String body, HttpRequest request, int statusCode) {
        return new HttpResponse(request) {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getHeaderValue(String name) {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.just(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(body.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just(body);
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return Mono.just(body);
            }
        };
    }

    static String serializeObject(JsonSerializable<?> o) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            o.toJson(writer);
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
