// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;


public class EmailTestBase extends TestBase {
    protected static final TestMode TEST_MODE = initializeTestMode();

    protected static final String CONNECTION_STRING = Configuration.getGlobalConfiguration()
        .get("COMMUNICATION_CONNECTION_STRING_EMAIL", "endpoint=https://REDACTED.communication.azure.com/;accesskey=QWNjZXNzS2V5");

    protected static final String RECIPIENT_ADDRESS = Configuration.getGlobalConfiguration()
        .get("RECIPIENT_ADDRESS", "customer@contoso.com");

    protected static final String SECOND_RECIPIENT_ADDRESS = Configuration.getGlobalConfiguration()
        .get("SECOND_RECIPIENT_ADDRESS", "customer2@contoso.com");

    protected static final String SENDER_ADDRESS = Configuration.getGlobalConfiguration()
        .get("SENDER_ADDRESS", "sender@domain.com");

    protected EmailClient getEmailClient(HttpClient httpClient) {
        return getEmailClientBuilder(httpClient).buildClient();
    }

    protected EmailAsyncClient getEmailAsyncClient(HttpClient httpClient) {
        return getEmailClientBuilder(httpClient).buildAsyncClient();
    }

    private EmailClientBuilder getEmailClientBuilder(HttpClient httpClient) {
        EmailClientBuilder emailClientBuilder = new EmailClientBuilder()
            .connectionString(CONNECTION_STRING)
            .httpClient(getHttpClientOrUsePlayback(httpClient));

        if (getTestMode() == TestMode.RECORD) {
            HttpPipelinePolicy recordPolicy = interceptorManager.getRecordPolicy();
            emailClientBuilder.addPolicy(recordPolicy);
            emailClientBuilder.addPolicy(
                (context, next) -> next.process().flatMap(response -> redactResponseHeaders(response, recordPolicy))
            );
        }

        return emailClientBuilder;
    }

    private Mono<HttpResponse> redactResponseHeaders(HttpResponse response, HttpPipelinePolicy policy) {
        response.getHeaders().set("Operation-Location", "REDACTED");
        return Mono.just(response);
    }

    private static TestMode initializeTestMode() {
        String azureTestMode = Configuration.getGlobalConfiguration().get("AZURE_TEST_MODE");
        if (azureTestMode != null) {
            System.out.println("azureTestMode: " + azureTestMode);
            try {
                return TestMode.valueOf(azureTestMode.toUpperCase(Locale.US));
            } catch (IllegalArgumentException var3) {
                return TestMode.PLAYBACK;
            }
        } else {
            return TestMode.PLAYBACK;
        }
    }

    static Stream<Arguments> getTestParameters() {
        // When this issues is closed, the newer version of junit will have better support for cartesian product of
        // arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();

        getHttpClients()
            .forEach(httpClient -> argumentsList.add(Arguments.of(httpClient)));

        return argumentsList.stream();
    }
}
