// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.common.CommunicationTokenCredential;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for all Chat tests
 */
public class ChatClientTestBase extends TestProxyTestBase {
    protected static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ENDPOINT", "https://playback.chat.azurefd.net");

    protected static final String ACCESS_KEY = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ACCESS_KEY", "pw==");


    protected ChatClientBuilder getChatClientBuilder(String token, HttpClient httpClient) {
        ChatClientBuilder builder = new ChatClientBuilder();

        builder
            .endpoint(ENDPOINT)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new CommunicationTokenCredential(generateRawToken()));
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-hmac-string-to-sign-base64", "repeatability-request-id"))));
            return builder;
        } else {
            builder.credential(new CommunicationTokenCredential(token));
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("token", null, "REDACTED", TestProxySanitizerType.BODY_KEY)));
        }

        return builder;
    }

    protected ChatThreadClientBuilder getChatThreadClientBuilder(String token, HttpClient httpClient) {
        ChatThreadClientBuilder builder = new ChatThreadClientBuilder();

        builder
            .endpoint(ENDPOINT)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new CommunicationTokenCredential(generateRawToken()));
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-hmac-string-to-sign-base64"))));
            return builder;
        } else {
            builder.credential(new CommunicationTokenCredential(token));
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("token", null, "REDACTED", TestProxySanitizerType.BODY_KEY)));
        }

        return builder;
    }

    protected CommunicationIdentityClientBuilder getCommunicationIdentityClientBuilder(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder.endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(ACCESS_KEY))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-hmac-string-to-sign-base64"))));
        }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(
                new TestProxySanitizer("token", null, "REDACTED", TestProxySanitizerType.BODY_KEY)));
        }
        return builder;
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

    protected boolean checkParticipantsListContainsParticipantId(List<ChatParticipant> participantList, String participantId) {
        for (ChatParticipant participant: participantList) {
            if (((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId().equals(participantId)) {
                return true;
            }
        }

        return false;
    }

    protected boolean checkReadReceiptListContainsMessageId(List<ChatMessageReadReceipt> receiptList, String messageId) {
        for (ChatMessageReadReceipt receipt: receiptList) {
            if (receipt.getChatMessageId().equals(messageId)) {
                return true;
            }
        }

        return false;
    }

    protected ChatClientBuilder addLoggingPolicyForIdentityClientBuilder(ChatClientBuilder builder, String testName) {
        return builder.addPolicy(new CommunicationLoggerPolicy(testName));
    }
}
