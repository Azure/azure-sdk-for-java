// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.communication.sms.models.SendSmsResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmsLiveTestBase extends TestBase {

    protected static final String DEFAULT_ACCESS_KEY = "VGhpcyBpcyBhIHRlc3Q="; // Base64 encoded "This is a test"
    static final TestMode TEST_MODE = initializeTestMode();
    static final String PHONENUMBER = Configuration.getGlobalConfiguration()
        .get("SMS_SERVICE_PHONE_NUMBER", "+18005555555");

    static final String ACCESSKEY = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ACCESS_KEY", DEFAULT_ACCESS_KEY);

    static final String ENDPOINT = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_SERVICE_ENDPOINT", "https://REDACTED.communication.azure.com");

    static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=VGhpcyBpcyBhIHRlc3Q=");

    protected SmsClientBuilder getSmsClientBuilder(HttpClient httpClient) {
        SmsClientBuilder builder = new SmsClientBuilder();

        builder.endpoint(ENDPOINT)
                .accessKey(ACCESSKEY)
                .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected SmsClientBuilder getSmsClientBuilderWithManagedIdentity(HttpClient httpClient) {
        SmsClientBuilder builder = new SmsClientBuilder();
        String livetestEndpoint = new CommunicationConnectionString(CONNECTION_STRING).getEndpoint();

        builder.endpoint(livetestEndpoint)
               .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new FakeCredentials());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected SmsClientBuilder getSmsClientBuilderWithConnectionString(HttpClient httpClient) {
        SmsClientBuilder builder = new SmsClientBuilder();

        builder
            .connectionString(CONNECTION_STRING)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    protected void verifyResponse(Response<SendSmsResponse> response) {
        assertNotNull(response);
        verifyResponse(response.getValue());
    }

    protected void verifyResponse(SendSmsResponse response) {
        assertNotNull(response);
        assertNotNull(response.getMessageId());
        assertTrue(response.getMessageId().contains("Outgoing_"));
    }

    private static TestMode initializeTestMode() {
        ClientLogger logger = new ClientLogger(SmsTestBase.class);
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

    protected SmsClientBuilder addLoggingPolicy(SmsClientBuilder builder, String testName) {
        return builder.addPolicy(new CommunicationLoggerPolicy(testName));
    }

    static class FakeCredentials implements TokenCredential {
        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {
            return Mono.just(new AccessToken("someFakeToken", OffsetDateTime.MAX));
        }
    }
}
