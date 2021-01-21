// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.chat.models.ErrorException;
import com.azure.communication.chat.models.*;
import com.azure.communication.common.CommunicationTokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import com.azure.communication.common.implementation.CommunicationConnectionString;

import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for all Chat tests
 */
public class ChatClientTestBase extends TestBase {

    protected static final TestMode TEST_MODE = initializeTestMode();

    static final String CONNECTION_STRING = Configuration.getGlobalConfiguration().get(
            "COMMUNICATION_LIVETEST_CONNECTION_STRING",
            "endpoint=https://REDACTED.communication.azure.com/;accesskey=VGhpcyBpcyBhIHRlc3Q=");

    private static String endPoint;

    private static String getTestEndPoint() {
        parseConnectionString();
        return endPoint;
    }

    private static String accessKey;

    private static String getAccessKey() {
        parseConnectionString();
        return accessKey;
    }

    private static void parseConnectionString() {
        if (endPoint == null) {
            CommunicationConnectionString envConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
            endPoint = envConnectionString.getEndpoint();
            accessKey = envConnectionString.getAccessKey();
        }
    }

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("id")
        .add("token");
        
    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT.toString()),
        Pattern.CASE_INSENSITIVE);


    protected ChatClientBuilder getChatClientBuilder(String token, HttpClient httpClient) {
        ChatClientBuilder builder = new ChatClientBuilder();

        builder.endpoint(getTestEndPoint())
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new CommunicationTokenCredential(generateRawToken()));
            return builder;
        } else {
            builder.credential(new CommunicationTokenCredential(token));
        }

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientBuilder(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder.endpoint(getTestEndPoint()).accessKey(getAccessKey())
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, HttpResponseException.class, expectedStatusCode);
    }

<<<<<<< HEAD
    static void assertRestException(Runnable exceptionThrower, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
=======
    static void assertRestException(Runnable exceptionThrower, Class<? extends ErrorException> expectedExceptionType,
            int expectedStatusCode) {
>>>>>>> e3ff6c8bd3... Parse connection string to reduce env variables
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a ErrorException and it has a specific
     * HTTP response code.
     *
     * @param exception          Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error
     *                           response
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, ErrorException.class, expectedStatusCode);
    }

<<<<<<< HEAD
    static void assertRestException(Throwable exception, Class<? extends HttpResponseException> expectedExceptionType, int expectedStatusCode) {
        assertTrue(expectedExceptionType.isAssignableFrom(exception.getClass()));
=======
    static void assertRestException(Throwable exception, Class<? extends ErrorException> expectedExceptionType,
            int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
>>>>>>> e3ff6c8bd3... Parse connection string to reduce env variables
        assertEquals(expectedStatusCode, ((HttpResponseException) exception).getResponse().getStatusCode());
    }

    /**
     * Helper method to verify that a command throws an IllegalArgumentException.
     *
     * @param exceptionThrower Command that should throw the exception
     */
    static <T> void assertRunnableThrowsException(Runnable exceptionThrower, Class<T> exception) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Exception ex) {
            assertEquals(exception, ex.getClass());
        }
    }

    public String generateRawToken() {
        String id = "communication:resourceId.userIdentity";
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        builder.claim("id", id);
        LocalDateTime expiresOnTimestamp = LocalDateTime.now().plusSeconds(100);
        ZonedDateTime ldtUTC = expiresOnTimestamp.atZone(ZoneId.of("UTC"));
        long expSeconds = ldtUTC.toInstant().toEpochMilli() / 1000;
        builder.claim("exp", expSeconds);

        JWTClaimsSet claims = builder.build();
        JWT idToken = new PlainJWT(claims);
        return idToken.serialize();
    }

<<<<<<< HEAD
    protected boolean checkParticipantsListContainsParticipantId(List<ChatParticipant> participantList, String participantId) {
        for (ChatParticipant participant: participantList) {
            if (participant.getUser().getId().equals(participantId)) {
=======
    protected boolean checkMembersListContainsMemberId(List<ChatThreadMember> memberList, String memberId) {
        for (ChatThreadMember member : memberList) {
            if (member.getUser().getId().equals(memberId)) {
>>>>>>> e3ff6c8bd3... Parse connection string to reduce env variables
                return true;
            }
        }

        return false;
    }

<<<<<<< HEAD
    protected boolean checkReadReceiptListContainsMessageId(List<ChatMessageReadReceipt> receiptList, String messageId) {
        for (ChatMessageReadReceipt receipt: receiptList) {
=======
    protected boolean checkReadReceiptListContainsMessageId(List<ReadReceipt> receiptList, String messageId) {
        for (ReadReceipt receipt : receiptList) {
>>>>>>> e3ff6c8bd3... Parse connection string to reduce env variables
            if (receipt.getChatMessageId().equals(messageId)) {
                return true;
            }
        }

        return false;
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(ChatClientTestBase.class);
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

    protected ChatClientBuilder addLoggingPolicyForIdentityClientBuilder(ChatClientBuilder builder, String testName) {
        return builder.addPolicy(new CommunicationLoggerPolicy(testName));
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
