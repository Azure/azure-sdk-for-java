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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OptOutsClient functionality including opt-out management operations.
 */
public class OptOutsClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOut(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        List<OptOutResult> results = client.addOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        OptOutResult result = results.get(0);
        assertNotNull(result.getTo());
        assertEquals(TO_PHONE_NUMBER, result.getTo());
        assertTrue(result.getHttpStatusCode() >= 200 && result.getHttpStatusCode() < 300);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOutMultiple(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);
        List<String> recipients = Arrays.asList(TO_PHONE_NUMBER, "+15551234568");

        List<OptOutResult> results = client.addOptOut(FROM_PHONE_NUMBER, recipients);

        assertNotNull(results);
        assertEquals(2, results.size());
        for (OptOutResult result : results) {
            assertNotNull(result.getTo());
            assertTrue(result.getHttpStatusCode() >= 200 && result.getHttpStatusCode() < 300);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOutWithResponse(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        Response<List<OptOutResult>> response
            = client.addOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue());
        assertFalse(response.getValue().isEmpty());
        OptOutResult result = response.getValue().get(0);
        assertEquals(TO_PHONE_NUMBER, result.getTo());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void checkOptOut(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        List<OptOutCheckResult> results = client.checkOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        OptOutCheckResult result = results.get(0);
        assertNotNull(result.getTo());
        assertEquals(TO_PHONE_NUMBER, result.getTo());
        // isOptedOut could be true, false, or null depending on state
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void checkOptOutWithResponse(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        Response<List<OptOutCheckResult>> response
            = client.checkOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue());
        assertFalse(response.getValue().isEmpty());
        OptOutCheckResult result = response.getValue().get(0);
        assertEquals(TO_PHONE_NUMBER, result.getTo());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void removeOptOut(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        List<OptOutResult> results = client.removeOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        OptOutResult result = results.get(0);
        assertNotNull(result.getTo());
        assertEquals(TO_PHONE_NUMBER, result.getTo());
        assertTrue(result.getHttpStatusCode() >= 200 && result.getHttpStatusCode() < 300);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void removeOptOutWithResponse(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        Response<List<OptOutResult>> response
            = client.removeOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue());
        assertFalse(response.getValue().isEmpty());
        OptOutResult result = response.getValue().get(0);
        assertEquals(TO_PHONE_NUMBER, result.getTo());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void fullOptOutWorkflow(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        // First, add opt-out
        List<OptOutResult> addResults = client.addOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        assertNotNull(addResults);
        assertFalse(addResults.isEmpty());

        // Check opt-out status
        List<OptOutCheckResult> checkResults = client.checkOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        assertNotNull(checkResults);
        assertFalse(checkResults.isEmpty());

        // Remove opt-out
        List<OptOutResult> removeResults = client.removeOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        assertNotNull(removeResults);
        assertFalse(removeResults.isEmpty());
    }

    @Test
    public void addOptOutWithNullFromThrowsException() {
        OptOutsClient client = getOptOutsClient(null);

        assertThrows(NullPointerException.class, () -> {
            client.addOptOut(null, TO_PHONE_NUMBER);
        });
    }

    @Test
    public void addOptOutWithNullToThrowsException() {
        OptOutsClient client = getOptOutsClient(null);

        assertThrows(NullPointerException.class, () -> {
            client.addOptOut(FROM_PHONE_NUMBER, (String) null);
        });
    }

    @Test
    public void checkOptOutWithNullFromThrowsException() {
        OptOutsClient client = getOptOutsClient(null);

        assertThrows(NullPointerException.class, () -> {
            client.checkOptOut(null, TO_PHONE_NUMBER);
        });
    }

    @Test
    public void removeOptOutWithNullFromThrowsException() {
        OptOutsClient client = getOptOutsClient(null);

        assertThrows(NullPointerException.class, () -> {
            client.removeOptOut(null, TO_PHONE_NUMBER);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void addOptOutWithInvalidFromNumber(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        assertThrows(HttpResponseException.class, () -> {
            client.addOptOut("invalid-number", TO_PHONE_NUMBER);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    public void checkOptOutWithInvalidFromNumber(HttpClient httpClient) {
        OptOutsClient client = getOptOutsClient(httpClient);

        assertThrows(HttpResponseException.class, () -> {
            client.checkOptOut("invalid-number", TO_PHONE_NUMBER);
        });
    }

    /**
     * Helper method to create OptOutsClient for testing.
     */
    private OptOutsClient getOptOutsClient(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = getTelcoMessagingClientBuilder(httpClient);
        return builder.buildClient().getOptOutsClient();
    }

    /**
     * Helper method to create TelcoMessagingClientBuilder with proper configuration.
     */
    private TelcoMessagingClientBuilder getTelcoMessagingClientBuilder(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder();
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
