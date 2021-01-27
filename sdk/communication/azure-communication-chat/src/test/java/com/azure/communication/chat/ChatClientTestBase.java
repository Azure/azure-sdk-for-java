// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.azure.communication.administration.CommunicationIdentityClientBuilder;
import com.azure.communication.chat.models.ErrorException;
import com.azure.communication.chat.models.*;
import com.azure.communication.common.CommunicationTokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;

/**
 * Abstract base class for all Chat tests
 */
public class ChatClientTestBase extends TestBase {

    protected static final TestMode TEST_MODE = initializeTestMode();

    protected static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ENDPOINT", "https://playback.chat.azurefd.net");

    protected static final String ACCESS_KEY = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ACCESS_KEY", "pw==");

    protected ChatClientBuilder getChatClientBuilder(String token, HttpClient httpClient) {
        ChatClientBuilder builder = new ChatClientBuilder();

        builder
            .endpoint(ENDPOINT)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new CommunicationTokenCredential(generateRawToken()));
            return builder;
        } else {
            builder.credential(new CommunicationTokenCredential(token));
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientBuilder(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder.endpoint(ENDPOINT)
            .accessKey(ACCESS_KEY)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        return builder;
    }

    static void assertRestException(Runnable exceptionThrower, int expectedStatusCode) {
        assertRestException(exceptionThrower, ErrorException.class, expectedStatusCode);
    }

    static void assertRestException(Runnable exceptionThrower, Class<? extends ErrorException> expectedExceptionType, int expectedStatusCode) {
        try {
            exceptionThrower.run();
            fail();
        } catch (Throwable ex) {
            assertRestException(ex, expectedExceptionType, expectedStatusCode);
        }
    }

    /**
     * Helper method to verify the error was a ErrorException and it has a specific HTTP response code.
     *
     * @param exception Expected error thrown during the test
     * @param expectedStatusCode Expected HTTP status code contained in the error response
     */
    static void assertRestException(Throwable exception, int expectedStatusCode) {
        assertRestException(exception, ErrorException.class, expectedStatusCode);
    }

    static void assertRestException(Throwable exception, Class<? extends ErrorException> expectedExceptionType, int expectedStatusCode) {
        assertEquals(expectedExceptionType, exception.getClass());
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

        JWTClaimsSet claims =  builder.build();
        JWT idToken = new PlainJWT(claims);
        return idToken.serialize();
    }

    protected boolean checkMembersListContainsMemberId(List<ChatThreadMember> memberList, String memberId) {
        for (ChatThreadMember member: memberList) {
            if (member.getUser().getId().equals(memberId)) {
                return true;
            }
        }

        return false;
    }

    protected boolean checkReadReceiptListContainsMessageId(List<ReadReceipt> receiptList, String messageId) {
        for (ReadReceipt receipt: receiptList) {
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
}
