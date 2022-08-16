// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.identity.CommunicationIdentityServiceVersion;
import com.azure.communication.rooms.models.*;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
// import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import reactor.core.publisher.Mono;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import static org.junit.jupiter.api.Assertions.*;

public class RoomsTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get(
                    "COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
                "endpoint=https://REDACTED.communication.azure.com/;accesskey=P2tP5RwZVFcJa3sfJvHEmGaKbemSAw2e");

    protected static final OffsetDateTime VALID_FROM = OffsetDateTime.now();
    protected static final OffsetDateTime VALID_UNTIL = VALID_FROM.plusDays(30);

    protected static RoomJoinPolicy roomJoinPolicy = RoomJoinPolicy.INVITE_ONLY;

    protected List<RoomParticipant> participants1;
    protected List<RoomParticipant> participants2;
    protected List<RoomParticipant> participants3;
    protected List<RoomParticipant> participants4;
    protected List<RoomParticipant> participants5;
    protected List<RoomParticipant> participants6;
    protected List<RoomParticipant> participants7;
    protected List<RoomParticipant> badParticipant;
    protected List<RoomParticipant> participantsWithRoleUpdates;

    private CommunicationIdentityClient communicationClient;

    protected CommunicationUserIdentifier firstParticipantId;
    protected CommunicationUserIdentifier secondParticipantId;
    protected CommunicationUserIdentifier thirdParticipantId;

    protected RoomParticipant firstParticipant;
    protected RoomParticipant secondParticipant;
    protected RoomParticipant thirdParticipant;
    protected RoomParticipant firstChangeParticipant;
    protected RoomParticipant secondChangeParticipant;
    protected RoomParticipant validateParticipant1;
    protected RoomParticipant validateParticipant2;
    protected RoomParticipant validateParticipant3;


    protected static final String NONEXIST_ROOM_ID = "NotExistingRoomID";

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT = new StringJoiner("\":\"|\"", "\"", "\":\"").add("roomId");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN = Pattern.compile(
            String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()), Pattern.CASE_INSENSITIVE);

    protected RoomsClientBuilder getRoomsClient(HttpClient httpClient) {
        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(
                CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        RoomsClientBuilder builder = new RoomsClientBuilder();
        builder.endpoint(communicationEndpoint).credential(new AzureKeyCredential(communicationAccessKey))
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);
        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected RoomsClientBuilder getRoomsClientWithToken(HttpClient httpClient, TokenCredential tokenCredential) {
        if (getTestMode() == TestMode.PLAYBACK) {
            tokenCredential = new FakeCredentials();
        }
        RoomsClientBuilder builder = new RoomsClientBuilder();
        builder.endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint()).credential(tokenCredential)
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected RoomsClientBuilder getRoomsClientWithConnectionString(HttpClient httpClient, RoomsServiceVersion version) {
        RoomsClientBuilder builder = new RoomsClientBuilder();
        builder.connectionString(CONNECTION_STRING)
                .serviceVersion(version)
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientBuilder(HttpClient httpClient) {

        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        CommunicationConnectionString connectionStringObject = new CommunicationConnectionString(CONNECTION_STRING);
        String endpoint = connectionStringObject.getEndpoint();
        String accessKey = connectionStringObject.getAccessKey();
        builder.endpoint(endpoint)
            .credential(new AzureKeyCredential(accessKey))
            .serviceVersion(CommunicationIdentityServiceVersion.V2021_03_07)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(RoomsTestBase.class);
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            System.out.println("azureTestMode: " + azureTestMode);
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException var3) {
                logger.error("Could not parse '{}' into TestEnum. Using 'Playback' mode.", azureTestMode);
                return TestMode.PLAYBACK;
            }
        } else {
            logger.info("Environment variable '{}' has not been set yet. Using 'Playback' mode.", "AZURE_TEST_MODE");
            return TestMode.PLAYBACK;
        }
    }

    protected void createUsers(HttpClient httpClient) {
        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        firstParticipantId = communicationClient.createUser();
        secondParticipantId = communicationClient.createUser();
        thirdParticipantId = communicationClient.createUser();

        firstParticipant = new RoomParticipant().setCommunicationIdentifier(firstParticipantId).setRole(RoleType.ATTENDEE);
        secondParticipant = new RoomParticipant().setCommunicationIdentifier(secondParticipantId).setRole(RoleType.ATTENDEE);
        thirdParticipant = new RoomParticipant().setCommunicationIdentifier(thirdParticipantId).setRole(RoleType.CONSUMER);

        firstChangeParticipant = new RoomParticipant().setCommunicationIdentifier(firstParticipantId).setRole(RoleType.CONSUMER);
        secondChangeParticipant = new RoomParticipant().setCommunicationIdentifier(firstParticipantId).setRole(RoleType.CONSUMER);

        validateParticipant1 = new RoomParticipant().setCommunicationIdentifier(firstParticipantId);
        validateParticipant2 = new RoomParticipant().setCommunicationIdentifier(secondParticipantId);
        validateParticipant3 = new RoomParticipant().setCommunicationIdentifier(thirdParticipantId);

        participants1 = Arrays.asList(firstParticipant, secondParticipant, thirdParticipant);
        participants2 = Arrays.asList(firstParticipant, secondParticipant);
        participants3 = Arrays.asList(secondParticipant);
        participants4 = Arrays.asList(firstParticipant, secondParticipant);
        participants5 = Arrays.asList(firstParticipant, secondParticipant, thirdParticipant);
        participants6 = Arrays.asList(secondParticipant, thirdParticipant);
        participants7 = Arrays.asList();
        badParticipant = Arrays.asList(new RoomParticipant().setCommunicationIdentifier(new CommunicationUserIdentifier("Dummy_Mri")).setRole(RoleType.CONSUMER));
        participantsWithRoleUpdates = Arrays.asList(firstChangeParticipant, secondChangeParticipant);
    }

    protected void cleanUpUsers() {
//        communicationClient.deleteUser(firstParticipantId);
//        communicationClient.deleteUser(secondParticipantId);
//        communicationClient.deleteUser(thirdParticipantId);
    }

    protected RoomsClientBuilder addLoggingPolicy(RoomsClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    protected void assertHappyPath(CommunicationRoom roomResult) {
        assertNotNull(roomResult.getRoomId());
    }

    protected void assertHappyPath(Response<CommunicationRoom> roomResult, int httpStatusCode) {
        assertEquals(roomResult.getStatusCode(), httpStatusCode);
        assertNotNull(roomResult.getValue().getRoomId());
    }

    private Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process().flatMap(httpResponse -> {
            final HttpResponse bufferedResponse = httpResponse.buffer();

            // Should sanitize printed reponse url
            System.out.println("MS-CV header for " + testName + " request " + bufferedResponse.getRequest().getUrl()
                    + ": " + bufferedResponse.getHeaderValue("MS-CV"));
            return Mono.just(bufferedResponse);
        });
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }

    private String redact(String content, Matcher matcher, String replacement) {
        while (matcher.find()) {
            String captureGroup = matcher.group(1);
            if (!CoreUtils.isNullOrEmpty(captureGroup)) {
                content = content.replace(matcher.group(1), replacement);
            }
        }
        return content;
    }

    protected boolean areParticipantsEqual(RoomParticipant participant1, RoomParticipant participant2) {
        return participant1.getCommunicationIdentifier().getRawId().equals(participant1.getCommunicationIdentifier().getRawId())
            && participant1.getRole().toString().equals(participant2.getRole().toString());
    }
}
