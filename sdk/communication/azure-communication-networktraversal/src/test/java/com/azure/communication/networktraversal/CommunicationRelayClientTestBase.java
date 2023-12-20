// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.communication.common.implementation.CommunicationConnectionString;
import com.azure.communication.identity.CommunicationIdentityClientBuilder;
import com.azure.communication.identity.CommunicationIdentityServiceVersion;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommunicationRelayClientTestBase extends TestProxyTestBase {
    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_LIVETEST_DYNAMIC_CONNECTION_STRING", "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    private static final List<String> JSON_PROPERTIES_TO_REDACT
        = new ArrayList<>(Arrays.asList("token", "id", "credential"));
    private static List<TestProxySanitizer> addBodyKeySanitizer() {
        return JSON_PROPERTIES_TO_REDACT.stream().map(key -> new TestProxySanitizer(String.format("$..%s", key), null,
            "REDACTED",
            TestProxySanitizerType.BODY_KEY)).collect(Collectors.toList());
    }

    protected CommunicationRelayClientBuilder createClientBuilder(HttpClient httpClient) {
        CommunicationRelayClientBuilder builder = new CommunicationRelayClientBuilder();

        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();
        String communicationAccessKey = communicationConnectionString.getAccessKey();

        builder.endpoint(communicationEndpoint)
            .credential(new AzureKeyCredential(communicationAccessKey))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        addSanitizersAndMatchers();
        return builder;
    }

    protected CommunicationRelayClientBuilder createClientBuilderUsingManagedIdentity(HttpClient httpClient) {
        CommunicationRelayClientBuilder builder = new CommunicationRelayClientBuilder();

        CommunicationConnectionString communicationConnectionString = new CommunicationConnectionString(CONNECTION_STRING);
        String communicationEndpoint = communicationConnectionString.getEndpoint();

        builder
            .endpoint(communicationEndpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new MockTokenCredential());
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        addSanitizersAndMatchers();
        return builder;
    }

    protected CommunicationRelayClientBuilder createClientBuilderUsingConnectionString(HttpClient httpClient) {
        CommunicationRelayClientBuilder builder = new CommunicationRelayClientBuilder();
        builder
            .connectionString(CONNECTION_STRING)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        addSanitizersAndMatchers();
        return builder;
    }

    protected CommunicationIdentityClientBuilder createIdentityClientBuilder(HttpClient httpClient) {
        CommunicationIdentityClientBuilder builder = new CommunicationIdentityClientBuilder();
        builder
            .connectionString(CONNECTION_STRING)
            .serviceVersion(CommunicationIdentityServiceVersion.V2022_10_01)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        addSanitizersAndMatchers();
        return builder;
    }

    private void addSanitizersAndMatchers() {
        interceptorManager.addMatchers(Arrays.asList(new CustomMatcher().setHeadersKeyOnlyMatch(
            Arrays.asList("x-ms-hmac-string-to-sign-base64", "x-ms-content-sha", "x-ms-content-sha256"))));
        interceptorManager.addSanitizers(addBodyKeySanitizer());
    }

    protected CommunicationRelayClientBuilder addLoggingPolicy(CommunicationRelayClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    private Mono<HttpResponse> logHeaders(String testName, HttpPipelineNextPolicy next) {
        return next.process()
            .flatMap(httpResponse -> {
                final HttpResponse bufferedResponse = httpResponse.buffer();

                // Should sanitize printed reponse url
                System.out.println("MS-CV header for " + testName + " request "
                    + bufferedResponse.getRequest().getUrl() + ": " + bufferedResponse.getHeaderValue("MS-CV"));
                return Mono.just(bufferedResponse);
            });
    }
}
