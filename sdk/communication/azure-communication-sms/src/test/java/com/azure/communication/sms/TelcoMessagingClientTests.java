// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.communication.sms.implementation.models.DeliveryReport;
import com.azure.communication.sms.models.OptOutResult;
import com.azure.communication.sms.models.OptOutCheckResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TelcoMessagingClientTests extends SmsTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void telcoMessagingClientCreation(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        assertNotNull(client);
        assertNotNull(client.getSmsClient());
        assertNotNull(client.getDeliveryReportsClient());
        assertNotNull(client.getOptOutsClient());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void sendSmsViaOrganizedClient(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        var response = client.getSmsClient().send(FROM_PHONE_NUMBER, TO_PHONE_NUMBER, MESSAGE);

        assertNotNull(response);
        assertNotNull(response.getMessageId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void getDeliveryReportWithInvalidMessageId(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        // Test with invalid message ID - should get an error response
        assertThrows(HttpResponseException.class, () -> {
            client.getDeliveryReportsClient().getDeliveryReport("invalid-message-id");
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void getDeliveryReportWithResponse(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        // Test with invalid message ID to check response handling
        assertThrows(HttpResponseException.class, () -> {
            client.getDeliveryReportsClient().getDeliveryReportWithResponse("invalid-message-id", Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void addOptOut(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        List<OptOutResult> results = client.getOptOutsClient().addOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        OptOutResult result = results.get(0);
        assertNotNull(result.getTo());
        assertEquals(TO_PHONE_NUMBER, result.getTo());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void addOptOutMultiple(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();
        List<String> recipients = Arrays.asList(TO_PHONE_NUMBER, "+15551234568");

        List<OptOutResult> results = client.getOptOutsClient().addOptOut(FROM_PHONE_NUMBER, recipients);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void checkOptOut(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        List<OptOutCheckResult> results = client.getOptOutsClient().checkOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        OptOutCheckResult result = results.get(0);
        assertNotNull(result.getTo());
        assertEquals(TO_PHONE_NUMBER, result.getTo());
        // isOptedOut could be true, false, or null
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void removeOptOut(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        List<OptOutResult> results = client.getOptOutsClient().removeOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        OptOutResult result = results.get(0);
        assertNotNull(result.getTo());
        assertEquals(TO_PHONE_NUMBER, result.getTo());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void addOptOutWithResponse(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        Response<List<OptOutResult>> response = client.getOptOutsClient()
            .addOptOutWithResponse(FROM_PHONE_NUMBER, Arrays.asList(TO_PHONE_NUMBER), Context.NONE);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getValue());
        assertFalse(response.getValue().isEmpty());
    }

    @Test
    public void optOutWithNullFromThrowsException() {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(null).buildClient();

        assertThrows(NullPointerException.class, () -> {
            client.getOptOutsClient().addOptOut(null, TO_PHONE_NUMBER);
        });
    }

    @Test
    public void optOutWithNullToThrowsException() {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(null).buildClient();

        assertThrows(NullPointerException.class, () -> {
            client.getOptOutsClient().addOptOut(FROM_PHONE_NUMBER, (String) null);
        });
    }

    @Test
    public void deliveryReportWithNullMessageIdThrowsException() {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(null).buildClient();

        assertThrows(IllegalArgumentException.class, () -> {
            client.getDeliveryReportsClient().getDeliveryReport(null);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @RecordWithoutRequestBody
    @EnabledIf("shouldEnableSmsTests")
    public void testFullOptOutWorkflow(HttpClient httpClient) {
        TelcoMessagingClient client = getTelcoMessagingClientUsingConnectionString(httpClient).buildClient();

        // First, add opt-out
        List<OptOutResult> addResults = client.getOptOutsClient().addOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        assertNotNull(addResults);
        assertFalse(addResults.isEmpty());

        // Check opt-out status
        List<OptOutCheckResult> checkResults = client.getOptOutsClient().checkOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        assertNotNull(checkResults);
        assertFalse(checkResults.isEmpty());

        // Remove opt-out
        List<OptOutResult> removeResults = client.getOptOutsClient().removeOptOut(FROM_PHONE_NUMBER, TO_PHONE_NUMBER);
        assertNotNull(removeResults);
        assertFalse(removeResults.isEmpty());
    }

    protected TelcoMessagingClientBuilder getTelcoMessagingClientUsingConnectionString(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder();
        builder.connectionString(CONNECTION_STRING);

        if (httpClient != null) {
            builder.httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        }

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        addTestProxySanitizer();
        addTestProxyMatcher();
        return builder;
    }

    protected TelcoMessagingClientBuilder getTelcoMessagingClientWithToken(HttpClient httpClient) {
        TelcoMessagingClientBuilder builder = new TelcoMessagingClientBuilder();
        builder.endpoint(new com.azure.communication.common.implementation.CommunicationConnectionString(CONNECTION_STRING).getEndpoint())
            .credential(new AzureKeyCredential("test-key"))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        addTestProxySanitizer();
        addTestProxyMatcher();
        return builder;
    }
}
