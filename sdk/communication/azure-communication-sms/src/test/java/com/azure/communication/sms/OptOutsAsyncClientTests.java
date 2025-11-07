// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.models.OptOutCheckResult;
import com.azure.communication.sms.models.OptOutResult;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OptOutsAsyncClient functionality including async opt-out management
 * operations.
 */
public class OptOutsAsyncClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOutAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<List<OptOutResult>> mono = client.addOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        StepVerifier.create(mono).assertNext(results -> {
            assertNotNull(results);
            assertFalse(results.isEmpty());
            OptOutResult result = results.get(0);
            assertNotNull(result.getTo());
            assertEquals(TO_PHONE_NUMBER, result.getTo());
            assertTrue(result.getHttpStatusCode() >= 200 && result.getHttpStatusCode() < 300);
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOutMultipleAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);
        List<String> recipients = Arrays.asList(TO_PHONE_NUMBER, "+15551234568");

        Mono<List<OptOutResult>> mono = client.addOptOut(FROM_PHONE_NUMBER, recipients);

        StepVerifier.create(mono).assertNext(results -> {
            assertNotNull(results);
            assertEquals(2, results.size());
            for (OptOutResult result : results) {
                assertNotNull(result.getTo());
                assertTrue(result.getHttpStatusCode() >= 200 && result.getHttpStatusCode() < 300);
            }
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOutWithResponseAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<Response<List<OptOutResult>>> mono
            = client.addOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE);

        StepVerifier.create(mono).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getValue());
            assertFalse(response.getValue().isEmpty());
            OptOutResult result = response.getValue().get(0);
            assertEquals(TO_PHONE_NUMBER, result.getTo());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void checkOptOutAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<List<OptOutCheckResult>> mono = client.checkOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        StepVerifier.create(mono).assertNext(results -> {
            assertNotNull(results);
            assertFalse(results.isEmpty());
            OptOutCheckResult result = results.get(0);
            assertNotNull(result.getTo());
            assertEquals(TO_PHONE_NUMBER, result.getTo());
            // isOptedOut could be true, false, or null depending on state
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void checkOptOutWithResponseAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<Response<List<OptOutCheckResult>>> mono
            = client.checkOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE);

        StepVerifier.create(mono).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getValue());
            assertFalse(response.getValue().isEmpty());
            OptOutCheckResult result = response.getValue().get(0);
            assertEquals(TO_PHONE_NUMBER, result.getTo());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void removeOptOutAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<List<OptOutResult>> mono = client.removeOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        StepVerifier.create(mono).assertNext(results -> {
            assertNotNull(results);
            assertFalse(results.isEmpty());
            OptOutResult result = results.get(0);
            assertNotNull(result.getTo());
            assertEquals(TO_PHONE_NUMBER, result.getTo());
            assertTrue(result.getHttpStatusCode() >= 200 && result.getHttpStatusCode() < 300);
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void removeOptOutWithResponseAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<Response<List<OptOutResult>>> mono
            = client.removeOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE);

        StepVerifier.create(mono).assertNext(response -> {
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertNotNull(response.getValue());
            assertFalse(response.getValue().isEmpty());
            OptOutResult result = response.getValue().get(0);
            assertEquals(TO_PHONE_NUMBER, result.getTo());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void fullOptOutWorkflowAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        // Chain the operations using reactive patterns
        Mono<Void> workflow = client.addOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER).flatMap(addResults -> {
            assertNotNull(addResults);
            assertFalse(addResults.isEmpty());
            return client.checkOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        }).flatMap(checkResults -> {
            assertNotNull(checkResults);
            assertFalse(checkResults.isEmpty());
            return client.removeOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        }).flatMap(removeResults -> {
            assertNotNull(removeResults);
            assertFalse(removeResults.isEmpty());
            return Mono.empty();
        });

        StepVerifier.create(workflow).verifyComplete();
    }

    @Test
    public void addOptOutWithNullFromThrowsExceptionAsync() {
        OptOutsAsyncClient client = getOptOutsAsyncClient(null);

        Mono<List<OptOutResult>> mono = client.addOptOut(null, TO_PHONE_NUMBER);

        StepVerifier.create(mono).expectError(NullPointerException.class).verify();
    }

    @Test
    public void addOptOutWithNullToThrowsExceptionAsync() {
        OptOutsAsyncClient client = getOptOutsAsyncClient(null);

        Mono<List<OptOutResult>> mono = client.addOptOut(FROM_PHONE_NUMBER, (String) null);

        StepVerifier.create(mono).expectError(NullPointerException.class).verify();
    }

    @Test
    public void checkOptOutWithNullFromThrowsExceptionAsync() {
        OptOutsAsyncClient client = getOptOutsAsyncClient(null);

        Mono<List<OptOutCheckResult>> mono = client.checkOptOut(null, TO_PHONE_NUMBER);

        StepVerifier.create(mono).expectError(NullPointerException.class).verify();
    }

    @Test
    public void removeOptOutWithNullFromThrowsExceptionAsync() {
        OptOutsAsyncClient client = getOptOutsAsyncClient(null);

        Mono<List<OptOutResult>> mono = client.removeOptOut(null, TO_PHONE_NUMBER);

        StepVerifier.create(mono).expectError(NullPointerException.class).verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOutWithInvalidFromNumberAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<List<OptOutResult>> mono = client.addOptOut("invalid-number", TO_PHONE_NUMBER);

        StepVerifier.create(mono).expectError(HttpResponseException.class).verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void checkOptOutWithInvalidFromNumberAsync(HttpClient httpClient) {
        OptOutsAsyncClient client = getOptOutsAsyncClient(httpClient);

        Mono<List<OptOutCheckResult>> mono = client.checkOptOut("invalid-number", TO_PHONE_NUMBER);

        StepVerifier.create(mono).expectError(HttpResponseException.class).verify();
    }

    /**
     * Helper method to create OptOutsAsyncClient for testing.
     */
    private OptOutsAsyncClient getOptOutsAsyncClient(HttpClient httpClient) {
        SmsClientBuilder builder = getSmsClientBuilder(httpClient);
        return builder.buildAsyncClient().getOptOutsAsyncClient();
    }

    /**
     * Helper method to create SmsClientBuilder with proper
     * configuration.
     */
    private SmsClientBuilder getSmsClientBuilder(HttpClient httpClient) {
        SmsClientBuilder builder = new SmsClientBuilder();
        builder.connectionString(CONNECTION_STRING);

        if (httpClient != null) {
            builder
                .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }
}
