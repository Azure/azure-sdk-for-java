// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.MediaType;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import reactor.core.publisher.Mono;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;

public class CallingServerTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_STATIC_CONNECTION_STRING",
            "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    protected static final String ENDPOINT = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT",
        "https://REDACTED.communication.azure.com/");

    protected static final String ENDPOINT_401 = Configuration.getGlobalConfiguration().get("COMMUNICATION_LIVETEST_STATIC_ENDPOINT_401",
        "https://REDACTED.communication.azure.com/");

    protected static final String AZURE_TENANT_ID = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_STATIC_RESOURCE_IDENTIFIER",
            "016a7064-0581-40b9-be73-6dde64d69d72");

    protected static final String FROM_PHONE_NUMBER = Configuration.getGlobalConfiguration()
        .get("AZURE_PHONE_NUMBER", "+15551234567");

    protected static final String TO_PHONE_NUMBER = Configuration.getGlobalConfiguration()
        .get("AZURE_PHONE_NUMBER", "+15551234567");

    protected static final String CALLBACK_URI = Configuration.getGlobalConfiguration()
        .get("CALLBACK_URI", "https://host.app/api/callback/calling");

    protected static final String AUDIO_FILE_URI = Configuration.getGlobalConfiguration()
        .get("AUDIO_FILE_URI", "https://host.app/audio/bot-callcenter-intro.wav");

    protected static final String METADATA_URL = Configuration.getGlobalConfiguration()
        .get("METADATA_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/acsmetadata");

    protected static final String VIDEO_URL = Configuration.getGlobalConfiguration()
        .get("VIDEO_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/video");

    protected static final String CONTENT_URL_404 = Configuration.getGlobalConfiguration()
        .get("CONTENT_URL_404", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141d/content/acsmetadata");

    protected static final String RECORDING_DELETE_URL = Configuration.getGlobalConfiguration()
        .get("RECORDING_DELETE_URL", "https://storage.asm.skype.com/v1/objects/0-eus-d11-229745ac3df2eeaf672cefd311ef2401");

    protected static final String RECORDING_DELETE_URL_404 = Configuration.getGlobalConfiguration()
        .get("RECORDING_DELETE_URL_404", "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c");

    private static final StringJoiner JSON_PROPERTIES_TO_REDACT
        = new StringJoiner("\":\"|\"", "\"", "\":\"")
        .add("to");

    private static final Pattern JSON_PROPERTY_VALUE_REDACTION_PATTERN
        = Pattern.compile(String.format("(?:%s)(.*?)(?:\",|\"})", JSON_PROPERTIES_TO_REDACT),
        Pattern.CASE_INSENSITIVE);

    protected String getNewUserId() {
        if (getTestMode() == TestMode.LIVE) {
            CommunicationIdentityClient communicationIdentityClient = new CommunicationIdentityClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();
            CommunicationUserIdentifier user = communicationIdentityClient.createUser();
            return user.getId();
        }
        return getRandomUserId();
    }

    protected String getRandomUserId() {
        return "8:acs:" + AZURE_TENANT_ID + "_" + UUID.randomUUID();
    }

    protected String getGroupId(String testName) {
        /*
          If tests are running in live mode, we want them to all
          have unique groupId's so they do not conflict with other
          recording tests running in live mode.
         */
        if (getTestMode() == TestMode.LIVE) {
            return UUID.randomUUID().toString();
        }

        /*
          For recording tests we need to make sure the groupId
          matches the recorded groupId, or the call will fail.
         */
        return UUID.nameUUIDFromBytes(testName.getBytes()).toString();
    }

    protected CallingServerClientBuilder getCallingServerClientUsingConnectionString(HttpClient httpClient) {
        CallingServerClientBuilder builder = new CallingServerClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected CallingServerClientBuilder getCallingServerClientUsingTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = getTestMode() == TestMode.PLAYBACK ? new FakeCredentials() : new DefaultAzureCredentialBuilder().build();

        CallingServerClientBuilder builder = new CallingServerClientBuilder()
            .endpoint(ENDPOINT)
            .credential(tokenCredential)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    protected CallingServerClientBuilder getCallingServerClientUsingInvalidTokenCredential(HttpClient httpClient) {
        TokenCredential tokenCredential = getTestMode() == TestMode.PLAYBACK ? new FakeCredentials() : new DefaultAzureCredentialBuilder().build();

        CallingServerClientBuilder builder = new CallingServerClientBuilder()
            .endpoint(ENDPOINT_401)
            .credential(tokenCredential)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            List<Function<String, String>> redactors = new ArrayList<>();
            redactors.add(data -> redact(data, JSON_PROPERTY_VALUE_REDACTION_PATTERN.matcher(data), "REDACTED"));
            builder.addPolicy(interceptorManager.getRecordPolicy(redactors));
        }
        return builder;
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(CallingServerTestBase.class);
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

    protected List<CallConnection> createCall(CallingServerClient callingServerClient,
                                              String groupId,
                                              String from,
                                              String to,
                                              String callBackUri) {
        CallConnection fromCallConnection =  null;
        CallConnection toCallConnection = null;

        try {
            CommunicationIdentifier fromParticipant = new CommunicationUserIdentifier(from);
            CommunicationIdentifier toParticipant = new CommunicationUserIdentifier(to);

            JoinCallOptions fromCallOptions = new JoinCallOptions(
                callBackUri,
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));
            fromCallConnection = callingServerClient.joinCall(groupId, fromParticipant, fromCallOptions);
            sleepIfRunningAgainstService(1000);
            CallingServerTestUtils.validateCallConnection(fromCallConnection);

            JoinCallOptions joinCallOptions = new JoinCallOptions(
                callBackUri,
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            toCallConnection = callingServerClient.joinCall(groupId, toParticipant, joinCallOptions);
            sleepIfRunningAgainstService(1000);
            CallingServerTestUtils.validateCallConnection(toCallConnection);

            return Arrays.asList(fromCallConnection, toCallConnection);
        } catch (Exception e) {
            System.out.println("Error creating call: " + e.getMessage());

            if (fromCallConnection != null) {
                fromCallConnection.hangup();
            }

            if (toCallConnection != null) {
                toCallConnection.hangup();
            }

            throw e;
        }
    }

    protected List<CallConnectionAsync> createAsyncCall(CallingServerAsyncClient callingServerClient,
                                                        String groupId,
                                                        String from,
                                                        String to,
                                                        String callBackUri) {
        CallConnectionAsync fromCallConnection =  null;
        CallConnectionAsync toCallConnection = null;

        try {
            CommunicationIdentifier fromParticipant = new CommunicationUserIdentifier(from);
            CommunicationIdentifier toParticipant = new CommunicationUserIdentifier(to);

            JoinCallOptions fromCallOptions = new JoinCallOptions(
                callBackUri,
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));
            fromCallConnection = callingServerClient.joinCall(groupId, fromParticipant, fromCallOptions).block();
            sleepIfRunningAgainstService(1000);
            CallingServerTestUtils.validateCallConnectionAsync(fromCallConnection);

            JoinCallOptions joinCallOptions = new JoinCallOptions(
                callBackUri,
                Collections.singletonList(MediaType.AUDIO),
                Collections.singletonList(EventSubscriptionType.PARTICIPANTS_UPDATED));

            toCallConnection = callingServerClient.joinCall(groupId, toParticipant, joinCallOptions).block();
            sleepIfRunningAgainstService(1000);
            CallingServerTestUtils.validateCallConnectionAsync(toCallConnection);

            return Arrays.asList(fromCallConnection, toCallConnection);
        } catch (Exception e) {
            System.out.println("Error creating call: " + e.getMessage());

            if (fromCallConnection != null) {
                fromCallConnection.hangup();
            }

            if (toCallConnection != null) {
                toCallConnection.hangup();
            }

            throw e;
        }
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
