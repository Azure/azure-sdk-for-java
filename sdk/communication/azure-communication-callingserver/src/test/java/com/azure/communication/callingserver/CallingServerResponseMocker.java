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

import com.azure.communication.callingserver.implementation.converters.CallLocatorConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.*;
import com.azure.communication.callingserver.models.*;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
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
    public static final String AUDIO_GROUPID = "AudioGroupId";
    public static final String OPERATION_CONTEXT = "operationContext";
    public static final String INCOMING_CALL_CONTEXT = "incomingCallContext";
    public static final int TIMEOUT = 3;
    public static final String MOCK_CONNECTION_STRING = "endpoint=https://REDACTED.communication.azure.com/;accesskey=eyJhbG";
    public static final String NEW_PARTICIPANT_ID = "newParticipantId";
    public static final String USER_ID = "USER_ID";
    public static final CommunicationUserIdentifier COMMUNICATION_USER = new CommunicationUserIdentifier(USER_ID);
    public static final String NEW_PARTICIPANT_ID_1 = "newParticipantId_1";
    public static final String USER_ID_1 = "USER_ID_1";
    public static final CommunicationUserIdentifier COMMUNICATION_USER_1 = new CommunicationUserIdentifier(USER_ID_1);
    public static final URI CALLBACK_URI = URI.create("https://callBackUri.local");
    public static final ServerCallLocator SERVERCALL_LOCATOR = new ServerCallLocator("aHR0cHM6Ly9jb252LXVzd2UtMDguY29udi5za3lwZS5jb20vY29udi8tby1FWjVpMHJrS3RFTDBNd0FST1J3P2k9ODgmZT02Mzc1Nzc0MTY4MDc4MjQyOTM");
    public static final PhoneNumberIdentifier ALTERNATE_CALLER_ID = new PhoneNumberIdentifier("+15551234567");

    public static String generateCreateCallResult(String callConnectionId) {

        CreateCallResultInternal result = new CreateCallResultInternal()
            .setCallConnectionId(callConnectionId);

        return serializeObject(result);
    }

    public static String generateGetAudioGroupsResult() {

        AudioGroupResultInternal result = new AudioGroupResultInternal()
            .setAudioRoutingMode(AudioRoutingMode.ONE_TO_ONE)
            .setTargets(Collections.singletonList(CommunicationIdentifierConverter.convert(COMMUNICATION_USER)));

        return serializeObject(result);
    }

    public static String generateAddParticipantResult() {

        AddParticipantResultInternal result = new AddParticipantResultInternal()
            .setOperationContext(OPERATION_CONTEXT)
            .setOperationId(OPERATION_ID)
            .setStatus(CallingOperationStatus.RUNNING)
            .setResultDetails(new CallingOperationResultDetailsInternal().setCode(202).setSubcode(0).setMessage("message"));

        return serializeObject(result);
    }

    public static String generateGetParticipantResult() {
        CallParticipantInternal result = new CallParticipantInternal()
            .setParticipantId(NEW_PARTICIPANT_ID)
            .setIdentifier(CommunicationIdentifierConverter.convert(COMMUNICATION_USER))
            .setIsMuted(true);

        return serializeObject(result);
    }

    public static String generateGetParticipantsResult() {
        List<CallParticipantInternal> result = new ArrayList<>();
        CallParticipantInternal participant = new CallParticipantInternal()
            .setParticipantId(NEW_PARTICIPANT_ID)
            .setIdentifier(CommunicationIdentifierConverter.convert(COMMUNICATION_USER))
            .setIsMuted(true);
        CallParticipantInternal participant1 = new CallParticipantInternal()
            .setParticipantId(NEW_PARTICIPANT_ID_1)
            .setIdentifier(CommunicationIdentifierConverter.convert(COMMUNICATION_USER_1))
            .setIsMuted(true);
        result.add(participant);
        result.add(participant1);

        return serializeObject(result);
    }

    public static String generateGetCallResult() {

        CallConnectionPropertiesInternal result = new CallConnectionPropertiesInternal()
            .setCallbackUri(CALLBACK_URI.toString())
            .setCallConnectionId(CALL_CONNECTION_ID)
            .setCallLocator(CallLocatorConverter.convert(SERVERCALL_LOCATOR))
            .setCallConnectionState(CallConnectionState.CONNECTED)
            .setRequestedCallEvents(Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED))
            .setRequestedMediaTypes(Collections.singletonList(CallMediaType.AUDIO))
            .setTargets(Collections.singletonList(CommunicationIdentifierConverter.convert(COMMUNICATION_USER)))
            .setSource(CommunicationIdentifierConverter.convert(COMMUNICATION_USER_1));

        return serializeObject(result);
    }

    public static String generateDownloadResult(String content) {
        return content;
    }

    public static String generatePlayAudioToParticipantResult() {
        return generatePlayAudioResult();
    }

    public static String generatePlayAudioResult() {
        PlayAudioResultInternal result = new PlayAudioResultInternal()
            .setOperationContext(OPERATION_CONTEXT)
            .setOperationId(OPERATION_ID)
            .setStatus(CallingOperationStatus.COMPLETED)
            .setResultDetails(new CallingOperationResultDetailsInternal().setCode(202).setSubcode(0).setMessage("message"));

        return serializeObject(result);
    }

    public static String generateJoinCallResult(String callConnectionId) {
        JoinCallResultInternal result = new JoinCallResultInternal()
            .setCallConnectionId(callConnectionId);

        return serializeObject(result);
    }

    public static String generateAnswerCallResult(String callConnectionId) {
        AnswerCallResultInternal result = new AnswerCallResultInternal()
            .setCallConnectionId(callConnectionId);

        return serializeObject(result);
    }

    public static String generateTransferCallResult() {
        TransferCallResultInternal result = new TransferCallResultInternal()
            .setOperationContext(OPERATION_CONTEXT)
            .setOperationId(OPERATION_ID)
            .setStatus(CallingOperationStatus.COMPLETED)
            .setResultDetails(new CallingOperationResultDetailsInternal().setCode(202).setSubcode(0).setMessage("message"));

        return serializeObject(result);
    }

    public static String generateCreateAudioGroupResult() {
        CreateAudioGroupResultInternal result = new CreateAudioGroupResultInternal()
            .setAudioGroupId(AUDIO_GROUPID);

        return serializeObject(result);
    }

    public static CallConnectionAsync getCallConnectionAsync(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallingServerAsyncClient callingServerAsyncClient = getCallingServerAsyncClient(responses);

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        CreateCallOptions options = new CreateCallOptions(
            CALLBACK_URI,
            Collections.singletonList(CallMediaType.AUDIO),
            Collections.singletonList(CallingEventSubscriptionType.PARTICIPANTS_UPDATED));

        return callingServerAsyncClient.createCallConnection(sourceUser, targetUsers, options).block();
    }

    public static CallConnection getCallConnection(ArrayList<SimpleEntry<String, Integer>> responses) {
        CallingServerClient callingServerClient = getCallingServerClient(responses);

        CommunicationUserIdentifier sourceUser = new CommunicationUserIdentifier("id");
        List<CommunicationIdentifier> targetUsers = new ArrayList<CommunicationIdentifier>();
        CreateCallOptions options = new CreateCallOptions(
            CALLBACK_URI,
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
