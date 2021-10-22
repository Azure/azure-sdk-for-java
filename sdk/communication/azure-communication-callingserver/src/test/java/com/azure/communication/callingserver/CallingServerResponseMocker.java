// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import com.azure.communication.callingserver.implementation.models.*;
import com.azure.communication.callingserver.models.CallMediaType;
import com.azure.communication.callingserver.models.CallingEventSubscriptionType;
import com.azure.communication.callingserver.models.CallingOperationStatus;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CallingServerResponseMocker {
    public static final String CALL_CONNECTION_ID = "callConnectionId";
    public static final String OPERATION_ID = "operationId";
    public static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbG";
    public static final CommunicationUserIdentifier NEW_PARTICIPANT = new CommunicationUserIdentifier("newParticipantId");
    public static final URI URI_CALLBACK = URI.create("https://callBackUri.local");
    public static final String SERVER_CALL_ID = "serverCallId";

    public static String generateCreateCallResult(String callConnectionId) {

        CreateCallResultInternal result = new CreateCallResultInternal()
            .setCallConnectionId(callConnectionId);

        return serializeObject(result);
    }

    public static String generateAddParticipantResult(String participantId) {

        AddParticipantResultInternal result = new AddParticipantResultInternal()
            .setParticipantId(participantId);

        return serializeObject(result);
    }

    public static String generateDownloadResult(String content) {
        return content;
    }

    public static String generatePlayAudioResult(String operationId, CallingOperationStatus status, CallingOperationResultDetailsInternal resultInfo) {

        PlayAudioResultInternal result = new PlayAudioResultInternal()
            .setOperationContext("operationContext")
            .setOperationId(operationId)
            .setStatus(status)
            .setResultInfo(resultInfo);

        return serializeObject(result);
    }

    public static String generateJoinCallResult(String callConnectionId) {
        JoinCallResultInternal result = new JoinCallResultInternal()
            .setCallConnectionId(callConnectionId);

        return serializeObject(result);
    }

    public static CallConnectionAsync getCallConnectionAsync(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(responses);

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        CreateCallOptions options = new CreateCallOptions(
            URI.create("https://callbackUri.local"),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        return callingServerAsyncClient.createCallConnection(sourceUser, targetUsers, options).block();
    }

    public static CallConnection getCallConnection(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallingServerClient callingServerClient = getCallingServerClient(responses);

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        CreateCallOptions options = new CreateCallOptions(
            URI.create("https://callbackUri.local"),
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        return callingServerClient.createCallConnection(sourceUser, targetUsers, options);
    }

    public static CallingServerAsyncClient getCallingServerAsyncClient(ArrayList<SimpleEntry<String, Integer>> responses) {
        HttpClient mockHttpClient = new MockHttpClient(responses);

        return new CallingServerClientBuilder()
            .httpClient(mockHttpClient)
            .connectionString(MOCK_CONNECTION_STRING)
            .buildAsyncClient();
    }

    public static CallingServerClient getCallingServerClient(ArrayList<SimpleEntry<String, Integer>> responses) {
        HttpClient mockHttpClient = new MockHttpClient(responses);

        return new CallingServerClientBuilder()
            .httpClient(mockHttpClient)
            .connectionString(MOCK_CONNECTION_STRING)
            .buildClient();
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

    private static String serializeObject(Object o) {
        ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return body;
    }
}
