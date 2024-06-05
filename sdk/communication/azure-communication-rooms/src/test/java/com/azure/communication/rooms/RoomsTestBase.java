// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RoomsTestBase extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(RoomsTestBase.class);

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get(
            "COMMUNICATION_CONNECTION_STRING_ROOMS",
            "endpoint=https://REDACTED.communication.azure.com/;accesskey=P2tP5RwZVFcJa3sfJvHEmGaKbemSAw2e");

    protected static final OffsetDateTime VALID_FROM = OffsetDateTime.now();
    protected static final OffsetDateTime VALID_UNTIL = VALID_FROM.plusDays(30);

    protected RoomsClientBuilder getRoomsClient(HttpClient httpClient) {
        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(
                CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        RoomsClientBuilder builder = new RoomsClientBuilder();
        builder.endpoint(communicationEndpoint).credential(new AzureKeyCredential(communicationAccessKey))
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        configureTestMode(builder);

        return builder;
    }

    protected RoomsClientBuilder getRoomsClientWithToken(HttpClient httpClient, TokenCredential tokenCredential) {
        if (getTestMode() == TestMode.PLAYBACK) {
            tokenCredential = new MockTokenCredential();
        }
        RoomsClientBuilder builder = new RoomsClientBuilder();
        builder.endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint()).credential(tokenCredential)
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        configureTestMode(builder);

        return builder;
    }

    protected RoomsClientBuilder getRoomsClientWithConnectionString(HttpClient httpClient,
            RoomsServiceVersion version) {
        RoomsClientBuilder builder = new RoomsClientBuilder();
        builder.connectionString(CONNECTION_STRING)
                .serviceVersion(version)
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        configureTestMode(builder);


        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientBuilder(HttpClient httpClient) {

        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(CONNECTION_STRING);
        String endpoint = connectionStringObject.getEndpoint();
        String accessKey = connectionStringObject.getAccessKey();
        builder.endpoint(endpoint)
                .credential(new AzureKeyCredential(accessKey))
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher(),
                    new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("repeatability-first-sent",
                            "repeatability-request-id", "x-ms-content-sha256", "x-ms-hmac-string-to-sign-base64"))));
        }

        if (!interceptorManager.isLiveMode()) {
            // Remove the sanitizer `id` from the list of common sanitizers
            interceptorManager.removeSanitizers("AZSDK3430");
        }

        return builder;
    }

    protected void configureTestMode(RoomsClientBuilder builder) {
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new BodilessMatcher(),
                    new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("repeatability-first-sent",
                            "repeatability-request-id", "x-ms-content-sha256", "x-ms-hmac-string-to-sign-base64"))));
        }
    }

    protected RoomsClientBuilder addLoggingPolicy(RoomsClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    protected void assertHappyPath(CommunicationRoom roomResult) {
        assertNotNull(roomResult.getRoomId());
        assertNotNull(roomResult.getValidUntil());
        assertNotNull(roomResult.getValidFrom());
        assertNotNull(roomResult.getCreatedAt());
    }

    protected void assertHappyPath(Response<CommunicationRoom> roomResult, int httpStatusCode) {
        assertEquals(roomResult.getStatusCode(), httpStatusCode);
        assertNotNull(roomResult.getValue().getRoomId());
        assertNotNull(roomResult.getValue().getValidUntil());
        assertNotNull(roomResult.getValue().getValidFrom());
        assertNotNull(roomResult.getValue().getCreatedAt());
    }

    private Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process().flatMap(httpResponse -> {
            final HttpResponse bufferedResponse = httpResponse.buffer();

            // Should sanitize printed reponse url
            LOGGER.log(LogLevel.VERBOSE, () -> "MS-CV header for " + testName + " request "
                + bufferedResponse.getRequest().getUrl() + ": " + bufferedResponse.getHeaderValue("MS-CV"));
            return Mono.just(bufferedResponse);
        });
    }

    protected boolean areParticipantsEqual(RoomParticipant participant1, RoomParticipant participant2) {
        return participant1.getCommunicationIdentifier().getRawId()
                .equals(participant1.getCommunicationIdentifier().getRawId())
                && participant1.getRole().toString().equals(participant2.getRole().toString());
    }

}
