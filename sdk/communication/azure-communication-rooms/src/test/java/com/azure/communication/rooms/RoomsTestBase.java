// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomParticipant;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    protected static final String LOCAL_STRING = "endpoint=https://rooms-ppe-us.ppe.communication.azure.net/;accesskey=J9gcDYLfopqKzHIKg7BI7+Qt/ZKTg0jeO/xvUF1JWxr8sHeA9Wq3DT+bjEIo3kRfjuj84CNm3s7B/zDrqkeLnA==";
    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get(
            "COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
            "endpoint=https://REDACTED.communication.azure.com/;accesskey=P2tP5RwZVFcJa3sfJvHEmGaKbemSAw2e");

    protected static final OffsetDateTime VALID_FROM = OffsetDateTime.of(2021, 12, 1, 5, 30, 20, 10, ZoneOffset.UTC);
    protected static final OffsetDateTime VALID_UNTIL = OffsetDateTime.of(2022, 1, 1, 5, 30, 20, 10, ZoneOffset.UTC);
    protected static final String USER1 = "8:acs:db75ed0c-e801-41a3-99a4-66a0a119a06c_0000000e-3240-55cf-9806-113a0d001dd9";
    protected static final String USER2 = "8:acs:db75ed0c-e801-41a3-99a4-66a0a119a06c_0000001e-322a-f9f7-740a-113a0d00ee19";
    protected static final String USER3 = "8:acs:db75ed0c-e801-41a3-99a4-66a0a119a06c_0000002e-5609-f66d-defd-8b3a0d002749";
    protected static final Map<String, Object> PARTICIPANTS1 = new HashMap<String, Object>() {{
            put(USER1, new RoomParticipant());
            put(USER2, new RoomParticipant());        
            put(USER3, new RoomParticipant());      
        }
    };

    protected static final Map<String, Object> PARTICIPANTS2 = new HashMap<String, Object>() {{
            put(USER1, null);
            put(USER2, null);
        }
    };

    protected static final Map<String, Object> PARTICIPANTS3 = new HashMap<String, Object>() {{
            put(USER2, null);
        }
    };

    protected static final Map<String, Object> PARTICIPANTS4 = new HashMap<String, Object>() {{
            put(USER2, new RoomParticipant());
            put(USER1, new RoomParticipant());
        }
    };

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

    protected RoomsClientBuilder getRoomsClientWithConnectionString(HttpClient httpClient) {
        RoomsClientBuilder builder = new RoomsClientBuilder();
        builder.connectionString(CONNECTION_STRING)
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
}
