// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.messages;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class CommunicationMessagesTestBase extends TestProxyTestBase {

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_CONNECTION_STRING_CPM",
            "endpoint=https://REDACTED.int.communication.azure.net;accessKey=secret");

    protected static final String CHANNEL_REGISTRATION_ID = Configuration.getGlobalConfiguration()
        .get("SENDER_CHANNEL_REGISTRATION_ID", "77ffd898-ec44-42cd-b560-57a8903d05c7");

    protected static final String RECIPIENT_IDENTIFIER = Configuration.getGlobalConfiguration()
        .get("RECIPIENT_IDENTIFIER", "+11234567788");

    protected static final String IMAGE_URL = Configuration.getGlobalConfiguration()
        .get("IMAGE_URL", "https://upload.wikimedia.org/wikipedia/commons/3/30/Building92microsoft.jpg");

    protected static final String VIDEO_URL = Configuration.getGlobalConfiguration()
        .get("VIDEO_URL", "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4");

    protected static final String DOCUMENT_URL = Configuration.getGlobalConfiguration()
        .get("DOCUMENT_URL", "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");

    protected NotificationMessagesClientBuilder getNotificationMessagesClientBuilder(HttpClient httpClient, TokenCredential token) {

        NotificationMessagesClientBuilder notificationMessagesClientBuilder = new NotificationMessagesClientBuilder()
            .httpClient(getHttpClientOrUsePlayback(httpClient));

        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-content-sha256", "x-ms-hmac-string-to-sign-base64",
                    "repeatability-first-sent", "repeatability-request-id"))
                .setComparingBodies(false)));
        }

        if (interceptorManager.isRecordMode()) {
            notificationMessagesClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (token == null) {
            notificationMessagesClientBuilder.connectionString(CONNECTION_STRING);
        } else {
            notificationMessagesClientBuilder
                .endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint())
                .credential(token);
        }

        addTestProxySanitizer();

        return notificationMessagesClientBuilder;
    }

    protected MessageTemplateClientBuilder getMessageTemplateClientBuilder(HttpClient httpClient, TokenCredential token) {
        MessageTemplateClientBuilder templateClientBuilder = new MessageTemplateClientBuilder()
            .httpClient(getHttpClientOrUsePlayback(httpClient));

        if (token == null) {
            templateClientBuilder.connectionString(CONNECTION_STRING);
        } else {
            templateClientBuilder
                .endpoint(new CommunicationConnectionString(CONNECTION_STRING).getEndpoint())
                .credential(token);
        }

        if (interceptorManager.isPlaybackMode()) {
            interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-content-sha256", "x-ms-hmac-string-to-sign-base64"))
                .setComparingBodies(false)));
        }

        if (getTestMode() == TestMode.RECORD) {
            templateClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }

        addTestProxySanitizer();

        return templateClientBuilder;
    }

    public Mono<HttpResponse> logHeaders(HttpPipelineNextPolicy next) {
        return next.process()
            .flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                // Should sanitize printed reponse url
                System.out.println("MS-CV header for request "
                    + bufferedResponse.getRequest().getUrl() + ": " + bufferedResponse.getHeaderValue("MS-CV"));
                return Mono.just(bufferedResponse);
            });
    }

    private void addTestProxySanitizer() {
        if (!interceptorManager.isLiveMode()) {
            interceptorManager.addSanitizers(Arrays.asList(new TestProxySanitizer("$..to", null,
                "REDACTED",
                TestProxySanitizerType.BODY_KEY)));
        }
    }

}
